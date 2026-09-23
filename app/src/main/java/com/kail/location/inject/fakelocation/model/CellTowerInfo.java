package com.kail.location.inject.fakelocation.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CellTowerInfo implements Parcelable {
    public static final Parcelable.Creator<CellTowerInfo> CREATOR = new Creator();

    private String radioType;

    private int mcc;

    private int mnc;

    private int lac;

    private int psc;

    private long cellId;

    private double latitude;

    private double longitude;

    private float accuracy;

    static class Creator implements Parcelable.Creator<CellTowerInfo> {
        Creator() {
        }

        @Override
        public CellTowerInfo createFromParcel(Parcel parcel) {
            return new CellTowerInfo(parcel);
        }

        @Override
        public CellTowerInfo[] newArray(int size) {
            return new CellTowerInfo[size];
        }
    }

    public CellTowerInfo() {
    }

    protected CellTowerInfo(Parcel parcel) {
        this.radioType = parcel.readString();
        this.mcc = parcel.readInt();
        this.mnc = parcel.readInt();
        this.lac = parcel.readInt();
        this.psc = parcel.readInt();
        this.cellId = parcel.readLong();
        this.latitude = parcel.readDouble();
        this.longitude = parcel.readDouble();
        this.accuracy = parcel.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.radioType);
        parcel.writeInt(this.mcc);
        parcel.writeInt(this.mnc);
        parcel.writeInt(this.lac);
        parcel.writeInt(this.psc);
        parcel.writeLong(this.cellId);
        parcel.writeDouble(this.latitude);
        parcel.writeDouble(this.longitude);
        parcel.writeFloat(this.accuracy);
    }

    public long getCellId() {
        return this.cellId;
    }

    public int getLac() {
        return this.lac;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public int getMcc() {
        return this.mcc;
    }

    public int getMnc() {
        return this.mnc;
    }

    public String getRadioType() {
        return this.radioType;
    }

    public int getPsc() {
        return this.psc;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
