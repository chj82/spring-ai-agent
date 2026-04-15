import type { Metadata } from "next";
import { cn } from "@/lib/utils";
import "./globals.css";

export const metadata: Metadata = {
  title: "Agent Admin",
  description: "AI 助手后台管理端",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN" className={cn("h-full antialiased", "font-sans")}>
      <body className="min-h-full bg-[linear-gradient(180deg,_#edf7fb_0%,_#f7fafc_100%)] text-slate-900">
        {children}
      </body>
    </html>
  );
}
