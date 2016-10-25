package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.extensability.Comprehender;

public class SetComprehender implements Comprehender<Set> {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Set apply) {
        final List list = (List) apply.stream()
                                      .collect(Collectors.toCollection(MaterializedList::new));
        return list.size() > 0 ? comp.of(list) : comp.empty();
    }

    @Override
    public Class getTargetClass() {
        return Set.class;
    }

    @Override
    public Object filter(final Set t, final Predicate p) {
        return SetX.fromIterable(t)
                   .filter(p);

    }

    @Override
    public Object map(final Set t, final Function fn) {
        return SetX.fromIterable(t)
                   .map(fn);
    }

    @Override
    public Object executeflatMap(final Set t, final Function fn) {
        return flatMap(t, input -> unwrapOtherMonadTypesLC(this, fn.apply(input)));
    }

    @Override
    public Object flatMap(final Set t, final Function fn) {
        return SetX.fromIterable((Iterable) t)
                   .flatMap(fn);

    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return apply instanceof Stream;
    }

    @Override
    public Set empty() {
        return Collections.unmodifiableSet(new HashSet());
    }

    @Override
    public Set of(final Object o) {
        return SetX.of(o);

    }

    @Override
    public Set fromIterator(final Iterator it) {
        return SetX.fromIterable(() -> it);
    }

    @Override
    public Set unwrap(final Object o) {
        if (o instanceof Set)
            return (Set) o;
        else
            return (Set) ((Stream) o).collect(Collectors.toSet());
    }

    static Set unwrapOtherMonadTypesLC(final Comprehender comp, final Object apply) {

        if (apply instanceof Collection) {
            return SetX.fromIterable((Collection) apply);
        }
        if (apply instanceof Iterable) {

            return SetX.fromIterable((Iterable) apply);
        }

        if (apply instanceof BaseStream) {

            return SetX.fromIterable(() -> ((BaseStream) apply).iterator());

        }
        final Object o = Comprehender.unwrapOtherMonadTypes(comp, apply);
        if (o instanceof Collection) {
            return SetX.fromIterable((Collection) apply);
        }
        if (o instanceof Iterable) {
            return SetX.fromIterable((Iterable) apply);
        }
        if (apply instanceof BaseStream) {
            return SetX.fromIterable(() -> ((BaseStream) apply).iterator());
        }
        return (Set) o;

    }

}
