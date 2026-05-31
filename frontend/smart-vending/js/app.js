/**
 * 應用主邏輯
 * 管理頁面切換、工具函數、模態框
 */

/**
 * 切換頁面
 */
let currentTab = 'dashboard';

function switchTab(tab) {
    currentTab = tab;
    document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
    const el = document.getElementById('nav-' + tab);
    if (el) el.classList.add('active');
    const area = document.getElementById('main-content');

    if (tab === 'dashboard') renderDashboard(area);
    else if (tab === 'machines') renderMachines(area);
    else if (tab === 'tasks') renderTasks(area);
    else if (tab === 'sales') renderSales(area);
    else if (tab === 'teams') renderTeams(area);
    else if (tab === 'regions') renderRegions(area);
    else if (tab === 'users') renderUsers(area);
    else if (tab === 'drinks') renderDrinks(area);
}

function refreshCurrentTab() {
    switchTab(currentTab || 'dashboard');
    showToast('🔄 數據已刷新');
}

/**
 * 渲染營運概覽頁面
 * 使用：GET /machines + GET /refill-tasks
 */
async function renderDashboard(area) {
    area.innerHTML = loadingHTML('載入概覽中...');

    let machines, tasks;
    if (USE_MOCK) {
        machines = MOCK.machines;
        tasks    = MOCK.tasks;
    } else {
        // GET /machines → { success, data: [ {machine_id, machine_name, region_name, status, ...} ] }
        const [mRes, tRes] = await Promise.all([
            apiFetch('GET', '/machines'),
            apiFetch('GET', '/refill-tasks'),
        ]);
        machines = (await mRes.json()).data;
        tasks    = (await tRes.json()).data;
    }

    const user = getCurrentUser();
    const pending  = tasks.filter(t => t.status === 'Pending').length;
    const critical = machines.filter(m => m.status === 'Critical').length;
    const low      = machines.filter(m => m.status === 'Low').length;

    area.innerHTML = `
        <div class="page-header">
            <h2>營運概覽</h2>
            <p>即時監控所有販賣機狀態｜登入身份：${user.user_name}</p>
        </div>
        <div class="grid-3" style="margin-bottom:20px">
            <div class="card stat-card">
                <div class="label">待補貨任務</div>
                <div class="value accent-blue">${pending}</div>
                <div class="sub">單待處理</div>
            </div>
            <div class="card stat-card">
                <div class="label">嚴重缺貨機台</div>
                <div class="value accent-red">${critical}</div>
                <div class="sub">台需立即補貨</div>
            </div>
            <div class="card stat-card">
                <div class="label">庫存偏低機台</div>
                <div class="value accent-yellow">${low}</div>
                <div class="sub">台需關注</div>
            </div>
        </div>
        <div class="card">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
                <div style="font-weight:700">機台狀態總覽</div>
                <button class="btn btn-ghost" onclick="switchTab('machines')">查看全部 →</button>
            </div>
            <table>
                <thead><tr>
                    <th>機台</th><th>地區</th><th>庫存明細</th><th>狀態</th>
                </tr></thead>
                <tbody>
                ${machines.map(m => `
                    <tr>
                        <td style="font-weight:700">${escapeHtml(m.machine_name)}</td>
                            <span style="font-size:11px;color:var(--muted);font-family:var(--mono)">#${m.machine_id}</span>
                        </td>
                        <td style="color:var(--muted)">${m.region_name}</td>
                        <td style="font-size:12px;color:var(--muted)">
                            ${(m.inventory||[]).map(i => `${i.drink_name}: ${i.quantity}`).join(' ／ ')}
                        </td>
                        <td>${statusBadge(m.status)}</td>
                    </tr>`).join('')}
                </tbody>
            </table>
        </div>`;
}

/**
 * 渲染帳號管理頁面
 * Manager 可搜尋 staff、查看 team，並建立新 staff 帳號。
 */
