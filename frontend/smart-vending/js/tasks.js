/**
 * 補貨任務管理相關功能
 */

let currentAssignTaskId = null;
let currentRestockTaskId = null;
let teamsCache = null;

function todayLocalDateString() {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
}

function formatTaskDate(value) {
    if (!value) return '—';
    if (typeof value === 'string') return value.substring(0, 10);
    if (Array.isArray(value) && value.length >= 3) {
        return `${value[0]}-${String(value[1]).padStart(2, '0')}-${String(value[2]).padStart(2, '0')}`;
    }
    return String(value);
}

/**
 * 渲染補貨任務頁面
 * Manager → GET /refill-tasks (§10.1)
 * Staff   → GET /staff/{staff_id}/refill-tasks (§10.5)
 */
async function renderTasks(area) {
    area.innerHTML = loadingHTML('載入補貨任務...');
    const user = getCurrentUser();
    const isManager = user.user_type === 'Manager';

    let tasks = [];
    if (USE_MOCK) {
        tasks = MOCK.tasks;
    } else {
        try {
            const path = isManager
                ? '/refill-tasks'
                : `/staff/${user.user_id}/refill-tasks`;
            const res = await apiFetch('GET', path);
            if (!res || !res.ok) throw new Error(`API 返回錯誤: ${res?.status || 'unknown'}`);
            const data = await res.json();
            if (data.success && Array.isArray(data.data)) {
                tasks = data.data;
            } else if (Array.isArray(data)) {
                tasks = data;
            } else if (data.data && Array.isArray(data.data)) {
                tasks = data.data;
            }
        } catch (e) {
            console.error('載入補貨任務失敗:', e);
            area.innerHTML = `<div style="padding:60px;text-align:center;color:var(--danger)">
                ❌ 載入補貨任務失敗: ${e.message}
            </div>`;
            return;
        }
    }

    // Staff 走新版頁面
    if (!isManager) {
        renderStaffTasksPage(area, tasks);
        return;
    }

    // Manager 原本頁面
    const myTeamId = !isManager && tasks.length > 0 ? tasks[0].teamId : null;
    area.innerHTML = `
        <div class="page-header">
            <h2>補貨任務管理</h2>
            <p>依地理位置自動分組排程</p>
            ${isManager ? `<button class="btn btn-primary" onclick="openAssignModal()" style="margin-top:10px">+ 新增分派任務</button>` : ''}
        </div>
        <div class="card">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
                <div style="font-weight:700">目前補貨排程</div>
                <input type="text" id="task-search" placeholder="🔍 搜尋地區、機台、類型..."
                    oninput="filterTasks()"
                    style="padding:8px 12px;background:var(--surface2);border:1px solid var(--border);border-radius:8px;color:var(--text);font-family:inherit;font-size:13px;width:280px;" />
            </div>
            <table>
                <thead><tr>
                    <th>任務編號</th><th>地區</th><th>機台</th><th>班組</th><th>任務日期</th><th>類型</th><th>狀態</th><th>操作</th>
                </tr></thead>
                <tbody id="task-table-body">
                ${tasks.map(t => `
                    <tr onclick="openRefillDetails(${t.refillTaskId})" style="cursor:pointer">
                        <td style="font-family:var(--mono);color:var(--muted)">#${t.refillTaskId}</td>
                        <td>${t.regionName || '#' + t.regionId}</td>
                        <td style="font-size:12px">${t.machineNames || '—'}</td>
                        <td style="color:var(--muted)">第 ${t.teamId} 組</td>
                        <td style="font-family:var(--mono);font-size:12px">${formatTaskDate(t.taskDate)}</td>
                        <td style="font-size:12px">${t.taskType}</td>
                        <td>${taskStatusBadge(t.status)}</td>
                        <td>
                            ${t.status !== 'Completed'
                                ? `<button class="btn btn-sm btn-primary" onclick="event.stopPropagation();openAssignTaskModal(${t.refillTaskId})">改派</button>
                                <button class="btn btn-sm btn-danger" onclick="event.stopPropagation();deleteTask(${t.refillTaskId})">刪除</button>`
                                : `<button class="btn btn-sm btn-danger" onclick="event.stopPropagation();deleteTask(${t.refillTaskId})">刪除</button>`}
                        </td>
                    </tr>`).join('')}
                </tbody>
            </table>
        </div>`;
}

