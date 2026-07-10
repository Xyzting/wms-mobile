package com.utb.wms.ui.dashboard

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.utb.wms.databinding.ItemBagianBinding
import com.utb.wms.databinding.ItemSambutanBinding
import com.utb.wms.databinding.ItemUbinMenuBinding

private const val TIPE_SAMBUTAN = 0
private const val TIPE_BAGIAN = 1
private const val TIPE_MENU = 2

class DashboardAdapter(
    private val onMenu: (MenuUtama) -> Unit,
    private val onUbin: (UbinRingkasan) -> Unit,
) : ListAdapter<BarisDashboard, RecyclerView.ViewHolder>(Pembanding) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is BarisDashboard.Sambutan -> TIPE_SAMBUTAN
        is BarisDashboard.Bagian -> TIPE_BAGIAN
        is BarisDashboard.Menu -> TIPE_MENU
    }

    fun spanUntuk(position: Int): Int =
        if (getItemViewType(position) == TIPE_MENU) 1 else 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val pengembang = LayoutInflater.from(parent.context)
        return when (viewType) {
            TIPE_SAMBUTAN -> SambutanViewHolder(
                ItemSambutanBinding.inflate(pengembang, parent, false),
                onUbin,
            )

            TIPE_BAGIAN -> BagianViewHolder(ItemBagianBinding.inflate(pengembang, parent, false))

            else -> MenuViewHolder(
                ItemUbinMenuBinding.inflate(pengembang, parent, false),
                onMenu,
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val baris = getItem(position)) {
            is BarisDashboard.Sambutan -> (holder as SambutanViewHolder).bind(baris)
            is BarisDashboard.Bagian -> (holder as BagianViewHolder).bind(baris)
            is BarisDashboard.Menu -> (holder as MenuViewHolder).bind(baris)
        }
    }

    class SambutanViewHolder(
        private val binding: ItemSambutanBinding,
        onUbin: (UbinRingkasan) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.ubinStok.setOnClickListener { onUbin(UbinRingkasan.STOK_MENIPIS) }
            binding.ubinDokumen.setOnClickListener { onUbin(UbinRingkasan.DOKUMEN_MENUNGGU) }
        }

        fun bind(baris: BarisDashboard.Sambutan) {
            binding.textNama.text = baris.nama
            binding.textRole.text = baris.role
            binding.textStokMenipis.text = baris.stokMenipis.toString()
            binding.textDokumenMenunggu.text = baris.dokumenMenunggu.toString()
        }
    }

    class BagianViewHolder(
        private val binding: ItemBagianBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baris: BarisDashboard.Bagian) {
            binding.textBagian.setText(baris.judul)
        }
    }

    class MenuViewHolder(
        private val binding: ItemUbinMenuBinding,
        private val onMenu: (MenuUtama) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baris: BarisDashboard.Menu) {
            val konteks = binding.root.context
            val menu = baris.menu

            binding.textJudulMenu.setText(menu.judul)
            binding.ikonMenu.setImageResource(menu.ikon)
            binding.root.contentDescription = konteks.getString(menu.keterangan)
            binding.root.setOnClickListener { onMenu(menu) }

            val latar = when (menu.kategori) {
                KategoriMenu.TRANSAKSI -> com.google.android.material.R.attr.colorPrimaryContainer
                KategoriMenu.INVENTARIS -> com.google.android.material.R.attr.colorTertiaryContainer
                KategoriMenu.ADMINISTRASI -> com.google.android.material.R.attr.colorSecondaryContainer
                KategoriMenu.LAPORAN -> com.google.android.material.R.attr.colorSurfaceVariant
            }
            val tint = when (menu.kategori) {
                KategoriMenu.TRANSAKSI -> com.google.android.material.R.attr.colorOnPrimaryContainer
                KategoriMenu.INVENTARIS -> com.google.android.material.R.attr.colorOnTertiaryContainer
                KategoriMenu.ADMINISTRASI -> com.google.android.material.R.attr.colorOnSecondaryContainer
                KategoriMenu.LAPORAN -> com.google.android.material.R.attr.colorOnSurfaceVariant
            }

            binding.wadahIkon.backgroundTintList =
                ColorStateList.valueOf(MaterialColors.getColor(binding.root, latar))
            binding.ikonMenu.imageTintList =
                ColorStateList.valueOf(MaterialColors.getColor(binding.root, tint))
        }
    }

    private object Pembanding : DiffUtil.ItemCallback<BarisDashboard>() {

        override fun areItemsTheSame(oldItem: BarisDashboard, newItem: BarisDashboard): Boolean =
            when {
                oldItem is BarisDashboard.Sambutan && newItem is BarisDashboard.Sambutan -> true

                oldItem is BarisDashboard.Bagian && newItem is BarisDashboard.Bagian ->
                    oldItem.judul == newItem.judul

                oldItem is BarisDashboard.Menu && newItem is BarisDashboard.Menu ->
                    oldItem.menu.actionId == newItem.menu.actionId

                else -> false
            }

        override fun areContentsTheSame(oldItem: BarisDashboard, newItem: BarisDashboard): Boolean =
            oldItem == newItem
    }
}
