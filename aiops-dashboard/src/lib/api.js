const BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

async function apiFetch(path, options = {}) {
  const token = sessionStorage.getItem("aiops_token");
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {}),
    },
  });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json();
}

export const api = {
  login:         (body)        => apiFetch("/api/auth/login",         { method: "POST", body: JSON.stringify(body) }),
  dashboard:     ()            => apiFetch("/api/dashboard"),
  incidents:     (params = "") => apiFetch(`/api/incidents${params}`),
  incident:      (id)          => apiFetch(`/api/incidents/${id}`),
  patchIncident: (id, body)    => apiFetch(`/api/incidents/${id}`,    { method: "PATCH", body: JSON.stringify(body) }),
  anomalies:     (params = "") => apiFetch(`/api/anomalies${params}`),
  alerts:        (params = "") => apiFetch(`/api/alerts${params}`),
  ackAlert:      (id)          => apiFetch(`/api/alerts/${id}/ack`,   { method: "PATCH" }),
};
