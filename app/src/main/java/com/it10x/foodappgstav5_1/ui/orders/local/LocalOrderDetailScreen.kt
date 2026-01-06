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
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LocalOrderDetailScreen(
    viewModel: LocalOrderDetailViewModel,
    onBack: () -> Unit
) {
    val order by viewModel.orderInfo.collectAsState()
    val products by viewModel.products.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val tax by viewModel.taxTotal.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔙 HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Order Details", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(12.dp))

        // 🗂 ORDER INFO
        order?.let { o ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {

                    // Top row: order number + date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order #${o.srNo}", fontWeight = FontWeight.Bold)
                        Text(formatDate(o.createdAt), color = Color.Gray)
                    }

                    Spacer(Modifier.height(4.dp))

                    // Order type and table
                    Row {
                        Text("Type: ${o.orderType}", modifier = Modifier.padding(end = 12.dp))
                        if (!o.tableNo.isNullOrEmpty()) Text("Table: ${o.tableNo}")
                    }

                    // Payment
                    Text("Payment: ${o.paymentType} (${o.paymentStatus})")

                    // Status
                    Text("Status: ${o.orderStatus}")

                    // Notes if available
                    if (!o.notes.isNullOrEmpty()) Text("Notes: ${o.notes}", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // 🧾 ITEMS
        Text("Items", style = MaterialTheme.typography.titleMedium)
        Divider(Modifier.padding(vertical = 4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(products, key = { it.id }) { item ->
                OrderProductRow(item)
                Divider(color = Color(0xFFE0E0E0))
            }
        }

        Spacer(Modifier.height(12.dp))

        // 💰 TOTALS
        OrderTotals(subtotal = subtotal, tax = tax, grandTotal = grandTotal)

        Spacer(Modifier.height(12.dp))

        // 🖨 PRINT BUTTON
        Button(
            onClick = { viewModel.printOrder() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Print Order")
        }
    }
}

@Composable
fun OrderProductRow(item: PosOrderItemEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(item.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${item.quantity} × ₹${item.basePrice}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Text("₹${"%.2f".format(item.finalTotal)}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun OrderTotals(subtotal: Double, tax: Double, grandTotal: Double) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TotalRow("Subtotal", subtotal)
        TotalRow("GST", tax)
        Divider(Modifier.padding(vertical = 4.dp))
        TotalRow("Grand Total", grandTotal, bold = true)
    }
}

@Composable
fun TotalRow(label: String, value: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("₹${"%.2f".format(value)}", fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
