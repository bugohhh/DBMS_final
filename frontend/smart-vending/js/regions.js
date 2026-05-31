/**
 * 地區管理相關功能
 */

async function renderRegions(area) {
    area.innerHTML = loadingHTML('載入地區資料...');
    try {
        const res = await apiFetch('GET', '/regions');
        const data = await res.json();
        const regions = Array.isArray(data) ? data : (data.data || []);
        window._regionRows = regions;

        const withManager = regions.filter(r => r.managerId).length;

        area.innerHTML = `
            <div class="page-header" style="display:flex;justify-content:space-between;align-items:flex-start;gap:16px;">
                <div>
                    <h2>地區管理</h2>
                    <p>維護機台可選地區。刪除地區時，若該地區仍有機台，後端會拒絕刪除。</p>
                </div>
                <button class="btn btn-primary" onclick="openAddRegionModal()">+ 新增地區</button>
            </div>

            <div class="grid-3" style="margin-bottom:20px;">
                <div class="card stat-card">
                    <div class="label">地區總數</div>
                    <div class="value accent-blue">${regions.length}</div>
                    <div class="sub">目前已建立地區</div>
                </div>
                <div class="card stat-card">
                    <div class="label">已指派 Manager</div>
                    <div class="value accent-green">${withManager}</div>
                    <div class="sub">有負責人的地區</div>
                </div>
                <div class="card stat-card">
                    <div class="label">刪除限制</div>
                    <div class="value accent-yellow" style="font-size:26px;">有機台不可刪</div>
                    <div class="sub">避免機台失去地區關聯</div>
                </div>
            </div>

            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;gap:12px;">
                <input type="text" id="region-search" placeholder="🔍 搜尋地區、Manager 或描述..." oninput="filterRegions()"
                    style="flex:1;max-width:420px;padding:10px 14px;background:var(--surface2);border:1px solid var(--border);border-radius:10px;color:var(--text);font-family:inherit;font-size:14px;" />
            </div>

            ${regions.length === 0
                ? '<div class="card" style="text-align:center;padding:48px;color:var(--muted);">尚無地區資料，請先新增地區。</div>'
                : `<div class="region-grid">
                    ${regions.map(r => `
                        <div class="card region-card region-row">
                            <div class="region-card-top">
                                <div>
                                    <div class="region-title">${escapeHtml(r.name || '未命名地區')}</div>
                                    <div class="region-meta">地區 ID #${r.id}</div>
                                </div>
                                <span class="badge ${r.managerId ? 'badge-ok' : 'badge-warn'}">${r.managerId ? '已指派' : '未指派'}</span>
                            </div>
                            <div style="background:var(--surface2);border:1px solid var(--border);border-radius:12px;padding:12px;">
                                <div style="font-size:11px;font-weight:700;color:var(--muted);margin-bottom:6px;">負責 Manager</div>
                                <div style="font-weight:700;">${escapeHtml(r.managerName || '—')}</div>
                                <div style="color:var(--muted);font-family:var(--mono);font-size:12px;margin-top:2px;">${r.managerId ? 'User #' + r.managerId : '尚未設定'}</div>
                            </div>
                            <div class="region-meta">${escapeHtml(r.description || '無描述')}</div>
                            <div class="region-actions">
                                <button class="btn btn-ghost btn-sm" onclick="openEditRegionModalById(${r.id})">編輯</button>
                                <button class="btn btn-ghost btn-sm" style="color:var(--danger);border-color:var(--danger);" onclick="deleteRegion(${r.id})">刪除</button>
                            </div>
                        </div>
                    `).join('')}
                </div>`}
        `;
    } catch (e) {
        area.innerHTML = `<div style="padding:60px;text-align:center;color:var(--danger)">❌ 載入失敗: ${e.message}</div>`;
    }
}

async function loadManagerOptions(selectId, selectedId = '') {
    const select = document.getElementById(selectId);
    select.innerHTML = '<option value="">載入 Manager 帳號中...</option>';
    try {
        const res = await apiFetch('GET', '/auth/users');
        const data = await res.json();
        const managers = (data.data || []).filter(u => u.user_type === 'Manager');
        select.innerHTML = '<option value="">-- 選擇有效 Manager 帳號 --</option>' +
            managers.map(u => `<option value="${u.user_id}" ${String(u.user_id) === String(selectedId) ? 'selected' : ''}>#${u.user_id} ${escapeHtml(u.user_name || '')}${u.account ? '（' + escapeHtml(u.account) + '）' : ''}</option>`).join('');
        if (managers.length === 0) {
            select.innerHTML = '<option value="">沒有可用的 Manager 帳號</option>';
        }
    } catch (e) {
        select.innerHTML = '<option value="">Manager 帳號載入失敗</option>';
    }
}

