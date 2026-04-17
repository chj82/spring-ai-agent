"use client";

import { AppShell, useAppShell } from "@/components/app-shell";
import { ConversationSidebar } from "@/components/conversation-sidebar";
import { MessageComposer } from "@/components/message-composer";
import { MessageList } from "@/components/message-list";
import {
  createConversation,
  deleteConversation,
  fetchConversationList,
  fetchConversationMessages,
  sendChatMessageStream,
  updateConversationTitle,
  type ConversationItem,
  type ConversationMessageItem,
} from "@/lib/app-api";
import {
  createConversationTitle,
  getErrorMessage,
} from "@/lib/format";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

function ChatWorkspace() {
  const { profile, logout, loggingOut } = useAppShell();
  const [conversations, setConversations] = useState<ConversationItem[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ConversationMessageItem[]>([]);
  const [input, setInput] = useState("");
  const [loadingConversations, setLoadingConversations] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [creatingConversation, setCreatingConversation] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const [messagesError, setMessagesError] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const initializedRef = useRef(false);
  const activeConversationIdRef = useRef<number | null>(null);
  const streamAbortRef = useRef<AbortController | null>(null);

  const activeConversation = useMemo(
    () =>
      conversations.find((conversation) => conversation.id === activeConversationId) ?? null,
    [activeConversationId, conversations],
  );

  useEffect(() => {
    activeConversationIdRef.current = activeConversationId;
  }, [activeConversationId]);

  const loadConversations = useCallback(
    async (options?: { preserveActiveId?: number | null; silent?: boolean }) => {
      setError("");
      if (!options?.silent) {
        setLoadingConversations(true);
      }

      try {
        const list = await fetchConversationList();
        setConversations(list);

        const preferredId =
          options?.preserveActiveId ?? activeConversationIdRef.current;
        if (!initializedRef.current) {
          initializedRef.current = true;
          setActiveConversationId(list[0]?.id ?? null);
          return;
        }

        if (preferredId && list.some((conversation) => conversation.id === preferredId)) {
          setActiveConversationId(preferredId);
          return;
        }

        setActiveConversationId(list[0]?.id ?? null);
      } catch (requestError) {
        setError(getErrorMessage(requestError, "加载会话列表失败"));
      } finally {
        if (!options?.silent) {
          setLoadingConversations(false);
        }
      }
    },
    [],
  );

  useEffect(() => {
    void loadConversations();
  }, [loadConversations]);

  useEffect(() => {
    let cancelled = false;

    async function loadMessages() {
      if (!activeConversationId) {
        setMessages([]);
        setMessagesError("");
        return;
      }

      setLoadingMessages(true);
      setMessagesError("");
      try {
        const list = await fetchConversationMessages(activeConversationId);
        if (!cancelled) {
          setMessages(list);
        }
      } catch (requestError) {
        if (!cancelled) {
          setMessages([]);
          setMessagesError(getErrorMessage(requestError, "加载消息记录失败"));
        }
      } finally {
        if (!cancelled) {
          setLoadingMessages(false);
        }
      }
    }

    void loadMessages();
    return () => {
      cancelled = true;
    };
  }, [activeConversationId]);

  useEffect(() => {
    return () => {
      streamAbortRef.current?.abort();
    };
  }, []);

  const handleCreateConversation = async () => {
    setCreatingConversation(true);
    setError("");
    try {
      const conversation = await createConversation(createConversationTitle());
      setConversations((current) => [conversation, ...current.filter((item) => item.id !== conversation.id)]);
      setActiveConversationId(conversation.id);
      setMessages([]);
      setSidebarOpen(false);
    } catch (requestError) {
      setError(getErrorMessage(requestError, "创建会话失败"));
    } finally {
      setCreatingConversation(false);
    }
  };

  const handleSelectConversation = (conversationId: number) => {
    if (sending) {
      setError("AI 正在回复当前会话，请稍后再切换。");
      return;
    }
    setError("");
    setActiveConversationId(conversationId);
    setSidebarOpen(false);
  };

  const handleRenameConversation = async () => {
    if (!activeConversation) {
      return;
    }

    const nextTitle = window.prompt("请输入新的会话标题", activeConversation.title)?.trim();
    if (!nextTitle || nextTitle === activeConversation.title) {
      return;
    }

    try {
      await updateConversationTitle(activeConversation.id, nextTitle);
      setConversations((current) =>
        current.map((conversation) =>
          conversation.id === activeConversation.id
            ? { ...conversation, title: nextTitle }
            : conversation,
        ),
      );
    } catch (requestError) {
      setError(getErrorMessage(requestError, "重命名会话失败"));
    }
  };

  const handleDeleteConversation = async () => {
    if (!activeConversation) {
      return;
    }

    const confirmed = window.confirm(`确认删除会话“${activeConversation.title}”吗？`);
    if (!confirmed) {
      return;
    }

    try {
      await deleteConversation(activeConversation.id);
      const nextConversations = conversations.filter(
        (conversation) => conversation.id !== activeConversation.id,
      );
      setConversations(nextConversations);
      setActiveConversationId(nextConversations[0]?.id ?? null);
      setMessages([]);
    } catch (requestError) {
      setError(getErrorMessage(requestError, "删除会话失败"));
    }
  };

  const handleSend = async () => {
    const trimmed = input.trim();
    if (!trimmed) {
      return;
    }

    if (!activeConversationId) {
      setError("请先新建一个会话。");
      return;
    }

    const tempUserId = `user-${Date.now()}`;
    const tempAssistantId = `assistant-${Date.now()}`;
    const controller = new AbortController();
    streamAbortRef.current?.abort();
    streamAbortRef.current = controller;

    setSending(true);
    setError("");
    setMessagesError("");
    setInput("");
    setMessages((current) => [
      ...current,
      {
        id: tempUserId,
        role: "user",
        content: trimmed,
        createdAt: new Date().toISOString(),
      },
      {
        id: tempAssistantId,
        role: "assistant",
        content: "",
        pending: true,
        createdAt: new Date().toISOString(),
      },
    ]);

    await sendChatMessageStream({
      conversationId: activeConversationId,
      message: trimmed,
      signal: controller.signal,
      onDelta: ({ content }) => {
        setMessages((current) =>
          current.map((message) =>
            message.id === tempAssistantId
              ? {
                  ...message,
                  content: `${message.content}${content}`,
                }
              : message,
          ),
        );
      },
      onDone: ({ assistantMessageId, reply }) => {
        setMessages((current) =>
          current.map((message) =>
            message.id === tempAssistantId
              ? {
                  ...message,
                  id: assistantMessageId,
                  content: reply,
                  pending: false,
                }
              : message,
          ),
        );
        setConversations((current) =>
          current.map((conversation) =>
            conversation.id === activeConversationId
              ? {
                  ...conversation,
                  messageCount: (conversation.messageCount ?? 0) + 2,
                  lastMessageAt: new Date().toISOString(),
                }
              : conversation,
          ),
        );
        setSending(false);
        void loadConversations({
          preserveActiveId: activeConversationId,
          silent: true,
        });
      },
      onError: (message) => {
        setMessages((current) =>
          current.map((item) =>
            item.id === tempAssistantId
              ? {
                  ...item,
                  pending: false,
                  failed: true,
                  content: item.content || message,
                }
              : item,
          ),
        );
        setError(message);
        setSending(false);
      },
    });
  };

  return (
    <div className="mx-auto grid min-h-[calc(100vh-1.5rem)] max-w-7xl gap-3 lg:grid-cols-[290px_1fr]">
      <ConversationSidebar
        conversations={conversations}
        activeConversationId={activeConversationId}
        loading={loadingConversations}
        creating={creatingConversation}
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        onCreate={handleCreateConversation}
        onSelect={handleSelectConversation}
      />

      <section className="flex min-h-[calc(100vh-1.5rem)] min-w-0 flex-col rounded-[30px] border border-white/70 bg-white/88 shadow-[0_20px_70px_rgba(15,23,42,0.08)]">
        <header className="flex flex-wrap items-start justify-between gap-4 border-b border-slate-100 px-4 py-5 md:px-6">
          <div className="flex items-start gap-3">
            <button
              type="button"
              onClick={() => setSidebarOpen(true)}
              className="mt-1 rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-600 lg:hidden"
            >
              会话
            </button>
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.25em] text-slate-500">
                Current Session
              </p>
              <h1 className="mt-2 text-2xl font-semibold text-slate-950">
                {activeConversation?.title ?? "通用 AI 助手"}
              </h1>
              <p className="mt-2 text-sm text-slate-500">
                {activeConversation
                  ? `当前用户：${profile.nickname || profile.username}`
                  : "新建一个会话后，就可以开始多轮对话。"}
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={handleRenameConversation}
              disabled={!activeConversation || sending}
              className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              重命名
            </button>
            <button
              type="button"
              onClick={handleDeleteConversation}
              disabled={!activeConversation || sending}
              className="rounded-2xl border border-rose-200 px-4 py-2 text-sm font-medium text-rose-600 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              删除
            </button>
            <button
              type="button"
              onClick={logout}
              disabled={loggingOut}
              className="rounded-2xl bg-slate-950 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loggingOut ? "退出中..." : "退出登录"}
            </button>
          </div>
        </header>

        {error ? (
          <div className="mx-4 mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 md:mx-6">
            {error}
          </div>
        ) : null}

        {!activeConversation && !loadingConversations ? (
          <div className="flex flex-1 items-center justify-center px-6 py-10">
            <div className="max-w-lg rounded-[32px] border border-dashed border-amber-200 bg-white/75 px-8 py-10 text-center">
              <p className="text-sm font-semibold uppercase tracking-[0.24em] text-amber-700">
                Welcome
              </p>
              <h3 className="mt-3 text-2xl font-semibold text-slate-950">
                先创建一个会话
              </h3>
              <p className="mt-3 text-sm leading-7 text-slate-500">
                你可以把不同任务拆成不同会话，方便后续追溯上下文和历史记录。
              </p>
            </div>
          </div>
        ) : (
          <MessageList
            messages={messages}
            loading={loadingMessages}
            error={messagesError}
            empty={Boolean(activeConversation && messages.length === 0)}
          />
        )}

        <MessageComposer
          value={input}
          sending={sending}
          disabled={!activeConversationId}
          autoFocusKey={activeConversationId}
          onChange={setInput}
          onSend={() => {
            void handleSend();
          }}
        />
      </section>
    </div>
  );
}

export default function ChatPage() {
  return (
    <AppShell>
      <ChatWorkspace />
    </AppShell>
  );
}
