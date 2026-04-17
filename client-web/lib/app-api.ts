import { request } from "@/lib/api";
import { getErrorMessage } from "@/lib/format";
import { encryptLoginPassword } from "@/lib/login-crypto";
import { fetchSse } from "@/lib/sse";

export type LoginUser = {
  userId: number;
  username: string;
  nickname: string;
  actorType: string;
  superAdmin: boolean;
};

export type ConversationItem = {
  id: number;
  title: string;
  messageCount: number;
  lastMessageAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type ConversationMessageItem = {
  id: number | string;
  role: string;
  content: string;
  tokenUsage?: number | null;
  status?: string | null;
  createdAt?: string | null;
  pending?: boolean;
  failed?: boolean;
};

type SendChatStreamHandlers = {
  signal?: AbortSignal;
  onStart?: (payload: { conversationId: number }) => void;
  onDelta?: (payload: { content: string }) => void;
  onDone?: (payload: {
    conversationId: number;
    assistantMessageId: number;
    reply: string;
  }) => void;
  onError?: (message: string) => void;
};

export function normalizeMessageRole(role: string) {
  const upper = role.toUpperCase();
  if (upper === "USER") {
    return "user";
  }
  if (upper === "ASSISTANT") {
    return "assistant";
  }
  return "system";
}

export function normalizeMessages(messages: ConversationMessageItem[]) {
  return messages.map((item) => ({
    ...item,
    role: normalizeMessageRole(item.role),
  }));
}

export async function appLogin(payload: { username: string; password: string }) {
  const encryptedPassword = await encryptLoginPassword(payload.password);
  return request<LoginUser>("/api/app/auth/login", {
    method: "POST",
    body: JSON.stringify({
      username: payload.username,
      encryptedPassword,
    }),
  });
}

export function appLogout() {
  return request<void>("/api/app/auth/logout", {
    method: "POST",
  });
}

export function fetchAppProfile() {
  return request<LoginUser>("/api/app/auth/me");
}

export function fetchConversationList() {
  return request<ConversationItem[]>("/api/app/conversations/list");
}

export function createConversation(title: string) {
  return request<ConversationItem>("/api/app/conversations/create", {
    method: "POST",
    body: JSON.stringify({ title }),
  });
}

export function fetchConversationMessages(conversationId: number) {
  return request<ConversationMessageItem[]>(
    `/api/app/conversations/messages?id=${conversationId}`,
  ).then(normalizeMessages);
}

export function updateConversationTitle(id: number, title: string) {
  return request<void>("/api/app/conversations/update-title", {
    method: "POST",
    body: JSON.stringify({ id, title }),
  });
}

export function deleteConversation(id: number) {
  return request<void>("/api/app/conversations/delete", {
    method: "POST",
    body: JSON.stringify({ id }),
  });
}

export async function sendChatMessageStream(
  payload: {
    conversationId: number;
    message: string;
  } & SendChatStreamHandlers,
) {
  try {
    await fetchSse({
      path: "/api/app/chat/send",
      body: JSON.stringify({
        conversationId: payload.conversationId,
        message: payload.message,
      }),
      signal: payload.signal,
      onEvent: (event) => {
        if (event.event === "start" && payload.onStart) {
          payload.onStart(event.data as { conversationId: number });
          return;
        }

        if (event.event === "delta" && payload.onDelta) {
          payload.onDelta(event.data as { content: string });
          return;
        }

        if (event.event === "done" && payload.onDone) {
          payload.onDone(
            event.data as {
              conversationId: number;
              assistantMessageId: number;
              reply: string;
            },
          );
          return;
        }

        if (event.event === "error" && payload.onError) {
          const data = event.data as { message?: string };
          payload.onError(data.message ?? "模型调用失败");
        }
      },
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") {
      return;
    }
    if (payload.onError) {
      payload.onError(getErrorMessage(error, "发送消息失败"));
    } else {
      throw error;
    }
  }
}
