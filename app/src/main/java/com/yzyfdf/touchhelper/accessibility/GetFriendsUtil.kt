package com.yzyfdf.touchhelper.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import cn.neetneet.library.kotlinextensions.isNull
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ToastUtils
import com.yzyfdf.touchhelper.util.Constant
import com.yzyfdf.touchhelper.util.FriendsUtil
import com.yzyfdf.touchhelper.util.Task

/**
 * Created by Administrator .
 * 描述
 */
class GetFriendsUtil(val detectionService: DetectionService) {

    var state: GetFState =
        GetFState.None
    var nowWindow = ""

    fun onAccessibilityEvent(
        event: AccessibilityEvent,
        className: String
    ) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                nowWindow = className
                when (className) {
                    Constant.LauncherUI -> {
                        if (state == GetFState.None) {
                            changeTab()
                        }
                    }
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                when (nowWindow) {
                    Constant.LauncherUI -> {
                        if (state == GetFState.Contacts) {
                            state = GetFState.Add
                            getFriends()
//                            AccessibilityNodeInfoUtil.getAllChilds(detectionService.rootInActiveWindow)
                        }
                    }
                }
            }
        }
    }

    /*切换到通讯录*/
    private fun changeTab() {
        state = GetFState.Contacts
        detectionService.rootInActiveWindow?.findAccessibilityNodeInfosByText("通讯录")?.forEach {
            if (it.className == "android.widget.TextView" && it.isEnabled) {
                clickContactsTab(it)
            }
        }
        Thread.sleep(500)
        //两次点击滑到顶部
        detectionService.rootInActiveWindow?.findAccessibilityNodeInfosByText("通讯录")?.forEach {
            if (it.className == "android.widget.TextView" && it.isEnabled) {
                clickContactsTab(it)
            }
        }
        Thread.sleep(800)
    }

    /*点击通讯录标签*/
    private fun clickContactsTab(ani: AccessibilityNodeInfo) {
        if (ani.className == "android.widget.RelativeLayout" && ani.isClickable && ani.isEnabled) {
            ani.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            ani.parent?.apply { clickContactsTab(this) }
        }
    }


    /*获取好友列表*/
    @Synchronized
    private fun getFriends() {
        val allList = arrayListOf<String>()
        var get = true

        Thread {
            while (get) {
                val listView = getListView(detectionService.rootInActiveWindow)
                val list = getOnePagerFriends(listView)
                if (!allList.containsAll(list)) {
                    val newList = deleteRepeat(allList, list)
                    allList.addAll(newList)
                    listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    Thread.sleep(800)
//                    newList.forEach { println("it = ${it}") }
//                    println("翻页")
                } else {
                    get = false
                }
            }

            allList.removeAt(allList.size - 1)
            allList.removeAt(0)
            println("联系人 ${allList.size}：" + GsonUtils.toJson(allList))
            FriendsUtil.saveFriendsByName(allList)

            state = GetFState.End
            complete()
        }.start()
    }

    /*去重*/
    private fun deleteRepeat(
        allList: ArrayList<String>,
        list: ArrayList<String>
    ): ArrayList<String> {
        if (allList.isNotEmpty() && list.isNotEmpty()) {
            val newList = arrayListOf<String>()
            while (list.size > 0) {
                val last = list.removeAt(list.size - 1)
                newList.add(0, last)
                if (allList.containsAll(list)) {
                    return newList
                }
            }
            return newList
        } else {
            return list
        }
    }

    /*获取一页的好友*/
    private fun getOnePagerFriends(listView: AccessibilityNodeInfo?): ArrayList<String> {
        val list = arrayListOf<String>()
        listView?.apply {
            for (i in 0 until childCount) {
                val group = getChild(i)
                if (group != null && group.childCount > 0) {
                    val child = group.getChild(group.childCount - 1)
                    val name = getName(child)
                    list.add(name ?: continue)
                }
            }
        }
        return list
    }

    /*好友列表listview*/
    private fun getListView(ani: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return ani.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/f4").firstOrNull()
    }

    /*获取好友昵称或者备注*/
    private fun getName(ani: AccessibilityNodeInfo): String? {
        if (!ani.isNull() && ani.isEnabled && !ani.text.isNullOrEmpty()) {
            return ani.text.toString()
        } else {
            repeat(ani.childCount) {
                val name = getName(ani.getChild(it))
                if (!name.isNullOrEmpty()) {
                    return name
                }
            }
        }
        return ""
    }

    private fun complete() {
        detectionService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        state = GetFState.End
        Constant.nowTask = Task.None
        ToastUtils.showShort("请进入群发列表，勾选需要群发的联系人")
    }

}

enum class GetFState {
    None,
    Contacts,
    Add,
    End,
}