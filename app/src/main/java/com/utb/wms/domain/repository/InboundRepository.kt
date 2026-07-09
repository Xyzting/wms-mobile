package com.utb.wms.domain.repository

import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsReceipt
import com.utb.wms.domain.model.GoodsReceiptDetail
import com.utb.wms.domain.model.Supplier
import com.utb.wms.domain.model.User
import kotlinx.coroutines.flow.Flow

interface InboundRepository {

    suspend fun createGoodsReceipt(
        supplier: Supplier,
        operator: User,
        details: List<GoodsReceiptDetail>,
        tanggal: Long,
    ): GoodsReceipt

    fun observeGoodsReceipts(): Flow<List<GoodsReceipt>>

    fun observeGoodsReceiptsByStatus(status: DocumentStatus): Flow<List<GoodsReceipt>>

    suspend fun findGoodsReceipt(id: String): GoodsReceipt?

    suspend fun validateGoodsReceipt(
        id: String,
        approver: User,
        catatan: String? = null,
    ): DocumentResult

    suspend fun postGoodsReceipt(id: String, tanggal: Long): DocumentResult

    suspend fun cancelGoodsReceipt(id: String, catatan: String? = null): DocumentResult
}
