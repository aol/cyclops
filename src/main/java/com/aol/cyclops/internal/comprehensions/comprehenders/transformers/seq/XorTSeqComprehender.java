package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.seq.XorTSeq;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class XorTSeqComprehender implements Comprehender<XorTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final XorTSeq apply) {
        final List list = (List) apply.stream()
                                      .collect(Collectors.toCollection(MaterializedList::new));
        return list.size() > 0 ? comp.of(list) : comp.empty();
    }

    @Override
    public Object filter(final XorTSeq t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final XorTSeq t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final XorTSeq t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public XorTSeq of(final Object o) {
        return XorTSeq.of(Xor.primary(o));
    }

    @Override
    public XorTSeq empty() {
        return XorTSeq.emptyList();
    }

    @Override
    public Class getTargetClass() {
        return XorTSeq.class;
    }

    @Override
    public XorTSeq fromIterator(final Iterator o) {
        return XorTSeq.of(Xor.fromIterable(() -> o));
    }

}
