/**
 * 補貨任務管理相關功能
 */

let currentAssignTaskId = null;
let currentRestockTaskId = null;
let teamsCache = null;

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
            // Manager 看全部；Staff 只看自己的
            const path = isManager
                ? '/refill-tasks'
                : `/staff/${user.user_id}/refill-tasks`;
            const res = await apiFetch('GET', path);
            if (!res || !res.ok) throw new Error(`API 返回錯誤: ${res?.status || 'unknown'}`);
            const data = await res.json();
            console.log('GET ' + path + ' 返回數據:', data);
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

    area.innerHTML = `
        <div class="page-header">
            <h2>補貨任務管理</h2>
            <p>依地理位置自動分組排程</p>
            ${isManager ? `<button class="btn btn-primary" onclick="openAssignModal()" style="margin-top:10px">+ 新增分派任務</button>` : ''}
        </div>
        <div class="card">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
                <div style="font-weight:700">目前補貨排程</div>
            </div>
            <table>
                <thead><tr>
                    <th>任務編號</th><th>地區</th><th>機台</th><th>班組</th><th>任務日期</th><th>類型</th><th>狀態</th><th>操作</th>
                </tr></thead>
                <tbody>
                ${tasks.map(t => `
                    <tr>
                        <td style="font-family:var(--mono);color:var(--muted)">#${t.refillTaskId}</td>
                        <td>${t.regionName || '#' + t.regionId}</td>
                        <td style="font-size:12px">${t.machineNames || '—'}</td>
                        <td style="color:var(--muted)">第 ${t.teamId} 組</td>
                        <td style="font-family:var(--mono);font-size:12px">${t.taskDate}</td>
                        <td style="font-size:12px">${t.taskType}</td>
                        <td>${taskStatusBadge(t.status)}</td>
                        <td>
                            ${isManager && t.status === 'Pending'
                                ? `<button class="btn btn-sm btn-primary" onclick="openAssignTaskModal(${t.refillTaskId})">分派</button>
                                <button class="btn btn-sm btn-ghost" onclick="openRestockModal(${t.refillTaskId})">更新</button>
                                <button class="btn btn-sm btn-danger" onclick="deleteTask(${t.refillTaskId})">刪除</button>`
                                : !isManager && t.status !== 'Completed'
                                    ? `<button class="btn btn-sm btn-primary" onclick="completeTask(${t.refillTaskId})">✓ 標記完成</button>`
                                    : isManager
                                        ? `<button class="btn btn-sm btn-danger" onclick="deleteTask(${t.refillTaskId})">刪除</button>`
                                        : '<span style="color:var(--muted);font-size:13px">—</span>'}
                        </td>
                    </tr>`).join('')}
                </tbody>
            </table>
        </div>`;
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
        const selectEl = document.getElementById('assign-team-select');
        selectEl.innerHTML = '<option value="">-- 選擇班組 --</option>' +
        teams.map(t => `<option value="${t.teamId}">${t.teamName || '#' + t.teamId}</option>`).join('');
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
        showToast('✅ 補貨任務已分派！');
        closeModal('modal-assign-task');
        switchTab('tasks');
    } catch (e) {
        showToast('❌ 分派失敗：' + e.message);
    }
}

/**
 * 打開補貨完成的 Modal
 */
function openRestockModal(taskId) {
    currentRestockTaskId = taskId;
    document.getElementById('modal-task-id').value = '#' + taskId;
    openModal('modal-restock');
}

/**
 * 提交補貨完成
 * PUT /refill-tasks/{taskId}/complete
 */
async function submitRestock() {
    const actualQty = parseInt(document.getElementById('modal-qty-cola').value);
    const now = new Date().toISOString().replace('T', ' ').substring(0, 19);

    if (USE_MOCK) {
        const task = MOCK.tasks.find(t => t.refilltask_id === currentRestockTaskId);
        if (task) task.status = 'Completed';
    } else {
        // PUT /refill-tasks/{taskId}/complete
        // Body: { completed_time }
        await apiFetch('PUT', `/refill-tasks/${currentRestockTaskId}/complete`, {
            completed_time: now,
        });
    }

    closeModal('modal-restock');
    showToast('✅ 補貨任務已完成！');
    switchTab('tasks');
}

/**
 * 打開新增分派任務的 Modal
 */
function openAssignModal() {
    // 新增分派任務的 modal
    (async () => {
        const teams = await loadTeams();
        const selectEl = document.getElementById('new-assign-team');
        selectEl.innerHTML = '<option value="">-- 選擇班組 --</option>' +
            teams.map(t => `<option value="${t.id}">${t.name || '#' + t.id}</option>`).join('');
    })();
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
    const teamId = parseInt(document.getElementById('new-assign-team').value);
    const taskType = document.getElementById('new-assign-type').value;

    if (!machineId || !regionId || !teamId || !taskType) {
        showToast('❌ 請填入所有必需欄位');
        return;
    }

    try {
        const taskDate = new Date().toISOString().split('T')[0];
        await apiFetch('POST', '/refill-tasks', {
            teamId: teamId,
            regionId: regionId,
            machineId: machineId,
            taskDate: taskDate,
            taskType: taskType,
            status: 'Pending'
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