async function renderUsers(area, keyword = '') {
    const user = getCurrentUser();
    if (!user || user.user_type !== 'Manager') {
        area.innerHTML = '<div class="card" style="padding:24px;color:var(--danger)">權限不足，只有管理員可以管理帳號。</div>';
        return;
    }

    area.innerHTML = loadingHTML('載入使用者名冊中...');

    try {
        const path = keyword ? `/auth/users?keyword=${encodeURIComponent(keyword)}` : '/auth/users';
        const res = await apiFetch('GET', path);
        const data = await res.json();
        if (!data.success) {
            area.innerHTML = `<div class="card" style="padding:24px;color:var(--danger)">${data.message || '載入使用者失敗'}</div>`;
            return;
        }

        const users = data.data || [];
        area.innerHTML = `
            <div class="page-header" style="display:flex;justify-content:space-between;align-items:flex-start">
                <div><h2>帳號管理</h2><p>可搜尋使用者、查看 staff 所屬 team，並新增 staff 帳號。</p></div>
                <button class="btn btn-primary" onclick="openCreateStaffModal()">+ 創建 Staff</button>
            </div>
            <div class="card">
                <div style="display:flex;gap:8px;margin-bottom:16px;">
                    <input type="text" id="user-search" placeholder="🔍 搜尋 ID、姓名、帳號、Team 或地區..." value="${keyword.replace(/"/g, '&quot;')}"
                        onkeydown="if(event.key==='Enter')renderUsers(document.getElementById('main-content'), this.value)"
                        style="flex:1;padding:10px 14px;background:var(--surface2);border:1px solid var(--border);border-radius:10px;color:var(--text);font-family:inherit;font-size:14px;" />
                    <button class="btn btn-ghost" onclick="renderUsers(document.getElementById('main-content'), document.getElementById('user-search').value)">查詢</button>
                </div>
                <table>
                    <thead><tr><th>User ID</th><th>姓名</th><th>帳號</th><th>角色</th><th>Team</th><th>地區</th><th>操作</th></tr></thead>
                    <tbody>
                        ${users.map(u => `
                            <tr>
                                <td style="font-family:var(--mono)">#${u.user_id}</td>
                                <td style="font-weight:700">${u.user_name}</td>
                                <td style="color:var(--muted)">${u.account || '—'}</td>
                                <td>${u.user_type === 'Manager' ? '<span class="badge badge-ok">Manager</span>' : '<span class="badge badge-warn">Staff</span>'}</td>
                                <td>${u.team_name || (u.team_id ? '第 ' + u.team_id + ' 組' : '—')}</td>
                                <td>${u.region_name || '—'}</td>
                                <td>${u.user_type === 'Staff'
                                    ? `<button class="btn btn-primary btn-sm" onclick="resetStaffPassword(${u.user_id}, '${String(u.user_name).replace(/'/g, "\\'")}')">重設密碼</button>`
                                    : '<span style="color:var(--muted);font-size:12px">—</span>'}</td>
                            </tr>`).join('')}
                    </tbody>
                </table>
            </div>`;
    } catch (e) {
        area.innerHTML = '<div class="card" style="padding:24px;color:var(--danger)">無法連線至伺服器，請確認後端是否啟動。</div>';
    }
}

/**
 * 6. 管理者直接幫指定員工重設密碼 (PUT /api/auth/users/{user_id}/password)
 */
async function resetStaffPassword(userId, userName) {
    // 1. 用瀏覽器內建的 prompt 彈窗，直接讓管理員輸入新密碼
    const newPassword = prompt(`🔑 請輸入要幫 [${userName}] 設定的新密碼：`);
    
    // 如果點取消或是沒輸入，就直接取消動作
    if (newPassword === null) return; 
    if (newPassword.trim() === '') {
        showToast('❌ 密碼不可為空！');
        return;
    }

    // 2. 呼叫真實 API 模式送往後端
    try {
        // 對應後端控制器的 @PutMapping("/users/{userId}/password")
        const res = await apiFetch('PUT', `/auth/users/${userId}/password`, {
            new_password: newPassword.trim()
        });
        
        const data = await res.json();
        
        if (data.success) {
            showToast(`✅ 已成功將 ${userName} 的密碼重設！`);
            // 重新刷新當前帳號管理頁面
            renderUsers(document.getElementById('main-content'), document.getElementById('user-search')?.value || '');
        } else {
            showToast('❌ 重設失敗：' + data.message);
        }
    } catch (e) {
        console.error(e);
        showToast('❌ 無法連線至伺服器重設密碼');
    }
}

