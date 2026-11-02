// the one place that knows where the backend is.
export const BASE_URL = "http://localhost:8080";

// fetch that sends the login token (saved by auth.js) with the request.
export function authFetch(url, options = {}) {
  const token = localStorage.getItem("jwtToken");
  const headers = { ...(options.headers || {}) };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return fetch(url, { ...options, headers });
}

// a protected call without a valid token is either answered with 401/403 or redirected to the login page.
export function isLoggedOut(response) {
  return response.redirected || response.status === 401 || response.status === 403;
}
