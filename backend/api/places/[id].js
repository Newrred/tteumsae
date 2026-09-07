import {
  getPlace,
  getPlaceCongestionForecast
} from "../../lib/database.js";
import { badRequest, json, methodNotAllowed, notFound, serverError } from "../../lib/http.js";
import { kstUsageDate } from "../../lib/provider-usage.js";
import { publicCongestionForecast } from "../../lib/tour-congestion.js";

function requestedForecastDate(url) {
  const raw = url.searchParams.get("atEpochMillis");
  if (raw == null) return kstUsageDate();
  if (!/^[0-9]{1,16}$/.test(raw)) return null;
  const epoch = Number(raw);
  const date = new Date(epoch);
  return Number.isSafeInteger(epoch) && epoch > 0 && !Number.isNaN(date.getTime())
    ? kstUsageDate(date)
    : null;
}

export default {
  async fetch(request) {
    if (request.method !== "GET") return methodNotAllowed(["GET"]);

    try {
      const url = new URL(request.url);
      const id = decodeURIComponent(url.pathname.split("/").pop() ?? "");
      if (!id) return notFound();
      const forecastDate = requestedForecastDate(url);
      if (!forecastDate) return badRequest("atEpochMillis 값이 올바르지 않습니다.");
      const place = await getPlace(id);
      if (!place) return notFound("장소를 찾을 수 없습니다.");
      const forecast = await getPlaceCongestionForecast(id, forecastDate);
      return json({
        data: {
          ...place,
          ...(forecast
            ? { congestion_forecast: publicCongestionForecast(forecast) }
            : {})
        }
      });
    } catch (error) {
      return serverError(error);
    }
  }
};

