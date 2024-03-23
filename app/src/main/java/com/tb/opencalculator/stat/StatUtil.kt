package com.tb.opencalculator.stat

import android.app.Activity
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}


/**
 * 检查字符串有效性
 */
fun String?.validateAndAssign(fieldName: String, activity: Activity): String? {
    if (isNullOrBlank() || isEmpty()) {
        activity.toast("$fieldName 不可为空！")
        return null
    }
    return this
}

fun date2Str(year: Int, month: Int, day: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
    }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(calendar.time)
}

fun String.str2Date(): Calendar {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = dateFormat.parse(this)!!
    return calendar
}

//fun Int.format(): String = if (this in 0 until 10) {
//    "0$this"
//} else {
//    toString()
//}

//数字转换为至少2位
fun Int.format2(): String = String.format("%02d", this)
fun Int.formatTime(): String {
    return "${(this / 60).format2()}:${(this % 60).format2()}"
}