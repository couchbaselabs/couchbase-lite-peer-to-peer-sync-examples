package com.couchbase.android.listsync.util;

import android.util.Log;
import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    public int size() {
        synchronized (content) { return content.size(); }
    }

    public V get(K key) {
        synchronized (content) { return content.get(key); }
    }

    public void put(K key, V value) {
        final Set<K> keySet;
        synchronized (content) {
            content.put(key, value);
            keySet = getKeysLocked();
        }
        notifyObservers(keySet);
    }

    public void replace(K key, V value) {
        final Set<K> keySet;
        synchronized (content) {
            if (!content.containsKey(key)) { return; }
            content.remove(key);
            content.put(key, value);
            keySet = getKeysLocked();
        }
        notifyObservers(keySet);
    }

    public V remove(K key) {
        final V value;
        Set<K> keySet = null;
        synchronized (content) {
            final boolean found = content.containsKey(key);
            value = content.remove(key);
            if (found) { keySet = getKeysLocked(); }
        }
        if (keySet != null) { notifyObservers(keySet); }
        return value;
    }

    private void notifyObservers(@NonNull Set<K> keySet) {
        synchronized (observers) {
            for (ObservableEmitter<Set<K>> obs: observers) {
                if (obs != null) { obs.onNext(keySet); }
            }
        }
    }

    private Set<K> getKeysLocked() { return new HashSet<>(content.keySet()); }
}
