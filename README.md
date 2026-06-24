# FinNote

<p align="center">
  <img width="300" alt="Fin" src="https://github.com/user-attachments/assets/529f6967-5ab6-4f39-a553-59ac6e32f4a5" />
</p>

FinNote adalah aplikasi pencatat keuangan (*expense tracker*) Android yang dirancang khusus untuk mahasiswa. Mengusung filosofi *"The Quiet Pocket Ledger"*, aplikasi ini fokus pada antarmuka yang tenang, bersih, dan memampukan pengguna untuk mengecek kondisi keuangan mereka dalam hitungan detik tanpa visual yang mengintimidasi.

## Fitur Utama

- **Pencatatan Cepat**: Tambah data pemasukan dan pengeluaran harian lengkap dengan tanggal, kategori, dan deskripsi.
- **Batas Anggaran (Budget Limit)**: Pantau batas pengeluaran bulanan dengan indikator *progress bar* yang intuitif dan tidak menghakimi.
- **Sistem Autentikasi**: Alur masuk (*Login*), daftar (*Register*), dan lupa sandi yang mulus dengan arsitektur *Single-Activity* dan animasi transisi kustom.
- **Riwayat Terstruktur**: Seluruh transaksi dikelompokkan secara otomatis berdasarkan bulan berjalan.
- **Mode Tamu (Guest Mode)**: Akses *read-only* agar pengguna dapat mencoba aplikasi tanpa keharusan membuat akun.

## Tech Stack

- **Bahasa**: Kotlin
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Database**: SQLite (Lokal via `SQLiteOpenHelper`)
- **Antarmuka**: XML Layouts, Material Design Components

## Filosofi Desain

FinNote sengaja menghindari antarmuka aplikasi keuangan korporat yang kaku atau *dashboard* yang terlalu padat. 
- **Warna**: Menggunakan palet *mint* yang menenangkan (`#ecf9f2`) dipadukan dengan kartu putih bersih. Warna merah (*coral*) digunakan murni sebagai penanda arah uang (pengeluaran), bukan peringatan panik atau *error*.
- **Tipografi**: Inter sebagai standar taktis untuk memastikan keterbacaan angka sekilas (*glanceable*).

## Unduh APK

File APK FinNote dapat diunduh melalui halaman berikut: [FinNote v1.0.1](https://github.com/kuenobaikbanget/finnote/releases/tag/1.0.1).
