

package com.tencent.matrix.resource.hproflib.model;

import java.util.Arrays;



public final class ID {
    private final byte[] mIdBytes;

    public static ID createNullID(int size) {
        return new ID(new byte[size]);
    }

    public ID(byte[] idBytes) {
        final int len = idBytes.length;
        mIdBytes = new byte[len];
        System.arraycopy(idBytes, 0, mIdBytes, 0, len);
    }

    public byte[] getBytes() {
        return mIdBytes;
    }

    public int getSize() {
        return mIdBytes.length;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ID)) {
            return false;
        }
        return Arrays.equals(mIdBytes, ((ID) obj).mIdBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mIdBytes);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte b : mIdBytes) {
            final int eb = b & 0xFF;
            sb.append(Integer.toHexString(eb));
        }
        return sb.toString();
    }
}
