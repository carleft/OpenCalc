package com.darkempire78.opencalculator.stat

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [WorkRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workRecordDao(): WorkRecordDao
}