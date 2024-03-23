package com.tb.opencalculator.stat

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WorkRecordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workRecord: WorkRecord): Long

    @Query("SELECT * FROM work_records ORDER BY date ASC, `index` ASC")
    suspend fun getAllWorkRecords(): List<WorkRecord>

    @Query("SELECT * FROM work_records WHERE date = :date AND `index` = :index LIMIT 1")
    suspend fun find(date: String, index: Int): WorkRecord?

    @Delete
    suspend fun delete(workRecord: WorkRecord): Int

    @Query("SELECT COUNT(*) FROM work_records")
    suspend fun getCount(): Int

}