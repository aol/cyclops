package cyclops.companion;

import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.aol.cyclops2.data.collections.extensions.lazy.*;
import cyclops.collections.DequeX;

import cyclops.collections.ListX;

import cyclops.collections.QueueX;

import cyclops.collections.SetX;

import cyclops.collections.SortedSetX;


/**
 * CyclopsCollectors for Cyclops Extended Collections
 * 
 * @author johnmcclean
 *
 */
public interface CyclopsCollectors {
    /**
     * @return Collector to QueueX
     */
    static <T> Collector<T, ?, QueueX<T>> toQueueX() {
        return java.util.stream.Collectors.collectingAndThen(QueueX.defaultCollector(), (final Queue<T> d) -> new LazyQueueX<>(
                                                                                                              d, QueueX.defaultCollector()));

    }

    /**
     * @return Collector for ListX
     */
    static <T> Collector<T, ?, ListX<T>> toListX() {
        return java.util.stream.Collectors.collectingAndThen(ListX.defaultCollector(), (final List<T> d) -> new LazyListX<T>(
                                                                                                           d, null,ListX.defaultCollector()));

    }

    /**
     * @return Collector for DequeX
     */
    static <T> Collector<T, ?, DequeX<T>> toDequeX() {
        return java.util.stream.Collectors.collectingAndThen(DequeX.defaultCollector(), (final Deque<T> d) -> new LazyDequeX<>(
                                                                                                              d, DequeX.defaultCollector()));

    }

    /**
     * @return Collector for SetX
     */
    static <T> Collector<T, ?, SetX<T>> toSetX() {
        return java.util.stream.Collectors.collectingAndThen(SetX.defaultCollector(), (final Set<T> d) -> new LazySetX<>(
                                                                                                        d, SetX.defaultCollector()));
    }

    /**
     * @return Collector for SortedSetX
     */
    static <T> Collector<T, ?, SortedSetX<T>> toSortedSetX() {
        return java.util.stream.Collectors.collectingAndThen(SortedSetX.defaultCollector(), (final SortedSet<T> d) -> new LazySortedSetX<>(
                                                                                                                          d,
                                                                                                                          SortedSetX.defaultCollector()));

    }
}
