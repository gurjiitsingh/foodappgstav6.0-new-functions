package com.it10x.foodappgstav5_1.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import com.it10x.foodappgstav5_1.data.local.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: CartRepository
) : ViewModel() {

    val cart = repository.observeCart()

    fun addToCart(product: PosCartEntity) {
        viewModelScope.launch {
            repository.addToCart(product)
        }
    }

    fun increase(item: PosCartEntity) {
        viewModelScope.launch {
            repository.addToCart(item)
        }
    }

    fun decrease(productId: String) {
        viewModelScope.launch {
            repository.decrease(productId)
        }
    }

    fun clear() {
        viewModelScope.launch {
            repository.clear()
        }
    }
}
