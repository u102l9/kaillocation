package com.kail.location.inject.utils;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesCipherUtils {
    private static byte[] decodeBase64(String encodedText) {
        return Base64.decode(encodedText, 0);
    }

    public static String decryptCfbNoPadding(String encryptedText, String aesKey, String iv) {
        try {
            byte[] encryptedBytes = decodeBase64(encryptedText);
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey.getBytes(), "AES"), new IvParameterSpec(iv.getBytes()));
            return new String(cipher.doFinal(encryptedBytes)).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String encodeBase64(byte[] bytes) {
        return new String(Base64.encode(bytes, Base64.NO_WRAP));
    }

    public static String encryptCfbNoPadding(String plainText, String aesKey, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            int blockSize = cipher.getBlockSize();
            byte[] plainBytes = plainText.getBytes();
            int paddedLength = plainBytes.length;
            if (paddedLength % blockSize != 0) {
                paddedLength += blockSize - (paddedLength % blockSize);
            }
            byte[] paddedBytes = new byte[paddedLength];
            System.arraycopy(plainBytes, 0, paddedBytes, 0, plainBytes.length);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey.getBytes(), "AES"), new IvParameterSpec(iv.getBytes()));
            return encodeBase64(cipher.doFinal(paddedBytes)).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
