package com.kail.location.inject.utils;

/**
 * Rebrand of the original FakeLocation license-state coordinator.
 *
 * The upstream framework verified each enabled feature against a remote
 * license server at {@code vef.api.fakeloc.cc} and gated controller-side
 * setters on a successful round-trip. The kail rebrand replaces that
 * with a no-op authorization that always reports "usable" — host-package
 * signature verification handled by {@link PackageSignatureVerifier} and
 * {@code fakeloc_common.h::verifyReleaseSignature} is the only gate left.
 *
 * The public API surface here is preserved as-is so the rest of the inject
 * tree continues to compile and link unchanged.
 */
public final class LicenseStateManager {

    private static volatile byte[] currentLicenseBytes;
    private static volatile String currentDeviceId;

    private LicenseStateManager() {}

    public static byte[] getCurrentLicenseBytes() {
        return currentLicenseBytes;
    }

    public static String getCurrentDeviceId() {
        return currentDeviceId;
    }

    /**
     * Recorded for diagnostic use only. The kail rebrand does not contact a
     * remote server or apply any expiry logic.
     */
    public static void updateLicenseState(String licenseToken, String deviceId) {
        currentLicenseBytes = licenseToken == null ? null : licenseToken.getBytes();
        currentDeviceId = deviceId;
    }

    public static long getFeatureExpiryTimeMillis() {
        return Long.MAX_VALUE;
    }

    public static long getLicenseExpiryTimeMillis() {
        return Long.MAX_VALUE;
    }

    public static String getLicenseStatusCode() {
        return "200";
    }

    public static boolean isFeatureExpired() {
        return false;
    }

    public static boolean isLicenseExpired() {
        return false;
    }

    public static boolean isLicenseUsable() {
        return true;
    }

    public static boolean hasRemoteDenial() {
        return false;
    }

    /** No-op stub — preserved for binary compatibility with the original. */
    public static void verifyWithServer(String licenseToken, String deviceId) {
        // intentionally empty
    }
}
