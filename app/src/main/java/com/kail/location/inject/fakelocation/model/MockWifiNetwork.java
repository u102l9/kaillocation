package com.kail.location.inject.fakelocation.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MockWifiNetwork implements Parcelable {
    public static final Parcelable.Creator<MockWifiNetwork> CREATOR = new Creator();

    public String id;

    public String networkType;

    private String ssid;

    private String bssid;

    private String username;

    private String password;

    private int rssi;

    private int linkSpeed;

    private int frequency;

    private String capabilities;

    static class Creator implements Parcelable.Creator<MockWifiNetwork> {
        Creator() {
        }

        @Override
        public MockWifiNetwork createFromParcel(Parcel parcel) {
            return new MockWifiNetwork(parcel);
        }

        @Override
        public MockWifiNetwork[] newArray(int size) {
            return new MockWifiNetwork[size];
        }
    }

    public MockWifiNetwork() {
        this.id = "" + System.currentTimeMillis();
        this.rssi = 200;
        this.linkSpeed = 866;
        this.frequency = 5745;
        this.capabilities = "[WPA-PSK-TKIP+CCMP][WPA2-PSK-TKIP+CCMP][ESS][WPS]";
    }

    protected MockWifiNetwork(Parcel parcel) {
        this.id = "" + System.currentTimeMillis();
        this.rssi = 200;
        this.linkSpeed = 866;
        this.frequency = 5745;
        this.capabilities = "[WPA-PSK-TKIP+CCMP][WPA2-PSK-TKIP+CCMP][ESS][WPS]";
        this.id = parcel.readString();
        this.networkType = parcel.readString();
        this.ssid = parcel.readString();
        this.bssid = parcel.readString();
        this.username = parcel.readString();
        this.password = parcel.readString();
        this.rssi = parcel.readInt();
        this.linkSpeed = parcel.readInt();
        this.frequency = parcel.readInt();
        this.capabilities = parcel.readString();
    }

    public MockWifiNetwork(String networkType, String ssid, String bssid, int rssi, int linkSpeed, int frequency, String capabilities) {
        this.id = "" + System.currentTimeMillis();
        this.rssi = 200;
        this.linkSpeed = 866;
        this.frequency = 5745;
        this.capabilities = "[WPA-PSK-TKIP+CCMP][WPA2-PSK-TKIP+CCMP][ESS][WPS]";
        this.networkType = networkType;
        this.ssid = ssid;
        this.bssid = bssid;
        this.rssi = rssi;
        this.linkSpeed = linkSpeed;
        this.frequency = frequency;
        this.capabilities = capabilities;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.id);
        parcel.writeString(this.networkType);
        parcel.writeString(this.ssid);
        parcel.writeString(this.bssid);
        parcel.writeString(this.username);
        parcel.writeString(this.password);
        parcel.writeInt(this.rssi);
        parcel.writeInt(this.linkSpeed);
        parcel.writeInt(this.frequency);
        parcel.writeString(this.capabilities);
    }

    public String getBssid() {
        return this.bssid;
    }

    public String getCapabilities() {
        return this.capabilities;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public int getLinkSpeed() {
        return this.linkSpeed;
    }

    public int getRssi() {
        return this.rssi;
    }

    public String getSsid() {
        return this.ssid;
    }
}
