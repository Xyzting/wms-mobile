# Berkas rilis

Folder ini menampung `app-release.apk`, satu dari empat komponen yang wajib
dikumpulkan pada UAS.

## Cara membuat

```bash
./gradlew assembleRelease           # Windows: .\gradlew assembleRelease
```

Hasilnya berada di `app/build/outputs/apk/release/app-release.apk`. Salin ke
folder ini, lalu commit.

## Penandatanganan

Build rilis ditandatangani memakai `signingConfig` bawaan `debug`, yang merujuk
`~/.android/debug.keystore`. Berkas kunci itu dibuat sendiri oleh Android Studio
pada setiap komputer, sehingga tidak ada kata sandi maupun berkas kunci yang
ikut tersimpan di dalam repositori.

Tanpa penandatanganan, `assembleRelease` hanya menghasilkan
`app-release-unsigned.apk` yang **tidak dapat dipasang** di perangkat mana pun.
Ketentuan UAS meminta berkas yang siap diinstal, karena itu konfigurasi ini
diperlukan.

Aplikasi ini tidak diedarkan lewat Google Play, jadi kunci rilis khusus tidak
dibutuhkan. Bila suatu saat dibutuhkan, buat keystore sendiri lalu simpan kata
sandinya di `local.properties`, jangan di `build.gradle.kts`.

## Kapan dibuat

**Paling akhir**, setelah seluruh Pull Request digabungkan ke `main`. Seluruh
layar kini sudah terisi, jadi APK yang dibuat dari `main` sudah utuh.

Sebelum menyalin APK ke sini, pasang dulu di perangkat sungguhan dan telusuri
setiap menu, di mode terang **dan** mode gelap. Ketentuan UAS menyebut aplikasi
yang mengalami force close akan mengurangi nilai secara signifikan.

```bash
adb install -r apk/app-release.apk
adb shell cmd uimode night yes     # telusuri mode gelap
adb shell cmd uimode night auto    # kembalikan setelah selesai
```
