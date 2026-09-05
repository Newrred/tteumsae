import {
  listPlacesForPresentationSync,
  listPlacesForIntroSync,
  recordPlaceEnrichmentFailure,
  savePlaceCommon,
  savePlaceMedia,
  savePlaceIntro
} from "../../lib/database.js";
import { integerEnv, requiredEnv } from "../../lib/env.js";
import { createDeadline, NETWORK_TIMEOUT_MS } from "../../lib/fetch-policy.js";
import { json, methodNotAllowed, serverError, unauthorized } from "../../lib/http.js";
import { runWithSyncLease } from "../../lib/sync-lease.js";
import {
  fetchPetTourDetail,
  fetchTourCommon,
  fetchTourImages,
  fetchTourIntro
} from "../../lib/tour-api.js";
import { runIntroBatch, runPresentationBatch } from "../../lib/tour-sync.js";

const defaultDependencies = {
  listPlaces: listPlacesForIntroSync,
  fetchIntro: fetchTourIntro,
  saveIntro: savePlaceIntro,
  recordFailure: recordPlaceEnrichmentFailure,
  runBatch: runIntroBatch,
  withLease: runWithSyncLease,
  deadlineFactory: createDeadline,
  now: () => new Date()
};

const presentationDefaultDependencies = {
  listPlaces: listPlacesForPresentationSync,
  fetchCommon: fetchTourCommon,
  fetchImages: fetchTourImages,
  fetchPet: fetchPetTourDetail,
  saveCommon: savePlaceCommon,
  saveMedia: savePlaceMedia,
  recordFailure: recordPlaceEnrichmentFailure,
  runBatch: runPresentationBatch,
  withLease: runWithSyncLease,
  deadlineFactory: createDeadline,
  now: () => new Date()
};

const emptyPresentationResult = {
  status: "idle",
  processed: 0,
  deferred: 0,
  completed: 0,
  partial: 0,
  failed: 0,
  commonUpdated: 0,
  commonEmpty: 0,
  commonFailed: 0,
  mediaUpdated: 0,
  mediaEmpty: 0,
  mediaFailed: 0
};

export function createTourIntroSyncHandler(dependencies = {}) {
  const deps = { ...defaultDependencies, ...dependencies };
  return {
    async fetch(request) {
      if (request.method !== "GET") return methodNotAllowed(["GET"]);
      try {
        if (request.headers.get("authorization") !== `Bearer ${requiredEnv("CRON_SECRET")}`) {
          return unauthorized();
        }

        const result = await deps.withLease({
          jobId: "tour_intro",
          run: async () => {
            const deadline = deps.deadlineFactory(NETWORK_TIMEOUT_MS.CRON);
            try {
              const now = deps.now();
              const batchSize = Math.min(integerEnv("TOUR_INTRO_SYNC_BATCH_SIZE", 20), 40);
              const concurrency = Math.min(integerEnv("TOUR_SYNC_CONCURRENCY", 4), 4);
              const places = await deps.listPlaces({ limit: batchSize, now, signal: deadline.signal });
              if (places.length === 0) {
                return {
                  status: "idle",
                  processed: 0,
                  deferred: 0,
                  updated: 0,
                  empty: 0,
                  failed: 0
                };
              }

              const counts = await deps.runBatch({
                places,
                fetchIntro: deps.fetchIntro,
                saveIntro: deps.saveIntro,
                recordFailure: deps.recordFailure,
                concurrency,
                syncedAt: now.toISOString(),
                signal: deadline.signal,
                canStart: () => deadline.canStart(5_000)
              });
              return { status: counts.deferred > 0 ? "partial" : "completed", ...counts };
            } finally {
              deadline.dispose();
            }
          }
        });
        return json(result);
      } catch (error) {
        return serverError(error);
      }
    }
  };
}

export function createTourPresentationSyncHandler(dependencies = {}) {
  const deps = { ...presentationDefaultDependencies, ...dependencies };
  return {
    async fetch(request) {
      if (request.method !== "GET") return methodNotAllowed(["GET"]);
      try {
        if (request.headers.get("authorization") !== `Bearer ${requiredEnv("CRON_SECRET")}`) {
          return unauthorized();
        }

        const result = await deps.withLease({
          jobId: "tour_presentation",
          run: async () => {
            const deadline = deps.deadlineFactory(NETWORK_TIMEOUT_MS.CRON);
            try {
              const now = deps.now();
              const batchSize = Math.min(
                integerEnv("TOUR_PRESENTATION_SYNC_BATCH_SIZE", 5),
                10
              );
              const concurrency = Math.min(integerEnv("TOUR_SYNC_CONCURRENCY", 4), 4);
              const places = await deps.listPlaces({
                limit: batchSize,
                now,
                signal: deadline.signal
              });
              if (places.length === 0) return { ...emptyPresentationResult };

              const counts = await deps.runBatch({
                places,
                fetchCommon: deps.fetchCommon,
                fetchImages: deps.fetchImages,
                fetchPet: deps.fetchPet,
                saveCommon: deps.saveCommon,
                saveMedia: deps.saveMedia,
                recordFailure: deps.recordFailure,
                concurrency,
                syncedAt: now.toISOString(),
                signal: deadline.signal,
                canStart: () => deadline.canStart(20_000)
              });
              const incomplete = counts.deferred > 0 || counts.partial > 0 || counts.failed > 0;
              return { status: incomplete ? "partial" : "completed", ...counts };
            } finally {
              deadline.dispose();
            }
          }
        });
        return json(result);
      } catch (error) {
        return serverError(error);
      }
    }
  };
}

export function createTourEnrichmentSyncHandler({
  introHandler = createTourIntroSyncHandler(),
  presentationHandler = createTourPresentationSyncHandler()
} = {}) {
  return {
    fetch(request) {
      const stage = new URL(request.url).searchParams.get("stage");
      return stage === "presentation"
        ? presentationHandler.fetch(request)
        : introHandler.fetch(request);
    }
  };
}

export default createTourEnrichmentSyncHandler();
