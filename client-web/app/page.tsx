"use client";

import { fetchAppProfile } from "@/lib/app-api";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function ClientHomePage() {
  const router = useRouter();

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      try {
        await fetchAppProfile();
        if (!cancelled) {
          router.replace("/chat");
        }
      } catch {
        if (!cancelled) {
          router.replace("/login");
        }
      }
    }

    void bootstrap();
    return () => {
      cancelled = true;
    };
  }, [router]);

  return (
    <main className="flex min-h-screen items-center justify-center px-6 py-10">
      <div className="w-full max-w-md rounded-[32px] border border-white/80 bg-white/90 px-6 py-8 text-center shadow-[0_20px_70px_rgba(148,109,32,0.12)]">
        <p className="text-sm text-slate-500">正在进入 AI 助手工作台...</p>
      </div>
    </main>
  );
}
