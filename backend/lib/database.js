import { requiredEnv } from "./env.js";
import { supabaseApiHeaders } from "./supabase-auth.js";

const basePublicColumns = [
  "content_id",
  "source",
  "name",
  "category",
  "content_type_id",
  "area_code",
  "sigungu_code",
  "latitude",
  "longitude",
  "address",
  "image_url",
  "tel",
  "default_stay_minutes"
];

const enrichmentPublicColumns = [
  "cat1",
  "cat2",
  "cat3",
  "opening_hours",
  "closed_days",
  "event_start_date",
  "event_end_date",
  "overview",
  "homepage_url",
  "image_urls",
  "tags"
];

const publicColumns = [...basePublicColumns, ...enrichmentPublicColumns].join(",");

const legacyPublicColumns = [...basePublicColumns, "raw"].join(",");

function toPublicPlace(row) {
  const legacy = row.raw?._tteumsae ?? {};
  const { raw: _raw, ...place } = row;
  const imageUrls = Array.isArray(place.image_urls)
    ? place.image_urls
    : Array.isArray(legacy.imageUrls)
      ? legacy.imageUrls
      : [];
  const tags = Array.isArray(place.tags)
    ? place.tags
    : Array.isArray(legacy.tags)
      ? legacy.tags
      : [];
  return {
    ...place,
    cat1: place.cat1 ?? row.raw?.cat1 ?? null,
    cat2: place.cat2 ?? row.raw?.cat2 ?? null,
    cat3: place.cat3 ?? row.raw?.cat3 ?? null,
    image_url: place.image_url || imageUrls[0] || null,
    image_urls: imageUrls,
    tags,
    opening_hours: place.opening_hours ?? legacy.openingHours ?? null,
    closed_days: place.closed_days ?? legacy.closedDays ?? null
  };
}

