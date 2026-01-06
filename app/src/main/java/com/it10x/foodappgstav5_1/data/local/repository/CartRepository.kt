package com.it10x.foodappgstav5_1.data.local.repository

import com.it10x.foodappgstav5_1.data.local.dao.CartDao
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

class CartRepository(
    private val dao: CartDao
) {

    // ---------- OBSERVE CART ----------
    fun observeCart(): Flow<List<PosCartEntity>> =
        dao.getCart()

    // ---------- ADD ----------
    suspend fun addToCart(product: PosCartEntity) {
        val existing = dao.getById(product.productId)

        if (existing == null) {
            dao.insert(
                product.copy(quantity = 1)
            )
        } else {
            dao.update(
                existing.copy(quantity = existing.quantity + 1)
            )
        }
    }

    // ---------- REMOVE SINGLE ROW ----------
    suspend fun remove(item: PosCartEntity) {
        dao.delete(item)
    }

    // ---------- CLEAR FULL CART ----------
    suspend fun clear() {
        dao.clearCart()
    }

    // ---------- DECREASE (ONE BY ONE) ----------
    suspend fun decrease(productId: String) {
        val existing = dao.getById(productId) ?: return

        if (existing.quantity > 1) {
            // 🔥 decrease quantity
            dao.update(
                existing.copy(quantity = existing.quantity - 1)
            )
        } else {
            // 🔥 remove only that product
            dao.delete(existing)
        }
    }
}

