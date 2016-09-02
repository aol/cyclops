package com.aol.cyclops.internal.stream.operators;

import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WindowStatefullyWhileOperator<T> {
    private static final Object UNSET = new Object();
    private final Stream<T> stream;

    public Stream<ListX<T>> windowStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        Iterator<T> it = stream.iterator();
        return StreamUtils.stream(new Iterator<ListX<T>>() {
            ListX<T> last = ListX.empty();
            T value = (T) UNSET;

            @Override
            public boolean hasNext() {
                return value != UNSET || it.hasNext();
            }

            @Override
            public ListX<T> next() {

                ListX<T> list = ListX.of();
                if (value != UNSET)
                    list.add(value);
                T value;
                while (list.size() == 0 && it.hasNext()) {
                    label: while (it.hasNext()) {
                        value = it.next();
                        list.add(value);

                        if (!predicate.test(last, value)) {
                            value = (T) UNSET;
                            break label;
                        }
                        value = (T) UNSET;

                    }

                }

                return last = list;
            }

        });
    }
}
