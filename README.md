# WMS Mobile (Android · Kotlin)

Aplikasi **Warehouse Management System** versi mobile — implementasi dari desain
OOAD (UAS OOAD, WMS). Dibangun dengan **Kotlin Native**, arsitektur
**MVVM + Repository**, dan persistensi **Room**.

Lapisan tampilan memakai `AppCompatActivity` tunggal, `Fragment` di bawah
Navigation Component, dan `RecyclerView` untuk seluruh daftar. Sebagian layar
formulir digambar dengan Jetpack Compose melalui `ComposeView`.

## Tautan

| Sumber | Tautan |
|---|---|
| Video demo aplikasi | _belum tersedia_ |
| Dokumen desain OOAD | [Buka di Google Docs](https://docs.google.com/document/d/1bBeJVVsAAmToXrQMYOm4iHlNcDzRNlTU/edit?usp=sharing) |
| Laporan OOAD (PDF) | [docs/UAS_OOAD_WMS.pdf](docs/UAS_OOAD_WMS.pdf) |
| Berkas rilis | [apk/](apk/) |

Ganti baris video demo dengan format `[Tonton di YouTube](https://...)` setelah
tautannya siap.

## Fitur

Mengikuti Use Case Diagram pada dokumen OOAD: 21 use case, 4 aktor.

| Paket | Use case | Status |
|---|---|---|
| Autentikasi | Login, Logout | ✅ |
| | View Profile | ⏳ |
| Master Data | Manage Master Data (item, lokasi, pemasok) | ⏳ |
| | Search Item, View Item Catalog | ⏳ |
| Administrasi | Manage Users, Manage Roles | ⏳ |
| Inbound | Create Goods Receipt | ✅ |
| | Approve Goods Receipt, View Receipt History | ⏳ |
| | Scan Barcode | ⏳ |
| Outbound | Create Goods Issue | ✅ |
| | Approve Goods Issue, View Issue History | ⏳ |
| Inventory | View Stock | ✅ |
| | View Stock Movement, Stock Adjustment | ⏳ |
| Reporting | Generate Stock Report, Generate Mutation Report | ⏳ |
| | Export Report | ⏳ |

Dokumen penerimaan dan pengeluaran barang mengikuti siklus
**Draft → Validated → Posted**, dan dapat dibatalkan selama belum `Posted`.
Stok hanya bergerak pada saat dokumen `Posted`. Seluruh pergerakan ditulis
dalam satu transaksi basis data, dan pengeluaran memeriksa ketersediaan seluruh
baris sebelum menulis apa pun, sehingga stok tidak pernah menjadi minus.

Menu yang tampil pada Dashboard disaring berdasarkan role pengguna. Operator
tidak melihat menu persetujuan maupun administrasi.

## Cara clone dan menjalankan

```bash
git clone https://github.com/Xyzting/wms-mobile.git
cd wms-mobile
```

Lewat Android Studio (2024.1 / Koala ke atas):

1. **File → Open**, pilih folder hasil clone.
2. Tunggu **Gradle Sync** selesai. Berkas `local.properties` dibuat otomatis.
3. **Run** pada emulator atau perangkat Android, minimal Android 8.0 (API 26).

Lewat baris perintah:

```bash
./gradlew assembleDebug      # Windows: .\gradlew assembleDebug
./gradlew test               # menjalankan unit test
```

Wrapper Gradle sudah disertakan, jadi Gradle tidak perlu dipasang lebih dahulu.
Yang dibutuhkan hanya **JDK 17** dan **Android SDK API 34**.

### Akun demo

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `operator` | `operator123` | Operator |
| `supervisor` | `supervisor123` | Supervisor |

Data contoh dimuat otomatis saat aplikasi pertama kali dijalankan: enam item,
empat lokasi, tiga pemasok.

## Tangkapan layar

_Menyusul._

## Struktur

```
app/src/main/java/com/utb/wms/
├── domain/
│   ├── model/        entity WMS (Item, Stock, GoodsReceipt, GoodsIssue, ...)
│   └── repository/   kontrak repository (interface)
├── data/
│   ├── local/        Room: entity, DAO, relasi, mapper, seeder
│   └── repository/   implementasi kontrak repository
├── di/               AppContainer (dependency injection manual)
└── ui/               Activity, Fragment, ViewModel, layar Compose
```

Aturan ketergantungan: `ui/` dan `data/` sama-sama bergantung pada `domain/`.
Paket `ui/` **tidak boleh** mengimpor `data/`.

## Komponen native yang dipakai

| Komponen | Contoh berkas |
|---|---|
| `Activity` | `MainActivity.kt` |
| `Fragment` | `ui/login/LoginFragment.kt` dan seluruh layar lain |
| `RecyclerView` | daftar stok, riwayat dokumen, katalog item |
| `Intent` | `ACTION_SEND` untuk ekspor laporan, `ACTION_DIAL` untuk kontak pemasok |

## Pembagian kerja tim

Proyek ini dikerjakan empat orang. Aturan kolaborasi dan kepemilikan berkas ada
di [CONTRIBUTING.md](CONTRIBUTING.md).

| Peran | Anggota | NIM | Ruang lingkup |
|---|---|---|---|
| BE-1 | Reyhan Fathir Alamsyah | 24552011032 | Kerangka proyek, model, kontrak repository, Room, DI, navigasi |
| BE-2 | Nazka Yasir Alman Paluthi | 24552011087 | Implementasi kontrak repository + unit test |
| FE-1 | M. Hafizul Hadi | 24552011218 | Tema, Login, Dashboard, Profil, Master Data, Administrasi, Laporan |
| FE-2 | Radhitias Salman Syam | 24552011112 | Komponen bersama, Inventory, Penerimaan & Pengeluaran Barang |
