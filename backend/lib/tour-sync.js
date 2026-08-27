import { normalizeTourIntro } from "./tour-api.js";

export async function runIntroBatch({
  places,
  fetchIntro,
  saveIntro,
  recordFailure,
  concurrency = 4,
  syncedAt
}) {
  const queue = Array.isArray(places) ? places : [];
  const workerCount = Math.min(Math.max(Number.parseInt(concurrency, 10) || 1, 1), 4, queue.length);
  const counts = { processed: queue.length, updated: 0, empty: 0, failed: 0 };
  const failureTime = new Date(syncedAt);
  let nextIndex = 0;
  let recordError = null;

  async function worker() {
    while (nextIndex < queue.length) {
      const place = queue[nextIndex];
      nextIndex += 1;
      try {
        const intro = await fetchIntro(place.content_id, place.content_type_id);
        const enrichment = normalizeTourIntro({
          contentTypeId: place.content_type_id,
          intro,
          syncedAt
        });
        await saveIntro(place, enrichment);
        if (intro === null) counts.empty += 1;
        else counts.updated += 1;
      } catch (error) {
        counts.failed += 1;
        try {
          await recordFailure(place, error, failureTime);
        } catch (failureRecordError) {
          recordError ??= failureRecordError;
        }
      }
    }
  }

  await Promise.all(Array.from({ length: workerCount }, () => worker()));
  if (recordError) throw recordError;
  return counts;
}
