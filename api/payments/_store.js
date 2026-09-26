const KEY="sleep:payments:v1";
const MAX_ITEMS=200;

const redisConfig=()=>{
  const url=process.env.KV_REST_API_URL||process.env.UPSTASH_REDIS_REST_URL||"";
  const token=process.env.KV_REST_API_TOKEN||process.env.UPSTASH_REDIS_REST_TOKEN||"";
  return url&&token?{url:url.replace(/\/$/,""),token}:null;
};

const redis=async command=>{
  const cfg=redisConfig();
  if(!cfg)throw new Error("redis_not_configured");
  const response=await fetch(cfg.url,{
    method:"POST",
    headers:{
      "Authorization":`Bearer ${cfg.token}`,
      "Content-Type":"application/json"
    },
    body:JSON.stringify(command)
  });
  const data=await response.json();
  if(!response.ok||data.error)throw new Error(data.error||"redis_error");
  return data.result;
};

const memory=()=>{
  if(!globalThis.__sleepPaymentMemory){
    globalThis.__sleepPaymentMemory={items:[],ids:new Set()};
  }
  return globalThis.__sleepPaymentMemory;
};

export const storageMode=()=>redisConfig()?"redis":"memory";

export async function addPayment(payment){
  const cfg=redisConfig();
  if(cfg){
    const dedupeKey="sleep:payment:event:"+payment.id;
    const fresh=await redis(["SET",dedupeKey,"1","NX","EX","604800"]);
    if(fresh!=="OK")return {duplicate:true,storage:"redis"};
    await redis(["LPUSH",KEY,JSON.stringify(payment)]);
    await redis(["LTRIM",KEY,"0",String(MAX_ITEMS-1)]);
    return {duplicate:false,storage:"redis"};
  }

  const state=memory();
  if(state.ids.has(payment.id))return {duplicate:true,storage:"memory"};
  state.ids.add(payment.id);
  state.items.unshift(payment);
  if(state.items.length>MAX_ITEMS)state.items.length=MAX_ITEMS;
  return {duplicate:false,storage:"memory"};
}

export async function listPayments(){
  if(redisConfig()){
    const rows=await redis(["LRANGE",KEY,"0",String(MAX_ITEMS-1)]);
    const payments=(Array.isArray(rows)?rows:[]).map(row=>{
      try{return JSON.parse(row);}catch{return null;}
    }).filter(Boolean);
    return {payments,storage:"redis"};
  }

  return {payments:[...memory().items],storage:"memory"};
}
