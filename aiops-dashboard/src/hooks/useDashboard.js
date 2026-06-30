import { useState, useEffect, useCallback } from "react";
import { api } from "../lib/api";
import { connectWS } from "../lib/websocket";

export function useDashboard() {
  const [summary,      setSummary]    = useState(null);
  const [incidents,    setIncidents]  = useState([]);
  const [alerts,       setAlerts]     = useState([]);
  const [sparklines,   setSparklines] = useState([]);
  const [wsStatus,     setWsStatus]   = useState("connecting"); // connecting | live | polling | disconnected
  const [loading,      setLoading]    = useState(true);

  const fetchAll = useCallback(async () => {
    try {
      const [sum, inc, ale] = await Promise.all([
        api.dashboard(),
        api.incidents("?status=OPEN&size=50"),
        api.alerts("?acknowledged=false&size=20"),
      ]);
      setSummary(sum);
      setIncidents(inc.content ?? []);
      setAlerts(ale.content ?? []);

      // Fetch actual database anomalies to plot on sparklines
      const services = [
        { id: 1, name: "payment-service", base: 0.25 },
        { id: 2, name: "auth-service", base: 0.20 },
        { id: 3, name: "order-service", base: 0.30 },
      ];

      const results = await Promise.all(
        services.map(s => api.anomalies(`?serviceId=${s.id}&size=30`))
      );

      const sparkData = services.map((s, idx) => {
        const anomalies = results[idx].content ?? [];
        
        // Base series representing normal baseline fluctuation
        const series = Array.from({ length: 30 }, (_, i) => ({
          t: i,
          score: s.base + (Math.random() - 0.5) * 0.05,
        }));

        // Overlay actual anomalies (which exceed the threshold) at the end of the series
        const sortedAnomalies = [...anomalies].reverse();
        const count = sortedAnomalies.length;
        for (let i = 0; i < Math.min(30, count); i++) {
          const anomaly = sortedAnomalies[count - 1 - i];
          series[29 - i] = {
            t: 29 - i,
            score: anomaly.anomalyScore,
          };
        }
        return { name: s.name, data: series };
      });
      setSparklines(sparkData);

    } catch (e) {
      console.error("Fetch error in dashboard hook:", e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const token = sessionStorage.getItem("aiops_token");
    if (!token) {
      setLoading(false);
      setWsStatus("disconnected");
      return;
    }

    fetchAll();

    const cleanup = connectWS({
      onConnect:    () => setWsStatus("live"),
      onDisconnect: () => setWsStatus("polling"),

      // Prepend new incident pushed from /topic/incidents
      onIncident: (evt) =>
        setIncidents((prev) => {
          if (prev.find((i) => i.id === evt.incidentId)) return prev;
          return [{
            id: evt.incidentId, serviceId: evt.serviceId,
            title: evt.title,   severity: evt.severity,
            status: "OPEN",     openedAt: evt.openedAt,
          }, ...prev];
        }),

      // Prepend new alert pushed from /topic/alerts
      onAlert: (evt) => {
        setAlerts((prev) => [{
          id: evt.alertId, incidentId: evt.incidentId,
          message: evt.message, acknowledged: false,
          sentAt: evt.sentAt, channel: "DASHBOARD",
        }, ...prev.slice(0, 19)]);
        setSummary((s) => s ? { ...s, openIncidents: (s.openIncidents ?? 0) + 1 } : s);
      },
    });

    // Polling fallback — catch anything WebSocket missed
    const poll = setInterval(fetchAll, 30_000);
    return () => { cleanup?.(); clearInterval(poll); };
  }, [fetchAll]);

  const ackAlert = useCallback(async (id) => {
    try {
      await api.ackAlert(id);
      setAlerts((prev) => prev.filter((a) => a.id !== id));
      setSummary((s) => s ? { ...s, openIncidents: Math.max(0, (s.openIncidents ?? 0) - 1) } : s);
    } catch (e) {
      console.error("Error acknowledging alert", e);
    }
  }, []);

  const updateIncident = useCallback(async (id, status) => {
    try {
      await api.patchIncident(id, { status });
      setIncidents((prev) => prev.map((i) => i.id === id ? { ...i, status } : i));
    } catch (e) {
      console.error("Error updating incident", e);
    }
  }, []);

  return { summary, incidents, alerts, sparklines, wsStatus, loading, ackAlert, updateIncident };
}
