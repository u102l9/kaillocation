package com.kail.location.views.sponsor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.kail.location.R
import com.kail.location.auth.AuthManager
import com.kail.location.network.RuoYiClient
import com.kail.location.utils.KailLog
import com.kail.location.views.base.BaseActivity
import com.kail.location.views.theme.locationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import android.graphics.Color as AndroidColor

/**
 * 收银台 WebView（Paddle/Stripe）。
 *
 * Stripe 微信支付二维码在 Android WebView 内无法渲染，因此：
 * WebView 仅用于展示结算页/发起支付；支付意图确认后由本类轮询后端 stripe-qrcode
 * 接口拿到二维码图片，用原生 Dialog 展示，并提供"微信内打开"按钮（weixin://）唤起微信。
 */
@OptIn(ExperimentalMaterial3Api::class)
class CheckoutWebViewActivity : BaseActivity() {

    companion object {
        const val EXTRA_URL = "checkout_url"
        const val EXTRA_SESSION_ID = "checkout_session_id"
        private const val TAG = "CheckoutWebView"
    }

    private var qrCodeImage by mutableStateOf<String?>(null)
    private var wechatUrl by mutableStateOf<String?>(null)
    private var redirectUrl by mutableStateOf<String?>(null)
    private var paymentMethod by mutableStateOf<String?>(null)
    private var sessionId: String = ""
    private var polling = false
    private var qrDismissed = false
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val checkoutUrl = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: ""

