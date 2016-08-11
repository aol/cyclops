package com.aol.cyclops;

import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.DequeXImpl;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.ListXImpl;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.QueueXImpl;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.data.collections.extensions.standard.SetXImpl;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetXImpl;

public interface CyclopsCollectors {
    static <T> Collector<T, ?, QueueX<T>> toQueueX() {
        return Collectors.collectingAndThen(QueueX.defaultCollector(), (Queue<T> d) -> new QueueXImpl<>(
                                                                                                        d, QueueX.defaultCollector()));

    }

    static <T> Collector<T, ?, ListX<T>> toListX() {
        return Collectors.collectingAndThen(ListX.defaultCollector(), (List<T> d) -> new ListXImpl<>(
                                                                                                     d, ListX.defaultCollector()));

    }

    static <T> Collector<T, ?, DequeX<T>> toDequeX() {
        return Collectors.collectingAndThen(DequeX.defaultCollector(), (Deque<T> d) -> new DequeXImpl<>(
                                                                                                        d, DequeX.defaultCollector()));

    }

    static <T> Collector<T, ?, SetX<T>> toSetX() {
        return Collectors.collectingAndThen(SetX.defaultCollector(), (Set<T> d) -> new SetXImpl<>(
                                                                                                  d, SetX.defaultCollector()));
    }

    static <T> Collector<T, ?, SortedSetX<T>> toSortedSetX() {
        return Collectors.collectingAndThen(SortedSetX.defaultCollector(), (SortedSet<T> d) -> new SortedSetXImpl<>(
                                                                                                                    d,
                                                                                                                    SortedSetX.defaultCollector()));

    }
}
