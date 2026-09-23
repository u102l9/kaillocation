package com.kail.location.lib.lhooker;

/**
 * Layout probe for the native LHooker engine.
 *
 * On ART a {@code jmethodID} is literally the {@code ArtMethod*}, and every
 * method declared in a single class is stored back-to-back in one contiguous
 * {@code LengthPrefixedArray<ArtMethod>}. The native side resolves the method
 * IDs of the static methods below and takes the minimum positive gap between
 * the pointers — that gap is the exact {@code sizeof(ArtMethod)} on the running
 * device, regardless of Android version. The quick entry point
 * ({@code entry_point_from_quick_compiled_code_}) is always the last
 * pointer-sized field, so its offset is {@code size - sizeof(void*)}.
 *
 * This lets the engine self-configure on any ROM (including future Android
 * versions) instead of relying on a hard-coded per-SDK offset table that
 * silently corrupts system_server when a new SDK isn't listed.
 *
 * The methods must stay declared in this class, have empty bodies, and never be
 * stripped/inlined — the slim inject DEX is built with D8 and no shrinking, so
 * they are preserved as-is. Do not add a custom constructor or fields; keep the
 * shape minimal so the method array stays tightly packed.
 */
public final class ArtMethodProbe {
    public static void m0() {}
    public static void m1() {}
    public static void m2() {}
    public static void m3() {}
    public static void m4() {}
    public static void m5() {}
    public static void m6() {}
    public static void m7() {}
    public static void m8() {}
    public static void m9() {}
    public static void m10() {}
    public static void m11() {}
}
