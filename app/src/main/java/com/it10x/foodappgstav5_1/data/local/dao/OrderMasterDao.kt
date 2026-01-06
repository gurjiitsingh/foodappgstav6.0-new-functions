package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.*
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderMasterDao {

    @Query("SELECT * FROM pos_order_master ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PosOrderMasterEntity>>

    @Query("SELECT * FROM pos_order_master WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): PosOrderMasterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PosOrderMasterEntity)

    @Query("SELECT * FROM pos_order_master WHERE id = :id LIMIT 1")
    suspend fun getByIdSync(id: String): PosOrderMasterEntity?

//    @Query("SELECT * FROM pos_order_master ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
//    fun getPagedOrders(limit: Int, offset: Int): List<PosOrderMasterEntity>

    @Query("""
    SELECT * FROM pos_order_master 
    ORDER BY createdAt DESC 
    LIMIT :limit OFFSET :offset
""")
    suspend fun getPagedOrders(limit: Int, offset: Int): List<PosOrderMasterEntity>

}


