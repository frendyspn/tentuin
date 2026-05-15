// Shared internal-key authorization untuk edge functions yang dipanggil dari
// pg_net trigger di database (bukan client). Mencegah edge function dipanggil
// dari publik / authenticated user biasa.

const HEADER = "x-internal-key";

/** Verify `x-internal-key` header matches INTERNAL_PUSH_KEY env var. */
export function verifyInternalKey(req: Request): { ok: true } | { ok: false; response: Response } {
  const provided = req.headers.get(HEADER);
  const expected = Deno.env.get("INTERNAL_PUSH_KEY");
  if (!expected) {
    return { ok: false, response: new Response("INTERNAL_PUSH_KEY not configured", { status: 500 }) };
  }
  if (provided !== expected) {
    return { ok: false, response: new Response("Forbidden", { status: 403 }) };
  }
  return { ok: true };
}

/** Sanity-check that request method matches expected (default POST). */
export function requireMethod(req: Request, method = "POST"): Response | null {
  if (req.method !== method) {
    return new Response("Method not allowed", { status: 405 });
  }
  return null;
}
