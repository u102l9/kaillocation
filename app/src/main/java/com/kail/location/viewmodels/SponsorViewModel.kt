package com.kail.location.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kail.location.R
import com.kail.location.auth.AuthManager
import com.kail.location.network.RuoYiClient
import com.kail.location.utils.KailLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SponsorViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SponsorViewModel"
    }

    var plans by mutableStateOf<List<RuoYiClient.SubscriptionPlan>>(emptyList())
        private set
    var selectedPlanId by mutableStateOf<Long?>(null)
        private set
    var isCreatingCheckout by mutableStateOf(false)
        private set
    var checkoutError by mutableStateOf<String?>(null)
        private set
    var plansLoaded by mutableStateOf(false)
    var wechatPayUrl by mutableStateOf<String?>(null)
        private set

    fun loadPlans() {
        val token = AuthManager.token ?: return
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                RuoYiClient.getPlans(token)
            }
            result.fold(
                onSuccess = { planList ->
                    plans = planList
                    plansLoaded = true
                    if (planList.isNotEmpty()) {
                        selectedPlanId = planList.first().id
                    }
                },
                onFailure = { e -> KailLog.w(null, TAG, "loadPlans: fetch subscription plans failed: ${e.message}") }
            )
        }
    }

    fun selectPlan(planId: Long) {
        selectedPlanId = planId
    }

    private fun createCheckoutUrl(onResult: (checkoutUrl: String, sessionId: String?) -> Unit) {
        val token = AuthManager.token ?: run { checkoutError = getApplication<Application>().getString(R.string.sponsor_error_not_logged_in); return }
        val planId = selectedPlanId ?: run { checkoutError = getApplication<Application>().getString(R.string.sponsor_error_select_plan); return }

        isCreatingCheckout = true
        checkoutError = null
        val errorCreateCheckout = getApplication<Application>().getString(R.string.sponsor_error_create_checkout)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val url = "${RuoYiClient.baseUrl}/member/subscription/create-checkout"
                    val jsonBody = JSONObject().apply { put("planId", planId) }
                    val request = Request.Builder()
                        .url(url)
                        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                        .header("Authorization", "Bearer $token")
                        .header("Content-Type", "application/json")
                        .header("tenant-id", "1")
                        .build()
                    val response = RuoYiClient.okHttpClient.newCall(request).execute()
                    val body = response.body?.string() ?: ""
                    val root = JSONObject(body)
                    if (root.optInt("code", -1) == 0) {
                        val data = root.getJSONObject("data")
                        data.getString("checkoutUrl") to data.optString("sessionId", "")
                    } else {
                        throw Exception(root.optString("msg", errorCreateCheckout))
                    }
                }
            }
            result.fold(
                onSuccess = { (url, sessionId) -> onResult(url, sessionId) },
                onFailure = { error ->
                    KailLog.w(null, TAG, "createCheckoutUrl: create checkout failed: ${error.message}")
                    checkoutError = error.message
                }
            )
            isCreatingCheckout = false
        }
    }

    fun createCheckout(onResult: (checkoutUrl: String, sessionId: String?) -> Unit) { createCheckoutUrl(onResult) }
    fun createWechatCheckout() {
        wechatPayUrl = null
        createCheckoutUrl { url, _ -> wechatPayUrl = url }
    }

    fun checkSubscriptionStatus() {
        val token = AuthManager.token ?: return

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                RuoYiClient.getSubscriptionStatus(token)
            }

            result.fold(
                onSuccess = { status ->
                    AuthManager.updateSubscription(status.active, status.expiresAt)
                },
                onFailure = { e -> KailLog.w(null, TAG, "checkSubscriptionStatus: query subscription status failed: ${e.message}") }
            )
        }
    }
}
