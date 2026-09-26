/* Sleep Client terms acceptance gate */
(function sleepTermsGate(){
  const KEY='sleep-terms-accepted-v1';
  const VERSION='2026-09-26';

  const lang=()=>{
    try{
      const stored=localStorage.getItem('language')||localStorage.getItem('site-language')||'';
      if(stored==='de'||stored==='en')return stored;
    }catch{}
    const active=document.querySelector('.lang-btn.active')?.dataset?.lang;
    if(active==='de'||active==='en')return active;
    return document.documentElement.lang==='de'?'de':'en';
  };

  const copy={
    de:{
      eyebrow:'NUTZUNGSBEDINGUNGEN',
      title:'Bevor du fortfährst',
      text:'Bitte lies und akzeptiere die Nutzungsbedingungen für Sleep Client.',
      checkbox:'Ich habe die Nutzungsbedingungen gelesen und akzeptiere sie.',
      open:'Nutzungsbedingungen öffnen',
      accept:'Akzeptieren und fortfahren',
      note:'Die Zustimmung zu diesen Bedingungen ersetzt keine Regeln von Minecraft, Mojang, Microsoft oder anderen Servern und Plattformen.'
    },
    en:{
      eyebrow:'TERMS OF USE',
      title:'Before you continue',
      text:'Please read and accept the Sleep Client terms of use.',
      checkbox:'I have read and accept the terms of use.',
      open:'Open terms of use',
      accept:'Accept and continue',
      note:'Accepting these terms does not replace the rules of Minecraft, Mojang, Microsoft or any other server or platform.'
    }
  };

  const accepted=()=>{
    try{
      const value=JSON.parse(localStorage.getItem(KEY)||'null');
      return value&&value.version===VERSION&&value.accepted===true;
    }catch{return false;}
  };

  const injectCss=()=>{
    if(document.querySelector('link[data-sleep-terms]'))return;
    const link=document.createElement('link');
    link.rel='stylesheet';
    link.href='./assets/terms-gate.css';
    link.dataset.sleepTerms='1';
    document.head.appendChild(link);
  };

  const addFooterLink=()=>{
    const row=document.querySelector('.footer-row');
    if(!row||row.querySelector('[data-terms-link]'))return;
    const a=document.createElement('a');
    a.href='./terms.html';
    a.textContent=lang()==='de'?'Nutzungsbedingungen':'Terms of Use';
    a.dataset.termsLink='1';
    a.className='footer-terms-link';
    row.appendChild(a);
  };

  const createGate=()=>{
    if(document.getElementById('sleep-terms-gate'))return;
    const s=copy[lang()];
    const gate=document.createElement('div');
    gate.id='sleep-terms-gate';
    gate.className='sleep-terms-gate';
    gate.innerHTML=`
      <div class="sleep-terms-backdrop"></div>
      <section class="sleep-terms-dialog" role="dialog" aria-modal="true" aria-labelledby="sleep-terms-title">
        <div class="sleep-terms-icon">SC</div>
        <div class="sleep-terms-eyebrow">${s.eyebrow}</div>
        <h2 id="sleep-terms-title">${s.title}</h2>
        <p class="sleep-terms-copy">${s.text}</p>

        <a class="sleep-terms-open" href="./terms.html" target="_blank" rel="noopener">${s.open} ↗</a>

        <label class="sleep-terms-check">
          <input id="sleep-terms-checkbox" type="checkbox">
          <span>${s.checkbox}</span>
        </label>

        <button class="btn btn-primary sleep-terms-accept" id="sleep-terms-accept" type="button" disabled>${s.accept}</button>
        <p class="sleep-terms-note">${s.note}</p>
      </section>`;
    document.body.appendChild(gate);
    document.body.classList.add('sleep-terms-locked');

    const checkbox=document.getElementById('sleep-terms-checkbox');
    const button=document.getElementById('sleep-terms-accept');

    checkbox?.addEventListener('change',()=>{
      if(button)button.disabled=!checkbox.checked;
    });

    button?.addEventListener('click',()=>{
      if(!checkbox?.checked)return;
      try{
        localStorage.setItem(KEY,JSON.stringify({
          accepted:true,
          version:VERSION,
          acceptedAt:Date.now()
        }));
      }catch{}
      gate.classList.add('is-leaving');
      document.body.classList.remove('sleep-terms-locked');
      window.setTimeout(()=>gate.remove(),180);
    });
  };

  const refreshLanguage=()=>{
    const link=document.querySelector('[data-terms-link]');
    if(link)link.textContent=lang()==='de'?'Nutzungsbedingungen':'Terms of Use';
  };

  const init=()=>{
    injectCss();
    addFooterLink();

    document.querySelectorAll('.lang-btn').forEach(btn=>{
      btn.addEventListener('click',()=>window.setTimeout(refreshLanguage,30));
    });

    const onTermsPage=document.body?.dataset?.page==='terms'||/\/terms\.html$/i.test(location.pathname);
    if(!onTermsPage&&!accepted())createGate();
  };

  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});
  else init();
})();