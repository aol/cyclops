package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class StreamTSeqComprehender implements Comprehender<StreamTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final StreamTSeq apply) {
        final List list = (List) apply.stream()
                                      .collect(Collectors.toCollection(MaterializedList::new));
        return list.size() > 0 ? comp.of(list) : comp.empty();
    }

    @Override
    public Object filter(final StreamTSeq t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final StreamTSeq t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final StreamTSeq t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public StreamTSeq of(final Object o) {
        return StreamTSeq.of(ReactiveSeq.of(o));
    }

    @Override
    public StreamTSeq empty() {
        return StreamTSeq.emptyStream();
    }

    @Override
    public Class getTargetClass() {
        return StreamTSeq.class;
    }

    @Override
    public StreamTSeq fromIterator(final Iterator o) {
        return StreamTSeq.of(ReactiveSeq.fromIterable(() -> o));
    }

}
