package com.utb.wms.domain.repository

import com.utb.wms.domain.model.DocumentStatus

sealed interface DocumentResult {

    data class Success(
        val id: String,
        val status: DocumentStatus,
    ) : DocumentResult

    data class InsufficientStock(
        val sku: String,
        val tersedia: Int,
        val diminta: Int,
    ) : DocumentResult

    data class InvalidTransition(
        val dari: DocumentStatus,
        val ke: DocumentStatus,
    ) : DocumentResult

    data object NotFound : DocumentResult
}
