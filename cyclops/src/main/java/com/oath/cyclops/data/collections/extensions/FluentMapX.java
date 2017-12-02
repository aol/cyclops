package com.oath.cyclops.data.collections.extensions;

import com.oath.cyclops.types.persistent.PersistentMap;
import cyclops.data.tuple.Tuple2;

import java.util.Collection;
import java.util.Map;

public interface FluentMapX<K, V> extends Map<K, V> {
    default FluentMapX<K, V> plus(final K key, final V value) {
        put(key, value);
        return this;
    }

    default FluentMapX<K, V> plusAll(final PersistentMap<? extends K, ? extends V> map) {

        for(Tuple2<? extends K,? extends V> t2 : map){
            put(t2._1(),t2._2());
        }
        return this;
    }

    default FluentMapX<K, V> plusAll(final Map<? extends K, ? extends V> map) {
        if (map != null && map.size() > 0) {
            map.forEach((key, value) -> {
                put(key, value);
            });
        }
        return this;
    }

    default FluentMapX<K, V> minus(final K key) {
        remove(key);
        return this;
    }

    default FluentMapX<K, V> minusAll(final Collection<? extends K> keys) {
        for (final Object next : keys)
            remove(next);
        return this;
    }

}
