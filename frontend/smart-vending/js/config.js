/**
 * 全局配置
 * 設定：後端串接時只需改這裡的 BASE_URL
 * 開發中（後端還沒好）→ USE_MOCK = true
 * 後端跑起來後        → USE_MOCK = false
 */

const BASE_URL = 'http://localhost:8080/api';
const USE_MOCK = false;

/**
 * Mock 數據（後端還沒好時使用）
 * 欄位名稱完全對齊 API 規格的 Response 格式
 */
const MOCK = {
    // GET /machines response → data[]
    machines: [
        { machine_id: 1, machine_name: "商學院 1F",   region_name: "文山區", status: "Normal",   inventory: [{drink_name:"可口可樂",quantity:18},{drink_name:"原萃綠茶",quantity:2},{drink_name:"美粒果",quantity:0}] },
        { machine_id: 2, machine_name: "圖書館 B1",   region_name: "文山區", status: "Low",      inventory: [{drink_name:"可口可樂",quantity:5},{drink_name:"原萃綠茶",quantity:12},{drink_name:"美粒果",quantity:8}] },
        { machine_id: 3, machine_name: "行政大樓 1F", region_name: "文山區", status: "Critical", inventory: [{drink_name:"可口可樂",quantity:1},{drink_name:"原萃綠茶",quantity:0},{drink_name:"美粒果",quantity:0}] },
        { machine_id: 4, machine_name: "學生活動中心",region_name: "大安區", status: "Normal",   inventory: [{drink_name:"可口可樂",quantity:20},{drink_name:"原萃綠茶",quantity:19},{drink_name:"美粒果",quantity:15}] },
    ],
    // GET /refill-tasks response → data[]
    tasks: [
        { refilltask_id: 101, region_name: "文山區", team_name: "A Team", task_date: "2026-05-09", task_type: "Regular Refill", status: "Pending",     details: [{drink_name:"可口可樂",planned_quantity:20},{drink_name:"原萃綠茶",planned_quantity:15}] },
        { refilltask_id: 102, region_name: "大安區", team_name: "B Team", task_date: "2026-05-08", task_type: "Regular Refill", status: "Completed",   details: [{drink_name:"可口可樂",planned_quantity:5},{drink_name:"原萃綠茶",planned_quantity:5}] },
    ],
    // GET /analytics/top-drinks response → data[]
    topDrinks: [
        { drink_name: "原萃綠茶", total_quantity: 842, total_revenue: 21050 },
        { drink_name: "可口可樂", total_quantity: 621, total_revenue: 18630 },
        { drink_name: "美粒果",   total_quantity: 390, total_revenue: 13650 },
        { drink_name: "雀巢咖啡", total_quantity: 275, total_revenue: 9625  },
    ],
    // GET /analytics/sales-summary response → data[]
    salesSummary: [
        { label: "05/03", total_quantity: 120, total_revenue: 4200 },
        { label: "05/04", total_quantity: 150, total_revenue: 5250 },
        { label: "05/05", total_quantity: 98,  total_revenue: 3430 },
        { label: "05/06", total_quantity: 173, total_revenue: 6055 },
        { label: "05/07", total_quantity: 142, total_revenue: 4970 },
        { label: "05/08", total_quantity: 188, total_revenue: 6580 },
        { label: "05/09", total_quantity: 206, total_revenue: 7210 },
    ],
};
