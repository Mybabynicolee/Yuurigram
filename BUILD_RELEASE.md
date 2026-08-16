# Yuurigram release build

Repository ini membangun Yuurigram menggunakan GitHub-hosted Ubuntu runner. Workflow menghasilkan **signed release APK** dan **AAB**, bukan debug build, lalu menyimpannya sebagai artifact selama 14 hari.

## Repository Secrets

Buka **Settings → Secrets and variables → Actions → New repository secret**. Masukkan secret berikut tanpa menuliskan nilainya ke source code:

| Secret | Isi |
|---|---|
| `TELEGRAM_API_ID` | API ID aplikasi Telegram milikmu |
| `TELEGRAM_API_HASH` | API hash aplikasi Telegram milikmu |
| `RELEASE_KEYSTORE_BASE64` | File keystore release yang sudah diubah menjadi Base64 satu baris |
| `RELEASE_KEY_PASSWORD` | Password keystore key |
| `RELEASE_KEY_ALIAS` | Alias key pada keystore |
| `RELEASE_STORE_PASSWORD` | Password keystore |

Untuk mengubah keystore menjadi Base64, jalankan perintah berikut di komputer pribadi dan jangan memasukkan file keystore ke commit:

```bash
base64 -w 0 release.keystore
```

Workflow akan gagal secara sengaja apabila salah satu secret wajib belum tersedia. Nilai API dan signing tidak dicetak ke log.

## Menjalankan build

Buka tab **Actions**, pilih **Build Yuurigram Release**, tekan **Run workflow**, kemudian tunggu job selesai. APK dan AAB dapat diunduh pada bagian **Artifacts**. Untuk repository publik, GitHub-hosted runner umumnya tersedia tanpa biaya tambahan sesuai kuota GitHub Actions.

## Batas akun

Yuurigram memakai kapasitas praktis **100 akun** pada build ini. Android tidak dapat menjamin akun benar-benar tidak terbatas karena setiap sesi membutuhkan penyimpanan, koneksi jaringan, notifikasi, dan memori. Slot akun yang belum dipakai tidak diinisialisasi penuh saat startup.

## Catatan API dan layanan Google

Application ID Android diubah menjadi `ja.yuurigram.org` dan nama aplikasi menjadi `Yuurigram`. Konfigurasi Firebase resmi Telegram tidak digunakan oleh workflow ini. Jika push notification atau layanan Google ingin diaktifkan untuk Yuurigram, buat konfigurasi Firebase sendiri untuk package tersebut dan tambahkan melalui secret atau file konfigurasi privat; jangan memakai kredensial resmi upstream.
