package com.utb.wms.domain.repository

import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.GoodsIssue
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.User
import kotlinx.coroutines.flow.Flow

sealed interface OutboundResult {

    data class Success(val issue: GoodsIssue) : OutboundResult

    data class InsufficientStock(
        val sku: String,
        val tersedia: Int,
        val diminta: Int,
    ) : OutboundResult
}

interface OutboundRepository {

    suspend fun createGoodsIssue(
        tujuan: String,
        operator: User,
        details: List<GoodsIssueDetail>,
        tanggal: Long,
    ): OutboundResult

    fun observeGoodsIssues(): Flow<List<GoodsIssue>>

    fun observeGoodsIssuesByStatus(status: DocumentStatus): Flow<List<GoodsIssue>>

    suspend fun findGoodsIssue(id: String): GoodsIssue?

    suspend fun validateGoodsIssue(
        id: String,
        approver: User,
        catatan: String? = null,
    ): DocumentResult

    suspend fun postGoodsIssue(id: String, tanggal: Long): DocumentResult

    suspend fun cancelGoodsIssue(id: String, catatan: String? = null): DocumentResult
}
