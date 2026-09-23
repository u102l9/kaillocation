package com.kail.location.inject.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * {@link LocationShm.Access} 的 {@code sun.misc.Unsafe} 实现。
 *
 * <p>最初用 {@code MethodHandles.byteBufferViewVarHandle} 做 volatile 访问，但在本机
 * Android 14（ART）上访问 direct ByteBuffer 会 SIGSEGV（ART 的
 * ByteBufferViewVarHandle 实现 bug，64 位地址被截断成 32 位）。改用
 * {@code sun.misc.Unsafe} 的 {@code *Volatile} 方法直接按地址做 off-heap
 * volatile 读写，这是 Android 上访问共享内存的标准做法。
 *
 * <p>Unsafe 属 hidden API，故通过反射获取；失败时构造函数抛异常，由
 * {@link LocationShm} 捕获并置为不可用，自动回退控制文件通道，不会崩溃。
 */
final class LocationShmAccess implements LocationShm.Access {

    private final Object unsafe;
    private final Method getIntVolatile;
    private final Method putIntVolatile;
    private final Method getLongVolatile;
    private final Method putLongVolatile;
    private final Method getLong;
    private final Method fullFence;
    private final long bufferAddressOffset;

    LocationShmAccess() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        unsafe = theUnsafe.get(null);

        getIntVolatile = unsafeClass.getMethod("getIntVolatile", Object.class, long.class);
        putIntVolatile = unsafeClass.getMethod("putIntVolatile", Object.class, long.class, int.class);
        getLongVolatile = unsafeClass.getMethod("getLongVolatile", Object.class, long.class);
        putLongVolatile = unsafeClass.getMethod("putLongVolatile", Object.class, long.class, long.class);
        getLong = unsafeClass.getMethod("getLong", Object.class, long.class);
        // Android 的 sun.misc.Unsafe 没有 getDoubleVolatile/putDoubleVolatile，
        // 故 double 统一按 64 位位模式走 long volatile。
        Method fenceMethod;
        try {
            fenceMethod = unsafeClass.getMethod("fullFence");
        } catch (NoSuchMethodException e) {
            fenceMethod = null; // 极老版本没有 fullFence，退化为仅靠 volatile 语义
        }
        fullFence = fenceMethod;

        Method objectFieldOffset = unsafeClass.getMethod("objectFieldOffset", Field.class);
        Field addressField = Buffer.class.getDeclaredField("address");
        bufferAddressOffset = (Long) objectFieldOffset.invoke(unsafe, addressField);
    }

    /** direct ByteBuffer 的基地址。 */
    private long address(ByteBuffer b) {
        try {
            return (Long) getLong.invoke(unsafe, b, bufferAddressOffset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getSeq(ByteBuffer b) {
        return getLong(b, LocationShm.OFF_SEQ);
    }

    @Override
    public void setSeq(ByteBuffer b, long value) {
        setLong(b, LocationShm.OFF_SEQ, value);
    }

    @Override
    public int getInt(ByteBuffer b, int byteOffset) {
        try {
            return (Integer) getIntVolatile.invoke(unsafe, null, address(b) + byteOffset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setInt(ByteBuffer b, int byteOffset, int value) {
        try {
            putIntVolatile.invoke(unsafe, null, address(b) + byteOffset, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLong(ByteBuffer b, int byteOffset) {
        try {
            return (Long) getLongVolatile.invoke(unsafe, null, address(b) + byteOffset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLong(ByteBuffer b, int byteOffset, long value) {
        try {
            putLongVolatile.invoke(unsafe, null, address(b) + byteOffset, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getDouble(ByteBuffer b, int byteOffset) {
        return Double.longBitsToDouble(getLong(b, byteOffset));
    }

    @Override
    public void setDouble(ByteBuffer b, int byteOffset, double value) {
        setLong(b, byteOffset, Double.doubleToRawLongBits(value));
    }

    @Override
    public void fence() {
        if (fullFence == null) return;
        try {
            fullFence.invoke(unsafe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
