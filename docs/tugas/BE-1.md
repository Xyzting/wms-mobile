# BE-1 — Kerangka, Kontrak, dan Basis Data

## Ruang lingkup

Menyiapkan fondasi yang dipakai tiga peran lain: konfigurasi proyek, model
domain, kontrak repository, basis data Room, dependency injection, dan navigasi.

## Berkas yang dimiliki

```
build.gradle.kts, settings.gradle.kts, gradle/libs.versions.toml
app/build.gradle.kts, app/src/main/AndroidManifest.xml, app/src/main/res/
app/src/main/java/com/utb/wms/
├── WmsApplication.kt
├── MainActivity.kt
├── di/AppContainer.kt
├── domain/model/{Master,Inventory,Transaction}.kt
├── domain/repository/{Auth,MasterData,Inventory,Inbound,Outbound}Repository.kt
├── data/local/{WmsDatabase,Converters,DatabaseSeeder,TransactionRunner}.kt
├── data/local/entity/Entities.kt
├── data/local/relation/Relations.kt
├── data/local/dao/Daos.kt
├── data/local/mapper/Mappers.kt
├── data/repository/*Impl.kt          (hanya rangka; isi dikerjakan BE-2)
├── ui/navigation/WmsNavHost.kt
├── ui/common/ComingSoonScreen.kt
└── ui/common/AppContainerAccess.kt
README.md, CONTRIBUTING.md, docs/tugas/, .gitignore
```

## Hasil

- Aplikasi dapat di-build dan dijalankan sejak hari pertama. Kelima layar sudah
  memiliki rute dan menampilkan `ComingSoonScreen` beserta nama penanggung jawab.
- Basis data Room berisi 11 tabel dan terisi data contoh saat pertama dijalankan.
- Rangka repository memakai `TODO(...)`. Fungsi `TODO` bertipe `Nothing`,
  sehingga berkas tetap lolos kompilasi untuk tipe kembalian apa pun. Kesalahan
  `NotImplementedError` hanya muncul bila fungsinya benar-benar dipanggil, dan
  belum ada layar yang memanggilnya.

## Peta ketergantungan

```
ui/  ──────►  domain/  ◄──────  data/
```

`domain/` tidak bergantung pada apa pun. `AppContainer` mengumumkan seluruh
repository dengan tipe **antarmuka**, sehingga `ui/` tidak pernah menyentuh
`data/`.

## Basis data

| Tabel | Kunci utama | Catatan |
|---|---|---|
| `roles` | `id` | |
| `users` | `id` | `username` unik |
| `suppliers` | `id` | |
| `items` | `sku` | |
| `locations` | `kode` | |
| `stocks` | `id` | pasangan (`sku`, `locationKode`) unik |
| `stock_movements` | `id` | kartu stok |
| `goods_receipts` | `id` | `noReceipt` unik |
| `goods_receipt_details` | `id` (otomatis) | |
| `goods_issues` | `id` | `noIssue` unik |
| `goods_issue_details` | `id` (otomatis) | |

Enum `MovementType` dan `DocumentStatus` disimpan sebagai teks melalui
`Converters`.

Data contoh dimasukkan oleh `DatabaseSeeder.seedIfEmpty`, dipanggil dari blok
`init` pada `AppContainer` di atas coroutine `Dispatchers.IO`. Pengisian
dibungkus satu transaksi dan berhenti lebih awal bila tabel `items` sudah terisi,
sehingga aman dipanggil berulang kali.

## Sisa pekerjaan

- Buka proyek di Android Studio, jalankan Gradle Sync, dan pastikan
  `./gradlew assembleDebug` hijau. Room dan KSP belum pernah dikompilasi.
- Buat repositori GitHub, undang tiga anggota lain sebagai kolaborator.
- Pastikan setiap anggota mengatur `git config user.email` sesuai surel akun
  GitHub masing-masing sebelum commit pertama (lihat CONTRIBUTING.md).
