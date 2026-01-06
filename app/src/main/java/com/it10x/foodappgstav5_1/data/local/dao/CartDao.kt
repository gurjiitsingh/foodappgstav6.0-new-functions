package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.*
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart")
    fun getCart(): Flow<List<PosCartEntity>>

    @Query("SELECT * FROM cart WHERE productId = :id LIMIT 1")
    suspend fun getById(id: String): PosCartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PosCartEntity)

    @Update
    suspend fun update(item: PosCartEntity)

    @Delete
    suspend fun delete(item: PosCartEntity)

    @Query("DELETE FROM cart")
    suspend fun clearCart()
}



//package com.it10x.foodappgstav5_1.data.local.dao
//
//import androidx.room.*
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface CartDao {
//
//
//    @Query("SELECT * FROM cart")
//    fun getCart(): Flow<List<CartEntity>>
//
//    @Query("SELECT * FROM cart WHERE productId = :id LIMIT 1")
//    suspend fun getById(id: String): CartEntity?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(item: CartEntity)
//
//    @Update
//    suspend fun update(item: CartEntity)
//
//    @Delete
//    suspend fun delete(item: CartEntity)
//
//    @Query("DELETE FROM cart")
//    suspend fun clearCart()
//
//
//


//    @Query("SELECT * FROM cart")
//    fun getCart(): Flow<List<CartEntity>>

//    @Query("SELECT * FROM cart WHERE productId = :id LIMIT 1")
//    suspend fun getById(id: String): CartEntity?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(item: CartEntity)
//
//    @Update
//    suspend fun update(item: CartEntity)
//
//    @Delete
//    suspend fun delete(item: CartEntity)
//
//    @Query("DELETE FROM cart")
//    suspend fun clearCart()


//}
