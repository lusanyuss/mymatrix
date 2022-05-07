

package com.tencent.matrix.resource;

import static com.tencent.matrix.resource.common.utils.StreamUtil.closeQuietly;
import static com.tencent.matrix.resource.common.utils.StreamUtil.copyFileToStream;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.resource.analyzer.model.HeapDump;
import com.tencent.matrix.resource.dumper.DumpStorageManager;
import com.tencent.matrix.resource.hproflib.HprofBufferShrinker;
import com.tencent.matrix.util.MatrixLog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 */
public class CanaryWorkerService extends MatrixJobIntentService {
    private static final String TAG = "Matrix.CanaryWorkerService";

    private static final int JOB_ID = 0xFAFBFCFD;
    private static final String ACTION_SHRINK_HPROF = "com.tencent.matrix.resource.worker.action.SHRINK_HPROF";
    private static final String EXTRA_PARAM_HEAPDUMP = "com.tencent.matrix.resource.worker.param.HEAPDUMP";

    public static void shrinkHprofAndReport(Context context, HeapDump heapDump) {
        final Intent intent = new Intent(context, CanaryWorkerService.class);
        intent.setAction(ACTION_SHRINK_HPROF);
        intent.putExtra(EXTRA_PARAM_HEAPDUMP, heapDump);
        enqueueWork(context, CanaryWorkerService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SHRINK_HPROF.equals(action)) {
                try {
                    intent.setExtrasClassLoader(this.getClassLoader());
                    final HeapDump heapDump = (HeapDump) intent.getSerializableExtra(EXTRA_PARAM_HEAPDUMP);
                    if (heapDump != null) {
                        doShrinkHprofAndReport(heapDump);
                    } else {
                        MatrixLog.e(TAG, "failed to deserialize heap dump, give up shrinking and reporting.");
                    }
                } catch (Throwable thr) {
                    MatrixLog.printErrStackTrace(TAG, thr,  "failed to deserialize heap dump, give up shrinking and reporting.");
                }
            }
        }
    }

    private void doShrinkHprofAndReport(HeapDump heapDump) {
        final File hprofDir = heapDump.getHprofFile().getParentFile();
        final File shrinkedHProfFile = new File(hprofDir, getShrinkHprofName(heapDump.getHprofFile()));
        final File zipResFile = new File(hprofDir, getResultZipName("dump_result_" + android.os.Process.myPid()));
        final File hprofFile = heapDump.getHprofFile();
        ZipOutputStream zos = null;
        try {
            long startTime = System.currentTimeMillis();
            new HprofBufferShrinker().shrink(hprofFile, shrinkedHProfFile);
            MatrixLog.i(TAG, "shrink hprof file %s, size: %dk to %s, size: %dk, use time:%d",
                    hprofFile.getPath(), hprofFile.length() / 1024, shrinkedHProfFile.getPath(), shrinkedHProfFile.length() / 1024, (System.currentTimeMillis() - startTime));

            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipResFile)));

            final ZipEntry resultInfoEntry = new ZipEntry("result.info");
            final ZipEntry shrinkedHProfEntry = new ZipEntry(shrinkedHProfFile.getName());

            zos.putNextEntry(resultInfoEntry);
            final PrintWriter pw = new PrintWriter(new OutputStreamWriter(zos, Charset.forName("UTF-8")));
            pw.println("# Resource Canary Result Infomation. THIS FILE IS IMPORTANT FOR THE ANALYZER !!");
            pw.println("sdkVersion=" + Build.VERSION.SDK_INT);
            pw.println("manufacturer=" + Matrix.with().getPluginByClass(ResourcePlugin.class).getConfig().getManufacture());
            pw.println("hprofEntry=" + shrinkedHProfEntry.getName());
            pw.println("leakedActivityKey=" + heapDump.getReferenceKey());
            pw.flush();
            zos.closeEntry();

            zos.putNextEntry(shrinkedHProfEntry);
            copyFileToStream(shrinkedHProfFile, zos);
            zos.closeEntry();

            shrinkedHProfFile.delete();
            hprofFile.delete();

            MatrixLog.i(TAG, "process hprof file use total time:%d", (System.currentTimeMillis() - startTime));

            CanaryResultService.reportHprofResult(this, zipResFile.getAbsolutePath(), heapDump.getActivityName());
        } catch (IOException e) {
            MatrixLog.printErrStackTrace(TAG, e, "");
        } finally {
            closeQuietly(zos);
        }
    }

    private String getShrinkHprofName(File origHprof) {
        final String origHprofName = origHprof.getName();
        final int extPos = origHprofName.indexOf(DumpStorageManager.HPROF_EXT);
        final String namePrefix = origHprofName.substring(0, extPos);
        return namePrefix + "_shrink" + DumpStorageManager.HPROF_EXT;
    }

    private String getResultZipName(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append('_')
                .append(new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(new Date()))
                .append(".zip");
        return sb.toString();
    }
}
