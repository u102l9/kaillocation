package com.kail.location.network

import com.kail.location.models.CellInfo
import com.kail.location.utils.KailLog
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * OpenCellID API 客户端。
 * 通过经纬度范围查询周边基站与 WiFi 数据。
 *
 * 文档: https://opencellid.org/ajax/searchCell.php
 */
object OpenCellIdClient {

    private const val TAG = "OpenCellIdClient"
    private const val BASE_URL = "https://opencellid.org/cell/getInArea"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun buildBBox(lat: Double, lon: Double, radiusKm: Double): Pair<String, String> {
        val latDelta = radiusKm / 111.0
        val lonDelta = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(lat)))
        val lat1 = lat - latDelta
        val lat2 = lat + latDelta
        val lon1 = lon - lonDelta
        val lon2 = lon + lonDelta
        return Pair("$lat1,$lon1,$lat2,$lon2", "$lat1,$lon1,$lat2,$lon2")
    }

    private fun doRequest(url: String): String? {
        // Log URL with key masked for security
        val masked = url.replace(Regex("key=[^&]*"), "key=xxx")
        KailLog.d(null, TAG, "Request URL: $masked")
        return try {
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            KailLog.d(null, TAG, "Response code: ${response.code}")
            if (!response.isSuccessful) {
                KailLog.w(null, TAG, "OpenCellID request failed: ${response.code}, body=${response.body?.string()}")
                return null
            }
            val body = response.body?.string()
            KailLog.d(null, TAG, "Response body: ${body?.take(500)}")
            body
        } catch (e: Exception) {
            KailLog.e(null, TAG, "OpenCellID fetch error: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    /**
     * 查询指定坐标周边范围内的基站。
     *
     * @param apiKey OpenCellID API Key
     * @param lat 中心纬度 (WGS84)
     * @param lon 中心经度 (WGS84)
     * @param radiusKm 查询半径（公里），默认 2km
     * @return 基站列表，失败返回空列表
     */
    fun fetchCellsInArea(
        apiKey: String,
        lat: Double,
        lon: Double,
        radiusKm: Double = 0.5
    ): List<CellInfo> {
        if (apiKey.isBlank()) {
            KailLog.w(null, TAG, "API key is blank")
            return emptyList()
        }

        val (bbox, _) = buildBBox(lat, lon, radiusKm)
        val url = "$BASE_URL?key=$apiKey&BBOX=$bbox&format=json"
        val body = doRequest(url) ?: return emptyList()
        return parseCellResponse(body)
    }

    private fun parseCellResponse(json: String): List<CellInfo> {
        val results = mutableListOf<CellInfo>()
        try {
            val root = JSONObject(json)
            if (!root.has("cells")) {
                KailLog.w(null, TAG, "No 'cells' field in response. Keys: ${root.keys().asSequence().toList()}")
                return results
            }
            val cells = root.getJSONArray("cells")
            KailLog.d(null, TAG, "Parsed ${cells.length()} cells total")
            for (i in 0 until cells.length()) {
                val obj = cells.getJSONObject(i)
                val radio = obj.optString("radio", "")
                if (radio.equals("wifi", ignoreCase = true)) continue
                val info = CellInfo(
                    id = java.util.UUID.randomUUID().toString(),
                    networkType = inferNetworkType(obj),
                    mcc = obj.optInt("mcc", 460),
                    mnc = obj.optInt("mnc", 0),
                    lac = obj.optInt("lac", 0),
                    cid = obj.optLong("cellId", 0),
                    psc = obj.optInt("psc", 0),
                    latitude = obj.optDouble("lat", 0.0),
                    longitude = obj.optDouble("lon", 0.0),
                    radius = obj.optDouble("range", 1000.0).toFloat()
                )
                results.add(info)
            }
            KailLog.d(null, TAG, "Filtered ${results.size} cell towers")
        } catch (e: Exception) {
            KailLog.e(null, TAG, "Parse cell error", e)
        }
        return results
    }

    private fun inferNetworkType(obj: JSONObject): String {
        val radio = obj.optString("radio", "")
        return when (radio.uppercase()) {
            "GSM" -> "GSM"
            "CDMA" -> "CDMA"
            "UMTS", "WCDMA" -> "WCDMA"
            "LTE" -> "LTE"
            "NR" -> "LTE"
            else -> {
                val cid = obj.optLong("cellId", 0)
                when {
                    cid > 268435455 -> "LTE"
                    cid > 65535 -> "WCDMA"
                    else -> "GSM"
                }
            }
        }
    }

}
