package com.jparkie.hawleyretainer.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.jparkie.hawleyretainer.HawleyRetain;
import com.jparkie.hawleyretainer.HawleyRetainer;

import java.util.concurrent.atomic.AtomicInteger;

public class BaseActivity extends ActionBarActivity {
    public static final String TAG = BaseActivity.class.getSimpleName();

    @HawleyRetain
    AtomicInteger mDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HawleyRetainer.restoreRetainedObjectMap(this, this);

        if (mDataSource == null) {
            mDataSource = new AtomicInteger(1);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        HawleyRetainer.saveRetainedObjectMap(this, this);
    }
}
