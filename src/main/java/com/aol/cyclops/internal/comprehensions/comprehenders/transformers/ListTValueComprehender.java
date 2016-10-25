package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class ListTValueComprehender implements Comprehender<ListTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final ListTValue apply) {

        return apply.isListPresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final ListTValue t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final ListTValue t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final ListTValue t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public ListTValue of(final Object o) {
        return ListTValue.of(ListX.of(o));
    }

    @Override
    public ListTValue empty() {
        return ListTValue.emptyOptional();
    }

    @Override
    public Class getTargetClass() {
        return ListTValue.class;
    }

    @Override
    public ListTValue fromIterator(final Iterator o) {
        return ListTValue.of(ListX.fromIterable(() -> o));
    }

}
