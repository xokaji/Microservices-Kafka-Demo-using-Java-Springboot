const API_BASE = "http://localhost:8081/orders";

const form = document.getElementById("order-form");
const orderIdInput = document.getElementById("orderId");
const productInput = document.getElementById("product");
const quantityInput = document.getElementById("quantity");
const statusEl = document.getElementById("api-status");
const ordersBody = document.getElementById("orders-body");

const stepOrder = document.getElementById("step-order");
const stepPayment = document.getElementById("step-payment");
const stepNotification = document.getElementById("step-notification");
const arrow1 = document.getElementById("arrow-1");
const arrow2 = document.getElementById("arrow-2");

const refreshBtn = document.getElementById("refresh-btn");
const clearLogBtn = document.getElementById("clear-log");
const logList = document.getElementById("log-list");

function addLog(message) {
  const li = document.createElement("li");
  const now = new Date().toLocaleTimeString();
  li.textContent = `[${now}] ${message}`;
  logList.prepend(li);
}

function setStatus(message, type) {
  statusEl.textContent = message;
  statusEl.classList.remove("ok", "error");
  if (type) {
    statusEl.classList.add(type);
  }
}

function resetFlow() {
  [stepOrder, stepPayment, stepNotification].forEach((el) => el.classList.remove("active"));
  [arrow1, arrow2].forEach((el) => el.classList.remove("active"));
}

async function animateFlow() {
  resetFlow();

  stepOrder.classList.add("active");
  addLog("Order service accepted the request.");
  await wait(550);

  arrow1.classList.add("active");
  addLog("Event published to Kafka topic: order-created.");
  await wait(550);

  stepPayment.classList.add("active");
  addLog("Payment service consumed order-created and processed payment.");
  await wait(550);

  arrow2.classList.add("active");
  addLog("Payment service published payment-success topic.");
  await wait(550);

  stepNotification.classList.add("active");
  addLog("Notification service consumed payment-success and sent confirmation.");
}

function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function renderOrders(orders) {
  ordersBody.innerHTML = "";

  if (!Array.isArray(orders) || orders.length === 0) {
    const row = document.createElement("tr");
    row.innerHTML = "<td colspan=\"3\">No orders in session yet.</td>";
    ordersBody.appendChild(row);
    return;
  }

  orders.forEach((order) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${escapeHtml(order.orderId ?? "")}</td>
      <td>${escapeHtml(order.product ?? "")}</td>
      <td>${escapeHtml(String(order.quantity ?? ""))}</td>
    `;
    ordersBody.appendChild(row);
  });
}

function escapeHtml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}

async function fetchOrders() {
  try {
    const response = await fetch(API_BASE);
    if (!response.ok) {
      throw new Error(`GET /orders failed with status ${response.status}`);
    }
    const data = await response.json();
    renderOrders(data);
    addLog("Fetched latest order list from order-service.");
  } catch (error) {
    addLog(`Unable to load orders: ${error.message}`);
    setStatus(
      "Could not fetch orders. Make sure order-service is running on http://localhost:8081.",
      "error"
    );
  }
}

async function createOrder(order) {
  const response = await fetch(API_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });

  if (!response.ok) {
    throw new Error(`POST /orders failed with status ${response.status}`);
  }

  return response.text();
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  setStatus("Sending order event...", null);

  const payload = {
    orderId: orderIdInput.value.trim(),
    product: productInput.value.trim(),
    quantity: Number(quantityInput.value),
  };

  if (!payload.orderId || !payload.product || !payload.quantity) {
    setStatus("Please fill all fields with valid values.", "error");
    return;
  }

  try {
    const responseText = await createOrder(payload);
    setStatus(responseText, "ok");
    addLog(`Order ${payload.orderId} submitted to order-service.`);
    await animateFlow();
    await fetchOrders();
  } catch (error) {
    setStatus(`Could not send order: ${error.message}`, "error");
    addLog(`Order submit failed: ${error.message}`);
  }
});

refreshBtn.addEventListener("click", async () => {
  setStatus("Refreshing order list...", null);
  await fetchOrders();
  setStatus("Order list updated.", "ok");
});

clearLogBtn.addEventListener("click", () => {
  logList.innerHTML = "";
  addLog("Log cleared.");
});

addLog("Dashboard ready. Start services and submit an order.");
fetchOrders();
