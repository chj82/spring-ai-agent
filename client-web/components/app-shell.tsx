"use client";

import {
  appLogout,
  fetchAppProfile,
  type LoginUser,
} from "@/lib/app-api";
import { getErrorMessage } from "@/lib/format";
import { usePathname, useRouter } from "next/navigation";
import {
  createContext,
  useContext,
  useEffect,
  useState,
  useTransition,
} from "react";

const AppShellContext = createContext<{
  profile: LoginUser;
  logout: () => void;
  loggingOut: boolean;
} | null>(null);

export function useAppShell() {
  const context = useContext(AppShellContext);
  if (!context) {
    throw new Error("useAppShell must be used within AppShell");
  }
  return context;
}

export function AppShell({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const pathname = usePathname();
  const router = useRouter();
  const [profile, setProfile] = useState<LoginUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [loggingOut, startLogoutTransition] = useTransition();

  useEffect(() => {
    let cancelled = false;

    async function loadProfile() {
      setError("");
      try {
        const currentProfile = await fetchAppProfile();
        if (!cancelled) {
          setProfile(currentProfile);
        }
      } catch {
        if (!cancelled) {
          const redirect =
            pathname && pathname !== "/" ? `?redirect=${encodeURIComponent(pathname)}` : "";
          router.replace(`/login${redirect}`);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadProfile();
    return () => {
      cancelled = true;
    };
  }, [pathname, router]);

  const logout = () => {
    startLogoutTransition(async () => {
      try {
        await appLogout();
      } catch (requestError) {
        setError(getErrorMessage(requestError, "退出登录失败"));
      } finally {
        router.replace("/login");
      }
    });
  };

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center px-6 py-10">
        <div className="w-full max-w-md rounded-[28px] border border-white/70 bg-white/90 px-6 py-8 text-center shadow-[0_18px_60px_rgba(15,23,42,0.08)]">
          <p className="text-sm text-slate-500">正在验证登录状态...</p>
        </div>
      </main>
    );
  }

  if (!profile) {
    return null;
  }

  return (
    <AppShellContext.Provider value={{ profile, logout, loggingOut }}>
      <main className="min-h-screen px-3 py-3 text-slate-900 md:px-4 md:py-4">
        {error ? (
          <div className="mx-auto mb-3 max-w-7xl rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
            {error}
          </div>
        ) : null}
        {children}
      </main>
    </AppShellContext.Provider>
  );
}
