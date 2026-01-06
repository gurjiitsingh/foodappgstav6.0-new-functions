package com.it10x.foodappgstav5_1.util

import com.it10x.foodappgstav5_1.data.PrinterRole
import java.text.SimpleDateFormat
import java.util.*

// Generic item interface
interface PrintableItem {
    val name: String
    val quantity: Int
    val price: Double
    val total: Double
}

// Extension to convert PosCartEntity to PrintableItem
fun com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity.toPrintable(): PrintableItem =
    object : PrintableItem {
        override val name = this@toPrintable.name
        override val quantity = this@toPrintable.quantity
        override val price = this@toPrintable.basePrice
        override val total = this@toPrintable.basePrice * this@toPrintable.quantity
    }

// Extension to convert PosOrderItemEntity to PrintableItem
fun com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity.toPrintable(): PrintableItem =
    object : PrintableItem {
        override val name = this@toPrintable.name
        override val quantity = this@toPrintable.quantity
        override val price = this@toPrintable.finalPricePerItem   // <--- updated
        override val total = this@toPrintable.finalTotal          // <--- updated
    }

// -----------------------------
// Receipt Builder
// -----------------------------
fun buildBillingReceipt(
    orderNo: String,
    customerName: String,
    createdAt: Long,
    items: List<PrintableItem>,
    grandTotal: Double
): String {
    val alignLeft = "\u001B\u0061\u0000"

    val itemsBlock = if (items.isEmpty()) "No items" else {
        val header = "QTY".padEnd(5) + "ITEM".take(12).padEnd(12) + "PRICE".padStart(7) + "TOTAL".padStart(6)
        val divider = "-".repeat(32)
        val lines = items.joinToString("\n") { item ->
            item.quantity.toString().padEnd(2) +
                    item.name.take(17).padEnd(17) +
                    "%.2f".format(item.price).padStart(6) +
                    "%.2f".format(item.total).padStart(7)
        }
        "$header\n$divider\n$lines"
    }

    return buildString {
        append(alignLeft)
        append(
            """
------------------------------
FOOD APP
------------------------------
Order No : $orderNo
Customer : $customerName
Date     : ${SimpleDateFormat("dd-MM-yyyy HH:mm").format(Date(createdAt))}
------------------------------
$itemsBlock
------------------------------
GRAND TOTAL: %.2f
------------------------------
Thank You!
""".trimIndent().format(grandTotal)
        )
    }
}
