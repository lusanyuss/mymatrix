

package com.tencent.matrix.batterycanary.utils;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class LinkStartTest {
    static final String TAG = "Matrix.test.LinkStartTest";

    Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void shutDown() {
    }

    @Test
    public void testLinking() {
        LinkStart link = new LinkStart(new LinkStart.NanoTicker());
        link.start();
        link.end(link.insert("node_1"));
        link.end(link.enter("node_2"));
        LinkStart.Session sessionNode3 = link.enter("node_3");
        link.end(link.insert("node_3_sub_1"));
        link.end(link.enter("node_3_sub_2"));
        link.end(sessionNode3);
        LinkStart.Session sessionNode4 = link.enter("node_4");
        link.end(link.insert("node_4_sub_1"));
        LinkStart.Session sessionsNode4Sub2 = link.enter("node_4_sub_2");
        link.end(link.insert("node_4_sub_2_sub_1"));
        link.end(link.enter("node_4_sub_2_sub_2"));
        link.end(sessionsNode4Sub2);
        link.end(link.enter("node_4_sub_3"));
        link.end(sessionNode4);
        link.end(link.enter("node_5"));
        link.finish();
        Assert.assertNotNull(link.toString());
    }
}

