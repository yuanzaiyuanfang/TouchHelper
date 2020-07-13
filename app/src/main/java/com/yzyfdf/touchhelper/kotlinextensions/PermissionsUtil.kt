package com.yzyfdf.touchhelper.kotlinextensions

import android.Manifest
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * 权限
 */
enum class PermissionType {
    EXTERNAL_STORAGE,//存储读写
    CAMERA,//相机
    PHONE_STATE,//设备状态

}

/**
 * @param  success 成功的操作
 * @param  error 失败的操作
 * @param  types PermissionType 需要的权限
 */
fun FragmentActivity.requestPermissions(
    success: () -> Unit,
    error: () -> Unit,
    vararg types: PermissionType
) {
    val observable =
        getPermissions(RxPermissions(this), *types)
    subscribe(observable, success, error)
}

fun Fragment.requestPermissions(
    success: () -> Unit,
    error: () -> Unit,
    vararg types: PermissionType
) {
    val observable =
        getPermissions(RxPermissions(this), *types)
    subscribe(observable, success, error)
}

private fun subscribe(
    observable: Observable<Boolean>,
    success: () -> Unit,
    error: () -> Unit
) {
    observable.subscribe(object : Observer<Boolean> {
        override fun onComplete() {

        }

        override fun onSubscribe(d: Disposable) {

        }

        override fun onNext(t: Boolean) {
            if (t) {
                success()
            } else {
                error()
            }
        }

        override fun onError(e: Throwable) {
            error()
        }
    })
}

private fun getPermissions(
    rxPermissions: RxPermissions,
    vararg types: PermissionType
): Observable<Boolean> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && types.isNotEmpty()) {
        val list = mutableListOf<String>()
        types.forEach {
            when (it) {
                PermissionType.EXTERNAL_STORAGE -> {
                    list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                PermissionType.CAMERA -> {
                    list.add(Manifest.permission.CAMERA)
                }
                PermissionType.PHONE_STATE -> {
                    list.add(Manifest.permission.READ_PHONE_STATE)
                }
            }
        }
        val array = list.toTypedArray()
        rxPermissions.request(*array)
    } else {
        Observable.create { emitter ->
            emitter.onNext(true)
            emitter.onComplete()
        }
    }
}
