export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

export class ApiError extends Error {
  code: number;
  status: number;

  constructor(message: string, code: number, status: number) {
    super(message);
    this.code = code;
    this.status = status;
  }
}

function shouldAttachJsonContentType(init?: RequestInit) {
  if (!init?.body) {
    return false;
  }

  const method = (init.method ?? "GET").toUpperCase();
  if (method === "GET" || method === "HEAD") {
    return false;
  }

  const body = init.body;
  if (
    body instanceof FormData ||
    body instanceof URLSearchParams ||
    body instanceof Blob ||
    body instanceof ArrayBuffer ||
    ArrayBuffer.isView(body)
  ) {
    return false;
  }

  return typeof body === "string";
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers ?? {});
  if (shouldAttachJsonContentType(init) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    credentials: "include",
    headers,
  });

  let payload: ApiResponse<T> | null = null;
  try {
    payload = (await response.json()) as ApiResponse<T>;
  } catch {
    payload = null;
  }

  if (!response.ok) {
    throw new ApiError(
      payload?.message ?? `请求失败: ${response.status}`,
      payload?.code ?? response.status,
      response.status,
    );
  }

  if (!payload) {
    throw new ApiError("接口返回为空", 500, response.status);
  }

  if (payload.code !== 0) {
    throw new ApiError(payload.message, payload.code, response.status);
  }

  return payload.data;
}
