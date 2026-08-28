import assert from "node:assert/strict";
import { readFile, readdir } from "node:fs/promises";
import { extname, join, relative } from "node:path";
import { fileURLToPath } from "node:url";

const root = new URL("../", import.meta.url);
const rootPath = fileURLToPath(root);
const requiredFiles = [
  "api/health.js",
  "api/ops/status.js",
  "api/account.js",
  "api/places/index.js",
  "api/places/[id].js",
  "api/recommendations.js",
  "api/route.js",
  "api/cron/tour-sync.js",
  "api/cron/tour-catalog-sync.js",
  "api/cron/tour-detail-sync.js",
  "api/cron/tour-intro-sync.js",
  "migrations/001_initial.sql",
  "migrations/002_detail_sync_state.sql",
  "migrations/003_user_accounts.sql",
  "migrations/004_tour_enrichment.sql",
  "migrations/005_sync_runtime_safety.sql",
  "migrations/006_gate_1b_data_trust.sql",
  "scripts/verify-user-rls.js",
  "lib/supabase-auth.js",
  "lib/fetch-policy.js",
  "lib/sync-lease.js",
  "lib/tour-sync.js",
  "privacy.html",
  "account-deletion.html",
  ".env.example",
  "vercel.json"
];

for (const file of requiredFiles) {
  await readFile(new URL(file, root), "utf8");
}

async function collectFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true });
  const files = [];
  for (const entry of entries) {
    if (["node_modules", ".git", ".vercel"].includes(entry.name)) continue;
    const path = join(directory, entry.name);
    if (entry.isDirectory()) files.push(...(await collectFiles(path)));
    else files.push(path);
  }
  return files;
}

const files = await collectFiles(rootPath);
for (const file of files) {
  if ([".apk", ".sha256"].includes(extname(file))) continue;
  const text = await readFile(file, "utf8");
  assert.equal(
    /(?<![a-f0-9])[a-f0-9]{64}(?![a-f0-9])/i.test(text),
    false,
    `64자리 인증키로 보이는 값이 포함됨: ${relative(rootPath, file)}`
  );
}

console.log(`Project check passed: ${files.length} files`);
