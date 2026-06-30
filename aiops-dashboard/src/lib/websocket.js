import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_URL = import.meta.env.VITE_WS_BASE ?? "http://localhost:8080/ws";

let client = null;

export function connectWS({ onIncident, onAlert, onConnect, onDisconnect }) {
  const token = sessionStorage.getItem("aiops_token");

  client = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe("/topic/incidents", (msg) => {
        try { onIncident?.(JSON.parse(msg.body)); } catch {}
      });
      client.subscribe("/topic/alerts", (msg) => {
        try { onAlert?.(JSON.parse(msg.body)); } catch {}
      });
      onConnect?.();
    },
    onDisconnect: () => onDisconnect?.(),
    onStompError:  (frame) => console.error("STOMP error", frame),
  });

  client.activate();
  return () => client?.deactivate();
}
