package cyclops.companion;

import java.util.List;
import java.util.stream.Stream;

import cyclops.collections.immutable.*;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import org.pcollections.AmortizedPQueue;
import org.pcollections.ConsPStack;
import org.pcollections.HashTreePBag;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.OrderedPSet;
import org.pcollections.PBag;
import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PQueue;
import org.pcollections.PSet;
import org.pcollections.PStack;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import cyclops.collections.immutable.BagX;
import com.aol.cyclops2.types.mixins.TupleWrapper;

import lombok.experimental.UtilityClass;

/**
 * Class that holds Reducers, Monoids with a type conversion for reducing a dataset toNested a single value.
 * 
 * Primary use case is the reduction of Streams toNested persistent collections
 * 
 * e.g.
 * <pre>
 * {@code 
 * PersistentQueueX<Integer> q = Reducers.<Integer>toPersistentQueueX()
                                .mapReduce(Stream.of(1,2,3,4));
 * 
 * }
 * </pre>
 * 
 * Use with care, as the mapReduce method is not type safe
 * 
 * @author johnmcclean
 *
 */
@UtilityClass
public class Reducers {
    private static <T> PQueue<T> queueOf(final T... values) {
        PQueue<T> result = AmortizedPQueue.empty();
        for (final T value : values) {
            result = result.plus(value);
        }
        return result;

    }

    /**
     * 
     * <pre>
     * {@code 
     * PersistentQueueX<Integer> q = Reducers.<Integer>toPersistentQueueX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer toNested PersistentQueueX types
     */
    public static <T> Reducer<PersistentQueueX<T>> toPersistentQueueX() {
        
        return Reducer.<PersistentQueueX<T>> of(PersistentQueueX.empty(), (final PersistentQueueX<T> a) -> b -> a.plusAll(b), (final T x) -> PersistentQueueX.singleton(x));
    }

    /**
     * <pre>
     * {@code 
     * OrderedSetX<Integer> q = Reducers.<Integer>toOrderedSetX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer toNested OrderedSetX
     */
    public static <T> Reducer<OrderedSetX<T>> toOrderedSetX() {
        return Reducer.<OrderedSetX<T>> of(OrderedSetX.<T> empty(), (final OrderedSetX<T> a) -> b -> a.plusAll(b),
                                            (final T x) -> OrderedSetX.singleton(x));
    }

    /**
     * <pre>
     * {@code 
     * PersistentSetX<Integer> q = Reducers.<Integer>toPersistentSetX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PersistentSetX
     */
    public static <T> Reducer<PersistentSetX<T>> toPersistentSetX() {
        return Reducer.<PersistentSetX<T>> of(PersistentSetX.empty(), (final PersistentSetX<T> a) -> b -> a.plusAll(b), (final T x) -> PersistentSetX.singleton(x));
    }

    /**
     * <pre>
     * {@code 
     * LinkedListX<Integer> q = Reducers.<Integer>toLinkedListX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for LinkedListX
     */
    public static <T> Reducer<LinkedListX<T>> toLinkedListX() {
        return Reducer.<LinkedListX<T>> of(LinkedListX.empty(), (final LinkedListX<T> a) -> b -> a.plusAll(b), (final T x) -> LinkedListX.singleton(x));
    }

    /**
     * <pre>
     * {@code 
     * VectorX<Integer> q = Reducers.<Integer>toVectorX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for VectorX
     */
    public static <T> Reducer<VectorX<T>> toVectorX() {
        return Reducer.<VectorX<T>> of(VectorX.empty(), (final VectorX<T> a) -> b -> a.plusAll(b), (final T x) -> VectorX.singleton(x));
    }


    /**
     * <pre>
     * {@code 
     * BagX<Integer> q = Reducers.<Integer>toBagX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for BagX
     */
    public static <T> Reducer<BagX<T>> toBagX() {
        return Reducer.<BagX<T>> of(BagX.empty(), (final BagX<T> a) -> b -> a.plusAll(b), (final T x) -> BagX.singleton(x));
    }

