package com.example.sekreku.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// [UAS-f] DATABASE LOKAL: Room (SQLite) bernama "sekreku_db", disimpan di penyimpanan aplikasi
@Database(
    entities = [InventarisEntity::class, NotulensiEntity::class, AgendaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventarisDao(): InventarisDao
    abstract fun notulensiDao(): NotulensiDao
    abstract fun agendaDao(): AgendaDao

    companion object {
        // @Volatile: nilai INSTANCE selalu terbaru dan sama untuk semua thread
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton: hanya ada satu instance database di seluruh aplikasi
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sekreku_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}