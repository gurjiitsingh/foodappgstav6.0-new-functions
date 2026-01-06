package com.it10x.foodappgstav5_1.ui.orders.local

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
import com.it10x.foodappgstav5_1.data.local.viewmodel.POSOrdersViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LocalOrdersScreen(
    viewModel: POSOrdersViewModel,
    navController: NavController
) {
    val orders by viewModel.orders.collectAsState()

    // Load first page when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadFirstPage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Local POS Orders",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No local orders yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE0E0E0))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                Text("Type", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                Text("Table", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                Text("Payment", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                Text("Status", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                Text("Date", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Notes", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                Text("Print", fontWeight = FontWeight.Bold)
            }

            Divider(color = Color.Gray, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders, key = { it.id }) { order ->
                    LocalOrderRow(
                        order = order,
                        onClick = {
                            navController.navigate("local_order_detail/${order.id}")
                        },
                        onPrint = {
                            viewModel.printOrder(order.id)
                        }
                    )
                    Divider(color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

@Composable
private fun LocalOrderRow(
    order: PosOrderMasterEntity,
    onClick: () -> Unit,
    onPrint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(order.srNo.toString(), modifier = Modifier.width(60.dp))
        Text(order.orderType, modifier = Modifier.width(80.dp))
        Text(order.tableNo ?: "-", modifier = Modifier.width(60.dp))
        Text("${order.paymentType} (${order.paymentStatus})", modifier = Modifier.width(100.dp))
        Text(order.orderStatus, modifier = Modifier.width(100.dp))
        Text("₹${"%.2f".format(order.grandTotal)}", modifier = Modifier.width(80.dp))
        Text(formatDate(order.createdAt), modifier = Modifier.weight(1f))
        Text(order.notes ?: "-", modifier = Modifier.width(100.dp))
        TextButton(onClick = onPrint) {
            Text("Print")
        }
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
