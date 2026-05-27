/**
 * 認證相關功能
 * 處理登入、註冊、登出
 */

let selectedLoginRole = 'admin';

/**
 * 選擇登入角色（管理員 / 補貨人員）
 */
function selectRole(role, el) {
    selectedLoginRole = role;
    document.querySelectorAll('.role-tab').forEach(t => t.classList.remove('active'));
    el.classList.add('active');
    
    // 只有補貨人員顯示註冊連結
    document.getElementById('register-link').style.display = 
        role === 'staff' ? 'block' : 'none';
}

/**
 * 切換登入/註冊表單
 */
function toggleRegister(show) {
    document.getElementById('login-form').style.display = show ? 'none' : 'block';
    document.getElementById('register-form').style.display = show ? 'block' : 'none';
    document.getElementById('role-tabs').style.display = show ? 'none' : 'flex';
}

/**
 * POST /auth/register — 註冊新帳號
 */
async function doRegister() {
    const user_name = document.getElementById('inp-reg-name').value.trim();
    const account   = document.getElementById('inp-reg-account').value.trim();
    const password  = document.getElementById('inp-reg-pw').value.trim();
    const err = document.getElementById('reg-err');
    err.style.display = 'none';

    if (!user_name || !account || !password) {
        err.textContent = '請填寫所有欄位'; err.style.display = 'block'; return;
    }

    try {
        const res = await fetch(BASE_URL + '/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ user_name, account, password }),
        });
        const data = await res.json();
        if (!data.success) {
            err.textContent = data.message || '註冊失敗'; err.style.display = 'block'; return;
        }
        toggleRegister(false);
        document.getElementById('inp-id').value = account;
        showToast('✅ 註冊成功！請登入');
    } catch(e) {
        err.textContent = '無法連線至伺服器'; err.style.display = 'block';
    }
}

/**
 * POST /auth/login — 登入系統
 * Request: { account, password }
 * Response: { success, data: { user_id, user_name, user_type, access_token, refresh_token } }
 */
async function doLogin() {
    const account = document.getElementById('inp-id').value.trim();
    const password = document.getElementById('inp-pw').value.trim();
    const err = document.getElementById('login-err');
    err.style.display = 'none';

    if (!account || !password) { err.textContent = '請填寫帳號與密碼'; err.style.display = 'block'; return; }

    if (USE_MOCK) {
        // ── Mock 模式：模擬後端回傳 ──
        const mockUsers = {
            manager01: { password: 'admin123', user_id: 1, user_name: '王小明', user_type: 'Manager' },
            staff01:   { password: 'staff123', user_id: 2, user_name: '陳大文', user_type: 'Staff'   },
        };
        const found = mockUsers[account];
        if (!found || found.password !== password) {
            err.textContent = '帳號或密碼錯誤，請重試'; err.style.display = 'block'; return;
        }
        setAuth(
            { access_token: 'mock_access_token', refresh_token: 'mock_refresh_token' },
            { user_id: found.user_id, user_name: found.user_name, user_type: found.user_type }
        );
        enterSystem();
        return;
    }

    // ── 真實 API 模式 ──
    try {
        const res = await fetch(BASE_URL + '/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ account, password }),
        });
        const data = await res.json();
        if (!data.success) {
            err.textContent = data.message || '帳號或密碼錯誤'; err.style.display = 'block'; return;
        }
        setAuth(
            { access_token: data.data.access_token, refresh_token: data.data.refresh_token },
            { user_id: data.data.user_id, user_name: data.data.user_name, user_type: data.data.user_type }
        );
        enterSystem();
    } catch (e) {
        err.textContent = '無法連線至伺服器，請確認後端是否啟動'; err.style.display = 'block';
    }
}

/**
 * 消費者模式（免登入）
 */
