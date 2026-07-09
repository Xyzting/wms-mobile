# FE-2 — Komponen Bersama, Penerimaan & Pengeluaran Barang

**Penanggung jawab:** Radhitias Salman Syam (24552011112)

## Ruang lingkup

Membangun dua layar formulir — bagian paling rumit dari sisi tampilan — beserta
komponen yang keduanya pakai bersama.

## Berkas yang dimiliki

```
app/src/main/java/com/utb/wms/ui/
├── common/Components.kt
├── inbound/{GoodsReceiptScreen.kt, GoodsReceiptViewModel.kt}
└── outbound/{GoodsIssueScreen.kt, GoodsIssueViewModel.kt}
```

`ui/common/ComingSoonScreen.kt` dan `ui/common/AppContainerAccess.kt` milik
BE-1, jangan diubah.

### Berkas milik BE-1 yang boleh disentuh

`ui/navigation/WmsNavHost.kt` — hanya dua blok `composable` bertanda
`penanggungJawab = "FE-2"`, yaitu `Routes.INBOUND` dan `Routes.OUTBOUND`.

## Komponen bersama

`Components.kt` berisi `LabeledDropdown`, dipakai untuk memilih pemasok, barang,
dan lokasi:

```kotlin
@Composable
fun <T> LabeledDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
)
```

## Pola layar

Layar ditulis tanpa keadaan, dibungkus `*Route` yang menghubungkannya ke
ViewModel. `appContainer()` hanya dipanggil dari `*Route`, tidak pernah dari
layar yang dipratinjau `@Preview`.

## Kontrak yang dipakai

| Kebutuhan | Antarmuka | Fungsi |
|---|---|---|
| Isi dropdown | `MasterDataRepository` | `observeItems()`, `observeLocations()`, `observeSuppliers()` |
| Operator saat ini | `AuthRepository` | `currentUser: StateFlow<User?>` |
| Simpan penerimaan | `InboundRepository` | `createGoodsReceipt(supplier, operator, details, tanggal)` |
| Simpan pengeluaran | `OutboundRepository` | `createGoodsIssue(tujuan, operator, details, tanggal)` |
| Sisa stok saat mengetik | `InventoryRepository` | `stockAt(sku, locationCode)` |

Impor hanya dari `com.utb.wms.domain.repository` dan `com.utb.wms.domain.model`.
Paket `com.utb.wms.data` **dilarang** diimpor dari `ui/`.

Nilai `tanggal` diisi `System.currentTimeMillis()`. Nilai `operator` diambil dari
`authRepository.currentUser.value`.

## Rincian layar

### Penerimaan Barang

Pilih pemasok, lalu tambah satu atau lebih baris detail (barang, lokasi,
kuantitas). Tombol simpan memanggil `createGoodsReceipt` dan mengembalikan
`GoodsReceipt`. Tampilkan `noReceipt` pada pesan berhasil.

Repository menolak dokumen tanpa detail dan kuantitas nol lewat `require`, yang
melempar `IllegalArgumentException`. Cegah keadaan itu di tampilan: nonaktifkan
tombol simpan selama daftar detail masih kosong atau ada kuantitas yang belum
diisi.

### Pengeluaran Barang

Isi tujuan, lalu tambah baris detail seperti pada Penerimaan Barang. Hasil
`createGoodsIssue` berupa `sealed interface` sehingga wajib ditangani keduanya:

```kotlin
when (val hasil = outboundRepository.createGoodsIssue(tujuan, operator, details, tanggal)) {
    is OutboundResult.Success -> tampilkanBerhasil(hasil.issue.noIssue)
    is OutboundResult.InsufficientStock -> tampilkanGalat(
        "Stok ${hasil.sku} tidak mencukupi: tersedia ${hasil.tersedia}, diminta ${hasil.diminta}"
    )
}
```

Bila stok tidak mencukupi, tidak ada satu pun data yang tersimpan. Pengguna cukup
memperbaiki kuantitas lalu menyimpan ulang.

## Catatan penting

Sampai BE-2 menyelesaikan bagiannya, memanggil repository saat aplikasi berjalan
akan melempar `NotImplementedError`. Ini normal. Kerjakan tampilan lebih dahulu
dan verifikasi lewat `@Preview` dengan data buatan.

## Selesai bila

- Kedua layar tampil, dapat dinavigasi, dan tombol kembali berfungsi.
- Kekurangan stok tampil sebagai pesan galat, bukan aplikasi berhenti.
- `./gradlew assembleDebug` hijau.
