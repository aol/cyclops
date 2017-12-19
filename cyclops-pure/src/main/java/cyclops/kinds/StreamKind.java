package cyclops.kinds;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.instances.jdk.StreamInstances;
import cyclops.typeclasses.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * Simulates Higher Kinded Types for Stream's
 *
 * StreamKind is a Stream and a Higher Kinded Type (stream,T)
 *
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Stream
 */

public  interface StreamKind<T> extends Higher<DataWitness.stream, T>, Stream<T> {

  public static  <T> Kleisli<DataWitness.stream,Stream<T>,T> kindKleisli(){
    return Kleisli.of(StreamInstances.monad(), StreamKind::widen);
  }

  public static  <T> Cokleisli<DataWitness.stream,T,Stream<T>> kindCokleisli(){
    return Cokleisli.of(StreamKind::narrowK);
  }
  public static <W1,T> Nested<DataWitness.stream,W1,T> nested(Stream<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(StreamKind.widen(nested), StreamInstances.definitions(),def2);
  }
  public static <W1,T> Product<DataWitness.stream,W1,T> product(Stream<T> f, Active<W1,T> active){
    return Product.of(allTypeclasses(f),active);
  }

  public static <W1,T> Coproduct<W1,DataWitness.stream,T> coproduct(Stream<T> f, InstanceDefinitions<W1> def2){
    return Coproduct.right(StreamKind.widen(f),def2, StreamInstances.definitions());
  }
  public static <T> Active<DataWitness.stream,T> allTypeclasses(Stream<T> f){
    return Active.of(StreamKind.widen(f), StreamInstances.definitions());
  }
  public static <W2,T,R> Nested<DataWitness.stream,W2,R> mapM(Stream<T> f, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    Stream<Higher<W2, R>> x = f.map(fn);
    return nested(x,defs);

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
   * @param stream Stream to widen to a StreamKind
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
  public static <C2,T> Higher<C2, Higher<DataWitness.stream,T>> widen2(Higher<C2, StreamKind<T>> stream){
    //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
    //cast seems safer as Higher<stream,T> must be a StreamKind
    return (Higher)stream;
  }
  /**
   * Convert the raw Higher Kinded Type for Stream types into the StreamKind type definition class
   *
   * @param stream HKT encoded Stream into a StreamKind
   * @return StreamKind
   */
  public static <T> StreamKind<T> narrowK(final Higher<DataWitness.stream, T> stream) {
    return (StreamKind<T>)stream;
  }
  /**
   * Convert the HigherKindedType definition for a Stream into
   *
   * @param stream Type Constructor to convert back into narrowed type
   * @return StreamX from Higher Kinded Type
   */
  public static <T> Stream<T> narrow(final Higher<DataWitness.stream, T> stream) {
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
    public StreamKind<T> sequential() {
      return widen(boxed.sequential());
    }

    @Override
    public StreamKind<T> parallel() {
      return widen(boxed.parallel());
    }

    @Override
    public StreamKind<T> unordered() {
      return widen(boxed.unordered());
    }

    @Override
    public StreamKind<T> onClose(final Runnable closeHandler) {
      return widen(boxed.onClose(closeHandler));
    }

    @Override
    public void close() {
      boxed.close();
    }

    @Override
    public StreamKind<T> filter(final Predicate<? super T> predicate) {
      return widen(boxed.filter(predicate));
    }

    @Override
    public <R> StreamKind<R> map(final Function<? super T, ? extends R> mapper) {
      return widen(boxed.map(mapper));
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
    public <R> StreamKind<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> mapper) {
      return widen(boxed.flatMap(mapper));
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
    public StreamKind<T> distinct() {
      return widen(boxed.distinct());
    }

    @Override
    public StreamKind<T> sorted() {
      return widen(boxed.sorted());
    }

    @Override
    public StreamKind<T> sorted(final Comparator<? super T> comparator) {
      return widen(boxed.sorted(comparator));
    }

    @Override
    public StreamKind<T> peek(final Consumer<? super T> action) {
      return widen(boxed.peek(action));
    }

    @Override
    public StreamKind<T> limit(final long maxSize) {
      return widen(boxed.limit(maxSize));
    }

    @Override
    public StreamKind<T> skip(final long n) {
      return widen(boxed.skip(n));
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
    public Active<DataWitness.stream,T> allTypeclasses(){
      return Active.of(this, StreamInstances.definitions());
    }
    public <W2,R> Nested<DataWitness.stream,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
      return Nested.of(map(fn), StreamInstances.definitions(), defs);
    }
  }

}
