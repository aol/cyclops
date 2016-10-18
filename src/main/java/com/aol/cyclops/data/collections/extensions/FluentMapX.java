package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.Map;

public interface FluentMapX<K, V> extends Map<K, V> {
    default FluentMapX<K, V> plus(final K key, final V value) {
        put(key, value);
        return this;
    }

    default FluentMapX<K, V> plusAll(final Map<? extends K, ? extends V> map) {
        putAll(map);
        return this;
    }

    default FluentMapX<K, V> minus(final Object key) {
        remove(key);
        return this;
    }

    default FluentMapX<K, V> minusAll(final Collection<?> keys) {
        for (final Object next : keys)
            remove(next);
        return this;
    }

}
