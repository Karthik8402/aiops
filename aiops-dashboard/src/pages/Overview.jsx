import { KpiCard }         from "../components/KpiCard";
import { AnomalySparkline } from "../components/AnomalySparkline";
import { AlertFeed }        from "../components/AlertFeed";

export function Overview({ summary, alerts, sparklines = [], onAck }) {
  return (
    <div className="flex flex-col gap-6">
      {/* KPI cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <KpiCard label="Open Incidents"
                 value={summary?.openIncidents}
                 accent="error" sub="requires attention" />
        <KpiCard label="Critical Anomalies 1h"
                 value={summary?.criticalAnomaliesLast1h}
                 accent="warning" sub="last 60 min" />
        <KpiCard label="Services Monitored"
                 value={summary?.servicesMonitored}
                 accent="blue" sub="across all envs" />
        <KpiCard label="Kafka Lag"
                 value={summary?.kafkaConsumerLagSeconds != null
                   ? `${summary.kafkaConsumerLagSeconds.toFixed(1)}s` : "—"}
                 accent="green" sub="consumer lag" />
      </div>

      {/* Anomaly sparklines */}
      <div>
        <h2 className="text-[var(--text-xs)] text-[var(--color-text-muted)]
                       uppercase tracking-widest font-medium mb-3">
          Anomaly Scores — Live
        </h2>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-3">
          {sparklines.map((s) => (
            <AnomalySparkline key={s.name} serviceName={s.name} data={s.data} />
          ))}
        </div>
      </div>

      {/* Recent alerts */}
      <div>
        <h2 className="text-[var(--text-xs)] text-[var(--color-text-muted)]
                       uppercase tracking-widest font-medium mb-3">
          Unacknowledged Alerts
        </h2>
        <AlertFeed alerts={alerts.slice(0, 5)} onAck={onAck} />
      </div>
    </div>
  );
}
