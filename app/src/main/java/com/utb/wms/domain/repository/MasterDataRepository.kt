package com.utb.wms.domain.repository

import com.utb.wms.domain.model.Item
import com.utb.wms.domain.model.Location
import com.utb.wms.domain.model.Supplier
import kotlinx.coroutines.flow.Flow

interface MasterDataRepository {

    fun observeItems(): Flow<List<Item>>

    fun observeLocations(): Flow<List<Location>>

    fun observeSuppliers(): Flow<List<Supplier>>
}
