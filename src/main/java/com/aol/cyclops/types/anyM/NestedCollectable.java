package com.aol.cyclops.types.anyM;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.FutureT;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.FluentSequenceX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Interface for manipulating monads nested inside monad transformers
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of the elements in the nested Monad
 */
public interface NestedCollectable<W extends WitnessType<W>,T> {
    public AnyM<W,? extends CyclopsCollectable<T>> nestedCollectables();

    
    /**
     * @return Convert to a List Transformer
     */
    default ListT<W,T> listT(Function<? super Iterable<T>,? extends FluentSequenceX<T>> toList) {
        if (this instanceof ListT)
            return (ListT) this;
        final AnyM<W,? extends FluentSequenceX<T>> anyM = nestedCollectables().map(s -> toList.apply(s));
        return ListT.of(anyM);
    }
    
    default <R> FutureT<W,R> futureT(Function<? super Iterable<T>,? extends R> fn,Executor exec) {
        final AnyM<W,FutureW<R>> anyM = nestedCollectables().map(s -> FutureW.ofSupplier(()->fn.apply(s),exec));
        return FutureT.of(anyM);
    }

    
   

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
     */
    default <R, A> AnyM<W,R> collect(final Collector<? super T, A, R> collector) {
        return nestedCollectables().map(s -> s.collect(collector));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
     */
    default <R, A, C extends Collection<R>> C collect(final Supplier<C> supplier, final Collector<? super T, A, R> collector) {
        return nestedCollectables().map(s -> s.collect(collector))
                                   .collect(Collectors.toCollection(supplier));
    }

    /**
     * Each nested value will be true if predicate matches all elements when Monad converted to a Stream
     * Nested analog to {@link ReactiveSeq#allMatch}
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate to check if all match
     */
    default AnyM<W,Boolean> allMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.allMatch(c));
    }

    /**
     * Each nested value will be True if a single element matches when Monad converted to a Stream
     * Nested analog to {@link ReactiveSeq#anyMatch}
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate to check if any match
     */
    default AnyM<W,Boolean> anyMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.anyMatch(c));
    }

    /**
     * Reduce each nested monad to a boolean value - true if the predicates match none of it's elements, otherwise false
     * 
     * @param c  Predicate to check if no match
     * @return Monad of booleans wrapped inside an AnyM
     */
    default AnyM<W,Boolean> noneMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.noneMatch(c));
    }

    /**
     * @return This monad transformer converted to nested Lists
     */
    default ListX<ListX<T>> toListOfLists() {
        return nestedCollectables().stream()
                                   .map(s -> s.collect(ListX.listXCollector()))
                                   .toListX();
    }

    /**
     * @return This monad transformer converted to nested Sets
     */
    default SetX<SetX<T>> toSetOfSets() {
        return nestedCollectables().stream()
                                   .map(s -> s.collect(SetX.setXCollector()))
                                   .toSetX();
    }

    /**
     * @return This monad transformer converted to a ListX nested in an AnyM
     */
    default AnyM<W,ListX<T>> toNestedListX() {
        return nestedCollectables().map(s -> s.collect(ListX.listXCollector()));
    }

    /**
     * @return This monad transformer converted to a SetX nested in an AnyM
     */
    default AnyM<W,SetX<T>> toNestedSetX() {
        return nestedCollectables().map(s -> s.collect(SetX.setXCollector()));
    }

}
