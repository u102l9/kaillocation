package com.kail.location.inject.fakelocation.hook.app;

import android.os.Environment;
import android.os.FileObserver;
import android.text.TextUtils;
import com.kail.location.inject.utils.ReflectionUtils;
import com.kail.location.inject.utils.HideRootServiceManager;
import com.kail.location.inject.utils.AntiDetectionServiceManager;
import com.kail.location.lib.lhooker.LHooker;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ, reason: contains not printable characters */
/* JADX INFO: loaded from: /home/kail/code/tool/jadx-1.5.5/bin/classes.dex */
public class RootHideHook {

    /* JADX INFO: renamed from: ֏, reason: contains not printable characters */
    public static final String fileClassName = File.class.getName();

    /* JADX INFO: renamed from: ؠ, reason: contains not printable characters */
    public static List<String> hiddenFakelocArtifacts = new HiddenFakelocArtifacts();

    /* JADX INFO: renamed from: ހ, reason: contains not printable characters */
    private static final HashMap<String, String> rootPathRedirects = new RootPathRedirectMap();

    /* JADX INFO: renamed from: ށ, reason: contains not printable characters */
    private static final HashMap<String, String> rootFileNameRedirects = new RootFileNameRedirectMap();

    /* JADX INFO: renamed from: ނ, reason: contains not printable characters */
    private static final HashMap<String, String> rootCommandRedirects = new RootCommandRedirectMap();

    /* JADX INFO: renamed from: ރ, reason: contains not printable characters */
    private static final HashMap<String, String> antiDetectionPathRedirects = new AntiDetectionPathRedirectMap();

    /* JADX INFO: renamed from: ބ, reason: contains not printable characters */
    private static final HashMap<String, String> antiDetectionFileNameRedirects = new AntiDetectionFileNameRedirectMap();

    /* JADX INFO: renamed from: ޅ, reason: contains not printable characters */
    private static final HashMap<String, String> antiDetectionCommandRedirects = new AntiDetectionCommandRedirectMap();

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ$֏, reason: contains not printable characters */
    static class HiddenFakelocArtifacts extends ArrayList<String> {
        HiddenFakelocArtifacts() {
            // 框架自身目录（Java File 钩子 + 原生 libc 钩子共用这份名单）。
            add("/data/kail-loc");
            add("/data/local/kail-lib");
            add("/data/system/kail-loc");
            // 注入器 / dex / 原生库。用"不带版本号、不带扩展名"的片段匹配，
            // 才能覆盖 liblhooker64.so、libfakeloc_apphook.so、
            // libantidetect64.so、libkail_native_hook.so 等实际落盘命名。
            add("kail_inject");
            add("liblhooker");
            add("libfakeloc");
            add("libantidetect");
            add("libStepSensor");
            add("libkail_native_hook");
        }
    }

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ$ؠ, reason: contains not printable characters */
    static class RootPathRedirectMap extends HashMap<String, String> {
        RootPathRedirectMap() {
            put("/system/bin", "/_system/bin");
            put("/system/xbin", "/_system/xbin");
            put("/system/sbin", "/_system/sbin");
            put("/system/sd/xbin", "/_system/sd/xbin");
            put("/system/usr/we-need-root", "/_system/usr/we-need-root");
            put("/vendor/bin", "/_vendor/bin");
            put("/vendor/xbin", "/_vendor/xbin");
            put("/sbin", "/_sbin");
            put("/su/bin", "/_system");
            put("/data/local", "/data/local/local");
            put("/persist/magisk", "/persist/_magi");
            put("/data/data/com.topjohnwu.magisk", "/_data/data/com.topjohnwu.magisk");
            put("/sdcard/MagiskManager", "/_sdcard/MagiskManager");
        }
    }

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ$ހ, reason: contains not printable characters */
    static class RootFileNameRedirectMap extends HashMap<String, String> {
        RootFileNameRedirectMap() {
            put("su", "fs");
            put("Superuser.apk", "_Superuser.apk");
            put("Kinguser.apk", "_Kinguser.apk");
            put("maps", "status");
            put("XposedBridge.jar", "_XposedBridge.jar");
            put("99SuperSUDaemon", "fs");
            put("daemonsu", "fs");
        }
    }

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ$ށ, reason: contains not printable characters */
    static class RootCommandRedirectMap extends HashMap<String, String> {
        RootCommandRedirectMap() {
            put("su", "fs");
            put("sh", "ps");
            put("which", "log");
            put("ls", "ps");
            put("l1", "ps");
            put("find", "ps");
            put("99SuperSUDaemon", "fs");
            put("daemonsu", "fs");
        }
    }

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ$ނ, reason: contains not printable characters */
    static class AntiDetectionPathRedirectMap extends HashMap<String, String> {
        AntiDetectionPathRedirectMap() {
            put("/sdcard/Android/data", "/sdcard/" + Environment.DIRECTORY_DOCUMENTS + "");
            put("/mnt/sdcard/Android/data", "/mnt/sdcard/" + Environment.DIRECTORY_DOCUMENTS + "");
            put("/mnt/user/0/primary/Android/data", "/mnt/user/0/primary/" + Environment.DIRECTORY_DOCUMENTS + "");
            put("/storage/emulated/0/Android/data", "/storage/emulated/0/" + Environment.DIRECTORY_DOCUMENTS + "");
            put("/storage/sdcard/Android/data", "/storage/sdcard/" + Environment.DIRECTORY_DOCUMENTS + "");
            put("/data/kail-loc", "/_system");
            put("_/data/kail-loc", "/data/kail-loc");
            put("/data/data/com.topjohnwu.magisk", "/_data/data/com.topjohnwu.magisk");
            put("/sdcard/MagiskManager", "/_sdcard/MagiskManager");
        }
    }

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ$ރ, reason: contains not printable characters */
    static class AntiDetectionFileNameRedirectMap extends HashMap<String, String> {
        AntiDetectionFileNameRedirectMap() {
            put("maps", "status");
            put("su", "fs");
            put("XposedBridge.jar", "_XposedBridge.jar");
            put("_/data/kail-loc", "/data/kail-loc");
            Iterator<String> it = RootHideHook.hiddenFakelocArtifacts.iterator();
            while (it.hasNext()) {
                put(it.next(), "_null");
            }
        }
    }

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ$ބ, reason: contains not printable characters */
    static class AntiDetectionCommandRedirectMap extends HashMap<String, String> {
        AntiDetectionCommandRedirectMap() {
            put("sh", "ps");
            put("su", "fs");
            put("ls", "ps");
            put("l1", "ps");
            put("cat", "ls");
            put("pm", "ps");
            put("find", "ps");
            put("99SuperSUDaemon", "fs");
            put("daemonsu", "fs");
        }
    }

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ހ$ޅ, reason: contains not printable characters */
    static class RelocatingOutputStream extends OutputStream {

