import crypto from "node:crypto";

const MESSAGES_KEY="sleep:chat:messages:v1";
const PRESENCE_KEY="sleep:chat:presence:v1";
const MAX_MESSAGES=100;
const ONLINE_WINDOW_MS=120000;
const PLAYER=/^\\.?[A-Za-z0-9_]{3,16}$/;
const SESSION=/^[A-Za-z0-9_-]{12,80}$/;

const json=(res,status,body)=>{
  res.statusCode=status;
  res.setHeader("Content-Type","application/json; charset=utf-8");
  res.setHeader("Cache-Control","no-store");
  res.end(JSON.stringify(body));
};

const redisConfig=()=>{
  const url=process.env.KV_REST_API_URL
    ||process.env.UPSTASH_REDIS_REST_URL
    ||process.env.UPSTASH_REDIS_REST_KV_REST_API_URL
    ||"";
  const token=process.env.KV_REST_API_TOKEN
    ||process.env.UPSTASH_REDIS_REST_TOKEN
    ||process.env.UPSTASH_REDIS_REST_KV_REST_API_TOKEN
    ||"";
  return url&&token?{url:url.replace(/\\/$/,""),token}:null;
};

const redis=async command=>{
  const cfg=redisConfig();
  if(!cfg)throw new Error("redis_not_configured");
  const response=await fetch(cfg.url,{
    method:"POST",
    headers:{
      Authorization:`Bearer ${cfg.token}`,
      "Content-Type":"application/json"
    },
    body:JSON.stringify(command)
  });
  const data=await response.json();
  if(!response.ok||data.error)throw new Error(data.error||"redis_error");
  return data.result;
};

const cleanText=value=>String(value||"")
  .replace(/[\\u0000-\\u001F\\u007F]/g," ")
  .replace(/\\s+/g," ")
  .trim()
  .slice(0,200);

const heartbeat=async sessionId=>{
  const now=Date.now();
  await redis(["ZADD",PRESENCE_KEY,String(now),sessionId]);
  const online=Number(await redis(["ZCOUNT",PRESENCE_KEY,String(now-ONLINE_WINDOW_MS),"+inf"]))||0;
  return online;
};

const listMessages=async()=>{
  const rows=await redis(["LRANGE",MESSAGES_KEY,"0","49"]);
  return (Array.isArray(rows)?rows:[])
    .map(row=>{try{return JSON.parse(row);}catch{return null;}})
    .filter(Boolean)
    .reverse();
};

export default async function handler(req,res){
  if(!redisConfig())return json(res,503,{ok:false,error:"chat_not_configured"});

  try{
    if(req.method==="GET"){
      const mode=String(req.query?.mode||"messages");
      if(mode==="messages"){
        return json(res,200,{ok:true,messages:await listMessages()});
      }
      if(mode==="presence"){
        const now=Date.now();
        const online=Number(await redis(["ZCOUNT",PRESENCE_KEY,String(now-ONLINE_WINDOW_MS),"+inf"]))||0;
        return json(res,200,{ok:true,online});
      }
      return json(res,400,{ok:false,error:"invalid_mode"});
    }

    if(req.method!=="POST")return json(res,405,{ok:false,error:"method_not_allowed"});

    const body=typeof req.body==="string"?JSON.parse(req.body||"{}"):(req.body||{});
    const type=String(body.type||"");
    const sessionId=String(body.sessionId||"").trim();
    const username=String(body.username||"").trim();

    if(!SESSION.test(sessionId))return json(res,400,{ok:false,error:"invalid_session"});
    if(!PLAYER.test(username))return json(res,400,{ok:false,error:"invalid_username"});

    if(type==="heartbeat"){
      return json(res,200,{ok:true,online:await heartbeat(sessionId)});
    }

    if(type!=="message")return json(res,400,{ok:false,error:"invalid_type"});

    const message=cleanText(body.message);
    if(!message)return json(res,400,{ok:false,error:"empty_message"});

    const rateKey="sleep:chat:rate:"+sessionId;
    const allowed=await redis(["SET",rateKey,"1","NX","EX","2"]);
    if(allowed!=="OK")return json(res,429,{ok:false,error:"rate_limited"});

    const entry={
      id:crypto.randomUUID(),
      username,
      message,
      at:Date.now()
    };

    await redis(["LPUSH",MESSAGES_KEY,JSON.stringify(entry)]);
    await redis(["LTRIM",MESSAGES_KEY,"0",String(MAX_MESSAGES-1)]);
    const online=await heartbeat(sessionId);

    return json(res,200,{ok:true,message:entry,online});
  }catch(error){
    return json(res,500,{ok:false,error:"chat_failed"});
  }
}
