package cyclops.reactive.collections.immutable;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.LazyPBagX;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.foldable.To;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;
import cyclops.companion.Reducers;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.Either;
import cyclops.data.Bag;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.function.Reducer;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import com.oath.cyclops.types.persistent.PersistentBag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;


/**
 *
 * A Lazy and Reactive Wrapper for Pesistent Bags (immutable collections like Sets that can contain duplicates).
 * BagX is lazy - chainging functional operations such as transform / filter / flatMap only results in the toX
 * being traversed once. It is materialized and cached on first access (or via {@link BagX#materialize()}
 *
 * It is also possible to populate BagX asynchronously using reactive-streams publishers such as those created via the
 * {@link Spouts} class
 *
 *
 * @param <T>
 */
public interface BagX<T> extends To<BagX<T>>,PersistentBag<T>, LazyCollectionX<T>, OnEmptySwitch<T, PersistentBag<T>> {
    BagX<T> lazy();
    BagX<T> eager();

    @Override
    default boolean isEmpty() {
        return PersistentBag.super.isEmpty();
    }

    @Override
    default boolean containsValue(T item) {
        return LazyCollectionX.super.containsValue(item);
    }

    public static <T> BagX<T> defer(Supplier<BagX<T>> s){
      return of(s)
        .map(Supplier::get)
        .concatMap(l->l);
    }


