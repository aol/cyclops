package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.Map;

public interface FluentMapX<K, V> extends Map<K, V> {
    default FluentMapX<K, V> plus(K key, V value) {
        put(key, value);
        return this;
    }

    default FluentMapX<K, V> plusAll(Map<? extends K, ? extends V> map) {
        putAll(map);
        return this;
    }

    default FluentMapX<K, V> minus(Object key) {
        remove(key);
        return this;
    }

    default FluentMapX<K, V> minusAll(Collection<?> keys) {
        for (Object next : keys)
            remove(next);
        return this;
    }

}
