/**
 * 團隊管理相關功能
 */

async function renderTeams(area) {
    area.innerHTML = loadingHTML('載入團隊資料...');

    let teams = [];
    let regions = [];
    try {
        const [teamsRes, regionsRes] = await Promise.all([
            apiFetch('GET', '/teams'),
            apiFetch('GET', '/regions')
        ]);
        const teamsData = await teamsRes.json();
        const regionsData = await regionsRes.json();
        teams = Array.isArray(teamsData) ? teamsData : (teamsData.data || []);
        regions = Array.isArray(regionsData) ? regionsData : (regionsData.data || []);
    } catch (e) {
        area.innerHTML = `<div style="padding:60px;text-align:center;color:var(--danger)">❌ 載入失敗: ${e.message}</div>`;
        return;
    }

    // 載入每個 team 的成員
    for (const team of teams) {
        try {
            const res = await apiFetch('GET', `/teams/${team.teamId}/staff`);
            const data = await res.json();
            team.staff = Array.isArray(data) ? data : (data.data || []);
        } catch (e) {
            team.staff = [];
        }
    }

    area.innerHTML = `
        <div class="page-header">
            <h2>團隊管理</h2>
            <p>管理班組、成員及地區</p>
        </div>

        <!-- 地區管理 -->
        <div class="card" style="margin-bottom:20px;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
                <div style="font-weight:700;">📍 地區管理</div>
                <button class="btn btn-ghost btn-sm" onclick="openModal('modal-add-region')">+ 新增地區</button>
            </div>
            ${regions.length === 0
                ? '<div style="color:var(--muted);font-size:13px;">尚無地區資料</div>'
                : `<div style="display:flex;flex-wrap:wrap;gap:8px;">
                    ${regions.map(r => `
                        <div style="display:flex;align-items:center;gap:8px;background:var(--surface2);padding:8px 14px;border-radius:10px;border:1px solid var(--border);">
                            <span style="font-size:13px;font-weight:600;">${r.name}</span>
                            <button class="btn btn-ghost" style="padding:2px 6px;color:var(--danger);border-color:var(--danger);font-size:11px;"
                                onclick="deleteRegion(${r.id})">✕</button>
                        </div>
                    `).join('')}
                </div>`}
        </div>

        <!-- 班組管理 -->
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;gap:12px;">
            <div style="font-weight:700;font-size:18px;">👥 班組管理</div>
            <input type="text" id="team-search" placeholder="🔍 搜尋班組或地區..." oninput="filterTeams()"
                style="flex:1;max-width:320px;padding:8px 12px;background:var(--surface2);border:1px solid var(--border);border-radius:8px;color:var(--text);font-family:inherit;font-size:13px;" />
            <button class="btn btn-primary btn-sm" onclick="openAddTeamModal()">+ 新增班組</button>
        </div>

        ${teams.length === 0
            ? '<div class="card" style="text-align:center;padding:48px;color:var(--muted);">尚無班組資料</div>'
            : teams.map(team => `
                <div class="card team-card" style="margin-bottom:16px;">
                    <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px;">
                        <div>
                            <h3 style="font-size:16px;font-weight:700;">第 ${team.teamId} 組</h3>
                            <div style="font-size:12px;color:var(--muted);margin-top:4px;">
                                ${team.teamName || ''}${team.regionName ? '｜地區：' + team.regionName : ''}
                            </div>
                        </div>
                        <span class="badge badge-ok">${team.staff.length} 名成員</span>
                    </div>
                    <div style="background:var(--surface2);border-radius:12px;padding:12px;margin-bottom:12px;">
                        <div style="font-size:11px;font-weight:600;color:var(--muted);margin-bottom:8px;">成員列表</div>
                        ${team.staff.length === 0
                            ? '<div style="color:var(--muted);font-size:13px;">尚無成員</div>'
                            : team.staff.map(s => `
                                <div style="display:flex;justify-content:space-between;align-items:center;padding:6px 0;border-bottom:1px solid var(--border);">
                                    <span style="font-size:13px;">${s.staffName || 'Staff #' + (s.staffId || s.staff_id)}</span>
                                    <button class="btn btn-ghost" style="padding:2px 8px;color:var(--danger);border-color:var(--danger);font-size:11px;"
                                        onclick="removeStaffFromTeam(${team.teamId}, ${s.staffId || s.staff_id})">移除</button>
                                </div>
                            `).join('')}
                    </div>
                    <div style="display:flex;gap:8px;">
                        <button class="btn btn-ghost btn-sm" onclick="openAddStaffToTeam(${team.teamId})">+ 新增成員</button>
                    </div>
                </div>
            `).join('')}
    `;
}

