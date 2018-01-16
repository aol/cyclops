package cyclops.reactive.collections.immutable;


import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.IndexedSequenceX;
import com.oath.cyclops.types.persistent.PersistentList;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.LazyPVectorX;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.Traversable;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;
import cyclops.control.*;
import cyclops.data.Vector;
import com.oath.cyclops.types.foldable.Evaluation;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.companion.Reducers;
import com.oath.cyclops.hkt.DataWitness.vectorX;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.mutable.ListX;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.foldable.To;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

import static com.oath.cyclops.types.foldable.Evaluation.LAZY;

/**
 * An eXtended Persistent Vector type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface VectorX<T> extends To<VectorX<T>>,
                                     PersistentList<T>,
                                     IndexedSequenceX<T>,
                                     LazyCollectionX<T>,
                                     OnEmptySwitch<T, PersistentList<T>>,
                                     Comparable<T>,
                                     Higher<vectorX,T>{

    public static <T> VectorX<T> defer(Supplier<VectorX<T>> s){
      return of(s)
                .map(Supplier::get)
                .concatMap(l->l);
    }

    @Override
    VectorX<T> updateAt(int i, T e);

    @Override
    default ReactiveSeq<T> stream() {
        return LazyCollectionX.super.stream();
    }

    default Maybe<T> headMaybe(){
        return headAndTail().headMaybe();
    }
    default T head(){
        return headAndTail().head();
    }
    default VectorX<T> tail(){
        return headAndTail().tail().to().vectorX(Evaluation.LAZY);
    }
    VectorX<T> lazy();
    VectorX<T> eager();
    static <T> CompletableVectorX<T> completable(){
        return new CompletableVectorX<>();
    }

    static class CompletableVectorX<T> implements InvocationHandler {
        Future<VectorX<T>> future = Future.future();
        public boolean complete(PersistentList<T> result){
            return future.complete(VectorX.fromIterable(result));
        }

        public VectorX<T> asVectorX(){
            VectorX f = (VectorX) Proxy.newProxyInstance(VectorX.class.getClassLoader(),
                    new Class[] { VectorX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            VectorX<T> target = future.visit(l->l,t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }
    public static <T> Higher<vectorX, T> widen(VectorX<T> narrow) {
    return narrow;
  }




    /**
     * Widen a PVectorType nest inside another HKT encoded type
     *
     * @param list HTK encoded type containing  a PVector to widen
     * @return HKT encoded type with a widened PVector
     */
    public static <C2,T> Higher<C2, Higher<vectorX,T>> widen2(Higher<C2, VectorX<T>> list){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<PVectorType.vectorX,T> must be a PVectorType
        return (Higher)list;
    }
    /**
     * Convert the raw Higher Kinded Type for PVector types into the PVectorType type definition class
     *
     * @param list HKT encoded list into a PVectorType
     * @return PVectorType
     */
    public static <T> VectorX<T> narrowK(final Higher<vectorX, T> list) {
        return (VectorX<T>)list;
    }
    /**
     * Narrow a covariant VectorX
     *
     * <pre>
     * {@code
     *  VectorX<? extends Fruit> set = VectorX.of(apple,bannana);
     *  VectorX<Fruit> fruitSet = VectorX.narrow(set);
     * }
     * </pre>
     *
     * @param vectorX to narrow generic type
     * @return OrderedSetX with narrowed type
     */
    public static <T> VectorX<T> narrow(final VectorX<? extends T> vectorX) {
        return (VectorX<T>) vectorX;
    }


    /**
     * Create a VectorX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range VectorX
     */
    public static VectorX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to()
                          .vectorX(LAZY);
    }

    /**
     * Create a VectorX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range VectorX
     */
    public static VectorX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to()
                          .vectorX(LAZY);
    }

    /**
     * Unfold a function into a VectorX
     *
     * <pre>
     * {@code
     *  VectorX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</code>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return VectorX generated by unfolder function
     */
    static <U, T> VectorX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to()
                          .vectorX(LAZY);
    }

    /**
     * Generate a VectorX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate VectorX elements
     * @return VectorX generated from the provided Supplier
     */
    public static <T> VectorX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit).to()
                          .vectorX(LAZY);
    }
    /**
     * Generate a VectorX from the provided value up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Value for VectorX elements
     * @return VectorX generated from the provided Supplier
     */
    public static <T> VectorX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit).to()
                          .vectorX(LAZY);
    }

    /**
     * Create a VectorX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return VectorX generated by iterative application
     */
    public static <T> VectorX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit).to()
                          .vectorX(LAZY);

    }

    /**
     * Construct a PVector from the provided values
     *
     * <pre>
     * {@code
     *  List<String> list = PVectors.of("a","b","c");
     *
     *  // or
     *
     *  PVector<String> list = PVectors.of("a","b","c");
     *
     *
     * }
     * </pre>
     *
     *
     * @param values To add to PVector
     * @return new PVector
     */
    public static <T> VectorX<T> of(final T... values) {
        return new LazyPVectorX<>(null,ReactiveSeq.of(values),Reducers.toPersistentVector(), LAZY);
    }
    /**
     *
     * Construct a VectorX from the provided Iterator
     *
     * @param it Iterator to populate VectorX
     * @return Newly populated VectorX
     */
    public static <T> VectorX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }
    /**
     * <pre>
     * {@code
     *     List<String> zero = PVectors.zero();
     *    //or
     *
     *     PVector<String> zero = PVectors.zero();
     * }
     * </pre>
     * @return an zero PVector
     */
    public static <T> VectorX<T> empty() {
        return new LazyPVectorX<T>(
                                  Vector.empty(),null,Reducers.toPersistentVector(), LAZY);
    }

    /**
     * Construct a PVector containing a single value
     * </pre>
     * {@code
     *    List<String> single = PVectors.singleton("1");
     *
     *    //or
     *
     *    PVector<String> single = PVectors.singleton("1");
     *
     * }
     * </pre>
     *
     * @param value Active value for PVector
     * @return PVector with a single value
     */
    public static <T> VectorX<T> singleton(final T value) {
        return new LazyPVectorX<>(
                                  Vector.of(value),null,Reducers.toPersistentVector(), LAZY);
    }

    /**
     * Construct a VectorX from an Publisher
     *
     * @param publisher
     *            to construct VectorX from
     * @return VectorX
     */
    public static <T> VectorX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to()
                          .vectorX(LAZY);
    }

    public static <T> VectorX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof VectorX)
            return (VectorX) iterable;
        if (iterable instanceof Vector)
            return new LazyPVectorX<>(
                                      (Vector) iterable,null,Reducers.toPersistentVector(), LAZY);

        return new LazyPVectorX<>(null,
                ReactiveSeq.fromIterable(iterable),
                Reducers.toPersistentVector(), LAZY);
    }
    VectorX<T> type(Reducer<? extends PersistentList<T>,T> reducer);

    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  VectorX<Integer> bag = vectorX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create VectorX from
     * @param <T> VectorX generated from Stream
     * @return
     */
    public static <T> VectorX<T> vectorX(ReactiveSeq<T> stream) {

        return new LazyPVectorX<T>(null,stream,Reducers.toPersistentVector(), LAZY);
    }




    /**
     * Reduce (immutable Collection) a Stream to a PVector
     *
     * <pre>
     * {@code
     *    PVector<Integer> list = PVectors.fromStream(Stream.of(1,2,3));
     *
     *  //list = [1,2,3]
     * }</pre>
     *
     *
     * @param stream to convert to a PVector
     * @return
     */
    default <T> VectorX<T> fromStream(final ReactiveSeq<T> stream) {
        return Reducers.<T>toVectorX()
                       .mapReduce(stream);
    }

    /**
    * Combine two adjacent elements in a VectorX using the supplied BinaryOperator
    * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
    * with it's neighbor
    * <pre>
    * {@code
    *  VectorX.of(1,1,2,3)
                 .combine((a, b)->a.equals(b),SemigroupK.intSum)
                 .listX()

    *  //ListX(3,4)
    * }</pre>
    *
    * @param predicate Test to see if two neighbors should be joined
    * @param op Reducer to combine neighbors
    * @return Combined / Partially Reduced VectorX
    */
    @Override
    default VectorX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (VectorX<T>) LazyCollectionX.super.combine(predicate, op);
    }
    @Override
    default VectorX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (VectorX<T>)LazyCollectionX.super.combine(op,predicate);
    }
    @Override
    default VectorX<T> materialize() {
        return (VectorX<T>)LazyCollectionX.super.materialize();
    }


    @Override
    default VectorX<T> take(final long num) {

        return limit(num);
    }
    @Override
    default VectorX<T> drop(final long num) {

        return skip(num);
    }


    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> VectorX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (VectorX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> VectorX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (VectorX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> VectorX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (VectorX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> VectorX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (VectorX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> VectorX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (VectorX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> VectorX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                        BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (VectorX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }


    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *     VectorX.of(1,2,3)
     *          .map(i->i*2)
     *          .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *     //VectorX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed VectorX
     */
    default <R> VectorX<R> coflatMap(Function<? super VectorX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }



    @Override
    default <X> VectorX<X> from(final Iterable<X> col) {
        return fromIterable(col);
    }

    //@Override
    default <T> Reducer<PersistentList<T>,T> monoid() {
        return Reducers.toPersistentVector();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#reverse()
     */
    @Override
    default VectorX<T> reverse() {
        return (VectorX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> filter(final Predicate<? super T> pred) {
        return (VectorX<T>) LazyCollectionX.super.filter(pred);
    }
    @Override
    default VectorX<T> peek(final Consumer<? super T> c) {
      return (VectorX<T>) LazyCollectionX.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> VectorX<R> map(final Function<? super T, ? extends R> mapper) {

        return (VectorX<R>) LazyCollectionX.super.map(mapper);
    }

    @Override
    default boolean isEmpty() {
        return PersistentList.super.isEmpty();
    }
    @Override
    default <R> VectorX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> VectorX<R> unit(final R value) {
        return singleton(value);
    }

   // @Override
    default <R> VectorX<R> emptyUnit() {
        return empty();
    }

    @Override
    default <R> VectorX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> VectorX<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (VectorX<R>) LazyCollectionX.super.concatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limit(long)
     */
    @Override
    default VectorX<T> limit(final long num) {
        return (VectorX<T>) LazyCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skip(long)
     */
    @Override
    default VectorX<T> skip(final long num) {
        return (VectorX<T>) LazyCollectionX.super.skip(num);
    }

    @Override
    default VectorX<T> takeRight(final int num) {
        return (VectorX<T>) LazyCollectionX.super.takeRight(num);
    }

    @Override
    default VectorX<T> dropRight(final int num) {
        return (VectorX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> takeWhile(final Predicate<? super T> p) {
        return (VectorX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> dropWhile(final Predicate<? super T> p) {
        return (VectorX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> takeUntil(final Predicate<? super T> p) {
        return (VectorX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> dropUntil(final Predicate<? super T> p) {
        return (VectorX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> VectorX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (VectorX<R>) LazyCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#slice(long, long)
     */
    @Override
    default VectorX<T> slice(final long from, final long to) {
        return (VectorX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> VectorX<T> sorted(final Function<? super T, ? extends U> function) {
        return (VectorX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    public VectorX<T> plus(T e);

    @Override
    public VectorX<T> plusAll(Iterable<? extends T> list);

    @Override
    public VectorX<T> insertAt(int i, T e);


    @Override
    public VectorX<T> insertAt(int i, Iterable<? extends T> list);

    @Override
    public VectorX<T> removeValue(T e);

    @Override
    public VectorX<T> removeAll(Iterable<? extends T> list);

    @Override
    public VectorX<T> removeAt(int i);

    @Override
    default boolean containsValue(T item) {
        return LazyCollectionX.super.containsValue(item);
    }

    @Override
    default Traversable<Vector<T>> grouped(final int groupSize) {
        return (VectorX<ListX<T>>) LazyCollectionX.super.grouped(groupSize);
    }


    @Override
    default <U> VectorX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (VectorX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> VectorX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (VectorX<R>) LazyCollectionX.super.zip(other, zipper);
    }


  /* (non-Javadoc)
   * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#permutations()
   */
    @Override
    default VectorX<ReactiveSeq<T>> permutations() {

        return (VectorX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#combinations(int)
     */
    @Override
    default VectorX<ReactiveSeq<T>> combinations(final int size) {

        return (VectorX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#combinations()
     */
    @Override
    default VectorX<ReactiveSeq<T>> combinations() {
        return (VectorX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    @Override
    default VectorX<VectorX<T>> sliding(final int windowSize) {
        return (VectorX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default VectorX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (VectorX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default VectorX<T> scanLeft(final Monoid<T> monoid) {
        return (VectorX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> VectorX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (VectorX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default VectorX<T> scanRight(final Monoid<T> monoid) {
        return (VectorX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> VectorX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (VectorX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default VectorX<T> plusInOrder(final T e) {

        return (VectorX<T>) LazyCollectionX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycle(int)
     */
    @Override
    default VectorX<T> cycle(final long times) {

        return (VectorX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default VectorX<T> cycle(final Monoid<T> m, final long times) {

        return (VectorX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (VectorX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (VectorX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> VectorX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (VectorX) LazyCollectionX.super.zipWithStream(other);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> VectorX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (VectorX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> VectorX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                             final Iterable<? extends T4> fourth) {

        return (VectorX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zipWithIndex()
     */
    @Override
    default VectorX<Tuple2<T, Long>> zipWithIndex() {

        return (VectorX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#distinct()
     */
    @Override
    default VectorX<T> distinct() {

        return (VectorX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted()
     */
    @Override
    default VectorX<T> sorted() {

        return (VectorX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default VectorX<T> sorted(final Comparator<? super T> c) {

        return (VectorX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> skipWhile(final Predicate<? super T> p) {

        return (VectorX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> skipUntil(final Predicate<? super T> p) {

        return (VectorX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> limitWhile(final Predicate<? super T> p) {

        return (VectorX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> limitUntil(final Predicate<? super T> p) {

        return (VectorX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default VectorX<T> intersperse(final T value) {

        return (VectorX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#shuffle()
     */
    @Override
    default VectorX<T> shuffle() {

        return (VectorX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skipLast(int)
     */
    @Override
    default VectorX<T> skipLast(final int num) {

        return (VectorX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limitLast(int)
     */
    @Override
    default VectorX<T> limitLast(final int num) {

        return (VectorX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default VectorX<T> onEmptySwitch(final Supplier<? extends PersistentList<T>> supplier) {
        if (this.isEmpty())
            return VectorX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default VectorX<T> onEmpty(final T value) {

        return (VectorX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default VectorX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (VectorX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> VectorX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (VectorX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default VectorX<T> shuffle(final Random random) {

        return (VectorX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> VectorX<U> ofType(final Class<? extends U> type) {

        return (VectorX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> filterNot(final Predicate<? super T> fn) {

        return (VectorX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#notNull()
     */
    @Override
    default VectorX<T> notNull() {

        return (VectorX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default VectorX<T> removeStream(final Stream<? extends T> stream) {

        return (VectorX<T>) LazyCollectionX.super.removeStream(stream);
    }
    @Override
    default VectorX<T> removeAll(CollectionX<? extends T> it) {
      return removeAll(narrowIterable());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default VectorX<T> removeAll(final T... values) {

        return (VectorX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default VectorX<T> retainAll(final Iterable<? extends T> it) {

        return (VectorX<T>) LazyCollectionX.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default VectorX<T> retainStream(final Stream<? extends T> seq) {

        return (VectorX<T>) LazyCollectionX.super.retainStream(seq);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default VectorX<T> retainAll(final T... values) {

        return (VectorX<T>) LazyCollectionX.super.retainAll(values);
    }


    @Override
    default <C extends Collection<? super T>> VectorX<C> grouped(final int size, final Supplier<C> supplier) {

        return (VectorX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default IterableX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (VectorX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default VectorX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (VectorX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default Traversable<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (VectorX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> VectorX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (VectorX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> VectorX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (VectorX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default <R> VectorX<R> retry(final Function<? super T, ? extends R> fn) {
        return (VectorX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> VectorX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (VectorX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> VectorX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (VectorX<R>)LazyCollectionX.super.flatMap(fn);
    }

    @Override
    default <R> VectorX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (VectorX<R>)LazyCollectionX.super.mergeMap(fn);
    }

    @Override
    default VectorX<T> prependStream(Stream<? extends T> stream) {
        return (VectorX<T>)LazyCollectionX.super.prependStream(stream);
    }

    @Override
    default VectorX<T> appendAll(T... values) {
        return (VectorX<T>)LazyCollectionX.super.appendAll(values);
    }

    @Override
    default VectorX<T> appendAll(T value) {
        return (VectorX<T>)LazyCollectionX.super.appendAll(value);
    }

    @Override
    default VectorX<T> prepend(T value) {
        return (VectorX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default VectorX<T> prependAll(T... values) {
        return (VectorX<T>)LazyCollectionX.super.prependAll(values);
    }

    @Override
    default VectorX<T> insertAt(int pos, T... values) {
        return (VectorX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default VectorX<T> deleteBetween(int start, int end) {
        return (VectorX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default VectorX<T> insertStreamAt(int pos, Stream<T> stream) {
        return (VectorX<T>)LazyCollectionX.super.insertStreamAt(pos,stream);
    }

    @Override
    default VectorX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (VectorX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> VectorX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (VectorX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default VectorX<T> plusLoop(int max, IntFunction<T> value) {
        return (VectorX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default VectorX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (VectorX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

  @Override
    default <T2, R> VectorX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (VectorX<R>)LazyCollectionX.super.zip(fn, publisher);
    }



    @Override
    default <U> VectorX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (VectorX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> VectorX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (VectorX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> VectorX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (VectorX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }



    public static  <T,R> VectorX<R> tailRec(T initial, Function<? super T, ? extends VectorX<? extends Either<T, R>>> fn) {
        return ListX.tailRec(initial,fn).to().vectorX(Evaluation.LAZY);
    }
}
