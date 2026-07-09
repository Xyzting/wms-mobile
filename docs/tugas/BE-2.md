# BE-2 — Implementasi Repository dan Pengujian

**Penanggung jawab:** Nazka Yasir Alman Paluthi (24552011087)

## Ruang lingkup

Mengisi lima rangka repository yang sudah disiapkan BE-1, lalu menulis unit test
untuk dua alur transaksi.

## Berkas yang dimiliki

```
app/src/main/java/com/utb/wms/data/repository/
├── AuthRepositoryImpl.kt
├── MasterDataRepositoryImpl.kt
├── InventoryRepositoryImpl.kt
├── InboundRepositoryImpl.kt
└── OutboundRepositoryImpl.kt

app/src/test/java/com/utb/wms/data/repository/
├── InboundRepositoryTest.kt
└── OutboundRepositoryTest.kt
```

Jangan mengubah antarmuka pada `domain/repository/` maupun DAO pada
`data/local/dao/Daos.kt`. Keduanya dipakai peran lain.

## Urutan pengerjaan yang disarankan

Kerjakan dari yang paling ringan: `MasterData` → `Inventory` → `Auth` →
`Inbound` → `Outbound`.

---

## 1. MasterDataRepositoryImpl

Ketiga fungsi hanya memetakan entity menjadi model domain. Penyaringan pemasok
aktif sudah dilakukan di SQL, jadi tidak perlu diulang di sini.

```kotlin
override fun observeItems(): Flow<List<Item>> =
    masterDataDao.observeItems().map { daftar -> daftar.map { it.toDomain() } }
```

Impor `kotlinx.coroutines.flow.map` dan `com.utb.wms.data.local.mapper.toDomain`.

## 2. InventoryRepositoryImpl

| Fungsi | Perilaku |
|---|---|
| `observeStocks()` | petakan `StockWithRefs` menjadi `Stock` |
| `observeMovements()` | petakan `StockMovementWithRefs` menjadi `StockMovement` |
| `totalStock(sku)` | teruskan ke `stockDao.totalStock(sku)` |
| `stockAt(sku, locationCode)` | `stockDao.find(sku, locationCode)?.jumlahStok ?: 0` |

## 3. AuthRepositoryImpl

```kotlin
override suspend fun login(username: String, password: String): User? {
    val baris = userDao.findByUsername(username) ?: return null
    if (!baris.user.aktif) return null
    if (baris.user.password != password) return null
    return baris.toDomain().also { _currentUser.value = it }
}

override fun logout() {
    _currentUser.value = null
}
```

Kata sandi sengaja dibandingkan apa adanya karena ini proyek kuliah dengan data
contoh. Pada sistem nyata, sandi wajib disimpan sebagai hash.

## 4. InboundRepositoryImpl

Validasi terlebih dahulu, di luar transaksi:

```kotlin
require(details.isNotEmpty()) { "Detail penerimaan tidak boleh kosong" }
require(details.all { it.qty > 0 }) { "Kuantitas harus lebih dari nol" }
```

Seluruh penulisan dibungkus satu transaksi:

```kotlin
return transactionRunner.transaction {
    val urut = goodsReceiptDao.count() + 1
    val id = "GR-$urut"
    val noReceipt = "GR-%04d".format(urut)

    goodsReceiptDao.insertHeader(
        GoodsReceiptEntity(
            id = id,
            noReceipt = noReceipt,
            tanggal = tanggal,
            supplierId = supplier.id,
            operatorId = operator.id,
            status = DocumentStatus.POSTED,
        ),
    )
    goodsReceiptDao.insertDetails(
        details.map {
            GoodsReceiptDetailEntity(
                receiptId = id,
                sku = it.item.sku,
                locationKode = it.location.kode,
                qty = it.qty,
            )
        },
    )

    details.forEach { d ->
        val lama = stockDao.find(d.item.sku, d.location.kode)
        stockDao.upsert(
            StockEntity(
                id = lama?.id ?: "STK-${stockDao.count() + 1}",
                sku = d.item.sku,
                locationKode = d.location.kode,
                jumlahStok = (lama?.jumlahStok ?: 0) + d.qty,
            ),
        )
        movementDao.insert(
            StockMovementEntity(
                id = "MOV-${movementDao.count() + 1}",
                sku = d.item.sku,
                locationKode = d.location.kode,
                tipe = MovementType.INBOUND,
                qty = d.qty,
                tanggal = tanggal,
                referensi = noReceipt,
            ),
        )
    }

    GoodsReceipt(id, noReceipt, tanggal, supplier, operator, DocumentStatus.POSTED, details)
}
```