function renderStaffTasksPage(area, tasks) {
    const myTeamId = tasks.length > 0 ? tasks[0].teamId : null;
    const pending = tasks.filter(t => t.status !== 'Completed');
    const completed = tasks.filter(t => t.status === 'Completed');

    window._staffPending = pending;
    window._staffCompleted = completed;

    area.innerHTML = `
        <div class="page-header">
            <h2>補貨任務管理</h2>
            <p>依地理位置自動分組排程</p>
            ${myTeamId ? `<p style="color:var(--accent);font-weight:700;margin-top:6px">👤 您目前所屬班組：第 ${myTeamId} 組</p>` : ''}
        </div>
        <div style="display:flex;gap:12px;border-bottom:1px solid var(--border);margin-bottom:20px">
            <button id="staff-tab-pending" onclick="switchStaffTab('pending')"
                style="background:none;border:none;padding:8px 16px;cursor:pointer;color:var(--text);font-weight:500;border-bottom:2px solid var(--accent);font-family:inherit;">
                待處理任務（${pending.length}）
            </button>
            <button id="staff-tab-completed" onclick="switchStaffTab('completed')"
                style="background:none;border:none;padding:8px 16px;cursor:pointer;color:var(--muted);font-weight:500;font-family:inherit;">
                已完成任務（${completed.length}）
            </button>
        </div>
        <div id="staff-tasks-container"></div>`;

    switchStaffTab('pending');
}

function switchStaffTab(type) {
    const tasks = type === 'pending' ? (window._staffPending || []) : (window._staffCompleted || []);

    document.getElementById('staff-tab-pending').style.borderBottom = type === 'pending' ? '2px solid var(--accent)' : 'none';
    document.getElementById('staff-tab-pending').style.color = type === 'pending' ? 'var(--text)' : 'var(--muted)';
    document.getElementById('staff-tab-completed').style.borderBottom = type === 'completed' ? '2px solid var(--accent)' : 'none';
    document.getElementById('staff-tab-completed').style.color = type === 'completed' ? 'var(--text)' : 'var(--muted)';

    const container = document.getElementById('staff-tasks-container');
    if (!container) return;

    if (tasks.length === 0) {
        container.innerHTML = `
            <div class="card" style="text-align:center;padding:48px;">
                <i class="fas fa-check-circle" style="font-size:48px;color:var(--accent);margin-bottom:16px;display:block"></i>
                <p>${type === 'pending' ? '目前沒有待處理的補貨任務' : '尚無已完成任務紀錄'}</p>
            </div>`;
        return;
    }

    container.innerHTML = tasks.map(t => `
        <div class="card" style="margin-bottom:16px;cursor:pointer;" onclick="openRefillDetails(${t.refillTaskId})">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px;flex-wrap:wrap;gap:8px;">
                <div>
                    <h3 style="font-size:16px;font-weight:700;">📦 任務 #${t.refillTaskId}</h3>
                    <div style="font-size:12px;color:var(--muted);margin-top:4px;">
                        <i class="fas fa-map-marker-alt"></i> ${t.regionName || '#' + t.regionId}
                        ${t.machineNames ? `｜${t.machineNames}` : ''}
                    </div>
                </div>
                ${taskStatusBadge(t.status)}
            </div>
            <div style="background:var(--surface2);border-radius:12px;padding:12px;margin:12px 0;font-size:13px;">
                <span style="color:var(--muted)">任務類型：</span>${t.taskType}
            </div>
            <div style="display:flex;justify-content:space-between;align-items:center;font-size:12px;color:var(--muted);">
                <span><i class="far fa-calendar-alt"></i> ${formatTaskDate(t.taskDate)}</span>
                ${type === 'pending'
                ? `<button class="btn btn-primary" onclick="event.stopPropagation();openStaffCompleteModal(${t.refillTaskId})" style="padding:6px 16px;font-size:12px;">
                    <i class="fas fa-check"></i> 回報完成
                </button>`
                : '<span style="color:var(--accent)">✓ 已完成</span>'}
            </div>
        </div>
    `).join('');
}

