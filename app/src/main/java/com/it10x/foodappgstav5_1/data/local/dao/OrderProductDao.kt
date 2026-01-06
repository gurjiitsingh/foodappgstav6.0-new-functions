package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.*
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity

import kotlinx.coroutines.flow.Flow



@Dao
interface OrderProductDao {

    @Insert
    suspend fun insertAll(items: List<PosOrderItemEntity>)

    @Query("SELECT * FROM pos_order_items WHERE orderMasterId = :orderId")
    fun getByOrderId(orderId: String): Flow<List<PosOrderItemEntity>>

    @Query("SELECT * FROM pos_order_items WHERE orderMasterId = :orderId")
    suspend fun getByOrderIdSync(orderId: String): List<PosOrderItemEntity>


}


//@Dao
//interface OrderProductDao {
//
//    @Query("SELECT * FROM order_products WHERE orderMasterId = :orderId")
//    fun getProductsForOrder(orderId: String): Flow<List<PosOrderItemEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(list: List<PosOrderItemEntity>)
//
//    @Query("DELETE FROM order_products WHERE orderMasterId = :orderId")
//    suspend fun deleteByOrder(orderId: String)
//
//    @Query("DELETE FROM order_products")
//    suspend fun clear()
//
//
//    @Query("SELECT * FROM order_products WHERE orderMasterId = :orderId")
//    fun getForOrder(orderId: String): Flow<List<PosOrderItemEntity>>
//
//}
//
//
