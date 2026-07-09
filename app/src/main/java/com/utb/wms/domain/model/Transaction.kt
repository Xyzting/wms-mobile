package com.utb.wms.domain.model

enum class DocumentStatus { DRAFT, VALIDATED, POSTED, CANCELLED }

interface TransactionDocument {
    val id: String
    val tanggal: Long
    val status: DocumentStatus
}

data class GoodsReceiptDetail(
    val item: Item,
    val location: Location,
    val qty: Int,
)

data class GoodsReceipt(
    override val id: String,
    val noReceipt: String,
    override val tanggal: Long,
    val supplier: Supplier,
    val operator: User,
    override val status: DocumentStatus = DocumentStatus.DRAFT,
    val details: List<GoodsReceiptDetail> = emptyList(),
) : TransactionDocument

data class GoodsIssueDetail(
    val item: Item,
    val location: Location,
    val qty: Int,
)

data class GoodsIssue(
    override val id: String,
    val noIssue: String,
    override val tanggal: Long,
    val tujuan: String,
    val operator: User,
    override val status: DocumentStatus = DocumentStatus.DRAFT,
    val details: List<GoodsIssueDetail> = emptyList(),
) : TransactionDocument
