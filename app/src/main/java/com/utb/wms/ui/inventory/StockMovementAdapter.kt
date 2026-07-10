package com.utb.wms.ui.inventory

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.utb.wms.R
import com.utb.wms.databinding.ItemMutasiBinding
import com.utb.wms.domain.model.MovementType
import com.utb.wms.domain.model.StockMovement
import com.utb.wms.ui.common.qtyBertanda
import com.utb.wms.ui.common.tanggalRingkas

class StockMovementAdapter :
    ListAdapter<StockMovement, StockMovementAdapter.MutasiViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MutasiViewHolder {
        val binding = ItemMutasiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return MutasiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MutasiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MutasiViewHolder(
        private val binding: ItemMutasiBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mutasi: StockMovement) {
            val konteks = binding.root.context

            binding.textNama.text = mutasi.item.nama
            binding.textRincian.text = konteks.getString(
                R.string.mutasi_rincian,
                mutasi.item.sku,
                mutasi.location.nama,
            )
            binding.textReferensi.text = konteks.getString(
                R.string.mutasi_rincian,
                mutasi.referensi,
                tanggalRingkas(mutasi.tanggal),
            )
            binding.textQty.text = qtyBertanda(mutasi.tipe, mutasi.qty)

            binding.textKeterangan.text = mutasi.keterangan.orEmpty()
            binding.textKeterangan.visibility =
                if (mutasi.keterangan.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.textOperator.text = mutasi.operator
                ?.let { konteks.getString(R.string.mutasi_operator, it.nama) }
                ?: konteks.getString(R.string.mutasi_tanpa_operator)

            val ikon = when (mutasi.tipe) {
                MovementType.INBOUND -> R.drawable.ic_arrow_downward
                MovementType.OUTBOUND -> R.drawable.ic_arrow_upward
                MovementType.ADJUSTMENT -> R.drawable.ic_tune
            }
            val label = when (mutasi.tipe) {
                MovementType.INBOUND -> R.string.mutasi_tipe_masuk
                MovementType.OUTBOUND -> R.string.mutasi_tipe_keluar
                MovementType.ADJUSTMENT -> R.string.mutasi_tipe_penyesuaian
            }
            binding.ikonTipe.setImageResource(ikon)
            binding.ikonTipe.contentDescription = konteks.getString(label)

            val positif = when (mutasi.tipe) {
                MovementType.INBOUND -> true
                MovementType.OUTBOUND -> false
                MovementType.ADJUSTMENT -> mutasi.qty >= 0
            }
            val atribut = if (positif) {
                com.google.android.material.R.attr.colorPrimary
            } else {
                com.google.android.material.R.attr.colorError
            }
            val warna = MaterialColors.getColor(binding.root, atribut)
            binding.textQty.setTextColor(warna)
            binding.ikonTipe.imageTintList = ColorStateList.valueOf(warna)
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<StockMovement>() {

        override fun areItemsTheSame(oldItem: StockMovement, newItem: StockMovement): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: StockMovement, newItem: StockMovement): Boolean =
            oldItem == newItem
    }
}
