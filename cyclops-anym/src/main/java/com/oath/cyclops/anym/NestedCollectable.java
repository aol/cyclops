package com.oath.cyclops.anym;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.collections.extensions.IndexedSequenceX;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.monads.AnyM;
import cyclops.control.Future;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.SetX;


/**
 * Interface for manipulating monads nested inside monad transformers
 *
 * @author johnmcclean
 *
 * @param <T> Data type of the elements in the nested Monad
 */
@Deprecated //use cyclops-pure Do instead
public interface NestedCollectable<W extends WitnessType<W>,T> {
    public AnyM<W,? extends IterableX<T>> nestedCollectables();

    default ListT<W,T> listT(Function<? super Iterable<T>,? extends IndexedSequenceX<T>> toList) {
        if (this instanceof ListT)
            return (ListT) this;
        final AnyM<W,? extends IndexedSequenceX<T>> anyM = nestedCollectables().map(s -> toList.apply(s));
        return ListT.of(anyM);
    }

    default <R> FutureT<W,R> futureT(Function<? super Iterable<T>,? extends R> fn,Executor exec) {
        final AnyM<W,Future<R>> anyM = nestedCollectables().map(s -> Future.of(()->fn.apply(s),exec));
        return FutureT.of(anyM);
    }


    default <R, A> AnyM<W,R> collect(final Collector<? super T, A, R> collector) {
        return nestedCollectables().map(s -> s.collect(collector));
    }


    default <R, A, C extends Collection<R>> C collect(final Supplier<C> supplier, final Collector<? super T, A, R> collector) {
        return nestedCollectables().map(s -> s.collect(collector))
                                   .collect(Collectors.toCollection(supplier));
    }

    default AnyM<W,Boolean> allMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.allMatch(c));
    }


    default AnyM<W,Boolean> anyMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.anyMatch(c));
    }


    default AnyM<W,Boolean> noneMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.noneMatch(c));
    }


    default ListX<ListX<T>> toListOfLists() {
        return nestedCollectables().stream()
                                   .map(s -> s.collect(ListX.listXCollector()))
                                   .to(ReactiveConvertableSequence::converter)
                                   .listX();
    }


    default SetX<SetX<T>> toSetOfSets() {
        return nestedCollectables().stream()
                                   .map(s -> s.collect(SetX.setXCollector()))
                                   .to(ReactiveConvertableSequence::converter)
                                   .setX();
    }


    default AnyM<W,ListX<T>> toNestedListX() {
        return nestedCollectables().map(s -> s.collect(ListX.listXCollector()));
    }


    default AnyM<W,SetX<T>> toNestedSetX() {
        return nestedCollectables().map(s -> s.collect(SetX.setXCollector()));
    }

}
