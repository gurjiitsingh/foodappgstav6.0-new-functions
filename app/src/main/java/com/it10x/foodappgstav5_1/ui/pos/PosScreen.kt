package com.it10x.foodappgstav5_1.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.data.local.AppDatabaseProvider
import com.it10x.foodappgstav5_1.data.local.repository.CartRepository
import com.it10x.foodappgstav5_1.ui.cart.CartViewModel
import com.it10x.foodappgstav5_1.data.local.viewmodel.getParentProducts
import com.it10x.foodappgstav5_1.data.local.viewmodel.POSOrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    onOpenSettings: () -> Unit,
    ordersViewModel: POSOrdersViewModel
) {
    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600


    val categories by db.categoryDao().getAll().collectAsState(initial = emptyList())

    val allProducts by db.productDao().getAll().collectAsState(initial = emptyList())
    val parentProducts = remember(allProducts) {
        getParentProducts(allProducts)
    }

    var selectedCatId by remember { mutableStateOf<String?>(null) }

    val filteredProducts =
        if (selectedCatId == null) parentProducts
        else parentProducts.filter { it.categoryId == selectedCatId }

    val cartViewModel = remember {
        CartViewModel(CartRepository(db.cartDao()))
    }

    val cartItems by cartViewModel.cart.collectAsState(initial = emptyList())
    val cartCount = cartItems.sumOf { it.quantity }

    var showCartSheet by remember { mutableStateOf(false) }

    var orderType by remember { mutableStateOf("DINE_IN") }
    var tableNo by remember { mutableStateOf("1") }

      // ✅ PAYMENT TYPE STATE (DEFAULT CASH)
    var paymentType by remember { mutableStateOf("CASH") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PosBackground) // ✅ ONLY COLOR
    ) {

        Row(modifier = Modifier.fillMaxSize()) {

            // ---------- LEFT CATEGORY SIDEBAR ----------
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .background(PosSidebarBackground)
                    .padding(15.dp)   // ✅ SAME AS PRODUCTS
            ) {

                CategoryButton(
                    label = "All",
                    selected = selectedCatId == null
                ) { selectedCatId = null }

                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(categories) { c ->
                        CategoryButton(
                            label = c.name,
                            selected = selectedCatId == c.id
                        ) {
                            selectedCatId = c.id
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ---------- PRODUCTS ----------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {


                // ---------- ORDER CONTROLS ----------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // ORDER TYPE
                    OrderChip(
                        label = when (orderType) {
                            "DINE_IN" -> "Dine In"
                            "TAKEAWAY" -> "Takeaway"
                            else -> "Delivery"
                        },
                        selected = true,
                        onClick = {
                            orderType = when (orderType) {
                                "DINE_IN" -> "TAKEAWAY"
                                "TAKEAWAY" -> "DELIVERY"
                                else -> "DINE_IN"
                            }
                        }
                    )

                    // TABLE NO (ONLY FOR DINE IN)
                    if (orderType == "DINE_IN") {
                        OrderChip(
                            label = "Table $tableNo",
                            selected = true,
                            onClick = {
                                tableNo = if (tableNo == "1") "2" else "1" // simple toggle for now
                            }
                        )
                    }
                }



                ProductList(
                    filteredProducts = filteredProducts,
                    allProducts = allProducts,
                    cartViewModel = cartViewModel
                )
            }

            // ---------- CART (TABLET ONLY) ----------
            if (!isPhone) {
                RightPanel(

                    cartViewModel = cartViewModel,
                    ordersViewModel = ordersViewModel,
                    orderType = orderType,
                    tableNo = tableNo,
                    paymentType = paymentType,
                    onPaymentChange = { paymentType = it }, // ✅
                    onOrderPlaced = { }


                )
            }
        }

        // ---------- MOBILE CART FAB ----------
        if (isPhone && cartCount > 0) {
            FloatingCartButton(
                count = cartCount,
                onClick = { showCartSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    if (isPhone && showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false }
        ) {
            RightPanel(
                cartViewModel = cartViewModel,
                ordersViewModel = ordersViewModel,
                orderType = orderType,
                tableNo = tableNo,
                paymentType = paymentType,
                onPaymentChange = { paymentType = it }, // ✅
                onOrderPlaced = {
                    showCartSheet = false
                }
            )
        }
    }
}

// ================= CATEGORY BUTTON =================

@Composable
fun CategoryButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) PosGreen else Color.White, // ✅ ONLY COLOR
        shape = MaterialTheme.shapes.small,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
fun FloatingCartButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {

        FloatingActionButton(
            onClick = onClick,
            containerColor = PosGreen // ✅ ONLY COLOR
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.White
            )
        }

        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(22.dp)
                    .background(Color.Red, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


@Composable
fun OrderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) PosGreen else Color.White,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
