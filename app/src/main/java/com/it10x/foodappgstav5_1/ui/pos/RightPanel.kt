package com.it10x.foodappgstav5_1.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import com.it10x.foodappgstav5_1.ui.cart.CartViewModel
import com.it10x.foodappgstav5_1.data.local.viewmodel.POSOrdersViewModel

import android.provider.Settings
import android.os.Build
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav5_1.BuildConfig


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightPanel(
    cartViewModel: CartViewModel,
    ordersViewModel: POSOrdersViewModel,
    orderType: String,
    tableNo: String,
    paymentType: String,
    onPaymentChange: (String) -> Unit,
    onOrderPlaced: () -> Unit
) {
    val context = LocalContext.current       // <-- ✔ valid place


    val cartItems: List<PosCartEntity> by
    cartViewModel.cart.collectAsState(initial = emptyList())


    Column(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .fillMaxHeight()
            .background(Color(0xFFF7F7F7))
            .padding(12.dp)
            .padding(
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
    ) {

//        Text("Cart", style = MaterialTheme.typography.titleMedium)
//        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(cartItems, key = { it.productId }) { item ->
                CartRow(item, cartViewModel)
            }
        }

        Divider(Modifier.padding(vertical = 8.dp))

        Text(
            text = "Payment",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {

            SegmentedButton(
                selected = paymentType == "CASH",
                onClick = { onPaymentChange("CASH") },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color(0xFFF97316),   // 🟠 orange-500
                    activeContentColor = Color.White,
                    inactiveContainerColor = Color(0xFF374151), // ⚫ dark gray-700
                    inactiveContentColor = Color.White
                )
            ) {
                Text("CASH", fontWeight = FontWeight.Bold)
            }

            SegmentedButton(
                selected = paymentType == "UPI",
                onClick = { onPaymentChange("UPI") },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color(0xFFF97316),
                    activeContentColor = Color.White,
                    inactiveContainerColor = Color(0xFF374151),
                    inactiveContentColor = Color.White
                )
            ) {
                Text("UPI")
            }

            SegmentedButton(
                selected = paymentType == "CARD",
                onClick = { onPaymentChange("CARD") },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color(0xFFF97316),
                    activeContentColor = Color.White,
                    inactiveContainerColor = Color(0xFF374151),
                    inactiveContentColor = Color.White
                )
            ) {
                Text("CARD")
            }
        }


        OrderSummaryScreen(cartViewModel)

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            onClick = {
                val deviceId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )

                ordersViewModel.placeOrder(
                   // cartItems = cartItems,
                    orderType = orderType,
                    tableNo = tableNo,
                    paymentType = paymentType,
                    deviceId = deviceId,
                    deviceName = Build.MODEL ?: "Unknown Device",
                    appVersion = BuildConfig.VERSION_NAME
                )

                onOrderPlaced()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A), // ✅ GREEN-600
                contentColor = Color.White          // ✅ WHITE TEXT
            )
        ) {
            Text(
                text = "Place Order",
                style = MaterialTheme.typography.titleMedium
            )
        }



    }
}






@Composable
fun CartRow(
    item: PosCartEntity,
    cartViewModel: CartViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White   // ✅ WHITE CARD
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ---------- ITEM INFO ----------
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₹${item.basePrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // ---------- QUANTITY CONTROLS ----------
// ---------- QUANTITY CONTROLS ----------
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ➖ MINUS BUTTON
                IconButton(
                    onClick = { cartViewModel.decrease(item.productId) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFFDC2626), // 🔴 red-600
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Text(
                        text = "−",
                        color = Color.White,
                        fontSize = 20.sp,              // ⬆ bigger
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                // ➕ PLUS BUTTON
                IconButton(
                    onClick = { cartViewModel.addToCart(item) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFF16A34A), // 🟢 green-600
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Text(
                        text = "+",
                        color = Color.White,
                        fontSize = 20.sp,              // ⬆ bigger
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        }
    }
}












