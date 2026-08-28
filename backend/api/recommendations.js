import { listPlaces } from "../lib/database.js";
import { integerEnv } from "../lib/env.js";
import { createDeadline, NETWORK_TIMEOUT_MS } from "../lib/fetch-policy.js";
import {
  badRequest,
  json,
  methodNotAllowed,
  rateLimit,
  readJson,
  serverError
} from "../lib/http.js";
import { fetchKakaoRoute, fetchKakaoRoutes } from "../lib/kakao-mobility.js";
import {
  createPathBounds,
  createSearchBounds,
  distanceToPathKm,
  estimateRoute
} from "../lib/routing.js";
import {
  MINIMUM_STAY_MINUTES,
  recommendPlaces,
  selectRouteCandidates
} from "../lib/time-safe.js";
import {
  ARRIVAL_DEADLINE_TIME_MODEL,
  parseRecommendationRequest
} from "../lib/validation.js";

export default {
  async fetch(request) {
    if (request.method !== "POST") return methodNotAllowed(["POST"]);
    const limited = rateLimit(request, "recommendations", 12);
    if (limited) return limited;

    const requestNow = new Date();
    let criteria;
    try {
      criteria = parseRecommendationRequest(await readJson(request), {
        nowEpochMillis: requestNow.getTime()
      });
    } catch (error) {
      return badRequest(
        error instanceof Error ? error.message : "요청 값이 올바르지 않습니다."
      );
    }

    const deadline = createDeadline(NETWORK_TIMEOUT_MS.RECOMMENDATION);
    try {
      let bounds = createSearchBounds(
        criteria.start,
        criteria.destination,
        criteria.transport
      );
      let baseRoute;
      let corridorRadiusMeters;
      if (criteria.transport === "CAR" && criteria.mode === "ON_THE_WAY") {
        baseRoute = await fetchKakaoRoute(
          criteria.start,
          criteria.destination,
          [],
          { signal: deadline.signal, now: requestNow }
        );
        if (!baseRoute) {
          throw new Error("Kakao Mobility could not calculate the base route");
        }
      }
      const baseRouteMinutes = baseRoute?.durationMinutes ??
        (criteria.mode === "NEARBY"
          ? 0
          : estimateRoute(
              criteria.start,
              criteria.destination,
              criteria.destination,
              criteria.transport
            ).directMinutes);
      const effectiveDeadlineMinutes = criteria.extraTimeMinutes == null
        ? criteria.deadlineMinutes
        : baseRouteMinutes + criteria.extraTimeMinutes;
      const effectiveCriteria = {
        ...criteria,
        deadlineMinutes: effectiveDeadlineMinutes
      };
      if (baseRoute) {
        corridorRadiusMeters = Math.min(
          8_000,
          Math.max(
            800,
            (effectiveDeadlineMinutes - baseRouteMinutes) * 20
          )
        );
        bounds = createPathBounds(
          baseRoute.path,
          corridorRadiusMeters / 1_000
        );
      }
      const boundedCandidates = await listPlaces({
        ...bounds,
        limit: 500,
        signal: deadline.signal
      });
      const candidates = corridorRadiusMeters
        ? boundedCandidates.filter((place) =>
            distanceToPathKm(place, baseRoute.path) <= corridorRadiusMeters / 1_000
          )
        : boundedCandidates;
      let recommendations;
      let routeProvider;
      let routeCandidateCount;
      let routeFailureCount = 0;
      let warning;

      if (criteria.transport === "CAR") {
        const routeLimit = Math.min(
          Math.max(integerEnv("KAKAO_ROUTE_CANDIDATE_LIMIT", 8), 1),
          8
        );
        const routeCandidates = selectRouteCandidates(
          effectiveCriteria,
          candidates,
          routeLimit,
          requestNow
        );
        const routeResult = await fetchKakaoRoutes(
          criteria.start,
          criteria.destination,
          routeCandidates,
          { baseRoute, signal: deadline.signal, now: requestNow }
        );
        routeCandidateCount = routeCandidates.length;
        routeFailureCount = routeResult.failedCount;
        routeProvider = "KAKAO_MOBILITY";
        recommendations = recommendPlaces(
          effectiveCriteria,
          routeCandidates,
          (_start, _destination, place) =>
            routeResult.routes.get(String(place.content_id)),
          requestNow
        ).slice(0, 20);
      } else {
        routeProvider = "ESTIMATE";
        routeCandidateCount = candidates.length;
        warning =
          "도보 이동시간은 직선거리 기반 예상값이에요. 실제 길과 신호에 따라 더 오래 걸릴 수 있으니 여유 있게 출발해 주세요.";
        recommendations = recommendPlaces(
          effectiveCriteria,
          candidates,
          estimateRoute,
          requestNow
        ).slice(0, 20);
      }

      return json({
        ...(baseRoute ? { baseRoute } : {}),
        data: recommendations,
        meta: {
          candidateCount: boundedCandidates.length,
          corridorCandidateCount: candidates.length,
          routeCandidateCount,
          routeFailureCount,
          recommendationCount: recommendations.length,
          routeProvider,
          baseRouteMinutes,
          ...(criteria.timeModel === ARRIVAL_DEADLINE_TIME_MODEL
            ? {
                timeModel: ARRIVAL_DEADLINE_TIME_MODEL,
                calculatedAtEpochMillis: requestNow.getTime(),
                arrivalDeadlineEpochMillis: criteria.arrivalDeadlineEpochMillis,
                minimumStayMinutes: MINIMUM_STAY_MINUTES
              }
            : {}),
          ...(criteria.extraTimeMinutes == null
            ? {}
            : { extraTimeMinutes: criteria.extraTimeMinutes }),
          effectiveDeadlineMinutes,
          safetyBufferMinutes: criteria.safetyBufferMinutes,
          ...(baseRoute ? { baseRoute } : {}),
          ...(corridorRadiusMeters ? { corridorRadiusMeters } : {}),
          ...(warning ? { warning } : {})
        }
      });
    } catch (error) {
      return serverError(error);
    } finally {
      deadline.dispose();
    }
  }
};
