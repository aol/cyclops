package cyclops.higherkindedtypes;



        import java.util.Comparator;
        import java.util.Iterator;
        import java.util.Optional;
        import java.util.Spliterator;
        import java.util.function.BiConsumer;
        import java.util.function.BiFunction;
        import java.util.function.BinaryOperator;
        import java.util.function.Consumer;
        import java.util.function.Function;
        import java.util.function.IntFunction;
        import java.util.function.Predicate;
        import java.util.function.Supplier;
        import java.util.function.ToDoubleFunction;
        import java.util.function.ToIntFunction;
        import java.util.function.ToLongFunction;
        import java.util.stream.Collector;
        import java.util.stream.DoubleStream;
        import java.util.stream.IntStream;
        import java.util.stream.LongStream;
        import java.util.stream.Stream;


        import com.aol.cyclops2.hkt.Higher;
        import lombok.AccessLevel;
        import lombok.AllArgsConstructor;

/**
 * Simulates Higher Kinded Types for Stream's
 *
 * StreamKind is a Stream and a Higher Kinded Type (StreamKind.µ,T)
 *
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Stream
 */

public interface StreamKind<T> extends Higher<StreamKind.µ, T>, Stream<T> {
    /**
     * Witness type
     *
     * @author johnmcclean
     *
     */
    public static class µ {
    }

    public static <T> StreamKind<T> of(T... elements){
        return widen(Stream.of(elements));
    }
    /**
     * Convert a Stream to a simulated HigherKindedType that captures Stream nature
     * and Stream element data type separately. Recover via @see StreamKind#narrow
     *
     * If the supplied Stream implements StreamKind it is returned already, otherwise it
     * is wrapped into a Stream implementation that does implement StreamKind
     *
     * @param Stream Stream to widen to a StreamKind
     * @return StreamKind encoding HKT info about Streams
     */
    public static <T> StreamKind<T> widen(final Stream<T> stream) {
        if (stream instanceof StreamKind)
            return (StreamKind<T>) stream;
        return new Box<>(
                stream);
    }
    /**
     * Widen a StreamKind nested inside another HKT encoded type
     *
     * @param stream HTK encoded type containing  a Stream to widen
     * @return HKT encoded type with a widened Stream
     */
    public static <C2,T> Higher<C2, Higher<StreamKind.µ,T>> widen2(Higher<C2, StreamKind<T>> stream){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<StreamKind.µ,T> must be a StreamKind
        return (Higher)stream;
    }
    /**
     * Convert the raw Higher Kinded Type for Stream types into the StreamKind type definition class
     *
     * @param stream HKT encoded Stream into a StreamKind
     * @return StreamKind
     */
    public static <T> StreamKind<T> narrowK(final Higher<StreamKind.µ, T> stream) {
        return (StreamKind<T>)stream;
    }
    /**
     * Convert the HigherKindedType definition for a Stream into
     *
     * @param Stream Type Constructor to convert back into narrowed type
     * @return StreamX from Higher Kinded Type
     */
    public static <T> Stream<T> narrow(final Higher<StreamKind.µ, T> stream) {
        if (stream instanceof Stream)
            return (Stream) stream;
        final Box<T> type = (Box<T>) stream;
        return type.narrow();
    }



    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Box<T> implements StreamKind<T> {

        private final Stream<T> boxed;

        /**
         * @return This back as a StreamX
         */
        public Stream<T> narrow() {
            return boxed;
        }

        @Override
        public Iterator<T> iterator() {
            return boxed.iterator();
        }

        @Override
        public Spliterator<T> spliterator() {
            return boxed.spliterator();
        }

        @Override
        public boolean isParallel() {
            return boxed.isParallel();
        }

        @Override
        public Stream<T> sequential() {
            return boxed.sequential();
        }

        @Override
        public Stream<T> parallel() {
            return boxed.parallel();
        }

        @Override
        public Stream<T> unordered() {
            return boxed.unordered();
        }

        @Override
        public Stream<T> onClose(final Runnable closeHandler) {
            return boxed.onClose(closeHandler);
        }

        @Override
        public void close() {
            boxed.close();
        }

        @Override
        public Stream<T> filter(final Predicate<? super T> predicate) {
            return boxed.filter(predicate);
        }

        @Override
        public <R> Stream<R> map(final Function<? super T, ? extends R> mapper) {
            return boxed.map(mapper);
        }

        @Override
        public IntStream mapToInt(final ToIntFunction<? super T> mapper) {
            return boxed.mapToInt(mapper);
        }

        @Override
        public LongStream mapToLong(final ToLongFunction<? super T> mapper) {
            return boxed.mapToLong(mapper);
        }

        @Override
        public DoubleStream mapToDouble(final ToDoubleFunction<? super T> mapper) {
            return boxed.mapToDouble(mapper);
        }

        @Override
        public <R> Stream<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> mapper) {
            return boxed.flatMap(mapper);
        }

