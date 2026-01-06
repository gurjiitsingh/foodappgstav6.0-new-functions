package com.it10x.foodappgstav5_1.service

import android.app.*
import android.content.*
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.it10x.foodappgstav5_1.R
import com.it10x.foodappgstav5_1.viewmodel.RealtimeOrdersViewModel
import com.it10x.foodappgstav5_1.printer.AutoPrintManager
import com.it10x.foodappgstav5_1.viewmodel.OrdersViewModel
import com.it10x.foodappgstav5_1.data.repository.OrdersRepository
import com.it10x.foodappgstav5_1.printer.PrinterManager

class OrderListenerService : Service() {

    private lateinit var vm: RealtimeOrdersViewModel

    // 🔔 Receiver to stop ringtone
    private val stopSoundReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        android.util.Log.e("STOP_SOUND", "Broadcast received")

        vm.stopRingtone()

        android.util.Log.e("STOP_SOUND", "Ringtone STOP requested")
    }
}

    override fun onCreate() {
        super.onCreate()

        // --- Printer Manager ---
        val printerManager = PrinterManager(this)

        // --- Orders VM (for printing) ---
        val ordersVM = OrdersViewModel(printerManager)

        // --- Repo ---
        val ordersRepo = OrdersRepository()

        // --- Auto-print manager ---
        val autoPrint = AutoPrintManager(
            ordersViewModel = ordersVM,
            ordersRepository = ordersRepo
        )

        // --- Realtime Orders VM (same logic as UI) ---
        vm = RealtimeOrdersViewModel(
            application = application,
            autoPrintManager = autoPrint
        )

        // 🔔 Start listening
        vm.startListening()

        // 🎧 Register STOP SOUND listener
        registerReceiver(
            stopSoundReceiver,
            IntentFilter("STOP_RINGTONE"),
            RECEIVER_NOT_EXPORTED   // ⭐ REQUIRED Android 12+
        )


        // 🚨 Foreground mode
        startForeground(99, buildNotification())
    }


    private fun buildNotification(): Notification {

        val channelId = "orders_monitor"
        val channelName = "Order Monitoring"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Order Monitoring Active")
            .setContentText("Listening for new orders…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }


    override fun onDestroy() {
        unregisterReceiver(stopSoundReceiver)
        vm.stopListening()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
