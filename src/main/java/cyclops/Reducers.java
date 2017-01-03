package cyclops;

import java.util.List;

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

import cyclops.collections.immutable.PBagX;
import cyclops.collections.immutable.PMapX;
import cyclops.collections.immutable.POrderedSetX;
import cyclops.collections.immutable.PQueueX;
import cyclops.collections.immutable.PSetX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import com.aol.cyclops2.types.mixins.TupleWrapper;

import lombok.experimental.UtilityClass;

/**
 * Class that holds Reducers, Monoids with a type conversion for reducing a dataset to a single value.
 * 
 * Primary use case is the reduction of Streams to persistent collections 
 * 
 * e.g.
 * <pre>
 * {@code 
 * PQueueX<Integer> q = Reducers.<Integer>toPQueueX()
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
     * PQueueX<Integer> q = Reducers.<Integer>toPQueueX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer to PQueueX types
     */
    public static <T> Reducer<PQueueX<T>> toPQueueX() {
        
        return Reducer.<PQueueX<T>> of(PQueueX.empty(), (final PQueueX<T> a) -> b -> a.plusAll(b), (final T x) -> PQueueX.singleton(x));
    }

    /**
     * <pre>
     * {@code 
     * POrderedSetX<Integer> q = Reducers.<Integer>toPOrderedSetX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer to POrderedSetX
     */
    public static <T> Reducer<POrderedSetX<T>> toPOrderedSetX() {
        return Reducer.<POrderedSetX<T>> of(POrderedSetX.<T> empty(), (final POrderedSetX<T> a) -> b -> a.plusAll(b),
                                            (final T x) -> POrderedSetX.singleton(x));
    }

    /**
     * <pre>
     * {@code 
     * PSetX<Integer> q = Reducers.<Integer>toPSetX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PSetX
     */
    public static <T> Reducer<PSetX<T>> toPSetX() {
        return Reducer.<PSetX<T>> of(PSetX.empty(), (final PSetX<T> a) -> b -> a.plusAll(b), (final T x) -> PSetX.singleton(x));
    }

    /**
     * <pre>
     * {@code 
     * PStackX<Integer> q = Reducers.<Integer>toPStackX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PStackX
     */
    public static <T> Reducer<PStackX<T>> toPStackX() {
        return Reducer.<PStackX<T>> of(PStackX.empty(), (final PStackX<T> a) -> b -> a.plusAll(b), (final T x) -> PStackX.singleton(x));
    }

    /**
     * <pre>
     * {@code 
     * PVectorX<Integer> q = Reducers.<Integer>toPVectorX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PVectorX
     */
    public static <T> Reducer<PVectorX<T>> toPVectorX() {
        return Reducer.<PVectorX<T>> of(PVectorX.empty(), (final PVectorX<T> a) -> b -> a.plusAll(b), (final T x) -> PVectorX.singleton(x));
    }


    /**
     * <pre>
     * {@code 
     * PBagX<Integer> q = Reducers.<Integer>toPBagX()
                                .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PBagX
     */
    public static <T> Reducer<PBagX<T>> toPBagX() {
        return Reducer.<PBagX<T>> of(PBagX.empty(), (final PBagX<T> a) -> b -> a.plusAll(b), (final T x) -> PBagX.singleton(x));
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
     * @return Reducer to PQueue types
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
     * @return Reducer to POrderedSet
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
     * PMapX<Integer,String> q = Reducers.<Integer,String>toPMapX()
                                        .mapReduce(Stream.of(Arrays.asList("hello",1),Arrays.asList("world",2)));
     * 
     * }
     * </pre>
     * @return Reducer for PMapX
     */
    public static <K, V> Reducer<PMapX<K, V>> toPMapX() {
        return Reducer.<PMapX<K, V>> of(PMapX.empty(), (final PMapX<K, V> a) -> b -> a.plusAll(b), (in) -> {
            final List w = ((TupleWrapper) () -> in).values();
            return PMapX.singleton((K) w.get(0), (V) w.get(1));
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