    private static <T> PQueue<T> queueSingleton(final T value) {
        PQueue<T> result = AmortizedPQueue.empty();
        result = result.plus(value);
        return result;
    }
    /**
     * 
     * <pre>
     * {@code 
     * PQueue<Integer> q = Reducers.<Integer>toPQueue()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer toNested PQueue types
     */
    public static <T> Reducer<PQueue<T>> toPQueue() {
        return Reducer.<PQueue<T>> of(AmortizedPQueue.empty(), (final PQueue<T> a) -> b -> a.plusAll(b), (final T x) -> queueSingleton(x));
    }
    /**
     * <pre>
     * {@code 
     * POrderedSet<Integer> q = Reducers.<Integer>toPOrderedSet()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer toNested POrderedSet
     */
    public static <T> Reducer<POrderedSet<T>> toPOrderedSet() {
        return Reducer.<POrderedSet<T>> of(OrderedPSet.empty(), (final POrderedSet<T> a) -> b -> a.plusAll(b),
                                           (final T x) -> OrderedPSet.singleton(x));
    }
    /**
     * <pre>
     * {@code 
     * PBag<Integer> q = Reducers.<Integer>toPBag()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PBag
     */
    public static <T> Reducer<PBag<T>> toPBag() {
        return Reducer.<PBag<T>> of(HashTreePBag.empty(), (final PBag<T> a) -> b -> a.plusAll(b), (final T x) -> HashTreePBag.singleton(x));
    }
    /**
     * <pre>
     * {@code 
     * PSet<Integer> q = Reducers.<Integer>toPSet()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PSet
     */
    public static <T> Reducer<PSet<T>> toPSet() {
        return Reducer.<PSet<T>> of(HashTreePSet.empty(), (final PSet<T> a) -> b -> a.plusAll(b), (final T x) -> HashTreePSet.singleton(x));
    }
    /**
     * <pre>
     * {@code 
     * PVector<Integer> q = Reducers.<Integer>toPVector()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PVector
     */
    public static <T> Reducer<PVector<T>> toPVector() {
        return Reducer.<PVector<T>> of(TreePVector.empty(), (final PVector<T> a) -> b -> a.plusAll(b), (final T x) -> TreePVector.singleton(x));
    }
    /**
     * <pre>
     * {@code 
     * PStack<Integer> q = Reducers.<Integer>toPStack()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PStack
     */
    public static <T> Reducer<PStack<T>> toPStack() {
        return Reducer.<PStack<T>> of(ConsPStack.empty(), (final PStack<T> a) -> b -> a.plusAll(a.size(), b), (final T x) -> ConsPStack.singleton(x));
    }
    /**
     * <pre>
     * {@code 
     * PStack<Integer> q = Reducers.<Integer>toPStackReversed()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PStack in reveresed order
     */
    public static <T> Reducer<PStack<T>> toPStackReversed() {
        return Reducer.<PStack<T>> of(ConsPStack.empty(), (final PStack<T> a) -> b -> a.plusAll(b), (final T x) -> ConsPStack.singleton(x));
    }
    /**
     * <pre>
     * {@code 
     * PMap<Integer,String> q = Reducers.<Integer,String>toPMap()
                                .mapReduce(Stream.of(Arrays.asList("hello",1),Arrays.asList("world",2)));
     * 
     * }
     * </pre>
     * @return Reducer for PMap
     */
    public static <K, V> Reducer<PMap<K, V>> toPMap() {
        return Reducer.<PMap<K, V>> of(HashTreePMap.empty(), (final PMap<K, V> a) -> b -> a.plusAll(b), (in) -> {  
            final List w = ((TupleWrapper) () -> in).values();
            return HashTreePMap.singleton((K) w.get(0), (V) w.get(1));
        });
    }
    /**
     * <pre>
     * {@code 
     * PersistentMapX<Integer,String> q = Reducers.<Integer,String>toPMapX()
                                        .mapReduce(Stream.of(Arrays.asList("hello",1),Arrays.asList("world",2)));
     * 
     * }
     * </pre>
     * @return Reducer for PersistentMapX
     */
    public static <K, V> Reducer<PersistentMapX<K, V>> toPMapX() {
        return Reducer.<PersistentMapX<K, V>> of(PersistentMapX.empty(), (final PersistentMapX<K, V> a) -> b -> a.plusAll(b), (in) -> {
            final List w = ((TupleWrapper) () -> in).values();
            return PersistentMapX.singleton((K) w.get(0), (V) w.get(1));
        });
    }

    /**
     * Monoid for String concatonation
     * 
     * @param joiner String combiner
     * @return
     */
    public static Monoid<String> toString(final String joiner) {
        return Monoid.of("", (a, b) -> a + joiner + b);
    }

    /**
     * 
     * @return Monoid for summing Ints
     */
    public static Monoid<Integer> toTotalInt() {
        return Monoid.of(0, a -> b -> a + b);
    }

    /**
     * @return Reducer for counting number of values
     */
    public static Reducer<Integer> toCountInt() {

        return Reducer.of(0, a -> b -> a + 1, (x) -> 1);
    }

    /**
     * @return Monoid for summing doubles
     */
    public static Monoid<Double> toTotalDouble() {
        return Monoid.of(0.0, (a, b) -> a + b);
    }

    /**
     * @return Reducer for counting doubles by converting toString toNested Double.valueOf( )
     */
    public static Reducer<Double> toCountDouble() {
        return Reducer.of(0.0, a -> b -> a + 1, (x) -> Double.valueOf("" + x));
    }

}
