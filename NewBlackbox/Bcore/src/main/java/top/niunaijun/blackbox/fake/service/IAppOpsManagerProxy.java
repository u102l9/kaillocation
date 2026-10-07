package top.niunaijun.blackbox.fake.service;

import android.app.AppOpsManager;
import android.app.SyncNotedAppOp;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;

import java.lang.reflect.Method;

import black.android.app.BRAppOpsManager;
import black.android.os.BRServiceManager;
import black.com.android.internal.app.BRIAppOpsServiceStub;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;
import top.niunaijun.blackbox.utils.Slog;


public class IAppOpsManagerProxy extends BinderInvocationStub {
    public IAppOpsManagerProxy() {
        super(BRServiceManager.get().getService(Context.APP_OPS_SERVICE));
    }

    @Override
    protected Object getWho() {
        IBinder call = BRServiceManager.get().getService(Context.APP_OPS_SERVICE);
        return BRIAppOpsServiceStub.get().asInterface(call);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        if (BRAppOpsManager.get(null)._check_mService() != null) {
            AppOpsManager appOpsManager = (AppOpsManager) BlackBoxCore.getContext().getSystemService(Context.APP_OPS_SERVICE);
            try {
                BRAppOpsManager.get(appOpsManager)._set_mService(getProxyInvocation());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        replaceSystemService(Context.APP_OPS_SERVICE);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        // Bypass all AppOps permission checks by allowing the operation. The tricky
        // part is the RETURN TYPE: on Android 11+ (API 30) several IAppOpsService
        // methods (noteOperation/startOperation/noteProxyOperation/startProxyOperation)
        // return a SyncNotedAppOp object instead of an int mode. Returning a bare int
        // there triggers a ClassCastException inside the framework's JNI binder call,
        // which becomes a fatal native abort and kills the sandbox process.
        // So we build the allowed result according to the method's declared return type.
        if (methodName.startsWith("check") ||
            methodName.startsWith("note") ||
            methodName.startsWith("start")) {
            Slog.d(TAG, "AppOps invoke: Bypassing system for " + methodName + ", allowing operation");
            return buildAllowedResult(method, args);
        }


        if (methodName.startsWith("finish")) {
            Slog.d(TAG, "AppOps invoke: Bypassing system for " + methodName);
            return null;
        }


        try {
            MethodParameterUtils.replaceFirstAppPkg(args);
            MethodParameterUtils.replaceLastUid(args);
            return super.invoke(proxy, method, args);
        } catch (SecurityException e) {

            Slog.w(TAG, "AppOps invoke: SecurityException caught for " + methodName + ", allowing operation", e);
            return buildAllowedResult(method, args);
        } catch (Exception e) {
            Slog.e(TAG, "AppOps invoke: Error in method " + methodName, e);

            return buildAllowedResult(method, args);
        }
    }

    /**
     * Builds an "operation allowed" return value matching the declared return type of
     * {@code method}. For methods that return {@link SyncNotedAppOp} (API 30+), a real
     * SyncNotedAppOp with {@code MODE_ALLOWED} is constructed; otherwise the int mode
     * {@link AppOpsManager#MODE_ALLOWED} is returned.
     */
    private static Object buildAllowedResult(Method method, Object[] args) {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && SyncNotedAppOp.class.isAssignableFrom(returnType)) {
            return buildSyncNotedAppOp(args);
        }
        return AppOpsManager.MODE_ALLOWED;
    }

    /**
     * Constructs a {@link SyncNotedAppOp} representing an allowed op. The op code is
     * extracted from the call arguments when possible so callers that inspect the
     * returned op (e.g. via {@code getOp()}) see a consistent value.
     *
     * The only constructor that lets us set the op mode to {@code MODE_ALLOWED} is the
     * hidden 4-arg one (the public constructors default to {@code MODE_IGNORED}, which
     * would make the framework treat the op as denied), so we reach it via reflection.
     */
    private static SyncNotedAppOp buildSyncNotedAppOp(Object[] args) {
        int opCode = 0;
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Integer) {
                    opCode = (Integer) arg;
                    break;
                }
            }
        }
        if (opCode < 0 || opCode >= NUM_OP) {
            opCode = 0;
        }
        // Hidden 4-arg constructor: SyncNotedAppOp(int opMode, int opCode, String attributionTag, String packageName)
        try {
            java.lang.reflect.Constructor<SyncNotedAppOp> ctor = SyncNotedAppOp.class
                    .getConstructor(int.class, int.class, String.class, String.class);
            return ctor.newInstance(AppOpsManager.MODE_ALLOWED, opCode, null, "android");
        } catch (Throwable t) {
            Slog.e(TAG, "AppOps: failed to construct allowed SyncNotedAppOp via reflection", t);
        }
        // Fallback: public 2-arg constructor (mode defaults to MODE_IGNORED, but at least
        // returns the correct type so the framework's cast does not abort the process).
        try {
            return new SyncNotedAppOp(opCode, null);
        } catch (Throwable t2) {
            Slog.e(TAG, "AppOps: failed to construct SyncNotedAppOp", t2);
            return null;
        }
    }

    /** Upper bound for op codes; mirrors AppOpsManager._NUM_OP validation. */
    private static final int NUM_OP = resolveNumOp();

    private static int resolveNumOp() {
        try {
            java.lang.reflect.Field f = AppOpsManager.class.getDeclaredField("_NUM_OP");
            f.setAccessible(true);
            return f.getInt(null);
        } catch (Throwable t) {
            // Reasonable upper bound covering all known op codes across API levels.
            return 200;
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("noteProxyOperation")
    public static class NoteProxyOperation extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    @ProxyMethod("checkPackage")
    public static class CheckPackage extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    @ProxyMethod("checkOperation")
    public static class CheckOperation extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            
            Slog.d(TAG, "AppOps CheckOperation: Bypassing system check, allowing operation");
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    
    @ProxyMethod("checkOperationForDevice")
    public static class CheckOperationForDevice extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            Slog.d(TAG, "AppOps CheckOperationForDevice: Bypassing system check, allowing operation");
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    @ProxyMethod("noteOperation")
    public static class NoteOperation extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            Slog.d(TAG, "AppOps NoteOperation: Bypassing system check, allowing operation");
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    @ProxyMethod("checkOpNoThrow")
    public static class CheckOpNoThrow extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            Slog.d(TAG, "AppOps CheckOpNoThrow: Bypassing system check, allowing operation");
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    
    @ProxyMethod("startOp")
    public static class StartOp extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            Slog.d(TAG, "AppOps StartOp: Bypassing system check, allowing operation");
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    @ProxyMethod("startOpNoThrow")
    public static class StartOpNoThrow extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            
            Slog.d(TAG, "AppOps StartOpNoThrow: Bypassing system check, allowing operation");
            return AppOpsManager.MODE_ALLOWED;
        }
    }

    
    @ProxyMethod("finishOp")
    public static class FinishOp extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                int op = (int) args[0];
                String name = getOpPublicName(op);
                if (name != null && isMediaStorageOrAudioOp(name)) {
                    Slog.d(TAG, "AppOps FinishOp: Finishing operation: " + name);
                }
            } catch (Throwable ignored) {
            }
            return null;
        }
    }

    
    @ProxyMethod("noteOp")
    public static class NoteOp extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                int op = (int) args[0];
                String name = getOpPublicName(op);
                if (name != null && (name.contains("RECORD_AUDIO") || name.contains("AUDIO") || name.contains("MICROPHONE"))) {
                    Slog.d(TAG, "AppOps NoteOp: Allowing RECORD_AUDIO operation: " + name);
                    return AppOpsManager.MODE_ALLOWED;
                }
            } catch (Throwable ignored) {
            }
            return method.invoke(who, args);
        }
    }

    
    @ProxyMethod("noteOpNoThrow")
    public static class NoteOpNoThrow extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                int op = (int) args[0];
                String name = getOpPublicName(op);
                if (name != null && (name.contains("RECORD_AUDIO") || name.contains("AUDIO") || name.contains("MICROPHONE"))) {
                    Slog.d(TAG, "AppOps NoteOpNoThrow: Allowing RECORD_AUDIO operation: " + name);
                    return AppOpsManager.MODE_ALLOWED;
                }
            } catch (Throwable ignored) {
            }
            return method.invoke(who, args);
        }
    }

    private static boolean isMediaStorageOrAudioOp(String opPublicNameOrStr) {
        if (opPublicNameOrStr == null) return false;
        
        String n = opPublicNameOrStr.toUpperCase();
        return n.contains("READ_MEDIA")
                || n.contains("READ_EXTERNAL_STORAGE")
                || n.contains("RECORD_AUDIO")
                || n.contains("CAPTURE_AUDIO_OUTPUT")
                || n.contains("MODIFY_AUDIO_SETTINGS")
                || n.contains("AUDIO")
                || n.contains("MICROPHONE")
                || n.contains("FOREGROUND_SERVICE")
                || n.contains("SYSTEM_ALERT_WINDOW")
                || n.contains("WRITE_SETTINGS")
                || n.contains("ACCESS_FINE_LOCATION")
                || n.contains("ACCESS_COARSE_LOCATION")
                || n.contains("CAMERA")
                || n.contains("BODY_SENSORS")
                || n.contains("BLUETOOTH_SCAN")
                || n.contains("BLUETOOTH_CONNECT")
                || n.contains("BLUETOOTH_ADVERTISE")
                || n.contains("NEARBY_WIFI_DEVICES")
                || n.contains("POST_NOTIFICATIONS");
    }

    private static String getOpPublicName(int op) {
        try {
            
            java.lang.reflect.Method m = AppOpsManager.class.getMethod("opToPublicName", int.class);
            Object name = m.invoke(null, op);
            return name != null ? name.toString() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
