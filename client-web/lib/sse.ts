import { API_BASE_URL, ApiError, ApiResponse } from "@/lib/api";

export type SseEvent<T = unknown> = {
  event: string;
  data: T;
};

type FetchSseOptions = {
  path: string;
  method?: "POST";
  body?: string;
  signal?: AbortSignal;
  onEvent: (event: SseEvent) => void;
};

function parseEventData(raw: string) {
  try {
    return JSON.parse(raw) as unknown;
  } catch {
    return raw;
  }
}

function emitEvent(block: string, onEvent: (event: SseEvent) => void) {
  const lines = block.split("\n");
  let eventName = "message";
  const dataLines: string[] = [];

  for (const line of lines) {
    if (!line || line.startsWith(":")) {
      continue;
    }
    if (line.startsWith("event:")) {
      eventName = line.slice(6).trim() || "message";
      continue;
    }
    if (line.startsWith("data:")) {
      dataLines.push(line.slice(5).trim());
    }
  }

  if (dataLines.length === 0) {
    return;
  }

  onEvent({
    event: eventName,
    data: parseEventData(dataLines.join("\n")),
  });
}

export async function fetchSse({
  path,
  method = "POST",
  body,
  signal,
  onEvent,
}: FetchSseOptions) {
  const headers = new Headers({
    Accept: "text/event-stream",
  });
  if (body) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    body,
    signal,
    credentials: "include",
    headers,
  });

  if (!response.ok) {
    let payload: ApiResponse<unknown> | null = null;
    try {
      payload = (await response.json()) as ApiResponse<unknown>;
    } catch {
      payload = null;
    }
    throw new ApiError(
      payload?.message ?? `请求失败: ${response.status}`,
      payload?.code ?? response.status,
      response.status,
    );
  }

  if (!response.body) {
    throw new Error("流式响应为空");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    const parts = buffer.split(/\n\n|\r\n\r\n/);
    buffer = parts.pop() ?? "";

    for (const part of parts) {
      emitEvent(part.replace(/\r\n/g, "\n"), onEvent);
    }
  }

  const finalText = buffer + decoder.decode();
  if (finalText.trim()) {
    emitEvent(finalText.replace(/\r\n/g, "\n"), onEvent);
  }
}
