package com.jparkie.hawleyretainer.internal;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class RetainerFragmentMap extends Fragment implements Map<String, Object> {
    public static final String TAG = RetainerFragmentMap.class.getSimpleName();

    public RetainerFragmentMap() {}

    public static <T> RetainerFragmentMap findOrCreateRetainerFragmentMap(T target, Activity activity) {
        if (target == null) {
            throw new NullPointerException(TAG + ": Provided target argument cannot be null.");
        }
        if (activity == null) {
            throw new NullPointerException(TAG + ": Provided activity argument cannot be null.");
        }

        final String uniqueRetainFragmentTag = target.getClass().getSimpleName() + ":" + activity.getLocalClassName() + ":" + TAG;
        final FragmentManager fragmentManager = activity.getFragmentManager();

        RetainerFragmentMap retainerFragmentMap = (RetainerFragmentMap)fragmentManager.findFragmentByTag(uniqueRetainFragmentTag);
        if (retainerFragmentMap == null) {
            retainerFragmentMap = new RetainerFragmentMap();

            fragmentManager.beginTransaction()
                    .add(retainerFragmentMap, uniqueRetainFragmentTag)
                    .commitAllowingStateLoss();
        }

        return retainerFragmentMap;
    }

    private Map<String, Object> mObjectMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mObjectMap.clear();
    }

    @Override
    public void clear() {
        mObjectMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return mObjectMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mObjectMap.containsValue(value);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return mObjectMap.entrySet();
    }

    @Override
    public Object get(Object key) {
        return mObjectMap.get(key);
    }

    @Override
    public boolean isEmpty() {
        return mObjectMap.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return mObjectMap.keySet();
    }

    @Override
    public Object put(String key, Object value) {
        return mObjectMap.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        mObjectMap.putAll(map);
    }

    @Override
    public Object remove(Object key) {
        return mObjectMap.remove(key);
    }

    @Override
    public int size() {
        return mObjectMap.size();
    }

    @Override
    public Collection<Object> values() {
        return mObjectMap.values();
    }
}