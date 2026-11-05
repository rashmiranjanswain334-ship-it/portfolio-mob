(() => {
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  const $ = (s) => document.querySelector(s);
  const $$ = (s) => [...document.querySelectorAll(s)];

  /* 1. Hero name: split into letters so CSS can raise them one by one */
  const name = $('[data-split]');
  if (name) {
    const text = name.textContent.trim();
    name.setAttribute('aria-label', text);
    name.textContent = '';
    let i = 0;
    text.split(/\s+/).forEach((w) => {
      const word = document.createElement('span');
      word.className = 'word';
      word.setAttribute('aria-hidden', 'true');
      [...w].forEach((ch) => {
        const s = document.createElement('span');
        s.className = 'letter';
        s.style.setProperty('--i', i++);
        s.textContent = ch;
        word.appendChild(s);
      });
      name.appendChild(word);
    });
  }

  /* 2. Typing effect for the role line */
  const typed = $('[data-typed]');
  if (typed) {
    const words = (typed.dataset.words || '').split(',').map((s) => s.trim()).filter(Boolean);
    if (words.length) {
      if (reduce) typed.textContent = words[0];
      else {
        let w = 0, c = 0, del = false;
        const tick = () => {
          const word = words[w];
          typed.textContent = word.slice(0, c);
          if (!del && c === word.length) { del = true; return setTimeout(tick, 1600); }
          if (del && c === 0) { del = false; w = (w + 1) % words.length; }
          c += del ? -1 : 1;
          setTimeout(tick, del ? 35 : 75);
        };
        setTimeout(tick, 1000);
      }
    }
  }

  /* 3. Particle network. Dots drift, link up, and reach toward the cursor. */
  const cv = $('#field');
  if (cv && !reduce) {
    const ctx = cv.getContext('2d');
    let w = 0, h = 0, pts = [], vis = false, running = false;
    const mouse = { x: -9999, y: -9999 };
    const seg = (a, b) => { ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke(); };
    const resize = () => {
      const r = cv.getBoundingClientRect(), d = Math.min(devicePixelRatio || 1, 2);
      w = r.width; h = r.height;
      cv.width = w * d; cv.height = h * d;
      ctx.setTransform(d, 0, 0, d, 0, 0);
      const n = Math.min(110, Math.floor((w * h) / 14000));
      pts = Array.from({ length: n }, () => ({
        x: Math.random() * w, y: Math.random() * h,
        vx: (Math.random() - 0.5) * 0.35, vy: (Math.random() - 0.5) * 0.35,
      }));
    };
    addEventListener('resize', resize);
    addEventListener('pointermove', (e) => {
      const r = cv.getBoundingClientRect();
      mouse.x = e.clientX - r.left; mouse.y = e.clientY - r.top;
    });
    const loop = () => {
      if (!vis) { running = false; return; }
      ctx.clearRect(0, 0, w, h);
      for (const p of pts) {
        p.x += p.vx; p.y += p.vy;
        if (p.x < 0 || p.x > w) p.vx *= -1;
        if (p.y < 0 || p.y > h) p.vy *= -1;
      }
      ctx.lineWidth = 1;
      for (let i = 0; i < pts.length; i++) {
        const a = pts[i];
        ctx.fillStyle = 'rgba(233,243,241,.7)';
        ctx.beginPath(); ctx.arc(a.x, a.y, 1.4, 0, 6.283); ctx.fill();
        for (let j = i + 1; j < pts.length; j++) {
          const b = pts[j], d = Math.hypot(a.x - b.x, a.y - b.y);
          if (d < 110) { ctx.strokeStyle = `rgba(45,212,191,${(1 - d / 110) * 0.35})`; seg(a, b); }
        }
        const dm = Math.hypot(a.x - mouse.x, a.y - mouse.y);
        if (dm < 170) { ctx.strokeStyle = `rgba(255,179,71,${(1 - dm / 170) * 0.8})`; seg(a, mouse); }
      }
      requestAnimationFrame(loop);
    };
    resize();
    new IntersectionObserver(([e]) => {
      vis = e.isIntersecting;
      if (vis && !running) { running = true; loop(); }
    }).observe(cv);
  }

  /* 4. Project cards tilt toward the cursor, with a light that follows it */
  if (!reduce) {
    $$('.tilt').forEach((el) => {
      el.addEventListener('pointermove', (e) => {
        if (e.pointerType !== 'mouse') return;
        const r = el.getBoundingClientRect();
        const x = (e.clientX - r.left) / r.width, y = (e.clientY - r.top) / r.height;
        el.style.transform = `perspective(900px) rotateX(${(0.5 - y) * 10}deg) rotateY(${(x - 0.5) * 12}deg) translateY(-4px)`;
        el.style.setProperty('--gx', x * 100 + '%');
        el.style.setProperty('--gy', y * 100 + '%');
      });
      el.addEventListener('pointerleave', () => { el.style.transform = ''; });
    });
  }

  /* 5. Reveal on scroll */
  const io = new IntersectionObserver((entries) => entries.forEach((e) => {
    if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); }
  }), { threshold: 0.15 });
  $$('.reveal').forEach((el) => io.observe(el));

  /* 6. Scroll progress bar, nav background, cursor light */
  const bar = $('#bar'), nav = $('#nav'), glow = $('#glow');
  const onScroll = () => {
    const max = document.documentElement.scrollHeight - innerHeight;
    if (bar) bar.style.transform = `scaleX(${max > 0 ? scrollY / max : 0})`;
    if (nav) nav.classList.toggle('scrolled', scrollY > 40);
  };
  addEventListener('scroll', onScroll, { passive: true });
  onScroll();
  if (glow && !reduce && matchMedia('(pointer: fine)').matches) {
    addEventListener('pointermove', (e) => { glow.style.transform = `translate(${e.clientX}px, ${e.clientY}px)`; });
  }

  /* 7. Phone menu */
  const btn = $('#menu-btn'), menu = $('#menu');
  if (btn && menu) {
    const set = (open) => {
      menu.classList.toggle('hidden', !open);
      nav.classList.toggle('open', open);
      btn.setAttribute('aria-expanded', String(open));
      btn.textContent = open ? 'Close' : 'Menu';
    };
    btn.addEventListener('click', () => set(menu.classList.contains('hidden')));
    menu.querySelectorAll('a').forEach((a) => a.addEventListener('click', () => set(false)));
  }
})();
