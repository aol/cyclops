package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Optional;
import java.util.function.Function;

import com.aol.cyclops.control.FluentFunctions;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class ReaderComprehender implements ValueComprehender<Reader> {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Reader apply) {
        return comp.of(apply);
    }

    @Override
    public Object map(final Reader t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final Reader t, final Function fn) {
        return t.flatMap(r -> fn.apply(r));
    }

    @Override
    public Reader of(final Object o) {
        return FluentFunctions.of(i -> o);
    }

    @Override
    public Reader empty() {
        return FluentFunctions.of(i -> Optional.empty());
    }

    @Override
    public Class getTargetClass() {
        return Reader.class;
    }

}