async function openAddRegionModal() {
    document.getElementById('new-region-name').value = '';
    document.getElementById('new-region-description').value = '';
    openModal('modal-add-region');
    await loadManagerOptions('new-region-manager-id');
}

async function submitAddRegion() {
    const name = document.getElementById('new-region-name').value.trim();
    const managerIdRaw = document.getElementById('new-region-manager-id').value.trim();
    const description = document.getElementById('new-region-description').value.trim();
    if (!name) { showToast('❌ 請填寫地區名稱'); return; }
    if (!managerIdRaw) { showToast('❌ 請選擇有效 Manager 帳號'); return; }
    try {
        const res = await apiFetch('POST', '/regions', { regionName: name, managerId: Number(managerIdRaw), description });
        if (!res.ok) {
            const err = await res.json().catch(() => null);
            throw new Error(err?.message || err?.error || ('API 返回錯誤: ' + res.status));
        }
        showToast('✅ 地區已新增！');
        closeModal('modal-add-region');
        switchTab('regions');
    } catch (e) {
        showToast('❌ 新增失敗：' + e.message);
    }
}

function openEditRegionModalById(regionId) {
    const region = (window._regionRows || []).find(r => String(r.id) === String(regionId));
    if (!region) { showToast('❌ 找不到地區資料'); return; }
    openEditRegionModal(region);
}

async function openEditRegionModal(region) {
    window._editRegionId = region.id;
    document.getElementById('edit-region-id').textContent = '#' + region.id;
    document.getElementById('edit-region-name').value = region.name || '';
    document.getElementById('edit-region-description').value = region.description || '';
    openModal('modal-edit-region');
    await loadManagerOptions('edit-region-manager-id', region.managerId || '');
}

async function submitEditRegion() {
    const id = window._editRegionId;
    const name = document.getElementById('edit-region-name').value.trim();
    const managerIdRaw = document.getElementById('edit-region-manager-id').value.trim();
    const description = document.getElementById('edit-region-description').value.trim();
    if (!id) { showToast('❌ 找不到地區 ID'); return; }
    if (!name) { showToast('❌ 請填寫地區名稱'); return; }
    if (!managerIdRaw) { showToast('❌ 請選擇有效 Manager 帳號'); return; }
    try {
        const res = await apiFetch('PUT', `/regions/${id}`, { regionName: name, managerId: Number(managerIdRaw), description });
        if (!res.ok) {
            const err = await res.json().catch(() => null);
            throw new Error(err?.message || err?.error || ('API 返回錯誤: ' + res.status));
        }
        showToast('✅ 地區已更新！');
        closeModal('modal-edit-region');
        switchTab('regions');
    } catch (e) {
        showToast('❌ 更新失敗：' + e.message);
    }
}

async function deleteRegion(regionId) {
    const region = (window._regionRows || []).find(r => String(r.id) === String(regionId));
    const name = region?.name ? `「${region.name}」` : `#${regionId}`;
    if (!confirm(`確定要刪除地區 ${name}？\n\n限制確認：若此地區底下仍有機台，後端會拒絕刪除；請先把機台移到其他地區或刪除機台。`)) return;
    try {
        const res = await apiFetch('DELETE', `/regions/${regionId}`);
        if (!res.ok) {
            const err = await res.json().catch(() => null);
            throw new Error(err?.message || err?.error || ('API 返回錯誤: ' + res.status));
        }
        showToast('✅ 地區已刪除');
        switchTab('regions');
    } catch (e) {
        showToast('❌ 刪除失敗：' + e.message);
    }
}

function filterRegions() {
    const keyword = document.getElementById('region-search')?.value.toLowerCase() || '';
    document.querySelectorAll('.region-row').forEach(row => {
        row.style.display = row.textContent.toLowerCase().includes(keyword) ? '' : 'none';
    });
}
