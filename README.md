# WMS Mobile (Android · Kotlin)

Aplikasi **Warehouse Management System** versi mobile — implementasi dari desain
OOAD (UAS OOAD, WMS). Dibangun dengan **Kotlin + Jetpack Compose (Material 3)**,
arsitektur **MVVM + Repository**, dan persistensi **Room**.

## Tautan

| Sumber | Tautan |
|---|---|
| Video demo aplikasi | _belum tersedia_ |
| Dokumen desain OOAD | [Buka di Google Docs](https://docs.google.com/document/d/1bBeJVVsAAmToXrQMYOm4iHlNcDzRNlTU/edit?usp=sharing) |

Ganti baris video demo dengan format `[Tonton di YouTube](https://...)` setelah
tautannya siap.

## Fitur

| Fitur | Keterangan |
|---|---|
| Login | Autentikasi dengan akun demo |
| Dashboard | Menu utama, menampilkan nama dan role pengguna |
| Penerimaan Barang | Goods Receipt multi-baris, menambah stok |
| Pengeluaran Barang | Goods Issue, menolak dokumen bila stok tidak mencukupi |
| Stok Gudang | Saldo stok tiap lokasi + penanda stok minimum |

Penerimaan dan pengeluaran barang ditulis dalam satu transaksi basis data, dan
pengeluaran memeriksa ketersediaan seluruh baris sebelum menulis apa pun,
sehingga stok tidak pernah menjadi minus.

## Cara menjalankan

1. Buka folder ini di **Android Studio** (2024.1 / Koala ke atas).
2. Tunggu proses **Gradle Sync** selesai.
3. **Run** di emulator atau perangkat Android (minimal Android 8.0 / API 26).

### Akun demo

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `operator` | `operator123` | Operator |
| `supervisor` | `supervisor123` | Supervisor |

Data contoh dimuat otomatis saat aplikasi pertama kali dijalankan.

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
└── ui/               layar Compose + navigasi
```

Aturan ketergantungan: `ui/` dan `data/` sama-sama bergantung pada `domain/`.
Paket `ui/` **tidak boleh** mengimpor `data/`.

## Pembagian kerja tim

Proyek ini dikerjakan empat orang. Aturan kolaborasi dan kepemilikan berkas ada
di [CONTRIBUTING.md](CONTRIBUTING.md).

| Peran | Anggota | NIM | Ruang lingkup |
|---|---|---|---|
| BE-1 | Reyhan Fathir Alamsyah | 24552011032 | Kerangka proyek, model, kontrak repository, Room, DI, navigasi |
| BE-2 | Nazka Yasir Alman Paluthi | 24552011087 | Implementasi kontrak repository + unit test |
| FE-1 | M. Hafizul Hadi | 24552011218 | Tema, layar Login, Dashboard, Stok Gudang |
| FE-2 | Radhitias Salman Syam | 24552011112 | Komponen bersama, layar Penerimaan & Pengeluaran Barang |

## Catatan

Jika `./gradlew` gagal karena `gradle-wrapper.jar` belum tersedia, buka proyek
lewat Android Studio (berkas tersebut dibuat ulang otomatis) atau jalankan
`gradle wrapper`.
