package com.it10x.foodappgstav5_1.data.local.repository

import com.it10x.foodappgstav5_1.data.local.dao.CartDao
import com.it10x.foodappgstav5_1.data.local.dao.OrderMasterDao
import com.it10x.foodappgstav5_1.data.local.dao.OrderProductDao
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class POSOrdersRepository(
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val cartDao: CartDao
) {

    // -------------------------
// ORDER DETAILS
// -------------------------
    suspend fun getOrderById(orderId: String): PosOrderMasterEntity? {
        return orderMasterDao.getOrderById(orderId)
    }

    // -------------------------
    // CART
    // -------------------------
    fun getCartItems(): Flow<List<PosCartEntity>> = cartDao.getCart()

    suspend fun clearCart() = cartDao.clearCart()

    // -------------------------
    // ORDERS
    // -------------------------
    suspend fun getPagedOrders(limit: Int, offset: Int): List<PosOrderMasterEntity> {
        return orderMasterDao.getPagedOrders(limit, offset)
    }

    suspend fun getOrderItems(orderId: String): List<PosOrderItemEntity> {
        return orderProductDao.getByOrderIdSync(orderId)
    }

    // -------------------------
    // INSERT ORDER
    // -------------------------
    suspend fun insertOrder(orderMaster: PosOrderMasterEntity, cartItems: List<PosCartEntity>) {
        // 1️⃣ Insert order master
        orderMasterDao.insert(orderMaster)

        val now = System.currentTimeMillis()

        // 2️⃣ Map cart items → order items
        val orderItems = cartItems.map { cart ->
            val taxRate = cart.taxRate ?: 0.0
            val taxType = cart.taxType ?: "inclusive"
            val itemSubtotal = cart.basePrice * cart.quantity

            val taxAmount = if (taxType == "exclusive") cart.basePrice * (taxRate / 100) else 0.0
            val finalPricePerItem = cart.basePrice + taxAmount
            val finalTotal = finalPricePerItem * cart.quantity

            PosOrderItemEntity(
                id = UUID.randomUUID().toString(),
                orderMasterId = orderMaster.id,
                productId = cart.productId,
                name = cart.name,
                categoryId = cart.categoryId,
                parentId = cart.parentId,
                isVariant = cart.parentId != null,
                basePrice = cart.basePrice,
                quantity = cart.quantity,
                itemSubtotal = itemSubtotal,
                taxRate = taxRate,
                taxType = taxType,
                taxAmountPerItem = taxAmount,
                taxTotal = taxAmount * cart.quantity,
                finalPricePerItem = finalPricePerItem,
                finalTotal = finalTotal,
                source = "POS",
                createdAt = now
            )
        }

        // 3️⃣ Insert order items
        orderProductDao.insertAll(orderItems)

        // 4️⃣ Clear cart
        clearCart()
    }
}
