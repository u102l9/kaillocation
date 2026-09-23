package com.kail.location.inject.fakelocation.hook.system;

import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.telephony.CellInfo;
import com.kail.location.inject.utils.ScopedListFilter;
import com.kail.location.inject.utils.CallingProcessUtils;
import com.kail.location.inject.utils.CellInfoFactory;
import com.kail.location.inject.utils.ReflectionUtils;
import com.kail.location.inject.utils.MockLocationHookManager;
import com.kail.location.lib.lhooker.LHooker;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kail.location.inject.fakelocation.model.CellTowerInfo;

public class TelephonyRegistryHook {

    private static Class<?> iPhoneStateListenerClass = null;

    private static boolean telephonyRegistryHooked = false;

    static Map<String, WeakReference<Object>> phoneStateListeners = Collections.synchronizedMap(new HashMap());

    static class ListenCallbackProxy implements InvocationHandler {

        final /* synthetic */ String packageName;

        final /* synthetic */ Object originalListener;

        ListenCallbackProxy(String str, Object obj) {
            this.packageName = str;
            this.originalListener = obj;
        }

        @Override
        public Object invoke(Object obj, Method method, Object[] objArr) {
            List<CellInfo> arrayList;
            int iM260;
            StringBuilder sb;
            try {
                if (TelephonyRegistryHook.isHook() && TelephonyRegistryHook.isAllowMockPackage(this.packageName)) {
                    String str = "";
                    if (objArr != null) {
                        String string = "";
                        for (int i = 0; i < objArr.length; i++) {
                            if (objArr[i] == null) {
                                sb = new StringBuilder();
                                sb.append(string);
                                sb.append("null null, ");
                            } else {
                                sb = new StringBuilder();
                                sb.append(string);
                                sb.append("");
                                sb.append(objArr[i].getClass().getName());
                                sb.append(" ");
                                sb.append(objArr[i]);
                                sb.append(", ");
                            }
                            string = sb.toString();
                        }
                        str = string;
                    }
                    TelephonyRegistryHook.log("listen." + method.getName() + " Parameter: " + str);
                    if ("onCellLocationChanged".equals(method.getName())) {
                        Bundle bundle = new Bundle();
                        List<CellTowerInfo> mockCells = MockLocationHookManager.getMockCells();
                        if (!TelephonyRegistryHook.isHookCells(this.packageName) || mockCells == null || mockCells.isEmpty()) {
                            iM260 = Integer.MAX_VALUE;
                            bundle.putInt("cid", Integer.MAX_VALUE);
                            bundle.putInt("lac", Integer.MAX_VALUE);
                            bundle.putInt("psc", Integer.MAX_VALUE);
                            bundle.putInt("baseStationId", Integer.MAX_VALUE);
                            bundle.putInt("systemId", Integer.MAX_VALUE);
                        } else {
                            CellTowerInfo c0058 = mockCells.get(new SecureRandom().nextInt(Math.min(3, mockCells.size())));
                            bundle.putInt("cid", (int) c0058.getCellId());
                            bundle.putInt("lac", c0058.getLac());
                            bundle.putInt("psc", c0058.getPsc());
                            bundle.putInt("baseStationId", (int) c0058.getCellId());
                            bundle.putInt("systemId", c0058.getMnc());
                            iM260 = c0058.getLac();
                        }
                        bundle.putInt("networkId", iM260);
                        Location mockLocation = MockLocationHookManager.getMockLocation();
                        if (mockLocation != null) {
                            bundle.putInt("baseStationLatitude", (int) (mockLocation.getLatitude() * 14400.0d));
                            bundle.putInt("baseStationLongitude", (int) (mockLocation.getLongitude() * 14400.0d));
                        }
                        bundle.putBoolean("empty", false);
                        bundle.putBoolean("emptyParcel", false);
                        bundle.putInt("mFlags", 1536);
                        bundle.putBoolean("parcelled", false);
                        bundle.putInt("size", 0);
                        return method.invoke(this.originalListener, bundle);
                    }
                    if ("onCellInfoChanged".equals(method.getName())) {
                        if (TelephonyRegistryHook.isHookCells(this.packageName)) {
                            CellInfoFactory.setCurrentLocation(MockLocationHookManager.getMockLocation());
                            arrayList = CellInfoFactory.createCellInfoList(MockLocationHookManager.getMockCells());
                        } else {
                            arrayList = null;
                        }
                        if (arrayList == null) {
                            arrayList = new ArrayList<>();
                        }
                        TelephonyRegistryHook.log("listen.onCellInfoChanged: cellInfos.size = " + arrayList.size());
                        return method.invoke(this.originalListener, arrayList);
                    }
                    if ("onSignalStrengthsChanged".equals(method.getName()) || "onSignalStrengthChanged".equals(method.getName())) {
                        return null;
                    }
                }
                return method.invoke(this.originalListener, objArr);
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        }
    }

