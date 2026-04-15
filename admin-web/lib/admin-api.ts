import { request } from "@/lib/api";
import { encryptLoginPassword } from "@/lib/login-crypto";

export type LoginUser = {
  userId: number;
  username: string;
  nickname: string;
  actorType: string;
  superAdmin: boolean;
};

export type UserItem = {
  id: number;
  username: string;
  nickname: string;
  enabled: boolean;
  superAdmin?: boolean | null;
  createBy?: number | null;
  updateBy?: number | null;
  createByName?: string | null;
  updateByName?: string | null;
  deleted?: boolean;
  lastLoginTime?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type PageResult<T> = {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
};

type UserListResponse = UserItem[] | PageResult<UserItem>;

export type UserPageParams = {
  keyword?: string;
  enabled?: boolean;
  createdFrom?: string;
  createdTo?: string;
  pageNum?: number;
  pageSize?: number;
};

export type CreateUserPayload = {
  username: string;
  nickname: string;
  password: string;
};

export type UpdateUserPayload = {
  nickname: string;
};

async function encryptPasswordPayload(password: string) {
  return encryptLoginPassword(password).then((encryptedPassword) => ({
    encryptedPassword,
  }));
}

export function adminLogin(payload: {
  username: string;
  password: string;
}) {
  return encryptLoginPassword(payload.password).then((encryptedPassword) =>
    request<LoginUser>("/api/admin/auth/login", {
      method: "POST",
      body: JSON.stringify({
        username: payload.username,
        encryptedPassword,
      }),
    }),
  );
}

export function adminLogout() {
  return request<void>("/api/admin/auth/logout", {
    method: "POST",
  });
}

export function fetchAdminProfile() {
  return request<LoginUser>("/api/admin/auth/me");
}

function normalizeDateBoundary(value: string | undefined, type: "start" | "end") {
  if (!value) {
    return undefined;
  }
  const trimmed = value.trim();
  if (!trimmed) {
    return undefined;
  }
  return `${trimmed} ${type === "start" ? "00:00:00" : "23:59:59"}`;
}

function buildUserPagePayload(params: UserPageParams = {}) {
  return {
    keyword: params.keyword?.trim() || undefined,
    enabled: typeof params.enabled === "boolean" ? params.enabled : undefined,
    createdFrom: normalizeDateBoundary(params.createdFrom, "start"),
    createdTo: normalizeDateBoundary(params.createdTo, "end"),
  };
}

function buildPageQuery(params: UserPageParams = {}) {
  const searchParams = new URLSearchParams();
  searchParams.set("pageNum", String(params.pageNum ?? 1));
  searchParams.set("pageSize", String(params.pageSize ?? 10));
  return searchParams.toString();
}

export function fetchAdminUsers(params: UserPageParams = {}) {
  return request<UserListResponse>(`/api/admin/admin-users/list?${buildPageQuery(params)}`, {
    method: "POST",
    body: JSON.stringify(buildUserPagePayload(params)),
  }).then((data) =>
    Array.isArray(data)
      ? {
          records: data,
          total: data.length,
          pageNum: params.pageNum ?? 1,
          pageSize: params.pageSize ?? 10,
        }
      : data,
  );
}

export function createAdminUser(payload: CreateUserPayload) {
  return encryptPasswordPayload(payload.password).then((passwordPayload) =>
    request<UserItem>("/api/admin/admin-users/create", {
      method: "POST",
      body: JSON.stringify({
        username: payload.username,
        nickname: payload.nickname,
        ...passwordPayload,
      }),
    }),
  );
}

export function updateAdminUser(id: number, payload: UpdateUserPayload) {
  return request<UserItem>("/api/admin/admin-users/update", {
    method: "POST",
    body: JSON.stringify({ id, ...payload }),
  });
}

export function updateAdminUserEnabled(id: number, enabled: boolean) {
  return request<void>("/api/admin/admin-users/update-enabled", {
    method: "POST",
    body: JSON.stringify({ id, enabled }),
  });
}

export function resetAdminUserPassword(id: number, password: string) {
  return encryptPasswordPayload(password).then((passwordPayload) =>
    request<void>("/api/admin/admin-users/reset-password", {
      method: "POST",
      body: JSON.stringify({ id, ...passwordPayload }),
    }),
  );
}

export function fetchAppUsers(params: UserPageParams = {}) {
  return request<UserListResponse>(`/api/admin/app-users/list?${buildPageQuery(params)}`, {
    method: "POST",
    body: JSON.stringify(buildUserPagePayload(params)),
  }).then((data) =>
    Array.isArray(data)
      ? {
          records: data,
          total: data.length,
          pageNum: params.pageNum ?? 1,
          pageSize: params.pageSize ?? 10,
        }
      : data,
  );
}

export function createAppUser(payload: CreateUserPayload) {
  return encryptPasswordPayload(payload.password).then((passwordPayload) =>
    request<UserItem>("/api/admin/app-users/create", {
      method: "POST",
      body: JSON.stringify({
        username: payload.username,
        nickname: payload.nickname,
        ...passwordPayload,
      }),
    }),
  );
}

export function updateAppUser(id: number, payload: UpdateUserPayload) {
  return request<UserItem>("/api/admin/app-users/update", {
    method: "POST",
    body: JSON.stringify({ id, ...payload }),
  });
}

export function updateAppUserEnabled(id: number, enabled: boolean) {
  return request<void>("/api/admin/app-users/update-enabled", {
    method: "POST",
    body: JSON.stringify({ id, enabled }),
  });
}

export function resetAppUserPassword(id: number, password: string) {
  return encryptPasswordPayload(password).then((passwordPayload) =>
    request<void>("/api/admin/app-users/reset-password", {
      method: "POST",
      body: JSON.stringify({ id, ...passwordPayload }),
    }),
  );
}
