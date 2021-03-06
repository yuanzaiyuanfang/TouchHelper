package com.yzyfdf.touchhelper.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.widget.NestedScrollView
import cn.neetneet.library.kotlinextensions.toGone
import cn.neetneet.library.kotlinextensions.toVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.UriUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.yzyfdf.library.base.BaseActivity
import com.yzyfdf.library.base.BaseModel
import com.yzyfdf.library.base.BasePresenter
import com.yzyfdf.library.sample.SampleRxSubscriber
import com.yzyfdf.library.utils.showToast
import com.yzyfdf.touchhelper.R
import com.yzyfdf.touchhelper.accessibility.AccessibilityUtil
import com.yzyfdf.touchhelper.kotlinextensions.PermissionType
import com.yzyfdf.touchhelper.kotlinextensions.requestPermissions
import com.yzyfdf.touchhelper.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity<BasePresenter<*, *>, BaseModel>() {


    private val selectPic4chat = 999
    private val selectPic4timeline = 998

    private val send2chat = 899
    private val send2timeline = 898


    override val layoutId: Int
        get() = R.layout.activity_main

    override fun initPresenter() {
        mRxManager.on<String> {
            LogUtil.addContent("已发送 = ${it}")
            refreshLog()
            if (Constant.nowList.isNullOrEmpty()) {
                LogUtil.complete()
                showToast("任务完成")
                refreshLog()
                return@on
            }

            Observable.timer(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SampleRxSubscriber<Long>(mRxManager) {
                    override fun _onNext(t: Long) {
                        when (Constant.nowTask) {
                            Task.Send2FriendsText -> {
                                shareWechatFriend(Constant.content)
                            }
                            Task.Send2FriendsPic -> {
                                shareWechatFriend(UriUtils.uri2File(Uri.parse(Constant.picPath)))
                            }
                        }
                    }

                    override fun _onError(message: String) {
                    }
                })


        }
    }

    override fun initView() {
        get_friends.setOnClickListener {
            needAccessibility {
                notice(
                    title = "提示",
                    message = if (FriendsUtil.getFriends().isNullOrEmpty())
                        "会打开你的微信，自动滑动通讯录列表，期间请不要进行任何操作！"
                    else
                        "重新获取好友列表会覆盖当前列表！"
                ) { getFriends() }
            }
        }
        group_msg.setOnClickListener { startActivity(Intent(this, FriendsActivity::class.java)) }

        send_wx_text.setOnClickListener {
            needAccessibility { checkFriends("群发消息") { editText { shareWechatFriend(it) } } }
        }
        send_wx_pic.setOnClickListener { /*needAccessibility {*/ needPermission { selectPic(code = selectPic4chat) } } /*}*/
        send_wxline_pic.setOnClickListener { needAccessibility { needPermission { selectPic(code = selectPic4timeline) } } }

        open_accessibility.setOnClickListener { AccessibilityUtil.goOpen(this) }
    }


    /**
     * 获取好友列表
     */
    private fun getFriends() {
        Constant.nowTask = Task.GetFreinds
        startActivity(Intent().apply {
            component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    /**
     * 群发前检查
     */
    private fun checkFriends(title: String, function: () -> Unit) {
        val friends = FriendsUtil.getFriends().filter { it.selected }
        if (friends.isNullOrEmpty()) {
            showToast("群发列表为空")
            return
        }
        Constant.nowList.clear()
        Constant.nowList.addAll(friends)

        notice(title, "${friends[0].name} 等${friends.size}人") { function() }
//        MaterialDialog(this).show {
//            title(text = "群发消息")
//            message(text = "${friends[0].name} 等${friends.size}人")
//            positiveButton {
//                function()
//            }
//        }
    }

    /**
     * 给好友发文字
     */
    private fun shareWechatFriend(content: String) {
        Constant.nowTask = Task.Send2FriendsText
        val intent = Intent().apply {
            component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            action = Intent.ACTION_SEND
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_TEXT, String.format(content, Constant.nowList[0].name))
        }
        startActivityForResult(intent, send2chat)
    }

    /**
     * 给好友发图片
     */
    private fun shareWechatFriend(picFile: File) {
        Constant.nowTask = Task.Send2FriendsPic
        val intent = Intent().apply {
            component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, ImageUtil.getImageContentUri(this@MainActivity, picFile))
        }
        startActivityForResult(Intent.createChooser(intent, "Share"), send2chat)
    }

    /**
     * 发送到朋友圈
     */
    private fun shareWechatTimeLine(content: String, picFile: File) {
        val intent = Intent().apply {
            component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, ImageUtil.getImageContentUri(this@MainActivity, picFile))
            putExtra("Kdescription", content)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivityForResult(intent, send2timeline)
    }

    /**
     * 编辑文本
     */
    private fun editText(function: (str: String) -> Unit) {
        MaterialDialog(this).show {
            input(prefill = Constant.contentHint) { dialog, text ->
                LogUtil.addTask("消息群发")
                refreshLog()
                Constant.content = text.toString()
                function(Constant.content)
            }
            negativeButton(text = "取消")
            positiveButton(text = "确认")
        }
    }

    /**
     * 选择图片
     */
    private fun selectPic(num: Int = 1, code: Int) {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .loadImageEngine(GlideEngine.createGlideEngine())
            .maxSelectNum(num)
            .forResult(code)
    }

    override fun onResume() {
        super.onResume()
        val accessibility = AccessibilityUtil.isAccessibilitySettingsOn(this)
        if (accessibility) {
            open_accessibility.toGone()
            state_accessibility.text = "辅助功能已开启"
        } else {
            open_accessibility.toVisible()
            state_accessibility.text = "辅助功能未开启"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode / 100) {
            9 -> {//选图片
                if (resultCode == Activity.RESULT_OK) {
                    val selectList = PictureSelector.obtainMultipleResult(data)
                    val path = selectList?.firstOrNull()?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            it.path
                        } else {
                            "file:${it.path}"
                        }
                    }
                    Constant.picPath = path ?: ""
                    when (requestCode) {
                        selectPic4chat -> {
                            checkFriends("群发图片") {
                                LogUtil.addTask("图片群发")
                                refreshLog()
                                shareWechatFriend(UriUtils.uri2File(Uri.parse(Constant.picPath)))
                            }
                        }
                        selectPic4timeline -> {
                            shareWechatTimeLine(
                                Constant.content,
                                UriUtils.uri2File(Uri.parse(path))
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 确认操作
     */
    private fun notice(title: String? = null, message: String? = null, function: () -> Unit) {
        MaterialDialog(this).show {
            title?.apply { title(text = this) }
            message?.apply { message(text = this) }
            positiveButton(text = "确认") {
                function()
            }
            negativeButton(text = "取消")
        }
    }

    /**
     * 需要权限
     */
    private fun needPermission(function: () -> Unit) {
        requestPermissions({ function() }, {}, PermissionType.EXTERNAL_STORAGE)
    }

    /**
     * 需要辅助功能
     */
    private fun needAccessibility(function: () -> Unit) {
        val accessibility = AccessibilityUtil.isAccessibilitySettingsOn(this)
        if (accessibility) {
            function()
        } else {
            ToastUtils.showShort("请先点击底部开关打开辅助功能")
        }
    }


    private fun refreshLog() {
        val logs = LogUtil.getLogs()
        tv_log.text = logs
        layout_log.fullScroll(NestedScrollView.FOCUS_DOWN);
    }

}
