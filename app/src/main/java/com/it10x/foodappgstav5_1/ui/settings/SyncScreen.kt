package com.it10x.foodappgstav5_1.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.it10x.foodappgstav5_1.viewmodel.ProductSyncViewModel

@Composable
fun SyncScreen(
    navController: NavController,
    onBack: () -> Unit = {}
) {
    val vm: ProductSyncViewModel = viewModel()

    val syncing by vm.syncing.collectAsState()
    val status by vm.status.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ===== HEADER =====
        Text(
            text = "Data Sync & Local Data",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ===== SYNC BUTTON =====
        Button(
            enabled = !syncing,
            onClick = { vm.syncAll() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (syncing) "Syncing…" else "Sync Now")
        }

        // ===== STATUS =====
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium
        )

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // ===== LOCAL DATA VIEW =====
        Text(
            text = "Local Data",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(
            onClick = { navController.navigate("local_products") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Local Products")
        }

        OutlinedButton(
            onClick = { navController.navigate("local_categories") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Local Categories")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== BACK BUTTON =====
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
