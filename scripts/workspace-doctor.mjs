import { access, copyFile, readFile } from "node:fs/promises";
import { dirname, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const LOCAL_CONFIGS = [
  ["android/local.properties.example", "android/local.properties"],
  ["backend/.env.example", "backend/.env.local"]
];

const ANDROID_REQUIRED = ["sdk.dir", "KAKAO_MAP_NATIVE_APP_KEY"];
const BACKEND_REQUIRED = [
  "TOUR_API_SERVICE_KEY",
  "KAKAO_REST_API_KEY",
  "SUPABASE_URL",
  "SUPABASE_SERVICE_ROLE_KEY",
  "CRON_SECRET"
];

async function exists(path) {
  try {
    await access(path);
    return true;
  } catch {
    return false;
  }
}

function parseProperties(source) {
  return Object.fromEntries(
    source
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter((line) => line && !line.startsWith("#") && line.includes("="))
      .map((line) => {
        const separator = line.indexOf("=");
        return [line.slice(0, separator).trim(), line.slice(separator + 1).trim()];
      })
  );
}

function isConfigured(value) {
  if (!value) return false;
  return !/YOUR_|별도_전달|새 컴퓨터|CHANGE_ME/i.test(value);
}

async function inspectConfig(repoRoot, relativePath, requiredKeys) {
  const path = join(repoRoot, relativePath);
  if (!(await exists(path))) {
    return { status: "missing_file", file: relativePath, missing: requiredKeys };
  }

  const values = parseProperties(await readFile(path, "utf8"));
  const missing = requiredKeys.filter((key) => !isConfigured(values[key]));
  return {
    status: missing.length === 0 ? "ready" : "needs_input",
    file: relativePath,
    missing
  };
}

export async function initializeLocalConfig(repoRoot) {
  const result = { created: [], preserved: [] };
  for (const [examplePath, targetPath] of LOCAL_CONFIGS) {
    const target = join(repoRoot, targetPath);
    if (await exists(target)) {
      result.preserved.push(targetPath);
      continue;
    }
    await copyFile(join(repoRoot, examplePath), target);
    result.created.push(targetPath);
  }
  return result;
}

export async function inspectWorkspace(
  repoRoot,
  { nodeVersion = process.versions.node } = {}
) {
  const nodeMajor = Number.parseInt(nodeVersion.split(".")[0], 10);
  return {
    repoRoot,
    node: nodeMajor === 24
      ? { status: "ready", expected: "24.x", actual: nodeVersion }
      : { status: "blocked", expected: "24.x", actual: nodeVersion },
    android: await inspectConfig(
      repoRoot,
      "android/local.properties",
      ANDROID_REQUIRED
    ),
    backend: await inspectConfig(
      repoRoot,
      "backend/.env.local",
      BACKEND_REQUIRED
    )
  };
}

function printConfig(label, config) {
  if (config.status === "ready") {
    console.log(`[READY] ${label}: ${config.file}`);
    return;
  }
  console.log(`[INPUT] ${label}: ${config.file}`);
  console.log(`        필요한 항목: ${config.missing.join(", ")}`);
}

async function main() {
  const scriptPath = fileURLToPath(import.meta.url);
  const repoRoot = dirname(dirname(scriptPath));
  if (process.argv.includes("--init")) {
    const initialized = await initializeLocalConfig(repoRoot);
    for (const path of initialized.created) console.log(`[CREATED] ${path}`);
    for (const path of initialized.preserved) console.log(`[KEEP] ${path}`);
  }

  const report = await inspectWorkspace(repoRoot);
  console.log(`\n틈새 작업공간: ${relative(process.cwd(), repoRoot) || "."}`);
  console.log(
    `[${report.node.status === "ready" ? "READY" : "BLOCKED"}] ` +
      `Node.js ${report.node.actual} (필요: ${report.node.expected})`
  );
  printConfig("Android 로컬 설정", report.android);
  printConfig("Backend 로컬 연동", report.backend);
  console.log("\n다음 문서: docs/00_START_HERE.md → docs/09_NEXT_VERSION_PLAN.md");
  console.log("비밀값 원문은 이 진단에 출력되지 않습니다.");
  if (report.node.status !== "ready" || report.android.status !== "ready") {
    process.exitCode = 1;
  }
}

const invokedPath = process.argv[1] ? resolve(process.argv[1]) : null;
if (invokedPath === fileURLToPath(import.meta.url)) {
  await main();
}