    static class ListenForSubscriberCallbackProxy implements InvocationHandler {

        final /* synthetic */ String packageName;

        final /* synthetic */ Object originalListener;

        ListenForSubscriberCallbackProxy(String str, Object obj) {
            this.packageName = str;
            this.originalListener = obj;
        }

        @Override
        public Object invoke(Object obj, Method method, Object[] objArr) {
            List<CellInfo> arrayList;
            int iM260;
            StringBuilder sb;
            try {
                if (TelephonyRegistryHook.isHook() && TelephonyRegistryHook.isAllowMockPackage(this.packageName)) {
                    String str = "";
                    if (objArr != null) {
                        String string = "";
                        for (int i = 0; i < objArr.length; i++) {
                            if (objArr[i] == null) {
                                sb = new StringBuilder();
                                sb.append(string);
                                sb.append("null null, ");
                            } else {
                                sb = new StringBuilder();
                                sb.append(string);
                                sb.append("");
                                sb.append(objArr[i].getClass().getName());
                                sb.append(" ");
                                sb.append(objArr[i]);
                                sb.append(", ");
                            }
                            string = sb.toString();
                        }
                        str = string;
                    }
                    TelephonyRegistryHook.log("listenForSubscriber." + method.getName() + " Parameter: " + str);
                    if ("onCellLocationChanged".equals(method.getName())) {
                        Bundle bundle = new Bundle();
                        List<CellTowerInfo> mockCells = MockLocationHookManager.getMockCells();
                        if (!TelephonyRegistryHook.isHookCells(this.packageName) || mockCells == null || mockCells.isEmpty()) {
                            iM260 = Integer.MAX_VALUE;
                            bundle.putInt("cid", Integer.MAX_VALUE);
                            bundle.putInt("lac", Integer.MAX_VALUE);
                            bundle.putInt("psc", Integer.MAX_VALUE);
                            bundle.putInt("baseStationId", Integer.MAX_VALUE);
                            bundle.putInt("systemId", Integer.MAX_VALUE);
                        } else {
                            CellTowerInfo c0058 = mockCells.get(new SecureRandom().nextInt(Math.min(3, mockCells.size())));
                            bundle.putInt("cid", (int) c0058.getCellId());
                            bundle.putInt("lac", c0058.getLac());
                            bundle.putInt("psc", c0058.getPsc());
                            bundle.putInt("baseStationId", (int) c0058.getCellId());
                            bundle.putInt("systemId", c0058.getMnc());
                            iM260 = c0058.getLac();
                        }
                        bundle.putInt("networkId", iM260);
                        Location mockLocation = MockLocationHookManager.getMockLocation();
                        if (mockLocation != null) {
                            bundle.putInt("baseStationLatitude", (int) (mockLocation.getLatitude() * 14400.0d));
                            bundle.putInt("baseStationLongitude", (int) (mockLocation.getLongitude() * 14400.0d));
                        }
                        bundle.putBoolean("empty", false);
                        bundle.putBoolean("emptyParcel", false);
                        bundle.putInt("mFlags", 1536);
                        bundle.putBoolean("parcelled", false);
                        bundle.putInt("size", 0);
                        return method.invoke(this.originalListener, bundle);
                    }
                    if ("onCellInfoChanged".equals(method.getName())) {
                        if (TelephonyRegistryHook.isHookCells(this.packageName)) {
                            CellInfoFactory.setCurrentLocation(MockLocationHookManager.getMockLocation());
                            arrayList = CellInfoFactory.createCellInfoList(MockLocationHookManager.getMockCells());
                        } else {
                            arrayList = null;
                        }
                        if (arrayList == null) {
                            arrayList = new ArrayList<>();
                        }
                        TelephonyRegistryHook.log("listenForSubscriber.onCellInfoChanged: cellInfos.size = " + arrayList.size());
                        return method.invoke(this.originalListener, arrayList);
                    }
                }
                return method.invoke(this.originalListener, objArr);
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        }
    }

    static class ListenWithEventListCallbackProxy implements InvocationHandler {

        final /* synthetic */ String packageName;

        final /* synthetic */ Object originalListener;

        ListenWithEventListCallbackProxy(String str, Object obj) {
            this.packageName = str;
            this.originalListener = obj;
        }

