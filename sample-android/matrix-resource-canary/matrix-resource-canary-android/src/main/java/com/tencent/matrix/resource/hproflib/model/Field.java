

package com.tencent.matrix.resource.hproflib.model;



/**
 * A field with an associated type and name.
 */
public final class Field {
    public final int typeId;
    public final ID nameId;
    public final Object staticValue;

    public Field(int typeId, ID nameId, Object staticValue) {
        this.typeId = typeId;
        this.nameId = nameId;
        this.staticValue = staticValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Field)) {
            return false;
        }

        final Field field = (Field) o;

        if (typeId != field.typeId) {
            return false;
        }

        if (!nameId.equals(field.nameId)) {
            return false;
        }

        return (staticValue == null || staticValue.equals(field.staticValue))
                && (field.staticValue == null || field.staticValue.equals(staticValue));
    }

    @Override
    public int hashCode() {
        return (nameId.hashCode() << 31) + typeId;
    }
}

