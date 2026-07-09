package com.utb.wms.domain.model

enum class DocumentStatus { DRAFT, VALIDATED, POSTED, CANCELLED }

fun DocumentStatus.bolehPindahKe(tujuan: DocumentStatus): Boolean = when (this) {
    DocumentStatus.DRAFT -> tujuan == DocumentStatus.VALIDATED || tujuan == DocumentStatus.CANCELLED
    DocumentStatus.VALIDATED -> tujuan == DocumentStatus.POSTED || tujuan == DocumentStatus.CANCELLED
    DocumentStatus.POSTED -> false
    DocumentStatus.CANCELLED -> false
}

interface TransactionDocument {
    val id: String
    val tanggal: Long
    val status: DocumentStatus
    val approvedBy: User?
    val approvedAt: Long?
    val catatan: String?
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
    override val approvedBy: User? = null,
    override val approvedAt: Long? = null,
    override val catatan: String? = null,
) : TransactionDocument {
    val totalQty: Int get() = details.sumOf { it.qty }
}

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
    override val approvedBy: User? = null,
    override val approvedAt: Long? = null,
    override val catatan: String? = null,
) : TransactionDocument {
    val totalQty: Int get() = details.sumOf { it.qty }
}