        @Override
        public Object invoke(Object obj, Method method, Object[] objArr) {
            List<CellInfo> arrayList;
            CellInfo cellInfoM96;
            StringBuilder sb;
            try {
                if (TelephonyRegistryHook.isHook() && TelephonyRegistryHook.isAllowMockPackage(this.packageName)) {
                    String str = "";
                    if (objArr != null) {
                        String string = "";
                        for (int i = 0; i < objArr.length; i++) {
                            if (objArr[i] == null) {
                                sb = new StringBuilder();
                                sb.append(string);
                                sb.append("null null, ");
                            } else {
                                sb = new StringBuilder();
                                sb.append(string);
                                sb.append("");
                                sb.append(objArr[i].getClass().getName());
                                sb.append(" ");
                                sb.append(objArr[i]);
                                sb.append(", ");
                            }
                            string = sb.toString();
                        }
                        str = string;
                    }
                    TelephonyRegistryHook.log("listenWithEventList_S." + method.getName() + " Parameter: " + str);
                    if ("onCellLocationChanged".equals(method.getName())) {
                        List<CellTowerInfo> mockCells = MockLocationHookManager.getMockCells();
                        if (!TelephonyRegistryHook.isHookCells(this.packageName) || mockCells == null || mockCells.isEmpty()) {
                            CellTowerInfo c0058 = new CellTowerInfo();
                            Location mockLocation = MockLocationHookManager.getMockLocation();
                            if (mockLocation != null) {
                                c0058.setLatitude(mockLocation.getLatitude());
                                c0058.setLongitude(mockLocation.getLongitude());
                            }
                            cellInfoM96 = CellInfoFactory.createCellInfo(c0058);
                        } else {
                            cellInfoM96 = CellInfoFactory.createCellInfo(mockCells.get(new SecureRandom().nextInt(Math.min(3, mockCells.size()))));
                        }
                        Object objM94 = CellInfoFactory.getCellIdentity(cellInfoM96);
                        objArr[0] = objM94;
                        return method.invoke(this.originalListener, objM94);
                    }
                    if ("onCellInfoChanged".equals(method.getName())) {
                        if (TelephonyRegistryHook.isHookCells(this.packageName)) {
                            CellInfoFactory.setCurrentLocation(MockLocationHookManager.getMockLocation());
                            arrayList = CellInfoFactory.createCellInfoList(MockLocationHookManager.getMockCells());
                        } else {
                            arrayList = null;
                        }
                        if (arrayList == null) {
                            arrayList = new ArrayList<>();
                        }
                        TelephonyRegistryHook.log("listenWithEventList_S.onCellInfoChanged: cellInfos.size = " + arrayList.size());
                        return method.invoke(this.originalListener, arrayList);
                    }
                }
                return method.invoke(this.originalListener, objArr);
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        }
    }

    static class ListenForSubscriberRCallbackProxy implements InvocationHandler {

        final /* synthetic */ String packageName;

        final /* synthetic */ Object originalListener;

        ListenForSubscriberRCallbackProxy(String str, Object obj) {
            this.packageName = str;
            this.originalListener = obj;
        }

        @Override
        public Object invoke(Object obj, Method method, Object[] objArr) {
            List<CellInfo> arrayList;
            CellInfo cellInfoM96;
            StringBuilder sb;
            try {
                if (TelephonyRegistryHook.isHook() && TelephonyRegistryHook.isAllowMockPackage(this.packageName)) {
                    String str = "";
                    if (objArr != null) {
                        String string = "";
                        for (int i = 0; i < objArr.length; i++) {
                            if (objArr[i] == null) {
                                sb = new StringBuilder();
                                sb.append(string);
                                sb.append("null null, ");
                            } else {
                                sb = new StringBuilder();
                                sb.append(string);
                                sb.append("");
                                sb.append(objArr[i].getClass().getName());
                                sb.append(" ");
                                sb.append(objArr[i]);
                                sb.append(", ");
                            }
                            string = sb.toString();
                        }
                        str = string;
                    }
                    TelephonyRegistryHook.log("listenForSubscriber." + method.getName() + " Parameter: " + str);
                    if ("onCellLocationChanged".equals(method.getName())) {
                        List<CellTowerInfo> mockCells = MockLocationHookManager.getMockCells();
                        if (!TelephonyRegistryHook.isHookCells(this.packageName) || mockCells == null || mockCells.isEmpty()) {
                            CellTowerInfo c0058 = new CellTowerInfo();
                            Location mockLocation = MockLocationHookManager.getMockLocation();
                            if (mockLocation != null) {
                                c0058.setLatitude(mockLocation.getLatitude());
                                c0058.setLongitude(mockLocation.getLongitude());
                            }
                            cellInfoM96 = CellInfoFactory.createCellInfo(c0058);
                        } else {
                            cellInfoM96 = CellInfoFactory.createCellInfo(mockCells.get(new SecureRandom().nextInt(Math.min(3, mockCells.size()))));
                        }
                        Object objM94 = CellInfoFactory.getCellIdentity(cellInfoM96);
                        objArr[0] = objM94;
                        return method.invoke(this.originalListener, objM94);
                    }
                    if ("onCellInfoChanged".equals(method.getName())) {
                        if (TelephonyRegistryHook.isHookCells(this.packageName)) {
                            CellInfoFactory.setCurrentLocation(MockLocationHookManager.getMockLocation());
                            arrayList = CellInfoFactory.createCellInfoList(MockLocationHookManager.getMockCells());
                        } else {
                            arrayList = null;
                        }
                        if (arrayList == null) {
                            arrayList = new ArrayList<>();
                        }
                        TelephonyRegistryHook.log("listenForSubscriber.onCellInfoChanged: cellInfos.size = " + arrayList.size());
                        return method.invoke(this.originalListener, arrayList);
                    }
                }
                return method.invoke(this.originalListener, objArr);
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        }
    }

