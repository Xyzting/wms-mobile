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

| Peran | Anggota                   | Berkas                                                                                                                                                        |
| ----- | ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BE-1  | Reyhan Fathir Alamsyah    | konfigurasi Gradle, `gradlew*`, `AndroidManifest.xml`, `MainActivity.kt`, `WmsApplication.kt`, `di/`, `domain/`, `data/local/`, `res/navigation/`, `res/values/`, `ui/common/AppContainerAccess.kt`, dokumentasi |
| BE-2  | Nazka Yasir Alman Paluthi | `data/repository/*Impl.kt`, `app/src/test/`                                                                                                                   |
| FE-1  | M. Hafizul Hadi           | `ui/theme/`, `ui/login/`, `ui/dashboard/`, `ui/profile/`, `ui/masterdata/`, `ui/catalog/`, `ui/admin/`, `ui/report/`                                          |
| FE-2  | Radhitias Salman Syam     | `ui/common/Components.kt`, `ui/inventory/`, `ui/inbound/`, `ui/outbound/`                                                                                     |

Layar Fragment membawa serta berkas tata letaknya. Pemilik `ui/login/` juga
memiliki `res/layout/fragment_login.xml`, dan seterusnya.

Sejak fase 2, `ui/inventory/` berpindah dari FE-1 ke FE-2 agar seluruh layar
yang menyentuh saldo stok berada pada satu tangan.

Seluruh destinasi `nav_graph.xml` kini menunjuk Fragment yang sesungguhnya.
`ComingSoonFragment`, penampung sementara yang dulu mengisi destinasi yang belum
dikerjakan, sudah dihapus.

### Pengecualian yang diizinkan

- `res/navigation/nav_graph.xml` — milik BE-1. Destinasi sudah diurutkan agar
  milik FE-1 dan FE-2 tidak bersebelahan, sehingga Git dapat menggabungkannya
  otomatis. Kini seluruh destinasi sudah terisi; berkas ini tidak perlu diubah
  lagi.
- `res/values/strings.xml` — **jangan disentuh peran FE.** Tulis teks baru pada
  berkas sendiri: FE-1 memakai `res/values/strings_fe1.xml`, FE-2 memakai
  `res/values/strings_fe2.xml`. Android menggabungkan seluruh berkas di
  `res/values/` menjadi satu, jadi `R.string.*` tetap bekerja seperti biasa dan
  tidak ada dua orang yang menulis pada berkas yang sama.
- `res/values/themes.xml`, `colors.xml`, `styles.xml`, `values-night/` — milik
  BE-1 secara nominal, tetapi disusun oleh FE-1 pada commit fondasi tema, sekali
  saja, sebelum pekerjaan tampilan yang lain dimulai. Sesudah itu tidak ada yang
  menyentuhnya.
- `app/build.gradle.kts` — milik BE-1. Bila sebuah pustaka baru dibutuhkan,
  mintakan kepada BE-1, jangan tambahkan sendiri.

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
- Perpindahan antar layar hanya lewat `findNavController().navigate(R.id.action_*)`.
  Sebuah Fragment tidak pernah membuat Fragment lain secara langsung.
- Penyaringan menu memakai berkas `domain/model/Permission.kt`, bukan
  perbandingan `namaRole` yang ditulis ulang di tiap layar.

## 6. Selama logika repository belum terisi

Rangka `data/repository/*Impl.kt` memakai `TODO("BE-2: ...")`. Berkas tersebut
**dapat dikompilasi**, tetapi memanggilnya saat aplikasi berjalan akan
melemparkan `NotImplementedError`. Karena itu FE dapat mulai menyusun tata letak
dan `@Preview` lebih dahulu, dan baru menyambungkannya ke ViewModel setelah
BE-2 menyelesaikan bagiannya.
