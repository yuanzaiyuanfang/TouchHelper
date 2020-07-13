package com.yzyfdf.touchhelper.ui

import android.content.Context
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.yzyfdf.library.base.BaseRvHolder
import com.yzyfdf.library.quick.QuickRvAdapter
import com.yzyfdf.library.view.globalloading.Gloading
import com.yzyfdf.touchhelper.R
import com.yzyfdf.touchhelper.bean.Friend
import com.yzyfdf.touchhelper.util.FriendsUtil
import kotlinx.android.synthetic.main.activity_friends.*

/**
 * Created by Administrator .
 * 描述
 */
class FriendsActivity : AppCompatActivity(R.layout.activity_friends) {

    var holder: Gloading.Holder? = null
    val friendsAdapter: FriendsAdapter by lazy { FriendsAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
    }

    private fun initView() {
        holder = Gloading.default.wrap(recycler_view).withRetry { onBackPressed() }
        recycler_view.adapter = friendsAdapter

        val friends = FriendsUtil.getFriends()
        if (friends.isNullOrEmpty()) {
            holder?.showEmpty(icon = R.mipmap.icon_empty, str = R.string.notice_get_friends)
        } else {
            holder?.showLoadSuccess()
            friendsAdapter.refresh(friends)
        }
    }

    override fun onDestroy() {
        val list = friendsAdapter.list
        FriendsUtil.saveFriends(list)
        super.onDestroy()
    }
}


class FriendsAdapter(context: Context) : QuickRvAdapter<Friend>(context, R.layout.item_friend) {
    override fun onBindViewHolder(holder: BaseRvHolder, position: Int, itemBean: Friend) {
        holder.setText(R.id.tv_name, itemBean.name)
        val cb = holder.getView<CheckBox>(R.id.cb)
        cb.isChecked = itemBean.selected
        cb.setOnClickListener {
            itemBean.selected = !itemBean.selected
            notifyItemChanged(position)
        }
    }
}
