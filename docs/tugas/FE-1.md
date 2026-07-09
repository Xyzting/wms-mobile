# FE-1 — Tema, Login, Dashboard, Stok Gudang

**Penanggung jawab:** M. Hafizul Hadi (24552011218)

## Ruang lingkup

Menetapkan tema visual aplikasi dan membangun tiga layar yang hanya membaca data.

## Berkas yang dimiliki

```
app/src/main/java/com/utb/wms/ui/
├── theme/{Color.kt, Type.kt, Theme.kt}
├── login/{LoginScreen.kt, LoginViewModel.kt}
├── dashboard/DashboardScreen.kt
└── inventory/{InventoryScreen.kt, InventoryViewModel.kt}
```

### Berkas milik BE-1 yang boleh disentuh

- `MainActivity.kt` — satu baris saja. Setelah `ui/theme/` siap, ganti
  `MaterialTheme {` menjadi `WMSMobileTheme {`.
- `ui/navigation/WmsNavHost.kt` — hanya tiga blok `composable` bertanda
  `penanggungJawab = "FE-1"`, yaitu `Routes.LOGIN`, `Routes.DASHBOARD`, dan
  `Routes.INVENTORY`. Ganti `ComingSoonScreen(...)` dengan layar Anda.

## Pola layar

Tulis layar **tanpa keadaan**: seluruh data masuk lewat parameter, seluruh
kejadian keluar lewat lambda. Pembungkusnya, `*Route`, yang menghubungkan layar
ke ViewModel. Dengan pola ini `@Preview` sudah bisa dipakai sebelum BE-2 selesai.

```kotlin
@Composable
fun LoginRoute(onLoginSuccess: () -> Unit) {
    val container = appContainer()
    val viewModel: LoginViewModel = viewModel { LoginViewModel(container.authRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginScreen(
        state = state,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = { viewModel.login(onLoginSuccess) },
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
) { ... }
```

`appContainer()` ada di `ui/common/AppContainerAccess.kt`. Panggil hanya dari
`*Route`, jangan pernah dari layar yang dipratinjau `@Preview` — pratinjau tidak
punya `WmsApplication`, sehingga akan gagal.

## Kontrak yang dipakai

| Layar | Antarmuka | Fungsi |
|---|---|---|
| Login | `AuthRepository` | `login(username, password): User?` — `null` berarti gagal |
| Dashboard | `AuthRepository` | `currentUser: StateFlow<User?>`, `logout()` |
| Stok Gudang | `InventoryRepository` | `observeStocks(): Flow<List<Stock>>` |

Impor hanya dari `com.utb.wms.domain.repository` dan `com.utb.wms.domain.model`.
Paket `com.utb.wms.data` **dilarang** diimpor dari `ui/`.

## Rincian layar

### Login

Kolom username dan password, tombol masuk. Bila `login()` mengembalikan `null`,
tampilkan pesan galat "Username atau kata sandi salah". Setelah berhasil,
panggil `onLoginSuccess()` — navigasinya sudah diatur di `WmsNavHost`.

Akun contoh ada di README.

### Dashboard

Tampilkan nama dan role pengguna dari `currentUser`, lalu tiga tombol menuju
Penerimaan Barang, Pengeluaran Barang, dan Stok Gudang. Sediakan tombol keluar
yang memanggil `logout()` lalu kembali ke Login.

### Stok Gudang

Daftar seluruh baris stok: SKU, nama barang, lokasi, jumlah, dan satuan.
Beri penanda visual bila `stock.jumlahStok < stock.item.stokMinimum`.

## Catatan penting

Sampai BE-2 menyelesaikan bagiannya, memanggil repository saat aplikasi berjalan
akan melempar `NotImplementedError`. Ini normal. Kerjakan tampilan lebih dahulu
dan verifikasi lewat `@Preview` dengan data buatan.

## Selesai bila

- `WMSMobileTheme` dipakai di `MainActivity`.
- Ketiga layar tampil dan dapat dinavigasi.
- `./gradlew assembleDebug` hijau.
