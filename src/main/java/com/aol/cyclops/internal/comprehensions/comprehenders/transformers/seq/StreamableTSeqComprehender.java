package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;
import com.aol.cyclops.util.stream.Streamable;

public class StreamableTSeqComprehender implements Comprehender<StreamableTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(Comprehender comp, StreamableTSeq apply) {
        List list = (List) apply.stream()
                                .collect(Collectors.toCollection(MaterializedList::new));
        return list.size() > 0 ? comp.of(list) : comp.empty();
    }

    @Override
    public Object filter(StreamableTSeq t, Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(StreamableTSeq t, Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(StreamableTSeq t, Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public StreamableTSeq of(Object o) {
        return StreamableTSeq.of(Streamable.of(o));
    }

    @Override
    public StreamableTSeq empty() {
        return StreamableTSeq.emptyStreamable();
    }

    @Override
    public Class getTargetClass() {
        return StreamableTSeq.class;
    }

    @Override
    public StreamableTSeq fromIterator(Iterator o) {
        return StreamableTSeq.of(Streamable.fromIterable(() -> o));
    }

}
