import {
  badRequest,
  json,
  methodNotAllowed,
  readJson,
  serverError
} from "../lib/http.js";
import { fetchKakaoRoute } from "../lib/kakao-mobility.js";
import { parseRouteRequest } from "../lib/validation.js";

export default {
  async fetch(request) {
    if (request.method !== "POST") return methodNotAllowed(["POST"]);

    let routeRequest;
    try {
      routeRequest = parseRouteRequest(await readJson(request));
    } catch (error) {
      return badRequest(
        error instanceof Error ? error.message : "요청 값이 올바르지 않습니다."
      );
    }

    try {
      const route = await fetchKakaoRoute(
        routeRequest.start,
        routeRequest.destination,
        routeRequest.waypoints
      );
      if (!route) throw new Error("Kakao Mobility could not calculate the route");
      return json({ data: route });
    } catch (error) {
      return serverError(error);
    }
  }
};