/**
 * 載入班組列表（快取結果）
 */
async function loadTeams() {
    if (teamsCache) return teamsCache;
    try {
        const res = await apiFetch('GET', '/teams');
        const data = await res.json();
        teamsCache = Array.isArray(data) ? data : (data.data || []);
        return teamsCache;
    } catch (e) {
        console.error('載入班組失敗:', e);
        return [];
    }
}

/**
 * 打開分派特定任務的 Modal
 */
function openAssignTaskModal(taskId) {
    currentAssignTaskId = taskId;
    document.getElementById('assign-task-id').textContent = '#' + taskId;
    (async () => {
        const teams = await loadTeams();
        const taskRes = await apiFetch('GET', `/refill-tasks/${taskId}`);
        const task = (await taskRes.json()).data || {};
        const allowedTeams = teams.filter(t => String(t.regionId) === String(task.regionId));
        const selectEl = document.getElementById('assign-team-select');
        selectEl.innerHTML = '<option value="">-- 選擇班組 --</option>' +
        allowedTeams.map(t => `<option value="${t.teamId}">${t.teamName || '#' + t.teamId}（${t.regionName || '同區域'}）</option>`).join('');
    })();
    openModal('modal-assign-task');
}

/**
 * 提交分派任務
 * PUT /refill-tasks/{taskId}/assign
 */
async function submitAssignTask() {
    const teamId = parseInt(document.getElementById('assign-team-select').value);
    if (!teamId) {
        showToast('❌ 請選擇班組');
        return;
    }
    
    try {
        await apiFetch('PUT', `/refill-tasks/${currentAssignTaskId}/assign`, { team_id: teamId });
        showToast('✅ 補貨任務已改派！');
        closeModal('modal-assign-task');
        switchTab('tasks');
    } catch (e) {
        showToast('❌ 改派失敗：' + e.message);
    }
}

/**
 * 打開新增分派任務的 Modal
 */
async function openAssignModal() {
    const [teams, machinesRes] = await Promise.all([
        loadTeams(),
        apiFetch('GET', '/machines')
    ]);
    const machinesData = await machinesRes.json();
    const machines = machinesData.data || [];

    // 機台選單
    const machineSelect = document.getElementById('new-assign-machine');
    machineSelect.innerHTML = '<option value="">-- 選擇機台 --</option>' +
        machines.map(m => `<option value="${m.machine_id}" data-region="${m.region_id}">${m.machine_name}（${m.region_name}）</option>`).join('');

    window._assignTeams = teams;
    updateAssignTeamOptions();

    openModal('modal-new-assign');
}

/**
 * 提交新增分派任務
 * POST /refill-tasks
 */
