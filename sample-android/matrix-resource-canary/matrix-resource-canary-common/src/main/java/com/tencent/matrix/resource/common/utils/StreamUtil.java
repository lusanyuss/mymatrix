

package com.tencent.matrix.resource.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;



public final class StreamUtil {

    public static void closeQuietly(Object target) {
        if (target == null) {
            return;
        }
        try {
            if (target instanceof Closeable) {
                ((Closeable) target).close();
            } else if (target instanceof ZipFile) {
                ((ZipFile) target).close();
            }
        } catch (Throwable ignored) {
            // Ignored.
        }
    }

    public static boolean preventZipSlip(java.io.File output, String zipEntryName) {

        try {
            if (zipEntryName.contains("..") && new File(output, zipEntryName).getCanonicalPath().startsWith(output.getCanonicalPath() + File.separator)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    public static void extractZipEntry(ZipFile zipFile, ZipEntry targetEntry, File output) throws IOException {

        if (preventZipSlip(output, targetEntry.getName())) {
            throw new IllegalStateException("extractZipEntry entry " + targetEntry.getName() + " failed!");
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(zipFile.getInputStream(targetEntry));
            os = new BufferedOutputStream(new FileOutputStream(output));
            final byte[] buffer = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = is.read(buffer)) > 0) {
                os.write(buffer, 0, bytesRead);
            }
        } finally {
            closeQuietly(os);
            closeQuietly(is);
        }
    }

    public static void copyFileToStream(File in, OutputStream out) throws IOException {
        InputStream is = null;
        final byte[] buffer = new byte[4096];
        try {
            is = new BufferedInputStream(new FileInputStream(in));
            int bytesRead = 0;
            while ((bytesRead = is.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } finally {
            closeQuietly(is);
        }
    }

    private StreamUtil() {
        throw new UnsupportedOperationException();
    }
}
