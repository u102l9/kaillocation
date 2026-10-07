package com.kail.location.inject.fakelocation.hook.app;

import android.location.Location;
import android.net.wifi.WifiInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import com.kail.location.inject.utils.ScopedListFilter;
import com.kail.location.inject.utils.CallingProcessUtils;
import com.kail.location.inject.utils.CellInfoFactory;
import com.kail.location.inject.utils.ReflectionUtils;
import com.kail.location.inject.utils.MockLocationServiceManager;
import com.kail.location.lib.lhooker.LHooker;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import com.kail.location.inject.fakelocation.model.CellTowerInfo;

/* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ށ, reason: contains not printable characters */
/* JADX INFO: loaded from: /home/kail/code/tool/jadx-1.5.5/bin/classes.dex */
public class ClientMockHook {

    /* JADX INFO: renamed from: ֏, reason: contains not printable characters */
    private static String targetPackageName;

    /* JADX INFO: renamed from: ؠ, reason: contains not printable characters */
    static int cellLocationRequestCount;

    /* JADX INFO: renamed from: ހ, reason: contains not printable characters */
    static long lastCellLocationTimeMillis;

    public static float getAccuracy(Object obj) {
        Location locationM163;
        log("getAccuracy", obj);
        try {
            if (isHook() && (locationM163 = MockLocationServiceManager.getInstance().getMockLocation()) != null) {
                Object objM107 = ReflectionUtils.getFieldValue(locationM163, Location.class, "mHorizontalAccuracyMeters");
                if (objM107 == null) {
                    objM107 = ReflectionUtils.getFieldValue(locationM163, Location.class, "mAccuracy");
                }
                if (objM107 != null) {
                    return Float.parseFloat("" + objM107);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return getAccuracy_bak(obj);
    }

    public static float getAccuracy_bak(Object obj) {
        log("getAccuracy_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getAccuracy_copy(obj);
    }

    public static float getAccuracy_copy(Object obj) {
        log("getAccuracy_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return 0.0f;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public static int getActiveSubscriptionInfoCount(Object obj) {
        List<SubscriptionInfo> listM164;
        return (!isHookSIMInfo() || (listM164 = MockLocationServiceManager.getInstance().getMockSubscriptionInfo()) == null) ? getActiveSubscriptionInfoCount_bak(obj) : listM164.size();
    }

    public static int getActiveSubscriptionInfoCountMax(Object obj) {
        List<SubscriptionInfo> listM164;
        return (!isHookSIMInfo() || (listM164 = MockLocationServiceManager.getInstance().getMockSubscriptionInfo()) == null) ? getActiveSubscriptionInfoCountMax_bak(obj) : listM164.size();
    }

    public static int getActiveSubscriptionInfoCountMax_bak(Object obj) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getActiveSubscriptionInfoCountMax_copy(obj);
    }

    public static int getActiveSubscriptionInfoCountMax_copy(Object obj) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getActiveSubscriptionInfoCount_bak(Object obj) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getActiveSubscriptionInfoCount_copy(obj);
    }

    public static int getActiveSubscriptionInfoCount_copy(Object obj) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<SubscriptionInfo> getActiveSubscriptionInfoList(Object obj) {
        return isHookSIMInfo() ? MockLocationServiceManager.getInstance().getMockSubscriptionInfo() : getActiveSubscriptionInfoList_bak(obj);
    }

    public static List<SubscriptionInfo> getActiveSubscriptionInfoList_bak(Object obj) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getActiveSubscriptionInfoList_copy(obj);
    }

    public static List<SubscriptionInfo> getActiveSubscriptionInfoList_copy(Object obj) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<CellInfo> getAllCellInfo(Object obj) {
        log("getAllCellInfo: ", obj, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        if (!isHook()) {
            try {
                List<CellInfo> allCellInfo_bak = getAllCellInfo_bak(obj);
                return allCellInfo_bak == null ? new ArrayList() : allCellInfo_bak;
            } catch (Throwable th) {
                th.printStackTrace();
                return new ArrayList();
            }
        }
        try {
            System.currentTimeMillis();
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            CellInfoFactory.setCurrentLocation(MockLocationServiceManager.getInstance().getMockLocation());
            List<CellInfo> listM101 = CellInfoFactory.createCellInfoList(listM162);
            if (listM101 != null && !listM101.isEmpty()) {
                log("mockAllCellInfo: ", listM101);
                return listM101;
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        return new ArrayList();
    }

    public static List<CellInfo> getAllCellInfo_bak(Object obj) {
        try {
            log("getAllCellInfo_bak", obj);
            long jCurrentTimeMillis = System.currentTimeMillis();
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
            }
            int i = ((System.currentTimeMillis() - jCurrentTimeMillis) > 20000L ? 1 : ((System.currentTimeMillis() - jCurrentTimeMillis) == 20000L ? 0 : -1));
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getAllCellInfo_copy(obj);
    }

    public static List<CellInfo> getAllCellInfo_copy(Object obj) {
        try {
            log("getAllCellInfo_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            long jCurrentTimeMillis = System.currentTimeMillis();
            if (listM162 != null) {
                listM162.size();
            }
            int i = ((System.currentTimeMillis() - jCurrentTimeMillis) > 20000L ? 1 : ((System.currentTimeMillis() - jCurrentTimeMillis) == 20000L ? 0 : -1));
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double getAltitude(Object obj) {
        Location locationM163;
        log("getAltitude", obj);
        try {
            if (isHook() && (locationM163 = MockLocationServiceManager.getInstance().getMockLocation()) != null) {
                return ((Double) ReflectionUtils.getFieldValue(locationM163, Location.class, "mAltitude")).doubleValue();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return getAltitude_bak(obj);
    }

    public static double getAltitude_bak(Object obj) {
        log("getAltitude_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getAltitude_copy(obj);
    }

    public static double getAltitude_copy(Object obj) {
        log("getAltitude_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return 0.0d;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0d;
        }
    }

    public static String getBSSID(Object obj) {
        return "";
    }

    public static float getBearing(Object obj) {
        Location locationM163;
        log("getBearing", obj);
        try {
            if (isHook() && (locationM163 = MockLocationServiceManager.getInstance().getMockLocation()) != null) {
                return ((Float) ReflectionUtils.getFieldValue(locationM163, Location.class, "mBearing")).floatValue();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return getBearing_bak(obj);
    }

    public static float getBearing_bak(Object obj) {
        log("getBearing_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getBearing_copy(obj);
    }

    public static float getBearing_copy(Object obj) {
        log("getBearing_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return 0.0f;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public static CellLocation getCellLocation(Object obj) {
        int iM260;
        log("getCellLocation: ", obj, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        if (!isHook()) {
            try {
                CellLocation cellLocation_bak = getCellLocation_bak(obj);
                log("srcCellLocation: " + cellLocation_bak);
                return cellLocation_bak;
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        }
        try {
            Bundle bundle = new Bundle();
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 == null || listM162.isEmpty()) {
                iM260 = Integer.MAX_VALUE;
                bundle.putInt("cid", Integer.MAX_VALUE);
                bundle.putInt("lac", Integer.MAX_VALUE);
                bundle.putInt("psc", Integer.MAX_VALUE);
                bundle.putInt("baseStationId", Integer.MAX_VALUE);
                bundle.putInt("systemId", Integer.MAX_VALUE);
            } else {
                CellTowerInfo c0058 = listM162.get(new SecureRandom().nextInt(Math.min(3, listM162.size())));
                bundle.putInt("cid", (int) c0058.getCellId());
                bundle.putInt("lac", c0058.getLac());
                bundle.putInt("psc", c0058.getPsc());
                bundle.putInt("baseStationId", (int) c0058.getCellId());
                bundle.putInt("systemId", c0058.getMnc());
                iM260 = c0058.getLac();
            }
            bundle.putInt("networkId", iM260);
            Location locationM163 = MockLocationServiceManager.getInstance().getMockLocation();
            if (locationM163 != null) {
                bundle.putInt("baseStationLatitude", (int) (locationM163.getLatitude() * 14400.0d));
                bundle.putInt("baseStationLongitude", (int) (locationM163.getLongitude() * 14400.0d));
            }
            bundle.putBoolean("empty", false);
            bundle.putBoolean("emptyParcel", false);
            bundle.putInt("mFlags", 1536);
            bundle.putBoolean("parcelled", false);
            bundle.putInt("size", 0);
            log("mockCellLocation: " + bundle);
            return new CdmaCellLocation(bundle);
        } catch (Throwable th2) {
            log("getCellLocation.err: " + th2.getMessage());
            th2.printStackTrace();
            return null;
        }
    }

    public static CellLocation getCellLocation_bak(Object obj) {
        try {
            log("getCellLocation_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = cellLocationRequestCount;
            } else {
                int i2 = cellLocationRequestCount;
            }
            int i3 = ((System.currentTimeMillis() - lastCellLocationTimeMillis) > 20000L ? 1 : ((System.currentTimeMillis() - lastCellLocationTimeMillis) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getCellLocation_copy(obj);
    }

    public static CellLocation getCellLocation_copy(Object obj) {
        try {
            log("getCellLocation_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = cellLocationRequestCount;
            } else {
                int i2 = cellLocationRequestCount;
            }
            int i3 = ((System.currentTimeMillis() - lastCellLocationTimeMillis) > 20000L ? 1 : ((System.currentTimeMillis() - lastCellLocationTimeMillis) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getCurrentPhoneType(Object obj) {
        return 0;
    }

    public static int getDataNetworkType(Object obj) {
        return 0;
    }

    public static double getLatitude(Object obj) {
        Location locationM163;
        log("getLatitude", obj);
        try {
            if (isHook() && (locationM163 = MockLocationServiceManager.getInstance().getMockLocation()) != null) {
                return ((Double) ReflectionUtils.getFieldValue(locationM163, Location.class, "mLatitude")).doubleValue();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return getLatitude_bak(obj);
    }

    public static double getLatitude_bak(Object obj) {
        log("getLatitude_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getLatitude_copy(obj);
    }

    public static double getLatitude_copy(Object obj) {
        log("getLatitude_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return 0.0d;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0d;
        }
    }

    public static double getLongitude(Object obj) {
        Location locationM163;
        log("getLongitude", obj);
        try {
            if (isHook() && (locationM163 = MockLocationServiceManager.getInstance().getMockLocation()) != null) {
                return ((Double) ReflectionUtils.getFieldValue(locationM163, Location.class, "mLongitude")).doubleValue();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return getLongitude_bak(obj);
    }

    public static double getLongitude_bak(Object obj) {
        log("getLongitude_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getLongitude_copy(obj);
    }

    public static double getLongitude_copy(Object obj) {
        log("getLongitude_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return 0.0d;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0d;
        }
    }

    public static String getMacAddress(Object obj) {
        return "";
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo(Object obj) {
        log("getNeighboringCellInfo: ", obj, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        if (!isHook()) {
            try {
                List<NeighboringCellInfo> neighboringCellInfo_bak = getNeighboringCellInfo_bak(obj);
                log("srcNeighboringCellInfo: ", neighboringCellInfo_bak);
                return neighboringCellInfo_bak;
            } catch (Throwable th) {
                th.printStackTrace();
                return new ArrayList();
            }
        }
        try {
            List<NeighboringCellInfo> listM103 = CellInfoFactory.createNeighboringCellInfoList(MockLocationServiceManager.getInstance().getMockCells());
            if (listM103 != null) {
                if (!listM103.isEmpty()) {
                    return listM103;
                }
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        return new ArrayList();
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo_bak(Object obj) {
        try {
            log("getNeighboringCellInfo_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = cellLocationRequestCount;
            } else {
                int i2 = cellLocationRequestCount;
            }
            int i3 = ((System.currentTimeMillis() - lastCellLocationTimeMillis) > 20000L ? 1 : ((System.currentTimeMillis() - lastCellLocationTimeMillis) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getNeighboringCellInfo_copy(obj);
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo_copy(Object obj) {
        try {
            log("getNeighboringCellInfo_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = cellLocationRequestCount;
            } else {
                int i2 = cellLocationRequestCount;
            }
            int i3 = ((System.currentTimeMillis() - lastCellLocationTimeMillis) > 20000L ? 1 : ((System.currentTimeMillis() - lastCellLocationTimeMillis) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNetworkCountryIso(Object obj) {
        return "";
    }

    public static String getNetworkOperator(Object obj) {
        return "";
    }

    public static String getNetworkOperatorName(Object obj) {
        log("getNetworkOperatorName", obj);
        return !isHook() ? getNetworkOperatorName_bak(obj) : "CDMA";
    }

    public static String getNetworkOperatorName_bak(Object obj) {
        log("getNetworkOperatorName_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getNetworkOperatorName_copy(obj);
    }

    public static String getNetworkOperatorName_copy(Object obj) {
        log("getNetworkOperatorName_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getNetworkType(Object obj) {
        return 0;
    }

    public static int getPhoneId(int i) {
        if (isHookSIMInfo()) {
            return 0;
        }
        return getPhoneId_bak(i);
    }

    public static int getPhoneId_bak(int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getPhoneId_copy(i);
    }

    public static int getPhoneId_copy(int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getPhoneType(Object obj) {
        return 0;
    }

    public static String getSSID(Object obj) {
        return "";
    }

    public static String getSimCountryIso(Object obj) {
        return "";
    }

    public static String getSimOperator(Object obj) {
        return "";
    }

    public static String getSimOperatorName(Object obj) {
        return "";
    }

    public static int getSimStateForSlotIdx(int i) {
        if (isHookSIMInfo()) {
            return 5;
        }
        return getSimStateForSlotIdx_bak(i);
    }

    public static int getSimStateForSlotIdx_bak(int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getSimStateForSlotIdx_copy(i);
    }

    public static int getSimStateForSlotIdx_copy(int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getSimStateForSlotIndex(int i) {
        if (isHookSIMInfo()) {
            return 5;
        }
        return getSimStateForSlotIndex_bak(i);
    }

    public static int getSimStateForSlotIndex_bak(int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getSimStateForSlotIndex_copy(i);
    }

    public static int getSimStateForSlotIndex_copy(int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getSimStateForSubscriber(int i) {
        if (isHookSIMInfo()) {
            return 5;
        }
        return getSimStateForSubscriber_bak(i);
    }

    public static int getSimStateForSubscriber_bak(int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getSimStateForSubscriber_copy(i);
    }

    public static int getSimStateForSubscriber_copy(int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static float getSpeed(Object obj) {
        Location locationM163;
        log("getSpeed", obj);
        try {
            if (isHook() && (locationM163 = MockLocationServiceManager.getInstance().getMockLocation()) != null) {
                return ((Float) ReflectionUtils.getFieldValue(locationM163, Location.class, "mSpeed")).floatValue();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return getSpeed_bak(obj);
    }

    public static float getSpeed_bak(Object obj) {
        log("getSpeed_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getSpeed_copy(obj);
    }

    public static float getSpeed_copy(Object obj) {
        log("getSpeed_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return 0.0f;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public static void hook(String str, ClassLoader classLoader) {
        targetPackageName = str;
        hookLocation();
        hookVMOS51Basestation();
    }

    private static void hookBasestation() {
        try {
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getNetworkOperatorName", String.class, null, ClientMockHook.class, "getNetworkOperatorName");
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getSimOperatorName", String.class, null, ClientMockHook.class, "getSimOperatorName");
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getSimOperator", String.class, null, ClientMockHook.class, "getSimOperator");
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getNetworkOperator", String.class, null, ClientMockHook.class, "getNetworkOperator");
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getSimCountryIso", String.class, null, ClientMockHook.class, "getSimCountryIso");
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getNetworkCountryIso", String.class, null, ClientMockHook.class, "getNetworkCountryIso");
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getNeighboringCellInfo", List.class, null, ClientMockHook.class, "getNeighboringCellInfo");
            int i = Build.VERSION.SDK_INT;
            if (i > 16) {
                LHooker.hookMethodAutoBackup(TelephonyManager.class, "getAllCellInfo", List.class, null, ClientMockHook.class, "getAllCellInfo");
                if (i >= 30) {
                    LHooker.hookMethodAutoBackup(TelephonyManager.class, "getAllCellInfo", List.class, new Class[]{String.class, String.class}, ClientMockHook.class, "getAllCellInfo");
                }
                LHooker.hookMethodAutoBackup(PhoneStateListener.class, "onCellInfoChanged", Void.TYPE, new Class[]{List.class}, ClientMockHook.class, "onCellInfoChanged");
            }
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getCellLocation", CellLocation.class, null, ClientMockHook.class, "getCellLocation");
            LHooker.hookMethodAutoBackup(PhoneStateListener.class, "onCellLocationChanged", Void.TYPE, new Class[]{CellLocation.class}, ClientMockHook.class, "onCellLocationChanged");
            Class cls = Integer.TYPE;
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getNetworkType", cls, null, ClientMockHook.class, "getNetworkType");
            if (i > 23) {
                LHooker.hookMethodAutoBackup(TelephonyManager.class, "getDataNetworkType", cls, null, ClientMockHook.class, "getDataNetworkType");
            }
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getPhoneType", cls, null, ClientMockHook.class, "getPhoneType");
            LHooker.hookMethodAutoBackup(TelephonyManager.class, "getCurrentPhoneType", cls, null, ClientMockHook.class, "getCurrentPhoneType");
        } catch (Throwable th) {
            th.printStackTrace();
            log(th.getMessage());
        }
    }

    private static void hookLocation() {
        try {
            LHooker.hookMethodAutoBackup(Location.class, "getLatitude", Double.TYPE, null, ClientMockHook.class, "getLatitude");
            LHooker.hookMethodAutoBackup(Location.class, "getLongitude", Double.TYPE, null, ClientMockHook.class, "getLongitude");
            Class cls = Float.TYPE;
            LHooker.hookMethodAutoBackup(Location.class, "getSpeed", cls, null, ClientMockHook.class, "getSpeed");
            LHooker.hookMethodAutoBackup(Location.class, "getAccuracy", cls, null, ClientMockHook.class, "getAccuracy");
            LHooker.hookMethodAutoBackup(Location.class, "getBearing", cls, null, ClientMockHook.class, "getBearing");
            LHooker.hookMethodAutoBackup(Location.class, "getAltitude", Double.TYPE, null, ClientMockHook.class, "getAltitude");
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    private static void hookVMOS51Basestation() {
        LHooker.hookMethodAutoBackup(TelephonyManager.class, "getAllCellInfo", List.class, null, ClientMockHook.class, "getAllCellInfo");
        LHooker.hookMethodAutoBackup(TelephonyManager.class, "getNeighboringCellInfo", List.class, null, ClientMockHook.class, "getNeighboringCellInfo");
        LHooker.hookMethodAutoBackup(TelephonyManager.class, "getCellLocation", CellLocation.class, null, ClientMockHook.class, "getCellLocation");
        if (Build.VERSION.SDK_INT >= 22) {
            LHooker.hookMethodAutoBackup(SubscriptionManager.class, "getActiveSubscriptionInfoList", List.class, null, ClientMockHook.class, "getActiveSubscriptionInfoList");
            Class cls = Integer.TYPE;
            LHooker.hookMethodAutoBackup(SubscriptionManager.class, "getActiveSubscriptionInfoCountMax", cls, null, ClientMockHook.class, "getActiveSubscriptionInfoCountMax");
            LHooker.hookMethodAutoBackup(SubscriptionManager.class, "getActiveSubscriptionInfoCount", cls, null, ClientMockHook.class, "getActiveSubscriptionInfoCount");
            LHooker.hookMethodAutoBackup(SubscriptionManager.class, "getPhoneId", cls, new Class[]{cls}, ClientMockHook.class, "getPhoneId");
            LHooker.hookMethodAutoBackup(SubscriptionManager.class, "getSimStateForSlotIndex", cls, new Class[]{cls}, ClientMockHook.class, "getSimStateForSlotIndex");
            LHooker.hookMethodAutoBackup(SubscriptionManager.class, "getSimStateForSubscriber", cls, new Class[]{cls}, ClientMockHook.class, "getSimStateForSubscriber");
            LHooker.hookMethodAutoBackup(SubscriptionManager.class, "getSimStateForSlotIdx", cls, new Class[]{cls}, ClientMockHook.class, "getSimStateForSlotIdx");
            LHooker.hookMethodAutoBackup(SubscriptionManager.class, "isActiveSubId", Boolean.TYPE, new Class[]{cls}, ClientMockHook.class, "isActiveSubId");
        }
    }

    private static void hookWifi() {
        try {
            LHooker.hookMethodAutoBackup(WifiInfo.class, "getMacAddress", String.class, null, ClientMockHook.class, "getMacAddress");
            LHooker.hookMethodAutoBackup(WifiInfo.class, "getSSID", String.class, null, ClientMockHook.class, "getSSID");
            LHooker.hookMethodAutoBackup(WifiInfo.class, "getBSSID", String.class, null, ClientMockHook.class, "getBSSID");
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    public static boolean isActiveSubId(Object obj, int i) {
        if (isHookSIMInfo()) {
            return true;
        }
        return isActiveSubId_bak(obj, i);
    }

    public static boolean isActiveSubId_bak(Object obj, int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isActiveSubId_copy(obj, i);
    }

    public static boolean isActiveSubId_copy(Object obj, int i) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean isAllowMockPackage(String str) {
        List<String> listM165 = MockLocationServiceManager.getInstance().getSafeApps();
        if (listM165 != null && !listM165.isEmpty() && !ScopedListFilter.isAllowed(listM165, str, "l")) {
            return false;
        }
        List<String> listM161 = MockLocationServiceManager.getInstance().getAllowMockPackages();
        if (listM161 == null || listM161.isEmpty()) {
            return true;
        }
        return listM161.contains(str);
    }

    private static boolean isHook() {
        boolean z = !CallingProcessUtils.isCallingShellUid() && MockLocationServiceManager.getInstance().isMocking() && isAllowMockPackage(targetPackageName);
        log("isHook: " + z, "uid: " + Binder.getCallingUid(), "pid: " + Binder.getCallingPid());
        return z;
    }

    private static boolean isHookSIMInfo() {
        return isHook() && MockLocationServiceManager.getInstance().getMockSubscriptionInfo() != null && ScopedListFilter.isAllowed(MockLocationServiceManager.getInstance().getSafeApps(), targetPackageName, "g");
    }

    private static void log(Object... objArr) {
        if (!com.kail.location.inject.utils.InjectLog.hookLogEnabled) return;
        com.kail.location.inject.utils.InjectLog.log("ClientMockHook", objArr);
    }

    public static void onCellInfoChanged(Object obj, List<CellInfo> list) {
    }

    public static void onCellLocationChanged(Object obj, CellLocation cellLocation) {
    }
}
