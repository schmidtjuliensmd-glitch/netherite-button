import {listPayments} from "./_store.js";

const json=(res,status,body)=>{
  res.statusCode=status;
  res.setHeader("Content-Type","application/json; charset=utf-8");
  res.setHeader("Cache-Control","no-store");
  res.end(JSON.stringify(body));
};

export default async function handler(req,res){
  if(req.method!=="GET")return json(res,405,{ok:false,error:"method_not_allowed"});

  const secret=process.env.SLEEP_ADMIN_SECRET;
  if(!secret)return json(res,503,{ok:false,error:"server_not_configured"});

  const auth=req.headers.authorization||"";
  if(auth!==`Bearer ${secret}`)return json(res,401,{ok:false,error:"unauthorized"});

  try{
    const data=await listPayments();
    return json(res,200,{ok:true,...data});
  }catch{
    return json(res,500,{ok:false,error:"payment_store_failed"});
  }
}
