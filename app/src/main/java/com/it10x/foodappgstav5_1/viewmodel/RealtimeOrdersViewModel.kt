package com.it10x.foodappgstav5_1.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.models.OrderMasterData
import com.it10x.foodappgstav5_1.data.repository.RealtimeOrdersRepository
import com.it10x.foodappgstav5_1.printer.AutoPrintManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RealtimeOrdersViewModel(
    application: Application,
    private val autoPrintManager: AutoPrintManager
) : AndroidViewModel(application) {


    private val repo = RealtimeOrdersRepository()

    private val _realtimeOrders =
        MutableStateFlow<List<OrderMasterData>>(emptyList())
    val realtimeOrders: StateFlow<List<OrderMasterData>> = _realtimeOrders


    // 🔔 Ringing state code to disapear button
    //private val _isRinging = MutableStateFlow(false)
    //val isRinging: StateFlow<Boolean> = _isRinging
    // -----------------------------
    // LISTENER STATE
    // -----------------------------

    private val listeningStartedAt: Timestamp = Timestamp.now()
    private var isListening = false

    // -----------------------------
    // START REALTIME LISTENING
    // -----------------------------

    fun startListening() {
        if (isListening) return
        isListening = true

        repo.startListening { newOrder ->

            // ⛔ Ignore POS orders
            if (newOrder.source == "POS") return@startListening

            // ⛔ Ignore already printed orders
            if (newOrder.printed == true) return@startListening

            // ⛔ Ignore OLD orders (CRITICAL FIX)
            val createdAt = newOrder.createdAt
            if (createdAt !is Timestamp) return@startListening
            if (createdAt.seconds <= listeningStartedAt.seconds) return@startListening

            // ⛔ Ignore duplicates already in memory
            if (_realtimeOrders.value.any { it.id == newOrder.id }) return@startListening

            // ✅ NOW this is truly a NEW order
            _realtimeOrders.value = listOf(newOrder) + _realtimeOrders.value

            // 🔔 Ring
            playSoundIfOrderIsNew(newOrder)

            // 🖨 Auto print
            autoPrintManager.onNewOrder(newOrder)
        }
    }


    // -----------------------------
    // SOUND LOGIC
    // -----------------------------

    private var ringtone: Ringtone? = null

    private fun playSoundIfOrderIsNew(order: OrderMasterData) {

    // ❌ DO NOT ring for POS orders
    if (order.source == "POS") return

    val orderTime = order.createdAt
    if (orderTime !is Timestamp) return
    if (orderTime.seconds <= listeningStartedAt.seconds) return

    // ❌ Do not ring if already acknowledged
    if (order.acknowledged == true) return

    val context = getApplication<Application>()
    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    ringtone = RingtoneManager.getRingtone(context, alarmUri).apply {
        audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        isLooping = true
        play()
    }
}


//    fun stopRingtone() {
//        ringtone?.stop()
//        ringtone = null
//    }

    fun stopRingtone() {
    try {
        ringtone?.stop()
    } catch (_: Exception) {}

    try {
        ringtone?.isLooping = false
    } catch (_: Exception) {}

    ringtone = null
}

    // -----------------------------
    // ✅ ACKNOWLEDGE ORDER (IMPORTANT)
    // -----------------------------

    fun acknowledgeOrder(orderId: String) {
        viewModelScope.launch {

            // 🔄 Update Firestore
            FirebaseFirestore.getInstance()
                .collection("orderMaster")
                .document(orderId)
                .update("acknowledged", true)

            // 🔕 Stop alarm
            stopRingtone()
        }
    }

    // -----------------------------
    // CLEANUP
    // -----------------------------

    fun stopListening() {
        repo.stopListening()
        isListening = false
        stopRingtone()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }



}
