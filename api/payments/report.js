import crypto from "node:crypto";
import {addPayment,storageMode} from "./_store.js";

const json=(res,status,body)=>{
  res.statusCode=status;
  res.setHeader("Content-Type","application/json; charset=utf-8");
  res.setHeader("Cache-Control","no-store");
  res.end(JSON.stringify(body));
};

const PLAYER=/^\\.?[A-Za-z0-9_]{3,16}$/;

export default async function handler(req,res){
  if(req.method!=="POST")return json(res,405,{ok:false,error:"method_not_allowed"});

  const secret=process.env.SLEEP_ADMIN_SECRET;
  if(!secret)return json(res,503,{ok:false,error:"server_not_configured"});

  const auth=req.headers.authorization||"";
  if(auth!==`Bearer ${secret}`)return json(res,401,{ok:false,error:"unauthorized"});

  try{
    const body=typeof req.body==="string"?JSON.parse(req.body||"{}"):(req.body||{});
    const payer=String(body.payer||"").trim();
    const receiver=String(body.receiver||"").trim();
    const amount=Number(body.amount);
    const amountRaw=String(body.amountRaw||"").trim().slice(0,64);
    const message=String(body.message||"").trim().slice(0,300);
    const receivedAt=Number(body.receivedAt)||Date.now();
    const eventId=String(body.eventId||"").trim()||crypto.randomUUID();

    if(!PLAYER.test(payer))return json(res,400,{ok:false,error:"invalid_payer"});
    if(receiver&&!PLAYER.test(receiver))return json(res,400,{ok:false,error:"invalid_receiver"});
    if(!Number.isSafeInteger(amount)||amount<=0||amount>1_000_000_000_000_000){
      return json(res,400,{ok:false,error:"invalid_amount"});
    }
    if(!/^[A-Za-z0-9-]{8,80}$/.test(eventId)){
      return json(res,400,{ok:false,error:"invalid_event_id"});
    }

    const plan=amount===10_000_000?"monthly":amount===25_000_000?"lifetime":null;
    const payment={
      id:eventId,
      payer,
      receiver,
      amount,
      amountRaw,
      plan,
      receivedAt:Math.min(Math.max(receivedAt,Date.now()-86_400_000),Date.now()+300_000),
      recordedAt:Date.now()
    };

    const result=await addPayment(payment);
    return json(res,200,{
      ok:true,
      duplicate:result.duplicate,
      storage:result.storage||storageMode(),
      payment
    });
  }catch(error){
    return json(res,500,{ok:false,error:"payment_store_failed"});
  }
}
