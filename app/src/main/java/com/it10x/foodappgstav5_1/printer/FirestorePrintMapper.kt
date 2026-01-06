package com.it10x.foodappgstav5_1.printer

import com.it10x.foodappgstav5_1.data.models.OrderMasterData
import com.it10x.foodappgstav5_1.data.models.OrderProductData
import com.it10x.foodappgstav5_1.data.models.formattedTime

object FirestorePrintMapper {

    fun map(
        order: OrderMasterData,
        items: List<OrderProductData>
    ): PrintOrder {

        val printItems = items.map { item ->
            PrintItem(
                name = item.name,
                quantity = item.quantity,
                price = toDouble(item.price),
                subtotal = toDouble(item.itemSubtotal)
            )
        }

        return PrintOrder(
            orderNo = order.srno.toString(),
            customerName = order.customerName.ifBlank { "Walk-in" },
            dateTime = order.formattedTime(),
            items = printItems,
            itemTotal = toDouble(order.itemTotal),
            deliveryFee = toDouble(order.deliveryFee),
            discount = calculateDiscount(order),
            tax = toDouble(order.taxAfterDiscount),
            grandTotal = toDouble(order.grandTotal)
        )
    }

    private fun toDouble(value: Any?): Double =
        when (value) {
            is Double -> value
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

    private fun calculateDiscount(order: OrderMasterData): Double {
        return toDouble(order.calculatedPickUpDiscountL) +
                toDouble(order.flatDiscount) +
                toDouble(order.calCouponDiscount)
    }
}
