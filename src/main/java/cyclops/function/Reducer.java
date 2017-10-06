package cyclops.function;

import org.pcollections.PBag;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A class that extends Monoid to include a map operation to map to the type
 * of the identity element first (to make reduction to immutable collections, for example, easier to
 * work with in Java 8 Streams).
 * 
 * @author johnmcclean
 *
 * @param <T> Type this Reducer operates on
 */
public interface Reducer<T> extends Monoid<T> {
    /**
     * Map this reducer to the supported Type t.
     * 
     * Default implementation is a simple cast.
     * 
     * @param stream Stream to convert
     * @return Converted Stream
     */
    default Stream<T> mapToType(final Stream<?> stream) {
        return (Stream<T>)stream;
    }

    /**
     * Map a given Stream to required type (via mapToType method), transform
     * reduce using this monoid
     * 
     * Example of multiple reduction using multiple Monoids and PowerTuples
     * <pre>{@code 
     *  Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
     *	Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
     *	<PTuple2<Integer,Integer>> result = PowerTuples.tuple(sum,mult).<PTuple2<Integer,Integer>>asReducer()
     *										.mapReduce(Stream.of(1,2,3,4));
     *	 
     *	assertThat(result,equalTo(tuple(10,24)));
     *  }</pre>
     * 
     * @param toReduce Stream to reduce
     * @return reduced value
     */
    default T mapReduce(final Stream<?> toReduce) {
        return reduce(mapToType(toReduce));
    }

    public static <T> Reducer<T> fromMonoid(final Monoid<T> monoid, final Function<?, ? extends T> mapper) {
        return of(monoid.zero(), monoid, mapper);
    }

    public static <T> Reducer<T> of(final T zero, final BiFunction<T, T, T> combiner, final Function<?, ? extends T> mapToType) {
        return new Reducer<T>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public Stream<T> mapToType(final Stream stream) {
                return stream.map(mapToType);
            }

            @Override
            public T apply(final T t, final T u) {
                return combiner.apply(t, u);
            }
        };
    }

    public static <T> Reducer<T> of(final T zero, final Function<T, Function<T, T>> combiner, final Function<?, T> mapToType) {
        return new Reducer<T>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public T apply(final T t, final T u) {
                return combiner.apply(t)
                               .apply(u);
            }

            @Override
            public Stream<T> mapToType(final Stream stream) {
                return stream.map(mapToType);
            }
        };
    }

    static <T> Reducer<T> narrow(Reducer<? extends T> reducer) {
        return (Reducer<T>)reducer;
    }
}
