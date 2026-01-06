package com.it10x.foodappgstav5_1.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.data.models.OrderMasterData
import com.it10x.foodappgstav5_1.data.models.createdAtMillis
import com.it10x.foodappgstav5_1.printer.PrinterManager
import com.it10x.foodappgstav5_1.viewmodel.OrdersViewModel
import com.it10x.foodappgstav5_1.viewmodel.RealtimeOrdersViewModel

@Composable
fun OrdersScreen(
    printerManager: PrinterManager,
    ordersViewModel: OrdersViewModel,
    realtimeOrdersViewModel: RealtimeOrdersViewModel
) {

    // ----------------------------------
    // LOAD DATA ONCE
    // ----------------------------------
    LaunchedEffect(Unit) {
        ordersViewModel.loadFirstPage()
    }

    // ----------------------------------
    // STATE
    // ----------------------------------
    val pagedOrders by ordersViewModel.orders.collectAsState()
    val realtimeOrders by realtimeOrdersViewModel.realtimeOrders.collectAsState()
    val loading by ordersViewModel.loading.collectAsState()
    val pageIndex by ordersViewModel.pageIndex.collectAsState()

    // ----------------------------------
    // MERGE REALTIME + PAGED (POS STYLE)
    // ----------------------------------
    val combinedOrders = remember(realtimeOrders, pagedOrders, pageIndex) {
        val isFirstPage = pageIndex == 0

        val list: List<OrderMasterData> =
            if (isFirstPage) {
                val realtimeIds = realtimeOrders.map { it.id }.toSet()
                realtimeOrders + pagedOrders.filter { it.id !in realtimeIds }
            } else {
                pagedOrders
            }

        // newest orders first
        list.sortedByDescending { it.createdAtMillis() }
    }

    // ----------------------------------
    // UI
    // ----------------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text(
            text = "POS Orders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        when {
            loading && combinedOrders.isEmpty() -> {
                Text("Loading orders...")
            }

            combinedOrders.isEmpty() -> {
                Text("No orders found")
            }

            else -> {
                PosOrderTableHeader()

                LazyColumn {
                    items(combinedOrders, key = { it.id }) { order ->
                        PosOrderTableRow(
                            order = order,
                            onOrderClick = {
                                println("OPEN ORDER DETAIL: ${order.srno}")
                            },
                            onPrintClick = {
                                ordersViewModel.printOrder(order)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // PAGINATION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { ordersViewModel.loadPrevPage() },
                        enabled = !loading
                    ) {
                        Text("← Previous")
                    }

                    Button(
                        onClick = { ordersViewModel.loadNextPage() },
                        enabled = !loading
                    ) {
                        Text("Next →")
                    }
                }
            }
        }
    }
}
