package com.utb.wms.ui.report

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.utb.wms.R
import com.utb.wms.databinding.FragmentReportBinding
import com.utb.wms.domain.model.BarisLaporanMutasi
import com.utb.wms.domain.model.BarisLaporanStok
import com.utb.wms.domain.model.MovementType
import com.utb.wms.ui.common.appContainer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

class ReportFragment : Fragment(R.layout.fragment_report) {

    private var binding: FragmentReportBinding? = null

    private val viewModel: ReportViewModel by viewModels {
        ReportViewModel.factory(appContainer.reportRepository)
    }

    private var tabStok = true

    private val formatTanggal: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("id", "ID"))
            .withZone(ZoneId.systemDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentReportBinding.bind(view)
        binding = tampilan

        val adapter = ReportAdapter()
        tampilan.daftarLaporan.adapter = adapter

        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        tampilan.toolbar.setOnMenuItemClickListener { butir ->
            if (butir.itemId == R.id.aksi_ekspor) {
                ekspor(tampilan)
                true
            } else {
                false
            }
        }

        tampilan.tabLaporan.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tabStok = tab.position == 0
                    gambarUlang(tampilan, adapter)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) = Unit

                override fun onTabReselected(tab: TabLayout.Tab) = Unit
            },
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.laporanStok.collect { gambarUlang(tampilan, adapter) } }
                launch { viewModel.laporanMutasi.collect { gambarUlang(tampilan, adapter) } }
            }
        }
    }

    private fun gambarUlang(tampilan: FragmentReportBinding, adapter: ReportAdapter) {
        val baris = if (tabStok) {
            viewModel.laporanStok.value.map { it.keBaris() }
        } else {
            viewModel.laporanMutasi.value.mapIndexed { posisi, mutasi -> mutasi.keBaris(posisi) }
        }
        adapter.submitList(baris)
        tampilan.textKosong.visibility = if (baris.isEmpty()) View.VISIBLE else View.GONE
        tampilan.textKeterangan.visibility = if (tabStok) View.GONE else View.VISIBLE
        tampilan.textKeterangan.setText(R.string.laporan_rentang)
    }

    private fun BarisLaporanStok.keBaris() = BarisLaporan(
        kunci = "stok:$sku:$kodeLokasi",
        utama = namaItem,
        kedua = "$sku · $namaLokasi",
        ketiga = getString(R.string.laporan_stok_minimum, stokMinimum),
        nilai = "$jumlahStok $satuan",
        peringatan = dibawahMinimum,
    )

    private fun BarisLaporanMutasi.keBaris(posisi: Int) = BarisLaporan(
        kunci = "mutasi:$posisi:$referensi",
        utama = namaItem,
        kedua = "$sku · $kodeLokasi · $referensi",
        ketiga = keterangan ?: formatTanggal.format(Instant.ofEpochMilli(tanggal)),
        nilai = qtyBertanda(tipe, qty),
        peringatan = false,
    )

    private fun qtyBertanda(tipe: MovementType, qty: Int): String = when (tipe) {
        MovementType.INBOUND -> "+$qty"
        MovementType.OUTBOUND -> "-$qty"
        MovementType.ADJUSTMENT -> if (qty >= 0) "+$qty" else "$qty"
    }

    private fun ekspor(tampilan: FragmentReportBinding) {
        val isi = if (tabStok) csvStok() else csvMutasi()
        if (isi == null) {
            Snackbar.make(
                tampilan.root,
                R.string.laporan_kosong_ekspor,
                Snackbar.LENGTH_SHORT,
            ).show()
            return
        }

        val judul = getString(
            if (tabStok) R.string.laporan_judul_stok else R.string.laporan_judul_mutasi,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, judul)
            putExtra(Intent.EXTRA_TEXT, isi)
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.laporan_bagikan)))
        } catch (galat: ActivityNotFoundException) {
            Snackbar.make(
                tampilan.root,
                R.string.laporan_tidak_ada_aplikasi,
                Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    private fun csvStok(): String? {
        val baris = viewModel.laporanStok.value
        if (baris.isEmpty()) return null

        val kepala = "SKU,Nama,Satuan,Kode Lokasi,Nama Lokasi,Jumlah,Minimum,Status"
        val isi = baris.joinToString("\n") { stok ->
            listOf(
                stok.sku,
                stok.namaItem,
                stok.satuan,
                stok.kodeLokasi,
                stok.namaLokasi,
                stok.jumlahStok.toString(),
                stok.stokMinimum.toString(),
                if (stok.dibawahMinimum) "Di bawah minimum" else "Aman",
            ).joinToString(",") { it.aman() }
        }
        return "$kepala\n$isi"
    }

    private fun csvMutasi(): String? {
        val baris = viewModel.laporanMutasi.value
        if (baris.isEmpty()) return null

        val kepala = "Tanggal,SKU,Nama,Kode Lokasi,Tipe,Qty,Referensi,Keterangan"
        val isi = baris.joinToString("\n") { mutasi ->
            listOf(
                formatTanggal.format(Instant.ofEpochMilli(mutasi.tanggal)),
                mutasi.sku,
                mutasi.namaItem,
                mutasi.kodeLokasi,
                mutasi.tipe.name,
                qtyBertanda(mutasi.tipe, mutasi.qty),
                mutasi.referensi,
                mutasi.keterangan.orEmpty(),
            ).joinToString(",") { it.aman() }
        }
        return "$kepala\n$isi"
    }

    private fun String.aman(): String =
        if (contains(',') || contains('"') || contains('\n')) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }

    override fun onResume() {
        super.onResume()
        viewModel.muat()
    }

    override fun onDestroyView() {
        binding?.daftarLaporan?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
