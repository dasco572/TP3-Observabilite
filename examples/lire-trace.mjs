/**
 * Lit une trace dans Jaeger et l'imprime en arbre, avec les durées.
 *
 *     node examples/lire-trace.mjs --service locations
 *     node examples/lire-trace.mjs --trace 4bf92f3577b34da6a3ce929d0e0e4736
 *     node examples/lire-trace.mjs --service locations --toutes
 *
 * <p>Pourquoi un outil, alors que Jaeger a une interface web ?
 * Parce qu'un compte-rendu cite des <b>valeurs</b>
 * (un identifiant de trace, un nom de span, une durée en millisecondes)
 * et qu'une capture d'écran ne se recopie pas.</p>
 *
 * <p>Il n'utilise que l'API v3 de Jaeger et le fetch intégré de Node.</p>
 */
const args = process.argv.slice(2);
const option = (nom, defaut) => {
  const i = args.indexOf(nom);
  return i !== -1 && args[i + 1] ? args[i + 1] : defaut;
};

const jaeger = option('--jaeger', 'http://localhost:16686');
const service = option('--service', null);
const trace = option('--trace', null);
const toutes = args.includes('--toutes');

if (!service && !trace) {
  console.error('usage : node examples/lire-trace.mjs --service <nom> | --trace <id>');
  console.error('        --jaeger <url>   (defaut : http://localhost:16686)');
  console.error('        --toutes         (toutes les traces, pas seulement la derniere)');
  process.exit(2);
}

const isoDecale = (minutes) =>
  new Date(Date.now() + minutes * 60_000).toISOString().replace(/\.\d+Z$/, 'Z');

async function lireJson(url) {
  const r = await fetch(url, { signal: AbortSignal.timeout(15000) });
  if (!r.ok) {
    throw new Error(`Jaeger a repondu ${r.status} pour ${url}`);
  }
  return r.json();
}

/**
 * Jaeger rend le genre et le statut d'un span soit en clair, soit sous leur
 * forme numérique OTLP. On ramène les deux à un nom.
 */
const GENRES = ['', 'INTERNAL', 'SERVER', 'CLIENT', 'PRODUCER', 'CONSUMER'];
const STATUTS = ['UNSET', 'OK', 'ERROR'];

const nommer = (valeur, noms, prefixe) => {
  if (typeof valeur === 'number') return noms[valeur] ?? '';
  return String(valeur ?? '').replace(prefixe, '');
};

/** Aplatit la reponse OTLP de Jaeger en une liste de spans exploitables. */
function spansDe(corps) {
  const lots = Array.isArray(corps) ? corps : [corps];
  const spans = [];
  for (const lot of lots) {
    for (const rs of lot?.result?.resourceSpans ?? []) {
      const nomService = (rs.resource?.attributes ?? []).find(
        (a) => a.key === 'service.name',
      )?.value?.stringValue;
      for (const ss of rs.scopeSpans ?? []) {
        const tracer = ss.scope?.name ?? '?';
        for (const s of ss.spans ?? []) {
          const debut = BigInt(s.startTimeUnixNano ?? 0);
          const fin = BigInt(s.endTimeUnixNano ?? 0);
          spans.push({
            service: nomService ?? '?',
            tracer,
            trace: s.traceId,
            span: s.spanId,
            parent: s.parentSpanId || null,
            nom: s.name,
            kind: nommer(s.kind, GENRES, 'SPAN_KIND_'),
            statut: nommer(s.status?.code, STATUTS, 'STATUS_CODE_'),
            debut,
            ms: Number(fin - debut) / 1e6,
          });
        }
      }
    }
  }
  return spans;
}

/** Imprime un arbre de spans, chaque enfant sous son parent. */
function imprimerArbre(spans) {
  const parTrace = spans[0].trace;
  const racines = spans.filter((s) => !s.parent || !spans.some((p) => p.span === s.parent));
  const total = Math.max(...spans.map((s) => s.ms));

  console.log('');
  console.log(`trace ${parTrace}   ${spans.length} span(s)`);
  console.log('-'.repeat(92));

  const descendre = (span, profondeur) => {
    const part = total > 0 ? Math.round((span.ms / total) * 100) : 0;
    const barre = '#'.repeat(Math.max(1, Math.round(part / 5))).padEnd(20, '.');
    const nom = '  '.repeat(profondeur) + span.nom;
    const erreur = String(span.statut).includes('ERROR') ? '  ERREUR' : '';
    console.log(
      `${nom.padEnd(44).slice(0, 44)} ${span.ms.toFixed(1).padStart(8)} ms  ` +
      `${String(part).padStart(3)} %  ${barre}  ${span.service}${erreur}`,
    );
    spans
      .filter((s) => s.parent === span.span)
      .sort((a, b) => Number(a.debut - b.debut))
      .forEach((enfant) => descendre(enfant, profondeur + 1));
  };

  racines.sort((a, b) => Number(a.debut - b.debut)).forEach((r) => descendre(r, 0));

  const services = [...new Set(spans.map((s) => s.service))];
  const tracers = [...new Set(spans.map((s) => s.tracer))];
  console.log('-'.repeat(92));
  console.log(`services traverses : ${services.join(', ')}`);
  console.log(`tracers : ${tracers.join(' | ')}`);
}

let spans;
if (trace) {
  spans = spansDe(await lireJson(`${jaeger}/api/v3/traces/${trace}`));
} else {
  const url =
    `${jaeger}/api/v3/traces?query.service_name=${encodeURIComponent(service)}` +
    `&query.num_traces=50&query.start_time_min=${isoDecale(-120)}` +
    `&query.start_time_max=${isoDecale(5)}`;
  spans = spansDe(await lireJson(url));
}

if (!spans.length) {
  console.log('aucun span trouve. Les services sont-ils instrumentes, et le');
  console.log('Collector exporte-t-il bien vers Jaeger ?');
  process.exit(1);
}

const traces = [...new Set(spans.map((s) => s.trace))];
const ordonnees = traces
  .map((t) => spans.filter((s) => s.trace === t))
  .sort((a, b) => Number(b[0].debut - a[0].debut));

for (const lot of toutes || trace ? ordonnees : ordonnees.slice(0, 1)) {
  imprimerArbre(lot);
}

if (!toutes && !trace && ordonnees.length > 1) {
  console.log('');
  console.log(`(${ordonnees.length - 1} autre(s) trace(s) : ajoutez --toutes)`);
}