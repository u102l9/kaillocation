package com.kail.location.models

import android.os.Parcel
import android.os.Parcelable

/**
 * 基站模拟数据模型
 * 对应 FakeLocation C0005
 */
data class CellInfo(
    var id: String = "",
    var networkType: String = "LTE",  // GSM, CDMA, LTE, WCDMA
    var mcc: Int = 460,
    var mnc: Int = 0,
    var lac: Int = 0,
    var cid: Long = 0,
    var psc: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var radius: Float = 1000f
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        networkType = parcel.readString() ?: "LTE",
        mcc = parcel.readInt(),
        mnc = parcel.readInt(),
        lac = parcel.readInt(),
        cid = parcel.readLong(),
        psc = parcel.readInt(),
        latitude = parcel.readDouble(),
        longitude = parcel.readDouble(),
        radius = parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(networkType)
        parcel.writeInt(mcc)
        parcel.writeInt(mnc)
        parcel.writeInt(lac)
        parcel.writeLong(cid)
        parcel.writeInt(psc)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeFloat(radius)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CellInfo> {
        override fun createFromParcel(parcel: Parcel): CellInfo = CellInfo(parcel)
        override fun newArray(size: Int): Array<CellInfo?> = arrayOfNulls(size)
    }
}
