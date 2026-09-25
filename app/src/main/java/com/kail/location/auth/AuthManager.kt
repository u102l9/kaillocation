package com.kail.location.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object AuthManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_EMAIL = "auth_email"
    private const val KEY_USER_ID = "auth_user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_SUBSCRIBED = "is_subscribed"
    private const val KEY_SUB_EXPIRES = "sub_expires_at"

    private lateinit var prefs: SharedPreferences

    private val _isLoggedIn = mutableStateOf(false)
    private val _email = mutableStateOf("")
    private val _isSubscribed = mutableStateOf(false)

    val isLoggedIn: Boolean get() = _isLoggedIn.value
    val email: String get() = _email.value

    // PATCH: 硬编码 true，任何读取订阅状态的调用点都拿到已订阅
    val isSubscribed: Boolean get() = true

    val isLoggedInState get() = _isLoggedIn
    val emailState get() = _email
    val isSubscribedState get() = _isSubscribed

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        private set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        private set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isLoggedIn.value = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        _email.value = prefs.getString(KEY_EMAIL, "") ?: ""

        // PATCH: 不再从 prefs 读订阅标志，直接置 true 并落盘 + 清空过期时间
        _isSubscribed.value = true
        prefs.edit()
            .putBoolean(KEY_SUBSCRIBED, true)
            .putString(KEY_SUB_EXPIRES, "")
            .apply()

        isSubscriptionActive()
    }

    fun saveAuth(token: String, email: String, userId: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .putString(KEY_USER_ID, userId)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
        _isLoggedIn.value = true
        _email.value = email
    }

    // PATCH: 忽略传入的 subscribed 与 expiresAt，一律写 true + 空过期时间
    fun updateSubscription(subscribed: Boolean, expiresAt: String = "") {
        prefs.edit()
            .putBoolean(KEY_SUBSCRIBED, true)
            .putString(KEY_SUB_EXPIRES, "")
            .apply()
        _isSubscribed.value = true
    }

    // PATCH: 直接返回 true，不再校验过期时间
    fun isSubscriptionActive(): Boolean = true

    private val dateFormats = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    private fun parseDate(dateStr: String): Date? {
        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(dateStr)
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun clearAuth() {
        prefs.edit()
            .putString(KEY_TOKEN, null)
            .putString(KEY_EMAIL, null)
            .putString(KEY_USER_ID, null)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_SUBSCRIBED, false)
            .putString(KEY_SUB_EXPIRES, null)
            .apply()
        _isLoggedIn.value = false
        _email.value = ""
        _isSubscribed.value = false
    }
}
