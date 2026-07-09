package com.utb.wms.data.repository

import com.utb.wms.data.local.dao.MasterDataDao
import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.repository.MasterDataRepository
import kotlinx.coroutines.flow.Flow

class MasterDataRepositoryImpl(
    private val masterDataDao: MasterDataDao,
) : MasterDataRepository {

    override fun observeItems(): Flow<List<Item>> {
        TODO("BE-2: petakan ItemEntity ke Item")
    }

    override fun observeLocations(): Flow<List<Location>> {
        TODO("BE-2: petakan LocationEntity ke Location")
    }

    override fun observeSuppliers(): Flow<List<Supplier>> {
        TODO("BE-2: petakan SupplierEntity ke Supplier")
    }
}