        /* JADX INFO: renamed from: ֏, reason: contains not printable characters */
        ArrayList<Integer> targetOutputStream = new ArrayList<>();

        /* JADX INFO: renamed from: ؠ, reason: contains not printable characters */
        final /* synthetic */ OutputStream commandBuffer;

        RelocatingOutputStream(OutputStream outputStream) {
            this.commandBuffer = outputStream;
        }

        @Override // java.io.OutputStream, java.io.Flushable
        public void flush() throws IOException {
            flushCommandBuffer();
            this.commandBuffer.flush();
        }

        @Override // java.io.OutputStream
        public void write(int i) {
            appendCommandByte(i);
        }

        /* JADX INFO: renamed from: ֏, reason: contains not printable characters */
        void appendCommandByte(int i) {
            synchronized (this.targetOutputStream) {
                this.targetOutputStream.add(Integer.valueOf(i));
                flushCommandBuffer();
            }
        }

        /* JADX INFO: renamed from: ؠ, reason: contains not printable characters */
        void clearCommandBuffer() {
            synchronized (this.targetOutputStream) {
                this.targetOutputStream.clear();
            }
        }

        /* JADX INFO: renamed from: ހ, reason: contains not printable characters */
        void flushCommandBuffer() {
            synchronized (this.targetOutputStream) {
                int size = this.targetOutputStream.size();
                if (size <= 0) {
                    return;
                }
                byte[] bArr = new byte[size];
                for (int i = 0; i < size; i++) {
                    bArr[i] = (byte) this.targetOutputStream.get(i).intValue();
                }
                String str = new String(bArr);
                if (str.endsWith("\n")) {
                    try {
                        writeRelocatedCommand(relocateCommand(str));
                    } catch (java.io.IOException ignored) {
                    }
                    clearCommandBuffer();
                }
            }
        }

