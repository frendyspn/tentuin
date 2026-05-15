// Shared FCM (Firebase Cloud Messaging) HTTP v1 helper.
// Dipakai oleh semua edge function push (send-admin-push, send-agent-push,
// send-university-push, send-student-push, send-school-pic-push).
//
// Required env di setiap caller:
//   FCM_SERVICE_ACCOUNT_JSON  — Firebase service account JSON (1 string, escaped \n di private_key OK)

export interface FcmServiceAccount {
  client_email: string;
  private_key:  string;
  project_id:   string;
}

export interface FcmNotification {
  title: string;
  body:  string;
}

export interface FcmAndroidConfig {
  priority?:     "HIGH" | "NORMAL";
  channel_id?:   string;
}

export interface FcmSendResult {
  sent:    number;
  failed:  number;
  /** Tokens yang FCM tolak sebagai unregistered/invalid — caller harus hapus dari DB. */
  stale:   string[];
}

// In-module cache OAuth access token (TTL ~1 jam, FCM keluarkan exp di response).
let cachedAccessToken: { token: string; expiresAt: number } | null = null;

/** Parse FCM_SERVICE_ACCOUNT_JSON env var. Throw kalau malformed/incomplete. */
export function parseFcmServiceAccount(json: string | undefined): FcmServiceAccount {
  if (!json) throw new Error("FCM_SERVICE_ACCOUNT_JSON env var is not set");
  let parsed: Partial<FcmServiceAccount>;
  try {
    parsed = JSON.parse(json);
  } catch {
    throw new Error("FCM_SERVICE_ACCOUNT_JSON is malformed JSON");
  }
  if (!parsed.client_email || !parsed.private_key || !parsed.project_id) {
    throw new Error("FCM_SERVICE_ACCOUNT_JSON missing required fields (client_email/private_key/project_id)");
  }
  return parsed as FcmServiceAccount;
}

/** Get OAuth access token untuk FCM. Cached per-isolate sampai ~5 menit sebelum exp. */
export async function getFcmAccessToken(sa: FcmServiceAccount): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  if (cachedAccessToken && cachedAccessToken.expiresAt > now + 60) {
    return cachedAccessToken.token;
  }

  const header  = { alg: "RS256", typ: "JWT" };
  const payload = {
    iss:   sa.client_email,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud:   "https://oauth2.googleapis.com/token",
    iat:   now,
    exp:   now + 3600,
  };

  const encoder = new TextEncoder();
  const headerB64  = base64UrlEncode(encoder.encode(JSON.stringify(header)));
  const payloadB64 = base64UrlEncode(encoder.encode(JSON.stringify(payload)));
  const unsigned   = `${headerB64}.${payloadB64}`;

  const pem  = sa.private_key.replace(/\\n/g, "\n");
  const key  = await importPrivateKey(pem);
  const sig  = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, encoder.encode(unsigned));
  const jwt  = `${unsigned}.${base64UrlEncode(new Uint8Array(sig))}`;

  const tokenRes = await fetch("https://oauth2.googleapis.com/token", {
    method:  "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body:    new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion:  jwt,
    }),
  });
  if (!tokenRes.ok) {
    throw new Error(`OAuth token exchange failed: ${tokenRes.status} ${await tokenRes.text()}`);
  }
  const tokenJson = await tokenRes.json() as { access_token: string; expires_in: number };
  cachedAccessToken = {
    token:     tokenJson.access_token,
    expiresAt: now + tokenJson.expires_in,
  };
  return tokenJson.access_token;
}

/**
 * Kirim 1 notification ke banyak token. Return statistik per-token.
 * Caller bertanggung jawab hapus token yang ada di `result.stale` dari DB.
 *
 * data values harus string (FCM HTTP v1 requirement). Non-string akan di-JSON-stringify.
 */
export async function sendFcmToTokens(
  sa:           FcmServiceAccount,
  tokens:       readonly string[],
  notification: FcmNotification,
  data:         Record<string, unknown> = {},
  android:      FcmAndroidConfig        = { priority: "HIGH" },
): Promise<FcmSendResult> {
  if (tokens.length === 0) return { sent: 0, failed: 0, stale: [] };

  const accessToken = await getFcmAccessToken(sa);
  const fcmEndpoint = `https://fcm.googleapis.com/v1/projects/${sa.project_id}/messages:send`;

  // FCM v1 requires data values jadi string
  const dataStr: Record<string, string> = {};
  for (const [k, v] of Object.entries(data)) {
    dataStr[k] = typeof v === "string" ? v : JSON.stringify(v);
  }

  const androidPayload = {
    priority: android.priority ?? "HIGH",
    ...(android.channel_id ? { notification: { channel_id: android.channel_id } } : {}),
  };

  let sent = 0, failed = 0;
  const stale: string[] = [];

  for (const token of tokens) {
    const res = await fetch(fcmEndpoint, {
      method:  "POST",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type":  "application/json",
      },
      body: JSON.stringify({
        message: { token, notification, data: dataStr, android: androidPayload },
      }),
    });

    if (res.ok) {
      sent++;
      continue;
    }
    failed++;
    const text = await res.text();
    if (res.status === 404 || /UNREGISTERED|INVALID_ARGUMENT|registration-token-not-registered/i.test(text)) {
      stale.push(token);
    }
    console.error(`FCM error for token ${token.slice(0, 12)}…: ${res.status} ${text}`);
  }

  return { sent, failed, stale };
}

// ─── internals ──────────────────────────────────────────────────────────────

function base64UrlEncode(bytes: Uint8Array): string {
  let s = "";
  for (const b of bytes) s += String.fromCharCode(b);
  return btoa(s).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
}

async function importPrivateKey(pem: string): Promise<CryptoKey> {
  const body = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, "")
    .replace(/-----END PRIVATE KEY-----/, "")
    .replace(/\s/g, "");
  const der = Uint8Array.from(atob(body), (c) => c.charCodeAt(0));
  return crypto.subtle.importKey(
    "pkcs8",
    der.buffer as ArrayBuffer,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
}
