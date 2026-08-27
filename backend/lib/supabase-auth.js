import { requiredEnv } from "./env.js";

export function readBearerToken(request) {
  const authorization = request.headers.get("authorization")?.trim() ?? "";
  const match = /^Bearer\s+([^\s]+)$/i.exec(authorization);
  return match?.[1] ?? null;
}

export async function verifySupabaseUser(token, fetchImpl = fetch) {
  const baseUrl = requiredEnv("SUPABASE_URL").replace(/\/$/, "");
  const publishableKey = requiredEnv("SUPABASE_PUBLISHABLE_KEY");
  const response = await fetchImpl(`${baseUrl}/auth/v1/user`, {
    method: "GET",
    headers: {
      apikey: publishableKey,
      authorization: `Bearer ${token}`
    }
  });

  if ([401, 403].includes(response.status)) return null;
  if (!response.ok) {
    throw new Error(`Supabase user verification failed (${response.status})`);
  }

  const user = await response.json();
  return typeof user?.id === "string" && user.id ? { id: user.id } : null;
}

export async function deleteSupabaseUser(userId, fetchImpl = fetch) {
  const baseUrl = requiredEnv("SUPABASE_URL").replace(/\/$/, "");
  const serviceRoleKey = requiredEnv("SUPABASE_SERVICE_ROLE_KEY");
  const response = await fetchImpl(
    `${baseUrl}/auth/v1/admin/users/${encodeURIComponent(userId)}`,
    {
      method: "DELETE",
      headers: {
        apikey: serviceRoleKey,
        authorization: `Bearer ${serviceRoleKey}`
      }
    }
  );

  if (response.ok || response.status === 404) return;
  throw new Error(`Supabase admin user deletion failed (${response.status})`);
}
