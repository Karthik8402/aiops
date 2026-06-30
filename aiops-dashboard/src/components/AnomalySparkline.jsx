import {
  AreaChart, Area, XAxis, YAxis, Tooltip,
  ResponsiveContainer, ReferenceLine,
} from "recharts";

const CustomTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded border border-[var(--color-border)]
                    bg-[var(--color-surface-2)] px-2 py-1">
      <p className="text-[var(--text-xs)] text-[var(--color-text)]">
        Score: <strong>{payload[0].value.toFixed(3)}</strong>
      </p>
    </div>
  );
};

export function AnomalySparkline({ data, threshold = 0.6, serviceName }) {
  return (
    <div className="rounded-lg border border-[var(--color-border)]
                    bg-[var(--color-surface)] p-4">
      <div className="flex items-center justify-between mb-3">
        <span className="text-[var(--text-sm)] font-medium text-[var(--color-text)]">
          {serviceName}
        </span>
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">
          anomaly score · 30 windows
        </span>
      </div>
      <ResponsiveContainer width="100%" height={80}>
        <AreaChart data={data} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
          <defs>
            <linearGradient id={`grad-${serviceName}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%"  stopColor="#58a6ff" stopOpacity={0.3} />
              <stop offset="95%" stopColor="#58a6ff" stopOpacity={0.0} />
            </linearGradient>
          </defs>
          <XAxis dataKey="t" hide />
          <YAxis domain={[0, 1]} hide />
          <Tooltip content={<CustomTooltip />} />
          <ReferenceLine y={threshold} stroke="#d29922"
                         strokeDasharray="3 3" strokeWidth={1} />
          <Area type="monotone" dataKey="score"
                stroke="#58a6ff" strokeWidth={1.5}
                fill={`url(#grad-${serviceName})`}
                dot={false} activeDot={{ r: 3, fill: "#58a6ff" }} />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
