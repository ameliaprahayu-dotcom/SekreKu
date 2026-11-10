# SekreKu — Aplikasi Administrasi Sekretaris (Offline)

Aplikasi Android offline untuk inventaris kantor, notulensi rapat, dan agenda.
Seluruh data tersimpan di HP (Room Database). Tanpa internet, cloud, atau server.

Tugas UTS Praktikum Pemrograman Perangkat Bergerak (SIFWP22523) — Universitas Trilogi.

## Teknologi
Kotlin, Jetpack Compose, Material 3, Room (KSP), Navigation Compose, Coil (foto lokal).

## Fitur
| Modul | Isi |
|---|---|
| Dashboard | Ringkasan jumlah data + agenda belum selesai, menu ke 3 modul, saklar tema terang/gelap |
| Inventaris Kantor | Nama, kategori, jumlah, lokasi, kondisi, foto |
| Notulensi Rapat | Judul, tanggal, peserta, pembahasan, tindak lanjut, foto lampiran |
| Agenda dan Jadwal | Judul, tanggal, jam, lokasi, status selesai |

Semua modul: CRUD lengkap (hapus dengan dialog konfirmasi), validasi input, pencarian + filter,
upload foto dari galeri (disalin ke penyimpanan internal aplikasi).

## Pemetaan materi
- Sesi 1-2: Kotlin, struktur project, Activity (MainActivity)
- Sesi 3: layout & komponen UI (Column, Row, Card, OutlinedTextField, LazyColumn)
- Sesi 4: event handling & navigasi antar-screen (Navigation Compose)
- Sesi 5: validasi, state, form (rememberSaveable, isError + supportingText)
- Sesi 6-7: Room (Entity, DAO, Database singleton) dan CRUD lanjutan (update, delete, dialog)

## Struktur
```
app/src/main/java/com/example/sekreku/
  MainActivity.kt
  data/        Entities.kt, Daos.kt, AppDatabase.kt
  navigation/  AppNavigation.kt (daftar rute)
  util/        Fmt.kt (validasi tanggal/jam), FotoStorage.kt (salin foto ke internal)
  ui/theme/    Theme.kt
  ui/components/Common.kt
  ui/dashboard | inventaris | notulensi | agenda
```

## Batasan
- Data hanya ada di satu HP; uninstall aplikasi / HP hilang = data hilang.
- Belum ada sinkronisasi, backup, login/PIN, notifikasi, atau lampiran PDF/Word.

## Rencana pengembangan
Ekspor/backup data, kunci PIN, pengingat agenda, lampiran PDF.
