

package com.tencent.matrix.resource.analyzer;

import com.tencent.matrix.resource.analyzer.model.AnalyzeResult;
import com.tencent.matrix.resource.analyzer.model.HeapSnapshot;



public interface HeapSnapshotAnalyzer<T extends AnalyzeResult> {
    T analyze(HeapSnapshot heapSnapshot);
}
