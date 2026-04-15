const conversations = [
  "本周工作总结",
  "用户管理接口联调",
  "智能助手架构讨论",
];

const messages = [
  {
    role: "user",
    content: "帮我整理一下当前项目第一期已经完成的能力。",
  },
  {
    role: "assistant",
    content:
      "第一期已经完成双账号体系、后台用户管理、原始会话持久化、Cookie 登录态以及基于官方记忆的聊天主链路骨架。",
  },
];

export default function ChatPage() {
  return (
    <main className="min-h-screen px-4 py-4 text-slate-900">
      <div className="mx-auto grid min-h-[calc(100vh-2rem)] max-w-7xl gap-4 lg:grid-cols-[320px_1fr]">
        <aside className="rounded-[30px] border border-white/70 bg-white/85 p-5 shadow-[0_18px_60px_rgba(15,23,42,0.08)] backdrop-blur">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.25em] text-slate-500">
                会话
              </p>
              <h2 className="mt-2 text-2xl font-semibold">聊天工作台</h2>
            </div>
            <button className="rounded-full bg-slate-950 px-4 py-2 text-sm font-semibold text-white">
              新建
            </button>
          </div>

          <div className="mt-6 space-y-3">
            {conversations.map((title, index) => (
              <div
                key={title}
                className={`rounded-2xl border px-4 py-3 ${
                  index === 0
                    ? "border-slate-900 bg-slate-950 text-white"
                    : "border-slate-200 bg-slate-50 text-slate-700"
                }`}
              >
                {title}
              </div>
            ))}
          </div>
        </aside>

        <section className="flex flex-col rounded-[30px] border border-white/70 bg-white/88 shadow-[0_18px_60px_rgba(15,23,42,0.08)] backdrop-blur">
          <header className="border-b border-slate-100 px-6 py-5">
            <p className="text-xs font-semibold uppercase tracking-[0.25em] text-slate-500">
              当前会话
            </p>
            <h1 className="mt-2 text-2xl font-semibold">本周工作总结</h1>
          </header>

          <div className="flex-1 space-y-6 overflow-auto px-6 py-6">
            {messages.map((message) => (
              <div
                key={`${message.role}-${message.content}`}
                className={`max-w-3xl rounded-3xl px-5 py-4 leading-7 ${
                  message.role === "user"
                    ? "ml-auto bg-amber-100 text-slate-900"
                    : "bg-slate-100 text-slate-700"
                }`}
              >
                {message.content}
              </div>
            ))}
          </div>

          <footer className="border-t border-slate-100 px-6 py-5">
            <div className="flex items-end gap-3">
              <textarea
                rows={3}
                placeholder="输入消息，后续这里会接后端聊天接口..."
                className="min-h-24 flex-1 resize-none rounded-3xl border border-slate-200 bg-slate-50 px-5 py-4 outline-none transition focus:border-cyan-400 focus:bg-white"
              />
              <button className="rounded-3xl bg-slate-950 px-5 py-4 text-sm font-semibold text-white">
                发送
              </button>
            </div>
          </footer>
        </section>
      </div>
    </main>
  );
}
