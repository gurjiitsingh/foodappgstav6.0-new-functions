package com.it10x.foodappgstav5_1.printer

import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity

object PrintOrderBuilder {

    fun build(
        master: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ): PrintOrder {

        val printItems = items.map { item ->
            PrintItem(
                name = item.name,
                quantity = item.quantity,
                price = item.finalPricePerItem,
                subtotal = item.finalTotal
            )
        }

        return PrintOrder(
            orderNo = master.srNo.toString(),
            customerName = "Walk-in",
            dateTime = master.createdAt.formatMillis(),
            items = printItems,
            itemTotal = master.itemTotal,
            deliveryFee = master.deliveryFee ?: 0.0,
            discount = master.discountTotal,
            tax = master.taxTotal,
            grandTotal = master.grandTotal
        )
    }

    private fun Long.formatMillis(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(this))
    }
}
