package com.kail.location.inject.utils;

import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import com.kail.location.inject.fakelocation.model.MockWifiNetwork;

public class MockWifiConfigManager {
    private static boolean mockWifiEnabled;
    private static List<MockWifiNetwork> mockWifiNetworks;
    private static List<String> allowMockPackages;
    private static final Object configLock = new Object();
    private static List<String> scopedAllowMockRules;

    private static final String bytesToHex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer(bytes.length);
        for (byte value : bytes) {
            String hexString = Integer.toHexString(value & 255);
            if (hexString.length() < 2) {
                stringBuffer.append(0);
            }
            stringBuffer.append(hexString.toUpperCase());
        }
        return stringBuffer.toString();
    }

    public static List<String> getAllowMockPackages() {
        List<String> packages;
        synchronized (configLock) {
            packages = allowMockPackages;
        }
        return packages;
    }

    public static MockWifiNetwork getPrimaryMockWifiNetwork() {
        synchronized (configLock) {
            try {
                try {
                    List<MockWifiNetwork> list = mockWifiNetworks;
                    if (list != null && !list.isEmpty()) {
                        return mockWifiNetworks.get(0);
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } finally {
            }
        }
    }

    public static List<MockWifiNetwork> getMockWifiNetworks() {
        List<MockWifiNetwork> networks;
        synchronized (configLock) {
            networks = mockWifiNetworks;
        }
        return networks;
    }

    public static List<String> getScopedAllowMockRules() {
        List<String> rules;
        synchronized (configLock) {
            rules = scopedAllowMockRules;
        }
        return rules;
    }

    private static boolean hasNoPackageFilters() {
        List<String> rules;
        boolean hasNoFilters;
        synchronized (configLock) {
            List<String> packages = allowMockPackages;
            hasNoFilters = (packages == null || packages.isEmpty()) && ((rules = scopedAllowMockRules) == null || rules.isEmpty());
        }
        return hasNoFilters;
    }

    public static boolean isPackageAllowedForPidUid(int pid, int uid) {
        if (hasNoPackageFilters()) {
            return true;
        }
        return isPackageAllowed(CallingProcessUtils.getPackageNameForPidOrUid(pid, uid));
    }

    public static boolean isPackageAllowed(String packageName) {
        if (packageName == null) {
            return false;
        }
        synchronized (configLock) {
            if (hasNoPackageFilters()) {
                return true;
            }
            List<String> rules = scopedAllowMockRules;
            if (rules != null && !rules.isEmpty() && !ScopedListFilter.isAllowed(scopedAllowMockRules, packageName, "c")) {
                return false;
            }
            List<String> packages = allowMockPackages;
            if (packages == null || packages.isEmpty()) {
                return true;
            }
            return allowMockPackages.contains(packageName);
        }
    }

    public static boolean isMockWifiEnabled() {
        return mockWifiEnabled;
    }

    public static void setAllowMockPackages(List<String> packages) {
        synchronized (configLock) {
            allowMockPackages = packages;
        }
    }

    public static void setPrimaryMockWifiNetwork(MockWifiNetwork network) {
        synchronized (configLock) {
            // 只替换主网络（列表第 0 个），不能清空其余已选网络。
            // 之前直接 new 一个只含 network 的 list，导致多选 WiFi 只生效第一个。
            if (mockWifiNetworks != null && !mockWifiNetworks.isEmpty()) {
                ArrayList<MockWifiNetwork> copy = new ArrayList<>(mockWifiNetworks);
                copy.set(0, network);
                mockWifiNetworks = copy;
            } else {
                ArrayList<MockWifiNetwork> arrayList = new ArrayList<>();
                arrayList.add(network);
                mockWifiNetworks = arrayList;
            }
        }
    }

    public static void setMockWifiNetworks(List<MockWifiNetwork> networks) {
        synchronized (configLock) {
            mockWifiNetworks = networks;
        }
    }

    public static void setMockWifiEnabled(boolean enabled) {
        mockWifiEnabled = enabled;
    }

    public static void setScopedAllowMockRules(List<String> rules) {
        synchronized (configLock) {
            scopedAllowMockRules = rules;
        }
    }

    public static ScanResult createScanResult(MockWifiNetwork network) {
        if (network == null) {
            return null;
        }
        try {
            ScanResult scanResult = (ScanResult) ScanResult.class.newInstance();
            scanResult.SSID = network.getSsid();
            scanResult.BSSID = network.getBssid();
            scanResult.level = network.getRssi();
            scanResult.frequency = network.getFrequency();
            scanResult.capabilities = network.getCapabilities();
            return scanResult;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static WifiInfo createWifiInfo(MockWifiNetwork network) {
        Object wifiSsid;
        if (network == null) {
            return null;
        }
        try {
            WifiInfo wifiInfo = (WifiInfo) WifiInfo.class.newInstance();
            try {
                if (!TextUtils.isEmpty(network.getSsid())) {
                    Class wifiSsidClass = ReflectionUtils.forName("android.net.wifi.WifiSsid");
                    try {
                        Method fromBytesMethod = wifiSsidClass.getDeclaredMethod("fromBytes", byte[].class);
                        fromBytesMethod.setAccessible(true);
                        wifiSsid = fromBytesMethod.invoke(null, network.getSsid().getBytes());
                    } catch (Throwable th) {
                        th.printStackTrace();
                        try {
                            Method createFromByteArrayMethod = wifiSsidClass.getDeclaredMethod("createFromByteArray", byte[].class);
                            createFromByteArrayMethod.setAccessible(true);
                            wifiSsid = createFromByteArrayMethod.invoke(null, network.getSsid().getBytes());
                        } catch (Throwable th2) {
                            th2.printStackTrace();
                            try {
                                Method createFromHexMethod = wifiSsidClass.getDeclaredMethod("createFromHex", String.class);
                                createFromHexMethod.setAccessible(true);
                                wifiSsid = createFromHexMethod.invoke(null, bytesToHex(network.getSsid().getBytes()));
                            } catch (Throwable th3) {
                                th3.printStackTrace();
                                wifiSsid = null;
                            }
                        }
                    }
                    Method setSsidMethod = WifiInfo.class.getDeclaredMethod("setSSID", wifiSsidClass);
                    setSsidMethod.setAccessible(true);
                    setSsidMethod.invoke(wifiInfo, wifiSsid);
                }
            } catch (Throwable th4) {
                th4.printStackTrace();
            }
            Method setMacAddressMethod = WifiInfo.class.getDeclaredMethod("setMacAddress", String.class);
            setMacAddressMethod.setAccessible(true);
            setMacAddressMethod.invoke(wifiInfo, network.getBssid());
            Method setBssidMethod = WifiInfo.class.getDeclaredMethod("setBSSID", String.class);
            setBssidMethod.setAccessible(true);
            setBssidMethod.invoke(wifiInfo, network.getBssid());
            Class intType = Integer.TYPE;
            Method setRssiMethod = WifiInfo.class.getDeclaredMethod("setRssi", intType);
            setRssiMethod.setAccessible(true);
            setRssiMethod.invoke(wifiInfo, Integer.valueOf(network.getRssi()));
            Method setLinkSpeedMethod = WifiInfo.class.getDeclaredMethod("setLinkSpeed", intType);
            setLinkSpeedMethod.setAccessible(true);
            setLinkSpeedMethod.invoke(wifiInfo, Integer.valueOf(network.getLinkSpeed()));
            Method setFrequencyMethod = WifiInfo.class.getDeclaredMethod("setFrequency", intType);
            setFrequencyMethod.setAccessible(true);
            setFrequencyMethod.invoke(wifiInfo, Integer.valueOf(network.getFrequency()));
            Method setNetworkIdMethod = WifiInfo.class.getDeclaredMethod("setNetworkId", intType);
            setNetworkIdMethod.setAccessible(true);
            setNetworkIdMethod.invoke(wifiInfo, 1000);
            Field scoreField = WifiInfo.class.getDeclaredField("score");
            scoreField.setAccessible(true);
            scoreField.set(wifiInfo, 60);
            Method setSupplicantStateMethod = WifiInfo.class.getDeclaredMethod("setSupplicantState", SupplicantState.class);
            setSupplicantStateMethod.setAccessible(true);
            setSupplicantStateMethod.invoke(wifiInfo, SupplicantState.COMPLETED);
            return wifiInfo;
        } catch (Throwable th5) {
            th5.printStackTrace();
            return null;
        }
    }
}
