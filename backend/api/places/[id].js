import {
  getPlace,
  getPlaceCongestionForecast,
  getWeatherForecastCache,
  upsertWeatherForecastCache
} from "../../lib/database.js";
import { badRequest, json, methodNotAllowed, notFound, serverError } from "../../lib/http.js";
import { kstUsageDate } from "../../lib/provider-usage.js";
import { publicCongestionForecast } from "../../lib/tour-congestion.js";
import { resolvePlaceWeather } from "../../lib/kma-weather.js";

function requestedForecastInstant(url) {
  const raw = url.searchParams.get("atEpochMillis");
  if (raw == null) return new Date();
  if (!/^[0-9]{1,16}$/.test(raw)) return null;
  const epoch = Number(raw);
  const date = new Date(epoch);
  return Number.isSafeInteger(epoch) && epoch > 0 && !Number.isNaN(date.getTime())
    ? date
    : null;
}

async function defaultWeather(place, arrivalAt) {
  return resolvePlaceWeather(place, arrivalAt, {
    loadCache: getWeatherForecastCache,
    saveCache: upsertWeatherForecastCache
  });
}

const defaultDependencies = {
  getPlace,
  getCongestion: getPlaceCongestionForecast,
  getWeather: defaultWeather
};

export function createPlaceHandler(dependencies = {}) {
  const deps = { ...defaultDependencies, ...dependencies };
  return { async fetch(request) {
    if (request.method !== "GET") return methodNotAllowed(["GET"]);

    try {
      const url = new URL(request.url);
      const id = decodeURIComponent(url.pathname.split("/").pop() ?? "");
      if (!id) return notFound();
      const forecastInstant = requestedForecastInstant(url);
      if (!forecastInstant) return badRequest("atEpochMillis 값이 올바르지 않습니다.");
      const place = await deps.getPlace(id);
      if (!place) return notFound("장소를 찾을 수 없습니다.");
      const forecast = await deps.getCongestion(id, kstUsageDate(forecastInstant));
      let weather = null;
      if (process.env.KMA_WEATHER_ENABLED?.trim().toLowerCase() === "true") {
        try {
          weather = await deps.getWeather(place, forecastInstant);
        } catch {
          console.error("Weather forecast could not be loaded");
        }
      }
      return json({
        data: {
          ...place,
          ...(forecast
            ? { congestion_forecast: publicCongestionForecast(forecast) }
            : {}),
          ...(weather ? { weather_forecast: weather } : {})
        }
      });
    } catch (error) {
      return serverError(error);
    }
  } };
}

export default createPlaceHandler();

