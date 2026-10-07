package com.kail.location.models

import android.os.Parcel
import android.os.Parcelable

/**
 * WiFi模拟数据模型
 * 对应 FakeLocation C0087
 */
data class WifiInfo(
    var id: String = "",
    var name: String = "",
    var ssid: String = "",
    var bssid: String = "",
    var rssi: Int = -50,
    var frequency: Int = 2412,
    var linkSpeed: Int = 65,
    var capabilities: String = "[WPA-PSK-CCMP]"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        ssid = parcel.readString() ?: "",
        bssid = parcel.readString() ?: "",
        rssi = parcel.readInt(),
        frequency = parcel.readInt(),
        linkSpeed = parcel.readInt(),
        capabilities = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(ssid)
        parcel.writeString(bssid)
        parcel.writeInt(rssi)
        parcel.writeInt(frequency)
        parcel.writeInt(linkSpeed)
        parcel.writeString(capabilities)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<WifiInfo> {
        override fun createFromParcel(parcel: Parcel): WifiInfo = WifiInfo(parcel)
        override fun newArray(size: Int): Array<WifiInfo?> = arrayOfNulls(size)
    }
}
