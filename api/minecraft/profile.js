const USERNAME_PATTERN=/^[A-Za-z0-9_]{3,16}$/;

const json=(res,status,body)=>{
  res.statusCode=status;
  res.setHeader("Content-Type","application/json; charset=utf-8");
  res.setHeader("Cache-Control","no-store");
  res.end(JSON.stringify(body));
};

export default async function handler(req,res){
  if(req.method!=="GET")return json(res,405,{ok:false,error:"method_not_allowed"});

  const username=String(req.query?.username||"").trim();
  if(!USERNAME_PATTERN.test(username)){
    return json(res,400,{ok:false,error:"invalid_minecraft_username"});
  }

  try{
    const response=await fetch(
      "https://api.mojang.com/users/profiles/minecraft/"+encodeURIComponent(username),
      {headers:{"Accept":"application/json"}}
    );

    if(response.status===204||response.status===404){
      return json(res,404,{ok:false,error:"minecraft_user_not_found"});
    }
    if(!response.ok){
      return json(res,502,{ok:false,error:"mojang_api_unavailable"});
    }

    const data=await response.json();
    const canonicalName=String(data?.name||"").trim();
    const uuid=String(data?.id||"").trim();

    if(!canonicalName||!uuid){
      return json(res,404,{ok:false,error:"minecraft_user_not_found"});
    }

    return json(res,200,{
      ok:true,
      username:canonicalName,
      uuid,
      avatarUrl:"https://mc-heads.net/avatar/"+encodeURIComponent(uuid)+"/128"
    });
  }catch{
    return json(res,502,{ok:false,error:"mojang_api_unavailable"});
  }
}
