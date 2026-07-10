package com.utb.wms.ui.document

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.utb.wms.R
import com.utb.wms.databinding.ItemDokumenBinding
import com.utb.wms.ui.common.tanggalRingkas

class DocumentAdapter(
    @StringRes private val labelPihak: Int,
    private val onAksi: (String, AksiDokumen) -> Unit,
) : ListAdapter<BarisDokumen, DocumentAdapter.DokumenViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DokumenViewHolder {
        val binding = ItemDokumenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return DokumenViewHolder(binding, labelPihak, onAksi)
    }

    override fun onBindViewHolder(holder: DokumenViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DokumenViewHolder(
        private val binding: ItemDokumenBinding,
        @StringRes private val labelPihak: Int,
        private val onAksi: (String, AksiDokumen) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baris: BarisDokumen) {
            val konteks = binding.root.context

            binding.textNomor.text = baris.nomor
            binding.textPihak.text = konteks.getString(labelPihak, baris.pihak)
            binding.textTanggal.text = tanggalRingkas(baris.tanggal)
            binding.textRingkasan.text = konteks.getString(
                R.string.riwayat_ringkasan,
                baris.jumlahBaris,
                baris.totalQty,
            )
            binding.textOperator.text = konteks.getString(R.string.riwayat_operator, baris.operator)

            binding.textPenyetuju.text = baris.penyetuju
                ?.let { konteks.getString(R.string.riwayat_penyetuju, it) }
                .orEmpty()
            binding.textPenyetuju.visibility =
                if (baris.penyetuju == null) View.GONE else View.VISIBLE

            binding.textCatatan.text = baris.catatan
                ?.let { konteks.getString(R.string.riwayat_catatan, it) }
                .orEmpty()
            binding.textCatatan.visibility =
                if (baris.catatan.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.chipStatus.setText(baris.status.labelRes())
            binding.chipStatus.chipBackgroundColor = ColorStateList.valueOf(
                MaterialColors.getColor(binding.root, baris.status.warnaLatarAttr()),
            )
            binding.chipStatus.setTextColor(
                MaterialColors.getColor(binding.root, baris.status.warnaTeksAttr()),
            )

            binding.grupTombol.visibility = if (baris.adaAksi) View.VISIBLE else View.GONE
            binding.tombolSetujui.visibility = if (baris.bolehSetujui) View.VISIBLE else View.GONE
            binding.tombolPosting.visibility = if (baris.bolehPosting) View.VISIBLE else View.GONE
            binding.tombolBatalkan.visibility = if (baris.bolehBatalkan) View.VISIBLE else View.GONE

            binding.tombolSetujui.setOnClickListener { onAksi(baris.id, AksiDokumen.SETUJUI) }
            binding.tombolPosting.setOnClickListener { onAksi(baris.id, AksiDokumen.POSTING) }
            binding.tombolBatalkan.setOnClickListener { onAksi(baris.id, AksiDokumen.BATALKAN) }
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<BarisDokumen>() {

        override fun areItemsTheSame(oldItem: BarisDokumen, newItem: BarisDokumen): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BarisDokumen, newItem: BarisDokumen): Boolean =
            oldItem == newItem
    }
}
