# Nasyah Cell - APK Pulsa Standar

Project Android native sederhana untuk aplikasi transaksi pulsa Nasyah Cell.

## Status versi 1.0
- Tampilan Android untuk input nomor HP.
- Pilihan nominal pulsa.
- Validasi input dasar.
- Belum mengirim transaksi ke OtomaX (aman sebagai prototipe).
- Belum ada login, saldo, deposit, atau koneksi database.

## Cara membuka dan membuat APK
1. Instal Android Studio pada PC Windows 10.
2. Ekstrak folder project ini.
3. Android Studio > Open > pilih folder `NasyahCell_Pulsa_Android`.
4. Tunggu Gradle Sync selesai. Jika diminta, instal Android SDK Platform 35.
5. Pilih Build > Build Bundle(s) / APK(s) > Build APK(s).
6. APK hasil build biasanya ada di `app/build/outputs/apk/debug/app-debug.apk`.

## Integrasi OtomaX berikutnya
Jangan isi alamat API atau kredensial database ke dalam APK.
Backend Nasyah Cell harus menjadi perantara. Sebelum integrasi diperlukan:
- URL API resmi OtomaX yang sesuai dengan versi 3.9.1.
- Metode autentikasi dan dokumentasi endpoint transaksi pulsa.
- Kode produk pulsa dari OtomaX.
- Endpoint cek status transaksi.

Jangan mengirim transaksi produksi sebelum pengujian dan konfirmasi API berhasil.
