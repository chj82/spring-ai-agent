import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Agent Client",
  description: "AI 助手客户端",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN" className="h-full antialiased">
      <body className="min-h-full bg-[linear-gradient(180deg,_#f4efe5_0%,_#f8fbfc_100%)] text-slate-900">
        {children}
      </body>
    </html>
  );
}
