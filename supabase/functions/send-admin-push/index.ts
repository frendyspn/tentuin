// Edge Function: send-admin-push
// Mengirim push notification ke semua admin (atau user_ids tertentu) via FCM HTTP v1.
//
// Invocation:
//   POST /functions/v1/send-admin-push
//   Headers:  x-internal-key: <INTERNAL_PUSH_KEY>
//             Content-Type: application/json
//   Body:     { event, title, body, data?, user_ids? }
//
// Required env (Supabase Dashboard → Edge Functions → Secrets):
//   INTERNAL_PUSH_KEY         random shared secret with pg_net trigger
//   FCM_SERVICE_ACCOUNT_JSON  Firebase service account JSON
//   SUPABASE_URL              auto-injected
//   SUPABASE_SERVICE_ROLE_KEY auto-injected

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.0";
import { parseFcmServiceAccount, sendFcmToTokens } from "../_shared/fcm.ts";
import { requireMethod, verifyInternalKey } from "../_shared/auth.ts";

interface PushRequest {
  event:     string;
  title:     string;
  body:      string;
  data?:     Record<string, unknown>;
  user_ids?: string[];
}

Deno.serve(async (req: Request) => {
  const methodErr = requireMethod(req);
  if (methodErr) return methodErr;

  const authCheck = verifyInternalKey(req);
  if (!authCheck.ok) return authCheck.response;

  let payload: PushRequest;
  try {
    payload = await req.json() as PushRequest;
  } catch {
    return new Response("Invalid JSON", { status: 400 });
  }

  let serviceAccount;
  try {
    serviceAccount = parseFcmServiceAccount(Deno.env.get("FCM_SERVICE_ACCOUNT_JSON"));
  } catch (e) {
    return new Response((e as Error).message, { status: 500 });
  }

  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    { auth: { persistSession: false } },
  );

  // Resolve target user_ids (semua admin kalau tidak di-specify)
  let targetUserIds: string[];
  if (payload.user_ids && payload.user_ids.length > 0) {
    targetUserIds = payload.user_ids;
  } else {
    const { data: admins, error } = await supabase
      .from("profiles").select("id").in("role", ["admin", "super_admin"]);
    if (error) {
      return new Response(`Profile fetch failed: ${error.message}`, { status: 500 });
    }
    targetUserIds = (admins ?? []).map((a) => a.id);
    if (targetUserIds.length === 0) {
      return new Response(JSON.stringify({ sent: 0, reason: "no_admins" }), {
        status: 200, headers: { "Content-Type": "application/json" },
      });
    }
  }

  const { data: tokens, error: tokErr } = await supabase
    .from("admin_push_tokens").select("user_id, fcm_token").in("user_id", targetUserIds);
  if (tokErr) {
    return new Response(`Token fetch failed: ${tokErr.message}`, { status: 500 });
  }
  if (!tokens || tokens.length === 0) {
    return new Response(JSON.stringify({ sent: 0, reason: "no_tokens" }), {
      status: 200, headers: { "Content-Type": "application/json" },
    });
  }

  const result = await sendFcmToTokens(
    serviceAccount,
    tokens.map((t) => t.fcm_token),
    { title: payload.title, body: payload.body },
    { ...payload.data, event: payload.event },
    { priority: "HIGH", channel_id: "admin_alerts" },
  );

  if (result.stale.length > 0) {
    await supabase.from("admin_push_tokens").delete().in("fcm_token", result.stale);
  }

  return new Response(
    JSON.stringify({
      sent:          result.sent,
      failed:        result.failed,
      removed_stale: result.stale.length,
      event:         payload.event,
    }),
    { status: 200, headers: { "Content-Type": "application/json" } },
  );
});
