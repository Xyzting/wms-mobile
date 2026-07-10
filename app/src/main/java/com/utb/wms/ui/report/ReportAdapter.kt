package com.utb.wms.ui.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utb.wms.databinding.ItemLaporanBinding

data class BarisLaporan(
    val kunci: String,
    val utama: String,
    val kedua: String,
    val ketiga: String,
    val nilai: String,
    val peringatan: Boolean,
)

class ReportAdapter : ListAdapter<BarisLaporan, ReportAdapter.LaporanViewHolder>(Pembanding) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaporanViewHolder {
        val binding = ItemLaporanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return LaporanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LaporanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LaporanViewHolder(
        private val binding: ItemLaporanBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baris: BarisLaporan) {
            binding.textUtama.text = baris.utama
            binding.textKedua.text = baris.kedua
            binding.textKetiga.text = baris.ketiga
            binding.textNilai.text = baris.nilai
            binding.textPeringatan.visibility =
                if (baris.peringatan) View.VISIBLE else View.GONE
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<BarisLaporan>() {

        override fun areItemsTheSame(oldItem: BarisLaporan, newItem: BarisLaporan): Boolean =
            oldItem.kunci == newItem.kunci

        override fun areContentsTheSame(oldItem: BarisLaporan, newItem: BarisLaporan): Boolean =
            oldItem == newItem
    }
}
