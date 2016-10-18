package com.aol.cyclops.control.monads.transformers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.control.monads.transformers.seq.ReaderTSeq;
import com.aol.cyclops.control.monads.transformers.values.ReaderTValue;
import com.aol.cyclops.types.MonadicValue;

public interface ReaderT<T, R> extends Publisher<T> {

    /**
     * @return The wrapped AnyM
     */
    public AnyM<Reader<T, R>> unwrap();

    public ReaderTValue<T, R> peek(Consumer<? super R> peek);

    public ReaderT<T, R> filter(Predicate<? super R> test);

    public <B> ReaderT<T, B> map(Function<? super R, ? extends B> f);

    default <B> ReaderT<T, B> bind(Function<? super R, ReaderT<? extends T, B>> f) {

        return of(unwrap().bind(reader -> reader.flatMap(r -> f.apply(r)
                                                               .unwrap()
                                                               .unwrap())));

    }

    public <B> ReaderT<B, R> flatMap(Function<? super T, ? extends Reader<? extends B, R>> f);

    public static <T, U, R> Function<ReaderT<T, U>, ReaderT<T, R>> lift(Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    public static <T, U1, U2, R> BiFunction<ReaderT<T, U1>, ReaderT<T, U2>, ReaderT<T, R>> lift2(BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    public static <A, R> ReaderT<A, R> of(AnyM<Reader<A, R>> monads) {
        return (ReaderT<A, R>) Matchables.anyM(monads)
                                         .visit(v -> ReaderTValue.<A, R> of(v), s -> ReaderTSeq.<A, R> of(s));

    }

    public static <A, R> ReaderTSeq<A, R> fromIterable(Iterable<Reader<A, R>> iterableOfEvals) {
        return ReaderTSeq.of(AnyM.fromIterable(iterableOfEvals));
    }

    public static <A, R> ReaderTSeq<A, R> fromStream(Stream<Reader<A, R>> streamOfEvals) {
        return ReaderTSeq.of(AnyM.fromStream(streamOfEvals));
    }

    public static <A, R> ReaderTSeq<A, R> fromPublisher(Publisher<Reader<A, R>> publisherOfEvals) {
        return ReaderTSeq.of(AnyM.fromPublisher(publisherOfEvals));
    }

    public static <A, R, V extends MonadicValue<Reader<A, R>>> ReaderTValue<A, R> fromValue(V monadicValue) {
        return ReaderTValue.fromValue(monadicValue);
    }

    public static <A, R> ReaderTValue<A, R> fromOptional(Optional<Reader<A, R>> optional) {
        return ReaderTValue.of(AnyM.fromOptional(optional));
    }

    public static <A, R> ReaderTValue<A, R> fromFuture(CompletableFuture<Reader<A, R>> future) {
        return ReaderTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A, R> ReaderTValue<A, R> fromIterableValue(Iterable<Reader<A, R>> iterableOfEvals) {
        return ReaderTValue.of(AnyM.fromIterableValue(iterableOfEvals));
    }

}
