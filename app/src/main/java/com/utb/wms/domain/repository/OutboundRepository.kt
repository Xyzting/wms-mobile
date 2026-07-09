package com.utb.wms.domain.repository

import com.utb.wms.domain.model.GoodsIssue
import com.utb.wms.domain.model.GoodsIssueDetail
import com.utb.wms.domain.model.User

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
}
