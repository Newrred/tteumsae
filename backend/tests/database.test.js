import test from "node:test";
import assert from "node:assert/strict";
import { listPlaces } from "../lib/database.js";

test("새 Supabase secret 키는 PostgREST Bearer 토큰으로 보내지 않는다", async () => {
  process.env.SUPABASE_URL = "https://supabase.test";
  process.env.SUPABASE_SERVICE_ROLE_KEY = "sb_secret_server-only-key";
  const originalFetch = globalThis.fetch;
  let request;
  globalThis.fetch = async (url, init) => {
    request = { url: String(url), init };
    return Response.json([]);
  };
  try {
    assert.deepEqual(await listPlaces(), []);
    assert.equal(request.init.headers.apikey, "sb_secret_server-only-key");
    assert.equal("authorization" in request.init.headers, false);
  } finally {
    globalThis.fetch = originalFetch;
  }
});
