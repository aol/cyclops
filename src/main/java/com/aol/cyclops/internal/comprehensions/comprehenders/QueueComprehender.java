package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;

import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.types.extensability.Comprehender;

@SuppressWarnings({ "rawtypes", "unchecked" })
public enum QueueComprehender implements Comprehender<Queue> {
    INSTANCE;
    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Queue apply) {
        final List list = (List) apply.stream()
                                      .collect(Collectors.toCollection(MaterializedList::new));
        return list.size() > 0 ? comp.of(list) : comp.empty();
    }
    @Override
    public Object map(Queue q, Function fn) {
        return QueueX.fromIterable(q).map(fn);
    }

    @Override
    public Object flatMap(Queue q, Function fn) {
        return QueueX.fromIterable(q).flatMap(fn);
    }

    @Override
    public Queue of(Object o) {
        return QueueX.of(o);
    }

    @Override
    public Queue fromIterator(Iterator o) {
        Iterable ir = () -> o;
        return QueueX.fromIterable(ir);
    }

    @Override
    public Queue empty() {
        return QueueX.empty();
    }

    @Override
    public Class getTargetClass() {
        return Queue.class;
    }

    @Override
    public Queue unwrap(Object o) {
        if (o instanceof Queue)
            return (Queue) o;
        else
            return QueueX.fromIterable(() -> ((BaseStream) o).iterator());
    }

    @Override
    public Object executeflatMap(Queue t, Function fn) {
        return flatMap(t, input -> unwrapOtherMonadTypesLC(this, fn.apply(input)));
    }

    private Queue unwrapOtherMonadTypesLC(Comprehender comp, Object apply) {

        return Helper.<Queue> unwrapOtherMonadTypesLC(comp, apply, QueueX::fromIterable);

    }
}