async function submitNewAssign() {
    const machineSelect = document.getElementById('new-assign-machine');
    const machineId = parseInt(machineSelect.value);
    const selectedOption = machineSelect.options[machineSelect.selectedIndex];
    const regionId = parseInt(selectedOption?.dataset.region);
    const teamSelect = document.getElementById('new-assign-team');
    const teamId = parseInt(teamSelect.value);
    const teamRegionId = parseInt(teamSelect.options[teamSelect.selectedIndex]?.dataset.region);
    const taskType = document.getElementById('new-assign-type').value;

    if (!machineId || !regionId || !teamId || !taskType) {
        showToast('❌ 請填入所有必需欄位');
        return;
    }
    if (teamRegionId !== regionId) {
        showToast('❌ 只能分派給負責該地區的班組');
        return;
    }

    try {
        const taskDate = todayLocalDateString();
        await apiFetch('POST', '/refill-tasks', {
            teamId: teamId,
            regionId: regionId,
            machineId: machineId,
            taskDate: taskDate,
            taskType: taskType,
            status: 'Assigned'
        });
        showToast('✅ 補貨任務已新增！');
        closeModal('modal-new-assign');
        switchTab('tasks');
    } catch (e) {
        showToast('❌ 新增失敗：' + e.message);
    }
}

//刪除已完成任務
async function deleteTask(taskId) {
    if (!confirm(`確定要刪除任務 #${taskId}？`)) return;
    try {
        const res = await apiFetch('DELETE', `/refill-tasks/${taskId}`);
        if (!res.ok) throw new Error('API 返回錯誤: ' + res.status);
        showToast('✅ 任務已刪除！');
        switchTab('tasks');
    } catch (e) {
        showToast('❌ 刪除失敗：' + e.message);
    }
}

async function openStaffCompleteModal(taskId) {
    document.getElementById('staff-complete-task-id').textContent = '#' + taskId;
    document.getElementById('staff-complete-note').value = '';
    window._completeTaskId = taskId;

    // 載入該機台的庫存項目
    const task = (window._staffPending || []).find(t => t.refillTaskId === taskId);
    const container = document.getElementById('staff-complete-items');

    if (task && task.machineNames) {
        // 打 API 取得機台庫存
        try {
            const res = await apiFetch('GET', `/staff/${getCurrentUser().user_id}/machines`);
            const data = await res.json();
            const machines = data.data || [];
            window._staffMachines = machines;
            const machine = machines.find(m => m.machine_name === task.machineNames);
            const inventory = machine?.inventory || [];

            if (inventory.length > 0) {
                container.innerHTML = inventory.map(i => `
                    <div style="display:flex;align-items:center;justify-content:space-between;background:var(--surface2);padding:10px 14px;border-radius:10px;">
                        <span style="font-size:13px;font-weight:600;">${i.drink_name}</span>
                        <div style="display:flex;align-items:center;gap:8px;">
                            <span style="font-size:12px;color:var(--muted)">補貨前</span>
                            <input type="number" min="0" value="${i.quantity ?? 0}" id="before-qty-${i.drink_id}"
                                style="width:64px;padding:6px 8px;background:var(--bg);border:1px solid var(--border);border-radius:8px;color:var(--text);font-family:inherit;font-size:13px;text-align:center" />
                            <span style="font-size:12px;color:var(--muted)">補貨量</span>
                            <input type="number" min="0" value="0" id="complete-qty-${i.drink_id}"
                                style="width:64px;padding:6px 8px;background:var(--bg);border:1px solid var(--border);border-radius:8px;color:var(--text);font-family:inherit;font-size:13px;text-align:center" />
                        </div>
                    </div>
                `).join('');
            } else {
                container.innerHTML = '<div style="color:var(--muted);font-size:13px;">無庫存資料</div>';
            }
        } catch (e) {
            container.innerHTML = '<div style="color:var(--muted);font-size:13px;">載入庫存失敗</div>';
        }
    } else {
        container.innerHTML = '<div style="color:var(--muted);font-size:13px;">無庫存資料</div>';
    }

    openModal('modal-staff-complete');
}

