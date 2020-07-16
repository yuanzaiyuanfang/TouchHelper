package com.yzyfdf.touchhelper.util

import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils

/**
 * Created by Administrator .
 * 描述
 */
object LogUtil {

    val line = "=============================="
    private val sb: StringBuilder = StringBuilder()
    var count = 0
    var startTime = 0L
    var endTime = 0L


    fun addTask(task: String) {
        sb.clear()
        count = 0
        startTime = System.currentTimeMillis()

        sb.append(line).append("\n")
            .append("任务：$task").append("\n")
            .append("开始：").append(TimeUtils.getString(startTime, 0L, TimeConstants.MSEC))
            .append("\n")
            .append(line).append("\n")
    }

    fun addContent(content: String) {
        sb.append("    ").append(content).append("\n")
        count++
    }

    fun complete() {
        endTime = System.currentTimeMillis()

        sb.append(line).append("\n")
            .append("结束：").append(TimeUtils.getString(endTime, 0L, TimeConstants.MSEC)).append("\n")
            .append("发送：${count}人，耗时：${(endTime - startTime) / 1000}秒").append("\n")
            .append(line).append("\n")
            .append("\n")
            .append("\n")

        val s = sb.toString()
        println(s)

    }

    fun getLogs(): String {
        return sb.toString()
    }
}