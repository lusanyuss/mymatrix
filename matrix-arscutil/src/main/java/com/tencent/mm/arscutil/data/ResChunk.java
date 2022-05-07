

package com.tencent.mm.arscutil.data;


/**
 * chunk翻译为中文就是“块、部分(尤指大部分，一大块)”的意思，
 * 例如：一棵树，
 * 可以分为三个chunk(部分)：树冠、树茎、树根。
 * 也可以将一棵树视为一个chunk，这个chunk就是这棵树。
 */

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
