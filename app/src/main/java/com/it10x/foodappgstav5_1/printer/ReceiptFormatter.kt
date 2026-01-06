package com.it10x.foodappgstav5_1.printer

// -----------------------------
// PRINT MODELS (ONE TRUTH)
// -----------------------------

data class PrintOrder(
    val orderNo: String,
    val customerName: String,
    val dateTime: String,
    val items: List<PrintItem>,
    val itemTotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val tax: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double
)

data class PrintItem(
    val name: String,
    val quantity: Int,
    val price: Double = 0.0,
    val subtotal: Double = 0.0
)

// -----------------------------
// RECEIPT FORMATTER
// -----------------------------

object ReceiptFormatter {

    private const val LINE_WIDTH = 32
    private const val ALIGN_LEFT = "\u001B\u0061\u0000"

    // -----------------------------
    // BILLING RECEIPT
    // -----------------------------
    fun billing(order: PrintOrder, title: String = "FOOD APP"): String {

        val itemsBlock = if (order.items.isEmpty()) {
            "No items found"
        } else {
            val header =
                "QTY".padEnd(4) +
                        "ITEM".padEnd(16) +
                        "PRICE".padStart(6) +
                        "TOTAL".padStart(6)

            val divider = "-".repeat(LINE_WIDTH)

            val lines = order.items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd(4)
                val name = item.name.take(16).padEnd(16)
                val price = format(item.price).padStart(6)
                val total = format(item.subtotal).padStart(6)
                qty + name + price + total
            }

            "$header\n$divider\n$lines"
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
------------------------------
$title
------------------------------
Order No : ${order.orderNo}
Customer : ${order.customerName.ifBlank { "Walk-in" }}
Date     : ${order.dateTime}
------------------------------
$itemsBlock
------------------------------
${totalLine("Item Total", order.itemTotal)}
${totalLine("Delivery", order.deliveryFee)}
${totalLine("Discount", order.discount)}
${totalLine("Tax", order.tax)}
------------------------------
${totalLine("GRAND TOTAL", order.grandTotal)}
------------------------------
Thank You!


""".trimIndent()
            )
        }
    }

    // -----------------------------
    // KITCHEN RECEIPT
    // -----------------------------
    fun kitchen(order: PrintOrder, title: String = "KITCHEN"): String {

        val itemsBlock = if (order.items.isEmpty()) {
            "No items"
        } else {
            order.items.joinToString("\n") { "${it.quantity.toString().padEnd(3)} ${it.name}" }
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
******** $title ********
Order No : ${order.orderNo}
------------------------
$itemsBlock
------------------------


""".trimIndent()
            )
        }
    }

    // -----------------------------
    // HELPERS
    // -----------------------------
    private fun totalLine(label: String, value: Double): String {
        if (value == 0.0) return ""
        val left = label.padEnd(14)
        val right = format(value).padStart(18)
        return left + right
    }

    private fun format(value: Double): String = "%.2f".format(value)
}
