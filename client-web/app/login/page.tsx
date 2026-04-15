"use client";

import Link from "next/link";
import { useState } from "react";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  return (
    <main className="min-h-screen px-6 py-10">
      <div className="mx-auto grid min-h-[calc(100vh-5rem)] w-full max-w-6xl items-center gap-8 lg:grid-cols-[1.1fr_0.9fr]">
        <section className="space-y-6">
          <p className="text-sm font-semibold uppercase tracking-[0.28em] text-amber-700">
            Client Login
          </p>
          <h1 className="text-5xl font-semibold tracking-tight text-slate-950">
            登录后开始你的 AI 会话
          </h1>
          <p className="max-w-xl text-lg leading-8 text-slate-600">
            前端客户登录后可以创建会话、查看历史记录，并与 AI 助手进行多轮对话。
          </p>
        </section>

        <section className="rounded-[32px] border border-white/80 bg-white/90 p-8 shadow-[0_20px_70px_rgba(148,109,32,0.12)]">
          <div className="space-y-5">
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                账号
              </label>
              <input
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                placeholder="请输入前端客户账号"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-amber-400 focus:bg-white"
              />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                密码
              </label>
              <input
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                type="password"
                placeholder="请输入密码"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-amber-400 focus:bg-white"
              />
            </div>
            <button className="w-full rounded-2xl bg-slate-950 px-4 py-3 text-sm font-semibold text-white">
              登录用户端
            </button>
            <Link
              href="/chat"
              className="block text-center text-sm font-medium text-slate-500 underline-offset-4 hover:text-slate-800 hover:underline"
            >
              先看聊天页骨架
            </Link>
          </div>
        </section>
      </div>
    </main>
  );
}
