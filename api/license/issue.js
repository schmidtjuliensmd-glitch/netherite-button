import crypto from "node:crypto";

const json = (res, status, body) => {
  res.statusCode = status;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.end(JSON.stringify(body));
};

const b64url = value => Buffer.from(value).toString("base64url");
const sign = (data, secret) => crypto.createHmac("sha256", secret).update(data).digest("base64url");

export default async function handler(req, res) {
  if (req.method !== "POST") return json(res, 405, {ok:false,error:"method_not_allowed"});

  const adminSecret = process.env.SLEEP_ADMIN_SECRET;
  const licenseSecret = process.env.SLEEP_LICENSE_SECRET;
  if (!adminSecret || !licenseSecret) return json(res, 503, {ok:false,error:"server_not_configured"});

  const auth = req.headers.authorization || "";
  if (auth !== `Bearer ${adminSecret}`) return json(res, 401, {ok:false,error:"unauthorized"});

  const body = typeof req.body === "string" ? JSON.parse(req.body || "{}") : (req.body || {});
  const username = String(body.username || "").trim();
  const plan = body.plan === "lifetime" ? "lifetime" : body.plan === "monthly" ? "monthly" : "";

  if (!/^[A-Za-z0-9_]{3,16}$/.test(username)) return json(res, 400, {ok:false,error:"invalid_minecraft_username"});
  if (!plan) return json(res, 400, {ok:false,error:"invalid_plan"});

  const now = Date.now();
  const expiresAt = plan === "monthly" ? now + 30 * 24 * 60 * 60 * 1000 : null;
  const payload = {
    v: 1,
    u: username.toLowerCase(),
    p: plan,
    iat: now,
    exp: expiresAt,
    n: crypto.randomBytes(8).toString("hex")
  };
  const encoded = b64url(JSON.stringify(payload));
  const signature = sign(encoded, licenseSecret);
  const key = `SLEEP.${encoded}.${signature}`;

  return json(res, 200, {
    ok: true,
    key,
    username,
    plan,
    expiresAt
  });
}