// 新增地區
async function submitAddRegion() {
    const name = document.getElementById('new-region-name').value.trim();
    if (!name) { showToast('❌ 請填寫地區名稱'); return; }
    try {
        const res = await apiFetch('POST', '/regions', { regionName: name });
        if (!res.ok) throw new Error('API 返回錯誤: ' + res.status);
        showToast('✅ 地區已新增！');
        closeModal('modal-add-region');
        document.getElementById('new-region-name').value = '';
        switchTab('teams');
    } catch (e) {
        showToast('❌ 新增失敗：' + e.message);
    }
}

// 刪除地區
async function deleteRegion(regionId) {
    if (!confirm('確定要刪除此地區？')) return;
    try {
        const res = await apiFetch('DELETE', `/regions/${regionId}`);
        if (!res.ok) throw new Error('API 返回錯誤: ' + res.status);
        showToast('✅ 地區已刪除');
        switchTab('teams');
    } catch (e) {
        showToast('❌ 刪除失敗：' + e.message);
    }
}

// 新增班組
async function openAddTeamModal() {
    const select = document.getElementById('new-team-region');
    select.innerHTML = '<option value="">-- 選擇負責地區 --</option>';
    try {
        const res = await apiFetch('GET', '/regions');
        const data = await res.json();
        const regions = Array.isArray(data) ? data : (data.data || []);
        select.innerHTML += regions.map(r => `<option value="${r.id}">${r.name}</option>`).join('');
    } catch(e) {
        select.innerHTML = '<option value="">地區載入失敗</option>';
    }
    openModal('modal-add-team');
}

async function submitAddTeam() {
    const name = document.getElementById('new-team-name').value.trim();
    const regionId = document.getElementById('new-team-region').value;
    if (!name) { showToast('❌ 請填寫班組名稱'); return; }
    if (!regionId) { showToast('❌ 請選擇負責地區'); return; }
    try {
        const res = await apiFetch('POST', '/teams', { teamName: name, regionId: Number(regionId), teamStatus: 'Active' });
        if (!res.ok) throw new Error('API 返回錯誤: ' + res.status);
        teamsCache = null;
        showToast('✅ 班組已新增！');
        closeModal('modal-add-team');
        document.getElementById('new-team-name').value = '';
        document.getElementById('new-team-region').value = '';
        switchTab('teams');
    } catch (e) {
        showToast('❌ 新增失敗：' + e.message);
    }
}

// 新增成員到班組
function openAddStaffToTeam(teamId) {
    window._addStaffTeamId = teamId;
    document.getElementById('add-staff-team-label').textContent = '第 ' + teamId + ' 組';
    document.getElementById('add-staff-id').value = '';
    openModal('modal-add-staff');
}

async function submitAddStaff() {
    const staffId = parseInt(document.getElementById('add-staff-id').value);
    const teamId = window._addStaffTeamId;
    if (!staffId) { showToast('❌ 請填寫 Staff User ID'); return; }

    // 先檢查該 user 是否存在且是 Staff
    try {
        const checkRes = await apiFetch('GET', '/regions');  // 先用任意 API 確認連線
        
        const res = await apiFetch('POST', `/teams/${teamId}/staff`, { staffId: staffId });
        if (!res.ok) {
            const errData = await res.json().catch(() => null);
            const msg = errData?.message || '新增失敗';
            if (msg.includes('找不到') || res.status === 404) {
                showToast('❌ 找不到該 Staff ID，請確認是否正確');
            } else if (msg.includes('Duplicate') || res.status === 409) {
                showToast('❌ 該成員已在此班組中');
            } else {
                showToast('❌ ' + msg);
            }
            return;
        }
        showToast('✅ 成員已加入！');
        closeModal('modal-add-staff');
        switchTab('teams');
    } catch (e) {
        showToast('❌ 找不到 User ID: ' + staffId + '，請確認是否存在');
    }
}

// 移除成員
async function removeStaffFromTeam(teamId, staffId) {
    if (!confirm('確定要移除此成員？')) return;
    try {
        const res = await apiFetch('DELETE', `/teams/${teamId}/staff/${staffId}`);
        if (!res.ok) throw new Error('API 返回錯誤: ' + res.status);
        showToast('✅ 成員已移除');
        teamsCache = null;
        switchTab('teams');
    } catch (e) {
        showToast('❌ 移除失敗：' + e.message);
    }
}

function filterTeams() {
    const keyword = document.getElementById('team-search')?.value.toLowerCase() || '';
    document.querySelectorAll('.team-card').forEach(card => {
        card.style.display = card.textContent.toLowerCase().includes(keyword) ? '' : 'none';
    });
}
