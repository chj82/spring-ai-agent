"use client";

import { AdminShell } from "@/components/admin-shell";
import { UserTable } from "@/components/user-table";
import {
  createAdminUser,
  fetchAdminUsers,
  type UserPageParams,
  resetAdminUserPassword,
  type CreateUserPayload,
  type UpdateUserPayload,
  type UserItem,
  updateAdminUser,
  updateAdminUserEnabled,
} from "@/lib/admin-api";
import { useCallback, useEffect, useState } from "react";

export default function UsersPage() {
  const [rows, setRows] = useState<UserItem[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [query, setQuery] = useState<UserPageParams>({
    pageNum: 1,
    pageSize: 10,
  });

  const load = useCallback(async (params: UserPageParams) => {
    setError("");
    setLoading(true);
    try {
      const data = await fetchAdminUsers(params);
      setRows(data.records);
      setTotal(data.total);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
    } catch (requestError) {
      const message =
        requestError instanceof Error ? requestError.message : "加载后台用户失败";
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load({
      pageNum: 1,
      pageSize: 10,
    });
  }, [load]);

  const handleQueryChange = async (nextQuery: UserPageParams) => {
    setQuery(nextQuery);
    await load(nextQuery);
  };

  const handleCreate = async (payload: CreateUserPayload) => {
    await createAdminUser(payload);
    const nextQuery = { ...query, pageNum: 1 };
    setQuery(nextQuery);
    await load(nextQuery);
  };

  const handleUpdate = async (id: number, payload: UpdateUserPayload) => {
    await updateAdminUser(id, payload);
    await load(query);
  };

  const handleEnabledChange = async (id: number, enabled: boolean) => {
    await updateAdminUserEnabled(id, enabled);
    await load(query);
  };

  const handleResetPassword = async (id: number, password: string) => {
    await resetAdminUserPassword(id, password);
    await load(query);
  };

  return (
    <AdminShell title="后台用户管理">
      <UserTable
        rows={rows}
        total={total}
        pageNum={pageNum}
        pageSize={pageSize}
        filters={query}
        loading={loading}
        error={error}
        emptyText="暂无后台用户数据"
        actorLabel="后台用户"
        showSuperAdmin
        onQueryChange={handleQueryChange}
        onCreate={handleCreate}
        onUpdate={handleUpdate}
        onUpdateEnabled={handleEnabledChange}
        onResetPassword={handleResetPassword}
      />
    </AdminShell>
  );
}
