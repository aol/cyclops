package cyclops.companion;

import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collector;

import com.aol.cyclops2.data.collections.extensions.lazy.*;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collectionx.mutable.DequeX;

import cyclops.collectionx.mutable.ListX;

import cyclops.collectionx.mutable.QueueX;

import cyclops.collectionx.mutable.SetX;

import cyclops.collectionx.mutable.SortedSetX;


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
                                                                                                              d, QueueX.defaultCollector(),Evaluation.LAZY));

    }

    /**
     * @return Collector for ListX
     */
    static <T> Collector<T, ?, ListX<T>> toListX() {
        return java.util.stream.Collectors.collectingAndThen(ListX.defaultCollector(), (final List<T> d) -> new LazyListX<T>(
                                                                                                           d, null,ListX.defaultCollector(),Evaluation.LAZY));

    }

    /**
     * @return Collector for DequeX
     */
    static <T> Collector<T, ?, DequeX<T>> toDequeX() {
        return java.util.stream.Collectors.collectingAndThen(DequeX.defaultCollector(), (final Deque<T> d) -> new LazyDequeX<>(
                                                                                                              d, DequeX.defaultCollector(),Evaluation.LAZY));

    }

    /**
     * @return Collector for SetX
     */
    static <T> Collector<T, ?, SetX<T>> toSetX() {
        return java.util.stream.Collectors.collectingAndThen(SetX.defaultCollector(), (final Set<T> d) -> new LazySetX<>(
                                                                                                        d, SetX.defaultCollector(),Evaluation.LAZY));
    }

    /**
     * @return Collector for SortedSetX
     */
    static <T> Collector<T, ?, SortedSetX<T>> toSortedSetX() {
        return java.util.stream.Collectors.collectingAndThen(SortedSetX.defaultCollector(), (final SortedSet<T> d) -> new LazySortedSetX<>(
                                                                                                                          d,
                                                                                                                          SortedSetX.defaultCollector(), Evaluation.LAZY));

    }
}
