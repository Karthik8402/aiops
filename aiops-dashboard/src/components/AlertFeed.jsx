function timeAgo(iso) {
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60_000);
  if (m < 1)  return "just now";
  if (m < 60) return `${m}m ago`;
  return `${Math.floor(m / 60)}h ago`;
}

export function AlertFeed({ alerts, onAck }) {
  if (!alerts.length) {
    return (
      <p className="text-center py-8 text-[var(--text-sm)] text-[var(--color-text-muted)]">
        No unacknowledged alerts
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-2">
      {alerts.map((a) => (
        <div key={a.id}
             className="flex items-start gap-3 p-3 rounded-lg
                        border border-[var(--color-border)]
                        bg-[var(--color-surface)] animate-slide-in">
          <div className="w-2 h-2 rounded-full bg-[var(--color-error)]
                          animate-pulse-dot flex-shrink-0 mt-1.5" />
          <div className="flex-1 min-w-0">
            <p className="text-[var(--text-sm)] text-[var(--color-text)] leading-snug">
              {a.message}
            </p>
            <div className="flex items-center gap-3 mt-1">
              <span className="text-[var(--text-xs)] text-[var(--color-text-muted)] font-mono">
                Incident #{a.incidentId}
              </span>
              <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">
                {timeAgo(a.sentAt)}
              </span>
            </div>
          </div>
          <button onClick={() => onAck(a.id)}
                  className="flex-shrink-0 text-[var(--text-xs)] text-[var(--color-text-muted)]
                             hover:text-[var(--color-text)] transition-colors px-2 py-1 rounded
                             border border-[var(--color-border)]
                             hover:border-[var(--color-text-muted)] cursor-pointer">
            Ack
          </button>
        </div>
      ))}
    </div>
  );
}
