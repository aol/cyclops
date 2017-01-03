package cyclops;

import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import cyclops.collections.DequeX;
import com.aol.cyclops2.data.collections.extensions.standard.DequeXImpl;
import cyclops.collections.ListX;
import com.aol.cyclops2.data.collections.extensions.standard.ListXImpl;
import cyclops.collections.QueueX;
import com.aol.cyclops2.data.collections.extensions.standard.QueueXImpl;
import cyclops.collections.SetX;
import com.aol.cyclops2.data.collections.extensions.standard.SetXImpl;
import cyclops.collections.SortedSetX;
import com.aol.cyclops2.data.collections.extensions.standard.SortedSetXImpl;

/**
 * Collectors for Cyclops Extended Collections
 * 
 * @author johnmcclean
 *
 */
public interface CyclopsCollectors {
    /**
     * @return Collector to QueueX
     */
    static <T> Collector<T, ?, QueueX<T>> toQueueX() {
        return Collectors.collectingAndThen(QueueX.defaultCollector(), (final Queue<T> d) -> new QueueXImpl<>(
                                                                                                              d, QueueX.defaultCollector()));

    }

    /**
     * @return Collector for ListX
     */
    static <T> Collector<T, ?, ListX<T>> toListX() {
        return Collectors.collectingAndThen(ListX.defaultCollector(), (final List<T> d) -> new ListXImpl<>(
                                                                                                           d, ListX.defaultCollector()));

    }

    /**
     * @return Collector for DequeX
     */
    static <T> Collector<T, ?, DequeX<T>> toDequeX() {
        return Collectors.collectingAndThen(DequeX.defaultCollector(), (final Deque<T> d) -> new DequeXImpl<>(
                                                                                                              d, DequeX.defaultCollector()));

    }

    /**
     * @return Collector for SetX
     */
    static <T> Collector<T, ?, SetX<T>> toSetX() {
        return Collectors.collectingAndThen(SetX.defaultCollector(), (final Set<T> d) -> new SetXImpl<>(
                                                                                                        d, SetX.defaultCollector()));
    }

    /**
     * @return Collector for SortedSetX
     */
    static <T> Collector<T, ?, SortedSetX<T>> toSortedSetX() {
        return Collectors.collectingAndThen(SortedSetX.defaultCollector(), (final SortedSet<T> d) -> new SortedSetXImpl<>(
                                                                                                                          d,
                                                                                                                          SortedSetX.defaultCollector()));

    }
}
