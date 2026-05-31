/**
 * API 層
 * 處理所有 HTTP 請求、token 管理、自動刷新 token
 */

let accessToken = null;
let refreshToken = null;
let currentUser = null; // { user_id, user_name, user_type }

/**
 * 所有需要登入的 API 都透過這個函式送出
 * 自動帶 Authorization header，並處理 token 過期
 */
async function apiFetch(method, path, body = null) {
    const headers = { 'Content-Type': 'application/json' };
    // 後端目前用 LoginSession.refresh_token_hash 驗證 /me、manager 權限與改密碼 API，
    // 因此 Authorization 必須帶 refreshToken；若只帶 accessToken，manager-only API 會一直被判定權限不足。
    const authToken = refreshToken || accessToken;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const res = await fetch(BASE_URL + path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null,
    });

    // access_token 過期（401）→ 自動嘗試用 refresh_token 換新的
    if (res.status === 401 && refreshToken) {
        const refreshed = await tryRefreshToken();
        if (refreshed) {
            // 用新 token 重打一次
            headers['Authorization'] = `Bearer ${refreshToken || accessToken}`;
            return fetch(BASE_URL + path, { method, headers, body: body ? JSON.stringify(body) : null });
        } else {
            // refresh 也失敗 → 登出
            logout();
            return;
        }
    }
    return res;
}

/**
 * POST /auth/refresh — 用 refresh_token 換新的 access_token
 */
async function tryRefreshToken() {
    try {
        const res = await fetch(BASE_URL + '/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refresh_token: refreshToken }),
        });
        const data = await res.json();
        if (data.success) {
            accessToken = data.data.access_token;
            return true;
        }
    } catch (e) {}
    return false;
}

/**
 * 取得當前用戶信息
 */
function getCurrentUser() {
    return currentUser;
}

/**
 * 設定 token 和用戶信息
 */
function setAuth(tokens, user) {
    accessToken = tokens.access_token;
    refreshToken = tokens.refresh_token;
    currentUser = user;
}

/**
 * 清除所有認證信息
 */
function clearAuth() {
    accessToken = null;
    refreshToken = null;
    currentUser = null;
}

/**
 * 檢查是否已登入
 */
function isAuthenticated() {
    return !!accessToken;
}

//重試機制
async function apiFetchWithRetry(method, path, body = null, maxRetries = 2) {
    let lastError;
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
        try {
            const res = await apiFetch(method, path, body);
            if (res.ok || res.status === 400 || res.status === 403 || res.status === 404) {
                return res; // 成功或明確的業務錯誤，不重試
            }
            if (res.status >= 500 && attempt < maxRetries) {
                console.warn(`API ${method} ${path} 返回 ${res.status}，重試第 ${attempt + 1} 次...`);
                await new Promise(r => setTimeout(r, 1000 * (attempt + 1))); // 漸進式等待
                continue;
            }
            return res;
        } catch (e) {
            lastError = e;
            if (attempt < maxRetries) {
                console.warn(`API ${method} ${path} 失敗，重試第 ${attempt + 1} 次...`, e.message);
                await new Promise(r => setTimeout(r, 1000 * (attempt + 1)));
            }
        }
    }
    throw lastError || new Error('API 請求失敗，已重試 ' + maxRetries + ' 次');
}
