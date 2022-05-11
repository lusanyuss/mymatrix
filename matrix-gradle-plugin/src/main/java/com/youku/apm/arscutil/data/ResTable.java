

package com.youku.apm.arscutil.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class ResTable extends ResChunk {

    private int packageCount; // 资源package数目, 4 bytes
    private ResStringBlock globalStringPool; // 全局资源池
    private ResPackage[] packages; // 资源package数组

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
