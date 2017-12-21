package com.oath.cyclops.data.collections.extensions;


import com.oath.cyclops.data.collections.extensions.persistent.PMapXImpl;
import com.oath.cyclops.types.persistent.PersistentMap;
import cyclops.reactive.collections.immutable.PersistentMapX;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import lombok.NonNull;
import cyclops.data.tuple.Tuple2;

import java.util.function.Supplier;

public class ExtensiblePMapX<K, V> extends PMapXImpl<K, V> {

    private final Supplier<Reducer<PersistentMapX<K, V>,Tuple2<K,V>>> reducer;

    public ExtensiblePMapX(@NonNull PersistentMap<K, V> map, @NonNull Supplier<Reducer<PersistentMapX<K, V>,Tuple2<K,V>>> reducer) {
        super(
                map);
        this.reducer = reducer;

    }

    public PersistentMapX<K, V> fromStream(final ReactiveSeq<Tuple2<K, V>> stream) {
        return stream.mapReduce(reducer.get());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PMapXImpl#withMap(org.pcollections.PMap)
     */
    @Override
    public PMapXImpl<K, V> withMap(PersistentMap<K, V> map) {
        return super.withMap(map);
    }

}
