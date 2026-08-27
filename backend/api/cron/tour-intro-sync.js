import {
  listPlacesForIntroSync,
  recordPlaceEnrichmentFailure,
  savePlaceIntro
} from "../../lib/database.js";
import { integerEnv, requiredEnv } from "../../lib/env.js";
import { json, methodNotAllowed, serverError, unauthorized } from "../../lib/http.js";
import { fetchTourIntro } from "../../lib/tour-api.js";
import { runIntroBatch } from "../../lib/tour-sync.js";

const defaultDependencies = {
  listPlaces: listPlacesForIntroSync,
  fetchIntro: fetchTourIntro,
  saveIntro: savePlaceIntro,
  recordFailure: recordPlaceEnrichmentFailure,
  runBatch: runIntroBatch
};

export function createTourIntroSyncHandler(dependencies = {}) {
  const deps = { ...defaultDependencies, ...dependencies };
  return {
    async fetch(request) {
      if (request.method !== "GET") return methodNotAllowed(["GET"]);
      if (request.headers.get("authorization") !== `Bearer ${requiredEnv("CRON_SECRET")}`) {
        return unauthorized();
      }

      try {
        const now = new Date();
        const batchSize = Math.min(integerEnv("TOUR_INTRO_SYNC_BATCH_SIZE", 20), 40);
        const concurrency = Math.min(integerEnv("TOUR_SYNC_CONCURRENCY", 4), 4);
        const places = await deps.listPlaces({ limit: batchSize, now });
        if (places.length === 0) {
          return json({ status: "idle", processed: 0, updated: 0, empty: 0, failed: 0 });
        }

        const counts = await deps.runBatch({
          places,
          fetchIntro: deps.fetchIntro,
          saveIntro: deps.saveIntro,
          recordFailure: deps.recordFailure,
          concurrency,
          syncedAt: now.toISOString()
        });
        return json({ status: "completed", ...counts });
      } catch (error) {
        return serverError(error);
      }
    }
  };
}

export default createTourIntroSyncHandler();
