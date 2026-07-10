package com.utb.wms.ui.document

import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.repository.DocumentResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DokumenUiTest {

    private fun baris(status: DocumentStatus, bolehMenyetujui: Boolean): BarisDokumen = BarisDokumen(
        id = "D-01",
        nomor = "GR-0001",
        pihak = "Pihak Uji",
        tanggal = 0L,
        status = status,
        jumlahBaris = 1,
        totalQty = 10,
        operator = "Operator Uji",
        penyetuju = null,
        catatan = null,
        bolehMenyetujui = bolehMenyetujui,
    )

    @Test
    fun `draft dengan wewenang menyetujui boleh disetujui dan dibatalkan tapi belum bisa diposting`() {
        val dokumen = baris(DocumentStatus.DRAFT, bolehMenyetujui = true)

        assertTrue(dokumen.bolehSetujui)
        assertFalse(dokumen.bolehPosting)
        assertTrue(dokumen.bolehBatalkan)
        assertTrue(dokumen.adaAksi)
    }

    @Test
    fun `validated dengan wewenang menyetujui boleh diposting dan dibatalkan tapi tidak disetujui lagi`() {
        val dokumen = baris(DocumentStatus.VALIDATED, bolehMenyetujui = true)

        assertFalse(dokumen.bolehSetujui)
        assertTrue(dokumen.bolehPosting)
        assertTrue(dokumen.bolehBatalkan)
        assertTrue(dokumen.adaAksi)
    }

    @Test
    fun `posted dengan wewenang menyetujui tidak bisa dibatalkan karena stok sudah bergerak`() {
        val dokumen = baris(DocumentStatus.POSTED, bolehMenyetujui = true)

        assertFalse(dokumen.bolehSetujui)
        assertFalse(dokumen.bolehPosting)
        assertFalse(dokumen.bolehBatalkan)
        assertFalse(dokumen.adaAksi)
    }

    @Test
    fun `cancelled dengan wewenang menyetujui tidak punya aksi tersisa`() {
        val dokumen = baris(DocumentStatus.CANCELLED, bolehMenyetujui = true)

        assertFalse(dokumen.bolehSetujui)
        assertFalse(dokumen.bolehPosting)
        assertFalse(dokumen.bolehBatalkan)
        assertFalse(dokumen.adaAksi)
    }

    @Test
    fun `draft tanpa wewenang menyetujui tidak punya aksi`() {
        val dokumen = baris(DocumentStatus.DRAFT, bolehMenyetujui = false)

        assertFalse(dokumen.bolehSetujui)
        assertFalse(dokumen.bolehPosting)
        assertFalse(dokumen.bolehBatalkan)
        assertFalse(dokumen.adaAksi)
    }

    @Test
    fun `validated tanpa wewenang menyetujui tidak punya aksi`() {
        val dokumen = baris(DocumentStatus.VALIDATED, bolehMenyetujui = false)

        assertFalse(dokumen.bolehSetujui)
        assertFalse(dokumen.bolehPosting)
        assertFalse(dokumen.bolehBatalkan)
        assertFalse(dokumen.adaAksi)
    }

    @Test
    fun `posted tanpa wewenang menyetujui tidak punya aksi`() {
        val dokumen = baris(DocumentStatus.POSTED, bolehMenyetujui = false)

        assertFalse(dokumen.bolehSetujui)
        assertFalse(dokumen.bolehPosting)
        assertFalse(dokumen.bolehBatalkan)
        assertFalse(dokumen.adaAksi)
    }

    @Test
    fun `cancelled tanpa wewenang menyetujui tidak punya aksi`() {
        val dokumen = baris(DocumentStatus.CANCELLED, bolehMenyetujui = false)

        assertFalse(dokumen.bolehSetujui)
        assertFalse(dokumen.bolehPosting)
        assertFalse(dokumen.bolehBatalkan)
        assertFalse(dokumen.adaAksi)
    }

    @Test
    fun `success menjadi berhasil dengan status yang sama`() {
        val hasil: DocumentResult = DocumentResult.Success(id = "D-01", status = DocumentStatus.VALIDATED)

        assertEquals(PesanDokumen.Berhasil(DocumentStatus.VALIDATED), hasil.kePesan())
    }

    @Test
    fun `insufficient stock menjadi stok kurang dengan sku tersedia dan diminta yang tidak tertukar`() {
        val hasil: DocumentResult = DocumentResult.InsufficientStock(sku = "SKU-01", tersedia = 3, diminta = 10)

        assertEquals(PesanDokumen.StokKurang(sku = "SKU-01", tersedia = 3, diminta = 10), hasil.kePesan())
    }

    @Test
    fun `invalid transition menjadi transisi tidak valid dengan status dari dan ke yang tidak tertukar`() {
        val hasil: DocumentResult =
            DocumentResult.InvalidTransition(dari = DocumentStatus.DRAFT, ke = DocumentStatus.POSTED)

        assertEquals(
            PesanDokumen.TransisiTidakValid(dari = DocumentStatus.DRAFT, ke = DocumentStatus.POSTED),
            hasil.kePesan(),
        )
    }

    @Test
    fun `not found menjadi tidak ditemukan`() {
        val hasil: DocumentResult = DocumentResult.NotFound

        assertEquals(PesanDokumen.TidakDitemukan, hasil.kePesan())
    }
}
