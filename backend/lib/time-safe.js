import { estimateRoute } from "./routing.js";
import { evaluateOperatingWindow } from "./operating-hours.js";
import { isFestivalVisitEligible } from "./festival-eligibility.js";

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

export function operationStatus(place, arrival = new Date()) {
  const instant = arrival instanceof Date ? arrival : new Date(arrival);
  return evaluateOperatingWindow(place, {
    arrival: instant,
    departure: new Date(instant.getTime() + 1),
    timeZone: "Asia/Seoul"
  }).status;
}

export function selectRouteCandidates(
  criteria,
  places,
  limit = 20,
  now = new Date()
) {
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
        eligible: isFestivalVisitEligible(
          place,
          new Date(now.getTime() + route.firstLegMinutes * 60_000)
        ),
        estimatedTotalMinutes:
          route.firstLegMinutes +
          place.default_stay_minutes +
          route.secondLegMinutes,
        estimatedDetourMinutes: route.detourMinutes
      };
    })
    .filter((item) => item.eligible)
    .sort(
      (left, right) =>
        left.estimatedDetourMinutes - right.estimatedDetourMinutes ||
        left.estimatedTotalMinutes - right.estimatedTotalMinutes
    )
    .slice(0, Math.max(1, limit))
    .map((item) => item.place);
}

export function recommendPlaces(
  criteria,
  places,
  routeProvider = estimateRoute,
  now = new Date()
) {
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
      const arrival = new Date(now.getTime() + route.firstLegMinutes * 60_000);
      if (!isFestivalVisitEligible(place, arrival)) return null;
      const departure = new Date(
        arrival.getTime() + Math.max(0, place.default_stay_minutes) * 60_000
      );
      const status = evaluateOperatingWindow(place, {
        arrival,
        departure,
        timeZone: "Asia/Seoul"
      }).status;
      if (status === "CLOSED") return null;
      const totalMinutes =
        route.firstLegMinutes + place.default_stay_minutes + route.secondLegMinutes;
      const marginMinutes = criteria.deadlineMinutes - totalMinutes;

      return {
        place,
        route,
        stayMinutes: place.default_stay_minutes,
        totalMinutes,
        marginMinutes,
        operationStatus: status,
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
