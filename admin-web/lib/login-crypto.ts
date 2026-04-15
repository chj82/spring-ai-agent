import { API_BASE_URL, request } from "@/lib/api";

type LoginPublicKey = {
  algorithm: string;
  publicKey: string;
};

function pemToArrayBuffer(pem: string) {
  const base64 = pem
    .replace("-----BEGIN PUBLIC KEY-----", "")
    .replace("-----END PUBLIC KEY-----", "")
    .replace(/\s+/g, "");

  const binary = window.atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index);
  }
  return bytes.buffer;
}

function arrayBufferToBase64(buffer: ArrayBuffer) {
  const bytes = new Uint8Array(buffer);
  let binary = "";
  for (const value of bytes) {
    binary += String.fromCharCode(value);
  }
  return window.btoa(binary);
}

export async function encryptLoginPassword(password: string) {
  const payload = await request<LoginPublicKey>("/api/public/auth/login-public-key", {
    method: "GET",
  });

  if (payload.algorithm !== "RSA-OAEP-256") {
    throw new Error("不支持的登录加密算法");
  }

  const cryptoKey = await window.crypto.subtle.importKey(
    "spki",
    pemToArrayBuffer(payload.publicKey),
    {
      name: "RSA-OAEP",
      hash: "SHA-256",
    },
    false,
    ["encrypt"],
  );

  const encrypted = await window.crypto.subtle.encrypt(
    {
      name: "RSA-OAEP",
    },
    cryptoKey,
    new TextEncoder().encode(password),
  );

  return arrayBufferToBase64(encrypted);
}
