"use client";

import type { ConversationItem } from "@/lib/app-api";
import { formatDateTime } from "@/lib/format";

export function ConversationSidebar({
  conversations,
  activeConversationId,
  loading,
  creating,
  open,
  onClose,
  onCreate,
  onSelect,
}: Readonly<{
  conversations: ConversationItem[];
  activeConversationId: number | null;
  loading: boolean;
  creating: boolean;
  open: boolean;
  onClose: () => void;
  onCreate: () => void;
  onSelect: (conversationId: number) => void;
}>) {
  return (
    <>
      <div
        className={
          open
            ? "fixed inset-0 z-20 bg-slate-950/20 backdrop-blur-[2px] lg:hidden"
            : "hidden"
        }
        onClick={onClose}
      />
      <aside
        className={`fixed inset-y-3 left-3 z-30 flex w-[290px] flex-col rounded-[30px] border border-white/70 bg-[rgba(255,252,246,0.92)] shadow-[0_24px_80px_rgba(82,58,19,0.14)] transition-transform duration-200 lg:static lg:inset-auto lg:w-auto lg:min-w-[290px] lg:translate-x-0 ${
          open ? "translate-x-0" : "-translate-x-[calc(100%+0.75rem)]"
        }`}
      >
        <div className="border-b border-amber-100/80 px-5 py-5">
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-amber-700">
            Assistant Space
          </p>
          <h2 className="mt-2 text-2xl font-semibold text-slate-950">我的会话</h2>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            为每个任务开一个新会话，历史内容会自动保留。
          </p>
          <button
            type="button"
            onClick={onCreate}
            disabled={creating}
            className="mt-5 w-full rounded-2xl bg-slate-950 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {creating ? "创建中..." : "新建会话"}
          </button>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto px-3 py-3">
          {loading ? (
            <div className="rounded-2xl border border-dashed border-amber-200 bg-white/70 px-4 py-6 text-sm text-slate-500">
              正在加载会话列表...
            </div>
          ) : null}

          {!loading && conversations.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-amber-200 bg-white/70 px-4 py-6 text-sm leading-6 text-slate-500">
              还没有任何会话，先新建一个开始聊天吧。
            </div>
          ) : null}

          <div className="space-y-2">
            {conversations.map((conversation) => {
              const active = conversation.id === activeConversationId;

              return (
                <button
                  type="button"
                  key={conversation.id}
                  onClick={() => onSelect(conversation.id)}
                  className={`w-full rounded-2xl border px-4 py-3 text-left transition ${
                    active
                      ? "border-slate-900 bg-slate-950 text-white shadow-[0_16px_40px_rgba(15,23,42,0.22)]"
                      : "border-amber-100 bg-white/78 text-slate-700 hover:border-amber-200 hover:bg-white"
                  }`}
                >
                  <div className="truncate text-sm font-semibold">
                    {conversation.title}
                  </div>
                  <div
                    className={`mt-2 flex items-center justify-between text-xs ${
                      active ? "text-slate-300" : "text-slate-400"
                    }`}
                  >
                    <span>{conversation.messageCount ?? 0} 条消息</span>
                    <span>{formatDateTime(conversation.lastMessageAt) || "未开始"}</span>
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      </aside>
    </>
  );
}
