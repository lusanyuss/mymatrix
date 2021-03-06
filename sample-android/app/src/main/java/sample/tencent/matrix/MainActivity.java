

package sample.tencent.matrix;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.matrix.trace.view.FrameDecorator;
import com.tencent.matrix.util.MatrixLog;

import sample.tencent.matrix.battery.TestBatteryActivity;
import sample.tencent.matrix.hooks.TestHooksActivity;
import sample.tencent.matrix.io.TestIOActivity;
import sample.tencent.matrix.issue.IssuesMap;
import sample.tencent.matrix.resource.TestLeakActivity;
import sample.tencent.matrix.sqlitelint.TestSQLiteLintActivity;
import sample.tencent.matrix.trace.TestTraceMainActivity;
import sample.tencent.matrix.traffic.TestTrafficActivity;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Matrix.MainActivity";

    @Override
    protected void onResume() {
        super.onResume();
        IssuesMap.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button testTrace = findViewById(R.id.test_trace);
        testTrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestTraceMainActivity.class);
                startActivity(intent);
            }
        });

        Button testIO = findViewById(R.id.test_io);
        testIO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestIOActivity.class);
                startActivity(intent);
            }
        });

        Button testLeak = findViewById(R.id.test_leak);
        testLeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestLeakActivity.class);
                startActivity(intent);
            }
        });

        Button testSQLiteLint = findViewById(R.id.test_sqlite_lint);
        testSQLiteLint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestSQLiteLintActivity.class);
                startActivity(intent);
            }
        });

        Button testBattery = findViewById(R.id.test_battery);
        testBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestBatteryActivity.class);
                startActivity(intent);
            }
        });

        Button testHooks = findViewById(R.id.test_hooks);
        testHooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestHooksActivity.class);
                startActivity(intent);
            }
        });

        Button testTraffic = findViewById(R.id.test_traffic_enter);
        testTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestTrafficActivity.class);
                startActivity(intent);
            }
        });

    }


}
