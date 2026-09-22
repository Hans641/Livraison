/* ==========================================================================
   LIVRAISON — app.js
   Thème sombre · sidebar · toasts · confirmations · validation de formulaires ·
   data tables (recherche live + tri) · aperçu rapide · compteurs animés.
   Dépend de : bootstrap.bundle.min.js (chargé avant, en defer).
   Tout est déclaratif : les templates Thymeleaf posent des attributs data-*.
   ========================================================================== */
(function () {
  'use strict';

  var doc = document;
  var html = doc.documentElement;
  var $ = function (s, r) { return (r || doc).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || doc).querySelectorAll(s)); };
  var reduced = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  var store = {
    get: function (k) { try { return localStorage.getItem(k); } catch (e) { return null; } },
    set: function (k, v) { try { localStorage.setItem(k, v); } catch (e) { /* ignore */ } }
  };

  /** Minuscules + suppression des accents : « Tanà » se trouve en tapant « tana ». */
  function norm(s) {
    return String(s == null ? '' : s).normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  }

  function el(tag, cls, attrs) {
    var n = doc.createElement(tag);
    if (cls) n.className = cls;
    if (attrs) Object.keys(attrs).forEach(function (k) { n.setAttribute(k, attrs[k]); });
    return n;
  }

  /* ------------------------------------------------------------------
     THÈME (clair / sombre)
     ------------------------------------------------------------------ */
  var themeListeners = [];
  var Theme = {
    get: function () { return html.getAttribute('data-bs-theme') || 'light'; },
    set: function (t, persist) {
      var apply = function () {
        html.setAttribute('data-bs-theme', t);
        themeListeners.forEach(function (cb) { try { cb(t); } catch (e) { /* ignore */ } });
        doc.dispatchEvent(new CustomEvent('ld:themechange', { detail: { theme: t } }));
      };
      // Fondu natif entre les deux thèmes quand le navigateur le permet
      if (doc.startViewTransition && !reduced) { doc.startViewTransition(apply); } else { apply(); }
      if (persist !== false) store.set('ld-theme', t);
    },
    toggle: function () { Theme.set(Theme.get() === 'dark' ? 'light' : 'dark'); }
  };

  if (window.matchMedia) {
    var mq = window.matchMedia('(prefers-color-scheme: dark)');
    var onSystem = function (e) { if (!store.get('ld-theme')) Theme.set(e.matches ? 'dark' : 'light', false); };
    if (mq.addEventListener) mq.addEventListener('change', onSystem);
  }

  doc.addEventListener('click', function (e) {
    var btn = e.target.closest('[data-theme-toggle]');
    if (!btn) return;
    btn.classList.remove('spin'); void btn.offsetWidth; btn.classList.add('spin');
    Theme.toggle();
  });

  /* ------------------------------------------------------------------
     SIDEBAR
     ------------------------------------------------------------------ */
  var desktop = window.matchMedia('(min-width: 992px)');

  function syncSidebarAria() {
    var open = desktop.matches
      ? html.getAttribute('data-sidebar') !== 'collapsed'
      : doc.body.classList.contains('sidebar-open');
    $$('[data-sidebar-toggle]').forEach(function (b) { b.setAttribute('aria-expanded', open ? 'true' : 'false'); });
  }

  function closeDrawer() {
    if (!doc.body.classList.contains('sidebar-open')) return;
    doc.body.classList.remove('sidebar-open');
    syncSidebarAria();
    var t = $('[data-sidebar-toggle]');
    if (t) t.focus();
  }

  doc.addEventListener('click', function (e) {
    if (e.target.closest('[data-sidebar-toggle]')) {
      if (desktop.matches) {
        var collapsed = html.getAttribute('data-sidebar') === 'collapsed';
        if (collapsed) html.removeAttribute('data-sidebar'); else html.setAttribute('data-sidebar', 'collapsed');
        store.set('ld-sidebar', collapsed ? 'expanded' : 'collapsed');
      } else {
        doc.body.classList.toggle('sidebar-open');
        if (doc.body.classList.contains('sidebar-open')) {
          var first = $('.sidebar .nav-link'); if (first) first.focus();
        }
      }
      syncSidebarAria();
    } else if (e.target.closest('[data-sidebar-close]')) {
      closeDrawer();
    } else if (!desktop.matches && e.target.closest('.sidebar .nav-link')) {
      doc.body.classList.remove('sidebar-open');
    }
  });
  doc.addEventListener('keydown', function (e) { if (e.key === 'Escape') closeDrawer(); });
  if (desktop.addEventListener) desktop.addEventListener('change', function () { doc.body.classList.remove('sidebar-open'); syncSidebarAria(); });

  function initShell() {
    // Lien actif : le chemin le plus long qui correspond à l'URL courante
    var links = $$('.sidebar .nav-link');
    if (links.length) {
      var cur = location.pathname.replace(/\/+$/, '') || '/';
      var dash = links[0];
      var dashPath = new URL(dash.href, location.origin).pathname;
      var root = dashPath.replace(/dashboard$/, '').replace(/\/+$/, '') || '/';
      if (cur === root) cur = dashPath;
      var best = null, bestLen = -1;
      links.forEach(function (a) {
        var p = new URL(a.href, location.origin).pathname.replace(/\/+$/, '');
        if ((cur === p || cur.indexOf(p + '/') === 0) && p.length > bestLen) { best = a; bestLen = p.length; }
      });
      if (best) { best.classList.add('active'); best.setAttribute('aria-current', 'page'); }
    }

    // Titre de la topbar = titre du document sans le suffixe
    var pt = $('[data-page-title]');
    if (pt) pt.textContent = doc.title.replace(/\s*-\s*Livraison\s*$/, '') || 'Livraison';

    // Initiales de l'avatar
    $$('[data-avatar-from]').forEach(function (a) {
      var src = $(a.getAttribute('data-avatar-from'), a.closest('.sidebar-user') || doc);
      var name = (src ? src.textContent : '').trim();
      if (!name) return;
      var parts = name.split(/[\s._-]+/).filter(Boolean);
      a.textContent = (parts.length > 1 ? parts[0][0] + parts[1][0] : name.slice(0, 2)).toUpperCase();
    });

    // Salutation + date
    $$('[data-greeting]').forEach(function (n) {
      var h = new Date().getHours();
      n.textContent = (h >= 18 || h < 5) ? 'Bonsoir' : 'Bonjour';
    });
    $$('[data-today]').forEach(function (n) {
      var s = new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
      n.textContent = s.charAt(0).toUpperCase() + s.slice(1);
    });

    syncSidebarAria();
  }

  /* ------------------------------------------------------------------
     TOASTS
     ------------------------------------------------------------------ */
  var ICONS = { success: 'fa-circle-check', error: 'fa-circle-exclamation', warning: 'fa-triangle-exclamation', info: 'fa-circle-info' };
  var TITLES = { success: 'Succès', error: 'Erreur', warning: 'Attention', info: 'Information' };

  function toastStack() {
    var s = $('.toast-stack');
    if (!s) { s = el('div', 'toast-stack', { 'aria-live': 'polite' }); doc.body.appendChild(s); }
    return s;
  }

  /**
   * LD.toast('Message', { type: 'success'|'error'|'warning'|'info', title: '…', timeout: 4500 })
   */
  function toast(message, opts) {
    opts = opts || {};
    var type = opts.type || 'info';
    var ms = opts.timeout != null ? opts.timeout : (type === 'error' ? 7000 : 4500);
    var stack = toastStack();

    // 4 toasts maximum à l'écran
    var live = $$('.ld-toast', stack);
    if (live.length >= 4 && window.bootstrap) {
      var old = window.bootstrap.Toast.getInstance(live[0]);
      if (old) old.hide(); else live[0].remove();
    }

    var t = el('div', 'toast ld-toast ld-toast-' + type, { role: type === 'error' ? 'alert' : 'status', 'aria-atomic': 'true' });
    t.style.setProperty('--ms', ms + 'ms');
    var body = el('div', 'ld-toast-body');
    var ico = el('span', 'ld-toast-ico'); ico.appendChild(el('i', 'fa-solid ' + ICONS[type], { 'aria-hidden': 'true' }));
    var txt = el('div');
    var title = el('div', 'ld-toast-title'); title.textContent = opts.title || TITLES[type];
    var msg = el('div', 'ld-toast-msg'); msg.textContent = message;
    txt.appendChild(title); txt.appendChild(msg);
    var close = el('button', 'ld-toast-close', { type: 'button', 'aria-label': 'Fermer' });
    close.appendChild(el('i', 'fa-solid fa-xmark', { 'aria-hidden': 'true' }));
    body.appendChild(ico); body.appendChild(txt); body.appendChild(close);
    t.appendChild(body);
    if (ms > 0) t.appendChild(el('div', 'ld-toast-bar'));
    stack.appendChild(t);

    var inst = new window.bootstrap.Toast(t, { autohide: false });
    var timer = null, remaining = ms, started = 0;
    function arm() { if (ms <= 0) return; started = Date.now(); timer = setTimeout(function () { inst.hide(); }, remaining); }
    function disarm() { clearTimeout(timer); remaining -= Date.now() - started; }
    t.addEventListener('mouseenter', disarm);
    t.addEventListener('mouseleave', arm);
    close.addEventListener('click', function () { clearTimeout(timer); inst.hide(); });
    t.addEventListener('hidden.bs.toast', function () { t.remove(); });
    inst.show();
    arm();
    return inst;
  }

  function initFlash() {
    var f = $('#ld-flash');
    if (!f) return;
    if (f.dataset.succes) toast(f.dataset.succes, { type: 'success' });
    if (f.dataset.erreur) toast(f.dataset.erreur, { type: 'error' });
  }

  /* ------------------------------------------------------------------
     CONFIRMATION (form[data-confirm="Message"])
     Options : data-confirm-title, data-confirm-label, data-confirm-tone="danger"
     ------------------------------------------------------------------ */
  var confirmModal = null;
  function buildConfirm() {
    var m = el('div', 'modal fade modal-confirm', { tabindex: '-1', 'aria-hidden': 'true', 'aria-labelledby': 'ld-confirm-title' });
    m.innerHTML =
      '<div class="modal-dialog modal-dialog-centered"><div class="modal-content">' +
      '<div class="modal-body p-4">' +
      '<div class="confirm-ico"><i class="fa-solid fa-circle-question" aria-hidden="true"></i></div>' +
      '<h2 class="h5 mb-2" id="ld-confirm-title"></h2><p></p></div>' +
      '<div class="modal-footer"><button type="button" class="btn secondary" data-bs-dismiss="modal">Annuler</button>' +
      '<button type="button" class="btn" data-ok></button></div>' +
      '</div></div>';
    doc.body.appendChild(m);
    return m;
  }

  function askConfirm(form, submitter) {
    if (!confirmModal) confirmModal = buildConfirm();
    var d = form.dataset;
    var danger = d.confirmTone === 'danger';
    confirmModal.classList.toggle('is-danger', danger);
    $('.confirm-ico i', confirmModal).className = 'fa-solid ' + (danger ? 'fa-triangle-exclamation' : 'fa-circle-question');
    $('h2', confirmModal).textContent = d.confirmTitle || 'Confirmer cette action ?';
    $('p', confirmModal).textContent = d.confirm;
    var ok = $('[data-ok]', confirmModal);
    ok.textContent = d.confirmLabel || 'Confirmer';
    ok.className = 'btn ' + (danger ? 'danger-solid' : '');
    var inst = window.bootstrap.Modal.getOrCreateInstance(confirmModal);
    var accepted = false;
    ok.onclick = function () { accepted = true; inst.hide(); };
    confirmModal.addEventListener('hidden.bs.modal', function once() {
      confirmModal.removeEventListener('hidden.bs.modal', once);
      if (!accepted) return;
      form.dataset.confirmed = '1';
      if (form.requestSubmit) form.requestSubmit(submitter && submitter.form === form ? submitter : undefined); else form.submit();
    });
    inst.show();
  }

  /* ------------------------------------------------------------------
     VALIDATION DES FORMULAIRES (form[data-validate])
     Messages en français, validation au blur puis en direct, focus sur la 1re erreur.
     Attributs utiles : data-positive (valeur > 0), data-msg-required="…"
     ------------------------------------------------------------------ */
  function controlsOf(form) {
    return Array.prototype.slice.call(form.elements).filter(function (c) {
      var t = (c.type || '').toLowerCase();
      return /^(input|select|textarea)$/i.test(c.tagName) && !c.disabled &&
        t !== 'hidden' && t !== 'submit' && t !== 'button' && t !== 'reset' && !c.hasAttribute('data-novalidate');
    });
  }

  function errorText(c) {
    var v = c.validity;
    if (v.customError) return c.validationMessage;
    if (v.valueMissing) return c.dataset.msgRequired || (c.tagName === 'SELECT' || c.type === 'radio' ? 'Faites un choix.' : 'Ce champ est obligatoire.');
    if (v.typeMismatch) return c.type === 'email' ? 'Adresse e-mail invalide.' : 'Format invalide.';
    if (v.badInput) return 'Saisissez une valeur valide.';
    if (v.rangeUnderflow) return 'La valeur doit être supérieure ou égale à ' + c.min + '.';
    if (v.rangeOverflow) return 'La valeur doit être inférieure ou égale à ' + c.max + '.';
    if (v.stepMismatch) return 'Valeur non valide.';
    if (v.tooShort) return 'Minimum ' + c.minLength + ' caractères.';
    if (v.patternMismatch) return c.dataset.msgPattern || 'Format invalide.';
    return 'Valeur non valide.';
  }

  function showState(c, message) {
    var field = c.closest('.field');
    if (!field) return;
    var err = $('.field-error', field);
    if (message) {
      if (!err) { err = el('p', 'field-error'); err.id = (c.id || c.name || 'f') + '-err'; field.appendChild(err); }
      err.textContent = message; err.hidden = false;
      field.classList.add('has-error');
      c.setAttribute('aria-invalid', 'true');
      c.setAttribute('aria-describedby', err.id || '');
    } else {
      if (err) { err.textContent = ''; err.hidden = true; }
      field.classList.remove('has-error');
      c.removeAttribute('aria-invalid');
      c.removeAttribute('aria-describedby');
    }
  }

  function validateControl(c) {
    c.setCustomValidity('');
    if (c.hasAttribute('data-positive') && c.value !== '' && !(parseFloat(c.value) > 0)) {
      c.setCustomValidity('La valeur doit être supérieure à 0.');
    }
    if (c.type !== 'radio' && typeof c.value === 'string' && c.hasAttribute('required') && c.value.trim() === '' && c.type !== 'number' && c.tagName !== 'SELECT') {
      c.setCustomValidity(c.dataset.msgRequired || 'Ce champ est obligatoire.'); // espaces seuls = vide
    }
    var ok = c.checkValidity();
    showState(c, ok ? '' : errorText(c));
    return ok;
  }

  function initValidation() {
    $$('form[data-validate]').forEach(function (f) { f.setAttribute('novalidate', ''); });
    $$('.choice-group[data-default]').forEach(function (g) {
      if (!$('input:checked', g)) { var d = $('input[value="' + g.dataset.default + '"]', g); if (d) d.checked = true; }
    });
  }

  doc.addEventListener('focusout', function (e) {
    var c = e.target, f = c.form;
    if (!f || !f.hasAttribute('data-validate') || controlsOf(f).indexOf(c) < 0) return;
    c.dataset.touched = '1';
    validateControl(c);
  });
  function live(e) {
    var c = e.target, f = c.form;
    if (!f || !f.hasAttribute('data-validate') || controlsOf(f).indexOf(c) < 0) return;
    var field = c.closest('.field');
    if (c.dataset.touched || (field && field.classList.contains('has-error')) || c.type === 'radio') validateControl(c);
  }
  doc.addEventListener('input', live);
  doc.addEventListener('change', live);

  // Phase de capture : confirmation d'abord, validation ensuite
  doc.addEventListener('submit', function (e) {
    var form = e.target;
    if (!(form instanceof HTMLFormElement)) return;

    if (form.hasAttribute('data-confirm') && form.dataset.confirmed !== '1') {
      e.preventDefault(); e.stopImmediatePropagation();
      askConfirm(form, e.submitter);
      return;
    }
    delete form.dataset.confirmed;

    if (form.hasAttribute('data-validate')) {
      var bad = [];
      controlsOf(form).forEach(function (c) { c.dataset.touched = '1'; if (!validateControl(c)) bad.push(c); });
      if (bad.length) {
        e.preventDefault(); e.stopImmediatePropagation();
        var first = bad[0];
        first.focus({ preventScroll: true });
        first.scrollIntoView({ block: 'center', behavior: reduced ? 'auto' : 'smooth' });
        var box = form.closest('.panel') || form;
        box.classList.remove('shake'); void box.offsetWidth; box.classList.add('shake');
        toast(bad.length === 1 ? 'Un champ est à corriger avant l\'envoi.' : bad.length + ' champs sont à corriger avant l\'envoi.',
          { type: 'error', title: 'Formulaire incomplet' });
      }
    }
  }, true);

  // Phase de bulle : si rien n'a bloqué l'envoi, on verrouille le bouton (anti double-clic)
  doc.addEventListener('submit', function (e) {
    var form = e.target;
    if (e.defaultPrevented || !(form instanceof HTMLFormElement)) return;
    if ((form.method || '').toLowerCase() !== 'post') return;
    var b = e.submitter || $('[type="submit"]', form);
    if (b && b.classList) { b.classList.add('is-loading'); b.setAttribute('aria-busy', 'true'); }
  });
  window.addEventListener('pageshow', function (e) {
    if (e.persisted) $$('.btn.is-loading').forEach(function (b) { b.classList.remove('is-loading'); b.removeAttribute('aria-busy'); });
  });

  /* ------------------------------------------------------------------
     DATA TABLES : recherche live + tri des colonnes (sur les lignes affichées)
     Markup : <table id="x" data-table data-noun="commande"> · tr[data-row] · th[data-sort="text|number"]
              <input data-table-search="x"> · [data-table-count="x"] · [data-table-empty="x"]
     ------------------------------------------------------------------ */
  var collator = new Intl.Collator('fr', { numeric: true, sensitivity: 'base' });

  function initTable(table) {
    var id = table.id;
    var tbody = table.tBodies[0];
    if (!id || !tbody) return;
    var input = $('[data-table-search="' + id + '"]');
    var count = $('[data-table-count="' + id + '"]');
    var empty = $('[data-table-empty="' + id + '"]');
    var noun = table.dataset.noun || 'ligne';
    var state = { col: -1, dir: 1 };

    function rows() { return $$(':scope > tr[data-row]', tbody); }
    function haystack(r) {
      if (!r._s) {
        r._s = norm($$('td', r).filter(function (td) { return !td.classList.contains('no-search'); })
          .map(function (td) { return td.textContent; }).join(' ').replace(/\s+/g, ' '));
      }
      return r._s;
    }

    function filter() {
      var raw = input ? input.value.trim() : '';
      var tokens = norm(raw).split(/\s+/).filter(Boolean);
      var all = rows(), vis = 0;
      all.forEach(function (r) {
        var ok = tokens.every(function (t) { return haystack(r).indexOf(t) > -1; });
        r.hidden = !ok;
        if (ok) vis++;
      });
      if (count) {
        count.textContent = tokens.length
          ? vis + ' résultat' + (vis > 1 ? 's' : '') + ' sur ' + all.length
          : all.length + ' ' + noun + (all.length > 1 ? 's' : '') + ' sur cette page';
      }
      if (empty) {
        empty.hidden = !(tokens.length && vis === 0);
        var q = $('[data-query]', empty); if (q) q.textContent = raw;
      }
      table.hidden = !!(tokens.length && vis === 0);
      var wrap = table.closest('.table-wrap'); if (wrap) wrap.hidden = table.hidden;
    }

    var timer;
    if (input) {
      input.addEventListener('input', function () { clearTimeout(timer); timer = setTimeout(filter, 60); });
      input.addEventListener('keydown', function (e) { if (e.key === 'Escape' && input.value) { input.value = ''; filter(); } });
      var clear = input.parentElement && $('.search-clear', input.parentElement);
      if (clear) clear.addEventListener('click', function () { input.value = ''; filter(); input.focus(); });
    }
    if (empty) {
      var rs = $('[data-table-reset]', empty);
      if (rs) rs.addEventListener('click', function () { if (input) { input.value = ''; filter(); input.focus(); } });
    }

    // Tri
    function cellValue(r, col, type) {
      var td = r.cells[col];
      var v = td ? (td.dataset.sortValue != null ? td.dataset.sortValue : td.textContent.trim()) : '';
      if (type === 'number') { var n = parseFloat(String(v).replace(',', '.').replace(/[^\d.\-]/g, '')); return isNaN(n) ? -Infinity : n; }
      return v;
    }
    function sortBy(col, dir) {
      var th = table.tHead.rows[0].cells[col];
      var type = th.dataset.sort || 'text';
      state.col = col; state.dir = dir;
      var arr = rows().sort(function (a, b) {
        var x = cellValue(a, col, type), y = cellValue(b, col, type);
        return (type === 'number' ? (x - y) : collator.compare(x, y)) * dir;
      });
      var i = 0;
      arr.forEach(function (r) {
        tbody.appendChild(r);
        if (!r.hidden && !reduced) {
          r.classList.remove('row-anim'); r.style.setProperty('--i', Math.min(i++, 14)); void r.offsetWidth;
          r.classList.add('row-anim');
        }
      });
      $$('th[data-sort]', table).forEach(function (h) { h.removeAttribute('aria-sort'); });
      th.setAttribute('aria-sort', dir === 1 ? 'ascending' : 'descending');
      if (sel) sel.value = col + ':' + dir;
    }

    var sortable = $$('thead th[data-sort]', table);
    var sel = null;
    sortable.forEach(function (th) {
      var label = th.textContent.trim();
      var b = el('button', 'sort-btn', { type: 'button' });
      b.textContent = label;
      th.textContent = ''; th.appendChild(b);
      th.dataset.label = label;
      th.addEventListener('click', function () {
        var col = th.cellIndex;
        sortBy(col, state.col === col && state.dir === 1 ? -1 : 1);
      });
    });
    tbody.addEventListener('animationend', function (e) { if (e.target.classList) e.target.classList.remove('row-anim'); });

    // Sélecteur de tri pour l'affichage mobile (les en-têtes y sont masqués)
    var tools = input && input.closest('.table-tools');
    if (tools && sortable.length) {
      sel = el('select', 'form-select table-sortby', { 'aria-label': 'Trier par' });
      sel.appendChild(new Option('Trier par…', ''));
      sortable.forEach(function (th) {
        var c = th.cellIndex, l = th.dataset.label;
        sel.appendChild(new Option(l + ' (croissant)', c + ':1'));
        sel.appendChild(new Option(l + ' (décroissant)', c + ':-1'));
      });
      sel.addEventListener('change', function () {
        if (!sel.value) return;
        var p = sel.value.split(':'); sortBy(parseInt(p[0], 10), parseInt(p[1], 10));
      });
      tools.insertBefore(sel, count || null);
    }

    filter();
  }

  // Raccourci « / » pour chercher
  doc.addEventListener('keydown', function (e) {
    if (e.key !== '/' || e.ctrlKey || e.metaKey || e.altKey) return;
    var t = e.target;
    if (t && (/^(input|textarea|select)$/i.test(t.tagName) || t.isContentEditable)) return;
    var s = $('input[data-table-search]');
    if (s) { e.preventDefault(); s.focus(); s.select(); }
  });

  /* ------------------------------------------------------------------
     APERÇU RAPIDE (modal #quickview alimenté par les data-* de la ligne)
     ------------------------------------------------------------------ */
  var ROUTE = ['CREEE', 'ASSIGNEE', 'PRISE_EN_CHARGE', 'EN_TRANSIT', 'LIVREE'];

  function openQuickView(tr) {
    var m = $('#quickview');
    if (!m) return;
    var d = tr.dataset;

    $$('[data-qv]', m).forEach(function (n) {
      var v = d[n.dataset.qv];
      n.textContent = (v != null && v !== '') ? v : (n.dataset.qvEmpty || '—');
    });

    var slot = $('[data-qv-badge]', m);
    var badge = $('[data-cell="statut"] .badge', tr);
    if (slot) slot.replaceChildren(badge ? badge.cloneNode(true) : doc.createTextNode(d.statut || ''));
    var prio = $('[data-qv-prio]', m), prioSrc = $('[data-cell="priorite"] .prio', tr);
    if (prio) prio.replaceChildren(prioSrc ? prioSrc.cloneNode(true) : doc.createTextNode('—'));

    var late = $('[data-qv-late]', m);
    if (late) late.hidden = !tr.classList.contains('is-late');

    // Progression
    var idx = ROUTE.indexOf(d.statut);
    var route = $('[data-qv-route]', m), banner = $('[data-qv-banner]', m);
    if (route) route.hidden = idx < 0;
    if (banner) {
      banner.hidden = idx >= 0;
      var bt = $('[data-qv-banner-text]', banner);
      if (bt && badge) bt.textContent = 'Statut : ' + badge.textContent.trim();
    }
    $$('[data-step]', m).forEach(function (li, i) {
      li.classList.toggle('is-done', idx > i);
      li.classList.toggle('is-current', idx === i);
    });

    var link = $('[data-qv-link]', m); if (link) link.href = d.href || '#';
    var cp = $('[data-qv-copy]', m); if (cp) cp.setAttribute('data-copy', d.numero || '');

    window.bootstrap.Modal.getOrCreateInstance(m).show();
  }
  doc.addEventListener('click', function (e) {
    var b = e.target.closest('[data-quickview]');
    if (!b) return;
    var tr = b.closest('tr');
    if (tr) { e.preventDefault(); openQuickView(tr); }
  });

  /* Modale de résolution d'incident : l'action du formulaire est fixée depuis la ligne */
  doc.addEventListener('click', function (e) {
    var b = e.target.closest('[data-resolve]');
    if (!b) return;
    var m = $('#resolveModal');
    if (!m) return;
    var form = $('form', m);
    form.action = form.dataset.actionTemplate.replace('ID_PLACEHOLDER', b.dataset.resolve);
    $$('[data-rv]', m).forEach(function (n) { n.textContent = b.dataset[n.dataset.rv] || ''; });
    var ta = $('textarea', m); ta.value = ''; showState(ta, '');
    delete ta.dataset.touched;
    var inst = window.bootstrap.Modal.getOrCreateInstance(m);
    m.addEventListener('shown.bs.modal', function once() { m.removeEventListener('shown.bs.modal', once); ta.focus(); });
    inst.show();
  });

  /* ------------------------------------------------------------------
     COPIER, COMPTEURS, DIVERS
     ------------------------------------------------------------------ */
  function copyText(text) {
    if (navigator.clipboard && window.isSecureContext) return navigator.clipboard.writeText(text).then(function () { return true; }, function () { return false; });
    return new Promise(function (resolve) {
      var ta = el('textarea'); ta.value = text; ta.style.position = 'fixed'; ta.style.opacity = '0';
      doc.body.appendChild(ta); ta.select();
      var ok = false; try { ok = doc.execCommand('copy'); } catch (err) { ok = false; }
      ta.remove(); resolve(ok);
    });
  }
  doc.addEventListener('click', function (e) {
    var b = e.target.closest('[data-copy]');
    if (!b) return;
    e.preventDefault();
    var text = b.getAttribute('data-copy');
    copyText(text).then(function (ok) {
      toast(ok ? text + ' copié dans le presse-papiers.' : 'Impossible de copier automatiquement.',
        { type: ok ? 'success' : 'error', title: ok ? 'Copié' : 'Erreur', timeout: 2500 });
    });
  });

  function countUp(node) {
    var target = parseFloat(node.dataset.count);
    if (isNaN(target) || reduced) return;
    var dec = parseInt(node.dataset.decimals || '0', 10);
    var final = node.textContent;
    var nf = new Intl.NumberFormat('fr-FR', { minimumFractionDigits: dec, maximumFractionDigits: dec });
    var dur = parseInt(node.dataset.duration || '1000', 10), t0 = performance.now();
    (function frame(t) {
      var p = Math.min(1, (t - t0) / dur);
      node.textContent = nf.format(target * (1 - Math.pow(1 - p, 4))).replace(/[\u202f\u00a0]/g, ' ');
      if (p < 1) requestAnimationFrame(frame); else node.textContent = final; // texte serveur restauré
    })(t0);
  }
  function initCounters() {
    var nodes = $$('[data-count]');
    if (!nodes.length) return;
    if (!('IntersectionObserver' in window)) { nodes.forEach(countUp); return; }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) { if (en.isIntersecting) { io.unobserve(en.target); countUp(en.target); } });
    }, { threshold: .3 });
    nodes.forEach(function (n) { io.observe(n); });
  }

  doc.addEventListener('click', function (e) { if (e.target.closest('[data-print]')) window.print(); });

  // Filtres serveur : soumission automatique au changement
  doc.addEventListener('change', function (e) {
    var s = e.target.closest('[data-autosubmit]');
    if (s && s.form) { if (s.form.requestSubmit) s.form.requestSubmit(); else s.form.submit(); }
  });

  // Périodes rapides (statistiques)
  function iso(d) { return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); }
  doc.addEventListener('click', function (e) {
    var b = e.target.closest('[data-range]');
    if (!b) return;
    var f = b.closest('form'), now = new Date(), from = null, to = now;
    switch (b.dataset.range) {
      case '7d': from = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 6); break;
      case '30d': from = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 29); break;
      case 'month': from = new Date(now.getFullYear(), now.getMonth(), 1); break;
      case 'year': from = new Date(now.getFullYear(), 0, 1); break;
      default: from = null; to = null;
    }
    f.elements['debut'].value = from ? iso(from) : '';
    f.elements['fin'].value = to ? iso(to) : '';
    if (f.requestSubmit) f.requestSubmit(); else f.submit();
  });

  // Connexion : afficher le mot de passe / remplir un compte de démonstration
  doc.addEventListener('click', function (e) {
    var t = e.target.closest('[data-toggle-password]');
    if (t) {
      var inp = $(t.getAttribute('data-toggle-password'));
      var show = inp.type === 'password';
      inp.type = show ? 'text' : 'password';
      t.setAttribute('aria-label', show ? 'Masquer le mot de passe' : 'Afficher le mot de passe');
      $('i', t).className = 'fa-solid ' + (show ? 'fa-eye-slash' : 'fa-eye');
      return;
    }
    var fill = e.target.closest('[data-fill-user]');
    if (fill) {
      $('#username').value = fill.dataset.fillUser;
      $('#password').value = fill.dataset.fillPass;
      $('#password').focus();
    }
  });

  /* ------------------------------------------------------------------
     INIT
     ------------------------------------------------------------------ */
  function init() {
    initShell();
    initValidation();
    initFlash();
    initCounters();
    $$('table[data-table]').forEach(initTable);
    // Menus dans un conteneur scrollable : position « fixed » pour ne pas être rognés
    $$('.table-wrap [data-bs-toggle="dropdown"]').forEach(function (b) {
      new window.bootstrap.Dropdown(b, { popperConfig: function (c) { c.strategy = 'fixed'; return c; } });
    });
    // Focus sur la 1re erreur renvoyée par le serveur
    var serverErr = $('.field.has-error .form-control, .field.has-error .form-select');
    if (serverErr) serverErr.focus({ preventScroll: false });
  }
  if (doc.readyState === 'loading') doc.addEventListener('DOMContentLoaded', init); else init();

  // API publique (utilisée par charts.js et les pages)
  window.LD = {
    toast: toast,
    theme: Theme,
    onTheme: function (cb) { themeListeners.push(cb); },
    reducedMotion: reduced,
    cssVar: function (name) { return getComputedStyle(html).getPropertyValue(name).trim(); }
  };
})();
