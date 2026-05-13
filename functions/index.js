const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// ── SePay Webhook ─────────────────────────────────────────────────────────────
// SePay gọi URL này khi có giao dịch tiền vào tài khoản 0383562784
// URL webhook: https://us-central1-dacs3-d4e36.cloudfunctions.net/sePayWebhook
exports.sePayWebhook = functions.https.onRequest(async (req, res) => {
    // Chỉ nhận POST
    if (req.method !== "POST") {
        return res.status(405).send("Method Not Allowed");
    }

    try {
        const body = req.body;
        functions.logger.info("SePay webhook received:", body);

        // SePay gửi JSON với các field:
        // id, gateway, transactionDate, accountNumber, subAccount,
        // amount_in, amount_out, accumulated, code, transaction_content,
        // referenceCode, description, transferType, virtualAccount, etc.

        const amountIn         = body.amount_in || 0;
        const transactionContent = (body.transaction_content || "").toLowerCase();
        const transferType     = body.transferType || "";

        // Chỉ xử lý tiền vào
        if (transferType !== "in" && amountIn <= 0) {
            return res.status(200).json({ success: false, message: "Not a credit transaction" });
        }

        // Tìm đơn hàng khớp với nội dung chuyển khoản
        // Nội dung format: "DOAN3 username timestamp"
        // Tìm đơn hàng có status "Chờ xác nhận" và total khớp amount_in
        const ordersSnap = await db.collection("orders")
            .where("status", "==", "Chờ xác nhận")
            .where("total", "==", Number(amountIn))
            .limit(10)
            .get();

        if (ordersSnap.empty) {
            functions.logger.warn("No matching order found for amount:", amountIn);
            return res.status(200).json({ success: false, message: "No matching order" });
        }

        // Tìm đơn khớp nội dung chuyển khoản
        let matchedOrder = null;
        ordersSnap.forEach(doc => {
            const order = doc.data();
            const username = (order.username || "").toLowerCase();
            if (transactionContent.includes("doan3") &&
                transactionContent.includes(username)) {
                matchedOrder = { id: doc.id, ...order };
            }
        });

        // Nếu không khớp username, lấy đơn đầu tiên có amount khớp
        if (!matchedOrder) {
            const firstDoc = ordersSnap.docs[0];
            matchedOrder = { id: firstDoc.id, ...firstDoc.data() };
        }

        // Cập nhật trạng thái đơn hàng → "Đã xác nhận"
        await db.collection("orders").doc(matchedOrder.id).update({
            status: "Đã xác nhận",
            paidAt: admin.firestore.FieldValue.serverTimestamp(),
            transactionContent: body.transaction_content || "",
            transactionId: body.id || ""
        });

        // Ghi log thanh toán
        await db.collection("payments").add({
            orderId: matchedOrder.id,
            amount: amountIn,
            transactionContent: body.transaction_content || "",
            transactionId: body.id || "",
            gateway: body.gateway || "MB",
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });

        functions.logger.info("Order updated:", matchedOrder.id);
        return res.status(200).json({ success: true, orderId: matchedOrder.id });

    } catch (error) {
        functions.logger.error("Webhook error:", error);
        return res.status(500).json({ success: false, error: error.message });
    }
});
