/* Sleep Client website live chat */
(function sleepLiveChat(){
  const PROFILE_KEY='sleep-minecraft-profile';
  const SESSION_KEY='sleep-live-chat-session';
  const ENDPOINT='/api/chat';
  const HEARTBEAT_MS=45000;
  const POLL_MS=5000;
  let open=false;
  let pollTimer=null;
  let heartbeatTimer=null;
  let lastSignature='';

  const lang=()=>{
    try{
      const stored=localStorage.getItem('language')||localStorage.getItem('site-language')||'';
      if(stored==='de'||stored==='en')return stored;
    }catch{}
    const active=document.querySelector('.lang-btn.active')?.dataset?.lang;
    if(active==='de'||active==='en')return active;
    return document.documentElement.lang==='de'?'de':'en';
  };

  const strings={
    de:{
      title:'Live Chat',
      online:'online',
      placeholder:'Nachricht schreiben...',
      send:'Senden',
      empty:'Noch keine Nachrichten. Schreib die erste.',
      profile:'Wähle zuerst dein Minecraft-Profil.',
      failed:'Chat konnte nicht geladen werden.',
      sending:'Wird gesendet...',
      slow:'Bitte kurz warten, bevor du erneut schreibst.'
    },
    en:{
      title:'Live Chat',
      online:'online',
      placeholder:'Write a message...',
      send:'Send',
      empty:'No messages yet. Start the chat.',
      profile:'Choose your Minecraft profile first.',
      failed:'Could not load chat.',
      sending:'Sending...',
      slow:'Please wait a moment before sending again.'
    }
  };

  const readProfile=()=>{
    try{return JSON.parse(localStorage.getItem(PROFILE_KEY)||'null');}
    catch{return null;}
  };

  const sessionId=()=>{
    try{
      let id=localStorage.getItem(SESSION_KEY)||'';
      if(!/^[A-Za-z0-9_-]{12,80}$/.test(id)){
        id=(crypto.randomUUID?crypto.randomUUID():Date.now().toString(36)+Math.random().toString(36).slice(2)).replace(/[^A-Za-z0-9_-]/g,'');
        localStorage.setItem(SESSION_KEY,id);
      }
      return id;
    }catch{
      return ('guest_'+Date.now().toString(36)+Math.random().toString(36).slice(2)).slice(0,70);
    }
  };

  const avatar=username=>'https://mc-heads.net/avatar/'+encodeURIComponent(username)+'/64';

  const injectCss=()=>{
    if(document.querySelector('link[data-sleep-live-chat]'))return;
    const link=document.createElement('link');
    link.rel='stylesheet';
    link.href='./assets/live-chat.css';
    link.dataset.sleepLiveChat='1';
    document.head.appendChild(link);
  };

  const createUi=()=>{
    if(document.getElementById('sleep-live-chat'))return;
    const wrap=document.createElement('div');
    wrap.id='sleep-live-chat';
    wrap.className='sleep-live-chat';
    wrap.innerHTML=`
      <button class="slc-launcher" id="slc-launcher" type="button" aria-expanded="false">
        <span class="slc-launcher-icon">⌁</span>
        <span class="slc-launcher-copy"><strong>Live Chat</strong><small><i></i><b id="slc-launcher-count">0</b> online</small></span>
      </button>
      <section class="slc-panel" id="slc-panel" hidden aria-label="Live Chat">
        <header class="slc-header">
          <div><strong id="slc-title">Live Chat</strong><span><i></i><b id="slc-count">0</b> <em id="slc-online-label">online</em></span></div>
          <button id="slc-close" type="button" aria-label="Close">×</button>
        </header>
        <div class="slc-messages" id="slc-messages"></div>
        <p class="slc-status" id="slc-status"></p>
        <form class="slc-form" id="slc-form">
          <input id="slc-input" maxlength="200" autocomplete="off" spellcheck="false">
          <button id="slc-send" type="submit">Send</button>
        </form>
      </section>`;
    document.body.appendChild(wrap);
  };

  const updateLanguage=()=>{
    const s=strings[lang()];
    const title=document.getElementById('slc-title');
    const input=document.getElementById('slc-input');
    const send=document.getElementById('slc-send');
    const label=document.getElementById('slc-online-label');
    const launcher=document.querySelector('.slc-launcher-copy strong');
    const launcherOnline=document.querySelector('.slc-launcher-copy small');
    if(title)title.textContent=s.title;
    if(launcher)launcher.textContent=s.title;
    if(input)input.placeholder=s.placeholder;
    if(send)send.textContent=s.send;
    if(label)label.textContent=s.online;
    if(launcherOnline){
      const count=document.getElementById('slc-launcher-count')?.textContent||'0';
      launcherOnline.innerHTML='<i></i><b id="slc-launcher-count">'+count+'</b> '+s.online;
    }
  };

  const setOnline=value=>{
    const count=Math.max(0,Number(value)||0);
    const a=document.getElementById('slc-count');
    const b=document.getElementById('slc-launcher-count');
    if(a)a.textContent=String(count);
    if(b)b.textContent=String(count);
  };

  const setStatus=text,error=false)=>{
    const el=document.getElementById('slc-status');
    if(!el)return;
    el.textContent=text||'';
    el.classList.toggle('is-error',!!error);
  };

  const request=async (url,options={})=>{
    const response=await fetch(url,{cache:'no-store',...options});
    let data={};
    try{data=await response.json();}catch{}
    if(!response.ok){
      const err=new Error(data.error||'request_failed');
      err.code=data.error||'request_failed';
      throw err;
    }
    return data;
  };

  const heartbeat=async()=>{
    const profile=readProfile();
    if(!profile?.username)return;
    try{
      const data=await request(ENDPOINT,{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({type:'heartbeat',sessionId:sessionId(),username:profile.username})
      });
      setOnline(data.online);
    }catch{}
  };

  const renderMessages=messages=>{
    const box=document.getElementById('slc-messages');
    if(!box)return;
    const list=Array.isArray(messages)?messages:[];
    const sig=list.map(m=>m.id).join('|');
    if(sig===lastSignature)return;
    lastSignature=sig;

    box.replaceChildren();
    if(!list.length){
      const empty=document.createElement('div');
      empty.className='slc-empty';
      empty.textContent=strings[lang()].empty;
      box.appendChild(empty);
      return;
    }

    for(const item of list){
      const row=document.createElement('article');
      row.className='slc-message';

      const img=document.createElement('img');
      img.src=avatar(item.username);
      img.alt='';
      img.width=34;
      img.height=34;

      const body=document.createElement('div');
      const top=document.createElement('div');
      top.className='slc-message-top';
      const name=document.createElement('strong');
      name.textContent=item.username;
      const time=document.createElement('time');
      try{time.textContent=new Date(item.at).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});}catch{time.textContent='';}
      top.append(name,time);

      const text=document.createElement('p');
      text.textContent=item.message;
      body.append(top,text);
      row.append(img,body);
      box.appendChild(row);
    }
    box.scrollTop=box.scrollHeight;
  };

  const loadMessages=async()=>{
    if(!open)return;
    try{
      const data=await request(ENDPOINT+'?mode=messages');
      renderMessages(data.messages);
      setStatus('');
    }catch{
      setStatus(strings[lang()].failed,true);
    }
  };

  const setOpen=value=>{
    open=!!value;
    const panel=document.getElementById('slc-panel');
    const launcher=document.getElementById('slc-launcher');
    if(!panel||!launcher)return;
    panel.hidden=!open;
    launcher.setAttribute('aria-expanded',String(open));
    document.getElementById('sleep-live-chat')?.classList.toggle('is-open',open);
    if(open){
      updateLanguage();
      heartbeat();
      loadMessages();
      clearInterval(pollTimer);
      pollTimer=setInterval(loadMessages,POLL_MS);
      setTimeout(()=>document.getElementById('slc-input')?.focus(),100);
    }else{
      clearInterval(pollTimer);
      pollTimer=null;
    }
  };

  const sendMessage=async event=>{
    event.preventDefault();
    const input=document.getElementById('slc-input');
    const button=document.getElementById('slc-send');
    const profile=readProfile();
    const text=(input?.value||'').trim();
    const s=strings[lang()];

    if(!profile?.username){
      setStatus(s.profile,true);
      document.getElementById('mc-profile-card')?.click();
      return;
    }
    if(!text)return;

    if(button)button.disabled=true;
    setStatus(s.sending);
    try{
      const data=await request(ENDPOINT,{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
          type:'message',
          sessionId:sessionId(),
          username:profile.username,
          message:text
        })
      });
      if(input)input.value='';
      setOnline(data.online);
      setStatus('');
      await loadMessages();
    }catch(err){
      setStatus(err?.code==='rate_limited'?s.slow:s.failed,true);
    }finally{
      if(button)button.disabled=false;
      input?.focus();
    }
  };

  const init=()=>{
    injectCss();
    createUi();
    updateLanguage();

    document.getElementById('slc-launcher')?.addEventListener('click',()=>setOpen(!open));
    document.getElementById('slc-close')?.addEventListener('click',()=>setOpen(false));
    document.getElementById('slc-form')?.addEventListener('submit',sendMessage);

    document.addEventListener('keydown',event=>{
      if(event.key==='Escape'&&open)setOpen(false);
    });
    document.querySelectorAll('.lang-btn').forEach(btn=>{
      btn.addEventListener('click',()=>setTimeout(updateLanguage,30));
    });

    heartbeat();
    heartbeatTimer=setInterval(heartbeat,HEARTBEAT_MS);
    window.addEventListener('focus',heartbeat);
  };

  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});
  else init();
})();
