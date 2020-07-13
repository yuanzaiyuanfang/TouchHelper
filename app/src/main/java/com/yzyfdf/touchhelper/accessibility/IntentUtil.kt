package com.yzyfdf.touchhelper.accessibility

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import com.yzyfdf.library.base.BaseApplication
import com.yzyfdf.touchhelper.ui.MainActivity


/**
 * Created by Administrator .
 * 描述
 */
object IntentUtil {

    /**
     * 切换到前台
     */
    fun setTopApp(context: Context) {
        if (!isRunningForeground(context)) {
            /**获取ActivityManager */
            val activityManager =
                context.getSystemService(ACTIVITY_SERVICE) as ActivityManager

            /**获得当前运行的task(任务) */
            val taskInfoList =
                activityManager.getRunningTasks(100)
            for (taskInfo in taskInfoList) {
                /**找到本应用的 task，并将它切换到前台 */
                if (taskInfo.topActivity!!.packageName.equals(context.getPackageName())) {
                    activityManager.moveTaskToFront(taskInfo.id, 0)
                    break
                }
            }
        }
    }

    private fun isRunningForeground(context: Context): Boolean {
        val activityManager =
            context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val appProcessInfoList =
            activityManager.runningAppProcesses
        /**枚举进程 */
        for (appProcessInfo in appProcessInfoList) {
            if (appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName == context.applicationInfo.processName) {
                    return true
                }
            }
        }
        return false
    }

}

