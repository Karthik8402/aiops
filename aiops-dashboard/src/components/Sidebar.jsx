import { LiveDot } from "./LiveDot";

const NAV = [
  { id: "overview",  label: "Overview",
    path: "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" },
  { id: "incidents", label: "Incidents",
    path: "M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" },
  { id: "anomalies", label: "Anomalies",
    path: "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" },
  { id: "alerts",    label: "Alerts",
    path: "M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" },
];

export function Sidebar({ active, setActive, wsStatus, alertCount }) {
  return (
    <aside className="hidden md:flex flex-col w-56 border-r border-[var(--color-border)]
                      bg-[var(--color-surface)] min-h-screen p-4 gap-1 flex-shrink-0">
      {/* Logo mark */}
      <div className="flex items-center gap-2 px-2 pb-6 pt-2">
        <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
          <rect width="28" height="28" rx="6" fill="#161b22"/>
          <circle cx="14" cy="14" r="7" stroke="#3fb950" strokeWidth="1.5"/>
          <path d="M10 14l3 3 5-6" stroke="#3fb950" strokeWidth="1.5"
                strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
        <div>
          <p className="text-[var(--text-sm)] font-semibold text-[var(--color-text)] leading-none">
            AIOps
          </p>
          <p className="text-[var(--text-xs)] text-[var(--color-text-muted)] leading-none mt-0.5">
            Platform
          </p>
        </div>
      </div>

      {NAV.map((item) => (
        <button key={item.id} onClick={() => setActive(item.id)}
                className={`flex items-center gap-3 px-3 py-2 rounded-md
                            text-[var(--text-sm)] font-medium transition-colors text-left cursor-pointer
                            ${active === item.id
                              ? "bg-[var(--color-primary)]/10 text-[var(--color-primary)]"
                              : "text-[var(--color-text-muted)] hover:bg-[var(--color-surface-offset)] hover:text-[var(--color-text)]"}`}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" strokeWidth="2"
               strokeLinecap="round" strokeLinejoin="round">
            <path d={item.path} />
          </svg>
          {item.label}
          {item.id === "alerts" && alertCount > 0 && (
            <span className="ml-auto bg-[var(--color-error)] text-white text-[10px]
                             font-bold rounded-full px-1.5 py-0.5
                             min-w-[18px] text-center">
              {alertCount > 9 ? "9+" : alertCount}
            </span>
          )}
        </button>
      ))}

      <div className="mt-auto pt-4 border-t border-[var(--color-border)] px-2">
        <LiveDot status={wsStatus} />
      </div>
    </aside>
  );
}
