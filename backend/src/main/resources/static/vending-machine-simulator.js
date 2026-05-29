const API_BASE = "/api";
const BOTTLE_PLACEHOLDER = "data:image/svg+xml;utf8," + encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 120 170">
  <defs>
    <linearGradient id="bottle" x1="0" x2="1" y1="0" y2="1">
      <stop offset="0" stop-color="#67e8f9"/>
      <stop offset="0.48" stop-color="#22c55e"/>
      <stop offset="1" stop-color="#0f766e"/>
    </linearGradient>
    <linearGradient id="shine" x1="0" x2="1">
      <stop offset="0" stop-color="white" stop-opacity="0.85"/>
      <stop offset="1" stop-color="white" stop-opacity="0"/>
    </linearGradient>
  </defs>
  <rect x="43" y="8" width="34" height="30" rx="8" fill="#164e63"/>
  <rect x="38" y="32" width="44" height="126" rx="18" fill="url(#bottle)"/>
  <rect x="45" y="73" width="30" height="38" rx="8" fill="#f8fafc" opacity="0.92"/>
  <text x="60" y="96" text-anchor="middle" font-family="Arial" font-size="13" font-weight="700" fill="#0f172a">DRINK</text>
  <path d="M49 42 C45 58 45 117 51 145" stroke="url(#shine)" stroke-width="7" stroke-linecap="round" fill="none"/>
</svg>`);

const form = document.querySelector("#machine-form");
const machineIdInput = document.querySelector("#machine-id-input");
const machineIdDisplay = document.querySelector("#machine-id-display");
const machineNameDisplay = document.querySelector("#machine-name-display");
const machineLocationDisplay = document.querySelector("#machine-location-display");
const statusMessage = document.querySelector("#status-message");
const productGrid = document.querySelector("#product-grid");

let currentMachineId = null;
let currentInventory = [];

form.addEventListener("submit", (event) => {
    event.preventDefault();
    refreshInventory();
});

document.addEventListener("DOMContentLoaded", () => {
    refreshInventory();
});

async function refreshInventory() {
    const machineId = Number(machineIdInput.value);
    if (!Number.isInteger(machineId) || machineId <= 0) {
        showStatus("請輸入有效的 machine_id。", "error");
        return;
    }

    setLoading(true);
    showStatus("正在從資料庫讀取庫存...", "loading");

    try {
        const [machine, inventory] = await Promise.all([
            fetchMachine(machineId),
            fetchInventory(machineId)
        ]);

        currentMachineId = machineId;
        currentInventory = inventory;
        renderMachine(machine, machineId);
        renderProducts(inventory);

        if (inventory.length === 0) {
            showStatus("此販賣機目前沒有庫存資料。", "info");
        } else {
            showStatus(`已載入 ${inventory.length} 項商品，資料來自資料庫。`, "success");
        }
    } catch (error) {
        currentMachineId = null;
        currentInventory = [];
        renderMachine(null, machineId);
        renderProducts([]);
        showStatus(normalizeErrorMessage(error), "error");
    } finally {
        setLoading(false);
    }
}

async function fetchMachine(machineId) {
    const response = await fetch(`${API_BASE}/machines/${machineId}`);
    if (!response.ok) {
        throw new Error("找不到此販賣機");
    }
    return response.json();
}

async function fetchInventory(machineId) {
    const response = await fetch(`${API_BASE}/public/machines/${machineId}/inventory/details`);
    const body = await parseJsonResponse(response);

    if (!response.ok || body.success === false) {
        throw new Error(body.message || "API 錯誤，無法讀取庫存。");
    }

    return Array.isArray(body.data) ? body.data : [];
}

async function purchaseDrink(drinkId) {
    if (!currentMachineId) {
        showStatus("請先刷新庫存，確認目前販賣機。", "error");
        return;
    }

    const item = currentInventory.find((product) => getField(product, "drinkId", "drink_id") === drinkId);
    if (!item || Number(getField(item, "quantity")) <= 0) {
        showStatus("庫存不足，無法購買。", "error");
        return;
    }

    setProductButtonsDisabled(true);
    showStatus("購買處理中：新增銷售紀錄並扣除庫存...", "loading");

    try {
        const response = await fetch(`${API_BASE}/machines/${currentMachineId}/purchase`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ drink_id: drinkId, quantity: 1 })
        });
        const body = await parseJsonResponse(response);

        if (!response.ok || body.success === false) {
            throw new Error(body.message || "購買失敗，請確認庫存或資料庫狀態。");
        }

        currentInventory = Array.isArray(body.data) ? body.data : await fetchInventory(currentMachineId);
        renderProducts(currentInventory);
        showStatus("購買成功：SalesRecord 已新增，庫存已即時更新。", "success");
    } catch (error) {
        showStatus(normalizeErrorMessage(error), "error");
    } finally {
        setProductButtonsDisabled(false);
    }
}

function renderMachine(machine, fallbackMachineId) {
    machineIdDisplay.textContent = getField(machine, "machineId", "machine_id") ?? fallbackMachineId ?? "-";
    machineNameDisplay.textContent = getField(machine, "machineName", "machine_name") ?? "找不到此販賣機";
    machineLocationDisplay.textContent = getField(machine, "location") ?? "-";
}

function renderProducts(inventory) {
    productGrid.innerHTML = "";

    if (!inventory.length) {
        productGrid.className = "product-grid empty-state";
        productGrid.innerHTML = "<p>沒有商品可顯示。</p>";
        return;
    }

    productGrid.className = "product-grid";

    inventory.forEach((item) => {
        const drinkId = getField(item, "drinkId", "drink_id");
        const name = getField(item, "drinkName", "drink_name") ?? `Drink #${drinkId}`;
        const brand = getField(item, "brand") ?? "Unknown Brand";
        const category = getField(item, "category") ?? "Drink";
        const size = getField(item, "size") ?? "";
        const price = Number(getField(item, "price") ?? 0);
        const quantity = Number(getField(item, "quantity") ?? 0);
        const threshold = Number(getField(item, "threshold") ?? 0);
        const capacity = getField(item, "capacity") ?? "-";
        const isSoldOut = quantity <= 0;
        const isLowStock = !isSoldOut && threshold > 0 && quantity <= threshold;

        const card = document.createElement("article");
        card.className = ["product-card", isLowStock ? "low-stock" : "", isSoldOut ? "sold-out" : ""].filter(Boolean).join(" ");

        card.innerHTML = `
            <img class="drink-image" src="${BOTTLE_PLACEHOLDER}" alt="${escapeHtml(name)} placeholder">
            <div>
                <h3 class="product-name">${escapeHtml(name)}</h3>
                <p class="product-meta">${escapeHtml(brand)} · ${escapeHtml(category)} ${escapeHtml(size)}</p>
            </div>
            <div class="product-stats">
                <div class="stat-pill"><span>PRICE</span>$${formatPrice(price)}</div>
                <div class="stat-pill"><span>STOCK</span>${quantity} / ${capacity}</div>
            </div>
            ${isSoldOut ? `<div class="sold-out-badge">售完</div>` : ""}
            ${isLowStock ? `<div class="low-stock-badge">⚠ Low Stock</div>` : ""}
            <button class="buy-button" type="button" ${isSoldOut ? "disabled" : ""}>${isSoldOut ? "售完" : "購買"}</button>
        `;

        card.querySelector(".buy-button").addEventListener("click", () => purchaseDrink(drinkId));
        productGrid.appendChild(card);
    });
}

