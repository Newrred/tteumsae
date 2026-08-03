import { estimateRoute } from "./routing.js";

export function safetyLevel(marginMinutes) {
  if (marginMinutes >= 20) return "COMFORTABLE";
  if (marginMinutes >= 10) return "AVAILABLE";
  return "TIGHT";
}

function matchesCategories(criteria, place) {
  return (
    criteria.categories.length === 0 || criteria.categories.includes(place.category)
  );
}

export function selectRouteCandidates(criteria, places, limit = 20) {
  return places
    .filter((place) => matchesCategories(criteria, place))
    .map((place) => {
      const route = estimateRoute(
        criteria.start,
        criteria.destination,
        place,
        criteria.transport
      );
      return {
        place,
        estimatedTotalMinutes:
          route.firstLegMinutes +
          place.default_stay_minutes +
          route.secondLegMinutes,
        estimatedDetourMinutes: route.detourMinutes
      };
    })
    .sort(
      (left, right) =>
        left.estimatedDetourMinutes - right.estimatedDetourMinutes ||
        left.estimatedTotalMinutes - right.estimatedTotalMinutes
    )
    .slice(0, Math.max(1, limit))
    .map((item) => item.place);
}

export function recommendPlaces(criteria, places, routeProvider = estimateRoute) {
  return places
    .filter((place) => matchesCategories(criteria, place))
    .map((place) => {
      const route = routeProvider(
        criteria.start,
        criteria.destination,
        place,
        criteria.transport
      );
      if (!route) return null;
      const totalMinutes =
        route.firstLegMinutes + place.default_stay_minutes + route.secondLegMinutes;
      const marginMinutes = criteria.deadlineMinutes - totalMinutes;

      return {
        place,
        route,
        stayMinutes: place.default_stay_minutes,
        totalMinutes,
        marginMinutes,
        safetyLevel: safetyLevel(marginMinutes)
      };
    })
    .filter(Boolean)
    .filter((item) => item.marginMinutes >= criteria.safetyBufferMinutes)
    .sort(
      (left, right) =>
        right.marginMinutes - left.marginMinutes ||
        left.route.detourMinutes - right.route.detourMinutes
    );
}
