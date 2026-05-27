/**
 * 銷售分析相關功能（Manager only）
 * 使用：GET /analytics/top-drinks
 *       GET /analytics/sales-summary
 */

/**
 * 渲染銷售分析頁面
 */
async function renderSales(area) {
    area.innerHTML = loadingHTML('載入銷售分析...');

    let topDrinks, salesSummary;
    const today = new Date().toISOString().split('T')[0];
    const weekAgo = new Date(Date.now() - 7*86400000).toISOString().split('T')[0];

    if (USE_MOCK) {
        topDrinks    = MOCK.topDrinks;
        salesSummary = MOCK.salesSummary;
    } else {
        // GET /analytics/top-drinks?start_date=...&end_date=...&limit=5
        // GET /analytics/sales-summary?start_date=...&end_date=...&group_by=day
        const [tRes, sRes] = await Promise.all([
            apiFetch('GET', `/analytics/top-drinks?start_date=${weekAgo}&end_date=${today}&limit=5`),
            apiFetch('GET', `/analytics/sales-summary?start_date=${weekAgo}&end_date=${today}&group_by=day`),
        ]);
        topDrinks    = (await tRes.json()).data;
        salesSummary = (await sRes.json()).data;
    }

    const maxQty = Math.max(...topDrinks.map(d => d.total_quantity));
    const totalQty = topDrinks.reduce((a, b) => a + b.total_quantity, 0);
    const totalRev = topDrinks.reduce((a, b) => a + b.total_revenue, 0);

    area.innerHTML = `
        <div class="page-header">
            <h2>銷售分析</h2>
            <p>近 7 日銷售數據</p>
        </div>
        <div class="grid-2">
            <div class="card">
                <div style="font-weight:700;margin-bottom:24px">熱門商品 Top ${topDrinks.length}</div>
                ${topDrinks.map((d, i) => `
                    <div class="bar-row">
                        <div class="bar-label">${i+1}. ${d.drink_name}</div>
                        <div class="bar-track">
                            <div class="bar-fill" style="width:0%" data-pct="${Math.round(d.total_quantity/maxQty*100)}"></div>
                        </div>
                        <div class="bar-val">${d.total_quantity.toLocaleString()}</div>
                    </div>`).join('')}
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
                    ${salesSummary.map(s => {
                        const maxS = Math.max(...salesSummary.map(x => x.total_quantity));
                        const h = Math.round(s.total_quantity / maxS * 100);
                        return `<div style="flex:1;background:var(--accent);opacity:.7;height:${h}%;border-radius:3px 3px 0 0;position:relative" title="${s.label}: ${s.total_quantity}"></div>`;
                    }).join('')}
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
