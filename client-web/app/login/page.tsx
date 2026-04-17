"use client";

import { appLogin, fetchAppProfile } from "@/lib/app-api";
import { getErrorMessage } from "@/lib/format";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState, useTransition } from "react";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [checking, setChecking] = useState(true);
  const [redirectPath, setRedirectPath] = useState("/chat");
  const [isPending, startTransition] = useTransition();

  useEffect(() => {
    let cancelled = false;

    async function checkLogin() {
      const redirect =
        typeof window !== "undefined"
          ? new URLSearchParams(window.location.search).get("redirect")
          : null;
      const targetPath = redirect || "/chat";

      if (!cancelled) {
        setRedirectPath(targetPath);
      }

      try {
        await fetchAppProfile();
        if (!cancelled) {
          router.replace(targetPath);
        }
      } catch {
        if (!cancelled) {
          setChecking(false);
        }
      }
    }

    void checkLogin();
    return () => {
      cancelled = true;
    };
  }, [router]);

  const handleLogin = () => {
    setError("");
    startTransition(async () => {
      try {
        await appLogin({
          username: username.trim(),
          password,
        });
        router.replace(redirectPath);
      } catch (requestError) {
        setError(getErrorMessage(requestError, "登录失败"));
      }
    });
  };

  if (checking) {
    return (
      <main className="flex min-h-screen items-center justify-center px-6 py-10">
        <div className="w-full max-w-md rounded-[32px] border border-white/80 bg-white/90 px-6 py-8 text-center shadow-[0_20px_70px_rgba(148,109,32,0.12)]">
          <p className="text-sm text-slate-500">正在检查登录状态...</p>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen px-6 py-10">
      <div className="mx-auto grid min-h-[calc(100vh-5rem)] w-full max-w-6xl items-center gap-8 lg:grid-cols-[1.08fr_0.92fr]">
        <section className="space-y-6">
          <p className="text-sm font-semibold uppercase tracking-[0.28em] text-amber-700">
            AI Workspace
          </p>
          <h1 className="text-5xl font-semibold tracking-tight text-slate-950">
            登录后开始你的 AI 会话
          </h1>
          <p className="max-w-2xl text-lg leading-8 text-slate-600">
            用户端聚焦一件事：让每个用户都能管理自己的会话，并和 AI 助手持续进行多轮对话。
          </p>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-[28px] border border-white/80 bg-white/70 p-5 shadow-[0_16px_48px_rgba(15,23,42,0.06)]">
              <p className="text-sm font-semibold text-slate-900">会话管理</p>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                支持新建、切换、重命名、删除，历史记录按用户隔离保存。
              </p>
            </div>
            <div className="rounded-[28px] border border-white/80 bg-white/70 p-5 shadow-[0_16px_48px_rgba(15,23,42,0.06)]">
              <p className="text-sm font-semibold text-slate-900">流式回复</p>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                支持实时展示 AI 回复内容，适合问答、总结、写作和研发协作。
              </p>
            </div>
          </div>
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
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-amber-400 focus:bg-white"
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
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    handleLogin();
                  }
                }}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-amber-400 focus:bg-white"
              />
            </div>
            <button
              type="button"
              onClick={handleLogin}
              disabled={isPending}
              className="w-full rounded-2xl bg-slate-950 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isPending ? "登录中..." : "登录用户端"}
            </button>
            {error ? (
              <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                {error}
              </div>
            ) : null}
            <Link
              href="/chat"
              className="block text-center text-sm font-medium text-slate-500 underline-offset-4 hover:text-slate-800 hover:underline"
            >
              直接查看聊天工作台
            </Link>
          </div>
        </section>
      </div>
    </main>
  );
}
