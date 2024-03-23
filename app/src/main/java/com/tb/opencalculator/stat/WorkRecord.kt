package com.tb.opencalculator.stat

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "work_records")
data class WorkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    //工作日期
    val date: String,
    //工位
    val workstation: String,
    //分钟
    val duration: Int,
    //是否加班（是否为夜班）
    val isOvertime: Boolean,
    //工作内容
    val content: String,
    //工作次数
    val times: Int,
)
