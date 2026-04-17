"use client";

import { useEffect, useRef } from "react";

export function MessageComposer({
  value,
  sending,
  disabled,
  onChange,
  onSend,
  autoFocusKey,
}: Readonly<{
  value: string;
  sending: boolean;
  disabled: boolean;
  onChange: (value: string) => void;
  onSend: () => void;
  autoFocusKey: string | number | null;
}>) {
  const textareaRef = useRef<HTMLTextAreaElement | null>(null);

  useEffect(() => {
    textareaRef.current?.focus();
  }, [autoFocusKey]);

  return (
    <div className="border-t border-amber-100/80 px-4 py-4 md:px-6 md:py-5">
      <div className="rounded-[30px] border border-amber-100 bg-[#fffaf2] p-3 shadow-[inset_0_1px_0_rgba(255,255,255,0.6)]">
        <textarea
          ref={textareaRef}
          rows={4}
          value={value}
          disabled={disabled}
          placeholder="输入你的问题，Enter 发送，Shift + Enter 换行"
          onChange={(event) => onChange(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter" && !event.shiftKey) {
              event.preventDefault();
              onSend();
            }
          }}
          className="min-h-28 w-full resize-none bg-transparent px-2 py-2 text-[15px] leading-7 text-slate-800 outline-none placeholder:text-slate-400 disabled:cursor-not-allowed disabled:opacity-60"
        />
        <div className="mt-3 flex items-center justify-between gap-3">
          <p className="text-xs text-slate-400">支持连续多轮对话，消息会自动保存。</p>
          <button
            type="button"
            disabled={disabled || sending}
            onClick={onSend}
            className="rounded-2xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {sending ? "发送中..." : "发送"}
          </button>
        </div>
      </div>
    </div>
  );
}
