

package com.tencent.matrix.resource.analyzer.model;

import java.lang.ref.WeakReference;
import java.util.EnumSet;



public enum AndroidExcludedBmpRefs {

    EXCLUDE_GCROOT_WITH_SYSTEM_CLASS() {
        @Override
        void config(ExcludedBmps.Builder builder) {
            builder.addClassNamePattern("^android\\..*", true);
            builder.addClassNamePattern("^com\\.android\\..*", true);
        }
    },

    EXCLUDE_WEAKREFERENCE_HOLDER() {
        @Override
        void config(ExcludedBmps.Builder builder) {
            builder.instanceField(WeakReference.class.getName(), "referent");
        }
    };

    public static ExcludedBmps.Builder createDefaults() {
        ExcludedBmps.Builder builder = ExcludedBmps.builder();
        for (AndroidExcludedBmpRefs item : EnumSet.allOf(AndroidExcludedBmpRefs.class)) {
            item.config(builder);
        }
        return builder;
    }

    abstract void config(ExcludedBmps.Builder builder);
}
