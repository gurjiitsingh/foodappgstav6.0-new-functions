package com.it10x.foodappgstav5_1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
import com.it10x.foodappgstav5_1.data.local.dao.*
import com.it10x.foodappgstav5_1.data.local.entities.*

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        PosOrderMasterEntity::class,
        PosOrderItemEntity::class,
        PosCartEntity::class
    ],
    version = 18
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderMasterDao(): OrderMasterDao
    abstract fun orderProductDao(): OrderProductDao

    abstract fun cartDao(): CartDao
}
