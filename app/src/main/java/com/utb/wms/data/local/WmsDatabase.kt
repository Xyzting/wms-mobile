package com.utb.wms.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.utb.wms.data.local.dao.GoodsIssueDao
import com.utb.wms.data.local.dao.GoodsReceiptDao
import com.utb.wms.data.local.dao.MasterDataDao
import com.utb.wms.data.local.dao.MovementDao
import com.utb.wms.data.local.dao.StockDao
import com.utb.wms.data.local.dao.UserDao
import com.utb.wms.data.local.entity.GoodsIssueDetailEntity
import com.utb.wms.data.local.entity.GoodsIssueEntity
import com.utb.wms.data.local.entity.GoodsReceiptDetailEntity
import com.utb.wms.data.local.entity.GoodsReceiptEntity
import com.utb.wms.data.local.entity.ItemEntity
import com.utb.wms.data.local.entity.LocationEntity
import com.utb.wms.data.local.entity.RoleEntity
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.StockMovementEntity
import com.utb.wms.data.local.entity.SupplierEntity
import com.utb.wms.data.local.entity.UserEntity

@Database(
    entities = [
        RoleEntity::class,
        UserEntity::class,
        SupplierEntity::class,
        ItemEntity::class,
        LocationEntity::class,
        StockEntity::class,
        StockMovementEntity::class,
        GoodsReceiptEntity::class,
        GoodsReceiptDetailEntity::class,
        GoodsIssueEntity::class,
        GoodsIssueDetailEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class WmsDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun masterDataDao(): MasterDataDao

    abstract fun stockDao(): StockDao

    abstract fun movementDao(): MovementDao

    abstract fun goodsReceiptDao(): GoodsReceiptDao

    abstract fun goodsIssueDao(): GoodsIssueDao

    companion object {

        private const val DATABASE_NAME = "wms.db"

        @Volatile
        private var INSTANCE: WmsDatabase? = null

        fun getInstance(context: Context): WmsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WmsDatabase::class.java,
                    DATABASE_NAME,
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
