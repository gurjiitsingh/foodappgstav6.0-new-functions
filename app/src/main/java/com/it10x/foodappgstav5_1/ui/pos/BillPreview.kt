package com.it10x.foodappgstav5_1.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.it10x.foodappgstav5_1.ui.cart.CartViewModel


@Composable
fun BillPreview(
    cartViewModel: CartViewModel
) {
    Button(
        onClick = {
            // TODO: generate bill / print
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Place Order")
    }
}
