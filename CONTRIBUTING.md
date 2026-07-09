# Panduan Kontribusi

Proyek ini dikerjakan oleh empat orang. Panduan ini menjaga agar pekerjaan
setiap anggota tercatat atas namanya sendiri dan tidak saling menimpa.

## 1. Menyiapkan identitas Git

**Wajib dilakukan sebelum commit pertama.** GitHub hanya mengakui sebuah commit
sebagai kontribusi Anda apabila alamat surel pada commit tersebut terdaftar di
akun GitHub Anda. Bila tidak, nama Anda tidak akan muncul pada grafik kontributor.

```bash
git config user.name  "Nama Lengkap"
git config user.email "surel-yang-terdaftar-di-github@contoh.com"
```

Periksa hasilnya:

```bash
git log -1 --format='%an <%ae>'
```

Alamat surel di atas harus sama persis dengan salah satu surel pada
**GitHub → Settings → Emails**.

## 2. Alur kerja

1. Buat cabang sendiri dari `main`:

   ```bash
   git switch -c feat/dev-<nama>
   ```

2. Kerjakan hanya berkas milik peran Anda (lihat tabel kepemilikan di bawah).
3. Pastikan proyek masih dapat di-build sebelum push:

   ```bash
   ./gradlew assembleDebug
   ./gradlew test
   ```

4. Push cabang, lalu buka Pull Request ke `main`.
5. Minimal satu anggota lain menyetujui sebelum PR digabungkan.

## 3. Format pesan commit

Gunakan pola `<tipe>(<cakupan>): <ringkasan>` dengan huruf kecil dan kalimat
bahasa Indonesia. Tipe yang dipakai:

| Tipe       | Penggunaan                                |
| ---------- | ----------------------------------------- |
| `feat`     | menambah kemampuan baru                   |
| `fix`      | memperbaiki kesalahan                     |
| `test`     | menambah atau mengubah pengujian          |
| `refactor` | merapikan kode tanpa mengubah perilaku    |
| `docs`     | mengubah dokumentasi                      |
| `chore`    | konfigurasi, dependensi, berkas pendukung |

Contoh: `feat(data): implementasi InboundRepositoryImpl`.

## 4. Kepemilikan berkas

Setiap berkas dimiliki satu peran. **Jangan mengubah berkas milik peran lain.**

| Peran | Anggota                   | Berkas                                                                                                                                                                    |
| ----- | ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BE-1  | Reyhan Fathir Alamsyah    | konfigurasi Gradle, `AndroidManifest.xml`, `res/`, `WmsApplication.kt`, `di/`, `domain/`, `data/local/`, `ui/navigation/`, `ui/common/AppContainerAccess.kt`, dokumentasi |
| BE-2  | Nazka Yasir Alman Paluthi | `data/repository/*Impl.kt`, `app/src/test/`                                                                                                                               |
| FE-1  | M. Hafizul Hadi           | `ui/theme/`, `ui/login/`, `ui/dashboard/`, `ui/inventory/`                                                                                                                |
| FE-2  | Radhitias Salman Syam     | `ui/common/Components.kt`, `ui/inbound/`, `ui/outbound/`                                                                                                                  |

### Pengecualian yang diizinkan

Dua berkas milik BE-1 boleh disentuh peran lain, terbatas pada baris tertentu:

- `ui/navigation/WmsNavHost.kt` — setiap peran FE hanya boleh mengubah blok
  `composable(...)` miliknya sendiri. Blok sudah diurutkan agar milik FE-1 dan
  FE-2 tidak bersebelahan, sehingga Git dapat menggabungkannya otomatis.
- `MainActivity.kt` — hanya FE-1, dan hanya untuk mengganti `MaterialTheme`
  menjadi `WMSMobileTheme` setelah `ui/theme/` selesai.

## 5. Aturan arsitektur

- Paket `ui/` **dilarang** mengimpor `com.utb.wms.data`. Layar dan ViewModel
  hanya boleh memakai antarmuka dari `com.utb.wms.domain.repository`.
- Antarmuka repository adalah kontrak bersama. Bila sebuah tanda tangan fungsi
  perlu berubah, bicarakan dahulu — perubahannya berdampak ke tiga peran lain.
- Layar Compose ditulis **tanpa keadaan** (stateless): seluruh data masuk lewat
  parameter, seluruh kejadian keluar lewat lambda. Pembungkusnya, misalnya
  `LoginRoute`, yang menghubungkannya ke ViewModel. Dengan begitu FE dapat
  memakai `@Preview` tanpa menunggu BE-2 selesai.
- `appContainer()` hanya boleh dipanggil dari `*Route`, tidak pernah dari layar
  yang dipratinjau `@Preview`.
