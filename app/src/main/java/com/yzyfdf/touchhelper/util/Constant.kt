package com.yzyfdf.touchhelper.util

import com.yzyfdf.touchhelper.bean.Friend

/**
 * Created by Administrator .
 * 描述
 */
object Constant {

    val contentHint = "%s，此消息测试，勿回。"
    var content = ""


    val LauncherUI = "com.tencent.mm.ui.LauncherUI"//微信主界面
    val SelectConversationUI = "com.tencent.mm.ui.transmit.SelectConversationUI"//选择联系人
    val SoftInputWindow = "android.inputmethodservice.SoftInputWindow"//输入法
    val SendAppMessageWrapperUI = "com.tencent.mm.ui.transmit.SendAppMessageWrapperUI"//发送弹窗——文字
    val SendAppMessageWrapperUI2 = "com.tencent.mm.ui.widget.a.d"//发送弹窗——图
    val ChattingUI = "com.tencent.mm.ui.chatting.ChattingUI"//聊天窗口

    var nowTask: Task = Task.None//记录当前任务
    var nowList: ArrayList<Friend> = arrayListOf()//群发列表
}

enum class Task {
    None,
    GetFreinds,
    Send2FriendsText,
    Send2FriendsPic,
}

