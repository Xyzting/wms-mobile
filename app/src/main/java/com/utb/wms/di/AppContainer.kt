package com.utb.wms.di

import android.content.Context
import com.utb.wms.data.local.DatabaseSeeder
import com.utb.wms.data.local.RoomTransactionRunner
import com.utb.wms.data.local.TransactionRunner
import com.utb.wms.data.local.WmsDatabase
import com.utb.wms.data.repository.AuthRepositoryImpl
import com.utb.wms.data.repository.InboundRepositoryImpl
import com.utb.wms.data.repository.InventoryRepositoryImpl
import com.utb.wms.data.repository.MasterDataRepositoryImpl
import com.utb.wms.data.repository.OutboundRepositoryImpl
import com.utb.wms.data.repository.ReportRepositoryImpl
import com.utb.wms.data.repository.UserRepositoryImpl
import com.utb.wms.domain.repository.AuthRepository
import com.utb.wms.domain.repository.InboundRepository
import com.utb.wms.domain.repository.InventoryRepository
import com.utb.wms.domain.repository.MasterDataRepository
import com.utb.wms.domain.repository.OutboundRepository
import com.utb.wms.domain.repository.ReportRepository
import com.utb.wms.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {

    private val database: WmsDatabase = WmsDatabase.getInstance(context)

    private val transactionRunner: TransactionRunner = RoomTransactionRunner(database)

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val authRepository: AuthRepository = AuthRepositoryImpl(
        userDao = database.userDao(),
    )

    val userRepository: UserRepository = UserRepositoryImpl(
        userDao = database.userDao(),
    )

    val masterDataRepository: MasterDataRepository = MasterDataRepositoryImpl(
        masterDataDao = database.masterDataDao(),
    )

    val inventoryRepository: InventoryRepository = InventoryRepositoryImpl(
        transactionRunner = transactionRunner,
        stockDao = database.stockDao(),
        movementDao = database.movementDao(),
    )

    val inboundRepository: InboundRepository = InboundRepositoryImpl(
        transactionRunner = transactionRunner,
        goodsReceiptDao = database.goodsReceiptDao(),
        stockDao = database.stockDao(),
        movementDao = database.movementDao(),
    )

    val outboundRepository: OutboundRepository = OutboundRepositoryImpl(
        transactionRunner = transactionRunner,
        goodsIssueDao = database.goodsIssueDao(),
        stockDao = database.stockDao(),
        movementDao = database.movementDao(),
    )

    val reportRepository: ReportRepository = ReportRepositoryImpl(
        stockDao = database.stockDao(),
        movementDao = database.movementDao(),
    )

    init {
        applicationScope.launch {
            DatabaseSeeder.seedIfEmpty(database)
        }
    }
}
