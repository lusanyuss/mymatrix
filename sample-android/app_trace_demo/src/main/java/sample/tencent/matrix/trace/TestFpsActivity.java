

package sample.tencent.matrix.trace;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.trace.TracePlugin;
import com.tencent.matrix.trace.constants.Constants;
import com.tencent.matrix.trace.listeners.IDoFrameListener;
import com.tencent.matrix.trace.tracer.FrameTracer;
import com.tencent.matrix.util.MatrixLog;

import java.util.Random;
import java.util.concurrent.Executor;

import sample.tencent.matrix.R;
import sample.tencent.matrix.issue.IssueFilter;

/**

 */

public class TestFpsActivity extends Activity {
    private static final String TAG = "Matrix.TestFpsActivity";
    private ListView mListView;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private static HandlerThread sHandlerThread = new HandlerThread("test");
    private FrameTracer mFrameTracer;

    static {
        sHandlerThread.start();
    }

    private int count;
    private long time = System.currentTimeMillis();
    private IDoFrameListener mDoFrameListener = new IDoFrameListener(new Executor() {
        Handler handler = new Handler(sHandlerThread.getLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }) {

        @Override
        public void doFrameAsync(String focusedActivity, long startNs, long endNs, int dropFrame, boolean isVsyncFrame, long intendedFrameTimeNs, long inputCostNs, long animationCostNs, long traversalCostNs) {
            super.doFrameAsync(focusedActivity, startNs, endNs, dropFrame, isVsyncFrame, intendedFrameTimeNs, inputCostNs, animationCostNs, traversalCostNs);
            MatrixLog.i(TAG, "[doFrameAsync]" + " costMs=" + (endNs - intendedFrameTimeNs) / Constants.TIME_MILLIS_TO_NANO
                    + " dropFrame=" + dropFrame + " isVsyncFrame=" + isVsyncFrame + " offsetVsync=" + ((startNs - intendedFrameTimeNs) / Constants.TIME_MILLIS_TO_NANO) + " [%s:%s:%s]", inputCostNs, animationCostNs, traversalCostNs);
        }

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_fps_layout);

        IssueFilter.setCurrentFilter(IssueFilter.ISSUE_TRACE);
        mFrameTracer = Matrix.with().getPluginByClass(TracePlugin.class).getFrameTracer();
        mFrameTracer.addListener(mDoFrameListener);

        time = System.currentTimeMillis();
        mListView = (ListView) findViewById(R.id.list_view);
        String[] data = new String[200];
        for (int i = 0; i < 200; i++) {
            data[i] = "MatrixTrace:" + i;
        }
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MatrixLog.i(TAG, "onTouch=" + motionEvent);
                SystemClock.sleep(80);
                return false;
            }
        });
        mListView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, data) {
            Random random = new Random();

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MatrixLog.i(TAG, "[onDestroy] count:" + count + " time:" + (System.currentTimeMillis() - time) + "");
        mFrameTracer.removeListener(mDoFrameListener);
    }
}
