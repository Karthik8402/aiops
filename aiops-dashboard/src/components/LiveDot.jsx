export function LiveDot({ status }) {
  if (status === "live") return (
    <span className="inline-flex items-center gap-1.5 text-[var(--text-xs)] text-[var(--color-success)]">
      <span className="w-2 h-2 rounded-full bg-[var(--color-success)] animate-pulse-dot" />
      LIVE
    </span>
  );
  if (status === "polling") return (
    <span className="inline-flex items-center gap-1.5 text-[var(--text-xs)] text-[var(--color-warning)]">
      <span className="w-2 h-2 rounded-full bg-[var(--color-warning)]" />
      POLLING
    </span>
  );
  return (
    <span className="inline-flex items-center gap-1.5 text-[var(--text-xs)] text-[var(--color-text-muted)]">
      <span className="w-2 h-2 rounded-full bg-[var(--color-text-muted)] animate-pulse-dot" />
      CONNECTING
    </span>
  );
}
