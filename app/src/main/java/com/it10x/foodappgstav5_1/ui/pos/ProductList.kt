package com.it10x.foodappgstav5_1.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav5_1.data.local.entities.ProductEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import com.it10x.foodappgstav5_1.ui.cart.CartViewModel

@Composable
fun ProductList(
    filteredProducts: List<ProductEntity>,
    allProducts: List<ProductEntity>,
    cartViewModel: CartViewModel
) {

    LaunchedEffect(allProducts) {
        allProducts.forEach {
            android.util.Log.d(
                "PRICE_DEBUG",
                "name=${it.name}, type=${it.type}, parentId=${it.parentId}, price=${it.price}"
            )
        }
    }

    LazyColumn {

        items(filteredProducts, key = { it.id }) { product ->

//            android.util.Log.d(
//               "PRICE_DEBUG",
//                "name=${product.name}, type=${product.type}, parentId=${product.parentId},haseVariant=${product.hasVariants}, price=${product.price}"
//           )

            // ❌ never render variants as parent rows
            if (product.type == "variant") return@items

            // ⭐ fetch variants for this product
            val variants = remember(product.id, allProducts) {



                allProducts.filter {
                    it.parentId == product.id && it.type == "variant"
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        1.dp,
                        Color(0xFFE0E0E0),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {

                Column(Modifier.padding(10.dp)) {

                    // ---------- PRODUCT NAME - DONT ADD HERE ----------
//                    Text(
//                        text = product.name,
//                        style = MaterialTheme.typography.titleMedium
//                    )

                  //  Spacer(Modifier.height(8.dp))


                    // ---------- PRODUCT & VARIANTS ROW ----------
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {


                        // =========================
                        // 1️⃣ IF PRODUCT HAS VARIANTS
                        // =========================
                        if (variants.isNotEmpty()) {

                            items(variants, key = { it.id }) { v ->

                                VariantCard(v, cartViewModel)
                            }
                        }


                        // =========================
                        // 2️⃣ IF PRODUCT HAS NO VARIANTS
                        // =========================
                        if (product.parentId == null) {
//                            android.util.Log.d(
//                                "PRICE_DEBUG",
//                                "name=${product.name}, type=${product.type}, parentId=${product.parentId},haseVariant=${product.hasVariants}, price=${product.price}"
//                            )
                            item {
                                ParentProductCard(product,cartViewModel)
                            }
//                            item {
//
//                                VariantCard(
//                                    product.copy(parentId = null),   // treat as product
//                                    cartViewModel
//                                )
//                            }
                        }
                    }
                }
            }
        }
    }
}




@Composable
private fun ParentProductCard(
    product: ProductEntity,
    cartViewModel: CartViewModel
) {

    Card(
        modifier = Modifier
            .width(160.dp)
            .border(
                1.dp,
                Color(0xFFE0E0E0),
                shape = MaterialTheme.shapes.small
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(7.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ⭐ PRODUCT NAME
            Text(product.name)

            // ⭐ PRODUCT PRICE
            Text(
                "₹${product.price}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ➖ decrease
                IconButton(
                    onClick = { cartViewModel.decrease(product.id) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                ) {
                    Text("−", color = Color.White, fontSize = 18.sp)
                }

                // ➕ add to cart
                IconButton(
                    onClick = {
                        cartViewModel.addToCart(
                            PosCartEntity(
                                productId = product.id,
                                name = product.name,
                                basePrice = product.price,
                                quantity = 1,
                                taxRate = product.taxRate ?: 0.0,
                                taxType = product.taxType ?: "inclusive",
                                parentId = null,
                                isVariant = false,
                                categoryId = product.categoryId
                            )
                        )
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                ) {
                    Text("+", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun VariantCard(
    product: ProductEntity,
    cartViewModel: CartViewModel
) {

    Card(
        modifier = Modifier
            .width(160.dp)
            .border(
                1.dp,
                Color(0xFFE0E0E0),
                shape = MaterialTheme.shapes.small
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(7.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(product.name)

            Text(
                "₹${product.price}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ➖
                IconButton(
                    onClick = { cartViewModel.decrease(product.id) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                ) {
                    Text("−", color = Color.White, fontSize = 18.sp)
                }

                // ➕
                IconButton(
                    onClick = {
                        cartViewModel.addToCart(
                            PosCartEntity(
                                productId = product.id,
                                name = product.name,
                                basePrice = product.price,
                                quantity = 1,
                                taxRate = product.taxRate ?: 0.0,
                                taxType = product.taxType ?: "inclusive",

                                parentId = product.parentId,
                                isVariant = product.parentId != null,

                                categoryId = product.categoryId
                            )
                        )
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                ) {
                    Text("+", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}
