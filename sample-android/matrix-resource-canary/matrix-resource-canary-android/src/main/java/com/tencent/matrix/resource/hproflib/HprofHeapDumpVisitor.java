

package com.tencent.matrix.resource.hproflib;

import com.tencent.matrix.resource.hproflib.model.Field;
import com.tencent.matrix.resource.hproflib.model.ID;



@SuppressWarnings("unused")
public class HprofHeapDumpVisitor {
    protected final HprofHeapDumpVisitor hdv;

    public HprofHeapDumpVisitor(HprofHeapDumpVisitor hdv) {
        this.hdv = hdv;
    }

    public void visitHeapDumpInfo(int heapId, ID heapNameId) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpInfo(heapId, heapNameId);
        }
    }

    public void visitHeapDumpBasicObj(int tag, ID id) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpBasicObj(tag, id);
        }
    }

    public void visitHeapDumpJniLocal(ID id, int threadSerialNumber, int stackFrameNumber) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpJniLocal(id, threadSerialNumber, stackFrameNumber);
        }
    }

    public void visitHeapDumpJavaFrame(ID id, int threadSerialNumber, int stackFrameNumber) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpJavaFrame(id, threadSerialNumber, stackFrameNumber);
        }
    }

    public void visitHeapDumpNativeStack(ID id, int threadSerialNumber) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpNativeStack(id, threadSerialNumber);
        }
    }

    public void visitHeapDumpThreadBlock(ID id, int threadSerialNumber) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpThreadBlock(id, threadSerialNumber);
        }
    }

    public void visitHeapDumpThreadObject(ID id, int threadSerialNumber, int stackFrameNumber) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpThreadObject(id, threadSerialNumber, stackFrameNumber);
        }
    }

    public void visitHeapDumpClass(ID id, int stackSerialNumber, ID superClassId, ID classLoaderId,
                                   int instanceSize, Field[] staticFields, Field[] instanceFields) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpClass(id, stackSerialNumber, superClassId, classLoaderId, instanceSize, staticFields, instanceFields);
        }
    }

    public void visitHeapDumpInstance(ID id, int stackId, ID typeId, byte[] instanceData) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpInstance(id, stackId, typeId, instanceData);
        }
    }

    public void visitHeapDumpJniMonitor(ID id, int threadSerialNumber, int stackDepth) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpJniMonitor(id, threadSerialNumber, stackDepth);
        }
    }

    public void visitHeapDumpPrimitiveArray(int tag, ID id, int stackId, int numElements, int typeId, byte[] elements) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpPrimitiveArray(tag, id, stackId, numElements, typeId, elements);
        }
    }

    public void visitHeapDumpObjectArray(ID id, int stackId, int numElements, ID typeId, byte[] elements) {
        if (this.hdv != null) {
            this.hdv.visitHeapDumpObjectArray(id, stackId, numElements, typeId, elements);
        }
    }

    public void visitEnd() {
        if (this.hdv != null) {
            this.hdv.visitEnd();
        }
    }
}
