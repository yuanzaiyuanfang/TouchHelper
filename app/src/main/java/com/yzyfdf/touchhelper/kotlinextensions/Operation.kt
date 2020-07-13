package cn.neetneet.library.kotlinextensions

/**
 * Created by Administrator .
 * 描述
 */

fun Any?.isNull(): Boolean {
    return this == null
}

fun Boolean.doIfTrue(block: () -> Unit) {
    if (this) {
        block()
    }
}

fun Boolean.doIfFalse(block: () -> Unit) {
    if (!this) {
        block()
    }
}


fun Float.range(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE): Float {
    return when {
        this > max -> max
        this < min -> min
        else -> this
    }
}