    private static void addIPhoneStateListener(Object obj, int i) {
        if (obj == null) {
            return;
        }
        try {
            String string = obj.toString();
            if (i == 0) {
                phoneStateListeners.remove(string);
            } else {
                phoneStateListeners.remove(string);
                phoneStateListeners.put(string, new WeakReference<>(obj));
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static void callbackMockCellLocation(Location location, List<CellTowerInfo> list) {
        String[] strArr;
        int i;
        Object objM101;
        Class<?> cls;
        Class[] clsArr;
        int iM260;
        try {
            String[] strArr2 = (String[]) phoneStateListeners.keySet().toArray(new String[0]);
            int i2 = 0;
            for (int length = strArr2.length; i2 < length; length = i) {
                String str = strArr2[i2];
                WeakReference<Object> weakReference = phoneStateListeners.get(str);
                if (weakReference == null) {
                    strArr = strArr2;
                    i = length;
                } else {
                    Object obj = weakReference.get();
                    if (obj == null) {
                        phoneStateListeners.remove(str);
                        strArr = strArr2;
                        i = length;
                    } else {
                        try {
                            Bundle bundle = new Bundle();
                            if (list == null || list.isEmpty()) {
                                strArr = strArr2;
                                i = length;
                                iM260 = Integer.MAX_VALUE;
                                bundle.putInt("cid", Integer.MAX_VALUE);
                                bundle.putInt("lac", Integer.MAX_VALUE);
                                bundle.putInt("psc", Integer.MAX_VALUE);
                                bundle.putInt("baseStationId", Integer.MAX_VALUE);
                                bundle.putInt("systemId", Integer.MAX_VALUE);
                            } else {
                                CellTowerInfo c0058 = list.get(new SecureRandom().nextInt(Math.min(3, list.size())));
                                strArr = strArr2;
                                i = length;
                                iM260 = Integer.MAX_VALUE;
                                try {
                                    bundle.putInt("cid", (int) c0058.getCellId());
                                    bundle.putInt("lac", c0058.getLac());
                                    bundle.putInt("psc", new SecureRandom().nextInt(256));
                                    bundle.putInt("baseStationId", (int) c0058.getCellId());
                                    bundle.putInt("systemId", c0058.getMnc());
                                    iM260 = c0058.getLac();
                                } catch (Throwable th) {
                                    th.printStackTrace();
                                }
                            }
                            bundle.putInt("networkId", iM260);
                            if (location != null) {
                                bundle.putInt("baseStationLatitude", (int) (location.getLatitude() * 14400.0d));
                                bundle.putInt("baseStationLongitude", (int) (location.getLongitude() * 14400.0d));
                            }
                            bundle.putBoolean("empty", false);
                            bundle.putBoolean("emptyParcel", false);
                            bundle.putInt("mFlags", 1536);
                            bundle.putBoolean("parcelled", false);
                            bundle.putInt("size", 0);
                            ReflectionUtils.invokeMethod(obj, iPhoneStateListenerClass, "onCellLocationChanged", new Class[]{Bundle.class}, new Object[]{bundle});
                        } catch (InvocationTargetException e2) {
                            strArr = strArr2;
                            i = length;
                        } catch (Throwable th2) {
                            strArr = strArr2;
                            i = length;
                        }
                        try {
                            CellInfoFactory.setCurrentLocation(MockLocationHookManager.getMockLocation());
                            objM101 = CellInfoFactory.createCellInfoList(list);
                            if (objM101 == null) {
                                objM101 = new ArrayList();
                            }
                            cls = iPhoneStateListenerClass;
                            clsArr = new Class[1];
                        } catch (Throwable th3) {
                            // swallowed
                            return;
                        }
                        try {
                            clsArr[0] = List.class;
                            ReflectionUtils.invokeMethod(obj, cls, "onCellInfoChanged", clsArr, new Object[]{objM101});
                        } catch (Throwable th4) {
                            th4.printStackTrace();
                        }
                    }
                }
                i2++;
                strArr2 = strArr;
            }
        } catch (Throwable th5) {
            th5.printStackTrace();
        }
    }

    public static Class forName(ClassLoader classLoader, String str) {
        return ReflectionUtils.loadClass(str, true, classLoader);
    }

    public static void hook(ClassLoader classLoader) {
        if (telephonyRegistryHooked) {
            return;
        }
        iPhoneStateListenerClass = ReflectionUtils.loadClass("com.android.internal.telephony.IPhoneStateListener", true, classLoader);
        try {
            Class clsForName = forName(classLoader, "com.android.server.TelephonyRegistry");
            Class cls = Void.TYPE;
            Class cls2 = Integer.TYPE;
            Class cls3 = Boolean.TYPE;
            LHooker.hookMethodWithBackup(clsForName, "listen", cls, new Class[]{String.class, forName(classLoader, "com.android.internal.telephony.IPhoneStateListener"), cls2, cls3}, TelephonyRegistryHook.class, "listen", "listen_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.server.TelephonyRegistry"), "listenForSubscriber", Void.TYPE, new Class[]{cls2, String.class, forName(classLoader, "com.android.internal.telephony.IPhoneStateListener"), cls2, cls3}, TelephonyRegistryHook.class, "listenForSubscriber", "listenForSubscriber_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.server.TelephonyRegistry"), "listenForSubscriber", Void.TYPE, new Class[]{cls2, String.class, String.class, forName(classLoader, "com.android.internal.telephony.IPhoneStateListener"), cls2, cls3}, TelephonyRegistryHook.class, "listenForSubscriber_R", "listenForSubscriber_R_bak");
            LHooker.hookMethodWithBackup(forName(classLoader, "com.android.server.TelephonyRegistry"), "listenWithEventList", Void.TYPE, new Class[]{cls2, String.class, String.class, forName(classLoader, "com.android.internal.telephony.IPhoneStateListener"), int[].class, cls3}, TelephonyRegistryHook.class, "listenWithEventList_S", "listenWithEventList_S_bak");
            telephonyRegistryHooked = true;
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static boolean isAllowMockPackage(String str) {
        return ScopedListFilter.isAllowed(MockLocationHookManager.getSafeApps(), str, "a") || ScopedListFilter.isAllowed(MockLocationHookManager.getSafeApps(), str, "e");
    }

    public static boolean isHook() {
        boolean z = MockLocationHookManager.isMocking() && !CallingProcessUtils.isCallingSystemUid();
        log("isHook: " + z, "uid: " + Binder.getCallingUid(), "pid: " + Binder.getCallingPid());
        return z;
    }

    public static boolean isHookCells(String str) {
        return ScopedListFilter.isAllowed(MockLocationHookManager.getSafeApps(), str, "e");
    }

    public static void listen(Object obj, String str, Object obj2, int i, boolean z) {
        log("listen: ", obj, str, obj2, Integer.valueOf(i), Boolean.valueOf(z));
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
        if (isHook() && isAllowMockPackage(str)) {
            try {
                listen_bak(obj, str, Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{iPhoneStateListenerClass}, new ListenCallbackProxy(str, obj2)), i, z);
                return;
            } catch (Throwable th) {
                th.printStackTrace();
                return;
            }
        }
        try {
            listen_bak(obj, str, obj2, i, z);
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
    }

    public static void listenForSubscriber(Object obj, int i, String str, Object obj2, int i2, boolean z) {
        log("listenForSubscriber: ", obj, Integer.valueOf(i), str, obj2, Integer.valueOf(i2), Boolean.valueOf(z));
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
        if (isHook() && isAllowMockPackage(str)) {
            try {
                listenForSubscriber_bak(obj, i, str, Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{iPhoneStateListenerClass}, new ListenForSubscriberCallbackProxy(str, obj2)), i2, z);
                return;
            } catch (Throwable th) {
                th.printStackTrace();
                return;
            }
        }
        try {
            listenForSubscriber_bak(obj, i, str, obj2, i2, z);
        } catch (Throwable th2) {
            th2.printStackTrace();
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
    }

    public static void listenForSubscriber_R(Object obj, int i, String str, String str2, Object obj2, int i2, boolean z) {
        log("listenForSubscriber_R: ", obj, Integer.valueOf(i), str, obj2, Integer.valueOf(i2), Boolean.valueOf(z));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isHook() && isAllowMockPackage(str)) {
            try {
                listenForSubscriber_R_bak(obj, i, str, str2, Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{iPhoneStateListenerClass}, new ListenForSubscriberRCallbackProxy(str, obj2)), i2, z);
                return;
            } catch (Throwable th) {
                th.printStackTrace();
                return;
            }
        }
        try {
            listenForSubscriber_R_bak(obj, i, str, str2, obj2, i2, z);
        } catch (Throwable th2) {
            th2.printStackTrace();
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
    }

    public static void listenForSubscriber_R_bak(Object obj, int i, String str, String str2, Object obj2, int i2, boolean z) {
        try {
            log("listenForSubscriber_R_bak", obj);
            listenForSubscriber_R_copy(obj, i, str, str2, obj2, i2, z);
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
    }

    public static void listenForSubscriber_R_copy(Object obj, int i, String str, String str2, Object obj2, int i2, boolean z) {
        try {
            log("listenForSubscriber_R_copy", obj);
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
    }

    public static void listenForSubscriber_bak(Object obj, int i, String str, Object obj2, int i2, boolean z) {
        try {
            log("listenForSubscriber_bak", obj);
            listenForSubscriber_copy(obj, i, str, obj2, i2, z);
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
    }

    public static void listenForSubscriber_copy(Object obj, int i, String str, Object obj2, int i2, boolean z) {
        try {
            log("listenForSubscriber_copy", obj);
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
    }

    public static void listenWithEventList_S(Object obj, int i, String str, String str2, Object obj2, int[] iArr, boolean z) {
        log("listenWithEventList_S: ", obj, Integer.valueOf(i), str, obj2, iArr, Boolean.valueOf(z));
        if (isHook() && isAllowMockPackage(str)) {
            try {
                listenWithEventList_S_bak(obj, i, str, str2, Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{iPhoneStateListenerClass}, new ListenWithEventListCallbackProxy(str, obj2)), iArr, z);
                return;
            } catch (Throwable th) {
                th.printStackTrace();
                return;
            }
        }
        try {
            listenWithEventList_S_bak(obj, i, str, str2, obj2, iArr, z);
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
    }

    public static void listenWithEventList_S_bak(Object obj, int i, String str, String str2, Object obj2, int[] iArr, boolean z) {
        log("listenWithEventList_S_bak: ", obj, Integer.valueOf(i), str, obj2, iArr, Boolean.valueOf(z));
        listenWithEventList_S_copy(obj, i, str, str2, obj2, iArr, z);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void listenWithEventList_S_copy(Object obj, int i, String str, String str2, Object obj2, int[] iArr, boolean z) {
        log("listenWithEventList_S_copy: ", obj, Integer.valueOf(i), str, obj2, iArr, Boolean.valueOf(z));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void listen_bak(Object obj, String str, Object obj2, int i, boolean z) {
        try {
            log("listen_bak", obj);
            listen_copy(obj, str, obj2, i, z);
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
    }

    public static void listen_copy(Object obj, String str, Object obj2, int i, boolean z) {
        try {
            log("listen_copy", obj);
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
    }

    public static void log(Object... objArr) {
        if (!com.kail.location.inject.utils.InjectLog.hookLogEnabled) return;
        com.kail.location.inject.utils.InjectLog.log("TelephonyRegistryHook", objArr);
    }

    public static void notifyCellInfo(Object obj, List<CellInfo> list) {
        log("notifyCellInfo: ", obj);
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
        if (isHook()) {
            try {
                CellInfoFactory.setCurrentLocation(MockLocationHookManager.getMockLocation());
                List<CellInfo> listM101 = CellInfoFactory.createCellInfoList(MockLocationHookManager.getMockCells());
                if (listM101 == null || listM101.isEmpty()) {
                    listM101 = Collections.emptyList();
                }
                notifyCellInfo_bak(obj, listM101);
                return;
            } catch (Throwable th) {
                th.printStackTrace();
                return;
            }
        }
        try {
            notifyCellInfo_bak(obj, list);
        } catch (Throwable th2) {
            th2.printStackTrace();
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
    }

    public static void notifyCellInfoForSubscriber(Object obj, int i, List<CellInfo> list) {
        log("notifyCellInfoForSubscriber: ", obj, Integer.valueOf(i));
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
        if (isHook()) {
            try {
                CellInfoFactory.setCurrentLocation(MockLocationHookManager.getMockLocation());
                List<CellInfo> listM101 = CellInfoFactory.createCellInfoList(MockLocationHookManager.getMockCells());
                if (listM101 == null || listM101.isEmpty()) {
                    listM101 = Collections.emptyList();
                }
                notifyCellInfoForSubscriber_bak(obj, i, listM101);
                return;
            } catch (Throwable th) {
                th.printStackTrace();
                return;
            }
        }
        try {
            notifyCellInfoForSubscriber_bak(obj, i, list);
        } catch (Throwable th2) {
            th2.printStackTrace();
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
    }

    public static void notifyCellInfoForSubscriber_bak(Object obj, int i, List<CellInfo> list) {
        try {
            log("notifyCellInfoForSubscriber_bak", obj);
            notifyCellInfoForSubscriber_copy(obj, i, list);
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
    }

    public static void notifyCellInfoForSubscriber_copy(Object obj, int i, List<CellInfo> list) {
        try {
            log("notifyCellInfoForSubscriber_copy", obj);
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
    }

    public static void notifyCellInfo_bak(Object obj, List<CellInfo> list) {
        try {
            log("notifyCellInfo_bak", obj, list);
            notifyCellInfo_copy(obj, list);
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
    }

    public static void notifyCellInfo_copy(Object obj, List<CellInfo> list) {
        try {
            log("notifyCellInfo_copy", obj);
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
    }

    public static void notifyCellLocation(Object obj, Bundle bundle) {
        int iM260;
        log("notifyCellLocation: ", obj, bundle);
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
            if (isHook()) {
                Bundle bundle2 = new Bundle();
                List<CellTowerInfo> mockCells = MockLocationHookManager.getMockCells();
                if (mockCells == null || mockCells.isEmpty()) {
                    iM260 = Integer.MAX_VALUE;
                    bundle2.putInt("cid", Integer.MAX_VALUE);
                    bundle2.putInt("lac", Integer.MAX_VALUE);
                    bundle2.putInt("psc", Integer.MAX_VALUE);
                    bundle2.putInt("baseStationId", Integer.MAX_VALUE);
                    bundle2.putInt("systemId", Integer.MAX_VALUE);
                } else {
                    CellTowerInfo c0058 = mockCells.get(new SecureRandom().nextInt(Math.min(3, mockCells.size())));
                    bundle2.putInt("cid", (int) c0058.getCellId());
                    bundle2.putInt("lac", c0058.getLac());
                    bundle2.putInt("psc", c0058.getPsc());
                    bundle2.putInt("baseStationId", (int) c0058.getCellId());
                    bundle2.putInt("systemId", c0058.getMnc());
                    iM260 = c0058.getLac();
                }
                bundle2.putInt("networkId", iM260);
                Location mockLocation = MockLocationHookManager.getMockLocation();
                if (mockLocation != null) {
                    bundle2.putInt("baseStationLatitude", (int) (mockLocation.getLatitude() * 14400.0d));
                    bundle2.putInt("baseStationLongitude", (int) (mockLocation.getLongitude() * 14400.0d));
                }
                bundle2.putBoolean("empty", false);
                bundle2.putBoolean("emptyParcel", false);
                bundle2.putInt("mFlags", 1536);
                bundle2.putBoolean("parcelled", false);
                bundle2.putInt("size", 0);
                try {
                    notifyCellLocation_bak(obj, bundle2);
                    return;
                } catch (Throwable th) {
                    th.printStackTrace();
                    return;
                }
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        try {
            notifyCellLocation_bak(obj, bundle);
        } catch (Throwable th3) {
            th3.printStackTrace();
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
    }

    /* JADX WARN: Can't wrap try/catch for region: R(25:0|2|69|3|4|(1:6)|75|7|(1:9)|(7:61|12|13|(1:15)|76|16|(1:18))|71|21|(11:23|24|(1:30)(1:28)|29|31|(1:33)|34|35|63|36|79)(11:44|65|48|73|52|53|(1:55)|77|(1:57)|78|80)|47|65|48|73|52|53|(0)|77|(0)|78|80|(1:(0))) */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x0167, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x0168, code lost:
    
        r0.printStackTrace();
     */
    /* JADX WARN: Removed duplicated region for block: B:55:0x0198 A[LOOP:4: B:54:0x0196->B:55:0x0198, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:57:0x019e A[LOOP:5: B:56:0x019c->B:57:0x019e, LOOP_END] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static void notifyCellLocationForSubscriber(java.lang.Object r16, int r17, android.os.Bundle r18) {
        /*
            Method dump skipped, instruction units count: 423
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kail.location.inject.fakelocation.hook.system.TelephonyRegistryHook.notifyCellLocationForSubscriber(java.lang.Object, int, android.os.Bundle):void");
    }

    public static void notifyCellLocationForSubscriber_bak(Object obj, int i, Bundle bundle) {
        try {
            log("notifyCellLocationForSubscriber_bak", obj);
            notifyCellLocationForSubscriber_copy(obj, i, bundle);
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
    }

    public static void notifyCellLocationForSubscriber_copy(Object obj, int i, Bundle bundle) {
        try {
            log("notifyCellLocationForSubscriber_copy", obj);
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
    }

    public static void notifyCellLocation_bak(Object obj, Bundle bundle) {
        try {
            log("notifyCellLocation_bak", obj);
            notifyCellLocation_copy(obj, bundle);
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
    }

    public static void notifyCellLocation_copy(Object obj, Bundle bundle) {
        try {
            log("notifyCellLocation_copy", obj);
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
    }

    public static void onCellInfoChanged(Object obj, List<CellInfo> list) {
        try {
            log("onCellInfoChanged", obj);
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
                CellInfoFactory.setCurrentLocation(MockLocationHookManager.getMockLocation());
                list = CellInfoFactory.createCellInfoList(MockLocationHookManager.getMockCells());
                if (list == null) {
                    list = new ArrayList<>();
                }
            }
            onCellInfoChanged_bak(obj, list);
        } catch (Throwable th) {
            th.printStackTrace();
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
    }

    public static void onCellInfoChanged_bak(Object obj, List<CellInfo> list) {
        try {
            log("onCellInfoChanged_bak", obj);
            onCellInfoChanged_copy(obj, list);
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
    }

    public static void onCellInfoChanged_copy(Object obj, List<CellInfo> list) {
        try {
            log("onCellInfoChanged_copy", obj);
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
    }

    public static void onCellLocationChanged(Object obj, Bundle bundle) {
        int iM260;
        try {
            log("onCellLocationChanged", obj);
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
            if (isHook()) {
                bundle = new Bundle();
                List<CellTowerInfo> mockCells = MockLocationHookManager.getMockCells();
                if (mockCells == null || mockCells.isEmpty()) {
                    iM260 = Integer.MAX_VALUE;
                    bundle.putInt("cid", Integer.MAX_VALUE);
                    bundle.putInt("lac", Integer.MAX_VALUE);
                    bundle.putInt("psc", Integer.MAX_VALUE);
                    bundle.putInt("baseStationId", Integer.MAX_VALUE);
                    bundle.putInt("systemId", Integer.MAX_VALUE);
                } else {
                    CellTowerInfo c0058 = mockCells.get(new SecureRandom().nextInt(Math.min(3, mockCells.size())));
                    bundle.putInt("cid", (int) c0058.getCellId());
                    bundle.putInt("lac", c0058.getLac());
                    bundle.putInt("psc", c0058.getPsc());
                    bundle.putInt("baseStationId", (int) c0058.getCellId());
                    bundle.putInt("systemId", c0058.getMnc());
                    iM260 = c0058.getLac();
                }
                bundle.putInt("networkId", iM260);
                Location mockLocation = MockLocationHookManager.getMockLocation();
                if (mockLocation != null) {
                    bundle.putInt("baseStationLatitude", (int) (mockLocation.getLatitude() * 14400.0d));
                    bundle.putInt("baseStationLongitude", (int) (mockLocation.getLongitude() * 14400.0d));
                }
                bundle.putBoolean("empty", false);
                bundle.putBoolean("emptyParcel", false);
                bundle.putInt("mFlags", 1536);
                bundle.putBoolean("parcelled", false);
                bundle.putInt("size", 0);
            }
            onCellLocationChanged_bak(obj, bundle);
        } catch (Throwable th) {
            th.printStackTrace();
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
    }

    public static void onCellLocationChanged_bak(Object obj, Bundle bundle) {
        try {
            log("onCellLocationChanged_bak", obj);
            onCellLocationChanged_copy(obj, bundle);
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
    }

    public static void onCellLocationChanged_copy(Object obj, Bundle bundle) {
        try {
            log("onCellLocationChanged_copy", obj);
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
    }
}
