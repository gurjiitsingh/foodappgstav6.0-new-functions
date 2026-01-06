package com.it10x.foodappgstav5_1.data.local.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
import com.it10x.foodappgstav5_1.data.local.repository.POSOrdersRepository
import com.it10x.foodappgstav5_1.printer.PrintOrderBuilder
import com.it10x.foodappgstav5_1.printer.PrinterManager
import com.it10x.foodappgstav5_1.data.PrinterRole
import com.it10x.foodappgstav5_1.printer.ReceiptFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class POSOrdersViewModel(
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    val orders: StateFlow<List<PosOrderMasterEntity>> get() = _orders
    private val _orders = MutableStateFlow<List<PosOrderMasterEntity>>(emptyList())

    val loading: StateFlow<Boolean> get() = _loading
    private val _loading = MutableStateFlow(false)

    val pageIndex = MutableStateFlow(0)
    private val limit = 10
    private val srNoCounter = AtomicInteger(1)

    // -------------------------
    // PAGINATION
    // -------------------------
    fun loadFirstPage() = loadOrders(0)
    fun loadNextPage() = loadOrders(pageIndex.value + 1)
    fun loadPrevPage() {
        val prev = if (pageIndex.value > 0) pageIndex.value - 1 else 0
        loadOrders(prev)
    }

    private fun loadOrders(page: Int) {
        viewModelScope.launch {
            _loading.value = true
            pageIndex.value = page
            val offset = page * limit
            val pagedOrders = repository.getPagedOrders(limit, offset)
            _orders.value = pagedOrders.sortedByDescending { it.createdAt }
            _loading.value = false
        }
    }

    // -------------------------
    // PLACE ORDER + AUTO PRINT
    // -------------------------
    fun placeOrder(
        orderType: String,
        tableNo: String?,
        paymentType: String,
        deviceId: String,
        deviceName: String?,
        appVersion: String?
    ) {
        viewModelScope.launch {
            _loading.value = true

            val cartList: List<PosCartEntity> = repository.getCartItems().first()
            if (cartList.isEmpty()) {
                Log.e("POS", "Cart is empty")
                _loading.value = false
                return@launch
            }

            try {
                val now = System.currentTimeMillis()
                val orderId = UUID.randomUUID().toString()
                val srNo = srNoCounter.getAndIncrement()

                // 2️⃣ Calculate totals
                val itemTotal = OrderCalculator.subtotal(cartList)
                val taxTotal = OrderCalculator.tax(cartList)
                val grandTotal = OrderCalculator.grandTotal(cartList)

                // 3️⃣ Create master entity
                val orderMaster = PosOrderMasterEntity(
                    id = orderId,
                    srNo = srNo,
                    orderType = orderType,
                    tableNo = tableNo,
                    itemTotal = itemTotal,
                    taxTotal = taxTotal,
                    discountTotal = 0.0,
                    grandTotal = grandTotal,
                    paymentType = paymentType,
                    paymentStatus = "PAID",
                    orderStatus = "NEW",
                    source = "POS",
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = appVersion,
                    createdAt = now,
                    updatedAt = now,
                    syncStatus = "PENDING",
                    lastSyncedAt = null,
                    notes = null
                )

                repository.insertOrder(orderMaster, cartList)

                // 4️⃣ Auto-print
                autoPrint(orderMaster, cartList)

            } catch (e: Exception) {
                Log.e("POS", "Error placing order: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // -------------------------
    // AUTO PRINT
    // -------------------------
    private fun autoPrint(order: PosOrderMasterEntity, cartItems: List<PosCartEntity>) {
        viewModelScope.launch {
            // Convert PosCartEntity -> PosOrderItemEntity
            val items = cartItems.map { cart ->
                PosOrderItemEntity(
                    id = UUID.randomUUID().toString(),
                    orderMasterId = order.id,
                    productId = cart.productId,
                    categoryId = cart.categoryId,
                    parentId = cart.parentId,
                    isVariant = cart.isVariant,
                    name = cart.name,
                    quantity = cart.quantity,
                    basePrice = cart.basePrice,
                    itemSubtotal = cart.basePrice * cart.quantity,
                    taxRate = cart.taxRate,
                    taxType = cart.taxType,
                    taxAmountPerItem = 0.0,
                    taxTotal = 0.0,
                    finalPricePerItem = cart.basePrice,
                    finalTotal = cart.basePrice * cart.quantity,
                    createdAt = System.currentTimeMillis()
                )
            }

            printOrderStandard(order, items)
        }
    }

    // -------------------------
    // PRINT ORDERS (AUTO + MANUAL)
    // -------------------------
    private fun printOrderStandard(order: PosOrderMasterEntity, items: List<PosOrderItemEntity>) {
        val printOrder = PrintOrderBuilder.build(order, items)

        // Print Billing
        printerManager.printText(PrinterRole.BILLING,
            ReceiptFormatter.billing(printOrder)) { success ->
            Log.d("POS_PRINT", "Billing printed: $success for order ${order.srNo}")
        }

        // Small delay then Kitchen
        viewModelScope.launch {
            kotlinx.coroutines.delay(150)
            printerManager.printText(PrinterRole.KITCHEN,
                ReceiptFormatter.kitchen(printOrder)) { success ->
                Log.d("POS_PRINT", "Kitchen printed: $success for order ${order.srNo}")
            }
        }
    }

    // -------------------------
    // ORDER DETAILS
    // -------------------------
    fun getOrderProducts(orderId: String): StateFlow<List<PosOrderItemEntity>> {
        val flow = MutableStateFlow<List<PosOrderItemEntity>>(emptyList())
        viewModelScope.launch {
            flow.value = repository.getOrderItems(orderId)
        }
        return flow
    }

    // -------------------------
    // MANUAL PRINT OLD ORDER
    // -------------------------
    fun printOrder(orderId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {

                Log.d("POS_PRINT", "Print requested for orderId=$orderId")

                val order = repository.getOrderById(orderId)
                if (order == null) {
                    Log.e("POS_PRINT", "Order NOT FOUND for orderId=$orderId")
                    return@launch
                }

                val items = repository.getOrderItems(orderId)
                if (items.isEmpty()) {
                    Log.e(
                        "POS_PRINT",
                        "Order items EMPTY for orderId=$orderId (items=${items.size})"
                    )
                    return@launch
                }

                Log.d(
                    "POS_PRINT",
                    "Printing order srNo=${order.srNo}, items=${items.size}"
                )

                printOrderStandard(order, items)

            } catch (e: Exception) {
                Log.e("POS_PRINT", "Exception while printing order", e)
            } finally {
                _loading.value = false
            }
        }
    }
}
