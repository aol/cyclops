package com.aol.cyclops2.data.collections.extensions;


import com.aol.cyclops2.data.collections.extensions.persistent.PMapXImpl;
import cyclops.collectionx.immutable.PersistentMapX;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import lombok.NonNull;
import cyclops.data.tuple.Tuple2;
import com.aol.cyclops2.data.collections.extensions.api.PMap;

import java.util.function.Supplier;

public class ExtensiblePMapX<K, V> extends PMapXImpl<K, V> {

    private final Supplier<Reducer<PersistentMapX<K, V>>> reducer;

    public ExtensiblePMapX(@NonNull PMap<K, V> map, @NonNull Supplier<Reducer<PersistentMapX<K, V>>> reducer) {
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
    public PMapXImpl<K, V> withMap(PMap<K, V> map) {
        return super.withMap(map);
    }

}