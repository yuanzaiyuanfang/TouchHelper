package com.yzyfdf.touchhelper.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.yzyfdf.library.base.BaseApplication
import com.yzyfdf.library.rx.RxBus
import com.yzyfdf.touchhelper.util.Constant
import com.yzyfdf.touchhelper.util.Task

/**
 * Created by Administrator .
 * 描述
 */
class Send2FriendsUtil(val detectionService: DetectionService) {

    var state: SendState = SendState.None


    fun onAccessibilityEvent(
        event: AccessibilityEvent,
        className: String
    ) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                println("className = ${className},state = ${state}")
                when (className) {
                    Constant.ChattingUI -> {//发图片返回
                        if (state == SendState.Back || state == SendState.Send) {
                            state = SendState.None
                            Thread.sleep(1000)
                            next()
                            IntentUtil.setTopApp(BaseApplication.appContext)
                        }
                    }
                    Constant.LauncherUI -> {//发文字返回
                        if (state == SendState.Back) {
                            state = SendState.None
                            if (Constant.nowList.isNullOrEmpty()) {
                                complete()
                            } else {
                                next()
                            }
                            IntentUtil.setTopApp(BaseApplication.appContext)
                        }
                    }
                    Constant.ShareImgUI -> {
                        state = SendState.None
                    }
                    Constant.SelectConversationUI, Constant.SelectConversationUI2 -> {
                        if (state == SendState.None) {
                            selectConversation()
                        }
                        if (state == SendState.Back) {
                            detectionService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                        }
                    }
                    Constant.SoftInputWindow -> {
                        if (state == SendState.ClickSearch) {
                            inputName()
                        }
                    }
                    Constant.SendAppMessageWrapperUI, Constant.SendAppMessageWrapperUI2 -> {
                        if (state == SendState.Find) {
                            sendTime = System.currentTimeMillis()
                            send(className)
                        }
                        if (state == SendState.Send) back()
                    }
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                when (className) {
                    "android.widget.ListView" -> {
                        if (state == SendState.Input) {
                            findConversation(
                                event.source.findAccessibilityNodeInfosByText(Constant.nowList[0].name)
                                    ?.firstOrNull()
                            )
                        }
                    }
                }
            }
        }
    }

    /*选择联系人界面*/
    private fun selectConversation() {
        detectionService.rootInActiveWindow?.findAccessibilityNodeInfosByText("搜索")?.firstOrNull()
            ?.let {
                if (it.className == "android.widget.TextView" && it.isEnabled) {
                    state = SendState.Start
                    clickSearch(it)
                    return
                }
            }
    }

    /*点击搜索*/
    private fun clickSearch(it: AccessibilityNodeInfo) {
        if (it.isFocusable && it.isClickable) {
            state = SendState.ClickSearch
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            clickSearch(it.parent)
        }
    }

    /*输入名字*/
    private fun inputName() {
        detectionService.rootInActiveWindow?.apply {
            repeat(childCount) {
                val child = getChild(it)
                if (child.className == "android.widget.EditText" && child.isEnabled && child.isFocused) {
                    state = SendState.Input
                    child.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                        putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            Constant.nowList[0].name
                        )
                    })
                    return@apply
                }
            }
        }
    }

    /*搜索结果中点击联系人*/
    private fun findConversation(source: AccessibilityNodeInfo?) {
        source?.apply {
            if (isClickable && isEnabled) {
                state = SendState.Find
                performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                findConversation(parent)
            }
        }
    }

    var sendTime = 0L

    /*发送*/
    private fun send(className: String) {
        val text = when (className) {
            Constant.SendAppMessageWrapperUI -> "分享"
            Constant.SendAppMessageWrapperUI2 -> "发送"
            else -> "分享"
        }
        detectionService.rootInActiveWindow?.findAccessibilityNodeInfosByText(text)?.forEach {
//            println("it.className = ${it.className},  text = ${it.text}")
            if (it.className == "android.widget.Button" && it.isClickable && it.isEnabled) {
                state = SendState.Send
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return@forEach
            }
        }
        //如果没找到，半秒内无限重试
        if (System.currentTimeMillis() - sendTime < 500) {
            send(className)
        }
    }

    /*返回*/
    private fun back() {
        detectionService.rootInActiveWindow?.findAccessibilityNodeInfosByText("返回第三方工具")?.forEach {
            if (it.className == "android.widget.Button" && it.isClickable && it.isEnabled) {
                state = SendState.Back
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return@forEach
            }
        }
    }

    private fun next() {
        RxBus.send(Constant.nowList.removeAt(0).name)
    }

    private fun complete() {
        Constant.nowTask = Task.None
        Constant.content = ""
    }
}


enum class SendState {
    Start,
    ClickSearch,
    Input,
    Find,
    Send,
    Back,
    None,
}