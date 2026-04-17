"use client";

import type { ConversationMessageItem } from "@/lib/app-api";
import { formatDateTime } from "@/lib/format";
import { useEffect, useRef } from "react";

function MessageBubble({
  message,
}: Readonly<{
  message: ConversationMessageItem;
}>) {
  const isUser = message.role === "user";

  return (
    <div className={`flex ${isUser ? "justify-end" : "justify-start"}`}>
      <div
        className={`max-w-3xl rounded-[28px] px-5 py-4 shadow-sm ${
          isUser
            ? "bg-[#f4d9a8] text-slate-950"
            : message.failed
              ? "border border-rose-200 bg-rose-50 text-rose-700"
              : "bg-white text-slate-700 ring-1 ring-slate-200/80"
        }`}
      >
        <div className="whitespace-pre-wrap break-words text-[15px] leading-7">
          {message.content || (message.pending ? "正在生成回复..." : "")}
        </div>
        <div
          className={`mt-3 text-[11px] ${
            isUser ? "text-slate-700/70" : "text-slate-400"
          }`}
        >
          {message.failed
            ? "发送失败"
            : message.pending
              ? "AI 正在回复"
              : formatDateTime(message.createdAt) || ""}
        </div>
      </div>
    </div>
  );
}

export function MessageList({
  messages,
  loading,
  error,
  empty,
}: Readonly<{
  messages: ConversationMessageItem[];
  loading: boolean;
  error: string;
  empty: boolean;
}>) {
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages]);

  if (loading) {
    return (
      <div className="flex flex-1 items-center justify-center px-6 py-10 text-sm text-slate-500">
        正在加载消息记录...
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-1 items-center justify-center px-6 py-10">
        <div className="max-w-md rounded-3xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm leading-6 text-rose-700">
          {error}
        </div>
      </div>
    );
  }

  if (empty) {
    return (
      <div className="flex flex-1 items-center justify-center px-6 py-10">
        <div className="max-w-lg rounded-[32px] border border-dashed border-amber-200 bg-white/75 px-8 py-10 text-center">
          <p className="text-sm font-semibold uppercase tracking-[0.24em] text-amber-700">
            Ready
          </p>
          <h3 className="mt-3 text-2xl font-semibold text-slate-950">
            从你的第一个问题开始
          </h3>
          <p className="mt-3 text-sm leading-7 text-slate-500">
            这个会话已经准备好了。你可以提需求、问问题、写总结，助手会基于当前会话继续多轮回答。
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-y-auto px-4 py-5 md:px-6">
      <div className="space-y-5">
        {messages.map((message) => (
          <MessageBubble key={String(message.id)} message={message} />
        ))}
        <div ref={bottomRef} />
      </div>
    </div>
  );
}
