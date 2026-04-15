import { AdminShell } from "@/components/admin-shell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ArrowUpRight, ShieldCheck, UserRound, Users } from "lucide-react";

export default function HomePage() {
  const stats = [
    { label: "客户总数", value: "128", icon: Users },
    { label: "后台用户", value: "6", icon: ShieldCheck },
    { label: "今日登录", value: "24", icon: UserRound },
  ];

  return (
    <AdminShell
      title="管理工作台"
      description="当前版本先聚焦后台用户和前端客户管理，为后续后台扩展留好入口。"
    >
      <div className="grid gap-4 xl:grid-cols-[1.2fr_1fr_1fr]">
        {stats.map((item) => {
          const Icon = item.icon;
          return (
          <Card
            key={item.label}
            className="rounded-[12px] border border-slate-200 bg-white shadow-none"
          >
            <CardHeader className="flex flex-row items-start justify-between p-5">
              <div>
                <CardTitle className="text-sm font-medium text-slate-500">
                  {item.label}
                </CardTitle>
                <p className="mt-4 text-3xl font-semibold text-slate-950">{item.value}</p>
              </div>
              <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#eef4ff] text-[#2f77ff]">
                <Icon className="size-5" />
              </div>
            </CardHeader>
            <CardContent className="px-5 pb-5 pt-0">
              <div className="flex items-center gap-2 text-sm text-emerald-600">
                <ArrowUpRight className="size-4" />
                较昨日稳定增长
              </div>
            </CardContent>
          </Card>
          );
        })}
      </div>
    </AdminShell>
  );
}