        /* JADX INFO: renamed from: ށ, reason: contains not printable characters */
        String relocateCommand(String str) {
            RootHideHook.log("relocatePath: " + str);
            boolean zM117 = HideRootServiceManager.getInstance().isHideRootEnabled();
            boolean z = AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled();
            if (!zM117 && !z) {
                return str;
            }
            if (zM117) {
                Iterator it = RootHideHook.rootCommandRedirects.keySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    String str2 = (String) it.next();
                    if (str.startsWith(str2 + " ")) {
                        str = str.replaceFirst(str2, (String) RootHideHook.rootCommandRedirects.get(str2));
                    } else if (str.equals(str2)) {
                        str = str.replace(str2, (CharSequence) RootHideHook.rootCommandRedirects.get(str2));
                        break;
                    }
                }
                Iterator it2 = RootHideHook.rootPathRedirects.keySet().iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    String str3 = (String) it2.next();
                    if (str.contains(str3)) {
                        str = str.replace(str3, (CharSequence) RootHideHook.rootPathRedirects.get(str3));
                        break;
                    }
                }
                Iterator it3 = RootHideHook.rootFileNameRedirects.keySet().iterator();
                while (true) {
                    if (!it3.hasNext()) {
                        break;
                    }
                    String str4 = (String) it3.next();
                    if (str.contains(str4)) {
                        str = str.replace(str4, (CharSequence) RootHideHook.rootFileNameRedirects.get(str4));
                        break;
                    }
                }
            }
            if (z) {
                Iterator it4 = RootHideHook.antiDetectionCommandRedirects.keySet().iterator();
                while (true) {
                    if (!it4.hasNext()) {
                        break;
                    }
                    String str5 = (String) it4.next();
                    if (str.contains(str5 + " ")) {
                        str = str.replace(str5 + " ", ((String) RootHideHook.antiDetectionCommandRedirects.get(str5)) + " ");
                    } else if (str.equals(str5)) {
                        str = str.replace(str5, (CharSequence) RootHideHook.antiDetectionCommandRedirects.get(str5));
                        break;
                    }
                }
                Iterator it5 = RootHideHook.antiDetectionPathRedirects.keySet().iterator();
                while (true) {
                    if (!it5.hasNext()) {
                        break;
                    }
                    String str6 = (String) it5.next();
                    if (str.contains(str6)) {
                        str = str.replace(str6, (CharSequence) RootHideHook.antiDetectionPathRedirects.get(str6));
                        break;
                    }
                }
                Iterator it6 = RootHideHook.antiDetectionFileNameRedirects.keySet().iterator();
                while (true) {
                    if (!it6.hasNext()) {
                        break;
                    }
                    String str7 = (String) it6.next();
                    if (str.contains(str7)) {
                        str = str.replace(str7, (CharSequence) RootHideHook.antiDetectionFileNameRedirects.get(str7));
                        break;
                    }
                }
            }
            RootHideHook.log("relocatePath.new: " + str);
            return str;
        }

        /* JADX INFO: renamed from: ނ, reason: contains not printable characters */
        void writeRelocatedCommand(String str) throws IOException {
            for (byte b : str.getBytes()) {
                this.commandBuffer.write(b);
            }
        }
    }

    public static void File(Object obj, String str) {
        log("File", obj, "pathname:" + str);
        try {
            if (!TextUtils.isEmpty(str)) {
                if (HideRootServiceManager.getInstance().isHideRootEnabled()) {
                    Iterator<String> it = rootPathRedirects.keySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String next = it.next();
                        if (str.startsWith(next)) {
                            str = str.replaceFirst(next, rootPathRedirects.get(next));
                            log("File", obj, "pathname_new:" + str);
                            break;
                        }
                    }
                    Iterator<String> it2 = rootFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        String next2 = it2.next();
                        if (str.endsWith("/" + next2)) {
                            str = str.substring(0, str.length() - next2.length()) + rootFileNameRedirects.get(next2);
                            log("File", obj, "pathname_new:" + str);
                            break;
                        }
                    }
                }
                if (AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled() && !str.contains(AppProcessHook.currentPackageName)) {
                    Iterator<String> it3 = antiDetectionPathRedirects.keySet().iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        String next3 = it3.next();
                        if (str.startsWith(next3)) {
                            str = str.replaceFirst(next3, antiDetectionPathRedirects.get(next3));
                            log("File", obj, "pathname_new:" + str);
                            break;
                        }
                    }
                    Iterator<String> it4 = antiDetectionFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it4.hasNext()) {
                            break;
                        }
                        String next4 = it4.next();
                        if (str.endsWith("/" + next4)) {
                            str = str.substring(0, str.length() - next4.length()) + antiDetectionFileNameRedirects.get(next4);
                            log("File", obj, "pathname_new:" + str);
                            break;
                        }
                    }
                }
            }
            File_bak(obj, str);
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:61:0x02ba A[PHI: r0
      0x02ba: PHI (r0v3 java.lang.String) = (r0v2 java.lang.String), (r0v2 java.lang.String), (r0v21 java.lang.String), (r0v21 java.lang.String) binds: [B:31:0x0177, B:33:0x0181, B:54:0x0264, B:102:0x02ba] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static void File2(Object obj, String parent, String child) {
        String child2 = child;
        log("File2", obj, "parent:" + parent, "child:" + child2);
        try {
            // 参照已实现的 File3（File parent, String child）还原：File(String parent, String child)。
            if (AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled()) {
                if (!TextUtils.isEmpty(child2)) {
                    Iterator<String> it = antiDetectionFileNameRedirects.keySet().iterator();
                    while (it.hasNext()) {
                        String next = it.next();
                        if (child2.equals(next)) {
                            child2 = antiDetectionFileNameRedirects.get(next);
                            log("File2", obj, "parent:" + parent, "child_new:" + child2);
                            break;
                        }
                    }
                }
                if (parent != null) {
                    String path = parent;
                    if (!path.contains(AppProcessHook.currentPackageName) && !("" + child2).contains(AppProcessHook.currentPackageName)) {
                        for (String key : antiDetectionPathRedirects.keySet()) {
                            if (path.startsWith(key)) {
                                String newParent = path.replaceFirst(key, (String) antiDetectionPathRedirects.get(key));
                                log("File2", obj, "parent_new:" + newParent, "child:" + child2);
                                File2_bak(obj, newParent, child2);
                                return;
                            }
                            String joined = path + (path.endsWith("/") ? "" : "/") + child2;
                            if (joined.startsWith(key)) {
                                String newParent = joined.replaceFirst(key, (String) antiDetectionPathRedirects.get(key)).replace(child2, "");
                                log("File2", obj, "parent_new:" + newParent, "child:" + child2);
                                File2_bak(obj, newParent, child2);
                                return;
                            }
                        }
                    }
                }
            }
            if (HideRootServiceManager.getInstance().isHideRootEnabled()) {
                if (!TextUtils.isEmpty(child2)) {
                    for (String key : rootFileNameRedirects.keySet()) {
                        if (child2.equals(key)) {
                            child2 = (String) rootFileNameRedirects.get(key);
                            log("File2", obj, "parent:" + parent, "child_new:" + child2);
                            break;
                        }
                    }
                }
                if (parent != null) {
                    for (String key : rootPathRedirects.keySet()) {
                        if (parent.startsWith(key)) {
                            String newParent = parent.replaceFirst(key, (String) rootPathRedirects.get(key));
                            log("File2", obj, "parent_new:" + newParent, "child:" + child2);
                            File2_bak(obj, newParent, child2);
                            return;
                        }
                        String joined = parent + (parent.endsWith("/") ? "" : "/") + child2;
                        if (joined.startsWith(key)) {
                            String newParent = joined.replaceFirst(key, (String) rootPathRedirects.get(key)).replace(child2, "");
                            log("File2", obj, "parent_new:" + newParent, "child:" + child2);
                            File2_bak(obj, newParent, child2);
                            return;
                        }
                    }
                }
            }
            File2_bak(obj, parent, child2);
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    public static void File2_bak(Object obj, String str, String str2) {
        log("File2_bak", obj, "parent:" + str, "child:" + str2);
        File2_copy(obj, str, str2);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
    }

    public static void File2_copy(Object obj, String str, String str2) {
        log("File2_copy", obj, "parent:" + str, "child:" + str2);
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
    }

    public static void File3(Object obj, File file, String str) {
        String str2 = str;
        char c = 0;
        char c2 = 1;
        log("File3", obj, "parent:" + file, "child:" + str2);
        try {
            if (AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled()) {
                if (!TextUtils.isEmpty(str)) {
                    Iterator<String> it = antiDetectionFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String next = it.next();
                        if (str2.equals(next)) {
                            str2 = antiDetectionFileNameRedirects.get(next);
                            log("File3", obj, "parent:" + file, "child_new:" + str2);
                            break;
                        }
                    }
                }
                if (file != null) {
                    String path = file.getPath();
                    if (!path.contains(AppProcessHook.currentPackageName)) {
                        if (!("" + str2).contains(AppProcessHook.currentPackageName)) {
                            Iterator<String> it2 = antiDetectionPathRedirects.keySet().iterator();
                            while (true) {
                                if (!it2.hasNext()) {
                                    break;
                                }
                                String next2 = it2.next();
                                if (path.startsWith(next2)) {
                                    String strReplaceFirst = path.replaceFirst(next2, antiDetectionPathRedirects.get(next2));
                                    Object[] objArr = new Object[4];
                                    objArr[c] = "File3";
                                    objArr[c2] = obj;
                                    objArr[2] = "parent_new:" + strReplaceFirst;
                                    objArr[3] = "child:" + str2;
                                    log(objArr);
                                    File3_bak(obj, new File(strReplaceFirst), str2);
                                    return;
                                }
                                StringBuilder sb = new StringBuilder();
                                sb.append(path);
                                sb.append(path.endsWith("/") ? "" : "/");
                                sb.append(str2);
                                String string = sb.toString();
                                if (string.startsWith(next2)) {
                                    String strReplaceAll = string.replaceFirst(next2, antiDetectionPathRedirects.get(next2)).replaceAll(str2, "");
                                    log("File3", obj, "parent_new:" + strReplaceAll, "child:" + str2);
                                    File3_bak(obj, new File(strReplaceAll), str2);
                                    break;
                                }
                                c = 0;
                                c2 = 1;
                            }
                        }
                    }
                }
            }
            if (HideRootServiceManager.getInstance().isHideRootEnabled()) {
                if (!TextUtils.isEmpty(str2)) {
                    Iterator<String> it3 = rootFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        String next3 = it3.next();
                        if (str2.equals(next3)) {
                            str2 = rootFileNameRedirects.get(next3);
                            log("File3", obj, "parent:" + file, "child_new:" + str2);
                            break;
                        }
                    }
                }
                if (file != null) {
                    String path2 = file.getPath();
                    for (String str3 : rootPathRedirects.keySet()) {
                        if (path2.startsWith(str3)) {
                            String strReplaceFirst2 = path2.replaceFirst(str3, rootPathRedirects.get(str3));
                            log("File3", obj, "parent_new:" + strReplaceFirst2, "child:" + str2);
                            File3_bak(obj, new File(strReplaceFirst2), str2);
                            return;
                        }
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(path2);
                        sb2.append(path2.endsWith("/") ? "" : "/");
                        sb2.append(str2);
                        String string2 = sb2.toString();
                        if (string2.startsWith(str3)) {
                            String strReplaceAll2 = string2.replaceFirst(str3, rootPathRedirects.get(str3)).replaceAll(str2, "");
                            log("File3", obj, "parent_new:" + strReplaceAll2, "child:" + str2);
                            File3_bak(obj, new File(strReplaceAll2), str2);
                            return;
                        }
                    }
                }
            }
            File3_bak(obj, file, str2);
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    public static void File3_bak(Object obj, File file, String str) {
        log("File3_bak", obj, "parent:" + file, "child:" + str);
        File3_copy(obj, file, str);
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
    }

    public static void File3_copy(Object obj, File file, String str) {
        log("File3_copy", obj, "parent:" + file, "child:" + str);
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

    public static void File4(Object obj, URI uri) {
        URI uri2;
        URI uri3;
        StringBuilder sb = new StringBuilder();
        sb.append("uri:");
        URI uri4 = uri;
        sb.append(uri4);
        log("File4", obj, sb.toString());
        String path = uri.getPath();
        try {
        if (!TextUtils.isEmpty(path)) {
            if (HideRootServiceManager.getInstance().isHideRootEnabled()) {
                Iterator<String> it = rootPathRedirects.keySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        uri3 = uri4;
                        break;
                    }
                    String next = it.next();
                    if (path.startsWith(next)) {
                        path = path.replaceFirst(next, rootPathRedirects.get(next));
                        uri3 = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), uri.getFragment());
                        break;
                    }
                }
                Iterator<String> it2 = rootFileNameRedirects.keySet().iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        uri4 = uri3;
                        break;
                    }
                    String next2 = it2.next();
                    if (path.endsWith("/" + next2)) {
                        path = path.substring(0, path.length() - next2.length()) + rootFileNameRedirects.get(next2);
                        uri4 = new URI(uri3.getScheme(), uri3.getUserInfo(), uri3.getHost(), uri3.getPort(), path, uri3.getQuery(), uri3.getFragment());
                        break;
                    }
                }
            }
            if (AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled() && !path.contains(AppProcessHook.currentPackageName)) {
                Iterator<String> it3 = antiDetectionPathRedirects.keySet().iterator();
                while (true) {
                    if (!it3.hasNext()) {
                        uri2 = uri4;
                        break;
                    }
                    String next3 = it3.next();
                    if (path.startsWith(next3)) {
                        path = path.replaceFirst(next3, antiDetectionPathRedirects.get(next3));
                        uri2 = new URI(uri4.getScheme(), uri4.getUserInfo(), uri4.getHost(), uri4.getPort(), path, uri4.getQuery(), uri4.getFragment());
                        break;
                    }
                }
                Iterator<String> it4 = antiDetectionFileNameRedirects.keySet().iterator();
                while (true) {
                    if (!it4.hasNext()) {
                        uri4 = uri2;
                        break;
                    }
                    String next4 = it4.next();
                    if (path.endsWith("/" + next4)) {
                        uri4 = new URI(uri2.getScheme(), uri2.getUserInfo(), uri2.getHost(), uri2.getPort(), path.substring(0, path.length() - next4.length()) + antiDetectionFileNameRedirects.get(next4), uri2.getQuery(), uri2.getFragment());
                        break;
                    }
                }
            }
        }
        } catch (java.net.URISyntaxException ignored) {
        }
        File4_bak(obj, uri4);
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
    }

    public static void File4_bak(Object obj, URI uri) {
        log("File4_bak", obj, "uri:" + uri);
        File4_copy(obj, uri);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
    }

    public static void File4_copy(Object obj, URI uri) {
        log("File4_copy", obj, "uri:" + uri);
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

    public static void FileObserver(Object obj, String str, int i) {
        log("FileObserver", obj, "path:" + str, "mask:" + i);
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
            if (!TextUtils.isEmpty(str)) {
                if (HideRootServiceManager.getInstance().isHideRootEnabled()) {
                    Iterator<String> it = rootPathRedirects.keySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String next = it.next();
                        if (str.startsWith(next)) {
                            str = str.replaceFirst(next, rootPathRedirects.get(next));
                            break;
                        }
                    }
                    Iterator<String> it2 = rootFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        String next2 = it2.next();
                        if (str.endsWith("/" + next2)) {
                            str = str.substring(0, str.length() - next2.length()) + rootFileNameRedirects.get(next2);
                            break;
                        }
                    }
                }
                if (AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled() && !str.contains(AppProcessHook.currentPackageName)) {
                    Iterator<String> it3 = antiDetectionPathRedirects.keySet().iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        String next3 = it3.next();
                        if (str.startsWith(next3)) {
                            str = str.replaceFirst(next3, antiDetectionPathRedirects.get(next3));
                            break;
                        }
                    }
                    Iterator<String> it4 = antiDetectionFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it4.hasNext()) {
                            break;
                        }
                        String next4 = it4.next();
                        if (str.endsWith("/" + next4)) {
                            str = str.substring(0, str.length() - next4.length()) + antiDetectionFileNameRedirects.get(next4);
                            break;
                        }
                    }
                }
            }
            FileObserver_bak(obj, str, i);
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
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    public static void FileObserver_bak(Object obj, String str, int i) {
        log("FileObserver_bak", obj, "path:" + str, "mask:" + i);
        FileObserver_copy(obj, str, i);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
    }

    public static void FileObserver_copy(Object obj, String str, int i) {
        log("FileObserver_copy", obj, "path:" + str, "mask:" + i);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
    }

    public static void FileObserver_onEvent(Object obj, int i, String str) {
        log("FileObserver_onEvent", obj, "event:" + i, "path:" + str);
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
            if (!TextUtils.isEmpty(str)) {
                if (HideRootServiceManager.getInstance().isHideRootEnabled()) {
                    Iterator<String> it = rootPathRedirects.keySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String next = it.next();
                        if (str.startsWith(next)) {
                            str = str.replaceFirst(next, rootPathRedirects.get(next));
                            break;
                        }
                    }
                    Iterator<String> it2 = rootFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        String next2 = it2.next();
                        if (str.endsWith("/" + next2)) {
                            str = str.substring(0, str.length() - next2.length()) + rootFileNameRedirects.get(next2);
                            break;
                        }
                    }
                }
                if (AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled() && !str.contains(AppProcessHook.currentPackageName)) {
                    Iterator<String> it3 = antiDetectionPathRedirects.keySet().iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        String next3 = it3.next();
                        if (str.startsWith(next3)) {
                            str = str.replaceFirst(next3, antiDetectionPathRedirects.get(next3));
                            break;
                        }
                    }
                    Iterator<String> it4 = antiDetectionFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it4.hasNext()) {
                            break;
                        }
                        String next4 = it4.next();
                        if (str.endsWith("/" + next4)) {
                            str = str.substring(0, str.length() - next4.length()) + antiDetectionFileNameRedirects.get(next4);
                            break;
                        }
                    }
                }
            }
            FileObserver_onEvent_bak(obj, i, str);
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
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    public static void FileObserver_onEvent_bak(Object obj, int i, String str) {
        log("FileObserver_onEvent_bak", obj, "event:" + i, "path:" + str);
        FileObserver_onEvent_copy(obj, i, str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
    }

    public static void FileObserver_onEvent_copy(Object obj, int i, String str) {
        log("FileObserver_onEvent_copy", obj, "event:" + i, "path:" + str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
    }

    public static void File_bak(Object obj, String str) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
        log("File_bak", obj, "pathname:" + str);
        File_copy(obj, str);
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
        try {
            StringBuffer stringBuffer9 = new StringBuffer();
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#" + obj);
            stringBuffer9.toString();
            for (int i17 = 0; i17 < 100; i17 = i17 + 1 + 1) {
            }
            for (int i18 = 0; i18 < 100; i18 = i18 + 1 + 1) {
            }
        } catch (Exception e9) {
            e9.printStackTrace();
        }
    }

    public static void File_copy(Object obj, String str) {
        log("File_copy", obj, "pathname:" + str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
    }

    public static void ProcessBuilder(Object obj, String[] strArr) {
        log("ProcessBuilder", obj, strArr);
        if (strArr != null) {
            try {
                boolean zM117 = HideRootServiceManager.getInstance().isHideRootEnabled();
                boolean z = AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled();
                if (zM117 && strArr.length == 2 && strArr[0].contains("which") && strArr[1].equals("su")) {
                    strArr[1] = rootCommandRedirects.get("su");
                    ProcessBuilder_bak(obj, strArr);
                    return;
                }
                for (int i = 0; i < strArr.length; i++) {
                    String strReplace = strArr[i];
                    if (strReplace != null) {
                        if (zM117) {
                            Iterator<String> it = rootCommandRedirects.keySet().iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    break;
                                }
                                String next = it.next();
                                if (!strReplace.contains(next + " ")) {
                                    if (strReplace.equals(next)) {
                                        strReplace = strReplace.replace(next, rootCommandRedirects.get(next));
                                        strArr[i] = strReplace;
                                        break;
                                    }
                                } else {
                                    strReplace = strReplace.replace(next + " ", rootCommandRedirects.get(next) + " ");
                                    strArr[i] = strReplace;
                                }
                            }
                            Iterator<String> it2 = rootPathRedirects.keySet().iterator();
                            while (true) {
                                if (!it2.hasNext()) {
                                    break;
                                }
                                String next2 = it2.next();
                                if (strReplace.contains(next2)) {
                                    strReplace = strReplace.replace(next2, rootPathRedirects.get(next2));
                                    strArr[i] = strReplace;
                                    break;
                                }
                            }
                            Iterator<String> it3 = rootFileNameRedirects.keySet().iterator();
                            while (true) {
                                if (!it3.hasNext()) {
                                    break;
                                }
                                String next3 = it3.next();
                                if (strReplace.contains(next3)) {
                                    strReplace = strReplace.replace(next3, rootFileNameRedirects.get(next3));
                                    strArr[i] = strReplace;
                                    break;
                                }
                            }
                        }
                        if (z) {
                            Iterator<String> it4 = antiDetectionCommandRedirects.keySet().iterator();
                            while (true) {
                                if (!it4.hasNext()) {
                                    break;
                                }
                                String next4 = it4.next();
                                if (!strReplace.contains(next4 + " ")) {
                                    if (strReplace.equals(next4)) {
                                        strReplace = strReplace.replace(next4, antiDetectionCommandRedirects.get(next4));
                                        strArr[i] = strReplace;
                                        break;
                                    }
                                } else {
                                    strReplace = strReplace.replace(next4 + " ", antiDetectionCommandRedirects.get(next4) + " ");
                                    strArr[i] = strReplace;
                                }
                            }
                            Iterator<String> it5 = antiDetectionPathRedirects.keySet().iterator();
                            while (true) {
                                if (!it5.hasNext()) {
                                    break;
                                }
                                String next5 = it5.next();
                                if (strReplace.contains(next5)) {
                                    strReplace = strReplace.replace(next5, antiDetectionPathRedirects.get(next5));
                                    strArr[i] = strReplace;
                                    break;
                                }
                            }
                            Iterator<String> it6 = antiDetectionFileNameRedirects.keySet().iterator();
                            while (true) {
                                if (!it6.hasNext()) {
                                    break;
                                }
                                String next6 = it6.next();
                                if (strReplace.contains(next6)) {
                                    strArr[i] = strReplace.replace(next6, antiDetectionFileNameRedirects.get(next6));
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
                ProcessBuilder_bak(obj, new String[]{"ps"});
                return;
            }
        }
        ProcessBuilder_bak(obj, strArr);
    }

    public static void ProcessBuilder_Q(Object obj, List<String> list) {
        log("ProcessBuilder_Q", obj, list);
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
        if (list != null) {
            try {
                boolean zM117 = HideRootServiceManager.getInstance().isHideRootEnabled();
                boolean z = AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled();
                if (zM117 && list.size() == 2 && list.get(0).contains("which") && list.get(1).equals("su")) {
                    list.set(1, rootCommandRedirects.get("su"));
                    ProcessBuilder_Q_bak(obj, list);
                    return;
                }
                for (int i5 = 0; i5 < list.size(); i5++) {
                    String strReplace = list.get(i5);
                    if (strReplace != null) {
                        if (zM117) {
                            Iterator<String> it = rootCommandRedirects.keySet().iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    break;
                                }
                                String next = it.next();
                                if (!strReplace.contains(next + " ")) {
                                    if (strReplace.equals(next)) {
                                        strReplace = strReplace.replace(next, rootCommandRedirects.get(next));
                                        list.set(i5, strReplace);
                                        break;
                                    }
                                } else {
                                    strReplace = strReplace.replace(next + " ", rootCommandRedirects.get(next) + " ");
                                    list.set(i5, strReplace);
                                }
                            }
                            Iterator<String> it2 = rootPathRedirects.keySet().iterator();
                            while (true) {
                                if (!it2.hasNext()) {
                                    break;
                                }
                                String next2 = it2.next();
                                if (strReplace.contains(next2)) {
                                    strReplace = strReplace.replace(next2, rootPathRedirects.get(next2));
                                    list.set(i5, strReplace);
                                    break;
                                }
                            }
                            Iterator<String> it3 = rootFileNameRedirects.keySet().iterator();
                            while (true) {
                                if (!it3.hasNext()) {
                                    break;
                                }
                                String next3 = it3.next();
                                if (strReplace.contains(next3)) {
                                    strReplace = strReplace.replace(next3, rootFileNameRedirects.get(next3));
                                    list.set(i5, strReplace);
                                    break;
                                }
                            }
                        }
                        if (z) {
                            Iterator<String> it4 = antiDetectionCommandRedirects.keySet().iterator();
                            while (true) {
                                if (!it4.hasNext()) {
                                    break;
                                }
                                String next4 = it4.next();
                                if (!strReplace.contains(next4 + " ")) {
                                    if (strReplace.equals(next4)) {
                                        strReplace = strReplace.replace(next4, antiDetectionCommandRedirects.get(next4));
                                        list.set(i5, strReplace);
                                        break;
                                    }
                                } else {
                                    strReplace = strReplace.replace(next4 + " ", antiDetectionCommandRedirects.get(next4) + " ");
                                    list.set(i5, strReplace);
                                }
                            }
                            Iterator<String> it5 = antiDetectionPathRedirects.keySet().iterator();
                            while (true) {
                                if (!it5.hasNext()) {
                                    break;
                                }
                                String next5 = it5.next();
                                if (strReplace.contains(next5)) {
                                    strReplace = strReplace.replace(next5, antiDetectionPathRedirects.get(next5));
                                    list.set(i5, strReplace);
                                    break;
                                }
                            }
                            Iterator<String> it6 = antiDetectionFileNameRedirects.keySet().iterator();
                            while (true) {
                                if (!it6.hasNext()) {
                                    break;
                                }
                                String next6 = it6.next();
                                if (strReplace.contains(next6)) {
                                    list.set(i5, strReplace.replace(next6, antiDetectionFileNameRedirects.get(next6)));
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
                log("ProcessBuilder_Q.err", th.getMessage());
                list.clear();
                list.add("ps");
                ProcessBuilder_Q_bak(obj, list);
                return;
            }
        }
        ProcessBuilder_Q_bak(obj, list);
    }

    public static void ProcessBuilder_Q_bak(Object obj, List<String> list) {
        log("ProcessBuilder_Q_bak", obj, list);
        ProcessBuilder_Q_copy(obj, list);
        try {
            StringBuffer stringBuffer = new StringBuffer();
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
    }

    public static void ProcessBuilder_Q_copy(Object obj, List<String> list) {
        log("ProcessBuilder_Q_copy", obj, list);
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
    }

    public static void ProcessBuilder_bak(Object obj, String[] strArr) {
        log("ProcessBuilder_bak", obj, strArr);
        ProcessBuilder_copy(obj, strArr);
        try {
            StringBuffer stringBuffer = new StringBuffer();
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
    }

    public static void ProcessBuilder_copy(Object obj, String[] strArr) {
        log("ProcessBuilder_copy", obj, strArr);
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
    }

    public static Process ProcessBuilder_start(Object obj) {
        log("ProcessBuilder_start", obj);
        try {
            List list = (List) ReflectionUtils.getFieldValue(obj, ProcessBuilder.class, "command");
            log("ProcessBuilder_start,command", list);
            if (list != null) {
                boolean zM117 = HideRootServiceManager.getInstance().isHideRootEnabled();
                boolean z = AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled();
                if (zM117 && list.size() == 2 && ((String) list.get(0)).contains("which") && ((String) list.get(1)).equals("su")) {
                    list.set(1, rootCommandRedirects.get("su"));
                    return ProcessBuilder_start_bak(obj);
                }
                for (int i = 0; i < list.size(); i++) {
                    String strReplace = (String) list.get(i);
                    if (strReplace != null) {
                        if (zM117) {
                            Iterator<String> it = rootCommandRedirects.keySet().iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    break;
                                }
                                String next = it.next();
                                if (!strReplace.contains(next + " ")) {
                                    if (strReplace.equals(next)) {
                                        strReplace = strReplace.replace(next, rootCommandRedirects.get(next));
                                        list.set(i, strReplace);
                                        break;
                                    }
                                } else {
                                    strReplace = strReplace.replace(next + " ", rootCommandRedirects.get(next) + " ");
                                    list.set(i, strReplace);
                                }
                            }
                            Iterator<String> it2 = rootPathRedirects.keySet().iterator();
                            while (true) {
                                if (!it2.hasNext()) {
                                    break;
                                }
                                String next2 = it2.next();
                                if (strReplace.contains(next2)) {
                                    strReplace = strReplace.replace(next2, rootPathRedirects.get(next2));
                                    list.set(i, strReplace);
                                    break;
                                }
                            }
                            Iterator<String> it3 = rootFileNameRedirects.keySet().iterator();
                            while (true) {
                                if (!it3.hasNext()) {
                                    break;
                                }
                                String next3 = it3.next();
                                if (strReplace.contains(next3)) {
                                    strReplace = strReplace.replace(next3, rootFileNameRedirects.get(next3));
                                    list.set(i, strReplace);
                                    break;
                                }
                            }
                        }
                        if (z) {
                            Iterator<String> it4 = antiDetectionCommandRedirects.keySet().iterator();
                            while (true) {
                                if (!it4.hasNext()) {
                                    break;
                                }
                                String next4 = it4.next();
                                if (!strReplace.contains(next4 + " ")) {
                                    if (strReplace.equals(next4)) {
                                        strReplace = strReplace.replace(next4, antiDetectionCommandRedirects.get(next4));
                                        list.set(i, strReplace);
                                        break;
                                    }
                                } else {
                                    strReplace = strReplace.replace(next4 + " ", antiDetectionCommandRedirects.get(next4) + " ");
                                    list.set(i, strReplace);
                                }
                            }
                            Iterator<String> it5 = antiDetectionPathRedirects.keySet().iterator();
                            while (true) {
                                if (!it5.hasNext()) {
                                    break;
                                }
                                String next5 = it5.next();
                                if (strReplace.contains(next5)) {
                                    strReplace = strReplace.replace(next5, antiDetectionPathRedirects.get(next5));
                                    list.set(i, strReplace);
                                    break;
                                }
                            }
                            Iterator<String> it6 = antiDetectionFileNameRedirects.keySet().iterator();
                            while (true) {
                                if (!it6.hasNext()) {
                                    break;
                                }
                                String next6 = it6.next();
                                if (strReplace.contains(next6)) {
                                    list.set(i, strReplace.replace(next6, antiDetectionFileNameRedirects.get(next6)));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return ProcessBuilder_start_bak(obj);
    }

    public static Process ProcessBuilder_start_bak(Object obj) {
        log("ProcessBuilder_start_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
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
        return ProcessBuilder_start_copy(obj);
    }

    public static Process ProcessBuilder_start_copy(Object obj) {
        log("ProcessBuilder_start_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static OutputStream UNIXProcess_getOutputStream(Object obj) {
        boolean z = false;
        log("UNIXProcess_getOutputStream", obj);
        OutputStream outputStreamUNIXProcess_getOutputStream_bak = UNIXProcess_getOutputStream_bak(obj);
        RelocatingOutputStream relocatingOutputStream = new RelocatingOutputStream(outputStreamUNIXProcess_getOutputStream_bak);
        boolean zM117 = HideRootServiceManager.getInstance().isHideRootEnabled();
        if (AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled()) {
            z = true;
        }
        return (zM117 || z) ? relocatingOutputStream : outputStreamUNIXProcess_getOutputStream_bak;
    }

    public static OutputStream UNIXProcess_getOutputStream_bak(Object obj) {
        log("UNIXProcess_getOutputStream_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
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
        return UNIXProcess_getOutputStream_copy(obj);
    }

    public static OutputStream UNIXProcess_getOutputStream_copy(Object obj) {
        log("UNIXProcess_getOutputStream_copy", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Process exec(Object obj, String[] strArr, String[] strArr2, File file) {
        log("exec", obj, strArr, strArr2, file);
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
        if (strArr != null) {
            boolean zM117 = HideRootServiceManager.getInstance().isHideRootEnabled();
            boolean z = AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isHookRulesEnabled();
            if (zM117 && strArr.length == 2 && strArr[0].contains("which") && strArr[1].equals("su")) {
                return exec_bak(obj, new String[]{strArr[0], rootCommandRedirects.get("su")}, strArr2, file);
            }
            for (int i5 = 0; i5 < strArr.length; i5++) {
                if (zM117) {
                    Iterator<String> it = rootCommandRedirects.keySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String next = it.next();
                        if (!strArr[i5].contains(next + " ")) {
                            if (strArr[i5].equals(next)) {
                                strArr[i5] = strArr[i5].replace(next, rootCommandRedirects.get(next));
                                break;
                            }
                        } else {
                            strArr[i5] = strArr[i5].replace(next + " ", rootCommandRedirects.get(next) + " ");
                        }
                    }
                    Iterator<String> it2 = rootPathRedirects.keySet().iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        String next2 = it2.next();
                        if (strArr[i5].contains(next2)) {
                            strArr[i5] = strArr[i5].replace(next2, rootPathRedirects.get(next2));
                            break;
                        }
                    }
                    Iterator<String> it3 = rootFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        String next3 = it3.next();
                        if (strArr[i5].contains(next3)) {
                            strArr[i5] = strArr[i5].replace(next3, rootFileNameRedirects.get(next3));
                            break;
                        }
                    }
                }
                if (z) {
                    Iterator<String> it4 = antiDetectionCommandRedirects.keySet().iterator();
                    while (true) {
                        if (!it4.hasNext()) {
                            break;
                        }
                        String next4 = it4.next();
                        if (!strArr[i5].contains(next4 + " ")) {
                            if (strArr[i5].equals(next4)) {
                                strArr[i5] = strArr[i5].replace(next4, antiDetectionCommandRedirects.get(next4));
                                break;
                            }
                        } else {
                            strArr[i5] = strArr[i5].replace(next4 + " ", antiDetectionCommandRedirects.get(next4) + " ");
                        }
                    }
                    Iterator<String> it5 = antiDetectionPathRedirects.keySet().iterator();
                    while (true) {
                        if (!it5.hasNext()) {
                            break;
                        }
                        String next5 = it5.next();
                        if (strArr[i5].contains(next5)) {
                            strArr[i5] = strArr[i5].replace(next5, antiDetectionPathRedirects.get(next5));
                            break;
                        }
                    }
                    Iterator<String> it6 = antiDetectionFileNameRedirects.keySet().iterator();
                    while (true) {
                        if (!it6.hasNext()) {
                            break;
                        }
                        String next6 = it6.next();
                        if (strArr[i5].contains(next6)) {
                            strArr[i5] = strArr[i5].replace(next6, antiDetectionFileNameRedirects.get(next6));
                            break;
                        }
                    }
                }
            }
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
        return exec_bak(obj, strArr, strArr2, file);
    }

    public static Process exec_bak(Object obj, String[] strArr, String[] strArr2, File file) {
        log("exec_bak", obj, strArr, strArr2, file);
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
        return exec_copy(obj, strArr, strArr2, file);
    }

    public static Process exec_copy(Object obj, String[] strArr, String[] strArr2, File file) {
        log("exec_copy", obj, strArr, strArr2, file);
        try {
            StringBuffer stringBuffer = new StringBuffer();
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static void hook(ClassLoader classLoader) {
        try {
            LHooker.hookConstructor(File.class, new Class[]{String.class}, RootHideHook.class, "File", "File_bak", "File_copy");
            LHooker.hookConstructor(File.class, new Class[]{String.class, String.class}, RootHideHook.class, "File2", "File2_bak", "File2_copy");
            LHooker.hookConstructor(File.class, new Class[]{File.class, String.class}, RootHideHook.class, "File3", "File3_bak", "File3_copy");
            LHooker.hookConstructor(File.class, new Class[]{URI.class}, RootHideHook.class, "File4", "File4_bak", "File4_copy");
            Class cls = Integer.TYPE;
            LHooker.hookConstructor(FileObserver.class, new Class[]{String.class, cls}, RootHideHook.class, "FileObserver", "FileObserver_bak", "FileObserver_copy");
            LHooker.hookMethodByNames(FileObserver.class, "onEvent", Void.TYPE, new Class[]{cls, String.class}, RootHideHook.class, "FileObserver_onEvent", "FileObserver_onEvent_bak", "FileObserver_onEvent_copy");
            LHooker.hookConstructor(ProcessBuilder.class, new Class[]{String[].class}, RootHideHook.class, "ProcessBuilder", "ProcessBuilder_bak", "ProcessBuilder_copy");
            LHooker.hookConstructor(ProcessBuilder.class, new Class[]{List.class}, RootHideHook.class, "ProcessBuilder_Q", "ProcessBuilder_Q_bak", "ProcessBuilder_Q_copy");
            LHooker.hookMethodByNames(Runtime.class, "exec", Process.class, new Class[]{String[].class, String[].class, File.class}, RootHideHook.class, "exec", "exec_bak", "exec_copy");
            LHooker.hookMethodWithBackup(ProcessBuilder.class, "start", Process.class, null, RootHideHook.class, "ProcessBuilder_start", "ProcessBuilder_start_bak");
            LHooker.hookMethodWithBackup(Class.forName("java.lang.UNIXProcess"), "getOutputStream", OutputStream.class, null, RootHideHook.class, "UNIXProcess_getOutputStream", "UNIXProcess_getOutputStream_bak");
        } catch (Throwable th) {
            th.printStackTrace();
            log(th.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void log(Object... objArr) {
        if (!com.kail.location.inject.utils.InjectLog.hookLogEnabled) return;
        com.kail.location.inject.utils.InjectLog.log("RootHideHook", objArr);
    }
}
