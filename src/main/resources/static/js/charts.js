/* ==========================================================================
   LIVRAISON — charts.js
   Graphiques Chart.js déclaratifs. Dépend de : chart.umd.min.js + app.js (window.LD).
   Markup attendu :
     <canvas id="x" data-chart="repartition" data-reussite="66.7" data-echec="8.3" [data-size="sm"]>
     <ul class="legend" data-legend-for="x">        ← légende interactive (facultative)
     <div data-empty-for="x">                       ← état vide (facultatif)
     <button data-chart-view="doughnut|bar" data-target="x">  ← bascule de vue (facultative)
   Les données proviennent uniquement des agrégats fournis par le serveur (taux de réussite/échec).
   ========================================================================== */
(function () {
  'use strict';
  if (!window.Chart || !window.LD) return;

  var LD = window.LD;
  var doc = document;
  var pct = new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
  var LABELS = ['Livrées', 'Échecs et retours', 'En cours ou annulées'];
  var live = new Map(); // canvas → instance Chart

  function palette() {
    return {
      colors: [LD.cssVar('--emerald'), LD.cssVar('--coral'), LD.cssVar('--slate')],
      text: LD.cssVar('--text'), text2: LD.cssVar('--text-2'), text3: LD.cssVar('--text-3'),
      line: LD.cssVar('--line-soft'), surface: LD.cssVar('--surface')
    };
  }

  function valuesOf(canvas) {
    var r = parseFloat(canvas.dataset.reussite) || 0;
    var e = parseFloat(canvas.dataset.echec) || 0;
    var o = Math.max(0, Math.round((100 - r - e) * 100) / 100);
    return [r, e, o];
  }

  // Texte au centre de l'anneau : le taux de réussite
  var centerText = {
    id: 'centerText',
    afterDraw: function (chart, args, opts) {
      if (chart.config.type !== 'doughnut' || !opts || !opts.value) return;
      var a = chart.chartArea, ctx = chart.ctx;
      var x = (a.left + a.right) / 2, y = (a.top + a.bottom) / 2;
      ctx.save();
      ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
      ctx.fillStyle = opts.color;
      ctx.font = '800 ' + opts.size + 'px "Plus Jakarta Sans", system-ui, sans-serif';
      ctx.fillText(opts.value, x, y - 8);
      ctx.fillStyle = opts.sub;
      ctx.font = '600 12px "Plus Jakarta Sans", system-ui, sans-serif';
      ctx.fillText(opts.label, x, y + opts.size * 0.55);
      ctx.restore();
    }
  };

  function buildLegend(canvas, chart, vals, colors) {
    var ul = doc.querySelector('[data-legend-for="' + canvas.id + '"]');
    if (!ul) return;
    ul.textContent = '';
    LABELS.forEach(function (label, i) {
      var li = doc.createElement('li');
      var b = doc.createElement('button');
      b.type = 'button'; b.className = 'legend-item'; b.style.setProperty('--c', colors[i]);
      b.setAttribute('aria-pressed', 'true');
      b.innerHTML = '<span class="sw"></span><span class="lbl"></span><span class="val"></span>';
      b.querySelector('.lbl').textContent = label;
      b.querySelector('.val').textContent = pct.format(vals[i]) + ' %';
      b.addEventListener('click', function () {
        chart.toggleDataVisibility(i); chart.update();
        var on = chart.getDataVisibility(i);
        b.classList.toggle('is-off', !on); b.setAttribute('aria-pressed', on ? 'true' : 'false');
      });
      function hl(on) {
        if (!chart.getDataVisibility(i)) return;
        var act = on ? [{ datasetIndex: 0, index: i }] : [];
        chart.setActiveElements(act);
        if (chart.tooltip) chart.tooltip.setActiveElements(act, { x: 0, y: 0 });
        chart.update('none');
      }
      b.addEventListener('mouseenter', function () { hl(true); });
      b.addEventListener('mouseleave', function () { hl(false); });
      b.addEventListener('focus', function () { hl(true); });
      b.addEventListener('blur', function () { hl(false); });
      li.appendChild(b); ul.appendChild(li);
    });
  }

  function build(canvas, animate) {
    var old = live.get(canvas);
    if (old) { old.destroy(); live.delete(canvas); }

    var vals = valuesOf(canvas), p = palette();
    var empty = doc.querySelector('[data-empty-for="' + canvas.id + '"]');
    var legend = doc.querySelector('[data-legend-for="' + canvas.id + '"]');
    var hasData = vals[0] + vals[1] > 0;
    canvas.parentElement.hidden = !hasData;
    if (legend) legend.hidden = !hasData;
    if (empty) empty.classList.toggle('show', !hasData);
    if (!hasData) return;

    var donut = (canvas.dataset.view || 'doughnut') === 'doughnut';
    var size = canvas.dataset.size === 'sm' ? 28 : 34;
    var anim = (LD.reducedMotion || animate === false) ? false : { duration: 900, easing: 'easeOutQuart' };

    var cfg = {
      type: donut ? 'doughnut' : 'bar',
      data: {
        labels: LABELS,
        datasets: [{
          data: vals,
          backgroundColor: p.colors,
          borderColor: donut ? p.surface : 'transparent',
          borderWidth: donut ? 3 : 0,
          borderRadius: donut ? 6 : 8,
          borderSkipped: false,
          hoverOffset: donut ? 8 : 0,
          maxBarThickness: 34
        }]
      },
      plugins: [centerText],
      options: {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: donut ? undefined : 'y',
        cutout: donut ? '72%' : undefined,
        animation: anim,
        layout: { padding: donut ? 8 : 0 },
        plugins: {
          legend: { display: false },
          centerText: donut ? { value: pct.format(vals[0]) + ' %', label: 'de réussite', size: size, color: p.text, sub: p.text3 } : false,
          tooltip: {
            backgroundColor: p.text, titleColor: p.surface, bodyColor: p.surface,
            padding: 12, cornerRadius: 10, displayColors: true, boxPadding: 4,
            titleFont: { weight: '700' }, bodyFont: { weight: '600' },
            callbacks: {
              label: function (c) { return ' ' + pct.format(donut ? c.parsed : c.parsed.x) + ' %'; }
            }
          }
        },
        scales: donut ? {} : {
          x: { min: 0, max: 100, grid: { color: p.line }, border: { display: false },
               ticks: { color: p.text3, callback: function (v) { return v + ' %'; }, font: { weight: '600' } } },
          y: { grid: { display: false }, border: { display: false }, ticks: { color: p.text2, font: { weight: '600', size: 12 } } }
        }
      }
    };

    var chart = new window.Chart(canvas, cfg);
    live.set(canvas, chart);
    buildLegend(canvas, chart, vals, p.colors);
  }

  function initAll() {
    Array.prototype.forEach.call(doc.querySelectorAll('canvas[data-chart="repartition"]'), function (c) { build(c, true); });
  }

  // Bascule anneau / barres
  doc.addEventListener('click', function (e) {
    var b = e.target.closest('[data-chart-view]');
    if (!b) return;
    var canvas = doc.getElementById(b.dataset.target);
    if (!canvas) return;
    canvas.dataset.view = b.dataset.chartView;
    Array.prototype.forEach.call(b.parentElement.querySelectorAll('[data-chart-view]'), function (x) {
      var on = x === b; x.classList.toggle('active', on); x.setAttribute('aria-pressed', on ? 'true' : 'false');
    });
    build(canvas, true);
  });

  // Thème : on reconstruit avec les nouvelles couleurs (sans rejouer l'animation)
  LD.onTheme(function () { live.forEach(function (_, c) { build(c, false); }); });

  // Onglets Bootstrap : un graphique masqué à la création doit être redimensionné à l'affichage
  doc.addEventListener('shown.bs.tab', function () { live.forEach(function (ch) { ch.resize(); }); });

  if (doc.readyState === 'loading') doc.addEventListener('DOMContentLoaded', initAll); else initAll();
})();
