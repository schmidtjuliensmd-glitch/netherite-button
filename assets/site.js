const enToDe={
"Open menu":"Menü öffnen",
"Home":"Startseite",
"Resource Packs":"Ressourcenpakete",
"and more.":"und mehr.",
"A modern download site for Minecraft Java. The Netherite Button mod is already available. More mods, resource packs and clients will follow.":"Eine moderne Download-Seite für Minecraft Java. Der Netherite Button Mod ist bereits verfügbar. Weitere Mods, Ressourcenpakete und Clients folgen später.",
"View Netherite Button →":"Netherite Button ansehen →",
"All Mods":"Alle Mods",
"Live Preview":"Live Vorschau",
"Mod available":"Mod verfügbar",
"Spruce button in Netherite style":"Fichtenknopf im Netherite-Look",
"Library":"Bibliothek",
"Your sections.":"Deine Bereiche.",
"The navigation is already prepared for more downloads. Categories that are not yet published will show “Coming Soon” for now.":"Die Navigation ist bereits für weitere Downloads vorbereitet. Noch nicht veröffentlichte Kategorien zeigen vorerst „Coming Soon“.",
"Available":"Verfügbar",
"Client mods with direct downloads.":"Client-Mods mit direktem Download.",
"Open Mods →":"Mods öffnen →",
"Visual Minecraft customizations and small graphical improvements.":"Optische Minecraft-Anpassungen und kleine visuelle Verbesserungen.",
"View section →":"Bereich ansehen →",
"A dedicated section for more Minecraft clients is ready.":"Ein eigener Bereich für weitere Minecraft-Clients ist vorbereitet.",
"Current Download":"Aktueller Download",
"The spruce button is displayed as a Netherite Ingot on your client.":"Der Fichtenknopf wird auf deinem Client als Netherite Ingot dargestellt.",
"The spruce button is displayed as a Netherite Ingot":"Fichtenknopf wird als Netherite Ingot dargestellt",
"Client-side visual change for Minecraft Java 1.21.11":"Client-seitige Darstellung für Minecraft Java 1.21.11",
"Download Library":"Download-Bibliothek",
"All published mods will appear here. Netherite Button is currently the first available download.":"Hier landen alle veröffentlichten Mods. Der Netherite Button ist aktuell der erste verfügbare Download.",
"1 mod available":"1 Mod verfügbar",
"In-game screenshot coming soon":"Ingame-Screenshot folgt",
"Fabric client mod for Minecraft 1.21.11. The spruce button is displayed in a Netherite style.":"Fabric Client-Mod für Minecraft 1.21.11. Der Fichtenknopf wird im Netherite-Look dargestellt.",
"Screenshot coming soon":"Screenshot folgt",
"Reserved for an upcoming mod with preview image, description, version and download.":"Platz für einen kommenden Mod mit Vorschaubild, Beschreibung, Version und Download.",
"Future resource packs will appear here with real in-game previews, version information, description and direct download.":"Hier erscheinen künftig Ressourcenpakete mit echten Ingame-Vorschaubildern, Versionsangabe, Beschreibung und direktem Download.",
"Reserved for an upcoming resource pack with preview image, description, version and download.":"Platz für ein kommendes Ressourcenpaket mit Vorschaubild, Beschreibung, Version und Download.",
"Different Minecraft clients can be added here later with preview image, features, version and download.":"Hier können später verschiedene Minecraft-Clients mit Vorschaubild, Funktionen, Version und Download eingebaut werden.",
"Reserved for an upcoming client with preview image, description, version and download.":"Platz für einen kommenden Client mit Vorschaubild, Beschreibung, Version und Download.",
"Fabric Client Mod · Minecraft 1.21.11":"Fabric Client-Mod · Minecraft 1.21.11",
"The spruce button is displayed as a Netherite Ingot on your Minecraft client.":"Der Fichtenknopf wird auf deinem Minecraft-Client als Netherite Ingot dargestellt.",
"Download mod ↓":"Mod herunterladen ↓",
"View preview":"Vorschau ansehen",
"Client-side":"Client-seitig",
"Features":"Funktionen",
"Netherite look for the spruce button.":"Netherite-Look für den Fichtenknopf.",
"The spruce button is displayed as a Netherite Ingot.":"Der Fichtenknopf wird als Netherite Ingot dargestellt.",
"Client-side visual change for Minecraft Java 1.21.11.":"Client-seitige Änderung für Minecraft Java 1.21.11.",
"The visual change only affects your own Minecraft client.":"Die Darstellung betrifft nur deinen eigenen Minecraft-Client.",
"This is what it looks like.":"So sieht es aus.",
"Your real screenshots will appear here. The gallery and full-screen preview are already prepared.":"Deine echten Screenshots kommen gleich hier hinein. Die Galerie und Vollbild-Vorschau sind schon vorbereitet.",
"Inventory view":"Inventaransicht",
"Item preview":"Item-Vorschau",
"In-game view":"Ingame-Ansicht",
"Quick setup.":"Schnell eingerichtet.",
"Install Fabric":"Fabric installieren",
"Install Fabric Loader for Minecraft 1.21.11 and place Fabric API in the mods folder.":"Fabric Loader für Minecraft 1.21.11 installieren und Fabric API in den Mods-Ordner legen.",
"Add JAR":"JAR einfügen",
"Place the downloaded mod file unopened in the":"Die heruntergeladene Mod-Datei ungeöffnet in den",
"folder.":"Ordner legen.",
"Start Minecraft":"Minecraft starten",
"Start Minecraft with the Fabric profile and use the new visual change in-game.":"Minecraft mit dem Fabric-Profil starten und die neue Darstellung im Spiel verwenden.",
"The mod only changes the visuals on your own Minecraft client.":"Der Mod ändert nur die Darstellung auf deinem eigenen Minecraft-Client.",
"Ready to test.":"Bereit zum Testen.",
"Download the current version directly.":"Lade die aktuelle Version direkt herunter.",
"Current version available":"Aktuelle Version verfügbar",
"Unofficial fan project. Not affiliated with Mojang Studios or Microsoft.":"Inoffizielles Fanprojekt. Nicht mit Mojang Studios oder Microsoft verbunden.",
"Language":"Sprache",
"Search mods...":"Mods durchsuchen...",
"Search resource packs...":"Ressourcenpakete durchsuchen...",
"Search clients...":"Clients durchsuchen...",
"Minecraft resource packs at JulienMChub.":"Minecraft Ressourcenpakete bei JulienMChub.",
"Minecraft clients at JulienMChub.":"Minecraft Clients bei JulienMChub.",
"Netherite Button Fabric client mod for Minecraft Java 1.21.11.":"Netherite Button Fabric Client-Mod für Minecraft Java 1.21.11.",
"Resource Packs | JulienMChub":"Ressourcenpakete | JulienMChub"
};
for(let i=1;i<=9;i++){
  const n=String(i).padStart(2,'0');
  enToDe[`Resource Pack Slot ${n}`]=`Ressourcenpaket Slot ${n}`;
}
const deToEn=Object.fromEntries(Object.entries(enToDe).map(([en,de])=>[de,en]));
function translateValue(value,map){
  if(!value)return value;
  const m=value.match(/^(\s*)([\s\S]*?)(\s*)$/);
  if(!m)return value;
  const translated=map[m[2]];
  return translated===undefined?value:m[1]+translated+m[3];
}
function applyLanguage(lang){
  const map=lang==='de'?enToDe:deToEn;
  const walker=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT);
  const nodes=[];
  while(walker.nextNode())nodes.push(walker.currentNode);
  nodes.forEach(node=>{
    const parent=node.parentElement;
    if(parent&&['SCRIPT','STYLE'].includes(parent.tagName))return;
    node.nodeValue=translateValue(node.nodeValue,map);
  });
  document.querySelectorAll('[placeholder]').forEach(el=>el.placeholder=translateValue(el.placeholder,map));
  document.querySelectorAll('[aria-label]').forEach(el=>el.setAttribute('aria-label',translateValue(el.getAttribute('aria-label'),map)));
  document.title=translateValue(document.title,map);
  const meta=document.querySelector('meta[name="description"]');
  if(meta)meta.content=translateValue(meta.content,map);
  document.documentElement.lang=lang;
  document.querySelectorAll('[data-lang]').forEach(btn=>btn.classList.toggle('active',btn.dataset.lang===lang));
}
let siteLanguage=localStorage.getItem('jmc-language');
if(siteLanguage!=='de'&&siteLanguage!=='en')siteLanguage='en';
applyLanguage(siteLanguage);
document.querySelectorAll('[data-lang]').forEach(btn=>btn.addEventListener('click',()=>{
  siteLanguage=btn.dataset.lang;
  localStorage.setItem('jmc-language',siteLanguage);
  applyLanguage(siteLanguage);
}));

