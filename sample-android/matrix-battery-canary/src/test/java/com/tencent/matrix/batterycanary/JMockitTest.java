

package com.tencent.matrix.batterycanary;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import static org.junit.Assert.assertEquals;

/**
* Example local unit test, which will execute on the development machine (host).
*
* @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
*/
@SuppressWarnings("SpellCheckingInspection")
@RunWith(JMockit.class)
public class JMockitTest {

   public static class JMock extends MockUp<FinalCounter> {
       @Mock
       public static int getCount() {
           return -1;
       }

       @Mock
       public static int getFinalCount() {
           return -2;
       }
   }

   @Test
   public void addition_isCorrect() throws Exception {
       assertEquals(4, 2 + 2);
   }

   @Test
   public void testGetMockCount() {
       Assert.assertEquals(1, FinalCounter.getCount());
       new JMock();
       Assert.assertEquals(-1, FinalCounter.getCount());
   }

   @Test
   public void testGetMockFinalCount() {
       Assert.assertEquals(1, FinalCounter.getFinalCount());
       new JMock();
       Assert.assertEquals(-2, FinalCounter.getFinalCount());
   }

   public static final class FinalCounter {
       public static int getCount() {
           return 1;
       }

       public static int getFinalCount() {
           return 1;
       }
   }
}
