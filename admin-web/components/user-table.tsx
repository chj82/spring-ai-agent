"use client";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type {
  CreateUserPayload,
  UpdateUserPayload,
  UserPageParams,
  UserItem,
} from "@/lib/admin-api";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useEffect, useRef, useState } from "react";
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  CircleCheck,
  CircleX,
  RefreshCcw,
  Search,
} from "lucide-react";

type ToastState =
  | {
      type: "success" | "error";
      text: string;
    }
  | null;

function renderEnabled(enabled: boolean) {
  return enabled ? "启用" : "禁用";
}

function formatDateTime(value?: string | null) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("zh-CN", {
    hour12: false,
  });
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

function formatDateFieldValue(value: string) {
  if (!value) {
    return "年 / 月 / 日";
  }
  return value.replace(/-/g, " / ");
}

function renderEnabledFilterLabel(value: string) {
  if (value === "true") {
    return "启用";
  }
  if (value === "false") {
    return "禁用";
  }
  return "全部";
}

export function UserTable({
  rows = [],
  total = 0,
  pageNum = 1,
  pageSize = 10,
  filters = {},
  loading,
  error,
  emptyText,
  actorLabel,
  showSuperAdmin = false,
  onQueryChange,
  onCreate,
  onUpdate,
  onUpdateEnabled,
  onResetPassword,
}: Readonly<{
  rows: UserItem[];
  total: number;
  pageNum: number;
  pageSize: number;
  filters: UserPageParams;
  loading: boolean;
  error: string;
  emptyText: string;
  actorLabel: string;
  showSuperAdmin?: boolean;
  onQueryChange: (query: UserPageParams) => Promise<void>;
  onCreate: (payload: CreateUserPayload) => Promise<void>;
  onUpdate: (id: number, payload: UpdateUserPayload) => Promise<void>;
  onUpdateEnabled: (id: number, enabled: boolean) => Promise<void>;
  onResetPassword: (id: number, password: string) => Promise<void>;
}>) {
  const [toast, setToast] = useState<ToastState>(null);
  const [submitting, setSubmitting] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [createUsername, setCreateUsername] = useState("");
  const [createNickname, setCreateNickname] = useState("");
  const [createPassword, setCreatePassword] = useState("");

  const [editOpen, setEditOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserItem | null>(null);
  const [editNickname, setEditNickname] = useState("");

  const [resetOpen, setResetOpen] = useState(false);
  const [resetUser, setResetUser] = useState<UserItem | null>(null);
  const [resetPassword, setResetPassword] = useState("");
  const [keywordInput, setKeywordInput] = useState("");
  const [keyword, setKeyword] = useState("");
  const [enabledInput, setEnabledInput] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");
  const [createdFromInput, setCreatedFromInput] = useState("");
  const [createdFrom, setCreatedFrom] = useState("");
  const [createdToInput, setCreatedToInput] = useState("");
  const [createdTo, setCreatedTo] = useState("");
  const createdFromRef = useRef<HTMLInputElement | null>(null);
  const createdToRef = useRef<HTMLInputElement | null>(null);

  const [enabledUser, setEnabledUser] = useState<UserItem | null>(null);
  const toastTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    setKeywordInput(filters.keyword ?? "");
    setKeyword(filters.keyword ?? "");
    const enabledValue =
      typeof filters.enabled === "boolean" ? String(filters.enabled) : "";
    setEnabledInput(enabledValue);
    setEnabledFilter(enabledValue);
    setCreatedFromInput(filters.createdFrom ?? "");
    setCreatedFrom(filters.createdFrom ?? "");
    setCreatedToInput(filters.createdTo ?? "");
    setCreatedTo(filters.createdTo ?? "");
  }, [filters]);

  useEffect(() => {
    return () => {
      if (toastTimerRef.current) {
        clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  const totalPages = total === 0 ? 1 : Math.ceil(total / pageSize);
  const currentPage = Math.min(Math.max(pageNum, 1), totalPages);

  const resetCreateForm = () => {
    setCreateUsername("");
    setCreateNickname("");
    setCreatePassword("");
  };

  const showToast = (type: "success" | "error", text: string) => {
    if (toastTimerRef.current) {
      clearTimeout(toastTimerRef.current);
    }
    setToast({ type, text });
    toastTimerRef.current = setTimeout(() => {
      setToast(null);
      toastTimerRef.current = null;
    }, 2200);
  };

  const handleCreate = async () => {
    setSubmitting(true);
    try {
      await onCreate({
        username: createUsername.trim(),
        nickname: createNickname.trim(),
        password: createPassword,
      });
      setCreateOpen(false);
      resetCreateForm();
      showToast("success", `${actorLabel}创建成功`);
    } catch (requestError) {
      showToast("error", getErrorMessage(requestError, `创建${actorLabel}失败`));
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = async () => {
    if (!editingUser) {
      return;
    }

    setSubmitting(true);
    try {
      await onUpdate(editingUser.id, {
        nickname: editNickname.trim(),
      });
      setEditOpen(false);
      setEditingUser(null);
      showToast("success", `${actorLabel}信息已更新`);
    } catch (requestError) {
      showToast("error", getErrorMessage(requestError, `更新${actorLabel}失败`));
    } finally {
      setSubmitting(false);
    }
  };

  const handleResetPassword = async () => {
    if (!resetUser) {
      return;
    }

    setSubmitting(true);
    try {
      await onResetPassword(resetUser.id, resetPassword);
      setResetOpen(false);
      setResetUser(null);
      setResetPassword("");
      showToast("success", `${actorLabel}密码已重置`);
    } catch (requestError) {
      showToast("error", getErrorMessage(requestError, `重置${actorLabel}密码失败`));
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdateEnabled = async () => {
    if (!enabledUser) {
      return;
    }

    const nextEnabled = !enabledUser.enabled;

    setSubmitting(true);
    try {
      await onUpdateEnabled(enabledUser.id, nextEnabled);
      setEnabledUser(null);
      showToast("success", `${actorLabel}${nextEnabled ? "已启用" : "已禁用"}`);
    } catch (requestError) {
      showToast(
        "error",
        getErrorMessage(
          requestError,
          `${nextEnabled ? "启用" : "禁用"}${actorLabel}失败`,
        ),
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleSearch = async () => {
    setKeyword(keywordInput.trim());
    setEnabledFilter(enabledInput);
    setCreatedFrom(createdFromInput);
    setCreatedTo(createdToInput);
    await onQueryChange({
      keyword: keywordInput.trim() || undefined,
      enabled:
        enabledInput === "" || enabledInput === "all"
          ? undefined
          : enabledInput === "true",
      createdFrom: createdFromInput || undefined,
      createdTo: createdToInput || undefined,
      pageNum: 1,
      pageSize,
    });
  };

  const handleResetFilters = async () => {
    setKeywordInput("");
    setKeyword("");
    setEnabledInput("");
    setEnabledFilter("");
    setCreatedFromInput("");
    setCreatedFrom("");
    setCreatedToInput("");
    setCreatedTo("");
    await onQueryChange({
      pageNum: 1,
      pageSize,
    });
  };

  const handlePageChange = async (nextPage: number) => {
    if (nextPage < 1 || nextPage > totalPages || nextPage === currentPage) {
      return;
    }

    await onQueryChange({
      keyword: keyword || undefined,
      enabled:
        enabledFilter === "" || enabledFilter === "all"
          ? undefined
          : enabledFilter === "true",
      createdFrom: createdFrom || undefined,
      createdTo: createdTo || undefined,
      pageNum: nextPage,
      pageSize,
    });
  };

  const openDatePicker = (ref: { current: HTMLInputElement | null }) => {
    const input = ref.current;
    if (!input) {
      return;
    }

    if ("showPicker" in input && typeof input.showPicker === "function") {
      input.showPicker();
      return;
    }

    input.focus();
    input.click();
  };

  return (
    <>
      {toast ? (
        <div className="pointer-events-none fixed top-4 left-1/2 z-50 -translate-x-1/2">
          <div
            className={
              toast.type === "success"
                ? "flex min-w-[356px] items-center gap-3 rounded-2xl border border-emerald-200 bg-[#f2fff7] px-4 py-4 text-sm font-medium text-emerald-700 shadow-[0_10px_24px_rgba(34,197,94,0.14)]"
                : "flex min-w-[356px] items-center gap-3 rounded-2xl border border-red-200 bg-[#fff4f4] px-4 py-4 text-sm font-medium text-red-700 shadow-[0_10px_24px_rgba(239,68,68,0.14)]"
            }
          >
            {toast.type === "success" ? (
              <CircleCheck className="size-5 shrink-0 text-emerald-600" />
            ) : (
              <CircleX className="size-5 shrink-0 text-red-600" />
            )}
            {toast.text}
          </div>
        </div>
      ) : null}

      <div className="rounded-[14px] border border-slate-200 bg-white px-5 py-5">
        <div className="grid gap-3 lg:grid-cols-2 xl:grid-cols-4">
          <div className="relative">
            <Search className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-slate-400" />
            <Input
              value={keywordInput}
              onChange={(event) => setKeywordInput(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  void handleSearch();
                }
              }}
              placeholder={`请输入${actorLabel}账号或昵称`}
              className="h-10 rounded-md border-slate-200 bg-white pl-9"
            />
          </div>

          <Select
            value={enabledInput || "all"}
            onValueChange={(value) => setEnabledInput(value ?? "")}
          >
            <SelectTrigger className="!h-10 w-full rounded-md border-slate-200 bg-white px-3 py-0 text-sm text-slate-700 data-placeholder:text-slate-400">
              <SelectValue>{renderEnabledFilterLabel(enabledInput)}</SelectValue>
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部</SelectItem>
              <SelectItem value="true">启用</SelectItem>
              <SelectItem value="false">禁用</SelectItem>
            </SelectContent>
          </Select>

          <button
            type="button"
            onClick={() => openDatePicker(createdFromRef)}
            className="relative flex h-10 w-full items-center rounded-md border border-slate-200 bg-white px-3 text-left text-sm text-slate-700 transition hover:border-slate-300"
          >
            <span className={createdFromInput ? "" : "text-slate-400"}>
              {formatDateFieldValue(createdFromInput)}
            </span>
            <CalendarDays className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-slate-400" />
            <input
              ref={createdFromRef}
              type="date"
              value={createdFromInput}
              onChange={(event) => setCreatedFromInput(event.target.value)}
              className="pointer-events-none absolute inset-0 opacity-0"
            />
          </button>

          <button
            type="button"
            onClick={() => openDatePicker(createdToRef)}
            className="relative flex h-10 w-full items-center rounded-md border border-slate-200 bg-white px-3 text-left text-sm text-slate-700 transition hover:border-slate-300"
          >
            <span className={createdToInput ? "" : "text-slate-400"}>
              {formatDateFieldValue(createdToInput)}
            </span>
            <CalendarDays className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-slate-400" />
            <input
              ref={createdToRef}
              type="date"
              value={createdToInput}
              onChange={(event) => setCreatedToInput(event.target.value)}
              className="pointer-events-none absolute inset-0 opacity-0"
            />
          </button>
        </div>

        <div className="mt-4 flex flex-wrap items-center justify-end gap-2">
          <Button
            variant="outline"
            className="h-9 rounded-md border-slate-200 bg-white px-4"
            onClick={() => void handleResetFilters()}
          >
            <RefreshCcw className="size-4" />
            重置
          </Button>
          <Button
            className="h-9 rounded-md bg-[#2f77ff] px-5 hover:bg-[#2868dd]"
            onClick={() => void handleSearch()}
          >
            <Search className="size-4" />
            查询
          </Button>
        </div>
      </div>

      <div className="mt-4 rounded-[14px] border border-slate-200 bg-white px-5 py-5">
        <div className="mb-4 flex flex-wrap items-center justify-end gap-3">
          <Button
            onClick={() => setCreateOpen(true)}
            className="h-10 rounded-md bg-[#2f77ff] px-5 hover:bg-[#2868dd]"
          >
            新增
          </Button>
        </div>

        {loading ? (
          <div className="rounded-lg border border-dashed px-4 py-10 text-center text-sm text-muted-foreground">
            正在加载数据...
          </div>
        ) : error ? (
          <div className="rounded-lg border border-destructive/20 bg-destructive/10 px-4 py-6 text-sm text-destructive">
            {error}
          </div>
        ) : rows.length === 0 ? (
          <div className="rounded-lg border border-dashed px-4 py-10 text-center text-sm text-muted-foreground">
            {emptyText}
          </div>
        ) : (
          <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
            <Table>
              <TableHeader className="bg-[#eef3fb]">
                <TableRow>
                  <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">账号</TableHead>
                  <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">昵称</TableHead>
                  {showSuperAdmin ? (
                    <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">超级管理员</TableHead>
                  ) : null}
                  <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">是否启用</TableHead>
                  <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">创建人</TableHead>
                  <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">修改人</TableHead>
                  <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">创建时间</TableHead>
                  <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">最后登录时间</TableHead>
                  <TableHead className="h-11 px-4 text-[13px] font-medium text-slate-600">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((row) => (
                  <TableRow key={row.id} className="border-slate-100 hover:bg-slate-50/80">
                    <TableCell className="px-4 py-3 font-medium">{row.username}</TableCell>
                    <TableCell className="px-4 py-3">{row.nickname}</TableCell>
                    {showSuperAdmin ? (
                      <TableCell className="px-4">
                        <Badge
                          variant={row.superAdmin ? "secondary" : "outline"}
                          className={
                            row.superAdmin
                              ? "bg-amber-100 text-amber-700"
                              : "border-slate-200 text-slate-500"
                          }
                        >
                          {row.superAdmin ? "是" : "否"}
                        </Badge>
                      </TableCell>
                    ) : null}
                    <TableCell className="px-4">
                      <Badge
                        variant={row.enabled ? "secondary" : "outline"}
                        className={
                          row.enabled
                            ? "bg-[#eaf2ff] text-[#2f77ff]"
                            : "border-slate-200 text-slate-500"
                        }
                      >
                        {renderEnabled(row.enabled)}
                      </Badge>
                    </TableCell>
                    <TableCell className="px-4 py-3 text-muted-foreground">
                      {row.createByName || "-"}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-muted-foreground">
                      {row.updateByName || "-"}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-muted-foreground">
                      {formatDateTime(row.createdAt)}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-muted-foreground">
                      {formatDateTime(row.lastLoginTime)}
                    </TableCell>
                    <TableCell className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          className="h-8 rounded-md"
                          onClick={() => {
                            setEditingUser(row);
                            setEditNickname(row.nickname);
                            setEditOpen(true);
                          }}
                        >
                          编辑
                        </Button>
                        <Button
                          variant={row.enabled ? "destructive" : "secondary"}
                          size="sm"
                          className="h-8 rounded-md"
                          onClick={() => setEnabledUser(row)}
                        >
                          {row.enabled ? "禁用" : "启用"}
                        </Button>
                        <Button
                          variant="secondary"
                          size="sm"
                          className="h-8 rounded-md"
                          onClick={() => {
                            setResetUser(row);
                            setResetPassword("");
                            setResetOpen(true);
                          }}
                        >
                          重置密码
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 px-4 py-3 text-[13px] text-slate-500">
              <p>
                第 {currentPage} 页 / 共 {totalPages} 页，共 {total} 条记录
              </p>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  className="h-8 rounded-md px-3"
                  disabled={loading || currentPage <= 1}
                  onClick={() => void handlePageChange(currentPage - 1)}
                >
                  <ChevronLeft className="size-4" />
                  上一页
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  className="h-8 rounded-md px-3"
                  disabled={loading || currentPage >= totalPages}
                  onClick={() => void handlePageChange(currentPage + 1)}
                >
                  下一页
                  <ChevronRight className="size-4" />
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>

      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>新增{actorLabel}</DialogTitle>
            <DialogDescription>创建新的{actorLabel}账号并设置初始密码。</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="create-username">账号</Label>
              <Input
                id="create-username"
                value={createUsername}
                onChange={(event) => setCreateUsername(event.target.value)}
                placeholder="请输入登录账号"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="create-nickname">昵称</Label>
              <Input
                id="create-nickname"
                value={createNickname}
                onChange={(event) => setCreateNickname(event.target.value)}
                placeholder="请输入昵称"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="create-password">密码</Label>
              <Input
                id="create-password"
                type="password"
                value={createPassword}
                onChange={(event) => setCreatePassword(event.target.value)}
                placeholder="请输入 6-32 位密码"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setCreateOpen(false);
                resetCreateForm();
              }}
            >
              取消
            </Button>
            <Button
              onClick={handleCreate}
              disabled={
                submitting ||
                !createUsername.trim() ||
                !createNickname.trim() ||
                createPassword.length < 6
              }
            >
              {submitting ? "保存中..." : "确认创建"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={editOpen}
        onOpenChange={(open) => {
          setEditOpen(open);
          if (!open) {
            setEditingUser(null);
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>编辑{actorLabel}</DialogTitle>
            <DialogDescription>当前只支持修改昵称信息。</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="edit-username">账号</Label>
              <Input id="edit-username" value={editingUser?.username ?? ""} disabled />
            </div>
            <div className="space-y-2">
              <Label htmlFor="edit-nickname">昵称</Label>
              <Input
                id="edit-nickname"
                value={editNickname}
                onChange={(event) => setEditNickname(event.target.value)}
                placeholder="请输入昵称"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setEditOpen(false);
                setEditingUser(null);
              }}
            >
              取消
            </Button>
            <Button onClick={handleEdit} disabled={submitting || !editNickname.trim()}>
              {submitting ? "保存中..." : "保存修改"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={resetOpen}
        onOpenChange={(open) => {
          setResetOpen(open);
          if (!open) {
            setResetUser(null);
            setResetPassword("");
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>重置密码</DialogTitle>
            <DialogDescription>
              为 {resetUser?.username ?? actorLabel} 设置新的登录密码。
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-2">
            <Label htmlFor="reset-password">新密码</Label>
            <Input
              id="reset-password"
              type="password"
              value={resetPassword}
              onChange={(event) => setResetPassword(event.target.value)}
              placeholder="请输入 6-32 位密码"
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setResetOpen(false)}>
              取消
            </Button>
            <Button
              onClick={handleResetPassword}
              disabled={submitting || resetPassword.length < 6}
            >
              {submitting ? "提交中..." : "确认重置"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={!!enabledUser} onOpenChange={(open) => !open && setEnabledUser(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {enabledUser?.enabled ? "确认禁用" : "确认启用"}
              {actorLabel}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {enabledUser
                ? `确定要${enabledUser.enabled ? "禁用" : "启用"}账号 ${enabledUser.username} 吗？`
                : ""}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction onClick={handleUpdateEnabled} disabled={submitting}>
              {submitting ? "处理中..." : "确认"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
