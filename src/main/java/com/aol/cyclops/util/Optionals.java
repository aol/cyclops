package com.aol.cyclops.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Value;

public class Optionals {

    public static <T> Optional<ListX<T>> sequence(final CollectionX<Optional<T>> opts) {
        return sequence(opts.stream()).map(s -> s.toListX());

    }

    public static <T> Optional<ListX<T>> sequencePresent(final CollectionX<Optional<T>> opts) {
        final Optional<ListX<T>> unwrapped = AnyM.sequence(opts.map(o -> AnyM.fromOptional(o)))
                                                 .unwrap();
        return unwrapped;
    }

    public static <T> Optional<ReactiveSeq<T>> sequence(final Stream<Optional<T>> opts) {
        return AnyM.sequence(opts.map(f -> AnyM.fromOptional(f)), () -> AnyM.fromOptional(Optional.of(Stream.<T> empty())))
                   .map(s -> ReactiveSeq.fromStream(s))
                   .unwrap();

    }

    public static <T, R> Optional<R> accumulatePresent(final CollectionX<Optional<T>> maybes, final Reducer<R> reducer) {
        return sequencePresent(maybes).map(s -> s.mapReduce(reducer));
    }

    public static <T, R> Optional<R> accumulatePresent(final CollectionX<Optional<T>> maybes, final Function<? super T, R> mapper,
            final Semigroup<R> reducer) {
        return sequencePresent(maybes).map(s -> s.map(mapper)
                                                 .reduce(reducer.reducer())
                                                 .get());
    }

    public static <T1, T2, R> Optional<R> combine(final Optional<? extends T1> f, final Value<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Maybe.fromOptional(f)
                           .combine(v, fn)
                           .toOptional());
    }

    public static <T1, T2, R> Optional<R> zip(final Optional<? extends T1> f, final Iterable<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Maybe.fromOptional(f)
                           .zip(v, fn)
                           .toOptional());
    }

    public static <T1, T2, R> Optional<R> zip(final Publisher<? extends T2> p, final Optional<? extends T1> f,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Maybe.fromOptional(f)
                           .zip(fn, p)
                           .toOptional());
    }

    public static <T> Optional<T> narrow(final Optional<? extends T> f) {
        return (Optional<T>) f;
    }

}
