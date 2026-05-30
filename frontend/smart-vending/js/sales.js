/** 銷售分析頁面 */
async function renderSales(area, selectedRegion = 'all') {
    area.innerHTML = loadingHTML('載入銷售分析...');

    try {
        const regionsRes = await apiFetch('GET', '/regions');
        const regionsRaw = await regionsRes.json();
        const regions = Array.isArray(regionsRaw) ? regionsRaw : (regionsRaw.data || []);

        const num = v => Number(String(v ?? 0).replace(/,/g, '')) || 0;
        let topDrinks = [];
        let regionRevenue = 0;
        let salesSummary = [];

        if (selectedRegion !== 'all') {
            const sumRes = await apiFetch('GET', `/sales-records/regions/${selectedRegion}/drink-summary`);
            const sumData = await sumRes.json();
            topDrinks = (sumData.data || []).map(d => ({
                drink_name: d.drink_name || d.drinkName || `Drink #${d.drink_id || d.drinkId || ''}`,
                total_quantity: num(d.total_quantity ?? d.totalQuantity),
                total_revenue: num(d.total_revenue ?? d.totalRevenue)
            })).sort((a, b) => b.total_quantity - a.total_quantity);
            regionRevenue = topDrinks.reduce((a, b) => a + b.total_revenue, 0);
            salesSummary = topDrinks.map(d => ({ label: d.drink_name, total_quantity: d.total_quantity }));
        } else {
            const sRes = await apiFetch('GET', '/sales-records');
            if (!sRes.ok) throw new Error('API 返回錯誤: ' + sRes.status);
            const salesData = (await sRes.json()).data || [];
            const drinkMap = {};
            const dayMap = {};
            salesData.forEach(s => {
                const drink = s.drinkName || s.drink_name || s.drink || `Drink #${s.drinkId || s.drink_id}`;
                const qty = num(s.quantity);
                const price = num(s.price ?? s.salePrice ?? s.unit_price);
                if (!drinkMap[drink]) drinkMap[drink] = { drink_name: drink, total_quantity: 0, total_revenue: 0 };
                drinkMap[drink].total_quantity += qty;
                drinkMap[drink].total_revenue += qty * price;

                const date = (s.saleTime || s.sale_time || '').substring(0, 10) || '未知';
                if (!dayMap[date]) dayMap[date] = { label: date.substring(5), total_quantity: 0, total_revenue: 0 };
                dayMap[date].total_quantity += qty;
                dayMap[date].total_revenue += qty * price;
            });
            topDrinks = Object.values(drinkMap).sort((a, b) => b.total_quantity - a.total_quantity).slice(0, 5);
            regionRevenue = topDrinks.reduce((a, b) => a + b.total_revenue, 0);
            salesSummary = Object.values(dayMap).sort((a, b) => a.label.localeCompare(b.label));
        }

        const maxQty = topDrinks.length ? Math.max(...topDrinks.map(d => d.total_quantity)) : 1;
        const totalQty = topDrinks.reduce((a, b) => a + b.total_quantity, 0);
        const totalRev = topDrinks.reduce((a, b) => a + b.total_revenue, 0);
        const regionOptions = `<option value="all" ${selectedRegion === 'all' ? 'selected' : ''}>🌍 所有地區</option>` +
            regions.map(r => `<option value="${r.id || r.regionId}" ${String(selectedRegion) === String(r.id || r.regionId) ? 'selected' : ''}>📍 ${r.name || r.regionName}</option>`).join('');

        area.innerHTML = `
            <div class="page-header" style="display:flex; justify-content:space-between; align-items:flex-end;">
                <div><h2>銷售分析</h2><p>銷售數量、飲料名稱與營收統計</p></div>
                <div class="form-group" style="margin-bottom:0; min-width:220px;">
                    <select id="region-filter" onchange="renderSales(document.getElementById('main-content'), this.value)"
                            style="width:100%; padding:8px 12px; border-radius:8px; border:1px solid var(--border); background:var(--surface2); color:var(--text); font-family:inherit;">
                        ${regionOptions}
                    </select>
                </div>
            </div>
            <div class="grid-2">
                <div class="card">
                    <div style="font-weight:700;margin-bottom:24px">熱門商品 Top ${topDrinks.length}</div>
                    ${topDrinks.length > 0 ? topDrinks.map((d, i) => `
                        <div class="bar-row">
                            <div class="bar-label">${i+1}. ${d.drink_name}<br><span style="font-size:11px;color:var(--muted)">營收 NT$ ${Math.round(d.total_revenue).toLocaleString()}</span></div>
                            <div class="bar-track"><div class="bar-fill" style="width:0%" data-pct="${Math.round(d.total_quantity/maxQty*100)}"></div></div>
                            <div class="bar-val">${d.total_quantity.toLocaleString()}</div>
                        </div>`).join('') : '<div style="color:var(--muted);text-align:center;">無銷售紀錄</div>'}
                </div>
                <div class="card">
                    <div style="font-weight:700;margin-bottom:20px">週期概況</div>
                    <div class="card stat-card" style="background:var(--surface2);margin-bottom:12px"><div class="label">總銷售瓶數</div><div class="value accent-blue">${totalQty.toLocaleString()}</div></div>
                    <div class="card stat-card" style="background:var(--surface2);margin-bottom:12px"><div class="label">${selectedRegion === 'all' ? '總營收' : '該地區總營收'}</div><div class="value accent-green" style="font-size:28px">NT$ ${Math.round(selectedRegion === 'all' ? totalRev : regionRevenue).toLocaleString()}</div></div>
                    <div class="card stat-card" style="background:var(--surface2)"><div class="label">最暢銷商品</div><div class="value" style="font-size:20px;font-weight:900">${topDrinks[0]?.drink_name || '—'}</div></div>
                    <div style="margin-top:20px"><div style="font-size:11px;font-weight:700;color:var(--muted);margin-bottom:12px">銷量趨勢</div><div style="display:flex;align-items:flex-end;gap:4px;height:60px">
                        ${salesSummary.length > 0 ? salesSummary.map(s => {
                            const maxS = Math.max(...salesSummary.map(x => x.total_quantity));
                            const h = maxS > 0 ? Math.round(s.total_quantity / maxS * 100) : 0;
                            return `<div style="flex:1;background:var(--accent);opacity:.7;height:${h}%;border-radius:3px 3px 0 0" title="${s.label}: ${s.total_quantity}"></div>`;
                        }).join('') : ''}
                    </div></div>
                </div>
            </div>`;

        setTimeout(() => document.querySelectorAll('.bar-fill').forEach(el => el.style.width = el.dataset.pct + '%'), 50);
    } catch(e) {
        area.innerHTML = `<div class="card" style="padding:24px;color:var(--danger)">銷售分析載入失敗：${e.message}</div>`;
    }
}