        setContent {
            locationTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(getString(R.string.checkout_title)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = getString(R.string.checkout_back_desc))
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White
                            )
                        )
                    }
                ) { paddingValues ->
                    AndroidView(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                webView = this
                                WebView.setWebContentsDebuggingEnabled(true)

                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false

                                webChromeClient = object : WebChromeClient() {
                                    override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
                                        Log.d(TAG, "$sourceID:$lineNumber: $message")
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                                        Log.d(TAG, "shouldOverrideUrlLoading: ${url.take(120)}")
                                        // 支付宝：Stripe 页面点支付后会自动发 alipays:// 唤起支付宝。
                                        // 不自动唤起——拦截停住，由用户点击弹窗「支付宝内打开支付」按钮再跳转。
                                        if (url.startsWith("alipays://") || url.startsWith("alipay://")) {
                                            Log.d(TAG, "阻止自动唤起支付宝（等待用户点击弹窗按钮）：${url.take(80)}")
                                            return true
                                        }
                                        return super.shouldOverrideUrlLoading(view, url)
                                    }

                                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        Log.d(TAG, "onPageStarted: ${url.take(100)}")
                                    }

                                    override fun onPageFinished(view: WebView, url: String) {
                                        super.onPageFinished(view, url)
                                        Log.d(TAG, "onPageFinished: ${url.take(100)}")
                                    }
                                }

                                loadUrl(checkoutUrl)
                            }
                        }
                    )
                }

                // 原生支付弹窗（微信/支付宝通用：二维码 + 跳转应用；可关闭；深色模式自适应；二维码白底）
                if (paymentMethod == "wechat_pay" || paymentMethod == "alipay") {
                    val isWechat = paymentMethod == "wechat_pay"
                    val title = if (isWechat) "微信支付" else "支付宝支付"
                    val qrContent = if (isWechat) wechatUrl else redirectUrl
                    val launchText = if (isWechat) "微信内打开支付" else "支付宝内打开支付"
                    AlertDialog(
                        onDismissRequest = {
                            Log.d(TAG, "支付弹窗被关闭（onDismissRequest），后续不再自动弹出")
                            qrDismissed = true
                            qrCodeImage = null
                            paymentMethod = null
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(title, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    Log.d(TAG, "支付弹窗被关闭（X 按钮），后续不再自动弹出")
                                    qrDismissed = true
                                    qrCodeImage = null
                                    paymentMethod = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "关闭")
                                }
                            }
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                val bitmap = try {
                                    // 优先本地生成高清二维码（Stripe 返回的 image_data_url 分辨率极低）
                                    val fromUrl = qrContent?.let { generateQrBitmap(it, 512) }
                                    if (fromUrl != null) {
                                        fromUrl
                                    } else {
                                        qrCodeImage?.let { img ->
                                            val clean = img.removePrefix("data:image/png;base64,").removePrefix("data:image/jpeg;base64,")
                                            val bytes = Base64.decode(clean, Base64.DEFAULT)
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        }
                                    }
                                } catch (e: Exception) { null }
                                if (bitmap != null) {
                                    // 固定白色背景：深色模式下二维码区仍是白底黑码，保证可识别
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = title + "二维码",
                                            modifier = Modifier.size(220.dp)
                                        )
                                    }
                                }
                                Text(if (isWechat) "请使用微信扫描二维码支付" else "请使用支付宝扫描二维码支付", modifier = Modifier.padding(top = 10.dp))
                                qrContent?.let { content ->
                                    Button(
                                        onClick = {
                                            Log.d(TAG, "点击「$launchText」，$title content=${content.take(60)}")
                                            launchAppPay(content)
                                        },
                                        modifier = Modifier.padding(top = 12.dp).fillMaxWidth()
                                    ) {
                                        Text(launchText)
                                    }
                                    Text(
                                        if (isWechat) "如无法唤起，请用另存微信或分身微信扫描上方二维码"
                                        else "如无法唤起，请用支付宝扫描上方二维码",
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {},
                    )
                }
            }
        }

        // 轮询二维码
        if (sessionId.isNotEmpty()) {
            startQrCodePolling(sessionId)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionId.isNotEmpty()) {
            startQrCodePolling(sessionId)
        }

    }

    private fun startQrCodePolling(sessionId: String) {
        if (polling) return
        polling = true
        val token = AuthManager.token ?: return
        lifecycleScope.launch {
            try {
                while (isActive) {
                    try {
                        val (qrImage, wxUrl, status, pm, redir) = withContext(Dispatchers.IO) {
                            fetchStripeQrCode(token, sessionId)
                        }
                        Log.d(TAG, "poll result: status=$status, pm=$pm, qrImage=${qrImage != null}, wxUrl=${wxUrl?.take(40)}, redirectUrl=${redir?.take(60)}")
                        when (status) {
                            "requires_action" -> {
                                if (pm == "wechat_pay" || pm == "alipay") {
                                    if (!qrDismissed && (wxUrl != null || redir != null || qrImage != null)) {
                                        Log.d(TAG, "poll: 展示支付弹窗（pm=$pm，qrDismissed=$qrDismissed）")
                                        paymentMethod = pm
                                        qrCodeImage = qrImage
                                        wechatUrl = wxUrl
                                        redirectUrl = redir
                                    } else if (qrDismissed) {
                                        Log.d(TAG, "poll: 弹窗已被用户关闭，不再自动弹出（后台轮询等待支付结果）")
                                    }
                                } else {
                                    Log.d(TAG, "poll: requires_action pm=$pm，非微信/支付宝，忽略")
                                }
                            }
                            "succeeded" -> {
                                Log.d(TAG, "poll: 支付成功，准备返回")
                                Toast.makeText(this@CheckoutWebViewActivity, "支付成功", Toast.LENGTH_SHORT).show()
                                delay(600)
                                finish()
                                return@launch
                            }
                            "canceled", "failed", "processing" -> delay(3000)
                            else -> delay(2500)
                        }
                    } catch (e: Exception) {
                        KailLog.w(null, TAG, "poll qr code error: ${e.message}")
                        Log.d(TAG, "poll error: ${e.message}")
                        delay(3000)
                    }
                }
            } finally {
                polling = false
            }
        }
    }

    /**
     * 用 ZXing 本地生成高清二维码（替代 Stripe 低分辨率 image_data_url）
     */
    private fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val matrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            KailLog.w(null, TAG, "generateQrBitmap failed: ${e.message}")
            null
        }
    }

    private fun launchAppPay(content: String) {
        try {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(content)
            )
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            // 不 setPackage：weixin:// 交给系统解析器（可直接唤起微信/分身，部分机型弹选择器）；
            // 支付宝的 https 链接会交给浏览器打开，浏览器/收银台会自动 alipays:// 唤起支付宝
            startActivity(intent)
            Log.d(TAG, "launchAppPay: 已发起唤起 $content")
        } catch (e: Exception) {
            Log.w(TAG, "launchAppPay 失败: ${e.message}")
            Toast.makeText(this, "未能唤起支付应用，请使用扫码支付", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStripeQrCode(token: String, sessionId: String): Five<String?, String?, String, String, String?> {        val url = "${RuoYiClient.baseUrl}/member/subscription/stripe-qrcode?sessionId=$sessionId"
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", "Bearer $token")
            .header("tenant-id", "1")
            .build()
        val response = RuoYiClient.okHttpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""
        val root = JSONObject(body)
        if (root.optInt("code", -1) != 0) {
            throw Exception(root.optString("msg", "query failed"))
        }
        val data = root.getJSONObject("data")
        return Five(
            data.optString("qrCodeImage", "").ifEmpty { null },
            data.optString("wechatUrl", "").ifEmpty { null },
            data.optString("status", "pending"),
            data.optString("paymentMethod", ""),
            data.optString("redirectUrl", "").ifEmpty { null }
        )
    }

    data class Five<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
}
