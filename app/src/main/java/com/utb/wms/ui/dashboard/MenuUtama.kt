package com.utb.wms.ui.dashboard

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.utb.wms.R
import com.utb.wms.domain.model.User
import com.utb.wms.domain.model.bolehMembuatDokumen
import com.utb.wms.domain.model.bolehMengelolaMasterData
import com.utb.wms.domain.model.bolehMengelolaPengguna
import com.utb.wms.domain.model.bolehMenyesuaikanStok

enum class KategoriMenu(@StringRes val judul: Int) {
    TRANSAKSI(R.string.dashboard_bagian_transaksi),
    INVENTARIS(R.string.dashboard_bagian_inventaris),
    ADMINISTRASI(R.string.dashboard_bagian_administrasi),
    LAPORAN(R.string.dashboard_bagian_laporan),
}

data class MenuUtama(
    @IdRes val actionId: Int,
    @DrawableRes val ikon: Int,
    @StringRes val judul: Int,
    @StringRes val keterangan: Int,
    val kategori: KategoriMenu,
)

fun menuUntuk(pengguna: User): List<MenuUtama> = buildList {
    if (pengguna.bolehMembuatDokumen) {
        add(
            MenuUtama(
                actionId = R.id.action_dashboard_to_goodsReceiptForm,
                ikon = R.drawable.ic_arrow_downward,
                judul = R.string.menu_penerimaan_judul,
                keterangan = R.string.menu_penerimaan_keterangan,
                kategori = KategoriMenu.TRANSAKSI,
            ),
        )
        add(
            MenuUtama(
                actionId = R.id.action_dashboard_to_goodsIssueForm,
                ikon = R.drawable.ic_arrow_upward,
                judul = R.string.menu_pengeluaran_judul,
                keterangan = R.string.menu_pengeluaran_keterangan,
                kategori = KategoriMenu.TRANSAKSI,
            ),
        )
    }

    add(
        MenuUtama(
            actionId = R.id.action_dashboard_to_receiptHistory,
            ikon = R.drawable.ic_history,
            judul = R.string.menu_riwayat_penerimaan_judul,
            keterangan = R.string.menu_riwayat_penerimaan_keterangan,
            kategori = KategoriMenu.TRANSAKSI,
        ),
    )
    add(
        MenuUtama(
            actionId = R.id.action_dashboard_to_issueHistory,
            ikon = R.drawable.ic_history,
            judul = R.string.menu_riwayat_pengeluaran_judul,
            keterangan = R.string.menu_riwayat_pengeluaran_keterangan,
            kategori = KategoriMenu.TRANSAKSI,
        ),
    )
    add(
        MenuUtama(
            actionId = R.id.action_dashboard_to_stockList,
            ikon = R.drawable.ic_list,
            judul = R.string.menu_stok_judul,
            keterangan = R.string.menu_stok_keterangan,
            kategori = KategoriMenu.INVENTARIS,
        ),
    )
    add(
        MenuUtama(
            actionId = R.id.action_dashboard_to_stockMovement,
            ikon = R.drawable.ic_assessment,
            judul = R.string.menu_kartu_stok_judul,
            keterangan = R.string.menu_kartu_stok_keterangan,
            kategori = KategoriMenu.INVENTARIS,
        ),
    )

    if (pengguna.bolehMenyesuaikanStok) {
        add(
            MenuUtama(
                actionId = R.id.action_dashboard_to_stockAdjustment,
                ikon = R.drawable.ic_tune,
                judul = R.string.menu_penyesuaian_judul,
                keterangan = R.string.menu_penyesuaian_keterangan,
                kategori = KategoriMenu.INVENTARIS,
            ),
        )
    }

    add(
        MenuUtama(
            actionId = R.id.action_dashboard_to_itemCatalog,
            ikon = R.drawable.ic_search,
            judul = R.string.menu_katalog_judul,
            keterangan = R.string.menu_katalog_keterangan,
            kategori = KategoriMenu.INVENTARIS,
        ),
    )

    if (pengguna.bolehMengelolaMasterData) {
        add(
            MenuUtama(
                actionId = R.id.action_dashboard_to_masterData,
                ikon = R.drawable.ic_inventory,
                judul = R.string.menu_master_data_judul,
                keterangan = R.string.menu_master_data_keterangan,
                kategori = KategoriMenu.ADMINISTRASI,
            ),
        )
    }

    if (pengguna.bolehMengelolaPengguna) {
        add(
            MenuUtama(
                actionId = R.id.action_dashboard_to_userManagement,
                ikon = R.drawable.ic_group,
                judul = R.string.menu_pengguna_judul,
                keterangan = R.string.menu_pengguna_keterangan,
                kategori = KategoriMenu.ADMINISTRASI,
            ),
        )
        add(
            MenuUtama(
                actionId = R.id.action_dashboard_to_roleManagement,
                ikon = R.drawable.ic_shield,
                judul = R.string.menu_role_judul,
                keterangan = R.string.menu_role_keterangan,
                kategori = KategoriMenu.ADMINISTRASI,
            ),
        )
    }

    add(
        MenuUtama(
            actionId = R.id.action_dashboard_to_report,
            ikon = R.drawable.ic_assessment,
            judul = R.string.menu_laporan_judul,
            keterangan = R.string.menu_laporan_keterangan,
            kategori = KategoriMenu.LAPORAN,
        ),
    )
}
