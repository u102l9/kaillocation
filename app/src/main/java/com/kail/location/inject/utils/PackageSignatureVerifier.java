package com.kail.location.inject.utils;

import android.content.Context;

/**
 * Rebrand of the original FakeLocation signature gate.
 *
 * Reduced to a no-op for the kail rebrand. Calling this from inside system_server
 * is brittle because {@code context.getPackageName()} returns
 * {@code "android"} there, never the host package name, so any meaningful
 * comparison would always fail.
 *
 * Kept as a class with the same signature so the rest of the inject tree
 * compiles unchanged.
 */
public class PackageSignatureVerifier {

    /** No-op. */
    public static void verifyPackageSignature(Context context, String packageName, String targetName) {
        // intentionally empty
    }
}
