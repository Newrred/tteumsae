import {
  recordProviderUsageResult,
  reserveProviderUsage
} from "./database.js";
import { integerEnv } from "./env.js";

const KST_OFFSET_MS = 9 * 60 * 60 * 1_000;

export const KAKAO_MOBILITY_OFFICIAL_DAILY_QUOTA = 10_000;
export const KAKAO_MOBILITY_DEFAULT_DAILY_BUDGET = 8_000;
export const KAKAO_MOBILITY_DEFAULT_WARNING = 7_000;

function validDate(value) {
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) throw new Error("Invalid provider usage time");
  return date;
}

export function kstUsageDate(value = new Date()) {
  const date = validDate(value);
  const shifted = new Date(date.getTime() + KST_OFFSET_MS);
  return [
    shifted.getUTCFullYear(),
    String(shifted.getUTCMonth() + 1).padStart(2, "0"),
    String(shifted.getUTCDate()).padStart(2, "0")
  ].join("-");
}

export function secondsUntilNextKstMidnight(value = new Date()) {
  const date = validDate(value);
  const shifted = new Date(date.getTime() + KST_OFFSET_MS);
  const nextMidnightUtc = Date.UTC(
    shifted.getUTCFullYear(),
    shifted.getUTCMonth(),
    shifted.getUTCDate() + 1
  ) - KST_OFFSET_MS;
  return Math.max(1, Math.ceil((nextMidnightUtc - date.getTime()) / 1_000));
}

export function mobilityBudgetPolicy() {
  const budgetLimit = Math.min(
    integerEnv(
      "KAKAO_MOBILITY_DAILY_BUDGET",
      KAKAO_MOBILITY_DEFAULT_DAILY_BUDGET
    ),
    KAKAO_MOBILITY_OFFICIAL_DAILY_QUOTA
  );
  const warningThreshold = Math.min(
    integerEnv(
      "KAKAO_MOBILITY_DAILY_WARNING",
      KAKAO_MOBILITY_DEFAULT_WARNING
    ),
    budgetLimit
  );
  return { budgetLimit, warningThreshold };
}

export class ProviderBudgetExhaustedError extends Error {
  constructor(provider, operation, retryAfterSeconds) {
    super("외부 경로 서비스의 오늘 사용 예산에 도달했습니다.");
    this.name = "ProviderBudgetExhaustedError";
    this.code = "UPSTREAM_BUDGET_EXHAUSTED";
    this.provider = provider;
    this.operation = operation;
    this.retryAfterSeconds = retryAfterSeconds;
  }
}

export class ProviderResponseError extends Error {
  constructor(provider, status, providerCode = null, { retryAfterSeconds } = {}) {
    super("외부 서비스가 요청을 처리하지 못했습니다.");
    this.name = "ProviderResponseError";
    this.provider = provider;
    this.status = status;
    this.providerCode = providerCode;
    this.retryAfterSeconds = retryAfterSeconds;
    this.code = status === 429 || Number(providerCode) === -10 || String(providerCode) === "22"
      ? "UPSTREAM_QUOTA_EXHAUSTED"
      : "UPSTREAM_ERROR";
  }
}

export function classifyProviderResult(error) {
  if (
    error?.code === "UPSTREAM_QUOTA_EXHAUSTED" ||
    error?.status === 429 ||
    Number(error?.providerCode) === -10 ||
    String(error?.providerCode) === "22"
  ) {
    return "quota";
  }
  if (error?.code === "UPSTREAM_TIMEOUT") return "timeout";
  if (Number.isInteger(error?.status) && error.status >= 500 && error.status <= 599) {
    return "server_error";
  }
  return "other_error";
}

function currentTime(now) {
  return validDate(typeof now === "function" ? now() : (now ?? new Date()));
}

async function recordBestEffort(record, input) {
  try {
    await record(input);
  } catch {
    console.error("Provider usage result could not be recorded");
  }
}

export async function trackProviderCall({
  provider,
  operation,
  budgetLimit = null,
  units = 1,
  signal,
  call,
  now,
  reserve = reserveProviderUsage,
  record = recordProviderUsageResult
}) {
  const requestedAt = currentTime(now);
  const usageDate = kstUsageDate(requestedAt);
  const reservation = await reserve({
    provider,
    operation,
    usageDate,
    budgetLimit,
    units,
    signal
  });

  if (!reservation.allowed) {
    throw new ProviderBudgetExhaustedError(
      provider,
      operation,
      secondsUntilNextKstMidnight(requestedAt)
    );
  }

  const resultInput = {
    provider,
    operation,
    usageDate,
    units,
    signal
  };
  try {
    const value = await call();
    await recordBestEffort(record, { ...resultInput, resultKind: "success" });
    return value;
  } catch (error) {
    await recordBestEffort(record, {
      ...resultInput,
      resultKind: classifyProviderResult(error)
    });
    throw error;
  }
}