    static <T> CompletableBagX<T> completable(){
        return new CompletableBagX<>();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class CompletableBagX<T> implements InvocationHandler {
        private final Future<BagX<T>> future = Future.future();


        public boolean complete(PersistentBag<T> result){
            return future.complete(BagX.fromIterable(result));
        }


        public BagX<T> asBagX(){
            BagX f = (BagX) Proxy.newProxyInstance(BagX.class.getClassLoader(),
                    new Class[] { BagX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            BagX<T> target = future.fold(l->l, t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }
    /**
     * Narrow a covariant BagX
     *
     * <pre>
     * {@code
     *  PBaagX<? extends Fruit> set = BagX.of(apple,bannana);
     *  BagX<Fruit> fruitSet = BagX.narrowK(set);
     * }
     * </pre>
     *
     * @param bagX to narrowK generic type
     * @return BagX with narrowed type
     */
    public static <T> BagX<T> narrow(final BagX<? extends T> bagX) {
        return (BagX<T>) bagX;
    }

    /**
     * Create a BagX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range BagX
     */
    public static BagX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to(ReactiveConvertableSequence::converter).bagX(Evaluation.LAZY);
    }

    /**
     * Create a BagX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range BagX
     */
    public static BagX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to(ReactiveConvertableSequence::converter).bagX(Evaluation.LAZY);

    }

    /**
     * Unfold a function into a BagX
     *
     * <pre>
     * {@code
     *  BagX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</code>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return BagX generated by unfolder function
     */
    static <U, T> BagX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to(ReactiveConvertableSequence::converter).bagX(Evaluation.LAZY);
    }

    /**
     * Generate a BagX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate BagX elements
     * @return BagX generated from the provided Supplier
     */
    public static <T> BagX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit).to(ReactiveConvertableSequence::converter).bagX(Evaluation.LAZY);
    }
    /**
     * Generate a BagX from the provided value up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Value for BagX elements
     * @return BagX generated from the provided Supplier
     */
    public static <T> BagX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit).to(ReactiveConvertableSequence::converter).bagX(Evaluation.LAZY);
    }
    @Override
    default BagX<T> materialize() {
        return (BagX<T>)LazyCollectionX.super.materialize();
    }

    /**
     * Create a BagX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return BagX generated by iterative application
     */
    public static <T> BagX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit).to(ReactiveConvertableSequence::converter).bagX(Evaluation.LAZY);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> BagX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                             BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                             Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                             Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (BagX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> BagX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                             BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                             Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                             Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                             Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (BagX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> BagX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                         BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                         Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (BagX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> BagX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                         BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                         Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                         Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (BagX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> BagX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (BagX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> BagX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                     BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (BagX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }




    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    BagX<T> type(Reducer<? extends PersistentBag<T>,T> reducer);

    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  BagX<Integer> bag = bagX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create BagX from
     * @param <T> BagX generated from Stream
     * @return
     */
    public static <T> BagX<T> bagX(ReactiveSeq<T> stream) {

        return new LazyPBagX<>(null,stream,Reducers.toPersistentBag(),Evaluation.LAZY);
    }
    public static <T> BagX<T> of(final T... values) {
        return new LazyPBagX<>(null,
                               ReactiveSeq.of(values),Reducers.toPersistentBag(),Evaluation.LAZY);
    }

    public static <T> BagX<T> empty() {
        return new LazyPBagX<>(null,
                               ReactiveSeq.empty(),Reducers.toPersistentBag(),Evaluation.LAZY);
    }

    public static <T> BagX<T> singleton(final T value) {
        //use concrete type for singleton as used in Reducers
        return new LazyPBagX<>(
                Bag.of(value),null,Reducers.toPersistentBag(),Evaluation.LAZY);
    }

    /**
     * Construct a BagX from an Publisher
     *
     * @param publisher
     *            to construct BagX from
     * @return BagX
     */
    public static <T> BagX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to(ReactiveConvertableSequence::converter).bagX(Evaluation.LAZY);
    }

    public static <T> BagX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof BagX)
            return (BagX) iterable;
        if (iterable instanceof PersistentBag)
            return new LazyPBagX<>(
                                   (PersistentBag) iterable,null,Reducers.toPersistentBag(),Evaluation.LAZY);


        return new LazyPBagX<>(null,
                                 ReactiveSeq.fromIterable(iterable),
                                 Reducers.toPersistentBag(),Evaluation.LAZY);
    }



    default <T> BagX<T> fromStream(final ReactiveSeq<T> stream) {
        return new LazyPBagX<>(null,ReactiveSeq.fromStream(stream),Reducers.toPersistentBag(),Evaluation.LAZY);
    }
    /**
     * coflatMap pattern, can be used to perform lazy reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *     BagX.of(1,2,3)
     *          .map(i->i*2)
     *          .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *      //BagX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed BagX
     */
    default <R> BagX<R> coflatMap(Function<? super BagX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }

    /**
    * Combine two adjacent elements in a BagX using the supplied BinaryOperator
    * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
    * with it's neighbor
    * <pre>
    * {@code
    *  BagX.of(1,1,2,3)
                 .combine((a, b)->a.equals(b),SemigroupK.intSum)
                 .listX()

    *  //ListX(3,4)
    * }</pre>
    *
    * @param predicate Test to see if two neighbors should be joined
    * @param op Reducer to combine neighbors
    * @return Combined / Partially Reduced BagX
    */
    @Override
    default BagX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (BagX<T>) LazyCollectionX.super.combine(predicate, op);
    }
    @Override
    default BagX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (BagX<T>)LazyCollectionX.super.combine(op,predicate);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Pure#unit(java.lang.Object)
     */
    @Override
    default <R> BagX<R> unit(final R value) {
        return singleton(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <R> BagX<R> unitIterable(final Iterable<R> it) {
        return fromIterable(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.persistent.LazyCollectionX#unit(java.util.Collection)
     */
    @Override
    default <R> BagX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.persistent.LazyCollectionX#emptyUnit()
     */
  //  @Override
    default <R> BagX<R> emptyUnit() {
        return empty();
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.persistent.LazyCollectionX#from(java.util.Collection)
     */
    @Override
    default <X> BagX<X> from(final Iterable<X> col) {
        return fromIterable(col);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.persistent.LazyCollectionX#monoid()
     */
 //   @Override
    default <T> Reducer<PersistentBag<T>,T> monoid() {
        return Reducers.toPersistentBag();
    }


    @Override
    public BagX<T> plus(T e);


    @Override
    public BagX<T> plusAll(Iterable<? extends T> list);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#removeValue(java.lang.Object)
     */
    @Override
    public BagX<T> removeValue(T e);


    @Override
    public BagX<T> removeAll(Iterable<? extends T> list);

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#reverse()
     */
    @Override
    default BagX<T> reverse() {
        return (BagX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default BagX<T> filter(final Predicate<? super T> pred) {
        return (BagX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> BagX<R> map(final Function<? super T, ? extends R> mapper) {
        return (BagX<R>) LazyCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> BagX<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (BagX<R>) LazyCollectionX.super.concatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limit(long)
     */
    default BagX<T> take(final long num) {
        return (BagX<T>) LazyCollectionX.super.take(num);
    }


    default BagX<T> drop(final long num) {
        return (BagX<T>) LazyCollectionX.super.drop(num);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#slice(long, long)
     */
    @Override
    default BagX<T> slice(final long from, final long to) {
        return (BagX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> BagX<T> sorted(final Function<? super T, ? extends U> function) {
        return (BagX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    default BagX<Vector<T>> grouped(final int groupSize) {
        return (BagX<Vector<T>>) LazyCollectionX.super.grouped(groupSize);
    }



    @Override
    default <U> BagX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (BagX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> BagX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (BagX<R>) LazyCollectionX.super.zip(other, zipper);
    }


  /* (non-Javadoc)
   * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#permutations()
   */
    @Override
    default BagX<ReactiveSeq<T>> permutations() {

        return (BagX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#combinations(int)
     */
    @Override
    default BagX<ReactiveSeq<T>> combinations(final int size) {

        return (BagX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#combinations()
     */
    @Override
    default BagX<ReactiveSeq<T>> combinations() {

        return (BagX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    @Override
    default BagX<Seq<T>> sliding(final int windowSize) {
        return (BagX<Seq<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default BagX<Seq<T>> sliding(final int windowSize, final int increment) {
        return (BagX<Seq<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default BagX<T> scanLeft(final Monoid<T> monoid) {
        return (BagX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> BagX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (BagX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default BagX<T> scanRight(final Monoid<T> monoid) {
        return (BagX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> BagX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (BagX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default BagX<T> plusInOrder(final T e) {

        return (BagX<T>) LazyCollectionX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycle(int)
     */
    @Override
    default BagX<T> cycle(final long times) {

        return (BagX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default BagX<T> cycle(final Monoid<T> m, final long times) {

        return (BagX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default BagX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (BagX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default BagX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (BagX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip(java.util.stream.Stream)
     */
    @Override
    default <U> BagX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {
        return (BagX) LazyCollectionX.super.zipWithStream(other);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> BagX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (BagX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> BagX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                          final Iterable<? extends T4> fourth) {

        return (BagX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zipWithIndex()
     */
    @Override
    default BagX<Tuple2<T, Long>> zipWithIndex() {

        return (BagX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#distinct()
     */
    @Override
    default BagX<T> distinct() {

        return (BagX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted()
     */
    @Override
    default BagX<T> sorted() {

        return (BagX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default BagX<T> sorted(final Comparator<? super T> c) {

        return (BagX<T>) LazyCollectionX.super.sorted(c);
    }


    default BagX<T> dropWhile(final Predicate<? super T> p) {

        return (BagX<T>) LazyCollectionX.super.dropWhile(p);
    }


    default BagX<T> dropUntil(final Predicate<? super T> p) {

        return (BagX<T>) LazyCollectionX.super.dropUntil(p);
    }


    default BagX<T> takeWhile(final Predicate<? super T> p) {

        return (BagX<T>) LazyCollectionX.super.takeWhile(p);
    }


    default BagX<T> takeUntil(final Predicate<? super T> p) {

        return (BagX<T>) LazyCollectionX.super.takeUntil(p);
    }


    @Override
    default BagX<T> intersperse(final T value) {

        return (BagX<T>) LazyCollectionX.super.intersperse(value);
    }


    @Override
    default BagX<T> shuffle() {

        return (BagX<T>) LazyCollectionX.super.shuffle();
    }


    default BagX<T> dropRight(final int num) {

        return (BagX<T>) LazyCollectionX.super.dropRight(num);
    }


    default BagX<T> takeRight(final int num) {

        return (BagX<T>) LazyCollectionX.super.takeRight(num);
    }


    @Override
    default BagX<T> onEmptySwitch(final Supplier<? extends PersistentBag<T>> supplier) {
        if (isEmpty())
            return BagX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default BagX<T> onEmpty(final T value) {

        return (BagX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default BagX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (BagX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> BagX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (BagX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default BagX<T> shuffle(final Random random) {

        return (BagX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> BagX<U> ofType(final Class<? extends U> type) {

        return (BagX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default BagX<T> filterNot(final Predicate<? super T> fn) {

        return (BagX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#notNull()
     */
    @Override
    default BagX<T> notNull() {

        return (BagX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default BagX<T> removeStream(final Stream<? extends T> stream) {

        return (BagX<T>) LazyCollectionX.super.removeStream(stream);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default BagX<T> removeAll(final T... values) {

        return (BagX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default BagX<T> retainAll(final Iterable<? extends T> it) {

        return (BagX<T>) LazyCollectionX.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default BagX<T> retainStream(final Stream<? extends T> seq) {

        return (BagX<T>) LazyCollectionX.super.retainStream(seq);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default BagX<T> retainAll(final T... values) {

        return (BagX<T>) LazyCollectionX.super.retainAll(values);
    }


    @Override
    default <C extends PersistentCollection<? super T>> BagX<C> grouped(final int size, final Supplier<C> supplier) {

        return (BagX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default BagX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (BagX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default BagX<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (BagX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default BagX<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (BagX<Vector<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends PersistentCollection<? super T>> BagX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (BagX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends PersistentCollection<? super T>> BagX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (BagX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default <R> BagX<R> retry(final Function<? super T, ? extends R> fn) {
        return (BagX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> BagX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (BagX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> BagX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (BagX<R>)LazyCollectionX.super.flatMap(fn);
    }

    @Override
    default <R> BagX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (BagX<R>)LazyCollectionX.super.mergeMap(fn);
    }

    @Override
    default BagX<T> prependStream(Stream<? extends T> stream) {
        return (BagX<T>)LazyCollectionX.super.prependStream(stream);
    }

    @Override
    default BagX<T> appendAll(T... values) {
        return (BagX<T>)LazyCollectionX.super.appendAll(values);
    }

    @Override
    default BagX<T> append(T value) {
        return (BagX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default BagX<T> prepend(T value) {
        return (BagX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default BagX<T> prependAll(T... values) {
        return (BagX<T>)LazyCollectionX.super.prependAll(values);
    }

    @Override
    default BagX<T> insertAt(int pos, T... values) {
        return (BagX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default BagX<T> deleteBetween(int start, int end) {
        return (BagX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default BagX<T> insertStreamAt(int pos, Stream<T> stream) {
        return (BagX<T>)LazyCollectionX.super.insertStreamAt(pos,stream);
    }

    @Override
    default BagX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (BagX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> BagX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (BagX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default BagX<T> plusLoop(int max, IntFunction<T> value) {
        return (BagX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default BagX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (BagX<T>)LazyCollectionX.super.plusLoop(supplier);
    }


    static <T> BagX<T> fromIterator(Iterator<T> iterator) {
        return fromIterable(()->iterator);
    }

  @Override
    default <T2, R> BagX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (BagX<R>)LazyCollectionX.super.zip(fn, publisher);
    }

    @Override
    default <U> BagX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (BagX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> BagX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (BagX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> BagX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (BagX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }

  @Override
  default BagX<T> removeAll(CollectionX<? extends T> it) {
    return removeAll(narrowIterable());
  }

  public static  <T,R> BagX<R> tailRec(T initial, Function<? super T, ? extends BagX<? extends Either<T, R>>> fn) {
        return ListX.tailRec(initial,fn).to().bagX(Evaluation.LAZY);
    }

}
