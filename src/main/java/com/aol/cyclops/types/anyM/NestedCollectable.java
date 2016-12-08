package com.aol.cyclops.types.anyM;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
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
public interface NestedCollectable<W extends WitnessType,T> {
    public AnyM<? extends CyclopsCollectable<T>> nestedCollectables();

    /**
     * @return Convert to a Stream Transformer
     */
    default StreamT<T> streamT() {
        if (this instanceof StreamT)
            return (StreamT) this;
        final AnyM<ReactiveSeq<T>> anyM = nestedCollectables().map(s -> ReactiveSeq.fromIterable(s));
        return Matchables.anyM(anyM)
                         .visit(v -> StreamT.fromValue(v.toEvalLater()), s -> StreamTSeq.of(s));
    }

    /**
     * @return Convert to a List Transformer
     */
    default ListT<T> listT() {
        if (this instanceof ListT)
            return (ListT) this;
        final AnyM<ListX<T>> anyM = nestedCollectables().map(s -> ListX.fromIterable(s));
        return Matchables.anyM(anyM)
                         .visit(v -> ListT.fromValue(v.toEvalLater()), s -> ListT.of(s));
    }

    /**
     * @return Convert to a Set Transformer
     */
    default SetT<T> setT() {
        if (this instanceof SetT)
            return (SetT) this;
        final AnyM<SetX<T>> anyM = nestedCollectables().map(s -> SetX.fromIterable(s));
        return Matchables.anyM(anyM)
                         .visit(v -> SetT.fromValue(v.toEvalLater()), s -> SetTSeq.of(s));
    }

    /**
     * @return Convert to a Streamable Transformer
     */
    default StreamableT<T> streamableT() {
        if (this instanceof StreamableT)
            return (StreamableT) this;
        final AnyM<Streamable<T>> anyM = nestedCollectables().map(s -> Streamable.fromIterable(s));
        return Matchables.anyM(anyM)
                         .visit(v -> StreamableT.fromValue(v.toEvalLater()), s -> StreamableTSeq.of(s));
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
