package com.couchbase.android.listsync.util;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.reactivex.ObservableEmitter;


public class ObservableMap<K, V> {
    private final Map<K, V> content = new HashMap<>();
    private final Set<ObservableEmitter<Set<K>>> observers = Collections.newSetFromMap(new WeakHashMap<>());

    public void addObserver(ObservableEmitter<Set<K>> observer) {
        synchronized (observers) { observers.add(observer); }
    }

    public boolean isObservedBy(ObservableEmitter<Set<K>> observer) {
        synchronized (observers) { return observers.contains(observer); }
    }

    public void removeObserver(ObservableEmitter<Set<K>> observer) {
        synchronized (observers) { observers.remove(observer); }
    }

    public void put(K key, V value) {
        final Set<K> keySet;
        synchronized (content) {
            content.put(key, value);
            keySet = content.keySet();
        }
        notifyObservers(keySet);
    }

    public V remove(K key) {
        final V value;
        final Set<K> keySet;
        synchronized (content) {
            value = content.remove(key);
            keySet = content.keySet();
        }
        notifyObservers(keySet);
        return value;
    }

    private void notifyObservers(@NonNull Set<K> keySet) {
        synchronized (observers) {
            for (ObservableEmitter<Set<K>> obs: observers) {
                if (obs != null) { obs.onNext(keySet); }
            }
        }
    }
}
