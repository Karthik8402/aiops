export function KpiCard({ label, value, sub, accent }) {
  const accentClass = {
    error:   "text-[var(--sev-critical)]",
    warning: "text-[var(--color-warning)]",
    green:   "text-[var(--color-success)]",
    blue:    "text-[var(--color-blue)]",
    default: "text-[var(--color-text)]",
  }[accent ?? "default"];

  return (
    <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)]
                    p-4 flex flex-col gap-1 animate-fade-in">
      <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]
                       uppercase tracking-widest font-medium">
        {label}
      </span>
      <span className={`text-[var(--text-xl)] font-semibold tabular-nums ${accentClass}`}>
        {value ?? "—"}
      </span>
      {sub && (
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">{sub}</span>
      )}
    </div>
  );
}
