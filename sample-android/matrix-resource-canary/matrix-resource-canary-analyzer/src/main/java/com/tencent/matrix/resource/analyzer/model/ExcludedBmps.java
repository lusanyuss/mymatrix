

package com.tencent.matrix.resource.analyzer.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;



public final class ExcludedBmps extends ExcludedRefs {
    public final Set<PatternInfo> mClassNamePatterns;

    public static final class PatternInfo {
        public Pattern mPattern;
        public boolean mForGCRootOnly;

        PatternInfo(Pattern pattern, boolean forGCRootOnly) {
            mPattern = pattern;
            mForGCRootOnly = forGCRootOnly;
        }
    }

    ExcludedBmps(BuilderWithParams builder) {
        super(builder);
        mClassNamePatterns = Collections.unmodifiableSet(builder.mClassNamePatterns);
    }

    public static Builder builder() {
        return new BuilderWithParams();
    }

    public interface Builder extends ExcludedRefs.Builder {
        Builder addClassNamePattern(String regex, boolean forGCRootOnly);
        ExcludedBmps build();
    }

    static final class BuilderWithParams extends ExcludedRefs.BuilderWithParams implements Builder {
        final Set<PatternInfo> mClassNamePatterns = new HashSet<>();

        @Override
        public Builder addClassNamePattern(String regex, boolean forGCRootOnly) {
            if (regex == null || regex.length() == 0) {
                throw new IllegalArgumentException("bad regex: " + regex);
            }
            mClassNamePatterns.add(new PatternInfo(Pattern.compile(regex), forGCRootOnly));
            return this;
        }

        @Override
        public ExcludedBmps build() {
            return new ExcludedBmps(this);
        }
    }
}
