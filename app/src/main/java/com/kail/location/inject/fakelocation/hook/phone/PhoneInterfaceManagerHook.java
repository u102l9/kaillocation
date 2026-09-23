package com.kail.location.inject.fakelocation.hook.phone;

import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.SubscriptionInfo;
import com.kail.location.inject.fakelocation.InjectDex;
import com.kail.location.inject.utils.ScopedListFilter;
import com.kail.location.inject.utils.CallingProcessUtils;
import com.kail.location.inject.utils.CellInfoFactory;
import com.kail.location.inject.utils.ReflectionUtils;
import com.kail.location.inject.utils.MockLocationServiceManager;
import com.kail.location.inject.utils.PackageSignatureVerifier;
import com.kail.location.lib.lhooker.LHooker;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import com.kail.location.inject.fakelocation.model.CellTowerInfo;

/* JADX INFO: renamed from: ֏.֏.ހ.֏.ؠ.֏, reason: contains not printable characters */
/* JADX INFO: loaded from: /home/kail/code/tool/jadx-1.5.5/bin/classes.dex */
public class PhoneInterfaceManagerHook {

    /* JADX INFO: renamed from: ֏, reason: contains not printable characters */
    private static boolean subscriptionInfoHooked = false;

    /* JADX INFO: renamed from: ؠ, reason: contains not printable characters */
    private static ClassLoader phoneClassLoader = null;

    /* JADX INFO: renamed from: ހ, reason: contains not printable characters */
    static int transactionGetActiveSubInfoCount = -1;

    /* JADX INFO: renamed from: ށ, reason: contains not printable characters */
    static int transactionGetActiveSubInfoCountMax = -1;

    /* JADX INFO: renamed from: ނ, reason: contains not printable characters */
    static int transactionGetActiveSubscriptionInfoList = -1;

    /* JADX INFO: renamed from: ރ, reason: contains not printable characters */
    static int transactionGetPhoneId = -1;

    /* JADX INFO: renamed from: ބ, reason: contains not printable characters */
    static int transactionGetSimStateForSlotIndex = -1;

    /* JADX INFO: renamed from: ޅ, reason: contains not printable characters */
    static int transactionGetSimStateForSubscriber = -1;

    /* JADX INFO: renamed from: ކ, reason: contains not printable characters */
    static int transactionGetSimStateForSlotIdx = -1;

    /* JADX INFO: renamed from: އ, reason: contains not printable characters */
    static int transactionIsActiveSubId = -1;

    /* JADX INFO: renamed from: ވ, reason: contains not printable characters */
    static int transactionGetNetworkCountryIsoForPhone = -1;

    /* JADX INFO: renamed from: މ, reason: contains not printable characters */
    static int hookTimingCounter;

    /* JADX INFO: renamed from: ފ, reason: contains not printable characters */
    static long lastHookElapsedRealtime;

    static int checkNull(Object obj) {
        if (obj == null) {
            return -1;
        }
        return ((Integer) obj).intValue();
    }

    public static Class forName(ClassLoader classLoader, String str) {
        return ReflectionUtils.loadClass(str, true, classLoader);
    }

