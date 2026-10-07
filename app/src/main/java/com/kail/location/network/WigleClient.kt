package com.kail.location.network

import android.util.Base64
import com.kail.location.models.WifiInfo
import com.kail.location.utils.KailLog
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * WiGLE API 客户端。
 * 通过经纬度范围查询周边 WiFi 热点数据。
 *
 * 需先在 https://wigle.net 注册账号并生成 API Token。
 * Auth: HTTP Basic Auth (username:APIToken)
 */
object WigleClient {

    private const val TAG = "WigleClient"
    private const val BASE_URL = "https://api.wigle.net/api/v2/network/search"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * 查询指定坐标周边范围内的 WiFi。
     *
     * @param apiKey WiGLE 的 API Token（格式: username:token，token 在 wigle.net → Account → API Token 获取）
     * @param lat 中心纬度 (WGS84)
     * @param lon 中心经度 (WGS84)
     * @param radiusKm 查询半径（公里），默认 0.3km
     * @return WiFi 列表，失败返回空列表
     */
    fun fetchWifiInArea(
        apiKey: String,
        lat: Double,
        lon: Double,
        radiusKm: Double = 0.3
    ): List<WifiInfo> {
        if (apiKey.isBlank()) {
            KailLog.w(null, TAG, "API key is blank")
            return emptyList()
        }

        val latDelta = radiusKm / 111.0
        val lonDelta = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(lat)))

        val lat1 = lat - latDelta
        val lat2 = lat + latDelta
        val lon1 = lon - lonDelta
        val lon2 = lon + lonDelta

        val url = "$BASE_URL?onlymine=false&freenet=false&paynet=false" +
                "&latrange1=$lat1&latrange2=$lat2" +
                "&longrange1=$lon1&longrange2=$lon2&results=50"

        val auth = "Basic " + Base64.encodeToString("$apiKey:".toByteArray(), Base64.NO_WRAP)

        return try {
            val request = Request.Builder()
                .url(url)
                .header("Authorization", auth)
                .get()
                .build()
            val response = client.newCall(request).execute()
            KailLog.d(null, TAG, "Response code: ${response.code}")
            if (!response.isSuccessful) {
                KailLog.w(null, TAG, "WiGLE request failed: ${response.code}, body=${response.body?.string()}")
                return emptyList()
            }
            val body = response.body?.string() ?: return emptyList()
            KailLog.d(null, TAG, "Response body: ${body.take(500)}")
            parseResponse(body)
        } catch (e: Exception) {
            KailLog.e(null, TAG, "WiGLE fetch error: ${e.javaClass.simpleName}: ${e.message}")
            emptyList()
        }
    }

    private fun parseResponse(json: String): List<WifiInfo> {
        val results = mutableListOf<WifiInfo>()
        try {
            val root = JSONObject(json)
            if (!root.has("results")) {
                KailLog.w(null, TAG, "No 'results' field. Keys: ${root.keys().asSequence().toList()}")
                return emptyList()
            }
            val array = root.getJSONArray("results")
            KailLog.d(null, TAG, "Parsed ${array.length()} results")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val ssid = obj.optString("ssid", "")
                val bssid = obj.optString("netid", "")
                if (bssid.isBlank()) continue

                val channel = obj.optInt("channel", 6)
                val frequency = channelToFrequency(channel)

                results.add(
                    WifiInfo(
                        id = java.util.UUID.randomUUID().toString(),
                        name = ssid.ifBlank { bssid },
                        ssid = ssid,
                        bssid = normalizeBssid(bssid),
                        rssi = -50 - (kotlin.random.Random.nextInt(0, 20)),
                        frequency = frequency,
                        linkSpeed = 65,
                        capabilities = "[WPA2-PSK-CCMP]"
                    )
                )
            }
            KailLog.d(null, TAG, "Filtered ${results.size} wifi entries")
        } catch (e: Exception) {
            KailLog.e(null, TAG, "Parse error", e)
        }
        return results
    }

    private fun channelToFrequency(channel: Int): Int {
        return when {
            channel in 1..14 -> 2407 + channel * 5  // 2.4GHz
            channel in 36..165 -> 5000 + channel * 5 // 5GHz rough
            else -> 2412
        }
    }

    private fun normalizeBssid(mac: String): String {
        val clean = mac.replace(":", "").replace("-", "").uppercase()
        if (clean.length != 12) return mac
        return clean.chunked(2).joinToString(":")
    }
}
