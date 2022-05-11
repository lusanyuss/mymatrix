

package com.youku.apm.arscutil.data;


public abstract class ResChunk {

    protected long start; // chunk开始位置

    protected short type; // 类型, 2 bytes
    protected short headSize; // 头大小, 2 bytes
    protected int chunkSize; // chunk大小, 4 bytes

    //header和chunk都要求4字节对齐，不够在后面补0
    protected int headPadding;    //头尾部padding
    protected int chunkPadding;  //chunk尾部padding

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public short getHeadSize() {
        return headSize;
    }

    public void setHeadSize(short headSize) {
        this.headSize = headSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public int getHeadPadding() {
        return headPadding;
    }

    public void setHeadPadding(int headPadding) {
        this.headPadding = headPadding;
    }

    public int getChunkPadding() {
        return chunkPadding;
    }

    public void setChunkPadding(int chunkPadding) {
        this.chunkPadding = chunkPadding;
    }

    public abstract byte[] toBytes();

}
