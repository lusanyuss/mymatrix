

package com.tencent.mm.arscutil.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**

 */

public class ResEntry {

    protected short size; // 总大小（包括size字段），2 bytes, 不包含后面紧跟的ResValue或者ResMapValue
    protected short flag; // 标识entry（FLAG_COMPLEX=0X001、FLAG_PUBLIC=0X002），2 bytes
    protected int stringPoolIndex; // 资源名称在资源项名称字符串池中的index，4 bytes

    protected String entryName;

    //FLAG_COMPLEX 为0
    private ResValue resValue;            //紧跟在ResEntry后面，不计入ResEntry的大小

    //FLAG_COMPLEX 为1
    private int parent;                            //4 bytes，parent ResMapEntry
    private int pairCount;                        //4 bytes, pair数目
    private List<ResMapValue> resMapValues;    //紧跟在ResEntry后面，不计入ResEntry的大小


    public short getSize() {
        return size;
    }

    public void setSize(short size) {
        this.size = size;
    }

    public short getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public int getStringPoolIndex() {
        return stringPoolIndex;
    }

    public void setStringPoolIndex(int stringPoolIndex) {
        this.stringPoolIndex = stringPoolIndex;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getPairCount() {
        return pairCount;
    }

    public void setPairCount(int pairCount) {
        this.pairCount = pairCount;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public ResValue getResValue() {
        return resValue;
    }

    public void setResValue(ResValue resValue) {
        this.resValue = resValue;
    }

    public List<ResMapValue> getResMapValues() {
        return resMapValues;
    }

    public void setResMapValues(List<ResMapValue> resMapValues) {
        this.resMapValues = resMapValues;
    }

    public byte[] toBytes() {
        ByteBuffer headBuffer = ByteBuffer.allocate(size);
        headBuffer.order(ByteOrder.LITTLE_ENDIAN);
        headBuffer.clear();
        headBuffer.putShort(size);
        headBuffer.putShort(flag);
        headBuffer.putInt(stringPoolIndex);

        int totalSize = size;

        List<ByteBuffer> content = new ArrayList<ByteBuffer>();

        if ((flag & ArscConstants.RES_TABLE_ENTRY_FLAG_COMPLEX) == 0) {
            ByteBuffer value = ByteBuffer.wrap(resValue.toBytes());
            content.add(value);
        } else {
            headBuffer.putInt(parent);
            headBuffer.putInt(pairCount);
            if (pairCount > 0) {
                for (int i = 0; i < resMapValues.size(); i++) {
                    ByteBuffer value = ByteBuffer.wrap(resMapValues.get(i).toBytes());
                    content.add(value);
                }
            }
        }
        for (ByteBuffer value : content) {
            totalSize += value.limit();
        }
        headBuffer.flip();
        ByteBuffer finalBuffer = ByteBuffer.allocate(totalSize);
        finalBuffer.order(ByteOrder.LITTLE_ENDIAN);
        finalBuffer.clear();
        finalBuffer.put(headBuffer.array());
        for (ByteBuffer value : content) {
            finalBuffer.put(value.array());
        }

        finalBuffer.flip();
        return finalBuffer.array();
    }

}
