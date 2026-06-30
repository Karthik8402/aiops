import { IncidentTimeline } from "../components/IncidentTimeline";

export function Incidents({ incidents, onAcknowledge, onResolve }) {
  const open  = incidents.filter((i) => i.status === "OPEN");
  const acked = incidents.filter((i) => i.status === "ACKNOWLEDGED");

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-[var(--text-lg)] font-semibold text-[var(--color-text)]">
          Incident Timeline
        </h1>
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">
          {open.length} open · {acked.length} acknowledged
        </span>
      </div>

      {open.length > 0 && (
        <section>
          <h2 className="text-[var(--text-xs)] text-[var(--color-error)]
                         uppercase tracking-widest font-medium mb-4">
            Active Incidents
          </h2>
          <IncidentTimeline incidents={open}
                            onAcknowledge={onAcknowledge}
                            onResolve={onResolve} />
        </section>
      )}

      {acked.length > 0 && (
        <section>
          <h2 className="text-[var(--text-xs)] text-[var(--color-warning)]
                         uppercase tracking-widest font-medium mb-4">
            Acknowledged
          </h2>
          <IncidentTimeline incidents={acked}
                            onAcknowledge={onAcknowledge}
                            onResolve={onResolve} />
        </section>
      )}

      {!open.length && !acked.length && (
        <div className="flex flex-col items-center justify-center py-24
                        text-[var(--color-text-muted)]">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" strokeWidth="1" className="mb-4 opacity-40">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <p className="text-[var(--text-base)] font-medium">All clear</p>
          <p className="text-[var(--text-sm)] mt-1">No active incidents detected</p>
        </div>
      )}
    </div>
  );
}
