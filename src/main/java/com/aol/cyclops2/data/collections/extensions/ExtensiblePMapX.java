package com.aol.cyclops2.data.collections.extensions;


import com.aol.cyclops2.data.collections.extensions.persistent.PMapXImpl;
import cyclops.collections.immutable.PMapX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import lombok.NonNull;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PMap;

import java.util.function.Supplier;

public class ExtensiblePMapX<K, V> extends PMapXImpl<K, V> {

    private final Supplier<Reducer<PMapX<K, V>>> reducer;

    public ExtensiblePMapX(@NonNull PMap<K, V> map, @NonNull Supplier<Reducer<PMapX<K, V>>> reducer) {
        super(
                map);
        this.reducer = reducer;

    }

    public PMapX<K, V> fromStream(final ReactiveSeq<Tuple2<K, V>> stream) {
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