package com.tencent.matrix.batterycanary;

import android.text.TextUtils;

import androidx.test.platform.app.InstrumentationRegistry;


public class TestUtils {
    public static boolean isAssembleTest() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String outerMethodName = ste[2 + 1].getMethodName();
        return !String.valueOf(InstrumentationRegistry.getArguments().get("class")).endsWith("#" + outerMethodName);
    }

    public static boolean isLaunchingFrom(String classReference) {
        String testEntry = String.valueOf(InstrumentationRegistry.getArguments().get("class"));
        if (!TextUtils.isEmpty(testEntry))
            if (testEntry.contains("#")) {
                return testEntry.substring(0, testEntry.lastIndexOf("#")).equals(classReference);
            } else {
                return testEntry.equals(classReference);
            }
        return false;
    }
}
