package com.utb.wms.ui.dashboard

import com.utb.wms.R
import com.utb.wms.domain.model.NamaRole
import com.utb.wms.domain.model.Role
import com.utb.wms.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuUtamaTest {

    private fun penggunaAdmin(): User = User(
        id = "U-01",
        username = "admin1",
        password = "rahasia1",
        nama = "Admin Satu",
        role = Role("R-01", NamaRole.ADMIN),
    )

    private fun penggunaOperator(): User = User(
        id = "U-02",
        username = "operator1",
        password = "rahasia2",
        nama = "Operator Satu",
        role = Role("R-02", NamaRole.OPERATOR),
    )

    private fun penggunaSupervisor(): User = User(
        id = "U-03",
        username = "supervisor1",
        password = "rahasia3",
        nama = "Supervisor Satu",
        role = Role("R-03", NamaRole.SUPERVISOR),
    )

    private val menuTanpaGerbangWewenang = listOf(
        R.id.action_dashboard_to_report,
        R.id.action_dashboard_to_stockList,
        R.id.action_dashboard_to_stockMovement,
        R.id.action_dashboard_to_itemCatalog,
        R.id.action_dashboard_to_receiptHistory,
        R.id.action_dashboard_to_issueHistory,
    )

    @Test
    fun `admin melihat 12 menu`() {
        assertEquals(12, menuUntuk(penggunaAdmin()).size)
    }

    @Test
    fun `operator melihat 8 menu`() {
        assertEquals(8, menuUntuk(penggunaOperator()).size)
    }

    @Test
    fun `supervisor melihat 7 menu`() {
        assertEquals(7, menuUntuk(penggunaSupervisor()).size)
    }

    @Test
    fun `admin melihat 4 kategori menu berbeda`() {
        assertEquals(4, menuUntuk(penggunaAdmin()).map { it.kategori }.distinct().size)
    }

    @Test
    fun `operator melihat 3 kategori menu berbeda`() {
        assertEquals(3, menuUntuk(penggunaOperator()).map { it.kategori }.distinct().size)
    }

    @Test
    fun `supervisor melihat 3 kategori menu berbeda`() {
        assertEquals(3, menuUntuk(penggunaSupervisor()).map { it.kategori }.distinct().size)
    }

    @Test
    fun `hanya admin yang punya menu kategori administrasi`() {
        assertTrue(menuUntuk(penggunaAdmin()).any { it.kategori == KategoriMenu.ADMINISTRASI })
        assertFalse(menuUntuk(penggunaOperator()).any { it.kategori == KategoriMenu.ADMINISTRASI })
        assertFalse(menuUntuk(penggunaSupervisor()).any { it.kategori == KategoriMenu.ADMINISTRASI })
    }

    @Test
    fun `supervisor tidak punya menu penerimaan maupun pengeluaran`() {
        val actionId = menuUntuk(penggunaSupervisor()).map { it.actionId }

        assertFalse(actionId.contains(R.id.action_dashboard_to_goodsReceiptForm))
        assertFalse(actionId.contains(R.id.action_dashboard_to_goodsIssueForm))
    }

    @Test
    fun `operator punya menu penerimaan dan pengeluaran`() {
        val actionId = menuUntuk(penggunaOperator()).map { it.actionId }

        assertTrue(actionId.contains(R.id.action_dashboard_to_goodsReceiptForm))
        assertTrue(actionId.contains(R.id.action_dashboard_to_goodsIssueForm))
    }

    @Test
    fun `operator tidak punya menu penyesuaian stok`() {
        assertFalse(
            menuUntuk(penggunaOperator()).map { it.actionId }.contains(R.id.action_dashboard_to_stockAdjustment),
        )
    }

    @Test
    fun `admin dan supervisor punya menu penyesuaian stok`() {
        assertTrue(
            menuUntuk(penggunaAdmin()).map { it.actionId }.contains(R.id.action_dashboard_to_stockAdjustment),
        )
        assertTrue(
            menuUntuk(penggunaSupervisor()).map { it.actionId }.contains(R.id.action_dashboard_to_stockAdjustment),
        )
    }

    @Test
    fun `setiap peran melihat menu tanpa gerbang wewenang`() {
        listOf(penggunaAdmin(), penggunaOperator(), penggunaSupervisor()).forEach { pengguna ->
            val actionId = menuUntuk(pengguna).map { it.actionId }

            menuTanpaGerbangWewenang.forEach { assertTrue(actionId.contains(it)) }
        }
    }

    @Test
    fun `tidak ada actionId yang muncul dua kali dalam satu peran`() {
        listOf(penggunaAdmin(), penggunaOperator(), penggunaSupervisor()).forEach { pengguna ->
            val actionId = menuUntuk(pengguna).map { it.actionId }

            assertEquals(actionId.distinct().size, actionId.size)
        }
    }

    @Test
    fun `urutan kategori menu konsisten dari transaksi ke inventaris ke administrasi ke laporan`() {
        listOf(penggunaAdmin(), penggunaOperator(), penggunaSupervisor()).forEach { pengguna ->
            val urutan = menuUntuk(pengguna).map { it.kategori.ordinal }

            assertEquals(urutan.sorted(), urutan)
        }
    }
}