async function submitStaffComplete() {
    const taskId = window._completeTaskId;
    const task = (window._staffPending || []).find(t => t.refillTaskId === taskId);

    // 收集補貨數量
    const items = [];
    const inputs = document.querySelectorAll('[id^="complete-qty-"]');
    inputs.forEach(input => {
        const drinkId = parseInt(input.id.replace('complete-qty-', ''));
        const qty = parseInt(input.value) || 0;
        const beforeQty = parseInt(document.getElementById(`before-qty-${drinkId}`)?.value);
        if (drinkId) {
            items.push({ drink_id: drinkId, before_quantity: isNaN(beforeQty) ? null : beforeQty, actual_quantity: qty });
        }
    });

        try {
            const res = await apiFetchWithRetry('PUT', `/refill-tasks/${taskId}/complete`, {
                machine_id: task?.machineId || null,
                items: items
            });
            if (!res.ok) throw new Error('API 返回錯誤: ' + res.status);
            showToast('✅ 任務已回報完成！');
            closeModal('modal-staff-complete');
            switchTab('tasks');
        } catch (e) {
            showToast('❌ 回報失敗，請稍後再試：' + e.message);
        }
    }


//新增任務裡找機台
function filterMachineOptions() {
    const keyword = document.getElementById('machine-search-input')?.value.toLowerCase() || '';
    const options = document.querySelectorAll('#new-assign-machine option');
    options.forEach(opt => {
        opt.style.display = opt.textContent.toLowerCase().includes(keyword) ? '' : 'none';
    });
    updateAssignTeamOptions();
}

function updateAssignTeamOptions() {
    const machineSelect = document.getElementById('new-assign-machine');
    const teamSelect = document.getElementById('new-assign-team');
    if (!machineSelect || !teamSelect) return;

    const selectedOption = machineSelect.options[machineSelect.selectedIndex];
    const regionId = selectedOption?.dataset.region;
    const teams = window._assignTeams || [];

    if (!regionId) {
        teamSelect.innerHTML = '<option value="">-- 請先選擇機台 --</option>';
        return;
    }

    const allowedTeams = teams.filter(t => String(t.regionId) === String(regionId));
    teamSelect.innerHTML = allowedTeams.length
        ? '<option value="">-- 選擇該地區班組 --</option>' + allowedTeams.map(t =>
            `<option value="${t.teamId}" data-region="${t.regionId || ''}">${t.teamName || '第 ' + t.teamId + ' 組'}${t.regionName ? '（' + t.regionName + '）' : ''}</option>`
        ).join('')
        : '<option value="">-- 此地區沒有可分派班組 --</option>';
}


//補貨任務搜尋
function filterTasks() {
    const keyword = document.getElementById('task-search')?.value.toLowerCase() || '';
    const rows = document.querySelectorAll('#task-table-body tr');
    rows.forEach(row => {
        row.style.display = row.textContent.toLowerCase().includes(keyword) ? '' : 'none';
    });
}



async function openRefillDetails(taskId) {
    const container = document.getElementById('refill-details-container');
    container.innerHTML = '<div style="color:var(--muted);font-size:13px">載入中...</div>';
    openModal('modal-refill-details');
    try {
        const res = await apiFetch('GET', `/refill-tasks/${taskId}/details`);
        const data = await res.json();
        const rows = data.data || [];
        container.innerHTML = rows.length === 0
            ? '<div style="color:var(--muted);font-size:13px">尚無 refilldetail。補貨任務完成回報後才會產生明細；若這是舊任務，請確認該任務是否真的有 RefillDetail 資料。</div>'
            : `<table><thead><tr><th>飲料</th><th>補貨前/計畫</th><th>補貨量</th><th>時間</th></tr></thead><tbody>${rows.map(r => `
                <tr><td>${r.drink_name || r.drinkName}</td><td>${r.planned_quantity ?? '—'}</td><td>${r.actual_quantity ?? '—'}</td><td style="font-size:12px;color:var(--muted)">${r.refill_time || ''}</td></tr>`).join('')}</tbody></table>`;
    } catch(e) {
        container.innerHTML = `<div style="color:var(--danger);font-size:13px">載入失敗：${e.message}</div>`;
    }
}
