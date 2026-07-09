package com.utb.wms.data.local

import androidx.room.withTransaction
import com.utb.wms.data.local.entity.ItemEntity
import com.utb.wms.data.local.entity.LocationEntity
import com.utb.wms.data.local.entity.RoleEntity
import com.utb.wms.data.local.entity.StockEntity
import com.utb.wms.data.local.entity.SupplierEntity
import com.utb.wms.data.local.entity.UserEntity
import com.utb.wms.domain.model.NamaRole

object DatabaseSeeder {

    suspend fun seedIfEmpty(database: WmsDatabase) {
        if (database.masterDataDao().countItems() > 0) return

        database.withTransaction {
            database.userDao().insertRoles(
                listOf(
                    RoleEntity(id = "R-01", namaRole = NamaRole.ADMIN),
                    RoleEntity(id = "R-02", namaRole = NamaRole.OPERATOR),
                    RoleEntity(id = "R-03", namaRole = NamaRole.SUPERVISOR),
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
                    SupplierEntity("SUP-001", "PT Sumber Makmur", "0215550001", true),
                    SupplierEntity("SUP-002", "CV Baja Sentosa", "0225550002", true),
                    SupplierEntity("SUP-003", "PT Karya Logam", "0245550003", true),
                ),
            )
            database.masterDataDao().insertItems(
                listOf(
                    ItemEntity(
                        sku = "BOLT-M8-30",
                        nama = "Bolt M8x30mm",
                        satuan = "pcs",
                        stokMinimum = 50,
                        barcode = "8991010000011",
                    ),
                    ItemEntity(
                        sku = "NUT-M8",
                        nama = "Mur M8",
                        satuan = "pcs",
                        stokMinimum = 50,
                        barcode = "8991010000028",
                    ),
                    ItemEntity(
                        sku = "PLT-STL-2",
                        nama = "Plat Baja 2mm",
                        satuan = "lembar",
                        stokMinimum = 10,
                        barcode = "8991010000035",
                    ),
                    ItemEntity(
                        sku = "WSH-M8",
                        nama = "Ring Plat M8",
                        satuan = "pcs",
                        stokMinimum = 100,
                        barcode = "8991010000042",
                    ),
                    ItemEntity(
                        sku = "PIPE-INC-1",
                        nama = "Pipa Besi 1 inci",
                        satuan = "batang",
                        stokMinimum = 20,
                        barcode = "8991010000059",
                    ),
                    ItemEntity(
                        sku = "PAINT-GRY-5",
                        nama = "Cat Besi Abu 5L",
                        satuan = "kaleng",
                        stokMinimum = 5,
                        barcode = "8991010000066",
                    ),
                ),
            )
            database.masterDataDao().insertLocations(
                listOf(
                    LocationEntity("A-01", "Rak A-01", 500),
                    LocationEntity("A-02", "Rak A-02", 500),
                    LocationEntity("B-01", "Rak B-01", 300),
                    LocationEntity("B-02", "Rak B-02", 300),
                ),
            )
            database.stockDao().insertAll(
                listOf(
                    StockEntity("STK-1", "BOLT-M8-30", "A-01", 120),
                    StockEntity("STK-2", "NUT-M8", "A-01", 80),
                    StockEntity("STK-3", "PLT-STL-2", "B-01", 15),
                    StockEntity("STK-4", "WSH-M8", "A-02", 60),
                    StockEntity("STK-5", "PIPE-INC-1", "B-02", 24),
                    StockEntity("STK-6", "PAINT-GRY-5", "B-02", 3),
                ),
            )
        }
    }
}
