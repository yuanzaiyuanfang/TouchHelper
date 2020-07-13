package com.yzyfdf.touchhelper.util

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.reflect.TypeToken
import com.yzyfdf.touchhelper.bean.Friend
import java.lang.Exception

/**
 * Created by Administrator .
 * 描述
 */
object FriendsUtil {

    val friends = "friends"

    fun saveFriendsByName(list: List<String>) {
        val map = list.map { Friend(it) }
        SPUtils.getInstance().put(friends, GsonUtils.toJson(map))
    }

    fun saveFriends(list: List<Friend>) {
        SPUtils.getInstance().put(friends, GsonUtils.toJson(list))
    }

    fun getFriends(): List<Friend> {
        val string = SPUtils.getInstance().getString(friends)
        return if (string.isNullOrEmpty()) {
            arrayListOf<Friend>()
        } else {
            try {
                GsonUtils.fromJson<List<Friend>>(string, object : TypeToken<List<Friend>>() {}.type)
            } catch (e: Exception) {
                arrayListOf<Friend>()
            }
        }
    }
}