async function openCreateStaffModal() {
    const select = document.getElementById('create-staff-team');
    select.innerHTML = '<option value="">不分配班組</option>';
    try {
        const res = await apiFetch('GET', '/teams');
        const data = await res.json();
        const teams = Array.isArray(data) ? data : (data.data || []);
        select.innerHTML += teams.map(t => `<option value="${t.teamId}">${t.teamName || '第 ' + t.teamId + ' 組'}${t.regionName ? '（' + t.regionName + '）' : ''}</option>`).join('');
    } catch(e) {}
    openModal('modal-create-staff');
}

async function submitCreateStaff() {
    const user_name = document.getElementById('create-staff-name').value.trim();
    const account = document.getElementById('create-staff-account').value.trim();
    const password = document.getElementById('create-staff-password').value.trim();
    const team_id = document.getElementById('create-staff-team').value;
    if (!user_name || !account || !password) { showToast('❌ 請填寫姓名、帳號與初始密碼'); return; }
    try {
        const res = await apiFetch('POST', '/auth/register', { user_name, account, password, team_id: team_id ? Number(team_id) : null });
        const data = await res.json();
        if (!data.success) throw new Error(data.message || '建立失敗');
        showToast('✅ Staff 已建立');
        closeModal('modal-create-staff');
        ['create-staff-name','create-staff-account','create-staff-password'].forEach(id => document.getElementById(id).value = '');
        document.getElementById('create-staff-team').value = '';
        switchTab('users');
    } catch(e) { showToast('❌ ' + e.message); }
}

/**
 * 工具函數：載入狀態提示
 */
function loadingHTML(msg) {
    return `<div style="padding:60px;text-align:center;color:var(--muted)">${msg}</div>`;
}

/**
 * 工具函數：狀態徽章（機台狀態）
 */
function statusBadge(s) {
    // 對應後端回傳的 status 字串
    const map = {
        Normal:   ['badge-ok',   '✓ 正常'],
        Low:      ['badge-warn', '⚠ 偏低'],
        Critical: ['badge-err',  '✕ 缺貨'],
    };
    const [cls, label] = map[s] || ['badge-ok', s];
    return `<span class="badge ${cls}">${label}</span>`;
}

/**
 * 工具函數：狀態徽章（任務狀態）
 */
function taskStatusBadge(s) {
    const map = {
        Pending:     ['badge-warn', '⏳ 待處理'],
        'In Progress':['badge-ok',  '🔄 進行中'],
        Completed:   ['badge-ok',   '✓ 已完成'],
        Assigned:    ['badge-ok',   '✓ 已分派'],
    };
    const [cls, label] = map[s] || ['badge-warn', s];
    return `<span class="badge ${cls}">${label}</span>`;
}

/**
 * 模態框：打開
 */
function openModal(id) {
    document.getElementById(id).classList.add('open');
}

/**
 * 模態框：關閉
 */
function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

/**
 * 吐司提示
 */
let toastTimer;
function showToast(msg) {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => t.classList.remove('show'), 2500);
}

/**
 * 初始化事件監聽
 */
document.addEventListener('DOMContentLoaded', () => {
    // 點擊 Modal 背景關閉
    document.querySelectorAll('.modal-backdrop').forEach(b => {
        b.addEventListener('click', e => { 
            if (e.target === b) b.classList.remove('open'); 
        });
    });
});

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
