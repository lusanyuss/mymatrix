package com.tencent.matrix.trace.util;

import android.view.View;
import android.view.ViewGroup;


public class ViewUtil {

    public static ViewInfo dumpViewInfo(View view) {
        ViewInfo info = new ViewInfo();
        traversalViewTree(info, 0, view);
        return info;
    }

    private static void traversalViewTree(ViewInfo info, int deep, View view) {

        if (view == null) {
            return;
        }

        deep++;

        if (deep > info.mViewDeep) {
            info.mViewDeep = deep;
        }

        if (!(view instanceof ViewGroup)) {
            return;
        }

        ViewGroup grp = (ViewGroup) view;
        final int n = grp.getChildCount();

        if (n <= 0) {
            return;
        }

        //        info.mViewCount += N;

        for (int i = 0; i < n; i++) {
            View v = grp.getChildAt(i);
            if (null != v && v.getVisibility() == View.GONE) {
                continue;
            }
            info.mViewCount++;
            traversalViewTree(info, deep, v);
        }

    }

    public static class ViewInfo {
        public int mViewCount = 0;
        public int mViewDeep = 0;
        public String mActivityName = "";

        @Override
        public String toString() {
            return "ViewCount:" + mViewCount + "," + "ViewDeep:" + mViewDeep + "," + "mActivityName:" + mActivityName;
        }
    }

}
