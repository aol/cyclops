package cyclops.collections.immutable;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import cyclops.control.*;
import cyclops.data.HashSet;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.LazyPSetX;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.companion.Reducers;
import com.oath.cyclops.hkt.DataWitness.persistentSetX;
import cyclops.reactive.ReactiveSeq;
import cyclops.collections.mutable.ListX;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.foldable.To;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.reactive.Spouts;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;

import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import com.oath.cyclops.types.persistent.PersistentSet;
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
 * An eXtended Persistent Set type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface PersistentSetX<T> extends To<PersistentSetX<T>>,PersistentSet<T>, Higher<persistentSetX,T>,LazyCollectionX<T>, OnEmptySwitch<T, PersistentSet<T>> {

    PersistentSetX<T> lazy();
    PersistentSetX<T> eager();

    static <T> CompletablePersistentSetX<T> completable(){
        return new CompletablePersistentSetX<>();
    }

    static class CompletablePersistentSetX<T> implements InvocationHandler {
        Future<PersistentSetX<T>> future = Future.future();
        public boolean complete(PersistentSet<T> result){
            return future.complete(PersistentSetX.fromIterable(result));
        }

        public PersistentSetX<T> asPersistentSetX(){
            PersistentSetX f = (PersistentSetX) Proxy.newProxyInstance(PersistentQueueX.class.getClassLoader(),
                    new Class[] { PersistentSetX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            PersistentSetX<T> target = future.visit(l->l,t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }
    public static  <T> Kleisli<persistentSetX,PersistentSetX<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), PersistentSetX::widen);
    }
    public static <T> Higher<persistentSetX, T> widen(PersistentSetX<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<persistentSetX,T,PersistentSetX<T>> kindCokleisli(){
        return Cokleisli.of(PersistentSetX::narrowK);
    }
    public static <W1,T> Nested<persistentSetX,W1,T> nested(PersistentSetX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<persistentSetX,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,persistentSetX,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<persistentSetX,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R> Nested<persistentSetX,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn),Instances.definitions(), defs);
    }

    /**
     * Narrow a covariant PersistentSetX
     *
     * <pre>
     * {@code
     *  PersistentSetX<? extends Fruit> set = PersistentSetX.of(apple,bannana);
     *  PersistentSetX<Fruit> fruitSet = PersistentSetX.narrowK(set);
     * }
     * </pre>
     *
     * @param setX to narrowK generic type
     * @return PersistentSetX with narrowed type
     */
    public static <T> PersistentSetX<T> narrow(final PersistentSetX<? extends T> setX) {
        return (PersistentSetX<T>) setX;
    }
    /**
     * Create a PersistentSetX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range PersistentSetX
     */
    public static PersistentSetX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .to().persistentSetX(LAZY);
    }

    /**
     * Create a PersistentSetX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range PersistentSetX
     */
    public static PersistentSetX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                .to().persistentSetX(LAZY);
    }

    /**
     * Unfold a function into a PersistentSetX
     *
     * <pre>
     * {@code
     *  PersistentSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5) in any order
     *
     * }</code>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return PersistentSetX generated by unfolder function
     */
    static <U, T> PersistentSetX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                .to().persistentSetX(LAZY);
    }

    /**
     * Generate a PersistentSetX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate PersistentSetX elements
     * @return PersistentSetX generated from the provided Supplier
     */
    public static <T> PersistentSetX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                .to().persistentSetX(LAZY);
    }

    /**
     * Create a PersistentSetX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return PersistentSetX generated by iterative application
     */
    public static <T> PersistentSetX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                .to().persistentSetX(LAZY);

    }

    public static <T> PersistentSetX<T> of(final T... values) {

        return new LazyPSetX<>(null,ReactiveSeq.of(values),Reducers.toPersistentSet(), LAZY);
    }

    public static <T> PersistentSetX<T> empty() {
        return new LazyPSetX<>(
                               HashSet.empty(),null,Reducers.toPersistentSet(), LAZY);
    }

    public static <T> PersistentSetX<T> singleton(final T value) {
        return new LazyPSetX<>(
                               HashSet.of(value),null,Reducers.toPersistentSet(), LAZY);
    }
    PersistentSetX<T> type(Reducer<? extends PersistentSet<T>,T> reducer);
    /**
    default ConvertableSequence<T> to(){

        return new ConvertableSequence<>(this);
    }
    default Collectable<T> collectors(){


        return Seq.seq(this);
    }
     **/
    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  PeristentSetX<Integer> bag = persistentSetX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create a PersistentSetX from
     * @param <T> PersistentSetX generated from Stream
     * @return
     */
    public static <T> PersistentSetX<T> persistentSetX(ReactiveSeq<T> stream) {
        return new LazyPSetX<>(null,stream,Reducers.toPersistentSet(), LAZY);
    }
    public static <T> PersistentSetX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof PersistentSetX)
            return (PersistentSetX) iterable;
        if (iterable instanceof PersistentSet)
            return new LazyPSetX<>(
                                   (PersistentSet) iterable,null,Reducers.toPersistentSet(), LAZY);


        return new LazyPSetX<>(null,
                ReactiveSeq.fromIterable(iterable),
                Reducers.toPersistentSet(), LAZY);
    }

    /**
     * Construct a PersistentSetX from an Publisher
     *
     * @param publisher
     *            to construct PersistentSetX from
     * @return PersistentSetX
     */
    public static <T> PersistentSetX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher)
                .to().persistentSetX(LAZY);
    }



    default <T> PersistentSetX<T> fromStream(final ReactiveSeq<T> stream) {
        return Reducers.<T>toPersistentSetX()
                       .mapReduce(stream);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> PersistentSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                       BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                       Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                       Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (PersistentSetX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> PersistentSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                       BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                       Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                       Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                       Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (PersistentSetX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> PersistentSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                   BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                   Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (PersistentSetX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> PersistentSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                   BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                   Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                   Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (PersistentSetX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> PersistentSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                               BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (PersistentSetX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> PersistentSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                               BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                               BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (PersistentSetX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }


    @Override
    default PersistentSetX<T> take(final long num) {
        return limit(num);
    }
    @Override
    default PersistentSetX<T> drop(final long num) {

        return skip(num);
    }

    /**
     * coflatMap pattern, can be used to perform lazy reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *     PersistentSetX.of(1,2,3)
     *          .map(i->i*2)
     *          .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *     //PersistentSetX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed PersistentSetX
     */
    default <R> PersistentSetX<R> coflatMap(Function<? super PersistentSetX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }

    /**
    * Combine two adjacent elements in a PersistentSetX using the supplied BinaryOperator
    * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
    * with it's neighbor
    * <pre>
    * {@code
    *  PersistentSetX.of(1,1,2,3)
                 .combine((a, b)->a.equals(b),SemigroupK.intSum)
                 .listX()

    *  //ListX(3,4)
    * }</pre>
    *
    * @param predicate Test to see if two neighbors should be joined
    * @param op Reducer to combine neighbors
    * @return Combined / Partially Reduced PersistentSetX
    */
    @Override
    default PersistentSetX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (PersistentSetX<T>) LazyCollectionX.super.combine(predicate, op);
    }
    @Override
    default PersistentSetX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (PersistentSetX<T>)LazyCollectionX.super.combine(op,predicate);
    }
    @Override
    default boolean isEmpty() {
        return PersistentSet.super.isEmpty();
    }
    @Override
    default <R> PersistentSetX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> PersistentSetX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> PersistentSetX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

   // @Override
    default <R> PersistentSetX<R> emptyUnit() {
        return empty();
    }

    @Override
    default PersistentSetX<T> materialize() {
        return (PersistentSetX<T>)LazyCollectionX.super.materialize();
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    @Override
    default boolean containsValue(T item) {
        return LazyCollectionX.super.containsValue(item);
    }

    @Override
    default <X> PersistentSetX<X> from(final Iterable<X> col) {
        return fromIterable(col);
    }

 //   @Override
    default <T> Reducer<PersistentSet<T>,T> monoid() {
        return Reducers.toPersistentSet();
    }

    /* (non-Javadoc)
     * @see org.pcollections.PSet#plus(java.lang.Object)
     */
    @Override
    public PersistentSetX<T> plus(T e);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#insertAt(java.util.Collection)
     */
    @Override
    public PersistentSetX<T> plusAll(Iterable<? extends T> list);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#removeValue(java.lang.Object)
     */
    @Override
    public PersistentSetX<T> removeValue(T e);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#removeAll(java.util.Collection)
     */
    @Override
    public PersistentSetX<T> removeAll(Iterable<? extends T> list);

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#reverse()
     */
    @Override
    default PersistentSetX<T> reverse() {
        return (PersistentSetX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> filter(final Predicate<? super T> pred) {
        return (PersistentSetX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> PersistentSetX<R> map(final Function<? super T, ? extends R> mapper) {
        return (PersistentSetX<R>) LazyCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> PersistentSetX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (PersistentSetX<R>) LazyCollectionX.super.flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limit(long)
     */
    @Override
    default PersistentSetX<T> limit(final long num) {
        return (PersistentSetX<T>) LazyCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skip(long)
     */
    @Override
    default PersistentSetX<T> skip(final long num) {
        return (PersistentSetX<T>) LazyCollectionX.super.skip(num);
    }

    @Override
    default PersistentSetX<T> takeRight(final int num) {
        return (PersistentSetX<T>) LazyCollectionX.super.takeRight(num);
    }

    @Override
    default PersistentSetX<T> dropRight(final int num) {
        return (PersistentSetX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> takeWhile(final Predicate<? super T> p) {
        return (PersistentSetX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> dropWhile(final Predicate<? super T> p) {
        return (PersistentSetX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> takeUntil(final Predicate<? super T> p) {
        return (PersistentSetX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> dropUntil(final Predicate<? super T> p) {
        return (PersistentSetX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> PersistentSetX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (PersistentSetX<R>) LazyCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#slice(long, long)
     */
    @Override
    default PersistentSetX<T> slice(final long from, final long to) {
        return (PersistentSetX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> PersistentSetX<T> sorted(final Function<? super T, ? extends U> function) {
        return (PersistentSetX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    default PersistentSetX<ListX<T>> grouped(final int groupSize) {
        return (PersistentSetX<ListX<T>>) LazyCollectionX.super.grouped(groupSize);
    }



    @Override
    default <U> PersistentSetX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (PersistentSetX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> PersistentSetX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (PersistentSetX<R>) LazyCollectionX.super.zip(other, zipper);
    }


  /* (non-Javadoc)
   * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#permutations()
   */
    @Override
    default PersistentSetX<ReactiveSeq<T>> permutations() {

        return (PersistentSetX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#combinations(int)
     */
    @Override
    default PersistentSetX<ReactiveSeq<T>> combinations(final int size) {

        return (PersistentSetX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#combinations()
     */
    @Override
    default PersistentSetX<ReactiveSeq<T>> combinations() {

        return (PersistentSetX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    @Override
    default PersistentSetX<VectorX<T>> sliding(final int windowSize) {
        return (PersistentSetX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default PersistentSetX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (PersistentSetX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default PersistentSetX<T> scanLeft(final Monoid<T> monoid) {
        return (PersistentSetX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> PersistentSetX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (PersistentSetX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default PersistentSetX<T> scanRight(final Monoid<T> monoid) {
        return (PersistentSetX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> PersistentSetX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (PersistentSetX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default PersistentSetX<T> plusInOrder(final T e) {

        return (PersistentSetX<T>) LazyCollectionX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycle(int)
     */
    @Override
    default LinkedListX<T> cycle(final long times) {

        return this.stream()
                   .cycle(times)
                .to().linkedListX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default LinkedListX<T> cycle(final Monoid<T> m, final long times) {

        return this.stream()
                   .cycle(m, times)
                .to().linkedListX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleWhile(predicate)
                .to().linkedListX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> cycleUntil(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleUntil(predicate)
                .to().linkedListX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip(java.util.stream.Stream)
     */
    @Override
    default <U> PersistentSetX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {
        return (PersistentSetX) LazyCollectionX.super.zipWithStream(other);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> PersistentSetX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (PersistentSetX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> PersistentSetX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                    final Iterable<? extends T4> fourth) {

        return (PersistentSetX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zipWithIndex()
     */
    @Override
    default PersistentSetX<Tuple2<T, Long>> zipWithIndex() {

        return (PersistentSetX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#distinct()
     */
    @Override
    default PersistentSetX<T> distinct() {

        return (PersistentSetX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted()
     */
    @Override
    default PersistentSetX<T> sorted() {

        return (PersistentSetX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default PersistentSetX<T> sorted(final Comparator<? super T> c) {

        return (PersistentSetX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> skipWhile(final Predicate<? super T> p) {

        return (PersistentSetX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> skipUntil(final Predicate<? super T> p) {

        return (PersistentSetX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> limitWhile(final Predicate<? super T> p) {

        return (PersistentSetX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> limitUntil(final Predicate<? super T> p) {

        return (PersistentSetX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default PersistentSetX<T> intersperse(final T value) {

        return (PersistentSetX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#shuffle()
     */
    @Override
    default PersistentSetX<T> shuffle() {

        return (PersistentSetX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skipLast(int)
     */
    @Override
    default PersistentSetX<T> skipLast(final int num) {

        return (PersistentSetX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limitLast(int)
     */
    @Override
    default PersistentSetX<T> limitLast(final int num) {

        return (PersistentSetX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default PersistentSetX<T> onEmptySwitch(final Supplier<? extends PersistentSet<T>> supplier) {
        if (this.isEmpty())
            return PersistentSetX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default PersistentSetX<T> onEmpty(final T value) {

        return (PersistentSetX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default PersistentSetX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (PersistentSetX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> PersistentSetX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (PersistentSetX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default PersistentSetX<T> shuffle(final Random random) {

        return (PersistentSetX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> PersistentSetX<U> ofType(final Class<? extends U> type) {

        return (PersistentSetX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default PersistentSetX<T> filterNot(final Predicate<? super T> fn) {

        return (PersistentSetX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#notNull()
     */
    @Override
    default PersistentSetX<T> notNull() {

        return (PersistentSetX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default PersistentSetX<T> removeStream(final Stream<? extends T> stream) {

        return (PersistentSetX<T>) LazyCollectionX.super.removeStream(stream);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default PersistentSetX<T> removeAll(final T... values) {

        return (PersistentSetX<T>) LazyCollectionX.super.removeAll(values);
    }
    @Override
    default PersistentSetX<T> removeAll(CollectionX<? extends T> it) {
      return removeAll(narrowIterable());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default PersistentSetX<T> retainAll(final Iterable<? extends T> it) {

        return (PersistentSetX<T>) LazyCollectionX.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default PersistentSetX<T> retainStream(final Stream<? extends T> seq) {

        return (PersistentSetX<T>) LazyCollectionX.super.retainStream(seq);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default PersistentSetX<T> retainAll(final T... values) {

        return (PersistentSetX<T>) LazyCollectionX.super.retainAll(values);
    }


    @Override
    default <C extends Collection<? super T>> PersistentSetX<C> grouped(final int size, final Supplier<C> supplier) {

        return (PersistentSetX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default PersistentSetX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (PersistentSetX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default PersistentSetX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (PersistentSetX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default PersistentSetX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (PersistentSetX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> PersistentSetX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (PersistentSetX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> PersistentSetX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (PersistentSetX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default <R> PersistentSetX<R> retry(final Function<? super T, ? extends R> fn) {
        return (PersistentSetX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> PersistentSetX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (PersistentSetX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> PersistentSetX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (PersistentSetX<R>)LazyCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> PersistentSetX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (PersistentSetX<R>)LazyCollectionX.super.flatMapP(fn);
    }

    @Override
    default PersistentSetX<T> prependS(Stream<? extends T> stream) {
        return (PersistentSetX<T>)LazyCollectionX.super.prependS(stream);
    }

    @Override
    default PersistentSetX<T> appendAll(T... values) {
        return (PersistentSetX<T>)LazyCollectionX.super.appendAll(values);
    }

    @Override
    default PersistentSetX<T> appendAll(T value) {
        return (PersistentSetX<T>)LazyCollectionX.super.appendAll(value);
    }

    @Override
    default PersistentSetX<T> prepend(T value) {
        return (PersistentSetX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default PersistentSetX<T> prependAll(T... values) {
        return (PersistentSetX<T>)LazyCollectionX.super.prependAll(values);
    }
    @Override
    default PersistentSetX<T> insertAt(int pos, T values) {
        LazyCollectionX<T> r = LazyCollectionX.super.insertAt(pos, values);
        return (PersistentSetX<T>)r;
    }

    @Override
    default PersistentSetX<T> insertAt(int pos, T... values) {
        LazyCollectionX<T> r = LazyCollectionX.super.insertAt(pos, values);
        return (PersistentSetX<T>)r;
    }

    @Override
    default PersistentSetX<T> deleteBetween(int start, int end) {
        return (PersistentSetX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default PersistentSetX<T> insertAtS(int pos, Stream<T> stream) {
        return (PersistentSetX<T>)LazyCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default PersistentSetX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (PersistentSetX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> PersistentSetX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (PersistentSetX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }


    @Override
    default PersistentSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (PersistentSetX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default PersistentSetX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (PersistentSetX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

    static <T> PersistentSetX<T> fromIterator(Iterator<T> iterator) {
        return fromIterable(()->iterator);
    }

  @Override
    default <T2, R> PersistentSetX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (PersistentSetX<R>)LazyCollectionX.super.zip(fn, publisher);
    }



    @Override
    default <U> PersistentSetX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (PersistentSetX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> PersistentSetX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (PersistentSetX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> PersistentSetX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (PersistentSetX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }


    public static <T> PersistentSetX<T> narrowK(final Higher<persistentSetX, T> persistentSetX) {
        return (PersistentSetX<T>)persistentSetX;
    }
    static class Instances {

        public static InstanceDefinitions<persistentSetX> definitions(){
            return new InstanceDefinitions<persistentSetX>() {
                @Override
                public <T, R> Functor<persistentSetX> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<persistentSetX> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<persistentSetX> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<persistentSetX> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Option<MonadZero<persistentSetX>> monadZero() {
                    return Option.some(Instances.monadZero());
                }

                @Override
                public <T> Option<MonadPlus<persistentSetX>> monadPlus() {
                    return Option.some(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<persistentSetX> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Option<MonadPlus<persistentSetX>> monadPlus(MonoidK<persistentSetX> m) {
                    return Option.some(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Traverse<persistentSetX> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<persistentSetX> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Option<Comonad<persistentSetX>> comonad() {
                    return Maybe.nothing();
                }
                @Override
                public <T> Option<Unfoldable<persistentSetX>> unfoldable() {
                    return Option.some(Instances.unfoldable());
                }
            };

        }
        public static Unfoldable<persistentSetX> unfoldable(){
            return new Unfoldable<persistentSetX>() {
                @Override
                public <R, T> Higher<persistentSetX, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
                    return PersistentSetX.unfold(b,fn);
                }
            };
        }
        /**
         *
         * Transform a persistentSetX, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  PersistentSetX<Integer> persistentSetX = Sets.functor().map(i->i*2, PersistentSetX.widen(Arrays.asSet(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Sets
         * <pre>
         * {@code
         *   PersistentSetX<Integer> persistentSetX = Sets.unit()
        .unit("hello")
        .applyHKT(h->Sets.functor().map((String v) ->v.length(), h))
        .convert(PersistentSetX::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Sets
         */
        public static <T,R>Functor<persistentSetX> functor(){
            BiFunction<PersistentSetX<T>,Function<? super T, ? extends R>,PersistentSetX<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * PersistentSetX<String> persistentSetX = Sets.unit()
        .unit("hello")
        .convert(PersistentSetX::narrowK3);

        //Arrays.asSet("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Sets
         */
        public static <T> Pure<persistentSetX> unit(){
            return General.<persistentSetX,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.oath.cyclops.hkt.jdk.PersistentSetX.widen;
         * import static com.oath.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asSet;
         *
        Sets.zippingApplicative()
        .ap(widen(asSet(l1(this::multiplyByTwo))),widen(asSet(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * PersistentSetX<Function<Integer,Integer>> persistentSetXFn =Sets.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(PersistentSetX::narrowK3);

        PersistentSetX<Integer> persistentSetX = Sets.unit()
        .unit("hello")
        .applyHKT(h->Sets.functor().map((String v) ->v.length(), h))
        .applyHKT(h->Sets.zippingApplicative().ap(persistentSetXFn, h))
        .convert(PersistentSetX::narrowK3);

        //Arrays.asSet("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Sets
         */
        public static <T,R> Applicative<persistentSetX> zippingApplicative(){
            BiFunction<PersistentSetX< Function<T, R>>,PersistentSetX<T>,PersistentSetX<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.oath.cyclops.hkt.jdk.PersistentSetX.widen;
         * PersistentSetX<Integer> persistentSetX  = Sets.monad()
        .flatMap(i->widen(PersistentSetX.range(0,i)), widen(Arrays.asSet(1,2,3)))
        .convert(PersistentSetX::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    PersistentSetX<Integer> persistentSetX = Sets.unit()
        .unit("hello")
        .applyHKT(h->Sets.monad().flatMap((String v) ->Sets.unit().unit(v.length()), h))
        .convert(PersistentSetX::narrowK3);

        //Arrays.asSet("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Sets
         */
        public static <T,R> Monad<persistentSetX> monad(){

            BiFunction<Higher<persistentSetX,T>,Function<? super T, ? extends Higher<persistentSetX,R>>,Higher<persistentSetX,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        public static <T,R> MonadRec<persistentSetX> monadRec(){

            return new MonadRec<persistentSetX>(){
                @Override
                public <T, R> Higher<persistentSetX, R> tailRec(T initial, Function<? super T, ? extends Higher<persistentSetX,? extends Either<T, R>>> fn) {
                    return PersistentSetX.tailRec(initial,fn.andThen(PersistentSetX::narrowK));
                }
            };
        }
        /**
         *
         * <pre>
         * {@code
         *  PersistentSetX<String> persistentSetX = Sets.unit()
        .unit("hello")
        .applyHKT(h->Sets.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(PersistentSetX::narrowK3);

        //Arrays.asSet("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<persistentSetX> monadZero(){

            return General.monadZero(monad(), PersistentSetX.empty());
        }

        public static <T> MonadPlus<persistentSetX> monadPlus(){
            Monoid<PersistentSetX<T>> m = Monoid.of(PersistentSetX.empty(), Instances::concat);
            Monoid<Higher<persistentSetX,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(), MonoidKs.persistentSetXConcat());
        }

        public static <T> MonadPlus<persistentSetX> monadPlus(MonoidK<persistentSetX> m){

            return General.monadPlus(monadZero(),m);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<persistentSetX> traverse(){
            BiFunction<Applicative<C2>,PersistentSetX<Higher<C2, T>>,Higher<C2, PersistentSetX<T>>> sequenceFn = (ap, persistentSetX) -> {

                Higher<C2,PersistentSetX<T>> identity = ap.unit(PersistentSetX.empty());

                BiFunction<Higher<C2,PersistentSetX<T>>,Higher<C2,T>,Higher<C2,PersistentSetX<T>>> combineToSet =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,PersistentSetX<T>>> combineSets = (a,b)-> ap.apBiFn(ap.unit((l1,l2)-> { l1.addAll(l2); return l1;}),a,b); ;

                return persistentSetX.stream()
                        .reduce(identity,
                                combineToSet,
                                combineSets);


            };
            BiFunction<Applicative<C2>,Higher<persistentSetX,Higher<C2, T>>,Higher<C2, Higher<persistentSetX,T>>> sequenceNarrow  =
                    (a,b) -> Instances.widen2(sequenceFn.apply(a, Instances.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Sets.foldable()
        .foldLeft(0, (a,b)->a+b, PersistentSetX.widen(Arrays.asSet(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T,R> Foldable<persistentSetX> foldable(){
            BiFunction<Monoid<T>,Higher<persistentSetX,T>,T> foldRightFn =  (m,l)-> PersistentSetX.fromIterable(narrow(l)).foldRight(m);
            BiFunction<Monoid<T>,Higher<persistentSetX,T>,T> foldLeftFn = (m,l)-> PersistentSetX.fromIterable(narrow(l)).reduce(m);
            Function3<Monoid<R>, Function<T, R>, Higher<persistentSetX, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);
            return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
        }

        private static  <T> PersistentSetX<T> concat(PersistentSet<T> l1, PersistentSet<T> l2){
            return PersistentSetX.persistentSetX(ReactiveSeq.fromStream(Stream.concat(l1.stream(),l2.stream())));
        }
        private static <T> PersistentSetX<T> of(T value){
            return PersistentSetX.of(value);
        }
        private static <T,R> PersistentSetX<R> ap(PersistentSetX<Function< T, R>> lt,  PersistentSetX<T> persistentSetX){
            return PersistentSetX.fromIterable(lt).zip(persistentSetX,(a,b)->a.apply(b));
        }
        private static <T,R> Higher<persistentSetX,R> flatMap( Higher<persistentSetX,T> lt, Function<? super T, ? extends  Higher<persistentSetX,R>> fn){
            return PersistentSetX.fromIterable(Instances.narrowK(lt)).flatMap(fn.andThen(Instances::narrowK));
        }
        private static <T,R> PersistentSetX<R> map(PersistentSetX<T> lt, Function<? super T, ? extends R> fn){
            return PersistentSetX.fromIterable(lt).map(fn);
        }



        /**
         * Widen a SetType nest inside another HKT encoded type
         *
         * @param flux HTK encoded type containing  a Set to widen
         * @return HKT encoded type with a widened Set
         */
        public static <C2, T> Higher<C2, Higher<persistentSetX, T>> widen2(Higher<C2, PersistentSetX<T>> flux) {
            // a functor could be used (if C2 is a functor / one exists for C2 type)
            // instead of casting
            // cast seems safer as Higher<persistentSetX,T> must be a PersistentSetX
            return (Higher) flux;
        }



        /**
         * Convert the raw Higher Kinded Type for SetType types into the SetType type definition class
         *
         * @param future HKT encoded persistentSetX into a SetType
         * @return SetType
         */
        public static <T> PersistentSetX<T> narrowK(final Higher<persistentSetX, T> future) {
            return (PersistentSetX<T>) future;
        }

        /**
         * Convert the HigherKindedType definition for a Set into
         *
         * @param completableSet Type Constructor to convert back into narrowed type
         * @return Set from Higher Kinded Type
         */
        public static <T> PersistentSetX<T> narrow(final Higher<persistentSetX, T> completableSet) {

            return ((PersistentSetX<T>) completableSet);//.narrow();

        }
    }

    public static  <T,R> PersistentSetX<R> tailRec(T initial, Function<? super T, ? extends PersistentSetX<? extends Either<T, R>>> fn) {
        ListX<Either<T, R>> lazy = ListX.of(Either.left(initial));
        ListX<Either<T, R>> next = lazy.eager();
        boolean newValue[] = {true};
        for(;;){

            next = next.flatMap(e -> e.visit(s -> {
                        newValue[0]=true;
                        return  fn.apply(s); },
                    p -> {
                        newValue[0]=false;
                        return ListX.of(e);
                    }));
            if(!newValue[0])
                break;

        }
        return Either.sequenceRight(next).orElse(ReactiveSeq.empty()).to().persistentSetX(Evaluation.LAZY);
    }

}
