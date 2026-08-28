import { requiredEnv } from "./env.js";
import { fetchWithTimeout, NETWORK_TIMEOUT_MS } from "./fetch-policy.js";
import {
  createProviderResponseError,
  trackProviderCall
} from "./provider-usage.js";

const keywordSearchUrl = "https://dapi.kakao.com/v2/local/search/keyword.json";
const regionSearchUrl = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

async function fetchKakaoLocalPayload(
  url,
  apiKey,
  operation,
  { signal, fetchImpl = fetch, usageTracker = trackProviderCall, now } = {}
) {
  return usageTracker({
    provider: "KAKAO_LOCAL",
    operation,
    budgetLimit: null,
    signal,
    now,
    call: async () => {
      const response = await fetchWithTimeout(url, {
        headers: { authorization: `KakaoAK ${apiKey}` }
      }, {
        provider: "KAKAO_LOCAL",
        timeoutMs: NETWORK_TIMEOUT_MS.KAKAO_LOCAL,
        signal,
        fetchImpl
      });
      if (!response.ok) {
        throw await createProviderResponseError(response, "KAKAO_LOCAL", { now });
      }
      return response.json();
    }
  });
}

export function parseKakaoPlaces(payload) {
  if (!Array.isArray(payload?.documents)) return [];

  return payload.documents
    .map((place) => ({
      id: String(place.id ?? ""),
      name: String(place.place_name ?? ""),
      address: String(place.road_address_name || place.address_name || ""),
      category: String(place.category_name ?? ""),
      latitude: Number.parseFloat(place.y),
      longitude: Number.parseFloat(place.x),
      kakaoMapUrl: String(place.place_url ?? "")
    }))
    .filter(
      (place) =>
        place.id &&
        place.name &&
        Number.isFinite(place.latitude) &&
        Number.isFinite(place.longitude)
    );
}

export async function searchKakaoPlaces(
  query,
  {
    latitude,
    longitude,
    apiKey = requiredEnv("KAKAO_REST_API_KEY"),
    signal,
    fetchImpl = fetch,
    usageTracker = trackProviderCall,
    now
  } = {}
) {
  const parameters = new URLSearchParams({
    query,
    size: "10",
    sort: Number.isFinite(latitude) && Number.isFinite(longitude)
      ? "distance"
      : "accuracy"
  });
  if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
    parameters.set("x", String(longitude));
    parameters.set("y", String(latitude));
    parameters.set("radius", "20000");
  }

  const payload = await fetchKakaoLocalPayload(
    `${keywordSearchUrl}?${parameters}`,
    apiKey,
    "KEYWORD_SEARCH",
    { signal, fetchImpl, usageTracker, now }
  );
  return parseKakaoPlaces(payload);
}

export function parseKakaoRegion(payload) {
  const region = payload?.documents?.find((item) => item.region_type === "H") ??
    payload?.documents?.[0];
  if (!region) return null;

  const province = String(region.region_1depth_name ?? "");
  return {
    province,
    address: String(region.address_name ?? ""),
    isGangwon: province.startsWith("강원")
  };
}

export async function lookupKakaoRegion(
  latitude,
  longitude,
  {
    apiKey = requiredEnv("KAKAO_REST_API_KEY"),
    signal,
    fetchImpl = fetch,
    usageTracker = trackProviderCall,
    now
  } = {}
) {
  const parameters = new URLSearchParams({
    x: String(longitude),
    y: String(latitude)
  });
  const payload = await fetchKakaoLocalPayload(
    `${regionSearchUrl}?${parameters}`,
    apiKey,
    "REGION",
    { signal, fetchImpl, usageTracker, now }
  );
  return parseKakaoRegion(payload);
}
