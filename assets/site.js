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