async function loginConsumer() {
    document.getElementById('login-screen').style.display = 'none';
    const cv = document.getElementById('consumer-view');
    cv.style.display = 'flex';

    // 取得所有機台
    let machines = [];
    try {
        const res = await fetch(BASE_URL + '/machines');
        const data = await res.json();
        machines = data.data || [];
    } catch(e) {
        machines = MOCK.machines;
    }

    // 渲染機台選擇按鈕
    const selector = document.getElementById('machine-selector');
    selector.innerHTML = machines.map((m, i) => `
        <button onclick="loadConsumerMachine(${m.machine_id}, '${m.machine_name}')"
            style="padding:8px 14px;border-radius:20px;border:1px solid var(--border);
                   background:${i===0?'var(--accent)':'var(--surface)'};
                   color:${i===0?'#fff':'var(--muted)'};
                   cursor:pointer;font-size:12px;font-weight:700;font-family:inherit;
                   transition:all .2s;"
            id="machine-btn-${m.machine_id}">
            ${m.machine_name}
        </button>
    `).join('');

    // 預設載入第一台
    if (machines.length > 0) {
        loadConsumerMachine(machines[0].machine_id, machines[0].machine_name);
    }
}

/**
 * 載入並顯示特定機台的庫存
 */
async function loadConsumerMachine(machineId, machineName) {
    // 更新按鈕樣式
    document.querySelectorAll('[id^="machine-btn-"]').forEach(btn => {
        btn.style.background = 'var(--surface)';
        btn.style.color = 'var(--muted)';
    });
    const activeBtn = document.getElementById('machine-btn-' + machineId);
    if (activeBtn) { activeBtn.style.background = 'var(--accent)'; activeBtn.style.color = '#fff'; }

    // 更新標題
    document.getElementById('consumer-machine-name').textContent = machineName;
    document.getElementById('consumer-machine-id').textContent = `機台編號: VM-00${machineId}`;
    document.getElementById('consumer-inventory').innerHTML =
        '<div style="text-align:center;color:var(--muted);padding:20px">載入中...</div>';

    // 取得庫存
    try {
        const res = await fetch(`${BASE_URL}/public/machines/${machineId}/inventory`);
        const data = await res.json();
        const items = data.data || [];

        if (items.length === 0) {
            document.getElementById('consumer-inventory').innerHTML =
                '<div style="text-align:center;color:var(--muted);padding:20px">此機台暫無庫存資料</div>';
            return;
        }

        document.getElementById('consumer-inventory').innerHTML = items.map(item => {
            const qty = item.quantity;
            let badge, opacity = '1';
            if (qty === 0) {
                badge = `<span class="badge badge-err">✕ 售罄</span>`;
                opacity = '0.5';
            } else if (qty <= 5) {
                badge = `<span class="badge badge-warn">⚠ 剩餘 ${qty} 瓶</span>`;
            } else {
                badge = `<span class="badge badge-ok">✓ 充足</span>`;
            }
            return `
                <div class="stock-item" style="opacity:${opacity}">
                    <div>
                        <div class="name">${item.drinkName}</div>
                        <div class="price">NT$ ${item.price}</div>
                    </div>
                    ${badge}
                </div>`;
        }).join('');
    } catch(e) {
        document.getElementById('consumer-inventory').innerHTML =
            '<div style="text-align:center;color:var(--danger);padding:20px">載入失敗</div>';
    }
}

/**
 * 進入系統（管理員模式）
 */
function enterSystem() {
    document.getElementById('login-screen').style.display = 'none';
    document.getElementById('admin-view').style.display = 'block';
    const user = getCurrentUser();
    document.getElementById('role-badge').textContent = user.user_name + ' · ' + user.user_type;

    // Staff 看不到銷售分析（前端畫面控制，後端仍會擋權限）
    document.getElementById('nav-sales').style.display =
        user.user_type === 'Manager' ? 'flex' : 'none';

    switchTab('dashboard');
}

/**
 * POST /auth/logout — 登出系統
 */
async function logout() {
    const user = getCurrentUser();
    if (!USE_MOCK && isAuthenticated()) {
        try {
            await apiFetch('POST', '/auth/logout', { refresh_token: refreshToken });
        } catch (e) {}
    }
    clearAuth();
    location.reload();
}
