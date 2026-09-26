import crypto from "node:crypto";
import {addPayment,storageMode} from "./_store.js";

const json=(res,status,body)=>{
  res.statusCode=status;
  res.setHeader("Content-Type","application/json; charset=utf-8");
  res.setHeader("Cache-Control","no-store");
  res.end(JSON.stringify(body));
};

const USERNAME=/^[A-Za-z0-9_]{3,16}$/;
const CURRENCY=/^[A-Z]{3}$/;

export default async function handler(req,res){
  if(req.method!=="POST")return json(res,405,{ok:false,error:"method_not_allowed"});

  const secret=process.env.SLEEP_ADMIN_SECRET;
  if(!secret)return json(res,503,{ok:false,error:"server_not_configured"});

  const auth=req.headers.authorization||"";
  if(auth!==`Bearer ${secret}`)return json(res,401,{ok:false,error:"unauthorized"});

  try{
    const body=typeof req.body==="string"?JSON.parse(req.body||"{}"):(req.body||{});
    const username=String(body.username||body.payer||"").trim();
    const plan=String(body.plan||"").trim().toLowerCase();
    const provider=String(body.provider||"manual").trim().slice(0,40);
    const currency=String(body.currency||"EUR").trim().toUpperCase();
    const amount=Number(body.amount);
    const eventId=String(body.eventId||"").trim()||crypto.randomUUID();
    const receivedAt=Number(body.receivedAt)||Date.now();

    if(!USERNAME.test(username))return json(res,400,{ok:false,error:"invalid_username"});
    if(plan!=="monthly"&&plan!=="lifetime")return json(res,400,{ok:false,error:"invalid_plan"});
    if(!CURRENCY.test(currency))return json(res,400,{ok:false,error:"invalid_currency"});
    if(!Number.isFinite(amount)||amount<=0||amount>1_000_000){
      return json(res,400,{ok:false,error:"invalid_amount"});
    }
    if(!/^[A-Za-z0-9._:-]{8,120}$/.test(eventId)){
      return json(res,400,{ok:false,error:"invalid_event_id"});
    }

    const payment={
      id:eventId,
      username,
      plan,
      provider,
      currency,
      amount:Math.round(amount*100)/100,
      status:"paid",
      receivedAt:Math.min(Math.max(receivedAt,Date.now()-7*86_400_000),Date.now()+300_000),
      recordedAt:Date.now()
    };

    const result=await addPayment(payment);
    return json(res,200,{
      ok:true,
      duplicate:result.duplicate,
      storage:result.storage||storageMode(),
      payment
    });
  }catch{
    return json(res,500,{ok:false,error:"payment_store_failed"});
  }
}
