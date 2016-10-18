package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.extensability.Comprehender;

public class ListComprehender implements Comprehender<List> {
    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final List apply) {
        final List list = (List) apply.stream()
                                      .collect(Collectors.toCollection(MaterializedList::new));
        return list.size() > 0 ? comp.of(list) : comp.empty();
    }

    @Override
    public Class getTargetClass() {
        return List.class;
    }

    @Override
    public Object filter(final List t, final Predicate p) {
        return ListX.fromIterable(t)
                    .filter(p);

    }

    @Override
    public Object map(final List t, final Function fn) {
        return ListX.fromIterable(t)
                    .map(fn);

    }

    @Override
    public Object executeflatMap(final List t, final Function fn) {
        return flatMap(t, input -> unwrapOtherMonadTypesLC(this, fn.apply(input)));
    }

    @Override
    public Object flatMap(final List t, final Function fn) {
        return ListX.fromIterable((Iterable) t)
                    .flatMap(fn);

    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return apply instanceof List;
    }

    @Override
    public List empty() {
        return Arrays.asList();
    }

    @Override
    public List of(final Object o) {
        return Arrays.asList(o);
    }

    @Override
    public List fromIterator(final Iterator it) {
        final List list = new ArrayList();
        for (final Object next : (Iterable) () -> it) {
            list.add(next);
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public List unwrap(final Object o) {
        if (o instanceof List)
            return (List) o;
        else
            return (List) ((Stream) o).collect(Collectors.toList());
    }

    static List unwrapOtherMonadTypesLC(final Comprehender comp, final Object apply) {

        if (apply instanceof Collection) {
            return ListX.fromIterable((Collection) apply);
        }
        if (apply instanceof Iterable) {

            return ListX.fromIterable((Iterable) apply);
        }

        if (apply instanceof BaseStream) {

            return ListX.fromIterable(() -> ((BaseStream) apply).iterator());

        }
        final Object o = Comprehender.unwrapOtherMonadTypes(comp, apply);
        if (o instanceof Collection) {
            return ListX.fromIterable((Collection) o);
        }
        if (o instanceof Iterable) {
            return ListX.fromIterable((Iterable) o);
        }
        if (o instanceof BaseStream) {
            return ListX.fromIterable(() -> ((BaseStream) o).iterator());
        }
        return (List) o;

    }

}
