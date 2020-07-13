package com.yzyfdf.touchhelper.accessibility

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Created by Administrator .
 * 描述
 */
object AccessibilityNodeInfoUtil {
    fun getAllChilds(ani: AccessibilityNodeInfo?, i: Int = 0) {
        ani?.apply {
            repeat(childCount) {
                val child = getChild(it)
                for (num in 0 until i) {
                    print("-")
                    print("-")
                    print("-")
                    print("-")
                }
                println("name = ${child.className}, ${child.text}")
                getAllChilds(
                    child,
                    i + 1
                )
            }
        }
    }
}