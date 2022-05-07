

package com.tencent.mm.arscutil.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 1.整个arsc文件是一个 RES_TABLE_TYPE 类型的chunk；
 * 2.RES_TABLE_TYPE 可分为三个部分：文件头部和两个子chunk
 * ( RES_STRING_POOL_TYPE 、 RES_TABLE_PACKAGE_TYPE )；
 */
public class ResTable extends ResChunk {

    private int packageCount; // 文件头部,资源package数目, 4 bytes
    private ResStringBlock globalStringPool; // 全局资源池 chunk
    private ResPackage[] packages; // 资源package数组 chunk

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
    }

    public ResStringBlock getGlobalStringPool() {
        return globalStringPool;
    }

    public void setGlobalStringPool(ResStringBlock globalStringPool) {
        this.globalStringPool = globalStringPool;
    }

    public ResPackage[] getPackages() {
        return packages;
    }

    public void setPackages(ResPackage[] packages) {
        this.packages = packages;
    }

    public void refresh() {
        recomputeChunkSize();
    }

    private void recomputeChunkSize() {
        chunkSize = 0;
        chunkSize += headSize;
        if (globalStringPool != null) {
            chunkSize += globalStringPool.getChunkSize();
        }
        if (packages != null) {
            for (ResPackage resPackage : packages) {
                chunkSize += resPackage.getChunkSize();
            }
        }
        if (chunkSize % 4 != 0) {
            chunkPadding = 4 - chunkSize % 4;
            chunkSize += chunkPadding;
        } else {
            chunkPadding = 0;
        }
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.clear();
        byteBuffer.putShort(type);
        byteBuffer.putShort(headSize);
        byteBuffer.putInt(chunkSize);
        byteBuffer.putInt(packageCount);
        if (headPadding > 0) {
            byteBuffer.put(new byte[headPadding]);
        }
        if (globalStringPool != null) {
            byteBuffer.put(globalStringPool.toBytes());
        }
        if (packages != null) {
            for (int i = 0; i < packages.length; i++) {
                byteBuffer.put(packages[i].toBytes());
            }
        }
        if (chunkPadding > 0) {
            byteBuffer.put(new byte[chunkPadding]);
        }
        byteBuffer.flip();

        return byteBuffer.array();
    }

}