Kolom `id` pada `GoodsReceiptDetailEntity` dibiarkan memakai nilai bawaan `0`
karena Room yang mengisinya secara otomatis.

## 5. OutboundRepositoryImpl

Aturan pentingnya: **periksa seluruh baris lebih dahulu, baru menulis.** Bila
satu baris saja kekurangan stok, dokumen dibatalkan dan tidak ada satu pun baris
stok atau kartu stok yang berubah.

Karena satu dokumen boleh memuat dua baris dengan SKU dan lokasi yang sama,
kebutuhan harus dijumlahkan dahulu sebelum dibandingkan dengan stok:

```kotlin
return transactionRunner.transaction {
    val kebutuhan = details
        .groupBy { it.item.sku to it.location.kode }
        .mapValues { (_, baris) -> baris.sumOf { it.qty } }

    kebutuhan.forEach { (kunci, diminta) ->
        val (sku, kodeLokasi) = kunci
        val tersedia = stockDao.find(sku, kodeLokasi)?.jumlahStok ?: 0
        if (tersedia < diminta) {
            return@transaction OutboundResult.InsufficientStock(sku, tersedia, diminta)
        }
    }

    val urut = goodsIssueDao.count() + 1
    val id = "GI-$urut"
    val noIssue = "GI-%04d".format(urut)
    ...
    OutboundResult.Success(issue)
}
```

Sisanya cermin dari Inbound: `insertHeader` dengan `GoodsIssueEntity`,
`insertDetails`, lalu untuk tiap detail kurangi `jumlahStok` dan catat
`StockMovementEntity` bertipe `MovementType.OUTBOUND` dengan `referensi = noIssue`.

Baris stok pada pengeluaran barang pasti sudah ada — sudah dipastikan oleh
pemeriksaan di awal.

## 6. Pengujian

Berjalan di JVM biasa, tanpa emulator. Buat DAO palsu: kelas biasa yang
mengimplementasi antarmuka DAO dan menyimpan data pada `MutableList`.

```kotlin
class FakeTransactionRunner : TransactionRunner {
    override suspend fun <R> transaction(block: suspend () -> R): R = block()
}
```

Untuk fungsi DAO yang mengembalikan `Flow` dan tidak dipakai pengujian, cukup
kembalikan `flowOf(emptyList())`.

Bungkus setiap pengujian dengan `runTest { }` dari `kotlinx-coroutines-test`.

### Kasus uji wajib

| Berkas | Kasus | Harapan |
|---|---|---|
| `InboundRepositoryTest` | penerimaan 20 unit pada SKU berstok 100 | stok menjadi 120, tercatat satu `StockMovement` bertipe `INBOUND` |
| `OutboundRepositoryTest` | pengeluaran 30 unit dari stok 100 | hasil `Success`, stok menjadi 70, tercatat satu `StockMovement` bertipe `OUTBOUND` |
| `OutboundRepositoryTest` | pengeluaran 150 unit dari stok 100 | hasil `InsufficientStock(tersedia = 100, diminta = 150)`, stok tetap 100, tidak ada `StockMovement` |
| `OutboundRepositoryTest` | dua baris SKU dan lokasi sama, 60 + 60, stok 100 | hasil `InsufficientStock(tersedia = 100, diminta = 120)`, stok tetap 100 |

Kasus terakhir adalah alasan kebutuhan dijumlahkan per pasangan SKU dan lokasi.
Tanpa penjumlahan itu, kedua baris masing-masing lolos pemeriksaan dan stok
menjadi minus.

## Selesai bila

- `./gradlew assembleDebug` hijau.
- `./gradlew test` hijau untuk keempat kasus di atas.
- Tidak ada lagi `TODO(` di dalam `data/repository/`.
