package com.aol.cyclops.internal.stream;

import com.aol.cyclops.internal.stream.spliterators.ReversableSpliterator;
import com.aol.cyclops.internal.stream.spliterators.push.PushingSpliterator;
import cyclops.stream.ReactiveSeq;

import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;


public class ReactiveSeqImpl<T> extends BaseExtendedStream<T> {

    public ReactiveSeqImpl(Stream<T> stream) {
        super(stream);
    }

    public ReactiveSeqImpl(Spliterator<T> stream, Optional<ReversableSpliterator> rev, Optional<PushingSpliterator<?>> split) {
        super(stream, rev, split);
    }

    public ReactiveSeqImpl(Stream<T> stream, Optional<ReversableSpliterator> rev, Optional<PushingSpliterator<?>> split) {
        super(stream, rev, split);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Stream<X> stream, Optional<ReversableSpliterator> reversible, Optional<PushingSpliterator<?>> split) {
        return new ReactiveSeqImpl<X>(stream,reversible,split);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Spliterator<X> stream, Optional<ReversableSpliterator> reversible, Optional<PushingSpliterator<?>> split) {
        return new ReactiveSeqImpl<X>(stream,reversible,split);
    }
}
