package cyclops.companion;


import cyclops.collectionx.immutable.*;
import cyclops.data.*;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.function.Reducer;

import com.aol.cyclops2.data.collections.extensions.api.PBag;
import com.aol.cyclops2.data.collections.extensions.api.PMap;
import com.aol.cyclops2.data.collections.extensions.api.POrderedSet;
import com.aol.cyclops2.data.collections.extensions.api.PQueue;
import com.aol.cyclops2.data.collections.extensions.api.PSet;
import com.aol.cyclops2.data.collections.extensions.api.PStack;
import com.aol.cyclops2.data.collections.extensions.api.PStack;


import cyclops.collectionx.immutable.BagX;


import lombok.experimental.UtilityClass;

import java.util.Comparator;

/**
 * Class that holds Reducers, Monoids with a type conversion for reducing a dataset to a singleUnsafe value.
 * 
 * Primary use case is the reduction of Streams to persistent collections
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
        PQueue<T> result = BankersQueue.empty();
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
     * @return Reducer to PersistentQueueX types
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
     * @return Reducer to OrderedSetX
     */
    public static <T> Reducer<OrderedSetX<T>> toOrderedSetX() {
        return Reducer.<OrderedSetX<T>> of(OrderedSetX.<T> empty(Comparators.naturalOrderIdentityComparator()), (final OrderedSetX<T> a) -> b -> a.plusAll(b),
                                            (final T x) -> OrderedSetX.singleton(Comparators.naturalOrderIdentityComparator(),x));
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
        PQueue<T> result = BankersQueue.empty();
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
     * @return Reducer to PQueue types
     */
    public static <T> Reducer<PQueue<T>> toPQueue() {
        return Reducer.<PQueue<T>> of(BankersQueue.empty(), (final PQueue<T> a) -> b -> a.plusAll(b), (final T x) -> queueSingleton(x));
    }
    /**
     * <pre>
     * {@code 
     * POrderedSet<Integer> q = Reducers.<Integer>toPOrderedSet()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer to POrderedSet
     */
    public static <T> Reducer<POrderedSet<T>> toPOrderedSet() {
        return Reducer.<POrderedSet<T>> of(TreeSet.empty(Comparators.naturalOrderIdentityComparator()), (final POrderedSet<T> a) -> b -> a.plusAll(b),
                                           (final T x) -> TreeSet.of(Comparators.naturalOrderIdentityComparator(),x));
    }
    public static <T> Reducer<POrderedSet<T>> toPOrderedSet(Comparator<T> comp) {
        return Reducer.<POrderedSet<T>> of(TreeSet.empty(comp), (final POrderedSet<T> a) -> b -> a.plusAll(b),
                (final T x) -> TreeSet.of(comp,x));
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
        return Reducer.<PBag<T>> of(Bag.empty(), (final PBag<T> a) -> b ->
                                        a.plusAll(b),
                (final T x) -> Bag.singleton(x));
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
        return Reducer.<PSet<T>> of(HashSet.empty(), (final PSet<T> a) -> b -> a.plusAll(b), (final T x) -> HashSet.singleton(x));
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
    public static <T> Reducer<PStack<T>> toPVector() {
        return Reducer.<PStack<T>> of(Vector.empty(), (final PStack<T> a) -> b -> a.plusAll(b), (final T x) -> Vector.of(x));
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
        return Reducer.<PStack<T>> of(Seq.empty(), (final PStack<T> a) -> b -> a.insertAt(a.size(), b), (final T x) -> Seq.of(x));
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
        return Reducer.<PStack<T>> of(Seq.empty(), (final PStack<T> a) -> b -> a.plusAll(b), (final T x) -> Seq.of(x));
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
//@TODO FIX THIS
        return Reducer.<PMap<K, V>> of(HashMap.empty(), (final PMap<K, V> a) -> b -> a.plusAll(b), (in) -> {

            final Tuple2 w = (Tuple2)in;
            return HashMap.of((K) w._1(), (V) w._2());

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
//@TODO FIX THIS
        return Reducer.<PersistentMapX<K, V>> of(PersistentMapX.empty(), (final PersistentMapX<K, V> a) -> b -> a.plusAll(b), (in) -> {
            final Tuple2 w = (Tuple2)in;
            return PersistentMapX.singleton((K) w._1(), (V) w._2());
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
     * @return Reducer for counting doubles by converting toString to Double.valueOf( )
     */
    public static Reducer<Double> toCountDouble() {
        return Reducer.of(0.0, a -> b -> a + 1, (x) -> Double.valueOf("" + x));
    }

}
