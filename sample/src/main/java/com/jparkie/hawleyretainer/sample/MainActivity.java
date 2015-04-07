package com.jparkie.hawleyretainer.sample;

import android.os.Bundle;
import android.widget.TextView;

import com.jparkie.hawleyretainer.HawleyRetain;
import com.jparkie.hawleyretainer.HawleyRetainer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int DATA_DELAY = 2500; // 2500 Milliseconds.

    private TextView mNumberTextView;

    @HawleyRetain
    Observable<Integer> mDataObservable;
    Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumberTextView = (TextView)findViewById(R.id.numberTextView);

        if (mDataObservable == null) {
            mDataObservable = Observable.just(mDataSource.getAndIncrement())
                    .delay(DATA_DELAY, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache();
        }

        if (mSubscription == null) {
            mSubscription = mDataObservable.subscribe(new Action1<Integer>() {
                @Override
                public void call(Integer integer) {
                    mNumberTextView.setText("Number: " + integer);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }
}
