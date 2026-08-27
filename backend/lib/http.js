const baseHeaders = {
  "content-type": "application/json; charset=utf-8",
  "cache-control": "no-store"
};

const rateWindows = new Map();

export function json(data, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...baseHeaders, ...extraHeaders }
  });
}

export function emptyResponse(status, extraHeaders = {}) {
  return new Response(null, {
    status,
    headers: { "cache-control": "no-store", ...extraHeaders }
  });
}

export function methodNotAllowed(allowed) {
  return json(
    { error: { code: "METHOD_NOT_ALLOWED", message: "지원하지 않는 요청 방식입니다." } },
    405,
    { allow: allowed.join(", ") }
  );
}

export function rateLimit(request, bucket, limit, windowMs = 60_000) {
  const address = request.headers.get("x-forwarded-for")?.split(",")[0].trim() ||
    request.headers.get("x-real-ip")?.trim();
  if (!address) return null;

  const now = Date.now();
  const key = `${bucket}:${address}`;
  const current = rateWindows.get(key);
  if (!current || current.resetAt <= now) {
    rateWindows.set(key, { count: 1, resetAt: now + windowMs });
    // ponytail: per-instance memory is a best-effort serverless guard; use a shared store if abuse becomes distributed.
    if (rateWindows.size > 1_000) rateWindows.delete(rateWindows.keys().next().value);
    return null;
  }
  if (current.count < limit) {
    current.count += 1;
    return null;
  }

  const retryAfterSeconds = Math.max(1, Math.ceil((current.resetAt - now) / 1_000));
  return json(
    {
      error: {
        code: "RATE_LIMITED",
        message: "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
      }
    },
    429,
    { "retry-after": String(retryAfterSeconds) }
  );
}

export function badRequest(message, details) {
  return json({ error: { code: "BAD_REQUEST", message, details } }, 400);
}

export function notFound(message = "요청한 데이터를 찾을 수 없습니다.") {
  return json({ error: { code: "NOT_FOUND", message } }, 404);
}

export function unauthorized() {
  return json(
    { error: { code: "UNAUTHORIZED", message: "인증되지 않은 요청입니다." } },
    401
  );
}

export function serverError(error) {
  const requestId = crypto.randomUUID();
  const message = error instanceof Error ? error.message : String(error);
  console.error(`[${requestId}] ${message}`);
  return json(
    {
      error: {
        code: "INTERNAL_ERROR",
        message: "서버 처리 중 오류가 발생했습니다.",
        requestId
      }
    },
    500
  );
}

export async function readJson(request) {
  const contentType = request.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    throw new Error("Content-Type must be application/json");
  }
  return request.json();
}
