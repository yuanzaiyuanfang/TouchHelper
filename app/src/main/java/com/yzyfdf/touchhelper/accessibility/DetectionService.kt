package com.yzyfdf.touchhelper.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.blankj.utilcode.util.AppUtils
import com.yzyfdf.touchhelper.util.Constant
import com.yzyfdf.touchhelper.util.Task

/**
 * Created by Administrator .
 * 描述
 */
class DetectionService : AccessibilityService() {

    companion object {

        internal var foregroundPackageName: String? = null//上一个程序包名
        internal var lastPackageName: String? = null//当前页面包名

        /**
         * 系统页
         */
        val onceIgnore = arrayOf("android")

        /**
         *  两次才是系统页面
         */
        val twiceIgnore = arrayOf("com.android.systemui", "com.huawei.android.launcher")
    }

    var send2FriendsUtil: Send2FriendsUtil? = null
    var getFriendsUtil: GetFriendsUtil? = null


    override fun onServiceConnected() {
        send2FriendsUtil =
            Send2FriendsUtil(this)
        getFriendsUtil =
            GetFriendsUtil(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_REDELIVER_INTENT // 根据需要返回不同的语义值
    }

    /**
     * 重载辅助功能事件回调函数，对窗口状态变化事件进行处理
     * @param event
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            val packageName = event.packageName?.toString() ?: return
            val className = event.className?.toString() ?: "无类名"

            lastPackageName =
                foregroundPackageName
            foregroundPackageName = packageName

            when (foregroundPackageName) {
                AppUtils.getAppPackageName() -> {
                    return//自己app不处理
                }
                in onceIgnore -> return//某些看不到的系统页，不处理
                in twiceIgnore -> {
                    if (lastPackageName != foregroundPackageName) {
                        return//一般要连续两次才是系统页面，一次不处理
                    }
                }
            }

//            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                println("TYPE_WINDOW_STATE_CHANGED = $className")
//            }
//            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//                println("TYPE_WINDOW_CONTENT_CHANGED = $className")
//            }

            when (Constant.nowTask) {
                Task.GetFreinds -> getFriendsUtil?.onAccessibilityEvent(event, className)
                Task.Send2FriendsText, Task.Send2FriendsPic -> {
                    send2FriendsUtil?.onAccessibilityEvent(event, className)
                }
                Task.None -> {
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {}

}


