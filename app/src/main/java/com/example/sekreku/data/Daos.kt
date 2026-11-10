package com.example.sekreku.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface InventarisDao {
    // [UAS-e] CRUD - CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventarisEntity)

    // [UAS-e] CRUD - READ
    @Query("SELECT * FROM inventaris ORDER BY id DESC")
    suspend fun getAll(): List<InventarisEntity>

    @Query("SELECT * FROM inventaris WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): InventarisEntity?

    @Query("SELECT COUNT(*) FROM inventaris")
    suspend fun hitung(): Int

    // [UAS-e] CRUD - UPDATE (dicocokkan lewat Primary Key)
    @Update
    suspend fun update(item: InventarisEntity)

    // [UAS-e] CRUD - DELETE
    @Delete
    suspend fun delete(item: InventarisEntity)
}

@Dao
interface NotulensiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NotulensiEntity)

    @Query("SELECT * FROM notulensi ORDER BY id DESC")
    suspend fun getAll(): List<NotulensiEntity>

    @Query("SELECT * FROM notulensi WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): NotulensiEntity?

    @Query("SELECT COUNT(*) FROM notulensi")
    suspend fun hitung(): Int

    @Update
    suspend fun update(item: NotulensiEntity)

    @Delete
    suspend fun delete(item: NotulensiEntity)
}

@Dao
interface AgendaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AgendaEntity)

    @Query("SELECT * FROM agenda ORDER BY id DESC")
    suspend fun getAll(): List<AgendaEntity>

    @Query("SELECT * FROM agenda WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): AgendaEntity?

    @Query("SELECT COUNT(*) FROM agenda")
    suspend fun hitung(): Int

    @Query("SELECT COUNT(*) FROM agenda WHERE selesai = 0")
    suspend fun hitungBelumSelesai(): Int

    @Query("UPDATE agenda SET selesai = :selesai WHERE id = :id")
    suspend fun setSelesai(id: Int, selesai: Boolean)

    @Update
    suspend fun update(item: AgendaEntity)

    @Delete
    suspend fun delete(item: AgendaEntity)
}