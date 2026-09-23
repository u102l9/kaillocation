package com.kail.location.sandbox

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import com.kail.location.R
import com.kail.location.utils.KailLog
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.utils.AbiUtils
import java.io.File

/**
 * 沙盒管理器 - 提供给宿主应用使用的简化 API。
 */
object SandboxManager {

    private const val TAG = "SandboxManager"

    private var hostContext: android.content.Context? = null

    fun init(context: android.content.Context) {
        hostContext = context.applicationContext
    }

    data class SandboxAppInfo(
        val name: String,
        val icon: Drawable?,
        val packageName: String,
        val sourceDir: String
    )

    data class SystemAppInfo(
        val name: String,
        val icon: Drawable?,
        val packageName: String,
        val sourceDir: String
    )

    /**
     * 获取沙盒中已安装的应用列表。
     */
    fun getSandboxApps(userId: Int = 0): List<SandboxAppInfo> {
        return try {
            val installedApps = BlackBoxCore.get().getInstalledApplications(0, userId)
            KailLog.d(null, TAG, "Found ${installedApps.size} sandbox apps")
            installedApps.map { appInfo ->
                SandboxAppInfo(
                    name = safeLoadAppLabel(appInfo),
                    icon = safeLoadAppIcon(appInfo),
                    packageName = appInfo.packageName,
                    sourceDir = appInfo.sourceDir
                )
            }
        } catch (e: Exception) {
            KailLog.e(null, TAG, "Error getting sandbox apps: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 获取系统中可克隆的应用列表。
     */
    fun getSystemApps(): List<SystemAppInfo> {
        return try {
            val ctx = hostContext ?: BlackBoxCore.getContext()
            val pm = ctx.packageManager
            val installedApplications = pm.getInstalledApplications(0)
            val hostPkg = BlackBoxCore.getHostPkg()
            KailLog.d(null, TAG, "hostContext=$hostContext, pm=$pm")
            KailLog.d(null, TAG, "Found ${installedApplications.size} installed apps, hostPkg=$hostPkg")
            if (installedApplications.isNotEmpty()) {
                KailLog.d(null, TAG, "First app: ${installedApplications[0].packageName}")
            }
            val result = installedApplications
                .filter { app ->
                    val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isHost = app.packageName == hostPkg
                    val isInstalled = BlackBoxCore.get().isInstalled(app.packageName, 0)
                    val isAbiSupported = AbiUtils.isSupport(File(app.sourceDir))
                    if (!isSystem && !isHost && !isInstalled && isAbiSupported) {
                        KailLog.d(null, TAG, "  + ${app.packageName} (cloneable)")
                    }
                    !isSystem && !isHost && !isInstalled && isAbiSupported
                }
                .map { app ->
                    SystemAppInfo(
                        name = safeLoadSystemAppLabel(app),
                        icon = safeLoadSystemAppIcon(app),
                        packageName = app.packageName,
                        sourceDir = app.sourceDir
                    )
                }
            KailLog.d(null, TAG, "Filtered to ${result.size} cloneable apps")
            result
        } catch (e: Exception) {
            KailLog.e(null, TAG, "Error getting system apps: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 克隆系统应用到沙盒。
     */
    fun cloneApp(packageName: String, userId: Int = 0): Pair<Boolean, String> {
        return try {
            val result = BlackBoxCore.get().installPackageAsUser(packageName, userId)
            if (result.success) {
                KailLog.i(null, TAG, "cloneApp: $packageName installed (userId=$userId)")
                Pair(true, hostContext!!.getString(R.string.sandbox_install_success))
            } else {
                KailLog.w(null, TAG, "cloneApp: $packageName install failed: ${result.msg}")
                Pair(false, hostContext!!.getString(R.string.sandbox_install_failed, result.msg))
            }
        } catch (e: Exception) {
            KailLog.e(null, TAG, "cloneApp: $packageName threw", e)
            Pair(false, hostContext!!.getString(R.string.sandbox_install_failed, e.message))
        }
    }

    /**
     * 卸载沙盒应用。
     */
    fun uninstallApp(packageName: String, userId: Int = 0): Pair<Boolean, String> {
        return try {
            BlackBoxCore.get().uninstallPackageAsUser(packageName, userId)
            KailLog.i(null, TAG, "uninstallApp: $packageName uninstalled (userId=$userId)")
            Pair(true, hostContext!!.getString(R.string.sandbox_uninstall_success))
        } catch (e: Exception) {
            KailLog.e(null, TAG, "uninstallApp: $packageName threw", e)
            Pair(false, hostContext!!.getString(R.string.sandbox_uninstall_failed, e.message))
        }
    }

    /**
     * 启动沙盒应用。
     */
    fun launchApp(packageName: String, userId: Int = 0): Pair<Boolean, String> {
        return try {
            val success = BlackBoxCore.get().launchApk(packageName, userId)
            if (success) {
                KailLog.i(null, TAG, "launchApp: $packageName launched (userId=$userId)")
                Pair(true, "")
            } else {
                KailLog.w(null, TAG, "launchApp: $packageName returned false")
                Pair(false, hostContext!!.getString(R.string.sandbox_launch_failed))
            }
        } catch (e: Exception) {
            KailLog.e(null, TAG, "launchApp: $packageName threw", e)
            Pair(false, hostContext!!.getString(R.string.sandbox_launch_failed_msg, e.message))
        }
    }

    /**
     * 清除沙盒应用数据。
     */
    fun clearAppData(packageName: String, userId: Int = 0): Pair<Boolean, String> {
        return try {
            BlackBoxCore.get().clearPackage(packageName, userId)
            KailLog.i(null, TAG, "clearAppData: $packageName cleared (userId=$userId)")
            Pair(true, hostContext!!.getString(R.string.sandbox_clear_success))
        } catch (e: Exception) {
            KailLog.e(null, TAG, "clearAppData: $packageName threw", e)
            Pair(false, hostContext!!.getString(R.string.sandbox_clear_failed, e.message))
        }
    }

    /**
     * 停止沙盒应用运行。
     */
    fun stopApp(packageName: String, userId: Int = 0): Pair<Boolean, String> {
        return try {
            BlackBoxCore.get().stopPackage(packageName, userId)
            KailLog.i(null, TAG, "stopApp: $packageName stopped (userId=$userId)")
            Pair(true, hostContext!!.getString(R.string.sandbox_stopped))
        } catch (e: Exception) {
            KailLog.e(null, TAG, "stopApp: $packageName threw", e)
            Pair(false, hostContext!!.getString(R.string.sandbox_stop_failed, e.message))
        }
    }

    private fun safeLoadAppLabel(appInfo: ApplicationInfo): String {
        return try {
            BlackBoxCore.getPackageManager().getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            appInfo.packageName
        }
    }

    private fun safeLoadAppIcon(appInfo: ApplicationInfo): Drawable? {
        return try {
            BlackBoxCore.getPackageManager().getApplicationIcon(appInfo)
        } catch (e: Exception) {
            null
        }
    }

    private fun safeLoadSystemAppLabel(appInfo: ApplicationInfo): String {
        return try {
            val ctx = hostContext ?: BlackBoxCore.getContext()
            ctx.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            appInfo.packageName
        }
    }

    private fun safeLoadSystemAppIcon(appInfo: ApplicationInfo): Drawable? {
        return try {
            val ctx = hostContext ?: BlackBoxCore.getContext()
            ctx.packageManager.getApplicationIcon(appInfo)
        } catch (e: Exception) {
            null
        }
    }
}