    public static int getActivePhoneType(Object obj) {
        log("getActivePhoneType: ", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        if (isHook()) {
            return 2;
        }
        return getActivePhoneType_bak(obj);
    }

    public static int getActivePhoneTypeForSlot(Object obj, int i) {
        log("getActivePhoneTypeForSlot: ", obj, Integer.valueOf(i));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (isHook()) {
            return 2;
        }
        return getActivePhoneTypeForSlot_bak(obj, i);
    }

    public static int getActivePhoneTypeForSlot_bak(Object obj, int i) {
        try {
            log("getActivePhoneTypeForSlot_bak: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getActivePhoneTypeForSlot_copy(obj, i);
    }

    public static int getActivePhoneTypeForSlot_copy(Object obj, int i) {
        try {
            log("getActivePhoneTypeForSlot_copy: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        return 0;
    }

    public static int getActivePhoneType_bak(Object obj) {
        try {
            log("getActivePhoneType_bak: ", obj);
            int i = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        return getActivePhoneType_copy(obj);
    }

    public static int getActivePhoneType_copy(Object obj) {
        try {
            log("getActivePhoneType_copy: ", obj);
            int i = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        return 0;
    }

    public static List<CellInfo> getAllCellInfo(Object obj, String str) {
        log("getAllCellInfo: ", obj, str, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (isHook()) {
            try {
                if (isHookCells()) {
                    System.currentTimeMillis();
                    List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
                    CellInfoFactory.setCurrentLocation(MockLocationServiceManager.getInstance().getMockLocation());
                    List<CellInfo> listM101 = CellInfoFactory.createCellInfoList(listM162);
                    if (listM101 != null && !listM101.isEmpty()) {
                        log("mockAllCellInfo: ", listM101);
                        return listM101;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return new ArrayList();
        }
        try {
            List<CellInfo> allCellInfo_bak = getAllCellInfo_bak(obj, str);
            return allCellInfo_bak == null ? new ArrayList() : allCellInfo_bak;
        } catch (Throwable th2) {
            th2.printStackTrace();
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            try {
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
                for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            try {
                StringBuffer stringBuffer7 = new StringBuffer();
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#" + obj);
                stringBuffer7.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e7) {
                e7.printStackTrace();
            }
            try {
                StringBuffer stringBuffer8 = new StringBuffer();
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#" + obj);
                stringBuffer8.toString();
                for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
                }
                for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
                }
            } catch (Exception e8) {
                e8.printStackTrace();
            }
            return new ArrayList();
        }
    }

    public static List<CellInfo> getAllCellInfo_M(Object obj) {
        log("getAllCellInfo_M: ", obj, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (isHook()) {
            try {
                if (isHookCells()) {
                    System.currentTimeMillis();
                    List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
                    CellInfoFactory.setCurrentLocation(MockLocationServiceManager.getInstance().getMockLocation());
                    List<CellInfo> listM101 = CellInfoFactory.createCellInfoList(listM162);
                    if (listM101 != null && !listM101.isEmpty()) {
                        log("mockAllCellInfo: ", listM101);
                        return listM101;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return new ArrayList();
        }
        try {
            List<CellInfo> allCellInfo_M_bak = getAllCellInfo_M_bak(obj);
            return allCellInfo_M_bak == null ? new ArrayList() : allCellInfo_M_bak;
        } catch (Throwable th2) {
            th2.printStackTrace();
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            try {
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
                for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            try {
                StringBuffer stringBuffer7 = new StringBuffer();
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#" + obj);
                stringBuffer7.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e7) {
                e7.printStackTrace();
            }
            try {
                StringBuffer stringBuffer8 = new StringBuffer();
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#" + obj);
                stringBuffer8.toString();
                for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
                }
                for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
                }
            } catch (Exception e8) {
                e8.printStackTrace();
            }
            return new ArrayList();
        }
    }

    public static List<CellInfo> getAllCellInfo_M_bak(Object obj) {
        try {
            log("getAllCellInfo_M_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getAllCellInfo_M_copy(obj);
    }

    public static List<CellInfo> getAllCellInfo_M_copy(Object obj) {
        try {
            log("getAllCellInfo_M_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static List<CellInfo> getAllCellInfo_R(Object obj, String str, String str2) {
        log("getAllCellInfo_R: ", obj, str, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (isHook()) {
            try {
                if (isHookCells()) {
                    System.currentTimeMillis();
                    List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
                    CellInfoFactory.setCurrentLocation(MockLocationServiceManager.getInstance().getMockLocation());
                    List<CellInfo> listM101 = CellInfoFactory.createCellInfoList(listM162);
                    if (listM101 != null && !listM101.isEmpty()) {
                        log("mockAllCellInfo: ", listM101);
                        return listM101;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return new ArrayList();
        }
        try {
            List<CellInfo> list = (List) ReflectionUtils.invokeMethod(obj, PhoneInterfaceManagerHook.class, "getAllCellInfo_R_bak", new Class[]{Object.class, String.class, String.class}, new Object[]{obj, str, str2});
            return list == null ? new ArrayList() : list;
        } catch (Throwable th2) {
            th2.printStackTrace();
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
                stringBuffer4.toString();
                for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                }
                for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            try {
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
                for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            return new ArrayList();
        }
    }

    public static List<CellInfo> getAllCellInfo_R_bak(Object obj, String str, String str2) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            log("getAllCellInfo_R_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i5 = hookTimingCounter;
            } else {
                int i6 = hookTimingCounter;
            }
            int i7 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getAllCellInfo_R_copy(obj, str, str2);
    }

    public static List<CellInfo> getAllCellInfo_R_copy(Object obj, String str, String str2) {
        try {
            log("getAllCellInfo_R_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static List<CellInfo> getAllCellInfo_bak(Object obj, String str) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            log("getAllCellInfo_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i9 = hookTimingCounter;
            } else {
                int i10 = hookTimingCounter;
            }
            int i11 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
            for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#" + obj);
            stringBuffer6.toString();
            for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
            }
            for (int i17 = 0; i17 < 100; i17 = i17 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        try {
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#" + obj);
            stringBuffer7.toString();
            for (int i18 = 0; i18 < 100; i18 = i18 + 1 + 1) {
            }
            for (int i19 = 0; i19 < 100; i19 = i19 + 1 + 1) {
            }
        } catch (Exception e8) {
            e8.printStackTrace();
        }
        return getAllCellInfo_copy(obj, str);
    }

    public static List<CellInfo> getAllCellInfo_copy(Object obj, String str) {
        try {
            log("getAllCellInfo_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Bundle getCellLocation(Object obj, String str) {
        int iM260;
        log("getCellLocation: ", obj, str, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (isHook()) {
            try {
                Bundle bundle = new Bundle();
                List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
                if (!isHookCells() || listM162 == null || listM162.isEmpty()) {
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
                return bundle;
            } catch (Throwable th) {
                log("getCellLocation.err: " + th.getMessage());
                th.printStackTrace();
                return null;
            }
        }
        try {
            Bundle cellLocation_bak = getCellLocation_bak(obj, str);
            log("srcCellLocation: " + cellLocation_bak);
            return cellLocation_bak;
        } catch (Throwable th2) {
            th2.printStackTrace();
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
                stringBuffer4.toString();
                for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                }
                for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            return null;
        }
    }

    public static Bundle getCellLocation_M(Object obj) {
        int iM260;
        log("getCellLocation_M: ", obj, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (isHook()) {
            try {
                Bundle bundle = new Bundle();
                List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
                if (!isHookCells() || listM162 == null || listM162.isEmpty()) {
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
                return bundle;
            } catch (Throwable th) {
                log("getCellLocation.err: " + th.getMessage());
                th.printStackTrace();
                return null;
            }
        }
        try {
            Bundle cellLocation_M_bak = getCellLocation_M_bak(obj);
            log("srcCellLocation: " + cellLocation_M_bak);
            return cellLocation_M_bak;
        } catch (Throwable th2) {
            th2.printStackTrace();
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
                stringBuffer4.toString();
                for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                }
                for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            return null;
        }
    }

    public static Bundle getCellLocation_M_bak(Object obj) {
        try {
            log("getCellLocation_M_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getCellLocation_M_copy(obj);
    }

    public static Bundle getCellLocation_M_copy(Object obj) {
        try {
            log("getCellLocation_M_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static CellIdentity getCellLocation_R(Object obj, String str, String str2) {
        log("getCellLocation_R: ", obj, str, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (isHook()) {
            try {
                List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
                if (isHookCells() && listM162 != null && !listM162.isEmpty()) {
                    CellIdentity cellIdentityM94 = CellInfoFactory.getCellIdentity(CellInfoFactory.createCellInfo(listM162.get(new SecureRandom().nextInt(Math.min(3, listM162.size())))));
                    if (cellIdentityM94 != null) {
                        return cellIdentityM94;
                    }
                    Iterator<CellTowerInfo> it = listM162.iterator();
                    while (it.hasNext()) {
                        CellIdentity cellIdentityM942 = CellInfoFactory.getCellIdentity(CellInfoFactory.createCellInfo(it.next()));
                        if (cellIdentityM942 != null) {
                            return cellIdentityM942;
                        }
                    }
                }
                return null;
            } catch (Throwable th) {
                log("getCellLocation_R.err: " + th.getMessage());
                th.printStackTrace();
                return null;
            }
        }
        try {
            CellIdentity cellIdentity = (CellIdentity) ReflectionUtils.invokeMethod(obj, PhoneInterfaceManagerHook.class, "getCellLocation_R_bak", new Class[]{Object.class, String.class, String.class}, new Object[]{obj, str, str2});
            log("srcCellLocation: " + cellIdentity);
            return cellIdentity;
        } catch (Throwable th2) {
            th2.printStackTrace();
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            try {
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
                for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            try {
                StringBuffer stringBuffer7 = new StringBuffer();
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#" + obj);
                stringBuffer7.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e7) {
                e7.printStackTrace();
            }
            try {
                StringBuffer stringBuffer8 = new StringBuffer();
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#" + obj);
                stringBuffer8.toString();
                for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
                }
                for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
                }
            } catch (Exception e8) {
                e8.printStackTrace();
            }
            return null;
        }
    }

    public static CellIdentity getCellLocation_R_bak(Object obj, String str, String str2) {
        try {
            log("getCellLocation_R_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getCellLocation_R_copy(obj, str, str2);
    }

    public static CellIdentity getCellLocation_R_copy(Object obj, String str, String str2) {
        try {
            log("getCellLocation_R_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static Bundle getCellLocation_bak(Object obj, String str) {
        try {
            log("getCellLocation_bak", obj);
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
                stringBuffer.toString();
                for (int i = 0; i < 100; i = i + 1 + 1) {
                }
                for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i7 = hookTimingCounter;
            } else {
                int i8 = hookTimingCounter;
            }
            int i9 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getCellLocation_copy(obj, str);
    }

    public static Bundle getCellLocation_copy(Object obj, String str) {
        try {
            log("getCellLocation_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static int getDataNetworkType(Object obj) {
        log("getDataNetworkType: ", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (isHook()) {
            return 4;
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#" + obj);
            stringBuffer6.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        try {
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#" + obj);
            stringBuffer7.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        return getDataNetworkType_bak(obj);
    }

    public static int getDataNetworkTypeForSubscriber(Object obj, int i, String str) {
        log("getDataNetworkTypeForSubscriber: ", obj, str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (isHook()) {
            return 4;
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#" + obj);
            stringBuffer6.toString();
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        try {
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#" + obj);
            stringBuffer7.toString();
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
            for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        return getDataNetworkTypeForSubscriber_bak(obj, i, str);
    }

    public static int getDataNetworkTypeForSubscriber_R(Object obj, int i, String str, String str2) {
        log("getDataNetworkTypeForSubscriber_R: ", obj, str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        if (isHook()) {
            return 4;
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#" + obj);
            stringBuffer6.toString();
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        try {
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#" + obj);
            stringBuffer7.toString();
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
            for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        try {
            StringBuffer stringBuffer8 = new StringBuffer();
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#" + obj);
            stringBuffer8.toString();
            for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
            }
            for (int i17 = 0; i17 < 100; i17 = i17 + 1 + 1) {
            }
        } catch (Exception e8) {
            e8.printStackTrace();
        }
        try {
            return ((Integer) ReflectionUtils.invokeMethod(obj, PhoneInterfaceManagerHook.class, "getDataNetworkTypeForSubscriber_R_bak", new Class[]{Object.class, Integer.TYPE, String.class, String.class}, new Object[]{obj, Integer.valueOf(i), str, str2})).intValue();
        } catch (Exception e9) {
            e9.printStackTrace();
            return -1;
        }
    }

    public static int getDataNetworkTypeForSubscriber_R_bak(Object obj, int i, String str, String str2) {
        try {
            log("getDataNetworkTypeForSubscriber_R_bak: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getDataNetworkTypeForSubscriber_R_copy(obj, i, str, str2);
    }

    public static int getDataNetworkTypeForSubscriber_R_copy(Object obj, int i, String str, String str2) {
        try {
            log("getDataNetworkTypeForSubscriber_R_copy: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        return 0;
    }

    public static int getDataNetworkTypeForSubscriber_bak(Object obj, int i, String str) {
        try {
            log("getDataNetworkTypeForSubscriber_bak: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        return getDataNetworkTypeForSubscriber_copy(obj, i, str);
    }

    public static int getDataNetworkTypeForSubscriber_copy(Object obj, int i, String str) {
        try {
            log("getDataNetworkTypeForSubscriber_copy: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        return 0;
    }

    public static int getDataNetworkType_bak(Object obj) {
        try {
            log("getDataNetworkType_bak: ", obj);
            int i = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getDataNetworkType_copy(obj);
    }

    public static int getDataNetworkType_copy(Object obj) {
        try {
            log("getDataNetworkType_copy: ", obj);
            int i = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return 0;
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo(Object obj, String str) {
        List<NeighboringCellInfo> listM103;
        log("getNeighboringCellInfo: ", obj, str, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (isHook()) {
            try {
                if (isHookCells() && (listM103 = CellInfoFactory.createNeighboringCellInfoList(MockLocationServiceManager.getInstance().getMockCells())) != null) {
                    if (!listM103.isEmpty()) {
                        return listM103;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return new ArrayList();
        }
        try {
            List<NeighboringCellInfo> neighboringCellInfo_bak = getNeighboringCellInfo_bak(obj, str);
            log("srcNeighboringCellInfo: ", neighboringCellInfo_bak);
            return neighboringCellInfo_bak;
        } catch (Throwable th2) {
            th2.printStackTrace();
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
                stringBuffer4.toString();
                for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                }
                for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            try {
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
                for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            return new ArrayList();
        }
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo_R(Object obj, String str, String str2) {
        List<NeighboringCellInfo> listM103;
        log("getNeighboringCellInfo_R: ", obj, str, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (isHook()) {
            try {
                if (isHookCells() && (listM103 = CellInfoFactory.createNeighboringCellInfoList(MockLocationServiceManager.getInstance().getMockCells())) != null && !listM103.isEmpty()) {
                    log("mockNeighboringCellInfo: ", listM103);
                    return listM103;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return new ArrayList();
        }
        try {
            List<NeighboringCellInfo> list = (List) ReflectionUtils.invokeMethod(obj, PhoneInterfaceManagerHook.class, "getNeighboringCellInfo", new Class[]{String.class, String.class}, new Object[]{str, str2});
            log("srcNeighboringCellInfo: ", list);
            return list;
        } catch (Throwable th2) {
            th2.printStackTrace();
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            try {
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
                for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            try {
                StringBuffer stringBuffer7 = new StringBuffer();
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#" + obj);
                stringBuffer7.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e7) {
                e7.printStackTrace();
            }
            return new ArrayList();
        }
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo_R_bak(Object obj, String str, String str2) {
        try {
            log("getNeighboringCellInfo_R_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getNeighboringCellInfo_R_copy(obj, str, str2);
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo_R_copy(Object obj, String str, String str2) {
        try {
            log("getNeighboringCellInfo_R_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo_bak(Object obj, String str) {
        try {
            log("getNeighboringCellInfo_bak", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return getNeighboringCellInfo_copy(obj, str);
    }

    public static List<NeighboringCellInfo> getNeighboringCellInfo_copy(Object obj, String str) {
        try {
            log("getNeighboringCellInfo_copy", obj);
            List<CellTowerInfo> listM162 = MockLocationServiceManager.getInstance().getMockCells();
            if (listM162 != null) {
                listM162.size();
                int i = hookTimingCounter;
            } else {
                int i2 = hookTimingCounter;
            }
            int i3 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static int getNetworkType(Object obj) {
        log("getNetworkType: ", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (isHook()) {
            return 4;
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#" + obj);
            stringBuffer6.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        try {
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#" + obj);
            stringBuffer7.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        return getNetworkType_bak(obj);
    }

    public static int getNetworkTypeForSubscriber(Object obj, int i, String str) {
        log("getNetworkTypeForSubscriber: ", obj, str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (isHook()) {
            return 4;
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#" + obj);
            stringBuffer6.toString();
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        try {
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#" + obj);
            stringBuffer7.toString();
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
            for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        return getNetworkTypeForSubscriber_bak(obj, i, str);
    }

    public static int getNetworkTypeForSubscriber_R(Object obj, int i, String str, String str2) {
        log("getNetworkTypeForSubscriber_R: ", obj, str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (isHook()) {
            return 4;
        }
        try {
            return ((Integer) ReflectionUtils.invokeMethod(obj, PhoneInterfaceManagerHook.class, "getNetworkTypeForSubscriber_R_bak", new Class[]{Object.class, Integer.TYPE, String.class, String.class}, new Object[]{obj, Integer.valueOf(i), str, str2})).intValue();
        } catch (Throwable th) {
            th.printStackTrace();
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            try {
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                }
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            try {
                StringBuffer stringBuffer7 = new StringBuffer();
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#" + obj);
                stringBuffer7.toString();
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
                for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
                }
            } catch (Exception e7) {
                e7.printStackTrace();
            }
            try {
                StringBuffer stringBuffer8 = new StringBuffer();
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#");
                stringBuffer8.append("#" + obj);
                stringBuffer8.toString();
                for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
                }
                for (int i17 = 0; i17 < 100; i17 = i17 + 1 + 1) {
                }
            } catch (Exception e8) {
                e8.printStackTrace();
            }
            try {
                StringBuffer stringBuffer9 = new StringBuffer();
                stringBuffer9.append("#");
                stringBuffer9.append("#");
                stringBuffer9.append("#");
                stringBuffer9.append("#");
                stringBuffer9.append("#");
                stringBuffer9.append("#" + obj);
                stringBuffer9.toString();
                for (int i18 = 0; i18 < 100; i18 = i18 + 1 + 1) {
                }
                for (int i19 = 0; i19 < 100; i19 = i19 + 1 + 1) {
                }
                return 16;
            } catch (Exception e9) {
                e9.printStackTrace();
                return 16;
            }
        }
    }

    public static int getNetworkTypeForSubscriber_R_bak(Object obj, int i, String str, String str2) {
        try {
            log("getNetworkTypeForSubscriber_R_bak: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        return getNetworkTypeForSubscriber_R_copy(obj, i, str, str2);
    }

    public static int getNetworkTypeForSubscriber_R_copy(Object obj, int i, String str, String str2) {
        try {
            log("getNetworkTypeForSubscriber_R_copy: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#" + obj);
            stringBuffer6.toString();
            for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
            }
            for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        return 0;
    }

    public static int getNetworkTypeForSubscriber_bak(Object obj, int i, String str) {
        try {
            log("getNetworkTypeForSubscriber_bak: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        return getNetworkTypeForSubscriber_copy(obj, i, str);
    }

    public static int getNetworkTypeForSubscriber_copy(Object obj, int i, String str) {
        try {
            log("getNetworkTypeForSubscriber_copy: ", obj);
            int i2 = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        return 0;
    }

    public static int getNetworkType_bak(Object obj) {
        try {
            log("getNetworkType_bak: ", obj);
            int i = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return getNetworkType_copy(obj);
    }

    public static int getNetworkType_copy(Object obj) {
        try {
            log("getNetworkType_copy: ", obj);
            int i = ((System.currentTimeMillis() - lastHookElapsedRealtime) > 20000L ? 1 : ((System.currentTimeMillis() - lastHookElapsedRealtime) == 20000L ? 0 : -1));
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return 0;
    }

    private static void getTransactionCode(Class cls) {
        transactionGetActiveSubInfoCount = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getActiveSubInfoCount"));
        transactionGetActiveSubInfoCountMax = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getActiveSubInfoCountMax"));
        transactionGetActiveSubscriptionInfoList = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getActiveSubscriptionInfoList"));
        transactionGetPhoneId = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getPhoneId"));
        transactionGetSimStateForSlotIndex = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getSimStateForSlotIndex"));
        transactionIsActiveSubId = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_isActiveSubId"));
        transactionGetNetworkCountryIsoForPhone = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getNetworkCountryIsoForPhone"));
        transactionGetSimStateForSubscriber = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getSimStateForSubscriber"));
        transactionGetSimStateForSlotIdx = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getSimStateForSlotIdx"));
    }

    public static void hook(ClassLoader classLoader) {
        phoneClassLoader = classLoader;
        try {
            PackageSignatureVerifier.verifyPackageSignature(InjectDex.getApplicationContext(), "com.kail.location", "com.android.phone.PhoneInterfaceManager");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getAllCellInfo", List.class, null, PhoneInterfaceManagerHook.class, "getAllCellInfo_M", "getAllCellInfo_M_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getCellLocation", Bundle.class, null, PhoneInterfaceManagerHook.class, "getCellLocation_M", "getCellLocation_M_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getAllCellInfo", List.class, new Class[]{String.class}, PhoneInterfaceManagerHook.class, "getAllCellInfo", "getAllCellInfo_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getCellLocation", Bundle.class, new Class[]{String.class}, PhoneInterfaceManagerHook.class, "getCellLocation", "getCellLocation_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getNeighboringCellInfo", List.class, new Class[]{String.class}, PhoneInterfaceManagerHook.class, "getNeighboringCellInfo", "getNeighboringCellInfo_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getAllCellInfo", List.class, new Class[]{String.class, String.class}, PhoneInterfaceManagerHook.class, "getAllCellInfo_R", "getAllCellInfo_R_bak");
            if (Build.VERSION.SDK_INT >= 28) {
                LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getCellLocation", CellIdentity.class, new Class[]{String.class, String.class}, PhoneInterfaceManagerHook.class, "getCellLocation_R", "getCellLocation_R_bak");
            }
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getNeighboringCellInfo", List.class, new Class[]{String.class, String.class}, PhoneInterfaceManagerHook.class, "getNeighboringCellInfo_R", "getNeighboringCellInfo_R_bak");
            Class clsForName = forName(classLoader, "com.android.phone.PhoneInterfaceManager");
            Class cls = Integer.TYPE;
            LHooker.hookMethodWithBackup(clsForName, "getActivePhoneTypeForSlot", cls, new Class[]{cls}, PhoneInterfaceManagerHook.class, "getActivePhoneTypeForSlot", "getActivePhoneTypeForSlot_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getActivePhoneType", cls, new Class[0], PhoneInterfaceManagerHook.class, "getActivePhoneType", "getActivePhoneType_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getNetworkType", cls, new Class[0], PhoneInterfaceManagerHook.class, "getNetworkType", "getNetworkType_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getDataNetworkType", cls, new Class[0], PhoneInterfaceManagerHook.class, "getDataNetworkType", "getDataNetworkType_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getNetworkTypeForSubscriber", cls, new Class[]{cls, String.class}, PhoneInterfaceManagerHook.class, "getNetworkTypeForSubscriber", "getNetworkTypeForSubscriber_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getDataNetworkTypeForSubscriber", cls, new Class[]{cls, String.class}, PhoneInterfaceManagerHook.class, "getDataNetworkTypeForSubscriber", "getDataNetworkTypeForSubscriber_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getNetworkTypeForSubscriber", cls, new Class[]{cls, String.class, String.class}, PhoneInterfaceManagerHook.class, "getNetworkTypeForSubscriber_R", "getNetworkTypeForSubscriber_R_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.phone.PhoneInterfaceManager"), "getDataNetworkTypeForSubscriber", cls, new Class[]{cls, String.class, String.class}, PhoneInterfaceManagerHook.class, "getDataNetworkTypeForSubscriber_R", "getDataNetworkTypeForSubscriber_R_bak");
        } catch (RuntimeException unused) {
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (isHookSIMInfo()) {
            hookSIMInfo(classLoader);
        }
    }

    public static void hookSIMInfo(ClassLoader classLoader) {
        if (subscriptionInfoHooked) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                getTransactionCode(forName(classLoader, "com.android.internal.telephony.ISub$Stub"));
                Class clsForName = forName(classLoader, "com.android.internal.telephony.ISub$Stub");
                Class cls = Boolean.TYPE;
                Class cls2 = Integer.TYPE;
                LHooker.hookMethodWithBackup(clsForName, "onTransact", cls, new Class[]{cls2, Parcel.class, Parcel.class, cls2}, PhoneInterfaceManagerHook.class, "onTransact", "onTransact_bak");
            } else {
                getTransactionCode(forName(classLoader, "com.android.internal.telephony.ISub$Stub"));
                Class clsForName2 = forName(classLoader, "com.android.internal.telephony.ISub$Stub");
                Class cls3 = Boolean.TYPE;
                Class cls4 = Integer.TYPE;
                LHooker.hookMethodWithBackup(clsForName2, "onTransact", cls3, new Class[]{cls4, Parcel.class, Parcel.class, cls4}, PhoneInterfaceManagerHook.class, "onTransact", "onTransact_bak");
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        subscriptionInfoHooked = true;
    }

    public static boolean isAllowMockPackage(String str, String str2) {
        List<String> listM165 = MockLocationServiceManager.getInstance().getSafeApps();
        if (listM165 != null && !listM165.isEmpty() && !ScopedListFilter.isAllowed(listM165, str, str2)) {
            return false;
        }
        List<String> listM161 = MockLocationServiceManager.getInstance().getAllowMockPackages();
        if (listM161 == null || listM161.isEmpty()) {
            return true;
        }
        return listM161.contains(str);
    }

    private static boolean isHook() {
        boolean z = !CallingProcessUtils.isCallingSystemUid() && MockLocationServiceManager.getInstance().isMocking() && (isAllowMockPackage(CallingProcessUtils.getPackageNameForPidOrUid(Binder.getCallingPid(), Binder.getCallingUid()), "a") || isAllowMockPackage(CallingProcessUtils.getPackageNameForPidOrUid(Binder.getCallingPid(), Binder.getCallingUid()), "e"));
        log("isHook: " + z, "uid: " + Binder.getCallingUid(), "pid: " + Binder.getCallingPid());
        if (z && !subscriptionInfoHooked && MockLocationServiceManager.getInstance().getMockSubscriptionInfo() != null) {
            hookSIMInfo(phoneClassLoader);
        }
        return z;
    }

    private static boolean isHookCells() {
        return isAllowMockPackage(CallingProcessUtils.getPackageNameForPidOrUid(Binder.getCallingPid(), Binder.getCallingUid()), "e");
    }

    private static boolean isHookSIMInfo() {
        return isHook() && MockLocationServiceManager.getInstance().getMockSubscriptionInfo() != null && isAllowMockPackage(CallingProcessUtils.getPackageNameForPidOrUid(Binder.getCallingPid(), Binder.getCallingUid()), "g");
    }

    private static void log(Object... objArr) {
        if (!com.kail.location.inject.utils.InjectLog.hookLogEnabled) return;
        com.kail.location.inject.utils.InjectLog.log("PhoneInterfaceManagerHook", objArr);
    }

    public static boolean onTransact(Object obj, int i, Parcel parcel, Parcel parcel2, int i2) {
        SubscriptionInfo subscriptionInfo;
        List<SubscriptionInfo> listM164;
        List<SubscriptionInfo> listM1642;
        List<SubscriptionInfo> listM1643;
        log("onTransact", obj, Integer.valueOf(i), parcel, parcel2, Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (obj != null && parcel != null && parcel2 != null) {
            try {
                if (i == transactionIsActiveSubId && isHookSIMInfo()) {
                    parcel.enforceInterface("com.android.internal.telephony.ISub");
                    parcel.readInt();
                    parcel2.writeNoException();
                    parcel2.writeInt(1);
                    return true;
                }
                if (i == transactionGetSimStateForSlotIndex && isHookSIMInfo()) {
                    parcel.enforceInterface("com.android.internal.telephony.ISub");
                    parcel.readInt();
                    parcel2.writeNoException();
                    parcel2.writeInt(5);
                    return true;
                }
                if (i == transactionGetSimStateForSubscriber && isHookSIMInfo()) {
                    parcel.enforceInterface("com.android.internal.telephony.ISub");
                    parcel.readInt();
                    parcel2.writeNoException();
                    parcel2.writeInt(5);
                    return true;
                }
                if (i == transactionGetSimStateForSlotIdx && isHookSIMInfo()) {
                    parcel.enforceInterface("com.android.internal.telephony.ISub");
                    parcel.readInt();
                    parcel2.writeNoException();
                    parcel2.writeInt(5);
                    return true;
                }
                if (i == transactionGetActiveSubInfoCount && isHookSIMInfo() && (listM1643 = MockLocationServiceManager.getInstance().getMockSubscriptionInfo()) != null) {
                    parcel.enforceInterface("com.android.internal.telephony.ISub");
                    parcel.readString();
                    int size = listM1643.size();
                    parcel2.writeNoException();
                    parcel2.writeInt(size);
                    return true;
                }
                if (i == transactionGetActiveSubscriptionInfoList && Build.VERSION.SDK_INT >= 22 && isHookSIMInfo() && (listM1642 = MockLocationServiceManager.getInstance().getMockSubscriptionInfo()) != null) {
                    parcel.enforceInterface("com.android.internal.telephony.ISub");
                    parcel.readString();
                    ArrayList arrayList = new ArrayList();
                    arrayList.addAll(listM1642);
                    parcel2.writeNoException();
                    parcel2.writeTypedList(arrayList);
                    return true;
                }
                if (i == transactionGetActiveSubInfoCountMax && isHookSIMInfo() && (listM164 = MockLocationServiceManager.getInstance().getMockSubscriptionInfo()) != null) {
                    parcel.enforceInterface("com.android.internal.telephony.ISub");
                    int size2 = listM164.size();
                    parcel2.writeNoException();
                    parcel2.writeInt(size2);
                    return true;
                }
                if (i == transactionGetPhoneId && isHookSIMInfo()) {
                    parcel.enforceInterface("com.android.internal.telephony.ISub");
                    parcel.readInt();
                    parcel2.writeNoException();
                    parcel2.writeInt(0);
                    return true;
                }
                if (i != transactionGetNetworkCountryIsoForPhone || !isHookSIMInfo()) {
                    return onTransact_bak(obj, i, parcel, parcel2, i2);
                }
                String countryIso = "";
                List<SubscriptionInfo> listM1644 = MockLocationServiceManager.getInstance().getMockSubscriptionInfo();
                if (listM1644.size() > 0 && (subscriptionInfo = listM1644.get(0)) != null) {
                    countryIso = subscriptionInfo.getCountryIso();
                }
                parcel.enforceInterface("com.android.internal.telephony.ISub");
                parcel.readInt();
                parcel2.writeNoException();
                parcel2.writeString(countryIso);
                return true;
            } catch (Throwable th) {
                th.printStackTrace();
                try {
                    StringBuffer stringBuffer3 = new StringBuffer();
                    stringBuffer3.append("#");
                    stringBuffer3.append("#");
                    stringBuffer3.append("#");
                    stringBuffer3.append("#");
                    stringBuffer3.append("#");
                    stringBuffer3.append("#" + obj);
                    stringBuffer3.toString();
                    for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                    }
                    for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
                try {
                    StringBuffer stringBuffer4 = new StringBuffer();
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#" + obj);
                    stringBuffer4.toString();
                    for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                    }
                    for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                    }
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                try {
                    StringBuffer stringBuffer5 = new StringBuffer();
                    stringBuffer5.append("#");
                    stringBuffer5.append("#");
                    stringBuffer5.append("#");
                    stringBuffer5.append("#");
                    stringBuffer5.append("#");
                    stringBuffer5.append("#" + obj);
                    stringBuffer5.toString();
                    for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                    }
                    for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                    }
                } catch (Exception e5) {
                    e5.printStackTrace();
                }
            }
        }
        return false;
    }

    public static boolean onTransact_bak(Object obj, int i, Parcel parcel, Parcel parcel2, int i2) {
        if (obj == null) {
            return false;
        }
        log("onTransact_bak", obj);
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
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        new Random().nextBoolean();
        try {
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#" + obj);
            stringBuffer7.toString();
            for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
            }
            for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        return onTransact_copy(obj, i, parcel, parcel2, i2);
    }

    public static boolean onTransact_copy(Object obj, int i, Parcel parcel, Parcel parcel2, int i2) {
        try {
            log("onTransact_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return false;
    }
}
