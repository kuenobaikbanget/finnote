# Panduan Code Review FinNote (Gemini Code Assist)

Repositori `finnote` adalah aplikasi Android native (Kotlin, AGP/Gradle Kotlin DSL, XML Layouts, Material Design Components, SQLite via `SQLiteOpenHelper`, dan `MPAndroidChart`). Seluruh review Pull Request WAJIB mengikuti aturan di bawah ini.

## 1. Bahasa dan Gaya Komunikasi (Wajib)

- **Seluruh** ringkasan PR (*PR summary*), komentar review inline, dan saran perbaikan **WAJIB ditulis dalam Bahasa Indonesia** yang jelas, langsung ke inti masalah, objektif, dan sopan.
- Istilah teknis, nama class, fungsi, variabel, API Android, dan potongan kode tetap menggunakan nama aslinya (contoh: `SQLiteOpenHelper`, `Cursor`, `onDestroyView`, `ValueAnimator`, `RecyclerView`, `SimpleDateFormat`).
- Berikan saran perbaikan yang konkret dan dapat langsung diterapkan, bukan sekadar kritik umum.

## 2. Format Keparahan Komentar (`LOW` ke atas)

Terapkan standar review yang ketat mulai dari tingkat keparahan `LOW` ke atas. Awali **setiap** komentar review dengan salah satu label berikut agar mudah diprioritaskan:

- `[KRITIS]` — Bug fatal, potensi crash tinggi, kerentanan keamanan serius, kebocoran data pengguna, atau korupsi data finansial.
- `[TINGGI]` — Bug logika signifikan, memory/resource leak (`Cursor`, `ValueAnimator`, `Dialog`, listener), race condition, atau masalah integritas perhitungan keuangan.
- `[SEDANG]` — Penanganan *edge case* yang terlewat, error handling yang menelan exception diam-diam, operasi berat di main thread, atau kurangnya cakupan test.
- `[RENDAH]` — Peningkatan keterbacaan, penyederhanaan kode tanpa mengubah perilaku utama, atau inkonsistensi kecil yang berdampak pada maintainability.

## 3. Aspek yang Wajib Diperiksa (Sesuai Tech Stack FinNote)

### A. Bug Logika & Edge Cases
- Periksa validasi input transaksi dan anggaran: nominal `0`, nilai negatif, input kosong/spasi, batas panjang teks, tanggal di akhir/awal bulan, serta kondisi saat daftar transaksi kosong (*empty state*).
- Periksa konsistensi parsing dan pemformatan tanggal (`yyyy-MM-dd` dengan `Locale.US` untuk penyimpanan/pengurutan vs format tampil `id-ID`) agar pengelompokan transaksi bulanan tidak meleset karena perbedaan zona waktu atau locale perangkat.

### B. Null Safety & Keamanan Tipe Kotlin
- Waspadai penggunaan `!!` (*non-null assertion*), type cast tidak aman (`as`), akses `lateinit` yang belum tentu terinisialisasi (seperti `DataStore.dbHelper` atau view binding manual), dan asumsi `Cursor` memiliki baris/kolom tanpa pengecekan aman.
- Pastikan data nullable dari `Bundle`, `Intent`, `Uri` (misal foto profil), dan query database ditangani dengan aman.

### C. State Management, Lifecycle & Resource Disposal
- Pada `Activity`, `Fragment`, dan custom `View` (misalnya `RecentTransactionsView`, `LedgerStampView`): pastikan `ValueAnimator` dihentikan (`cancel()`), `BottomSheetDialog`/`AlertDialog` di-`dismiss()`, `TextWatcher`/listener dilepas, dan referensi `View` dibersihkan di `onDestroyView()` atau `onDetachedFromWindow()` untuk mencegah memory leak dan crash saat view sudah dihancurkan.
- Pastikan pembaruan UI dari callback asinkron atau dialog memeriksa status lifecycle (`isAdded`, `view != null`, `viewLifecycleOwner`).
- Periksa pemulihan state saat perubahan konfigurasi (rotasi layar) atau *process death*, termasuk draft form transaksi dan mode tampilan.
- Pastikan seluruh `Cursor` ditutup menggunakan `.use { ... }`.

### D. Error Handling & Database SQLite (`FinNoteDatabaseHelper` & `DataStore`)
- Waspadai blok `catch (_: Exception)` yang menelan kesalahan secara diam-diam saat operasi tulis/baca SQLite gagal sehingga pengguna mengira data tersimpan padahal tidak.
- Pastikan seluruh query SQLite menggunakan parameter binding (`?` dan `selectionArgs`), bukan string concatenation.
- Periksa perubahan skema di `FinNoteDatabaseHelper.onUpgrade`: pastikan tidak menghapus data transaksi atau anggaran pengguna secara tidak sengaja (`DROP TABLE`) tanpa migrasi yang aman.
- Untuk operasi database multi-langkah yang saling bergantung, pastikan atomisitas dengan transaksi SQLite (`beginTransaction()` / `setTransactionSuccessful()` / `endTransaction()`).

### E. Keamanan & Integritas Data Finansial
- Pastikan tidak ada penyimpanan rahasia, kredensial, token, atau data sensitif secara plaintext di source code, `strings.xml`, log (`Log.*`), atau tabel `preferences`.
- Validasi URI eksternal, input file/gambar profil, dan seluruh input pengguna sebelum disimpan atau ditampilkan.
- Periksa akurasi dan integritas data finansial:
  - Perhatikan risiko integer overflow pada `Int` ketika menjumlahkan nominal transaksi besar atau akumulasi anggaran bulanan (pertimbangkan `Long` atau `BigDecimal` bila diperlukan).
  - Periksa perhitungan persentase progres anggaran, pembulatan (*rounding*), pemisah ribuan, dan konsistensi format mata uang IDR (`id-ID`).
  - Pastikan pemisahan tipe transaksi (`income` vs `expense`) konsisten di model, query, grafik (`MPAndroidChart`), dan ringkasan saldo.

### F. Performa UI, Async & Kompatibilitas Android
- Hindari query SQLite yang berulang di dalam loop atau di `onBindViewHolder`, serta operasi I/O / decoding gambar `Uri` berat di main thread yang dapat menyebabkan *jank*/ANR.
- Periksa efisiensi pembaruan `RecyclerView` (gunakan `DiffUtil` / `ListAdapter` daripada `notifyDataSetChanged()` jika memungkinkan) dan hindari alokasi objek berlebihan di dalam `onDraw` (misal custom `View` / `TransactionBarChartRenderer`).
- Perhatikan kompatibilitas API Android (`minSdk = 24` hingga `targetSdk = 36`), penanganan `WindowInsets` / *edge-to-edge*, serta hindari hardcoded user-facing string (gunakan `strings.xml`).

### G. Kecukupan Test
- Pastikan perubahan pada logika bisnis (perhitungan saldo/anggaran, validasi input, formatting, model `Transaction`, atau operasi `DataStore`) disertai unit test (`app/src/test`) atau instrumented test (`app/src/androidTest`) yang mencakup skenario normal maupun *edge case*.

## 4. Fokus pada Hal Bermakna (Hindari Nitpicking)

- **Jangan** mengomentari hal-hal pemformatan sepele (indentasi, spasi, panjang baris, urutan import, atau gaya penulisan Kotlin standar) yang sudah diatur oleh `kotlin.code.style=official` dan Android Lint.
- Fokuskan seluruh komentar pada kebenaran logika, keamanan, integritas data keuangan, manajemen lifecycle/memori, performa, dan keandalan test.