async function databaseRequest(path, { method = "GET", body, prefer } = {}) {
  const baseUrl = requiredEnv("SUPABASE_URL").replace(/\/$/, "");
  const serviceKey = requiredEnv("SUPABASE_SERVICE_ROLE_KEY");
  const headers = supabaseApiHeaders(serviceKey, serviceKey, {
    "content-type": "application/json"
  });
  if (prefer) headers.prefer = prefer;

  const response = await fetch(`${baseUrl}/rest/v1/${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  if (!response.ok) {
    const detail = await response.text();
    const error = new Error(`Database request failed (${response.status}): ${detail.slice(0, 300)}`);
    error.status = response.status;
    try {
      const databaseError = JSON.parse(detail);
      error.code = databaseError.code;
      error.databaseMessage = databaseError.message;
    } catch {
      error.code = null;
      error.databaseMessage = null;
    }
    throw error;
  }
  if (response.status === 204) return null;
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

async function requestPublicPlaceRows(query) {
  try {
    return await databaseRequest(`places?${query}`);
  } catch (error) {
    const missingEnrichmentColumn =
      error.code === "42703" &&
      enrichmentPublicColumns.some((column) => error.databaseMessage?.includes(`places.${column}`));
    if (!missingEnrichmentColumn) throw error;
    query.set("select", legacyPublicColumns);
    return databaseRequest(`places?${query}`);
  }
}

export async function listPlaces({
  limit = 30,
  offset = 0,
  category,
  sigunguCode,
  minLatitude,
  maxLatitude,
  minLongitude,
  maxLongitude
} = {}) {
  const query = new URLSearchParams({
    select: publicColumns,
    is_active: "eq.true",
    order: "name.asc",
    limit: String(Math.min(Math.max(limit, 1), 500)),
    offset: String(Math.max(offset, 0))
  });
  if (category) query.set("category", `eq.${category}`);
  if (Number.isInteger(sigunguCode)) query.set("sigungu_code", `eq.${sigunguCode}`);
  if (Number.isFinite(minLatitude)) query.set("latitude", `gte.${minLatitude}`);
  if (Number.isFinite(maxLatitude)) query.append("latitude", `lte.${maxLatitude}`);
  if (Number.isFinite(minLongitude)) query.set("longitude", `gte.${minLongitude}`);
  if (Number.isFinite(maxLongitude)) query.append("longitude", `lte.${maxLongitude}`);

  const rows = await requestPublicPlaceRows(query);
  return rows.map(toPublicPlace);
}

export async function getPlace(contentId) {
  const query = new URLSearchParams({
    select: publicColumns,
    content_id: `eq.${contentId}`,
    is_active: "eq.true",
    limit: "1"
  });
  const rows = await requestPublicPlaceRows(query);
  return rows?.[0] ? toPublicPlace(rows[0]) : null;
}

export async function upsertPlaces(rows) {
  if (rows.length === 0) return;
  const chunkSize = 100;
  for (let index = 0; index < rows.length; index += chunkSize) {
    const chunk = rows.slice(index, index + chunkSize);
    await databaseRequest("places?on_conflict=content_id", {
      method: "POST",
      body: chunk,
      prefer: "resolution=merge-duplicates,return=minimal"
    });
  }
}

export async function listPlacesForEnrichment({ limit = 5, offset = 0 } = {}) {
  const query = new URLSearchParams({
    select: "content_id,content_type_id,image_url,raw",
    is_active: "eq.true",
    order: "content_id.asc",
    limit: String(Math.min(Math.max(limit, 1), 20)),
    offset: String(Math.max(offset, 0))
  });
  return databaseRequest(`places?${query}`);
}

export async function updatePlaceEnrichment(place, enrichment) {
  await databaseRequest(`places?content_id=eq.${encodeURIComponent(place.content_id)}`, {
    method: "PATCH",
    body: {
      image_url: place.image_url || enrichment.imageUrls[0] || null,
      raw: {
        ...(place.raw ?? {}),
        _tteumsae: enrichment
      }
    },
    prefer: "return=minimal"
  });
}

const introOwnedTags = new Set(["주차 가능", "아이 동반"]);

export async function listPlacesForIntroSync({ limit = 20, now = new Date() } = {}) {
  const dueAt = now instanceof Date ? now : new Date(now);
  if (Number.isNaN(dueAt.getTime())) throw new Error("Invalid intro sync reference time");
  const query = new URLSearchParams({
    select: "content_id,content_type_id,tags,enrichment_raw,enrichment_attempts",
    is_active: "eq.true",
    intro_synced_at: "is.null",
    or: `(next_enrichment_at.is.null,next_enrichment_at.lte.${dueAt.toISOString()})`,
    order: "next_enrichment_at.asc.nullsfirst,content_id.asc",
    limit: String(Math.min(Math.max(Number.parseInt(limit, 10) || 1, 1), 40))
  });
  return databaseRequest(`places?${query}`);
}

export async function savePlaceIntro(place, enrichment) {
  const body = {
    enrichment_raw: {
      ...(place.enrichment_raw ?? {}),
      intro: enrichment.intro
    },
    intro_synced_at: enrichment.syncedAt,
    enrichment_attempts: 0,
    enrichment_last_error: null,
    next_enrichment_at: null
  };

  if (enrichment.intro !== null) {
    if (enrichment.openingHours != null) body.opening_hours = enrichment.openingHours;
    if (enrichment.closedDays != null) body.closed_days = enrichment.closedDays;
    if (enrichment.eventStartDate != null) body.event_start_date = enrichment.eventStartDate;
    if (enrichment.eventEndDate != null) body.event_end_date = enrichment.eventEndDate;
    if (Array.isArray(enrichment.tags) && enrichment.tags.length > 0) {
      const retainedTags = (Array.isArray(place.tags) ? place.tags : [])
        .filter((tag) => !introOwnedTags.has(tag));
      body.tags = [...new Set([...retainedTags, ...enrichment.tags])];
    }
  }

  await databaseRequest(`places?content_id=eq.${encodeURIComponent(place.content_id)}`, {
    method: "PATCH",
    body,
    prefer: "return=minimal"
  });
}

export async function recordPlaceEnrichmentFailure(place, error, now = new Date()) {
  const failedAt = now instanceof Date ? now : new Date(now);
  if (Number.isNaN(failedAt.getTime())) throw new Error("Invalid enrichment failure time");
  const attempts = Math.max(Number.parseInt(place.enrichment_attempts, 10) || 0, 0) + 1;
  const exponent = Math.min(attempts - 1, 16);
  const delayMinutes = Math.min(24 * 60, 15 * (2 ** exponent));
  const nextAttemptAt = new Date(failedAt.getTime() + delayMinutes * 60_000);
  const message = (error instanceof Error ? error.message : String(error)).slice(0, 300);

  await databaseRequest(`places?content_id=eq.${encodeURIComponent(place.content_id)}`, {
    method: "PATCH",
    body: {
      enrichment_attempts: attempts,
      enrichment_last_error: message,
      next_enrichment_at: nextAttemptAt.toISOString()
    },
    prefer: "return=minimal"
  });
}

export async function getSyncState(id = "tour_api") {
  const query = new URLSearchParams({
    select: "*",
    id: `eq.${id}`,
    limit: "1"
  });
  const rows = await databaseRequest(`sync_state?${query}`);
  return (
    rows?.[0] ?? {
      id,
      next_page: 1,
      total_count: 0,
      last_processed_page: 0,
      last_item_count: 0
    }
  );
}

export async function saveSyncState(state) {
  await databaseRequest("sync_state?on_conflict=id", {
    method: "POST",
    body: {
      id: state.id ?? "tour_api",
      ...state,
      updated_at: new Date().toISOString()
    },
    prefer: "resolution=merge-duplicates,return=minimal"
  });
}
