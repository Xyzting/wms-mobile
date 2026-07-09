package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.MasterDataDao
import com.utb.wms.data.local.mapper.toDomain
import com.utb.wms.data.local.mapper.toEntity
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
        masterDataDao.observeAllItems().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeLocations(): Flow<List<Location>> =
        masterDataDao.observeLocations().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeAllLocations(): Flow<List<Location>> =
        masterDataDao.observeAllLocations().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeSuppliers(): Flow<List<Supplier>> =
        masterDataDao.observeSuppliers().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeAllSuppliers(): Flow<List<Supplier>> =
        masterDataDao.observeAllSuppliers().map { daftar -> daftar.map { it.toDomain() } }

    override fun searchItems(kataKunci: String): Flow<List<Item>> {
        val bersih = kataKunci.trim()
        val sumber = if (bersih.isEmpty()) {
            masterDataDao.observeItems()
        } else {
            masterDataDao.searchItems(bersih)
        }
        return sumber.map { daftar -> daftar.map { it.toDomain() } }
    }

    override suspend fun findItem(sku: String): Item? =
        masterDataDao.findItem(sku.trim())?.toDomain()

    override suspend fun findItemByBarcode(barcode: String): Item? {
        val bersih = barcode.trim()
        if (bersih.isEmpty()) return null
        return masterDataDao.findItemByBarcode(bersih)?.toDomain()
    }

    override suspend fun simpanItem(item: Item) {
        require(item.sku.isNotBlank()) { "SKU wajib diisi" }
        require(item.nama.isNotBlank()) { "Nama item wajib diisi" }
        require(item.satuan.isNotBlank()) { "Satuan wajib diisi" }
        require(item.stokMinimum >= 0) { "Stok minimum tidak boleh negatif" }

        val bersih = item.copy(
            sku = item.sku.trim(),
            nama = item.nama.trim(),
            satuan = item.satuan.trim(),
            barcode = item.barcode?.trim()?.takeIf { it.isNotEmpty() },
        )
        masterDataDao.upsertItem(bersih.toEntity())
    }

    override suspend fun nonaktifkanItem(sku: String) {
        masterDataDao.nonaktifkanItem(sku)
    }

    override suspend fun simpanSupplier(supplier: Supplier) {
        require(supplier.id.isNotBlank()) { "Kode pemasok wajib diisi" }
        require(supplier.nama.isNotBlank()) { "Nama pemasok wajib diisi" }

        val bersih = supplier.copy(
            id = supplier.id.trim(),
            nama = supplier.nama.trim(),
            kontak = supplier.kontak.trim(),
        )
        masterDataDao.upsertSupplier(bersih.toEntity())
    }

    override suspend fun nonaktifkanSupplier(id: String) {
        masterDataDao.nonaktifkanSupplier(id)
    }

    override suspend fun simpanLocation(location: Location) {
        require(location.kode.isNotBlank()) { "Kode lokasi wajib diisi" }
        require(location.nama.isNotBlank()) { "Nama lokasi wajib diisi" }
        require(location.kapasitas >= 0) { "Kapasitas tidak boleh negatif" }

        val bersih = location.copy(
            kode = location.kode.trim(),
            nama = location.nama.trim(),
        )
        masterDataDao.upsertLocation(bersih.toEntity())
    }

    override suspend fun nonaktifkanLocation(kode: String) {
        masterDataDao.nonaktifkanLocation(kode)
    }
}
