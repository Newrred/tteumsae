import {
  listPlacesForIntroSync,
  recordPlaceEnrichmentFailure,
  savePlaceIntro
} from "../../lib/database.js";
import { integerEnv, requiredEnv } from "../../lib/env.js";
import { createDeadline, NETWORK_TIMEOUT_MS } from "../../lib/fetch-policy.js";
import { json, methodNotAllowed, serverError, unauthorized } from "../../lib/http.js";
import { runWithSyncLease } from "../../lib/sync-lease.js";
import { fetchTourIntro } from "../../lib/tour-api.js";
import { runIntroBatch } from "../../lib/tour-sync.js";

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

export default createTourIntroSyncHandler();
