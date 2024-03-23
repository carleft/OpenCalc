package com.darkempire78.opencalculator.timecalc

import java.lang.StringBuilder


const val hourSymbol = "h"
const val minSymbol = "m"
const val secSymbol = "s"
const val error = "Illegal Input"

/**
 * 是否为TimeSymbol
 */
fun String?.isTimeSymbol() = equals(hourSymbol) || equals(minSymbol) || equals(secSymbol)

/**
 * 判断最后一位是否为TimeSymbol
 */
fun String?.endWithTimeSymbol() = this?.run{ endsWith(hourSymbol) || endsWith(minSymbol) || endsWith(secSymbol) } ?: false

/**
 * 判断最后一位是否为计算符号
 */
fun String?.endWithCalcSymbol() = this?.matches(Regex(".*[+\\-x÷%]$")) ?: false

/**
 * 判断最后一位是否为数字
 */
fun String?.endWithNum() = this?.matches(Regex(".*\\d$")) ?: false

/**
 * 判断是否包含符号
 */
fun String?.containsCalcSymbols() = this?.contains(Regex("[+\\-×÷%()]")) ?: false

/**
 * 判断是否含有与时间计算冲突的符号
 */
fun String?.containsTimeConflictSymbols() = this?.contains(Regex("[×÷%()]")) ?: false

/**
 * 判断是否含有时间计算符号
 */
fun String?.containsTimeCalcSymbols() = this?.contains(Regex("[+\\-]")) ?: false

/**
 * 判断是否含有时间符号
 */
fun String?.containsTimeSymbols() = this?.contains(Regex("[$hourSymbol$minSymbol$secSymbol]")) ?: false

/**
 * 提取最后一个操作符之后的所有字符，返回一个新字符串
 */
fun String.extractAfterLastOperator(): String {
    val regex = Regex("[+\\-x÷]")
    val matches = regex.findAll(this)
    val lastMatch = matches.lastOrNull()

    return if (lastMatch != null) {
        substring(lastMatch.range.last + 1)
    } else {
        this
    }
}

/**
 * 计算时间算式
 */
fun String?.calculateTimeExpression(): String {
    if (this == null || !this.containsTimeSymbols()) {
        return error
    }

    //1.将算式按照运算符拆分
    val timeList = split(Regex("[+\\-]")).filter { it.isNotBlank() }
        .map { it.getSeconds() ?: run { return error } }.toMutableList()
    //2.提取运算符
    val calcSymbolList = Regex("[+-]").findAll(this).map { it.value }.toList()
    //3.分别运算
    var index = 0
    var result = timeList[0]
    while (index + 1 < timeList.size && index < calcSymbolList.size) {
        val calcSymbol = calcSymbolList[index]
        if ("+" == calcSymbol) {
            result += timeList[index + 1]
        } else if ("-" == calcSymbol) {
            result -= timeList[index + 1]
        }
        index ++
    }
    return result.getTimeString()

}

/**
 * 时间字符串转秒
 */
fun String.getSeconds(): Int? {
    val regex = Regex("(\\d+)([hms])") // 匹配数字后跟着h、m或s的正则表达式
    var totalSeconds = 0

    regex.findAll(this).forEach { matchResult ->
        val (value, unit) = matchResult.destructured
        val intValue = value.toInt()

        totalSeconds += when (unit) {
            "h" -> intValue * 3600 // 1小时 = 3600秒
            "m" -> intValue * 60   // 1分钟 = 60秒
            "s" -> intValue        // 秒数
            else -> return null
        }
    }

    return totalSeconds
}

/**
 * 秒转时间字符串
 */
fun Int.getTimeString(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    val formattedTime = StringBuilder()

    if (hours > 0) {
        formattedTime.append("${hours}h")
    }

    if (minutes > 0) {
        formattedTime.append("${minutes}m")
    }

    if (seconds > 0) {
        formattedTime.append("${seconds}s")
    }

    return if (formattedTime.isEmpty()) "0h0m0s" else formattedTime.toString()
}