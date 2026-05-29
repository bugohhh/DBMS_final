/**
 * 渲染銷售分析頁面
 * @param {HTMLElement} area - 內容容器
 * @param {string} selectedRegion - 選擇的地區 (預設為 'all')
 */
async function renderSales(area, selectedRegion = 'all') {
    area.innerHTML = loadingHTML('載入銷售分析...');

    let topDrinks, salesSummary, regions;
    const today = new Date().toISOString().split('T')[0];
    const weekAgo = new Date(Date.now() - 7*86400000).toISOString().split('T')[0];

    // 1. 準備查詢參數
    let topDrinksUrl = `/analytics/top-drinks?start_date=${weekAgo}&end_date=${today}&limit=5`;
    let summaryUrl = `/analytics/sales-summary?start_date=${weekAgo}&end_date=${today}&group_by=day`;

    // 如果有選擇特定地區，就把參數加到 URL 後面
    if (selectedRegion !== 'all') {
        topDrinksUrl += `&region=${encodeURIComponent(selectedRegion)}`;
        summaryUrl += `&region=${encodeURIComponent(selectedRegion)}`;
    }

    if (USE_MOCK) {
        topDrinks    = MOCK.topDrinks;
        salesSummary = MOCK.salesSummary;
        regions = [{ region_id: '文山區', region_name: '文山區' }, { region_id: '信義區', region_name: '信義區' }];
    } else {
        try {
            // 嘗試用後端 API
            const rRes = await apiFetch('GET', '/regions');
            const regionsData = await rRes.json();
            regions = Array.isArray(regionsData) ? regionsData.map(r => ({ region_id: r.name, region_name: r.name })) : [];

            // 取得銷售資料
            const sRes = await apiFetch('GET', '/sales-records');
            if (!sRes.ok) throw new Error('API 返回錯誤');
            const salesData = (await sRes.json()).data || [];

            // 從銷售紀錄計算 topDrinks
            const drinkMap = {};
            salesData.forEach(s => {
                const key = s.drinkName || s.drink_name || 'unknown';
                if (!drinkMap[key]) drinkMap[key] = { drink_name: key, total_quantity: 0, total_revenue: 0 };
                drinkMap[key].total_quantity += (s.quantity || 1);
                drinkMap[key].total_revenue += (s.totalPrice || s.total_price || 0);
            });
            topDrinks = Object.values(drinkMap).sort((a, b) => b.total_quantity - a.total_quantity).slice(0, 5);

            // 計算每日摘要
            const dayMap = {};
            salesData.forEach(s => {
                const date = (s.saleTime || s.sale_time || '').substring(0, 10);
                if (!dayMap[date]) dayMap[date] = { label: date.substring(5), total_quantity: 0, total_revenue: 0 };
                dayMap[date].total_quantity += (s.quantity || 1);
                dayMap[date].total_revenue += (s.totalPrice || s.total_price || 0);
            });
            salesSummary = Object.values(dayMap).sort((a, b) => a.label.localeCompare(b.label));

            if (topDrinks.length === 0) throw new Error('無資料');
        } catch (e) {
            console.warn('銷售分析 API 不可用，使用 mock data', e);
            topDrinks    = MOCK.topDrinks;
            salesSummary = MOCK.salesSummary;
            regions = [{ region_id: '文山區', region_name: '文山區' }, { region_id: '信義區', region_name: '信義區' }];
        }
    }

    const maxQty = topDrinks.length ? Math.max(...topDrinks.map(d => d.total_quantity)) : 1;
    const totalQty = topDrinks.reduce((a, b) => a + b.total_quantity, 0);
    const totalRev = topDrinks.reduce((a, b) => a + b.total_revenue, 0);

    // 3. 產生下拉選單的 HTML (onchange 時重新呼叫 renderSales)
    const regionOptions = `<option value="all" ${selectedRegion === 'all' ? 'selected' : ''}>🌍 所有地區 (整體營收)</option>` +
        regions.map(r => `<option value="${r.region_name}" ${selectedRegion === r.region_name ? 'selected' : ''}>📍 ${r.region_name}</option>`).join('');

    area.innerHTML = `
        <div class="page-header" style="display:flex; justify-content:space-between; align-items:flex-end;">
            <div>
                <h2>銷售分析</h2>
                <p>近 7 日銷售數據</p>
            </div>
            <div class="form-group" style="margin-bottom:0; min-width:200px;">
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
                        <div class="bar-label">${i+1}. ${d.drink_name}</div>
                        <div class="bar-track">
                            <div class="bar-fill" style="width:0%" data-pct="${Math.round(d.total_quantity/maxQty*100)}"></div>
                        </div>
                        <div class="bar-val">${d.total_quantity.toLocaleString()}</div>
                    </div>`).join('') : '<div style="color:var(--muted);text-align:center;">該地區無銷售紀錄</div>'}
            </div>
            <div class="card">
                <div style="font-weight:700;margin-bottom:20px">週期概況</div>
                <div class="card stat-card" style="background:var(--surface2);margin-bottom:12px">
                    <div class="label">總銷售瓶數</div>
                    <div class="value accent-blue">${totalQty.toLocaleString()}</div>
                </div>
                <div class="card stat-card" style="background:var(--surface2);margin-bottom:12px">
                    <div class="label">總營收</div>
                    <div class="value accent-green" style="font-size:28px">NT$ ${totalRev.toLocaleString()}</div>
                </div>
                <div class="card stat-card" style="background:var(--surface2)">
                    <div class="label">最暢銷商品</div>
                    <div class="value" style="font-size:20px;font-weight:900">${topDrinks[0]?.drink_name || '—'}</div>
                </div>
                <div style="margin-top:20px">
                    <div style="font-size:11px;font-weight:700;color:var(--muted);text-transform:uppercase;letter-spacing:.08em;margin-bottom:12px">每日銷量趨勢</div>
                    <div style="display:flex;align-items:flex-end;gap:4px;height:60px">
                    ${salesSummary.length > 0 ? salesSummary.map(s => {
                        const maxS = Math.max(...salesSummary.map(x => x.total_quantity));
                        const h = maxS > 0 ? Math.round(s.total_quantity / maxS * 100) : 0;
                        return `<div style="flex:1;background:var(--accent);opacity:.7;height:${h}%;border-radius:3px 3px 0 0;position:relative" title="${s.label}: ${s.total_quantity}"></div>`;
                    }).join('') : ''}
                    </div>
                    <div style="display:flex;gap:4px;margin-top:4px">
                    ${salesSummary.map(s => `<div style="flex:1;font-size:9px;color:var(--muted);text-align:center">${s.label}</div>`).join('')}
                    </div>
                </div>
            </div>
        </div>`;

    setTimeout(() => {
        document.querySelectorAll('.bar-fill').forEach(el => {
            el.style.width = el.dataset.pct + '%';
        });
    }, 50);
}