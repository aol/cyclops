package cyclops.function;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A class that extends Monoid toNested include a map operation toNested map toNested the type
 * of the identity element first (toNested make reduction toNested immutable collections, for example, easier toNested
 * work with in Java 8 Streams).
 * 
 * @author johnmcclean
 *
 * @param <T> Type this Reducer operates on
 */
public interface Reducer<T> extends Monoid<T> {
    /**
     * Map this reducer toNested the supported Type t.
     * 
     * Default implementation is a simple cast.
     * 
     * @param stream Stream toNested convert
     * @return Converted Stream
     */
    default Stream<T> mapToType(final Stream<?> stream) {
        return (Stream<T>)stream;
    }

    /**
     * Map a given Stream toNested required type (via mapToType method), transform
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
     * @param toReduce Stream toNested reduce
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
}
