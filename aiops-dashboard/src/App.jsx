import { useState } from "react";
import { Sidebar } from "./components/Sidebar";
import { LiveDot } from "./components/LiveDot";
import { Overview } from "./pages/Overview";
import { Incidents } from "./pages/Incidents";
import { AlertFeed } from "./components/AlertFeed";
import { useDashboard } from "./hooks/useDashboard";
import { api } from "./lib/api";

export default function App() {
  const [token, setToken] = useState(sessionStorage.getItem("aiops_token"));
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [authError, setAuthError] = useState("");
  const [page, setPage] = useState("overview");

  const {
    summary, incidents, alerts, sparklines,
    wsStatus, loading,
    ackAlert, updateIncident,
  } = useDashboard();

  const handleLogin = async (e) => {
    e.preventDefault();
    setAuthError("");
    try {
      const data = await api.login({ username, password });
      sessionStorage.setItem("aiops_token", data.token);
      setToken(data.token);
      window.location.reload(); // Reload to initialize WebSocket with token
    } catch {
      setAuthError("Invalid username or password");
    }
  };

  const handleLogout = () => {
    sessionStorage.removeItem("aiops_token");
    setToken(null);
    window.location.reload();
  };

  if (!token) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-[var(--color-bg)]">
        <form onSubmit={handleLogin} className="w-full max-w-sm p-6 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] animate-fade-in flex flex-col gap-4">
          <div className="flex items-center gap-2 mb-4 justify-center">
            <svg width="32" height="32" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="6" fill="#161b22"/>
              <circle cx="14" cy="14" r="7" stroke="#3fb950" strokeWidth="1.5"/>
              <path d="M10 14l3 3 5-6" stroke="#3fb950" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <div className="text-left">
              <p className="text-[var(--text-base)] font-semibold text-[var(--color-text)] leading-none">AIOps Platform</p>
              <p className="text-[var(--text-xs)] text-[var(--color-text-muted)] mt-1">Sign in to console</p>
            </div>
          </div>

          {authError && (
            <div className="text-[var(--text-xs)] text-[var(--color-error)] bg-[var(--color-error)]/10 border border-[var(--color-error)]/25 rounded p-2 text-center">
              {authError}
            </div>
          )}

          <div className="flex flex-col gap-1">
            <label className="text-[var(--text-xs)] text-[var(--color-text-muted)] uppercase tracking-wider font-semibold">Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="px-3 py-2 rounded border border-[var(--color-border)] bg-[var(--color-bg)] text-[var(--color-text)] focus:outline-none focus:border-[var(--color-primary)] text-sm"
              placeholder="e.g. admin"
              required
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[var(--text-xs)] text-[var(--color-text-muted)] uppercase tracking-wider font-semibold">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="px-3 py-2 rounded border border-[var(--color-border)] bg-[var(--color-bg)] text-[var(--color-text)] focus:outline-none focus:border-[var(--color-primary)] text-sm"
              placeholder="••••••••"
              required
            />
          </div>

          <button
            type="submit"
            className="w-full mt-2 py-2 bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold rounded transition-colors text-sm cursor-pointer"
          >
            Login
          </button>
          
          <div className="mt-4 pt-4 border-t border-[var(--color-border)] text-center">
            <p className="text-[11px] text-[var(--color-text-muted)]">Demo Credentials:</p>
            <p className="text-[11px] text-[var(--color-text)] font-mono mt-1">admin / adminpass</p>
          </div>
        </form>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen">
      <Sidebar active={page} setActive={setPage}
               wsStatus={wsStatus} alertCount={alerts.length} />

      <div className="flex-1 flex flex-col min-w-0">
        {/* Sticky top bar */}
        <header className="sticky top-0 z-10 flex items-center justify-between
                           px-6 py-3 border-b border-[var(--color-border)]
                           bg-[var(--color-bg)]/80 backdrop-blur-sm">
          <h1 className="text-[var(--text-sm)] font-semibold text-[var(--color-text)] capitalize">
            {page}
          </h1>
          <div className="flex items-center gap-4">
            <LiveDot status={wsStatus} />
            {loading && (
              <span className="text-[var(--text-xs)] text-[var(--color-text-muted)] animate-pulse">
                Loading…
              </span>
            )}
            <button
              onClick={handleLogout}
              className="text-[var(--text-xs)] text-[var(--color-text-muted)] hover:text-[var(--color-text)] px-2 py-1 border border-[var(--color-border)] rounded hover:border-[var(--color-text-muted)] transition-colors cursor-pointer"
            >
              Logout
            </button>
          </div>
        </header>

        {/* Page router */}
        <main className="flex-1 p-6 overflow-auto">
          {page === "overview" && (
            <Overview summary={summary} alerts={alerts} sparklines={sparklines} onAck={ackAlert} />
          )}
          {page === "incidents" && (
            <Incidents
              incidents={incidents}
              onAcknowledge={(id) => updateIncident(id, "ACKNOWLEDGED")}
              onResolve={(id) => updateIncident(id, "RESOLVED")}
            />
          )}
          {page === "alerts" && (
            <div className="max-w-2xl">
              <div className="flex items-center justify-between mb-4">
                <h1 className="text-[var(--text-lg)] font-semibold text-[var(--color-text)]">
                  Alert Feed
                </h1>
                <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">
                  {alerts.length} unacknowledged
                </span>
              </div>
              <AlertFeed alerts={alerts} onAck={ackAlert} />
            </div>
          )}
          {page === "anomalies" && (
            <p className="text-[var(--text-sm)] text-[var(--color-text-muted)] p-8 text-center">
              Anomalies are processed in real-time inside the pipeline.
            </p>
          )}
        </main>
      </div>
    </div>
  );
}
