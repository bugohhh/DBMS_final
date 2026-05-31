/**
 * 地區管理相關功能
 */

async function renderRegions(area) {
    area.innerHTML = loadingHTML('載入地區資料...');
    try {
        const res = await apiFetch('GET', '/regions');
        const data = await res.json();
        const regions = Array.isArray(data) ? data : (data.data || []);

        area.innerHTML = `
            <div class="page-header">
                <h2>地區管理</h2>
                <p>管理地區名稱與負責 Manager</p>
            </div>
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;gap:12px;">
                <input type="text" id="region-search" placeholder="🔍 搜尋地區或 Manager..." oninput="filterRegions()"
                    style="flex:1;max-width:360px;padding:8px 12px;background:var(--surface2);border:1px solid var(--border);border-radius:8px;color:var(--text);font-family:inherit;font-size:13px;" />
                <button class="btn btn-primary btn-sm" onclick="openAddRegionModal()">+ 新增地區</button>
            </div>
            ${regions.length === 0
                ? '<div class="card" style="text-align:center;padding:48px;color:var(--muted);">尚無地區資料</div>'
                : `<div class="card"><table>
                    <thead><tr><th>地區 ID</th><th>地區名稱</th><th>負責 Manager User ID</th><th>Manager 姓名</th><th>操作</th></tr></thead>
                    <tbody>
                    ${regions.map(r => `
                        <tr class="region-row">
                            <td style="font-family:var(--mono)">${r.id}</td>
                            <td style="font-weight:700">${escapeHtml(r.name || '')}</td>
                            <td>${r.managerId || '—'}</td>
                            <td>${escapeHtml(r.managerName || '—')}</td>
                            <td>
                                <button class="btn btn-ghost btn-sm" onclick='openEditRegionModal(${JSON.stringify(r).replace(/'/g, "&#39;")})'>編輯</button>
                                <button class="btn btn-ghost btn-sm" style="color:var(--danger);border-color:var(--danger);" onclick="deleteRegion(${r.id})">刪除</button>
                            </td>
                        </tr>
                    `).join('')}
                    </tbody>
                </table></div>`}
        `;
    } catch (e) {
        area.innerHTML = `<div style="padding:60px;text-align:center;color:var(--danger)">❌ 載入失敗: ${e.message}</div>`;
    }
}

function openAddRegionModal() {
    document.getElementById('new-region-name').value = '';
    document.getElementById('new-region-manager-id').value = '';
    openModal('modal-add-region');
}

async function submitAddRegion() {
    const name = document.getElementById('new-region-name').value.trim();
    const managerIdRaw = document.getElementById('new-region-manager-id').value.trim();
    if (!name) { showToast('❌ 請填寫地區名稱'); return; }
    if (!managerIdRaw) { showToast('❌ 請填寫負責 Manager User ID'); return; }
    try {
        const res = await apiFetch('POST', '/regions', { regionName: name, managerId: Number(managerIdRaw) });
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

function openEditRegionModal(region) {
    window._editRegionId = region.id;
    document.getElementById('edit-region-id').textContent = '#' + region.id;
    document.getElementById('edit-region-name').value = region.name || '';
    document.getElementById('edit-region-manager-id').value = region.managerId || '';
    openModal('modal-edit-region');
}

async function submitEditRegion() {
    const id = window._editRegionId;
    const name = document.getElementById('edit-region-name').value.trim();
    const managerIdRaw = document.getElementById('edit-region-manager-id').value.trim();
    if (!id) { showToast('❌ 找不到地區 ID'); return; }
    if (!name) { showToast('❌ 請填寫地區名稱'); return; }
    if (!managerIdRaw) { showToast('❌ 請填寫負責 Manager User ID'); return; }
    try {
        const res = await apiFetch('PUT', `/regions/${id}`, { regionName: name, managerId: Number(managerIdRaw) });
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
    if (!confirm('確定要刪除此地區？')) return;
    try {
        const res = await apiFetch('DELETE', `/regions/${regionId}`);
        if (!res.ok) throw new Error('API 返回錯誤: ' + res.status);
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
