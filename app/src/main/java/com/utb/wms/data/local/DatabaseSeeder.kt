package com.utb.wms.data.local

import androidx.room.withTransaction
import com.utb.wms.data.local.entity.ItemEntity
import com.utb.wms.data.local.entity.LocationEntity
import com.utb.wms.data.local.entity.RoleEntity
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.SupplierEntity
import com.utb.wms.data.local.entity.UserEntity

object DatabaseSeeder {

    suspend fun seedIfEmpty(database: WmsDatabase) {
        if (database.masterDataDao().countItems() > 0) return

        database.withTransaction {
            database.userDao().insertRoles(
                listOf(
                    RoleEntity(id = "R-01", namaRole = "Admin"),
                    RoleEntity(id = "R-02", namaRole = "Operator"),
                    RoleEntity(id = "R-03", namaRole = "Supervisor"),
                ),
            )
            database.userDao().insertUsers(
                listOf(
                    UserEntity("U-01", "admin", "admin123", "Administrator", "R-01", true),
                    UserEntity("U-02", "operator", "operator123", "Budi Santoso", "R-02", true),
                    UserEntity("U-03", "supervisor", "supervisor123", "Siti Rahma", "R-03", true),
                ),
            )
            database.masterDataDao().insertSuppliers(
                listOf(
                    SupplierEntity("SUP-001", "PT Sumber Makmur", "021-555-0001", true),
                    SupplierEntity("SUP-002", "CV Baja Sentosa", "022-555-0002", true),
                ),
            )
            database.masterDataDao().insertItems(
                listOf(
                    ItemEntity("BOLT-M8-30", "Bolt M8x30mm", "pcs", 50),
                    ItemEntity("NUT-M8", "Mur M8", "pcs", 50),
                    ItemEntity("PLT-STL-2", "Plat Baja 2mm", "lembar", 10),
                ),
            )
            database.masterDataDao().insertLocations(
                listOf(
                    LocationEntity("A-01", "Rak A-01", 500),
                    LocationEntity("A-02", "Rak A-02", 500),
                    LocationEntity("B-01", "Rak B-01", 300),
                ),
            )
            database.stockDao().insertAll(
                listOf(
                    StockEntity("STK-1", "BOLT-M8-30", "A-01", 120),
                    StockEntity("STK-2", "NUT-M8", "A-01", 80),
                    StockEntity("STK-3", "PLT-STL-2", "B-01", 15),
                ),
            )
        }
    }
}
