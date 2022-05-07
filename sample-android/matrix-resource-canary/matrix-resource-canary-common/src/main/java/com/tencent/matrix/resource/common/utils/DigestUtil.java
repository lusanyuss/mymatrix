

package com.tencent.matrix.resource.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public final class DigestUtil {
    private static final char[] HEX_DIGITS
            = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getMD5String(byte[] buffer) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            final byte[] resBytes = md.digest();
            return bytesToHexString(resBytes);
        } catch (NoSuchAlgorithmException e) {
            // Should not happen.
            throw new IllegalStateException(e);
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (b >= 0 && b <= 15) {
                sb.append('0').append(HEX_DIGITS[b]);
            } else {
                sb.append(HEX_DIGITS[(b >> 4) & 0x0F]).append(HEX_DIGITS[b & 0x0F]);
            }
        }
        return sb.toString();
    }

    private DigestUtil() {
        throw new UnsupportedOperationException();
    }
}
