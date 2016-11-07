package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;

import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.types.extensability.Comprehender;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DequeComprehender implements Comprehender<Deque> {
    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Deque apply) {
        final List list = (List) apply.stream()
                                      .collect(Collectors.toCollection(MaterializedList::new));
        return list.size() > 0 ? comp.of(list) : comp.empty();
    }
    
    @Override
    public Object map(Deque t, Function fn) {
        return DequeX.fromIterable(t).map(fn);
    }

    @Override
    public Object flatMap(Deque t, Function fn) {
        return DequeX.fromIterable(t).flatMap(fn);
    }

    @Override
    public Deque of(Object o) {
        return DequeX.of(o);
    }

    @Override
    public Deque fromIterator(Iterator o) {
        Iterable ir = () -> o;
        return DequeX.fromIterable(ir);
    }

    @Override
    public Deque empty() {
        return DequeX.empty();
    }

    @Override
    public Class getTargetClass() {
        return Deque.class;
    }

    @Override
    public Object executeflatMap(Deque t, Function fn) {
        return flatMap(t, input -> unwrapOtherMonadTypesLC(this, fn.apply(input)));
    }

    @Override
    public Deque unwrap(Object o) {
        if (o instanceof Deque)
            return (Deque) o;
        else
            return DequeX.fromIterable(() -> ((BaseStream) o).iterator());
    }

    private Deque unwrapOtherMonadTypesLC(Comprehender comp, Object apply) {

        return Helper.<Deque> unwrapOtherMonadTypesLC(comp, apply, DequeX::fromIterable);

    }
}
