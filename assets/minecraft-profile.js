/* Sleep Client Minecraft profile component */
(function minecraftProfileComponent(){
  const STORAGE_KEY='sleep-minecraft-profile';
  const USERNAME_PATTERN=/^[A-Za-z0-9_]{3,16}$/;
  const avatarUrl=value=>'https://mc-heads.net/avatar/'+encodeURIComponent(value)+'/128';

  const strings={
    en:{
      eyebrow:'MINECRAFT PROFILE',
      title:'Enter your Minecraft username',
      copy:'Your Minecraft name is used for Sleep Client purchases and license activation.',
      placeholder:'Minecraft username',
      continue:'Continue',
      cancel:'Cancel',
      invalid:'Enter a valid Minecraft Java username (3–16 letters, numbers or _).',
      checking:'Checking Minecraft account…',
      notFound:'No Minecraft Java account was found with that name.',
      unavailable:'Minecraft account verification is temporarily unavailable. Please try again.',
      profile:'Minecraft profile',
      change:'Change Minecraft account'
    },
    de:{
      eyebrow:'MINECRAFT PROFIL',
      title:'Gib deinen Minecraft-Namen ein',
      copy:'Dein Minecraft-Name wird für Sleep-Client-Käufe und die Lizenzaktivierung verwendet.',
      placeholder:'Minecraft-Name',
      continue:'Weiter',
      cancel:'Abbrechen',
      invalid:'Gib einen gültigen Minecraft-Java-Namen ein (3–16 Zeichen: Buchstaben, Zahlen oder _).',
      checking:'Minecraft-Account wird geprüft…',
      notFound:'Unter diesem Namen wurde kein Minecraft-Java-Account gefunden.',
      unavailable:'Die Minecraft-Accountprüfung ist gerade nicht erreichbar. Versuch es erneut.',
      profile:'Minecraft Profil',
      change:'Minecraft-Account wechseln'
    }
  };

  const getLang=()=>{
    try{
      const stored=localStorage.getItem('language')||localStorage.getItem('site-language')||'';
      if(stored==='de'||stored==='en')return stored;
    }catch{}
    const active=document.querySelector('.lang-btn.active')?.dataset?.lang;
    if(active==='de'||active==='en')return active;
    return document.documentElement.lang==='de'?'de':'en';
  };

  const readProfile=()=>{
    try{
      const value=JSON.parse(localStorage.getItem(STORAGE_KEY)||'null');
      if(value&&USERNAME_PATTERN.test(String(value.username||'')))return value;
    }catch{}
    return null;
  };

  const writeProfile=profile=>{
    localStorage.setItem(STORAGE_KEY,JSON.stringify(profile));
    return profile;
  };

  const syncActivationInput=(username,force=false)=>{
    const input=document.getElementById('sleep-mc-name');
    if(input&&(force||!input.value.trim()))input.value=username;
  };

  const createUi=()=>{
    if(document.getElementById('mc-profile-gate'))return;

    const gate=document.createElement('div');
    gate.id='mc-profile-gate';
    gate.className='mc-profile-gate';
    gate.hidden=true;
    gate.innerHTML=`
      <div class="mc-profile-backdrop" aria-hidden="true"></div>
      <section class="mc-profile-modal" role="dialog" aria-modal="true" aria-labelledby="mc-profile-title">
        <button class="mc-profile-close" id="mc-profile-close" type="button" aria-label="Close">×</button>
        <div class="mc-profile-preview">
          <div class="mc-profile-head-wrap">
            <img id="mc-profile-preview-head" class="mc-profile-head" alt="" width="72" height="72">
          </div>
        </div>
        <div class="mc-profile-eyebrow" id="mc-profile-eyebrow"></div>
        <h2 id="mc-profile-title"></h2>
        <p class="mc-profile-copy" id="mc-profile-copy"></p>
        <form id="mc-profile-form" novalidate>
          <label class="mc-profile-field">
            <span>Minecraft Java</span>
            <input id="mc-profile-name" type="text" maxlength="16" autocomplete="off" spellcheck="false">
          </label>
          <p class="mc-profile-error" id="mc-profile-error" aria-live="polite"></p>
          <div class="mc-profile-actions">
            <button class="btn btn-secondary mc-profile-cancel" id="mc-profile-cancel" type="button"></button>
            <button class="btn btn-primary" id="mc-profile-submit" type="submit"></button>
          </div>
        </form>
      </section>`;
    document.body.appendChild(gate);

    const card=document.createElement('button');
    card.id='mc-profile-card';
    card.className='mc-profile-card';
    card.type='button';
    card.hidden=true;
    card.innerHTML=`
      <img class="mc-profile-card-head" alt="" width="44" height="44">
      <span class="mc-profile-card-copy">
        <small class="mc-profile-card-label"></small>
        <strong class="mc-profile-card-name"></strong>
      </span>
      <span class="mc-profile-card-change" aria-hidden="true">↻</span>`;
    document.body.appendChild(card);
  };

  const updateLanguage=()=>{
    const s=strings[getLang()];
    const set=(id,value)=>{const el=document.getElementById(id);if(el)el.textContent=value;};
    set('mc-profile-eyebrow',s.eyebrow);
    set('mc-profile-title',s.title);
    set('mc-profile-copy',s.copy);
    set('mc-profile-submit',s.continue);
    set('mc-profile-cancel',s.cancel);
    const input=document.getElementById('mc-profile-name');
    if(input)input.placeholder=s.placeholder;
    const label=document.querySelector('.mc-profile-card-label');
    if(label)label.textContent=s.profile;
    const card=document.getElementById('mc-profile-card');
    if(card)card.setAttribute('aria-label',s.change);
  };

  const renderCard=profile=>{
    const card=document.getElementById('mc-profile-card');
    if(!card)return;
    if(!profile){
      card.hidden=true;
      return;
    }
    const img=card.querySelector('.mc-profile-card-head');
    const name=card.querySelector('.mc-profile-card-name');
    if(img){
      img.src=profile.avatarUrl||avatarUrl(profile.uuid||profile.username);
      img.alt=profile.username+' Minecraft skin head';
    }
    if(name)name.textContent=profile.username;
    card.hidden=false;
    updateLanguage();
  };

  const updatePreview=value=>{
    const img=document.getElementById('mc-profile-preview-head');
    if(!img)return;
    const username=String(value||'').trim();
    if(USERNAME_PATTERN.test(username)){
      img.src=avatarUrl(username);
      img.alt=username+' Minecraft skin head';
      img.classList.add('is-ready');
    }else{
      img.removeAttribute('src');
      img.alt='';
      img.classList.remove('is-ready');
    }
  };

  const hideGate=()=>{
    const gate=document.getElementById('mc-profile-gate');
    if(!gate)return;
    gate.classList.remove('is-open');
    document.body.classList.remove('mc-profile-gate-open');
    window.setTimeout(()=>{gate.hidden=true;},180);
  };

  const showGate=(allowCancel=false)=>{
    const gate=document.getElementById('mc-profile-gate');
    const input=document.getElementById('mc-profile-name');
    const close=document.getElementById('mc-profile-close');
    const cancel=document.getElementById('mc-profile-cancel');
    const error=document.getElementById('mc-profile-error');
    const profile=readProfile();
    if(!gate||!input)return;

    updateLanguage();
    if(error)error.textContent='';
    close.hidden=!allowCancel;
    cancel.hidden=!allowCancel;
    input.value=profile?.username||'';
    updatePreview(input.value);

    gate.hidden=false;
    document.body.classList.add('mc-profile-gate-open');
    requestAnimationFrame(()=>gate.classList.add('is-open'));
    window.setTimeout(()=>{input.focus();input.select();},120);
  };

  const verifyUsername=async username=>{
    const response=await fetch('/api/minecraft/profile?username='+encodeURIComponent(username),{
      headers:{Accept:'application/json'},
      cache:'no-store'
    });
    let data={};
    try{data=await response.json();}catch{}
    if(!response.ok||!data.ok){
      const error=new Error(data.error||'verification_failed');
      error.code=data.error||'verification_failed';
      throw error;
    }
    return data;
  };

  const init=()=>{
    createUi();
    const profile=readProfile();
    renderCard(profile);
    if(profile)syncActivationInput(profile.username,false);

    const form=document.getElementById('mc-profile-form');
    const input=document.getElementById('mc-profile-name');
    const error=document.getElementById('mc-profile-error');
    const submit=document.getElementById('mc-profile-submit');
    const card=document.getElementById('mc-profile-card');
    const close=document.getElementById('mc-profile-close');
    const cancel=document.getElementById('mc-profile-cancel');

    input?.addEventListener('input',()=>{
      if(error)error.textContent='';
      updatePreview(input.value);
    });

    form?.addEventListener('submit',async event=>{
      event.preventDefault();
      const s=strings[getLang()];
      const username=(input?.value||'').trim();

      if(!USERNAME_PATTERN.test(username)){
        if(error)error.textContent=s.invalid;
        input?.focus();
        return;
      }

      if(error)error.textContent=s.checking;
      if(submit)submit.disabled=true;
      if(input)input.disabled=true;

      try{
        const data=await verifyUsername(username);
        const saved=writeProfile({
          username:data.username,
          uuid:data.uuid,
          avatarUrl:data.avatarUrl,
          savedAt:Date.now()
        });
        renderCard(saved);
        syncActivationInput(saved.username,true);
        hideGate();
      }catch(err){
        if(error)error.textContent=err?.code==='minecraft_user_not_found'?s.notFound:s.unavailable;
      }finally{
        if(submit)submit.disabled=false;
        if(input)input.disabled=false;
      }
    });

    card?.addEventListener('click',()=>showGate(true));
    close?.addEventListener('click',hideGate);
    cancel?.addEventListener('click',hideGate);

    document.addEventListener('keydown',event=>{
      const gate=document.getElementById('mc-profile-gate');
      if(event.key==='Escape'&&readProfile()&&gate&&!gate.hidden)hideGate();
    });

    document.querySelectorAll('.lang-btn').forEach(btn=>{
      btn.addEventListener('click',()=>window.setTimeout(updateLanguage,25));
    });

    if(!profile)showGate(false);
  };

  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});
  else init();
})();


