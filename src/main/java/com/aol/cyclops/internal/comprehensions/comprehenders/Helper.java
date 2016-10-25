package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.BaseStream;

import com.aol.cyclops.types.extensability.Comprehender;

public class Helper {

    public static <T> T first(Comprehender<T> comp, Collection c) {
        Iterator<T> it = c.iterator();
        if (it.hasNext())
            return comp.of(it.next());
        return comp.empty();
    }

    static <T> T unwrapOtherMonadTypesLC(Comprehender comp, Object apply, Function<Iterable, T> f) {

        if (apply instanceof Collection) {
            return f.apply((Collection) apply);
        }
        if (apply instanceof Iterable) {
            return f.apply((Iterable) apply);
        }

        if (apply instanceof BaseStream) {
            return f.apply((() -> ((BaseStream) apply).iterator()));

        }
        Object o = Comprehender.unwrapOtherMonadTypes(comp, apply);
        if (o instanceof Collection) {
            return f.apply((Collection) o);
        }
        if (o instanceof Iterable) {
            return f.apply((Iterable) o);
        }
        if (o instanceof BaseStream) {
            return f.apply(() -> ((BaseStream) o).iterator());
        }
        return (T) o;

    }
}
