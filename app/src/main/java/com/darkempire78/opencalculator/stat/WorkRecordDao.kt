package com.darkempire78.opencalculator.stat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkRecordDao {
    @Insert
    suspend fun insert(workRecord: WorkRecord)

    @Query("SELECT * FROM work_records ORDER BY date ASC, id ASC")
    suspend fun getAllWorkRecords(): List<WorkRecord>

}