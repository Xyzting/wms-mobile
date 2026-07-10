package com.utb.wms.ui.document

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.utb.wms.R
import com.utb.wms.databinding.FragmentDocumentHistoryBinding
import com.utb.wms.domain.model.DocumentStatus
import kotlinx.coroutines.launch

abstract class DocumentHistoryFragment : Fragment(R.layout.fragment_document_history) {

    protected abstract val viewModel: RiwayatDokumenViewModel

    @get:StringRes
    protected abstract val judul: Int

    @get:StringRes
    protected abstract val teksKosong: Int

    @get:StringRes
    protected abstract val labelPihak: Int

    private var binding: FragmentDocumentHistoryBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentDocumentHistoryBinding.bind(view)
        binding = tampilan

        val adapter = DocumentAdapter(labelPihak, ::mintaKonfirmasi)
        tampilan.daftarDokumen.adapter = adapter

        tampilan.toolbar.setTitle(judul)
        tampilan.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        tampilan.grupStatus.setOnCheckedStateChangeListener { _, terpilih ->
            viewModel.saringStatus(statusUntuk(terpilih.firstOrNull()))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.baris.collect { daftar ->
                        adapter.submitList(daftar)
                        tampilan.kosong.ikonKosong.setImageResource(R.drawable.ic_history)
                        tampilan.kosong.textKosongJudul.text =
                            teksKosongUntuk(viewModel.status.value)
                        tampilan.kosong.textKosongKeterangan.setText(
                            R.string.riwayat_kosong_keterangan,
                        )
                        tampilan.kosong.root.visibility =
                            if (daftar.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.pesan.collect { pesan ->
                        Snackbar.make(tampilan.root, teksPesan(pesan), Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun mintaKonfirmasi(id: String, aksi: AksiDokumen) {
        when (aksi) {
            AksiDokumen.SETUJUI -> viewModel.jalankan(id, aksi)

            AksiDokumen.POSTING -> konfirmasi(
                judulRes = R.string.dokumen_konfirmasi_posting_judul,
                pesanRes = R.string.dokumen_konfirmasi_posting_pesan,
                lanjutRes = R.string.dokumen_posting,
            ) { viewModel.jalankan(id, aksi) }

            AksiDokumen.BATALKAN -> konfirmasi(
                judulRes = R.string.dokumen_konfirmasi_batal_judul,
                pesanRes = R.string.dokumen_konfirmasi_batal_pesan,
                lanjutRes = R.string.dokumen_batalkan,
            ) { viewModel.jalankan(id, aksi) }
        }
    }

    private fun konfirmasi(
        @StringRes judulRes: Int,
        @StringRes pesanRes: Int,
        @StringRes lanjutRes: Int,
        onLanjut: () -> Unit,
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(judulRes)
            .setMessage(pesanRes)
            .setNegativeButton(R.string.dokumen_konfirmasi_kembali, null)
            .setPositiveButton(lanjutRes) { _, _ -> onLanjut() }
            .show()
    }

    private fun teksPesan(pesan: PesanDokumen): String = when (pesan) {
        is PesanDokumen.Berhasil -> when (pesan.status) {
            DocumentStatus.VALIDATED -> getString(R.string.dokumen_disetujui)
            DocumentStatus.POSTED -> getString(R.string.dokumen_diposting)
            DocumentStatus.CANCELLED -> getString(R.string.dokumen_dibatalkan)
            DocumentStatus.DRAFT -> getString(R.string.dokumen_gagal)
        }

        is PesanDokumen.StokKurang -> getString(
            R.string.dokumen_stok_kurang,
            pesan.sku,
            pesan.tersedia,
            pesan.diminta,
        )

        is PesanDokumen.TransisiTidakValid -> getString(
            R.string.dokumen_transisi_tidak_valid,
            getString(pesan.dari.labelRes()),
            getString(pesan.ke.labelRes()),
        )

        PesanDokumen.TidakDitemukan -> getString(R.string.dokumen_tidak_ditemukan)
        PesanDokumen.TidakBerwenang -> getString(R.string.dokumen_tidak_berwenang)
        PesanDokumen.SesiBerakhir -> getString(R.string.dokumen_sesi_berakhir)
        PesanDokumen.Gagal -> getString(R.string.dokumen_gagal)
    }

    private fun teksKosongUntuk(saring: DocumentStatus?): String = if (saring == null) {
        getString(teksKosong)
    } else {
        getString(R.string.riwayat_kosong_saring, getString(saring.labelRes()))
    }

    private fun statusUntuk(idChip: Int?): DocumentStatus? = when (idChip) {
        R.id.chip_status_draft -> DocumentStatus.DRAFT
        R.id.chip_status_validated -> DocumentStatus.VALIDATED
        R.id.chip_status_posted -> DocumentStatus.POSTED
        R.id.chip_status_cancelled -> DocumentStatus.CANCELLED
        else -> null
    }

    override fun onDestroyView() {
        binding?.daftarDokumen?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
