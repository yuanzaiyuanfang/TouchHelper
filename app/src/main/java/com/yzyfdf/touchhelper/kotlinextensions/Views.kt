package cn.neetneet.library.kotlinextensions

import android.view.View

/**
 * Created by Administrator .
 * 描述
 */
class Views {
}

fun View.toVisible(visibility: Boolean = true, invisible: Boolean = false) {
    if (visibility) {
        toVisible()
    } else {
        if (invisible) {
            toInVisible()
        } else {
            toGone()
        }
    }
}

fun View.toVisible() {
    this.visibility = View.VISIBLE
}

fun View.toInVisible() {
    this.visibility = View.INVISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}