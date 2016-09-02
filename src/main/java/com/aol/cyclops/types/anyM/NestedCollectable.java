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
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.stream.CyclopsCollectable;

public interface NestedCollectable<T> {
    public AnyM<? extends CyclopsCollectable<T>> nestedCollectables();

    default StreamT<T> streamT() {
        if (this instanceof StreamT)
            return (StreamT) this;
        AnyM<ReactiveSeq<T>> anyM = nestedCollectables().map(s -> ReactiveSeq.fromIterable(s));
        return Matchables.anyM(anyM)
                         .visit(v -> StreamT.fromValue(v.toEvalLater()), s -> StreamTSeq.of(s));
    }

    default ListT<T> listT() {
        if (this instanceof ListT)
            return (ListT) this;
        AnyM<ListX<T>> anyM = nestedCollectables().map(s -> ListX.fromIterable(s));
        return Matchables.anyM(anyM)
                         .visit(v -> ListT.fromValue(v.toEvalLater()), s -> ListTSeq.of(s));
    }

    default SetT<T> setT() {
        if (this instanceof SetT)
            return (SetT) this;
        AnyM<SetX<T>> anyM = nestedCollectables().map(s -> SetX.fromIterable(s));
        return Matchables.anyM(anyM)
                         .visit(v -> SetT.fromValue(v.toEvalLater()), s -> SetTSeq.of(s));
    }

    default StreamableT<T> streamableT() {
        if (this instanceof StreamableT)
            return (StreamableT) this;
        AnyM<Streamable<T>> anyM = nestedCollectables().map(s -> Streamable.fromIterable(s));
        return Matchables.anyM(anyM)
                         .visit(v -> StreamableT.fromValue(v.toEvalLater()), s -> StreamableTSeq.of(s));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
     */
    default <R, A> AnyM<R> collect(Collector<? super T, A, R> collector) {
        return nestedCollectables().map(s -> s.collect(collector));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
     */
    default <R, A, C extends Collection<R>> C collect(Supplier<C> supplier, Collector<? super T, A, R> collector) {
        return nestedCollectables().map(s -> s.collect(collector))
                                   .collect(Collectors.toCollection(supplier));
    }

    /**
     * True if predicate matches all elements when Monad converted to a Stream
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate to check if all match
     */
    default AnyM<Boolean> allMatch(Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.allMatch(c));
    }

    /**
     * True if a single element matches when Monad converted to a Stream
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate to check if any match
     */
    default AnyM<Boolean> anyMatch(Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.anyMatch(c));
    }

    default AnyM<Boolean> noneMatch(Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.noneMatch(c));
    }

    default ListX<ListX<T>> toListOfLists() {
        return nestedCollectables().stream()
                                   .map(s -> s.collect(ListX.listXCollector()))
                                   .toListX();
    }

    default SetX<SetX<T>> toSetOfSets() {
        return nestedCollectables().stream()
                                   .map(s -> s.collect(SetX.setXCollector()))
                                   .toSetX();
    }

    default AnyM<ListX<T>> toNestedListX() {
        return nestedCollectables().map(s -> s.collect(ListX.listXCollector()));
    }

    default AnyM<SetX<T>> toNestedSetX() {
        return nestedCollectables().map(s -> s.collect(SetX.setXCollector()));
    }

}
