package com.newyear.redbull.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager


object AccessibilityUtil {

    private const val TAG = "AccessibilityUtil"

    /**
     * Checks if the accessibility service is enabled.
     * @param ctx The context.
     * @param accessibilityService The name of the accessibility service.
     * @return True if the accessibility service is enabled, false otherwise.
     */
    fun isAccessibilityServiceEnabled(ctx: Context, accessibilityService: String) : Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            val accessibilityManager = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            // If the platform API is lower and the called API is not available, the enabledServices wil be empty
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
            for (enabledService in enabledServices) {
                val serviceInfo = enabledService.resolveInfo.serviceInfo
                if (serviceInfo.packageName.equals(ctx.packageName) && serviceInfo.name.equals(accessibilityService)) {
                    return true
                }
            }
            return false
        } else {
            val expectedComponentName = ComponentName(ctx, accessibilityService)

            val enabledServicesSetting = Settings.Secure.getString(
                    ctx.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledService = ComponentName.unflattenFromString(componentNameString)

                if (enabledService != null && enabledService == expectedComponentName) return true
            }

            return false
        }
    }

    /**
     * Opens the accessibility settings screen.
     * @param ctx The context.
     */
    fun openAccessibilityServiceSetting(ctx: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            ctx.startActivity(intent)
        }catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "openAccessibilityServiceSetting: ${e.message}")
        }
    }
}