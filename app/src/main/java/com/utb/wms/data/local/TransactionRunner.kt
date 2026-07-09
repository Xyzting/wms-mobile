package com.utb.wms.data.local

import androidx.room.withTransaction

interface TransactionRunner {

    suspend fun <R> transaction(block: suspend () -> R): R
}

class RoomTransactionRunner(
    private val database: WmsDatabase,
) : TransactionRunner {

    override suspend fun <R> transaction(block: suspend () -> R): R =
        database.withTransaction { block() }
}