async function parseJsonResponse(response) {
    const text = await response.text();
    if (!text) return {};
    try {
        return JSON.parse(text);
    } catch {
        return { success: false, message: text };
    }
}

function setLoading(isLoading) {
    form.querySelector("button").disabled = isLoading;
    machineIdInput.disabled = isLoading;
}

function setProductButtonsDisabled(disabled) {
    document.querySelectorAll(".buy-button").forEach((button) => {
        if (button.textContent !== "售完") {
            button.disabled = disabled;
        }
    });
}

function showStatus(message, type = "info") {
    statusMessage.textContent = message;
    statusMessage.className = `status-message ${type}`;
}

function getField(object, ...keys) {
    if (!object) return undefined;
    for (const key of keys) {
        if (object[key] !== undefined && object[key] !== null) {
            return object[key];
        }
    }
    return undefined;
}

function formatPrice(value) {
    if (!Number.isFinite(value)) return "-";
    return Number.isInteger(value) ? value.toString() : value.toFixed(2);
}

function normalizeErrorMessage(error) {
    const message = error?.message || "API 錯誤或資料庫錯誤。";
    if (message.includes("Inventory item not found") || message.includes("stock is insufficient")) {
        return "庫存不足，或此販賣機沒有該商品。";
    }
    if (message.includes("Failed to fetch")) {
        return "API 錯誤：請確認 Spring Boot 是否已啟動。";
    }
    return message;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
