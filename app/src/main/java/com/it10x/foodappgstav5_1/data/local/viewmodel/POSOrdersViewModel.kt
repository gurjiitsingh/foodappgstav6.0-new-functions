package com.it10x.foodappgstav5_1.data.local.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
import com.it10x.foodappgstav5_1.data.local.repository.POSOrdersRepository
import com.it10x.foodappgstav5_1.printer.PrinterManager
import com.it10x.foodappgstav5_1.data.PrinterRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class POSOrdersViewModel(
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    fun getOrderMaster(orderId: String): Flow<PosOrderMasterEntity?> = flow {
        emit(repository.getOrderById(orderId))
    }
    val orders: StateFlow<List<PosOrderMasterEntity>> get() = _orders
    private val _orders = MutableStateFlow<List<PosOrderMasterEntity>>(emptyList())

    val loading: StateFlow<Boolean> get() = _loading
    private val _loading = MutableStateFlow(false)

    val pageIndex = MutableStateFlow(0)
    private val limit = 10
    private val srNoCounter = AtomicInteger(1) // Daily running number

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

            // 1️⃣ Fetch cart (first emission)
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
            // Billing
            val billingReceipt = buildBillingReceipt(order, cartItems)
            printerManager.printText(PrinterRole.BILLING, billingReceipt) { success ->
                Log.d("POS_PRINT", "Billing printed: $success")
            }

            // Delay 10 sec
            kotlinx.coroutines.delay(10_000)

            // Kitchen
            val kitchenReceipt = buildKitchenReceipt(order, cartItems)
            printerManager.printText(PrinterRole.KITCHEN, kitchenReceipt) { success ->
                Log.d("POS_PRINT", "Kitchen printed: $success")
            }
        }
    }

    // -------------------------
    // RECEIPT BUILDERS
    // -------------------------
    private fun buildBillingReceipt(order: PosOrderMasterEntity, items: List<PosCartEntity>): String {
        val alignLeft = "\u001B\u0061\u0000"
        val itemsBlock = if (items.isEmpty()) "No items found" else {
            val header =
                "QTY".padEnd(5) + "ITEM".take(12).padEnd(12) + "PRICE".padStart(7) + "TOTAL".padStart(6)
            val divider = "-".repeat(32)
            val lines = items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd(2)
                val name = item.name.take(17).padEnd(17)
                val price = formatAmount(item.basePrice).padStart(6)
                val total = formatAmount(item.basePrice * item.quantity).padStart(7)
                qty + name + price + total
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
Order No : ${order.srNo}
Customer : Walk-in
Date     : ${java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(order.createdAt)}
------------------------------
$itemsBlock
------------------------------
${totalLine("Item Total", order.itemTotal)}
${totalLine("Discount", order.discountTotal)}
${totalLine("Tax", order.taxTotal)}
------------------------------
${totalLine("GRAND TOTAL", order.grandTotal)}
------------------------------
Thank You!
""".trimIndent()
            )
        }
    }

    private fun buildKitchenReceipt(order: PosOrderMasterEntity, items: List<PosCartEntity>): String {
        val alignLeft = "\u001B\u0061\u0000"
        val itemsBlock = if (items.isEmpty()) "No items" else items.joinToString("\n") { "${it.quantity.toString().padEnd(3)} ${it.name}" }
        return buildString {
            append(alignLeft)
            append(
                """
******** KITCHEN ********
Order No : ${order.srNo}
------------------------
$itemsBlock
------------------------
""".trimIndent()
            )
        }
    }

    // -------------------------
    // HELPERS
    // -------------------------
    private fun totalLine(label: String, value: Double): String {
        if (value == 0.0) return ""
        val left = label.padEnd(14)
        val right = "%.2f".format(value).padStart(18)
        return left + right
    }

    private fun formatAmount(value: Double?): String = "%.2f".format(value ?: 0.0)


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
}
