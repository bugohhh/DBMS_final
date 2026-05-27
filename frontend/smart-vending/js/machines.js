/**
 * 機台管理相關功能
 */

/**
 * 渲染機台管理頁面
 * 使用：GET /machines、POST /machines、DELETE /machines/{id}
 */
async function renderMachines(area) {
    area.innerHTML = loadingHTML('載入機台資料...');
    const user = getCurrentUser();
    const isManager = user.user_type === 'Manager';

    let machines = [];
    if (USE_MOCK) {
        machines = MOCK.machines;
    } else {
        try {
            // GET /machines
            const res = await apiFetch('GET', '/machines');
            if (!res || !res.ok) {
                throw new Error(`API 返回錯誤: ${res?.status || 'unknown'}`);
            }
            const data = await res.json();
            console.log('GET /machines 返回數據:', data);
            
            if (data.success && data.data) {
                machines = data.data;
            } else if (data.data) {
                machines = data.data;
            } else {
                console.warn('無法解析機台數據', data);
                machines = [];
            }
        } catch (e) {
            console.error('載入機台失敗:', e);
            area.innerHTML = `<div style="padding:60px;text-align:center;color:var(--danger)">
                ❌ 載入機台失敗: ${e.message}
            </div>`;
            return;
        }
    }

    area.innerHTML = `
        <div class="page-header" style="display:flex;justify-content:space-between;align-items:flex-start">
            <div><h2>機台管理</h2><p>管理所有販賣機台資訊</p></div>
            ${isManager ? `<button class="btn btn-primary" onclick="openAddMachineModal()">+ 新增機台</button>` : ''}
        </div>
        <div class="card">
            ${machines.length === 0 ? `
                <div style="padding:40px;text-align:center;color:var(--muted)">
                    📭 目前沒有機台。${isManager ? '點擊「新增機台」建立第一台。' : ''}
                </div>
            ` : `
                <table>
                    <thead><tr>
                        <th>機台 ID</th><th>名稱</th><th>地區</th><th>庫存明細</th><th>狀態</th>
                        ${isManager ? '<th>操作</th>' : ''}
                    </tr></thead>
                    <tbody>
                    ${machines.map(m => `
                        <tr>
                            <td style="font-family:var(--mono);color:var(--muted)">#${m.machine_id}</td>
                            <td style="font-weight:700">${m.machine_name}</td>
                            <td style="color:var(--muted)">${m.region_name}</td>
                            <td style="font-size:12px;color:var(--muted)">
                                ${(m.inventory||[]).map(i => `${i.drink_name}: ${i.quantity}`).join(' ／ ')}
                            </td>
                            <td>${statusBadge(m.status)}</td>
                            ${isManager ? `<td>
                                <button class="btn btn-ghost" style="color:var(--danger);border-color:var(--danger)" onclick="deleteMachine(${m.machine_id})">刪除</button>
                            </td>` : ''}
                        </tr>`).join('')}
                    </tbody>
                </table>
            `}
        </div>`;
}

/**
 * 刪除機台
 * DELETE /machines/{machineId}
 */
async function deleteMachine(machineId) {
    if (!confirm(`確定要刪除機台 #${machineId}？`)) return;
    
    if (USE_MOCK) {
        const idx = MOCK.machines.findIndex(m => m.machine_id === machineId);
        if (idx !== -1) MOCK.machines.splice(idx, 1);
    } else {
        try {
            await apiFetch('DELETE', `/machines/${machineId}`);
        } catch (e) {
            showToast('❌ 刪除失敗：無法連線伺服器');
            return;
        }
    }
    
    showToast(`✅ 機台 #${machineId} 已刪除`);
    switchTab('machines'); // 重新載入列表
}

/**
 * 打開新增機台 Modal
 */
async function openAddMachineModal() {
    openModal('modal-add-machine');
    const container = document.getElementById('new-vm-inventory');
    container.innerHTML = '<div style="color:var(--muted);font-size:13px">載入中...</div>';

    try {
        const res = await fetch(`${BASE_URL}/public/drinks`);
        const data = await res.json();
        const drinks = data.data || [];

        container.innerHTML = drinks.map(d => `
            <div style="display:flex;align-items:center;gap:12px;background:var(--surface2);padding:12px;border-radius:10px;border:1px solid var(--border)">
                <div style="flex:1;font-size:13px;font-weight:700">${d.drinkName}</div>
                <div style="display:flex;align-items:center;gap:8px">
                    <span style="font-size:12px;color:var(--muted)">數量</span>
                    <input type="number" min="0" max="30" value="0"
                        id="inv-qty-${d.drinkId}"
                        style="width:60px;padding:6px 8px;background:var(--bg);border:1px solid var(--border);border-radius:8px;color:var(--text);font-family:inherit;font-size:13px;text-align:center" />
                    <span style="font-size:12px;color:var(--muted)">價格 NT$</span>
                    <input type="number" min="0" value="30"
                        id="inv-price-${d.drinkId}"
                        style="width:64px;padding:6px 8px;background:var(--bg);border:1px solid var(--border);border-radius:8px;color:var(--text);font-family:inherit;font-size:13px;text-align:center" />
                </div>
            </div>
        `).join('');

        container.dataset.drinks = JSON.stringify(drinks.map(d => d.drinkId));
    } catch(e) {
        container.innerHTML = '<div style="color:var(--danger);font-size:13px">載入飲料清單失敗</div>';
    }
}

/**
 * 提交新增機台
 * POST /machines
 * POST /inventory (for each drink)
 */
async function submitAddMachine() {
    const machine_name = document.getElementById('new-vm-name').value.trim();
    const region_name = document.getElementById('new-vm-area').value.trim();
    const location = document.getElementById('new-vm-location').value.trim();

    if (!machine_name || !region_name) {
        showToast('❌ 請填寫機台名稱和地區');
        return;
    }

    // 收集庫存資料
    const container = document.getElementById('new-vm-inventory');
    const drinkIds = JSON.parse(container.dataset.drinks || '[]');
    const inventoryItems = drinkIds.map(drinkId => ({
        drinkId,
        quantity: parseInt(document.getElementById(`inv-qty-${drinkId}`)?.value || 0),
        price: parseFloat(document.getElementById(`inv-price-${drinkId}`)?.value || 0),
    }));

    if (USE_MOCK) {
        const newId = MOCK.machines.length > 0 ? Math.max(...MOCK.machines.map(m => m.machine_id)) + 1 : 1;
        MOCK.machines.push({ machine_id: newId, machine_name, region_name, status: "Normal", inventory: [] });
    } else {
        try {
            // 1. 新增機台
            const res = await apiFetch('POST', '/machines', { machine_name, region_name, location });
            const data = await res.json();
            console.log('新增機台回傳:', JSON.stringify(data));
            const newMachineId = data.data?.machineId || data.data?.machine_id;
            console.log('newMachineId:', newMachineId);

            // 2. 新增庫存
            if (newMachineId) {
                for (const item of inventoryItems) {
                    await apiFetch('POST', '/inventory', {
                        machineId: newMachineId,
                        drinkId: item.drinkId,
                        quantity: item.quantity,
                        price: item.price,
                        lowStockThreshold: 5,
                        capacity: 30,
                    });
                }
            }
        } catch(e) {
            showToast('❌ 新增失敗');
            return;
        }
    }

    closeModal('modal-add-machine');
    showToast(`✅ 機台 ${machine_name} 已新增！`);
    document.getElementById('new-vm-name').value = '';
    document.getElementById('new-vm-location').value = '';
    document.getElementById('new-vm-area').value = '';
    switchTab('machines');
}
