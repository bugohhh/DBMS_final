/**
 * 團隊管理相關功能
 */

async function renderTeams(area) {
    area.innerHTML = loadingHTML('載入團隊資料...');

    let teams = [];
    try {
        const teamsRes = await apiFetch('GET', '/teams');
        const teamsData = await teamsRes.json();
        teams = Array.isArray(teamsData) ? teamsData : (teamsData.data || []);
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

    window._teamRows = teams;

    area.innerHTML = `
        <div class="page-header">
            <h2>團隊管理</h2>
            <p>管理班組與成員；地區請到「地區管理」頁面維護。</p>
        </div>

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
                        <button class="btn btn-ghost btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="deleteTeam(${team.teamId})">刪除班組</button>
                    </div>
                </div>
            `).join('')}
    `;
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
async function openAddStaffToTeam(teamId) {
    window._addStaffTeamId = teamId;
    document.getElementById('add-staff-team-label').textContent = '第 ' + teamId + ' 組';
    const select = document.getElementById('add-staff-id');
    select.innerHTML = '<option value="">載入 Staff 帳號中...</option>';
    openModal('modal-add-staff');

    try {
        const res = await apiFetch('GET', '/auth/users');
        const data = await res.json();
        const users = (data.data || []).filter(u => u.user_type === 'Staff');
        const currentTeam = (window._teamRows || []).find(t => String(t.teamId) === String(teamId));
        const currentStaffIds = new Set((currentTeam?.staff || []).map(s => String(s.staffId || s.staff_id)));
        const available = users.filter(u => !currentStaffIds.has(String(u.user_id)));
        select.innerHTML = '<option value="">-- 選擇有效 Staff 帳號 --</option>' +
            available.map(u => `<option value="${u.user_id}">#${u.user_id} ${escapeHtml(u.user_name || '')}${u.account ? '（' + escapeHtml(u.account) + '）' : ''}</option>`).join('');
        if (available.length === 0) {
            select.innerHTML = '<option value="">沒有可加入的有效 Staff 帳號</option>';
        }
    } catch (e) {
        select.innerHTML = '<option value="">Staff 帳號載入失敗</option>';
    }
}

async function submitAddStaff() {
    const staffId = parseInt(document.getElementById('add-staff-id').value);
    const teamId = window._addStaffTeamId;
    if (!staffId) { showToast('❌ 請選擇有效 Staff 帳號'); return; }

    try {
        const res = await apiFetch('POST', `/teams/${teamId}/staff`, { staffId: staffId });
        if (!res.ok) {
            const errData = await res.json().catch(() => null);
            const msg = errData?.message || '新增失敗';
            if (msg.includes('找不到') || res.status === 404 || res.status === 400) {
                showToast('❌ ' + msg);
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

async function deleteTeam(teamId) {
    const team = (window._teamRows || []).find(t => String(t.teamId) === String(teamId));
    const teamName = team?.teamName || '';
    const staffCount = team?.staff?.length || 0;
    const warning = `確定要刪除第 ${teamId} 組${teamName ? '（' + teamName + '）' : ''}？\n\n後果：\n- 此班組會被永久刪除\n- 此班組的補貨任務與補貨明細會一起刪除\n- ${staffCount} 名成員會被移出此班組（帳號不會刪除）`;
    if (!confirm(warning)) return;
    try {
        const res = await apiFetch('DELETE', `/teams/${teamId}`);
        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            throw new Error(data.message || 'API 返回錯誤: ' + res.status);
        }
        teamsCache = null;
        showToast('✅ 班組已刪除，相關補貨任務已同步刪除');
        switchTab('teams');
    } catch (e) {
        showToast('❌ 刪除失敗：' + e.message);
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
