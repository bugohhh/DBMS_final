async function renderDrinks(area) {
    area.innerHTML = loadingHTML('載入飲料資料...');
    try {
        const res = await apiFetch('GET', '/drinks');
        if (!res.ok) throw new Error('API 返回錯誤: ' + res.status);
        const data = await res.json();
        const drinks = Array.isArray(data) ? data : (data.data || []);
        window._drinkRows = drinks;
        area.innerHTML = `
            <div class="page-header" style="display:flex;justify-content:space-between;align-items:flex-start">
                <div><h2>飲料管理</h2><p>顯示 Drink ID、名稱、品牌、類別、容量、狀態與全機台庫存加總</p></div>
                <button class="btn btn-primary" onclick="openModal('modal-drink-manager-add')">+ 新增飲料</button>
            </div>
            <div class="card">
                <table>
                    <thead><tr><th>Drink ID</th><th>名稱</th><th>品牌</th><th>類別</th><th>容量</th><th>總庫存</th><th>狀態</th><th>操作</th></tr></thead>
                    <tbody>
                        ${drinks.length ? drinks.map(d => {
                            const id = d.drink_id ?? d.drinkId;
                            return `
                            <tr>
                                <td style="font-family:var(--mono);color:var(--muted)">#${id}</td>
                                <td style="font-weight:700">${d.drink_name ?? d.drinkName ?? '—'}</td>
                                <td>${d.brand || '—'}</td>
                                <td>${d.category || '—'}</td>
                                <td>${d.size || '—'}</td>
                                <td>${d.drink_quantity ?? d.drinkQuantity ?? 0}</td>
                                <td>${d.status || '—'}</td>
                                <td style="display:flex;gap:6px;">
                                    <button class="btn btn-ghost btn-sm" onclick="openEditDrink(${id})">編輯</button>
                                    <button class="btn btn-ghost btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="deleteDrink(${id})">刪除</button>
                                </td>
                            </tr>`}).join('') : '<tr><td colspan="8" style="text-align:center;color:var(--muted);padding:32px">目前沒有飲料資料</td></tr>'}
                    </tbody>
                </table>
            </div>`;
    } catch (e) {
        area.innerHTML = `<div class="card" style="padding:24px;color:var(--danger)">飲料資料載入失敗：${e.message}</div>`;
    }
}

async function submitDrinkManagerAdd() {
    const name = document.getElementById('drink-manager-name').value.trim();
    const brand = document.getElementById('drink-manager-brand').value.trim();
    const category = document.getElementById('drink-manager-category').value.trim();
    const size = document.getElementById('drink-manager-size').value.trim();
    if (!name) { showToast('❌ 請填寫飲料名稱'); return; }
    try {
        const res = await apiFetch('POST', '/drinks', { drinkName: name, brand, category, size, status: 'Active' });
        const data = await res.json().catch(() => ({}));
        if (!res.ok || data.success === false) throw new Error(data.message || 'API 返回錯誤: ' + res.status);
        showToast('✅ 飲料已新增');
        closeModal('modal-drink-manager-add');
        ['drink-manager-name','drink-manager-brand','drink-manager-category','drink-manager-size'].forEach(id => document.getElementById(id).value = '');
        switchTab('drinks');
    } catch(e) { showToast('❌ 新增失敗：' + e.message); }
}

function openEditDrink(drinkId) {
    const drink = (window._drinkRows || []).find(d => String(d.drink_id ?? d.drinkId) === String(drinkId));
    if (!drink) { showToast('❌ 找不到飲料資料'); return; }
    window._editDrinkId = drinkId;
    document.getElementById('edit-drink-id').textContent = '#' + drinkId;
    document.getElementById('edit-drink-name').value = drink.drink_name ?? drink.drinkName ?? '';
    document.getElementById('edit-drink-brand').value = drink.brand || '';
    document.getElementById('edit-drink-category').value = drink.category || '';
    document.getElementById('edit-drink-size').value = drink.size || '';
    document.getElementById('edit-drink-status').value = drink.status || 'Active';
    openModal('modal-drink-manager-edit');
}

async function submitDrinkManagerEdit() {
    const drinkId = window._editDrinkId;
    const drinkName = document.getElementById('edit-drink-name').value.trim();
    if (!drinkName) { showToast('❌ 請填寫飲料名稱'); return; }
    try {
        const res = await apiFetch('PUT', `/drinks/${drinkId}`, {
            drinkName,
            brand: document.getElementById('edit-drink-brand').value.trim(),
            category: document.getElementById('edit-drink-category').value.trim(),
            size: document.getElementById('edit-drink-size').value.trim(),
            status: document.getElementById('edit-drink-status').value
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok || data.success === false) throw new Error(data.message || 'API 返回錯誤: ' + res.status);
        showToast('✅ 飲料已更新');
        closeModal('modal-drink-manager-edit');
        switchTab('drinks');
    } catch(e) { showToast('❌ 編輯失敗：' + e.message); }
}

async function deleteDrink(drinkId) {
    if (!confirm(`確定刪除飲料 #${drinkId}？若該飲料已被庫存、銷售或補貨紀錄引用，後端會拒絕刪除。`)) return;
    try {
        const res = await apiFetch('DELETE', `/drinks/${drinkId}`);
        const data = await res.json().catch(() => ({}));
        if (!res.ok || data.success === false) throw new Error(data.message || '資料庫外鍵限制，無法刪除');
        showToast('✅ 飲料已刪除');
        switchTab('drinks');
    } catch(e) { showToast('❌ 刪除失敗：' + e.message); }
}
