package com.jparkie.hawleyretainer.internal;

import android.app.Activity;

public class Retainer {
    public static final String TAG = Retainer.class.getSimpleName();

    public Retainer() {}

    public static class Object<T> extends Retainer {
        public void restoreRetainedObjectMap(T target, Activity activity) {}

        public void saveRetainedObjectMap(T target, Activity activity) {}
    }
}