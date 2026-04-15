"use client";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { adminLogin, fetchAdminProfile } from "@/lib/admin-api";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState, useTransition } from "react";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("123456");
  const [error, setError] = useState("");
  const [isPending, startTransition] = useTransition();
  const [checking, setChecking] = useState(true);
  const [redirectPath, setRedirectPath] = useState("/users");

  useEffect(() => {
    let cancelled = false;

    async function checkLogin() {
      const redirect =
        typeof window !== "undefined"
          ? new URLSearchParams(window.location.search).get("redirect")
          : null;
      const targetPath = redirect || "/users";

      if (!cancelled) {
        setRedirectPath(targetPath);
      }

      try {
        await fetchAdminProfile();
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
        await adminLogin({ username, password });
        router.push(redirectPath);
      } catch (requestError) {
        const message =
          requestError instanceof Error ? requestError.message : "登录失败";
        setError(message);
      }
    });
  };

  if (checking) {
    return (
      <main className="flex min-h-screen items-center justify-center px-6 py-10">
        <Card className="w-full max-w-md bg-white/92">
          <CardContent className="py-10 text-center text-sm text-muted-foreground">
            正在检查登录状态...
          </CardContent>
        </Card>
      </main>
    );
  }

  return (
    <main className="min-h-screen px-6 py-10">
      <div className="mx-auto grid min-h-[calc(100vh-5rem)] max-w-6xl items-center gap-8 lg:grid-cols-[1fr_430px]">
        <section>
          <p className="text-sm font-semibold uppercase tracking-[0.3em] text-cyan-700">
            Admin Console
          </p>
          <h1 className="mt-4 text-5xl font-semibold tracking-tight text-slate-950">
            管理后台登录
          </h1>
          <p className="mt-5 max-w-2xl text-lg leading-8 text-slate-600">
            这一期后台只聚焦后台用户和前端客户管理，后续再继续扩展助手、技能、工具和知识库模块。
          </p>
          <Card className="mt-6 max-w-xl bg-white/85">
            <CardHeader className="pb-3">
              <CardTitle className="text-base">默认初始化账号</CardTitle>
              <CardDescription>首次启动时会自动创建超级管理员。</CardDescription>
            </CardHeader>
            <CardContent className="space-y-1 text-sm text-slate-600">
              <p>默认账号：admin</p>
              <p>默认密码：123456</p>
            </CardContent>
          </Card>
        </section>

        <Card className="rounded-[32px] border-white/70 bg-white/92 shadow-[0_20px_70px_rgba(35,85,106,0.12)]">
          <CardHeader>
            <CardTitle>登录后台</CardTitle>
            <CardDescription>登录后可管理后台账号和前端客户。</CardDescription>
          </CardHeader>
          <CardContent className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="username">管理账号</Label>
              <Input
                id="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">密码</Label>
              <Input
                id="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                type="password"
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    handleLogin();
                  }
                }}
              />
            </div>
            <Button
              onClick={handleLogin}
              disabled={isPending}
              className="w-full"
              size="lg"
            >
              {isPending ? "登录中..." : "登录后台"}
            </Button>
            {error ? (
              <div className="rounded-lg border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {error}
              </div>
            ) : null}
            <Link
              href="/users"
              className="block text-center text-sm font-medium text-slate-500 underline-offset-4 hover:text-slate-800 hover:underline"
            >
              先看后台工作台骨架
            </Link>
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
