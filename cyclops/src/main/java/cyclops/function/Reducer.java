package cyclops.function;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A class that extends Monoid to include a transform operation to transform to the type
 * of the identity element first (to make reduction to immutable collections, for example, easier to
 * work with in Java 8 Streams).
 *
 * @author johnmcclean
 *
 * @param <T> Type this Reducer operates on
 */
public interface Reducer<T,U> extends Monoid<T> {

    default BiFunction<T,? super U, T> reducer(){
        return (a,b) -> apply(a,conversion().apply(b));
    }
    Function<? super U, T> conversion();

    /**
     * Map this reducer to the supported Type t.
     *
     * Default implementation is a simple cast.
     *
     * @param stream Stream to convert
     * @return Converted Stream
     */
    default Stream<T> mapToType(final Stream<U> stream){
        return stream.map(conversion());
    }


    default T foldMap(final Stream<U> toReduce) {
        return foldLeft(mapToType(toReduce));
    }

    public static <T,U> Reducer<T,U> fromMonoid(final Monoid<T> monoid, final Function<? super U, T> mapper) {
        return of(monoid.zero(), monoid, mapper);
    }

    public static <T,U> Reducer<T,U> of(final T zero, final BiFunction<T, T, T> combiner, final Function<? super U, T> mapToType) {
        return new Reducer<T,U>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public Function<? super U, T> conversion(){
                return mapToType;
            }

            @Override
            public T apply(final T t, final T u) {
                return combiner.apply(t, u);
            }
        };
    }

    public static <T,U> Reducer<T,U> of(final T zero, final Function<T, Function<T, T>> combiner, final Function<? super U, T> mapToType) {
        return new Reducer<T,U>() {
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
            public Function<? super U, T> conversion(){
                return mapToType;
            }
        };
    }

    static <T,U> Reducer<T,U> narrow(Reducer<? extends T,U> reducer) {
        return (Reducer<T,U>)reducer;
    }
}