        @Override
        public IntStream flatMapToInt(final Function<? super T, ? extends IntStream> mapper) {
            return boxed.flatMapToInt(mapper);
        }

        @Override
        public LongStream flatMapToLong(final Function<? super T, ? extends LongStream> mapper) {
            return boxed.flatMapToLong(mapper);
        }

        @Override
        public DoubleStream flatMapToDouble(final Function<? super T, ? extends DoubleStream> mapper) {
            return boxed.flatMapToDouble(mapper);
        }

        @Override
        public Stream<T> distinct() {
            return boxed.distinct();
        }

        @Override
        public Stream<T> sorted() {
            return boxed.sorted();
        }

        @Override
        public Stream<T> sorted(final Comparator<? super T> comparator) {
            return boxed.sorted(comparator);
        }

        @Override
        public Stream<T> peek(final Consumer<? super T> action) {
            return boxed.peek(action);
        }

        @Override
        public Stream<T> limit(final long maxSize) {
            return boxed.limit(maxSize);
        }

        @Override
        public Stream<T> skip(final long n) {
            return boxed.skip(n);
        }

        @Override
        public void forEach(final Consumer<? super T> action) {
            boxed.forEach(action);
        }

        @Override
        public void forEachOrdered(final Consumer<? super T> action) {
            boxed.forEachOrdered(action);
        }

        @Override
        public Object[] toArray() {
            return boxed.toArray();
        }

        @Override
        public <A> A[] toArray(final IntFunction<A[]> generator) {
            return boxed.toArray(generator);
        }

        @Override
        public T reduce(final T identity, final BinaryOperator<T> accumulator) {
            return boxed.reduce(identity, accumulator);
        }

        @Override
        public Optional<T> reduce(final BinaryOperator<T> accumulator) {
            return boxed.reduce(accumulator);
        }

        @Override
        public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator,
                            final BinaryOperator<U> combiner) {
            return boxed.reduce(identity, accumulator, combiner);
        }

        @Override
        public <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator,
                             final BiConsumer<R, R> combiner) {
            return boxed.collect(supplier, accumulator, combiner);
        }

        @Override
        public <R, A> R collect(final Collector<? super T, A, R> collector) {
            return boxed.collect(collector);
        }

        @Override
        public Optional<T> min(final Comparator<? super T> comparator) {
            return boxed.min(comparator);
        }

        @Override
        public Optional<T> max(final Comparator<? super T> comparator) {
            return boxed.max(comparator);
        }

        @Override
        public long count() {
            return boxed.count();
        }

        @Override
        public boolean anyMatch(final Predicate<? super T> predicate) {
            return boxed.anyMatch(predicate);
        }

        @Override
        public boolean allMatch(final Predicate<? super T> predicate) {
            return boxed.allMatch(predicate);
        }

        @Override
        public boolean noneMatch(final Predicate<? super T> predicate) {
            return boxed.noneMatch(predicate);
        }

        @Override
        public Optional<T> findFirst() {
            return boxed.findFirst();
        }

        @Override
        public Optional<T> findAny() {
            return boxed.findAny();
        }

    }

}