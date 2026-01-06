package com.it10x.foodappgstav5_1.printer

import android.util.Log
import com.it10x.foodappgstav5_1.data.models.OrderMasterData
import com.it10x.foodappgstav5_1.data.repository.OrdersRepository
import com.it10x.foodappgstav5_1.viewmodel.OrdersViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val printingOrders = mutableSetOf<String>()
class AutoPrintManager(
    private val ordersViewModel: OrdersViewModel,
    private val ordersRepository: OrdersRepository

) {

    fun onNewOrder(order: OrderMasterData) {

        Log.e("AUTO_PRINT", "🔥 onNewOrder called srno=${order.srno}")

        // ⛔ Already printed in DB
        if (order.printed == true) {
            Log.d("AUTO_PRINT", "⛔ Already printed srno=${order.srno}")
            return
        }

        // ⛔ Already printing in this session
        synchronized(printingOrders) {
            if (printingOrders.contains(order.id)) {
                Log.w("AUTO_PRINT", "⛔ Duplicate call ignored srno=${order.srno}")
                return
            }
            printingOrders.add(order.id)
        }

        CoroutineScope(Dispatchers.IO).launch {

            try {
                Log.d("AUTO_PRINT", "⏳ Waiting for items srno=${order.srno}")

                var itemsReady = false

                repeat(10) { attempt ->
                    val items = ordersRepository.getOrderProducts(order.id)
                    if (items.isNotEmpty()) {
                        Log.d("AUTO_PRINT", "✅ Items found at attempt=$attempt")
                        itemsReady = true
                        return@repeat
                    }
                    delay(1000)
                }

                if (!itemsReady) {
                    Log.e("AUTO_PRINT", "❌ No items found srno=${order.srno}")
                    return@launch
                }

                // 🖨 PRINT ONCE
                Log.e("AUTO_PRINT", "🖨 Printing srno=${order.srno}")
                ordersViewModel.printOrder(order)

                // ✅ MARK PRINTED IN DB
                ordersRepository.markOrderAsPrinted(order.id)

                Log.e("AUTO_PRINT", "✅ Auto print DONE srno=${order.srno}")

            } catch (e: Exception) {
                Log.e("AUTO_PRINT", "❌ Auto print failed", e)

            } finally {
                // 🔓 RELEASE LOCK
                synchronized(printingOrders) {
                    printingOrders.remove(order.id)
                }
            }
        }
    }

}
