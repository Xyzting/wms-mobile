package com.utb.wms.ui.dashboard

import androidx.annotation.StringRes

enum class UbinRingkasan { STOK_MENIPIS, PENERIMAAN_MENUNGGU, PENGELUARAN_MENUNGGU }

sealed interface BarisDashboard {

    data class Sambutan(
        val nama: String,
        val role: String,
        val stokMenipis: Int,
        val penerimaanMenunggu: Int,
        val pengeluaranMenunggu: Int,
    ) : BarisDashboard

    data class Bagian(@StringRes val judul: Int) : BarisDashboard

    data class Menu(val menu: MenuUtama) : BarisDashboard
}
