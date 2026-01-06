package com.it10x.foodappgstav5_1.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "cart",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["parentId"])
    ]
)
data class PosCartEntity(

    @PrimaryKey
    val productId: String,      // Firestore product ID

    val name: String,
    val categoryId: String,

    val parentId: String?,      // variant support
    val isVariant: Boolean,

    val basePrice: Double,      // price BEFORE tax
    val quantity: Int,

    val taxRate: Double,
    val taxType: String,        // inclusive | exclusive

    val createdAt: Long = System.currentTimeMillis()
)



//@Entity(tableName = "cart")
//data class PosCartEntity(
//
//    @PrimaryKey
//    val productId: String,        // Firestore product ID
//
//    val name: String,
//    val categoryId: String,
//
//    // VARIANT
//    val parentId: String?,        // null = main product
//    val isVariant: Boolean,
//
//    // PRICING SNAPSHOT
//    val basePrice: Double,        // price before tax
//    val quantity: Int,
//
//    // TAX SNAPSHOT
//    val taxRate: Double,
//    val taxType: String,          // inclusive | exclusive
//)



//@Entity(tableName = "cart")
//data class PosCartEntity(
//
//    // =====================================================
//    // CORE PRODUCT IDENTITY
//    // =====================================================
//    @PrimaryKey
//    val productId: String,          // Firestore product ID (or variant ID)
//
//    val name: String,
//    val categoryId: String,
//
//    // =====================================================
//    // VARIANT INFO (CRITICAL FOR ORDER ITEMS)
//    // =====================================================
//    val parentId: String?,          // NULL = simple product, NOT NULL = variant
//    val isVariant: Boolean,         // derived at add-to-cart time
//
//    // =====================================================
//    // PRICING (MUTABLE, TEMP)
//    // =====================================================
//    val BasePrice: Double,              // BASE price before tax
//    val quantity: Int,
//
//    // =====================================================
//    // TAX INFO (FROM PRODUCT/CATEGORY SNAPSHOT)
//    // =====================================================
//    val taxRate: Double?,           // % value
//    val taxType: String?,           // inclusive | exclusive
//
//    // =====================================================
//    // OPTIONAL UI HELPERS
//    // =====================================================
//    val searchCode: String? = null  // barcode / short code (POS only)
//)


//@Entity(tableName = "pos_cart")
//data class PosCartEntity(
//
//    @PrimaryKey
//    val productId: String,
//
//    val name: String,
//    val categoryId: String,
//
//    val parentId: String?,      // for variants
//    val isVariant: Boolean,
//
//    val basePrice: Double,
//    val quantity: Int,
//
//    val taxRate: Double?,
//    val taxType: String?        // inclusive | exclusive
//)
