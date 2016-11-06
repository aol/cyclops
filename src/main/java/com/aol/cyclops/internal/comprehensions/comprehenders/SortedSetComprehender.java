package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Deque;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.BaseStream;

import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.types.extensability.Comprehender;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SortedSetComprehender implements Comprehender<SortedSet> {

    @Override
    public Object map(SortedSet t, Function fn) {
        return SortedSetX.fromIterable(t).map(fn);

    }

    @Override
    public Object flatMap(SortedSet t, Function fn) {
        return SortedSetX.fromIterable(t).flatMap(fn);
    }

    @Override
    public SortedSet of(Object o) {
        return SortedSetX.of(o);
    }

    @Override
    public SortedSet fromIterator(Iterator o) {
        Iterable it = () -> o;
        return SortedSetX.fromIterable(it);
    }

    @Override
    public SortedSet empty() {
        return SortedSetX.empty();
    }

    @Override
    public Class getTargetClass() {
        return SortedSet.class;
    }

    @Override
    public Object executeflatMap(SortedSet t, Function fn) {
        return flatMap(t, input -> unwrapOtherMonadTypesLC(this, fn.apply(input)));
    }

    @Override
    public SortedSet unwrap(Object o) {
        if (o instanceof Deque)
            return (SortedSet) o;
        else
            return SortedSetX.fromIterable(() -> ((BaseStream) o).iterator());
    }

    private SortedSet unwrapOtherMonadTypesLC(Comprehender comp, Object apply) {

        return Helper.<SortedSet> unwrapOtherMonadTypesLC(comp, apply, SortedSetX::fromIterable);

    }

}
