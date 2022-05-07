

package sample.tencent.matrix.resource;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.plugin.Plugin;
import com.tencent.matrix.resource.ResourcePlugin;
import com.tencent.matrix.util.MatrixLog;
import com.tencent.matrix.util.MatrixUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sample.tencent.matrix.R;
import sample.tencent.matrix.issue.IssueFilter;



public class TestLeakActivity extends Activity {
    private static final String TAG = "Matrix.TestLeakActivity";

    private static Set<Activity> testLeaks = new HashSet<>();

    private static ArrayList<Bitmap> bitmaps = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("memory" + MatrixUtil.getProcessName(TestLeakActivity.this), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        testLeaks.add(this);
        Plugin plugin = Matrix.with().getPluginByClass(ResourcePlugin.class);
        if (!plugin.isPluginStarted()) {
            MatrixLog.i(TAG, "plugin-resource start");
            plugin.start();
        }

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
//        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.welcome_bg, options));
        MatrixLog.i(TAG, "test leak activity size: %d, bitmaps size: %d", testLeaks.size(), bitmaps.size());

        setContentView(R.layout.test_leak);

        IssueFilter.setCurrentFilter(IssueFilter.ISSUE_LEAK);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MatrixLog.i(TAG, "test leak activity destroy:" + this.hashCode());

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Runtime.getRuntime().gc();
//                Runtime.getRuntime().runFinalization();
//                Runtime.getRuntime().gc();
//            }
//        }).start();
    }
}
