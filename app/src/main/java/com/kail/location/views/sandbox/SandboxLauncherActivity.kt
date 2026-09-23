package com.kail.location.views.sandbox

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.kail.location.R
import com.kail.location.views.base.BaseActivity
import top.niunaijun.blackbox.BlackBoxCore

/**
 * 沙盒应用快捷方式启动器。
 * 接收快捷方式传入的包名，启动对应的沙盒应用后自动关闭。
 */
class SandboxLauncherActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra("package_name")
        val userId = intent.getIntExtra("user_id", 0)

        if (packageName.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.sandbox_invalid_shortcut), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            val success = BlackBoxCore.get().launchApk(packageName, userId)
            if (!success) {
                Toast.makeText(this, getString(R.string.sandbox_launch_failed), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.sandbox_launch_failed_msg, e.message), Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
