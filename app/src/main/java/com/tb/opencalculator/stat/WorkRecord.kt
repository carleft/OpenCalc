package com.tb.opencalculator.stat

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(
    tableName = "work_records",
    indices = [Index(value = ["date", "index"], unique = true)]
)
data class WorkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    //工作日期
    val date: String,
    //序号
    val index: Int,
    //工位
    val workstation: String,
    //工作时间，分钟
    val duration: Int,
    //是否加班（是否为夜班）
    val isOvertime: Boolean,
    //工作内容
    val content: String,
    //工作次数
    val times: Int,
) {
    override fun toString(): String {
        return "工作记录(日期:'$date', 序号:$index, 工位:'$workstation', 工作时长:${duration.formatTime()}, 班次:${if (isOvertime) "夜班" else "白班"}, 工作次数:$times, 工作内容:'$content')"
    }
}
