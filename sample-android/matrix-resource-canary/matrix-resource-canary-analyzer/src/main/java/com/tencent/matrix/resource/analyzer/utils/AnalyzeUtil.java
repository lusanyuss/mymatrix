

package com.tencent.matrix.resource.analyzer.utils;

import com.squareup.haha.perflib.RootObj;
import com.squareup.haha.perflib.Snapshot;
import com.squareup.haha.trove.THashMap;
import com.squareup.haha.trove.TObjectProcedure;

import java.util.Collection;

import static java.util.concurrent.TimeUnit.NANOSECONDS;



public final class AnalyzeUtil {

    /**
     * Pruning duplicates reduces memory pressure from hprof bloat added in Marshmallow.
     */
    public static void deduplicateGcRoots(Snapshot snapshot) {
        // THashMap has a smaller memory footprint than HashMap.
        final THashMap<String, RootObj> uniqueRootMap = new THashMap<>();

        final Collection<RootObj> gcRoots = snapshot.getGCRoots();
        for (RootObj root : gcRoots) {
            String key = generateRootKey(root);
            if (!uniqueRootMap.containsKey(key)) {
                uniqueRootMap.put(key, root);
            }
        }

        // Repopulate snapshot with unique GC roots.
        gcRoots.clear();
        uniqueRootMap.forEach(new TObjectProcedure<String>() {
            @Override
            public boolean execute(String key) {
                return gcRoots.add(uniqueRootMap.get(key));
            }
        });
    }

    private static String generateRootKey(RootObj root) {
        return String.format("%s@0x%08x", root.getRootType().getName(), root.getId());
    }

    public static long since(long analysisStartNanoTime) {
        return NANOSECONDS.toMillis(System.nanoTime() - analysisStartNanoTime);
    }

    private AnalyzeUtil() {
        throw new UnsupportedOperationException();
    }
}
