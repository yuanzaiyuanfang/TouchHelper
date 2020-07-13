package com.yzyfdf.touchhelper.accessibility

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * Created by Administrator .
 * 描述
 */
object AccessibilityUtil {

    internal val TAG = "AccessibilityUtil"

    // 此方法用来判断当前应用的辅助功能服务是否开启
    fun isAccessibilitySettingsOn(context: Context): Boolean {
        var accessibilityEnabled = 0
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            Log.i(TAG, e.message)
        }

        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (services != null) {
                return services.toLowerCase().contains(context.packageName.toLowerCase())
            }
        }

        return false
    }

    fun goOpen(context: Context) {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}
