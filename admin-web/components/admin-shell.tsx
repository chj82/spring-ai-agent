"use client";

import { Card, CardContent } from "@/components/ui/card";
import { adminLogout, fetchAdminProfile, type LoginUser } from "@/lib/admin-api";
import {
  ChevronDown,
  ChevronRight,
  LogOut,
  SidebarClose,
  ShieldCheck,
  SquareUserRound,
  Users,
} from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useRef, useState, useTransition } from "react";

const menuGroups = [
  {
    label: "账号中心",
    icon: SquareUserRound,
    items: [
      { label: "后台用户", href: "/users", icon: ShieldCheck },
      { label: "前端客户", href: "/app-users", icon: Users },
    ],
  },
];

export function AdminShell({
  title,
  description,
  children,
}: Readonly<{
  title: string;
  description?: string;
  children: React.ReactNode;
}>) {
  const pathname = usePathname();
  const router = useRouter();
  const [profile, setProfile] = useState<LoginUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [isPending, startTransition] = useTransition();
  const [menuOpen, setMenuOpen] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [expandedGroups, setExpandedGroups] = useState<Record<string, boolean>>({
    账号中心: true,
  });
  const menuRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadProfile() {
      try {
        const currentProfile = await fetchAdminProfile();
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

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      if (!menuOpen) {
        return;
      }
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuOpen(false);
      }
    }

    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setMenuOpen(false);
      }
    }

    document.addEventListener("mousedown", handlePointerDown);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handlePointerDown);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [menuOpen]);

  const activePath = useMemo(() => pathname ?? "/", [pathname]);

  useEffect(() => {
    const nextExpanded: Record<string, boolean> = {};
    for (const group of menuGroups) {
      nextExpanded[group.label] = group.items.some((item) => item.href === activePath);
    }
    setExpandedGroups((current) => ({ ...current, ...nextExpanded }));
  }, [activePath]);

  const handleLogout = () => {
    startTransition(async () => {
      try {
        await adminLogout();
      } finally {
        router.replace("/login");
      }
    });
  };

  const toggleGroup = (label: string) => {
    setExpandedGroups((current) => ({
      ...current,
      [label]: !current[label],
    }));
  };

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center px-6 py-6">
        <Card className="w-full max-w-md bg-white/92">
          <CardContent className="py-10 text-center text-sm text-muted-foreground">
            正在校验后台登录状态...
          </CardContent>
        </Card>
      </main>
    );
  }

  return (
    <main className="flex min-h-screen bg-[#f3f6fb] text-slate-900">
      <aside
        className={
          sidebarCollapsed
            ? "hidden w-[72px] shrink-0 border-r border-slate-200 bg-white transition-all duration-200 xl:flex xl:flex-col"
            : "hidden w-[220px] shrink-0 border-r border-slate-200 bg-white transition-all duration-200 xl:flex xl:flex-col"
        }
      >
        <div className="border-b border-slate-100">
          <div
            className={
              sidebarCollapsed
                ? "flex h-15 items-center justify-center px-3"
                : "flex h-15 items-center gap-3 px-4"
            }
          >
            <button
              type="button"
              onClick={() => {
                if (sidebarCollapsed) {
                  setSidebarCollapsed(false);
                }
              }}
              className={
                sidebarCollapsed
                  ? "flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[#edf4ff] text-[#2f77ff]"
                  : "flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[#edf4ff] text-[#2f77ff]"
              }
              aria-label={sidebarCollapsed ? "展开菜单栏" : "管理台"}
            >
              <ShieldCheck className="size-4.5" />
            </button>
            {!sidebarCollapsed ? (
              <>
                <div className="min-w-0">
                  <p className="truncate text-[14px] font-semibold text-slate-950">智助管理台</p>
                  <p className="text-[11px] text-slate-400">Enterprise Console</p>
                </div>
                <button
                  type="button"
                  onClick={() => setSidebarCollapsed(true)}
                  className="ml-auto hidden h-7 items-center justify-center px-1 text-slate-400 transition hover:text-slate-700 xl:flex"
                  aria-label="收起菜单栏"
                >
                  <SidebarClose className="size-4" />
                </button>
              </>
            ) : null}
          </div>
        </div>

        <nav className="flex-1 px-2 py-4">
          {menuGroups.map((group) => {
            const GroupIcon = group.icon;
            const expanded = expandedGroups[group.label];
            const groupActive = group.items.some((item) => item.href === activePath);

            return (
              <div key={group.label} className="mb-1.5">
                <button
                  type="button"
                  onClick={() => toggleGroup(group.label)}
                  className={
                    sidebarCollapsed
                      ? groupActive
                        ? "flex h-11 w-full items-center justify-center rounded-lg bg-[#f3f7ff] text-[#2f77ff]"
                        : "flex h-11 w-full items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-50 hover:text-slate-900"
                      : groupActive
                        ? "flex h-11 w-full items-center gap-3 px-3 text-left text-[#2f77ff]"
                        : "flex h-11 w-full items-center gap-3 px-3 text-left text-slate-700 transition hover:text-slate-950"
                  }
                  title={sidebarCollapsed ? group.label : undefined}
                >
                  <GroupIcon className={groupActive ? "size-4 shrink-0 text-[#2f77ff]" : "size-4 shrink-0 text-slate-500"} />
                  {!sidebarCollapsed ? (
                    <>
                      <span className="flex-1 text-[15px] font-medium">{group.label}</span>
                      {expanded ? (
                        <ChevronDown className="size-4 text-slate-400" />
                      ) : (
                        <ChevronRight className="size-4 text-slate-400" />
                      )}
                    </>
                  ) : null}
                </button>

                {!sidebarCollapsed && expanded ? (
                  <div className="mt-1 space-y-1 pl-3">
                    {group.items.map((menu) => {
                      const Icon = menu.icon;
                      const active = activePath === menu.href;

                      return (
                        <Link
                          key={`${group.label}-${menu.label}`}
                          href={menu.href}
                          className={
                            active
                              ? "relative flex h-10 items-center gap-3 rounded-lg bg-[#eef4ff] px-3 text-[14px] font-medium text-[#2f77ff]"
                              : "relative flex h-10 items-center gap-3 rounded-lg px-3 text-[14px] font-medium text-slate-700 transition hover:bg-slate-50 hover:text-slate-950"
                          }
                        >
                          <Icon className={active ? "size-4 shrink-0 text-[#2f77ff]" : "size-4 shrink-0 text-slate-400"} />
                          <span className="truncate">{menu.label}</span>
                        </Link>
                      );
                    })}
                  </div>
                ) : null}
              </div>
            );
          })}
        </nav>
      </aside>

      <section className="min-w-0 flex-1">
        <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/92 backdrop-blur">
          <div className="flex h-14 items-center justify-between px-5 lg:px-8">
            <div className="flex items-center gap-3">
              <div className="hidden items-center gap-2 text-[12px] text-slate-400 md:flex">
                <span>首页</span>
                <ChevronRight className="size-3.5" />
                <span>账号中心</span>
                <ChevronRight className="size-3.5" />
                <span className="text-slate-600">{title}</span>
              </div>
            </div>

            <div ref={menuRef} className="relative">
              {menuOpen ? (
                <div className="absolute top-[calc(100%+10px)] right-0 z-20 w-[220px] overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-[0_18px_38px_rgba(15,23,42,0.14)]">
                  <div className="flex items-center gap-3 px-4 py-4">
                    <div className="flex h-11 w-11 items-center justify-center rounded-full bg-slate-100 text-sm font-semibold text-slate-500">
                      {profile?.nickname?.slice(0, 1) ?? "A"}
                    </div>
                    <div className="min-w-0">
                      <p className="truncate text-sm font-semibold text-slate-900">
                        {profile?.nickname ?? "管理员"}
                      </p>
                      <p className="truncate text-xs text-slate-400">{profile?.username}</p>
                    </div>
                  </div>
                  <div className="mx-4 h-px bg-slate-100" />
                  <button
                    type="button"
                    onClick={handleLogout}
                    disabled={isPending}
                    className="flex w-full items-center gap-2 px-4 py-3.5 text-left text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    <LogOut className="size-4" />
                    {isPending ? "退出中..." : "退出登录"}
                  </button>
                </div>
              ) : null}

              <button
                type="button"
                onClick={() => setMenuOpen((value) => !value)}
                className="flex items-center gap-2 rounded-lg px-2 py-1.5 text-left transition hover:bg-slate-100"
              >
                <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-100 text-xs font-semibold text-slate-500">
                  {profile?.nickname?.slice(0, 1) ?? "A"}
                </div>
                <div className="min-w-0">
                  <p className="max-w-[120px] truncate text-sm font-medium text-slate-800">
                    {profile?.nickname ?? "管理员"}
                  </p>
                  <p className="max-w-[120px] truncate text-xs text-slate-400">
                    {profile?.username}
                  </p>
                </div>
                <ChevronDown
                  className={
                    menuOpen
                      ? "size-4 shrink-0 text-slate-400 transition-transform rotate-180"
                      : "size-4 shrink-0 text-slate-400 transition-transform"
                  }
                />
              </button>
            </div>
          </div>
        </header>

        <div className="px-5 py-5 lg:px-8">
          {description ? (
            <div className="mb-5 rounded-2xl border border-slate-200 bg-white px-5 py-4">
              <h1 className="text-[22px] font-semibold text-slate-950">{title}</h1>
              <p className="mt-1 text-sm text-slate-500">{description}</p>
            </div>
          ) : null}
          {children}
        </div>
      </section>
    </main>
  );
}
