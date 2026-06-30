const SEV = {
  CRITICAL: "bg-[#f85149]/15 text-[#f85149] border border-[#f85149]/30",
  HIGH:     "bg-[#ff7b72]/15 text-[#ff7b72] border border-[#ff7b72]/30",
  MEDIUM:   "bg-[#d29922]/15 text-[#d29922] border border-[#d29922]/30",
  LOW:      "bg-[#3fb950]/15 text-[#3fb950] border border-[#3fb950]/30",
};

const STAT = {
  OPEN:         "bg-[#f85149]/15 text-[#f85149] border border-[#f85149]/30",
  ACKNOWLEDGED: "bg-[#d29922]/15 text-[#d29922] border border-[#d29922]/30",
  RESOLVED:     "bg-[#3fb950]/15 text-[#3fb950] border border-[#3fb950]/30",
};

export function SeverityBadge({ severity }) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full
                      text-[11px] font-medium ${SEV[severity] ?? SEV.LOW}`}>
      {severity}
    </span>
  );
}

export function StatusBadge({ status }) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full
                      text-[11px] font-medium ${STAT[status] ?? ""}`}>
      {status}
    </span>
  );
}
