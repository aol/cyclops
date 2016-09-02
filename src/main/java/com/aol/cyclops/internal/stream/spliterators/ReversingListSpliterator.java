package com.aol.cyclops.internal.stream.spliterators;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ReversingListSpliterator<T> implements Spliterator<T>, ReversableSpliterator {

    private final List<T> list;
    private ListIterator<T> it;
    @Getter
    @Setter
    private boolean reverse = false;

    public ReversingListSpliterator(List<T> elements, boolean reverse) {
        this.list = elements;
        this.reverse = reverse;
        this.it = elements.listIterator();

    }

    public ReversingListSpliterator<T> invert() {
        setReverse(!isReverse());
        it = list.listIterator(list.size());
        return this;
    }

    public ReversableSpliterator copy() {
        return new ReversingListSpliterator(
                                            list, reverse);

    }

    @Override
    public long estimateSize() {
        return list.size();
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);

        if (!reverse) {
            if (it.hasNext()) {
                action.accept(it.next());
                return true;
            }

        } else {
            if (it.hasPrevious()) {
                action.accept(it.previous());
                return true;
            }

        }
        return false;

    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }

}
