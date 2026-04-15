import Link from "next/link";

export default function ClientHomePage() {
  return (
    <main className="min-h-screen px-6 py-10">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-8">
        <section className="rounded-[32px] border border-white/70 bg-white/85 p-8 shadow-[0_20px_80px_rgba(80,57,20,0.08)]">
          <p className="text-sm font-medium uppercase tracking-[0.32em] text-amber-700">
            Agent Client
          </p>
          <h1 className="mt-4 text-5xl font-semibold tracking-tight text-slate-950">
            面向用户的 AI 助手客户端
          </h1>
          <p className="mt-5 max-w-2xl text-lg leading-8 text-slate-600">
            客户端项目独立负责登录、会话列表、历史记录和聊天对话体验。
          </p>
        </section>
        <div className="grid gap-6 md:grid-cols-2">
          <Link
            href="/login"
            className="rounded-[28px] border border-slate-200/70 bg-white/80 p-6 shadow-[0_16px_48px_rgba(15,23,42,0.08)]"
          >
            <h2 className="text-2xl font-semibold">登录页</h2>
            <p className="mt-3 text-slate-600">前端客户账号密码登录入口。</p>
          </Link>
          <Link
            href="/chat"
            className="rounded-[28px] border border-slate-200/70 bg-white/80 p-6 shadow-[0_16px_48px_rgba(15,23,42,0.08)]"
          >
            <h2 className="text-2xl font-semibold">聊天页</h2>
            <p className="mt-3 text-slate-600">会话列表、消息历史和发送输入框。</p>
          </Link>
        </div>
      </div>
    </main>
  );
}
