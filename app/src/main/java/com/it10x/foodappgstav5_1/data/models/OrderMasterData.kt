package com.it10x.foodappgstav5_1.data.models

import com.google.firebase.Timestamp

/**
 * OrderMasterData (NEW SYSTEM)
 *
 * Only standard, clean fields.
 * Used for POS printing, Firestore, and Web orders.
 */
data class OrderMasterData(

    // =====================================================
    // CORE IDENTIFIERS
    // =====================================================
    var id: String = "",
    var userId: String = "",
    var srno: Int = 0,
    var source: String? = null,      // WEB | POS | APP

    // =====================================================
    // CUSTOMER
    // =====================================================
    var customerName: String = "",
    var email: String = "",
    var addressId: String = "",
    var customerPhone: String? = null, // optional, recommended for delivery/online

    // =====================================================
    // ORDER TYPE
    // =====================================================
    var orderType: String? = null,   // DINE_IN | TAKEAWAY | DELIVERY | ONLINE
    var tableNo: String? = null,     // Only for DINE_IN

    // =====================================================
    // AMOUNTS (FINAL)
    // =====================================================
    var itemTotal: Double = 0.0,          // total items cost before any discount or tax
    var subTotal: Double? = null,         // after discounts
    var discountTotal: Double? = null,    // total discounts applied
    var taxAfterDiscount: Double? = null, // tax after discount
    var deliveryFee: Double? = null,      // delivery charges
    var grandTotal: Double? = null,       // final payable

    // =====================================================
    // PAYMENT
    // =====================================================
    var paymentType: String = "",
    var paymentStatus: String? = null,  // PAID | PENDING | FAILED | REFUNDED

    // =====================================================
    // ORDER FLOW
    // =====================================================
    var orderStatus: String? = null,   // NEW | SCHEDULED | ACCEPTED | PREPARING | READY | COMPLETED | CANCELLED

    // =====================================================
    // TIMESTAMPS
    // =====================================================
    var createdAt: Timestamp? = null,  // Firestore server timestamp

    // =====================================================
    // AUTOMATION FLAGS
    // =====================================================
    var printed: Boolean? = null,
    var acknowledged: Boolean? = null,

    // =====================================================
    // SYNC CONTROL
    // =====================================================
    var syncStatus: String? = null,       // PENDING | SYNCED | FAILED
    //OLD SYSTEM
    var calculatedPickUpDiscountL: Double? = null,
    var flatDiscount: Double? = null,
    var calCouponDiscount: Double? = null,
)




// Extension to get milliseconds from Firestore Timestamp
fun OrderMasterData.createdAtMillis(): Long {
    return createdAt?.toDate()?.time ?: 0L
}

// Extension to format timestamp for printing
fun OrderMasterData.formattedTime(): String {
    val millis = createdAtMillis()
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

//package com.it10x.foodappgstav5_1.data.models
//
//import com.google.firebase.Timestamp
//
///**
// * OrderMasterData
// *
// * - Legacy fields are kept for backward compatibility
// * - New clean fields are OPTIONAL (nullable)
// * - Android should prefer NEW fields when available
// */
//data class OrderMasterData(
//
//    // --------------------------------------------------
//    // BASIC IDENTIFIERS
//    // --------------------------------------------------
//    var id: String = "",
//    var customerName: String = "",
//    var email: String = "",
//    var userId: String = "",
//    var addressId: String = "",
//
//    // --------------------------------------------------
//    // ORDER META
//    // --------------------------------------------------
//    var srno: Int = 0,
//    var timeId: String = "",
//    var time: String = "",
//    var paymentType: String = "",
//
//    /**
//     * LEGACY STATUS
//     * (used earlier for payment state like COMPLETED / PENDING)
//     */
//    var status: String = "",
//
//    // --------------------------------------------------
//    // LEGACY TOTALS (DO NOT REMOVE)
//    // --------------------------------------------------
//    var itemTotal: Double = 0.0,                 // before discount & tax
//    var endTotalG: Double = 0.0,                  // legacy final total
//    var finalGrandTotal: Double = 0.0,            // legacy final total
//
//    var deliveryCost: Double = 0.0,
//    var totalDiscountG: Double = 0.0,
//    var flatDiscount: Double = 0.0,
//    var calculatedPickUpDiscountL: Double = 0.0,
//    var calCouponDiscount: Double = 0.0,
//
//    var couponDiscountPercentL: Double = 0.0,
//    var pickUpDiscountPercentL: Double = 0.0,
//    var couponCode: String? = null,
//
//    /**
//     * LEGACY TAX (raw / before discount)
//     */
//    var totalTax: Double = 0.0,
//
//    // --------------------------------------------------
//    // ✅ NEW CLEAN TOTAL FIELDS (PREFERRED)
//    // --------------------------------------------------
//
//    /**
//     * Total of ALL discounts combined
//     */
//    var discountTotal: Double? = null,
//
//    /**
//     * Tax BEFORE discount
//     */
//    var taxBeforeDiscount: Double? = null,
//
//    /**
//     * Tax AFTER discount (correct tax)
//     */
//    var taxAfterDiscount: Double? = null,
//
//    /**
//     * Subtotal AFTER discount, BEFORE tax
//     */
//    var subTotal: Double? = null,
//
//    /**
//     * Delivery fee (clean naming)
//     */
//    var deliveryFee: Double? = null,
//
//    /**
//     * Final payable amount (correct)
//     */
//    var grandTotal: Double? = null,
//
//    // --------------------------------------------------
//    // ✅ ORDER SOURCE & FLOW (NEW SYSTEM)
//    // --------------------------------------------------
//    var source: String? = null,          // WEB | POS | APP
//    var orderStatus: String? = null,     // NEW | ACCEPTED | COMPLETED | CANCELLED
//    var paymentStatus: String? = null,   // PAID | UNPAID | FAILED
//
//    // --------------------------------------------------
//    // AUTOMATION FLAGS
//    // --------------------------------------------------
//    var printed: Boolean? = null,        // auto-print handled
//    var acknowledged: Boolean? = null,   // sound acknowledged
//
//    // --------------------------------------------------
//    // TIMESTAMPS
//    // --------------------------------------------------
//    var createdAt: Timestamp? = null,    // Firestore server timestamp
//    var createdAtUTC: String? = null     // ISO UTC string
//)


