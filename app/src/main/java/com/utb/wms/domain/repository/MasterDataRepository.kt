package com.utb.wms.domain.repository

import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Supplier
import kotlinx.coroutines.flow.Flow

interface MasterDataRepository {

    fun observeItems(): Flow<List<Item>>

    fun observeAllItems(): Flow<List<Item>>

    fun observeLocations(): Flow<List<Location>>

    fun observeAllLocations(): Flow<List<Location>>

    fun observeSuppliers(): Flow<List<Supplier>>

    fun observeAllSuppliers(): Flow<List<Supplier>>

    fun searchItems(kataKunci: String): Flow<List<Item>>

    suspend fun findItem(sku: String): Item?

    suspend fun findItemByBarcode(barcode: String): Item?

    suspend fun simpanItem(item: Item)

    suspend fun nonaktifkanItem(sku: String)

    suspend fun simpanSupplier(supplier: Supplier)

    suspend fun nonaktifkanSupplier(id: String)

    suspend fun simpanLocation(location: Location)

    suspend fun nonaktifkanLocation(kode: String)
}