/* Sleep Client generic payment command */
(function sleepPurchaseCommandComponent(){
  const PROFILE_KEY='sleep-minecraft-profile';
  const PURCHASE_KEY='sleep-selected-purchase';
  const PAY_TO='JulienFv';

  const copyText=async value=>{
    try{
      await navigator.clipboard.writeText(value);
      return true;
    }catch{
      try{
        const area=document.createElement('textarea');
        area.value=value;
        area.style.position='fixed';
        area.style.opacity='0';
        document.body.appendChild(area);
        area.select();
        const ok=document.execCommand('copy');
        area.remove();
        return ok;
      }catch{
        return false;
      }
    }
  };

  const lang=()=>{
    try{
      const stored=localStorage.getItem('language')||localStorage.getItem('site-language')||'';
      if(stored==='de'||stored==='en')return stored;
    }catch{}
    const active=document.querySelector('.lang-btn.active')?.dataset?.lang;
    if(active==='de'||active==='en')return active;
    return document.documentElement.lang==='de'?'de':'en';
  };

  const readProfile=()=>{
    try{return JSON.parse(localStorage.getItem(PROFILE_KEY)||'null');}
    catch{return null;}
  };

  const strings={
    en:{
      eyebrow:'PAYMENT',
      monthly:'Monthly Access',
      lifetime:'Lifetime Access',
      buyer:'Minecraft account',
      amount:'Amount',
      instruction:'Run this payment command exactly as shown:',
      note:'Pay from the same Minecraft account shown here. After the payment is verified, your product key can be created.',
      copy:'Copy Command',
      copied:'Copied!',
      close:'Close'
    },
    de:{
      eyebrow:'ZAHLUNG',
      monthly:'Monatlicher Zugang',
      lifetime:'Lifetime Zugang',
      buyer:'Minecraft Account',
      amount:'Betrag',
      instruction:'Führe diesen Zahlungsbefehl genau so aus:',
      note:'Bezahle mit demselben Minecraft Account, der hier angezeigt wird. Nach der Prüfung kann dein Product Key erstellt werden.',
      copy:'Befehl kopieren',
      copied:'Kopiert!',
      close:'Schließen'
    }
  };

  const createModal=()=>{
    if(document.getElementById('sleep-payment-modal'))return;
    const modal=document.createElement('div');
    modal.id='sleep-payment-modal';
    modal.className='sleep-payment-modal';
    modal.hidden=true;
    modal.innerHTML=`
      <div class="sleep-payment-backdrop" data-payment-close></div>
      <section class="sleep-payment-dialog" role="dialog" aria-modal="true" aria-labelledby="sleep-payment-title">
        <button class="sleep-payment-x" type="button" data-payment-close aria-label="Close">×</button>
        <div class="sleep-payment-eyebrow" id="sleep-payment-eyebrow"></div>
        <h2 id="sleep-payment-title"></h2>

        <div class="sleep-payment-info">
          <div><span id="sleep-payment-buyer-label"></span><strong id="sleep-payment-buyer"></strong></div>
          <div><span id="sleep-payment-amount-label"></span><strong id="sleep-payment-amount"></strong></div>
        </div>

        <p class="sleep-payment-instruction" id="sleep-payment-instruction"></p>

        <div class="sleep-payment-command-row">
          <code id="sleep-payment-command"></code>
          <button type="button" class="sleep-payment-copy" id="sleep-payment-copy"></button>
        </div>

        <p class="sleep-payment-note" id="sleep-payment-note"></p>
        <button type="button" class="btn btn-secondary sleep-payment-close-btn" data-payment-close id="sleep-payment-close"></button>
      </section>`;
    document.body.appendChild(modal);

    modal.querySelectorAll('[data-payment-close]').forEach(el=>{
      el.addEventListener('click',hideModal);
    });

    document.getElementById('sleep-payment-copy')?.addEventListener('click',async()=>{
      const command=document.getElementById('sleep-payment-command')?.textContent||'';
      const button=document.getElementById('sleep-payment-copy');
      const s=strings[lang()];
      if(!button||!command)return;
      const ok=await copyText(command);
      button.textContent=ok?s.copied:s.copy;
      if(ok)setTimeout(()=>{button.textContent=strings[lang()].copy;},1400);
    });
  };

  const hideModal=()=>{
    const modal=document.getElementById('sleep-payment-modal');
    if(!modal)return;
    modal.classList.remove('is-open');
    document.body.classList.remove('sleep-payment-open');
    setTimeout(()=>{modal.hidden=true;},180);
  };

  const showModal=plan=>{
    createModal();
    const profile=readProfile();
    const s=strings[lang()];
    if(!profile?.username){
      const card=document.getElementById('mc-profile-card');
      if(card&&!card.hidden)card.click();
      return;
    }

    const isLifetime=plan==='lifetime';
    const amount=isLifetime?'25m':'10m';
    const amountLabel=isLifetime?'25M':'10M';
    const command='/pay '+PAY_TO+' '+amount;

    try{
      localStorage.setItem(PURCHASE_KEY,JSON.stringify({
        username:profile.username,
        plan:isLifetime?'lifetime':'monthly',
        amount:amountLabel,
        command,
        selectedAt:Date.now()
      }));
    }catch{}

    const modal=document.getElementById('sleep-payment-modal');
    if(!modal)return;

    document.getElementById('sleep-payment-eyebrow').textContent=s.eyebrow;
    document.getElementById('sleep-payment-title').textContent=isLifetime?s.lifetime:s.monthly;
    document.getElementById('sleep-payment-buyer-label').textContent=s.buyer;
    document.getElementById('sleep-payment-buyer').textContent=profile.username;
    document.getElementById('sleep-payment-amount-label').textContent=s.amount;
    document.getElementById('sleep-payment-amount').textContent=amountLabel;
    document.getElementById('sleep-payment-instruction').textContent=s.instruction;
    document.getElementById('sleep-payment-command').textContent=command;
    document.getElementById('sleep-payment-copy').textContent=s.copy;
    document.getElementById('sleep-payment-note').textContent=s.note;
    document.getElementById('sleep-payment-close').textContent=s.close;

    modal.hidden=false;
    document.body.classList.add('sleep-payment-open');
    requestAnimationFrame(()=>modal.classList.add('is-open'));
  };

  document.addEventListener('click',event=>{
    const button=event.target.closest?.('.sleep-buy-btn');
    if(!button)return;
    event.preventDefault();
    event.stopPropagation();
    event.stopImmediatePropagation();
    showModal(button.dataset.plan);
  },true);

  document.addEventListener('keydown',event=>{
    const modal=document.getElementById('sleep-payment-modal');
    if(event.key==='Escape'&&modal&&!modal.hidden)hideModal();
  });
})();
