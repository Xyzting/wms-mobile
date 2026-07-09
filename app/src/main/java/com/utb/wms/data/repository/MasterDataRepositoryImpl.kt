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

    override fun observeLocations(): Flow<List<Location>> =
        masterDataDao.observeLocations().map { daftar -> daftar.map { it.toDomain() } }

    override fun observeSuppliers(): Flow<List<Supplier>> =
        masterDataDao.observeSuppliers().map { daftar -> daftar.map { it.toDomain() } }
}
