package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.MasterDataDao
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.repository.MasterDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MasterDataRepositoryImpl(
    private val masterDataDao: MasterDataDao,
) : MasterDataRepository {

    override fun observeItems(): Flow<List<Item>> =
        masterDataDao.observeItems().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeAllItems(): Flow<List<Item>> =
        TODO("BE-2: pakai masterDataDao.observeAllItems()")

    override fun observeLocations(): Flow<List<Location>> =
        masterDataDao.observeLocations().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeAllLocations(): Flow<List<Location>> =
        TODO("BE-2: pakai masterDataDao.observeAllLocations()")

    override fun observeSuppliers(): Flow<List<Supplier>> =
        masterDataDao.observeSuppliers().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeAllSuppliers(): Flow<List<Supplier>> =
        TODO("BE-2: pakai masterDataDao.observeAllSuppliers()")

    override fun searchItems(kataKunci: String): Flow<List<Item>> =
        TODO("BE-2: kata kunci kosong harus mengembalikan seluruh item aktif")

    override suspend fun findItem(sku: String): Item? =
        TODO("BE-2: pakai masterDataDao.findItem(sku)")

    override suspend fun findItemByBarcode(barcode: String): Item? =
        TODO("BE-2: pakai masterDataDao.findItemByBarcode(barcode)")

    override suspend fun simpanItem(item: Item) {
        TODO("BE-2: validasi sku tidak kosong, stokMinimum tidak negatif, lalu upsertItem")
    }

    override suspend fun nonaktifkanItem(sku: String) {
        TODO("BE-2: penonaktifan lunak, isActive = 0, baris stok tetap utuh")
    }

    override suspend fun simpanSupplier(supplier: Supplier) {
        TODO("BE-2: pakai masterDataDao.upsertSupplier()")
    }

    override suspend fun nonaktifkanSupplier(id: String) {
        TODO("BE-2: pakai masterDataDao.nonaktifkanSupplier(id)")
    }

    override suspend fun simpanLocation(location: Location) {
        TODO("BE-2: validasi kapasitas tidak negatif, lalu upsertLocation")
    }

    override suspend fun nonaktifkanLocation(kode: String) {
        TODO("BE-2: pakai masterDataDao.nonaktifkanLocation(kode)")
    }
}