const glow=document.getElementById('cursor-glow');
let raf=0,x=0,y=0;
window.addEventListener('pointermove',e=>{
  if(!glow||e.pointerType==='touch')return;
  x=e.clientX;y=e.clientY;glow.style.opacity='1';
  if(!raf)raf=requestAnimationFrame(()=>{glow.style.left=x+'px';glow.style.top=y+'px';raf=0;});
});
document.documentElement.addEventListener('mouseleave',()=>{if(glow)glow.style.opacity='0'});

const navToggle=document.querySelector('.mobile-toggle');
const navLinks=document.querySelector('.nav-links');
if(navToggle&&navLinks)navToggle.addEventListener('click',()=>navLinks.classList.toggle('open'));

const current=document.body.dataset.page;
document.querySelectorAll('[data-nav]').forEach(a=>{if(a.dataset.nav===current)a.classList.add('active')});

const io=new IntersectionObserver(entries=>entries.forEach(entry=>{
  if(entry.isIntersecting){entry.target.classList.add('visible');io.unobserve(entry.target)}
}),{threshold:.12});
document.querySelectorAll('.reveal').forEach(el=>io.observe(el));

document.querySelectorAll('[data-tilt]').forEach(card=>{
  card.addEventListener('pointermove',e=>{
    if(e.pointerType==='touch')return;
    const r=card.getBoundingClientRect();
    const rx=((e.clientY-r.top)/r.height-.5)*-5;
    const ry=((e.clientX-r.left)/r.width-.5)*6;
    card.style.transform=`perspective(900px) rotateX(${rx}deg) rotateY(${ry}deg) translateY(-3px)`;
  });
  card.addEventListener('pointerleave',()=>card.style.transform='');
});

document.querySelectorAll('[data-search]').forEach(input=>{
  input.addEventListener('input',()=>{
    const q=input.value.trim().toLowerCase();
    document.querySelectorAll('[data-search-item]').forEach(item=>{
      item.style.display=item.innerText.toLowerCase().includes(q)?'':'none';
    });
  });
});

const lightbox=document.querySelector('.lightbox');
document.querySelectorAll('[data-lightbox]').forEach(img=>{
  img.addEventListener('click',()=>{
    if(!lightbox)return;
    const target=lightbox.querySelector('img');
    target.src=img.src;target.alt=img.alt||'Screenshot';
    lightbox.classList.add('open');
  });
});
if(lightbox){
  lightbox.addEventListener('click',e=>{if(e.target===lightbox||e.target.classList.contains('lightbox-close'))lightbox.classList.remove('open')});
}
