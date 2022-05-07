

package com.tencent.sqlitelint.behaviour.alert;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.tencent.sqlitelint.R;

/**
 * 
 */

public abstract class SQLiteLintBaseActivity extends Activity {
    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateView();
    }

    protected void onCreateView() {
        setContentView(R.layout.activity_sqlitelint_base);
        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        int layoutId = getLayoutId();
        assert layoutId != 0;
        layoutInflater.inflate(layoutId, contentLayout);

        if (Build.VERSION.SDK_INT >= 21) {
            mToolBar = (Toolbar) findViewById(R.id.toolbar);
            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackBtnClick();
                }
            });
            Drawable drawable = mToolBar.getLogo();
            if (drawable != null) {
                drawable.setVisible(false, true);
            }
        } else {
            Toast.makeText(this, "SQLiteLint toolbar only support in api level >= 21.", Toast.LENGTH_LONG);
        }
    }

    protected void setTitle(String title) {
        if (Build.VERSION.SDK_INT >= 21) {
            mToolBar.setTitle(title);
        }
    }

    protected void onBackBtnClick() {
        finish();
    }

    protected abstract int getLayoutId();
}
