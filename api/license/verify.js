import crypto from "node:crypto";

const json = (res, status, body) => {
  res.statusCode = status;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Cache-Control", "no-store");
  res.end(JSON.stringify(body));
};

const sign = (data, secret) => crypto.createHmac("sha256", secret).update(data).digest("base64url");
const safeEqual = (a,b) => {
  const aa = Buffer.from(a);
  const bb = Buffer.from(b);
  return aa.length === bb.length && crypto.timingSafeEqual(aa, bb);
};

export default async function handler(req, res) {
  if (req.method !== "POST") return json(res, 405, {ok:false,error:"method_not_allowed"});
  const secret = process.env.SLEEP_LICENSE_SECRET;
  if (!secret) return json(res, 503, {ok:false,error:"server_not_configured"});

  try {
    const body = typeof req.body === "string" ? JSON.parse(req.body || "{}") : (req.body || {});
    const username = String(body.username || "").trim().toLowerCase();
    const key = String(body.key || "").trim();

    if (!/^[A-Za-z0-9_]{3,16}$/.test(username)) return json(res, 400, {ok:false,error:"invalid_minecraft_username"});

    const parts = key.split(".");
    if (parts.length !== 3 || parts[0] !== "SLEEP") return json(res, 401, {ok:false,error:"invalid_key"});

    const encoded = parts[1];
    const expected = sign(encoded, secret);
    if (!safeEqual(parts[2], expected)) return json(res, 401, {ok:false,error:"invalid_key"});

    const payload = JSON.parse(Buffer.from(encoded, "base64url").toString("utf8"));
    if (payload.v !== 1 || payload.u !== username) return json(res, 401, {ok:false,error:"key_not_bound_to_username"});
    if (payload.exp && Date.now() > payload.exp) return json(res, 401, {ok:false,error:"license_expired"});

    return json(res, 200, {
      ok: true,
      username,
      plan: payload.p,
      expiresAt: payload.exp || null,
      lifetime: payload.p === "lifetime"
    });
  } catch {
    return json(res, 401, {ok:false,error:"invalid_key"});
  }
}
