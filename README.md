# WMS Mobile (Android · Kotlin)

Aplikasi **Warehouse Management System** versi mobile — implementasi dari desain
OOAD (UAS OOAD, WMS). Dibangun dengan **Kotlin + Jetpack Compose (Material 3)**,
arsitektur **MVVM + Repository**, dan persistensi **Room**.

## Fitur (target MVP)

| Fitur | Keterangan | Status |
|---|---|---|
| Login | Autentikasi dengan akun demo | Rangka |
| Dashboard | Menu utama | Rangka |
| Penerimaan Barang | Goods Receipt multi-baris, menambah stok | Rangka |
| Pengeluaran Barang | Goods Issue, validasi ketersediaan stok | Rangka |
| Stok Gudang | Saldo stok + penanda stok minimum | Rangka |

Kelima layar sudah punya rute navigasi dan tampil sebagai layar sementara.
Aplikasi **sudah dapat di-build dan dijalankan** meskipun isi tiap fitur belum lengkap.

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

Proyek ini dikerjakan empat orang. Rincian tugas per anggota ada di
[`docs/tugas/`](docs/tugas/), aturan kolaborasi ada di [CONTRIBUTING.md](CONTRIBUTING.md).

| Peran | Ruang lingkup |
|---|---|
| BE-1 | Kerangka proyek, model, kontrak repository, Room, DI, navigasi |
| BE-2 | Implementasi kontrak repository + unit test |
| FE-1 | Tema, layar Login, Dashboard, Stok Gudang |
| FE-2 | Komponen bersama, layar Penerimaan & Pengeluaran Barang |

## Catatan

Jika `./gradlew` gagal karena `gradle-wrapper.jar` belum tersedia, buka proyek
lewat Android Studio (berkas tersebut dibuat ulang otomatis) atau jalankan
`gradle wrapper`.
