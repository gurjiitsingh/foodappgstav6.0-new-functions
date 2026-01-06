package com.it10x.foodappgstav5_1.ui.orders.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.PrinterRole
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
import com.it10x.foodappgstav5_1.data.local.viewmodel.POSOrdersViewModel
import com.it10x.foodappgstav5_1.printer.PrinterManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.it10x.foodappgstav5_1.printer.PosReceiptBuilder
class LocalOrderDetailViewModel(
    private val orderId: String,
    private val repository: POSOrdersViewModel,
    private val printerManager: PrinterManager
) : ViewModel() {

    // 🗂 ORDER HEADER
    val orderInfo: StateFlow<PosOrderMasterEntity?> =
        repository.orders
            .map { list -> list.firstOrNull { it.id == orderId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // 🧾 ORDER ITEMS
    val products: StateFlow<List<PosOrderItemEntity>> =
        repository.getOrderProducts(orderId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // 💰 TOTALS
    val subtotal = products
        .map { it.sumOf { p -> p.itemSubtotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val taxTotal = products
        .map { it.sumOf { p -> p.taxTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val grandTotal = products
        .map { it.sumOf { p -> p.finalTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // 🖨 MANUAL PRINT ONLY (NO AUTO-PRINT)
    fun printOrder() {
        viewModelScope.launch {

            val order = orderInfo.value ?: return@launch
            val items = products.value
            if (items.isEmpty()) return@launch

            val receipt = PosReceiptBuilder.buildBilling(order, items)

            printerManager.printText(
                PrinterRole.BILLING,
                receipt
            )
        }
    }

}
