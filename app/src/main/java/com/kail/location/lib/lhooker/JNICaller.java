package com.kail.location.lib.lhooker;

public class JNICaller {
    public static native int call32(int functionAddress, int arg1, int arg2);

    public static native long call64(long functionAddress, long arg1, long arg2);
}
