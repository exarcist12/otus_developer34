package ru.otus.cachehw;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyCache<K, V> implements HwCache<K, V> {
    // Надо реализовать эти методы
    Map<K, V> cache = new WeakHashMap<>();
    HwListener<K, V> listener;
    private final List<HwListener<K, V>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        listeners.forEach(listener -> listener.notify(key, value, "put"));
    }

    @Override
    public void remove(K key) {
        V value = cache.get(key);
        cache.remove(key);
        listeners.forEach(listener -> listener.notify(key, value, "remove"));
    }

    @Override
    public V get(K key) {
        V value = cache.get(key);
        listeners.forEach(listener -> listener.notify(key, value, "get"));
        return value;
    }

    @Override
    public void addListener(HwListener<K, V> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(HwListener<K, V> listener) {
        listeners.remove(listener);
    }
}
