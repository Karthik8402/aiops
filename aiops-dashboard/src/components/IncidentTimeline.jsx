import { SeverityBadge, StatusBadge } from "./SeverityBadge";

function timeAgo(iso) {
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60_000);
  if (m < 1)  return "just now";
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  return `${Math.floor(h / 24)}d ago`;
}

const dotColor = (sev) => ({
  CRITICAL: { ring: "border-[#f85149] bg-[#f85149]/20", dot: "bg-[#f85149]" },
  HIGH:     { ring: "border-[#ff7b72] bg-[#ff7b72]/20", dot: "bg-[#ff7b72]" },
  MEDIUM:   { ring: "border-[#d29922] bg-[#d29922]/20", dot: "bg-[#d29922]" },
  LOW:      { ring: "border-[#3fb950] bg-[#3fb950]/20", dot: "bg-[#3fb950]" },
}[sev] ?? { ring: "border-[#3fb950] bg-[#3fb950]/20", dot: "bg-[#3fb950]" });

export function IncidentTimeline({ incidents, onAcknowledge, onResolve }) {
  if (!incidents.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16
                      text-[var(--color-text-muted)]">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" strokeWidth="1.5" className="mb-3 opacity-40">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
          <polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
        <p className="text-[var(--text-sm)]">No open incidents</p>
        <p className="text-[var(--text-xs)] mt-1">System is healthy</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col">
      {incidents.map((inc, idx) => {
        const c = dotColor(inc.severity);
        return (
          <div key={inc.id}
               className="relative flex gap-4 pb-6 animate-fade-in"
               style={{ animationDelay: `${idx * 40}ms` }}>

            {/* Spine line connecting dots */}
            {idx < incidents.length - 1 && (
              <div className="absolute left-[11px] top-6 bottom-0 w-px
                              bg-[var(--color-border)]" />
            )}

            {/* Severity dot */}
            <div className={`relative z-10 w-6 h-6 rounded-full border-2 flex-shrink-0
                             mt-0.5 flex items-center justify-center ${c.ring}`}>
              {inc.status === "OPEN" && (
                <span className={`w-2 h-2 rounded-full animate-pulse-dot ${c.dot}`} />
              )}
            </div>

            {/* Card */}
            <div className="flex-1 min-w-0 rounded-lg border border-[var(--color-border)]
                            bg-[var(--color-surface)] p-3">
              <div className="flex flex-wrap items-start justify-between gap-2 mb-1">
                <div className="flex flex-wrap items-center gap-2">
                  <SeverityBadge severity={inc.severity} />
                  <StatusBadge status={inc.status} />
                  <span className="text-[var(--text-xs)] text-[var(--color-text-muted)] font-mono">
                    #{inc.id}
                  </span>
                </div>
                <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">
                  {timeAgo(inc.openedAt)}
                </span>
              </div>

              <p className="text-[var(--text-sm)] font-medium text-[var(--color-text)]
                            mb-1 truncate">
                {inc.title}
              </p>
              <p className="text-[var(--text-xs)] text-[var(--color-text-muted)] mb-3">
                Service ID: {inc.serviceId}
              </p>

              {inc.status === "OPEN" && (
                <div className="flex gap-2">
                  <button onClick={() => onAcknowledge(inc.id)}
                          className="px-3 py-1 text-[var(--text-xs)] font-medium rounded
                                     border border-[var(--color-warning)]
                                     text-[var(--color-warning)]
                                     hover:bg-[var(--color-warning)]/10 transition-colors cursor-pointer">
                    Acknowledge
                  </button>
                  <button onClick={() => onResolve(inc.id)}
                          className="px-3 py-1 text-[var(--text-xs)] font-medium rounded
                                     border border-[var(--color-success)]
                                     text-[var(--color-success)]
                                     hover:bg-[var(--color-success)]/10 transition-colors cursor-pointer">
                    Resolve
                  </button>
                </div>
              )}

              {inc.status === "ACKNOWLEDGED" && (
                <div className="flex gap-2">
                  <button onClick={() => onResolve(inc.id)}
                          className="px-3 py-1 text-[var(--text-xs)] font-medium rounded
                                     border border-[var(--color-success)]
                                     text-[var(--color-success)]
                                     hover:bg-[var(--color-success)]/10 transition-colors cursor-pointer">
                    Resolve
                  </button>
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
