package cyclops.collections.mutable;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.LazySetX;
import com.oath.cyclops.hkt.Higher;

import com.oath.cyclops.types.foldable.Evaluation;

import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.collections.immutable.VectorX;
import cyclops.control.*;
import cyclops.function.Monoid;
import com.oath.cyclops.hkt.DataWitness.set;
import cyclops.reactive.ReactiveSeq;
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
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oath.cyclops.types.foldable.Evaluation.LAZY;

/**
 * An eXtended Set type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this SetX
 */
public interface SetX<T> extends To<SetX<T>>,Set<T>, LazyCollectionX<T>, Higher<set,T>,OnEmptySwitch<T, Set<T>> {

    public static <T> SetX<T> defer(Supplier<SetX<T>> s){
        return of(s)
                .map(Supplier::get)
                .flatMap(l->l);
    }

    static <T> CompletableSetX<T> completable(){
        return new CompletableSetX<>();
    }

    static class CompletableSetX<T> implements InvocationHandler {
        cyclops.async.Future<SetX<T>> future    = cyclops.async.Future.future();
        public boolean complete(Set<T> result){
            return future.complete(SetX.fromIterable(result));
        }

        public SetX<T> asSetX(){
            SetX f = (SetX) Proxy.newProxyInstance(SetX.class.getClassLoader(),
                    new Class[] { SetX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //@TODO
            SetX<T> target = future.visit(l->l,t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }
    SetX<T> lazy();
    SetX<T> eager();
    public static <W1,T> Nested<set,W1,T> nested(SetX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<set,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,set,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<set,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R> Nested<set,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn),Instances.definitions(), defs);
    }
    public static  <T> Kleisli<set,SetX<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), SetX::widen);
    }
    public static <T> Higher<set, T> widen(SetX<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<set,T,SetX<T>> kindCokleisli(){
        return Cokleisli.of(SetX::narrowK);
    }
    /**
     * Create a SetX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range SetX
     */
    public static SetX<Integer> range(final int start, final int end) {

        return ReactiveSeq.range(start, end)
                          .to()
                          .setX(LAZY);
    }

    /**
     * Create a SetX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range SetX
     */
    public static SetX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to()
                          .setX(LAZY);
    }

    /**
     * Unfold a function into a SetX
     *
     * <pre>
     * {@code
     *  SetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</pre>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return SetX generated by unfolder function
     */
    static <U, T> SetX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to()
                          .setX(LAZY);
    }

    /**
     * Generate a SetX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate SetX elements
     * @return SetX generated from the provided Supplier
     */
    public static <T> SetX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit).to()
                          .setX(LAZY);
    }

    /**
     * Create a SetX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return SetX generated by iterative application
     */
    public static <T> SetX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit).to()
                          .setX(LAZY);

    }

    static <T> Collector<T, ?, SetX<T>> setXCollector() {
        return Collectors.toCollection(() -> SetX.of());
    }

    static <T> Collector<T, ?, Set<T>> defaultCollector() {
        return Collectors.toCollection(() -> new HashSet<>());
    }

    static <T> Collector<T, ?, Set<T>> immutableCollector() {
        return Collectors.collectingAndThen(defaultCollector(), (final Set<T> d) -> Collections.unmodifiableSet(d));

    }

    public static <T> SetX<T> empty() {
        return fromIterable((Set<T>) defaultCollector().supplier()
                                                       .get());
    }

    @SafeVarargs
    public static <T> SetX<T> of(final T... values) {
      return new LazySetX<T>(null,
                ReactiveSeq.of(values),
                defaultCollector(), LAZY);
    }
    public static <T> SetX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }
    public static <T> SetX<T> singleton(final T value) {
        return SetX.<T> of(value);
    }

    /**
     * Construct a SetX from an Publisher
     *
     * @param publisher
     *            to construct SetX from
     * @return SetX
     */
    public static <T> SetX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to()
                          .setX(LAZY);
    }
    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  SetX<Integer> set = setX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create SetX from
     * @param <T> SetX generated from Stream
     * @return
     */
    public static <T> SetX<T> setX(ReactiveSeq<T> stream){
        return new LazySetX<T>(null,
                stream,
                defaultCollector(), LAZY);
    }



    public static <T> SetX<T> fromIterable(final Iterable<T> it) {
        if (it instanceof SetX)
            return (SetX) it;
        if (it instanceof Set)
            return new LazySetX<T>(
                                   (Set) it, defaultCollector(), LAZY);
        return new LazySetX<T>(null,ReactiveSeq.fromIterable(it),
                                          defaultCollector(), LAZY);
    }

    public static <T> SetX<T> fromIterable(final Collector<T, ?, Set<T>> collector, final Iterable<T> it) {
        if (it instanceof SetX)
            return ((SetX) it).type(collector);
        if (it instanceof Set)
            return new LazySetX<T>(
                                   (Set) it, collector, LAZY);
        return new LazySetX<T>(null,
                                ReactiveSeq.fromIterable(it),
                                collector, LAZY);
    }
    @Override
    default Object[] toArray(){
        return LazyCollectionX.super.toArray();
    }

    @Override
    default <T1> T1[] toArray(T1[] a){
        return LazyCollectionX.super.toArray(a);
    }

    @Override
    default SetX<T> materialize() {
        return (SetX<T>)LazyCollectionX.super.materialize();
    }

    @Override
    default SetX<T> take(final long num) {

        return (SetX<T>) LazyCollectionX.super.limit(num);
    }
    @Override
    default SetX<T> drop(final long num) {

        return (SetX<T>) LazyCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> SetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (SetX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> SetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (SetX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> SetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (SetX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> SetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (SetX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> SetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (SetX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> SetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (SetX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }

    SetX<T> type(Collector<T, ?, Set<T>> collector);

    /**
     * coflatMap pattern, can be used to perform lazy reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *     SetX.of(1,2,3)
     *           .map(i->i*2)
     *           .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *      //SetX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed Set
     */
    default <R> SetX<R> coflatMap(Function<? super SetX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                  .apply(this);
    }

    /**
     * Combine two adjacent elements in a SetX using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code
     *  SetX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .listX(Evaluation.LAZY)

     *  //ListX(3,4)
     * }</pre>
     *
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced SetX
     */
    @Override
    default SetX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (SetX<T>) LazyCollectionX.super.combine(predicate, op);
    }
    @Override
    default SetX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (SetX<T>)LazyCollectionX.super.combine(op,predicate);
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    @Override
    default <R> SetX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> SetX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> SetX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    @Override
    default <T1> SetX<T1> from(final Iterable<T1> c) {
        return SetX.<T1> fromIterable(getCollector(), c);
    }

    public <T> Collector<T, ?, Set<T>> getCollector();

    @Override
    default <X> SetX<X> fromStream(final ReactiveSeq<X> stream) {
        return new LazySetX<>(
                              stream.collect(getCollector()), getCollector(), LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#reverse()
     */
    @Override
    default SetX<T> reverse() {
        return (SetX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default SetX<T> filter(final Predicate<? super T> pred) {

        return (SetX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> SetX<R> map(final Function<? super T, ? extends R> mapper) {

        return (SetX<R>) LazyCollectionX.super.<R> map(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> SetX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (SetX<R>) LazyCollectionX.super.<R> flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limit(long)
     */
    @Override
    default SetX<T> limit(final long num) {
        return (SetX<T>) LazyCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#skip(long)
     */
    @Override
    default SetX<T> skip(final long num) {

        return (SetX<T>) LazyCollectionX.super.skip(num);
    }

    @Override
    default SetX<T> takeRight(final int num) {
        return (SetX<T>) LazyCollectionX.super.takeRight(num);
    }

    @Override
    default SetX<T> dropRight(final int num) {
        return (SetX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default SetX<T> takeWhile(final Predicate<? super T> p) {

        return (SetX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default SetX<T> dropWhile(final Predicate<? super T> p) {

        return (SetX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default SetX<T> takeUntil(final Predicate<? super T> p) {

        return (SetX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default SetX<T> dropUntil(final Predicate<? super T> p) {

        return (SetX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> SetX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (SetX<R>) LazyCollectionX.super.<R> trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#slice(long, long)
     */
    @Override
    default SetX<T> slice(final long from, final long to) {

        return (SetX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> SetX<T> sorted(final Function<? super T, ? extends U> function) {

        return (SetX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    default SetX<ListX<T>> grouped(final int groupSize) {
        return (SetX) LazyCollectionX.super.grouped(groupSize);
    }



    @Override
    default <U> SetX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (SetX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> SetX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SetX<R>) LazyCollectionX.super.zip(other, zipper);
    }


  @Override
    default SetX<VectorX<T>> sliding(final int windowSize) {
        return (SetX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default SetX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (SetX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default SetX<T> scanLeft(final Monoid<T> monoid) {
        return (SetX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> SetX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (SetX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default SetX<T> scanRight(final Monoid<T> monoid) {
        return (SetX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> SetX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (SetX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    @Override
    default SetX<T> plus(final T e) {
        add(e);
        return this;
    }

    @Override
    default SetX<T> plusAll(final Iterable<? extends T> list) {
        for(T next : list) {
            add(next);
        }
        return this;
    }

    @Override
    default SetX<T> removeValue(final T e) {
        remove(e);
        return this;
    }

    @Override
    default SetX<T> removeAll(final Iterable<? extends T> list) {
        for(T next : list) {
            removeValue(next);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycle(int)
     */
    @Override
    default ListX<T> cycle(final long times) {

        return this.stream()
                   .cycle(times)
                   .to()
                   .listX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default ListX<T> cycle(final Monoid<T> m, final long times) {

        return this.stream()
                   .cycle(m, times)
                   .to()
                   .listX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleWhile(predicate)
                   .to()
                   .listX(LAZY);
    }
    /**

    default ConvertableSequence<T> to(){


        return new ConvertableSequence<>(this);
    }
    default Collectable<T> collectors(){


        return Seq.seq(this);
    }
     **/

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleUntil(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleUntil(predicate)
                   .to()
                   .listX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip(java.util.stream.Stream)
     */
    @Override
    default <U> SetX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (SetX) LazyCollectionX.super.zipWithStream(other);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> SetX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (SetX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> SetX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (SetX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zipWithIndex()
     */
    @Override
    default SetX<Tuple2<T, Long>> zipWithIndex() {

        return (SetX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#distinct()
     */
    @Override
    default SetX<T> distinct() {

        return (SetX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#sorted()
     */
    @Override
    default SetX<T> sorted() {

        return (SetX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default SetX<T> sorted(final Comparator<? super T> c) {

        return (SetX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default SetX<T> skipWhile(final Predicate<? super T> p) {

        return (SetX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default SetX<T> skipUntil(final Predicate<? super T> p) {

        return (SetX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default SetX<T> limitWhile(final Predicate<? super T> p) {

        return (SetX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default SetX<T> limitUntil(final Predicate<? super T> p) {

        return (SetX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default SetX<T> intersperse(final T value) {

        return (SetX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#shuffle()
     */
    @Override
    default SetX<T> shuffle() {

        return (SetX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#skipLast(int)
     */
    @Override
    default SetX<T> skipLast(final int num) {

        return (SetX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitLast(int)
     */
    @Override
    default SetX<T> limitLast(final int num) {

        return (SetX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default SetX<T> onEmptySwitch(final Supplier<? extends Set<T>> supplier) {
        if (isEmpty())
            return SetX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default SetX<T> onEmpty(final T value) {

        return (SetX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default SetX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (SetX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> SetX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (SetX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default SetX<T> shuffle(final Random random) {

        return (SetX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> SetX<U> ofType(final Class<? extends U> type) {

        return (SetX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default SetX<T> filterNot(final Predicate<? super T> fn) {

        return (SetX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#notNull()
     */
    @Override
    default SetX<T> notNull() {

        return (SetX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default SetX<T> removeAllS(final Stream<? extends T> stream) {

        return (SetX<T>) LazyCollectionX.super.removeAllS(stream);
    }

    @Override
    default SetX<T> removeAll(CollectionX<? extends T> it) {
      return removeAll(narrowIterable());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default SetX<T> removeAll(final T... values) {

        return (SetX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default SetX<T> retainAllI(final Iterable<? extends T> it) {

        return (SetX<T>) LazyCollectionX.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default SetX<T> retainAllS(final Stream<? extends T> seq) {

        return (SetX<T>) LazyCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default SetX<T> retainAll(final T... values) {

        return (SetX<T>) LazyCollectionX.super.retainAll(values);
    }




    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> SetX<C> grouped(final int size, final Supplier<C> supplier) {

        return (SetX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default SetX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (SetX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default SetX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (SetX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> SetX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (SetX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> SetX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (SetX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default SetX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (SetX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }


    @Override
    default <R> SetX<R> retry(final Function<? super T, ? extends R> fn) {
        return (SetX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> SetX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (SetX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> SetX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (SetX<R>)LazyCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> SetX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (SetX<R>)LazyCollectionX.super.flatMapP(fn);
    }

    @Override
    default SetX<T> prependS(Stream<? extends T> stream) {
        return (SetX<T>)LazyCollectionX.super.prependS(stream);
    }

    @Override
    default SetX<T> append(T... values) {
        return (SetX<T>)LazyCollectionX.super.append(values);
    }

    @Override
    default SetX<T> append(T value) {
        return (SetX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default SetX<T> prepend(T value) {
        return (SetX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default SetX<T> prependAll(T... values) {
        return (SetX<T>)LazyCollectionX.super.prependAll(values);
    }

    @Override
    default SetX<T> insertAt(int pos, T... values) {
        return (SetX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default SetX<T> deleteBetween(int start, int end) {
        return (SetX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default SetX<T> insertAtS(int pos, Stream<T> stream) {
        return (SetX<T>)LazyCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default SetX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (SetX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> SetX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (SetX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default SetX<T> plusLoop(int max, IntFunction<T> value) {
        return (SetX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default SetX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (SetX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

  @Override
    default <T2, R> SetX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (SetX<R>)LazyCollectionX.super.zip(fn, publisher);
    }



    @Override
    default <U> SetX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (SetX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> SetX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (SetX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> SetX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (SetX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }

    /**
     * Narrow a covariant Set
     *
     * <pre>
     * {@code
     * SetX<? extends Fruit> set = SetX.of(apple,bannana);
     * SetX<Fruit> fruitSet = SetX.narrowK(set);
     * }
     * </pre>
     *
     * @param setX to narrowK generic type
     * @return SetX with narrowed type
     */
    public static <T> SetX<T> narrow(final SetX<? extends T> setX) {
        return (SetX<T>) setX;
    }

    public static <T> SetX<T> narrowK(final Higher<set, T> set) {
        return (SetX<T>)set;
    }
    static class Instances {

        public static InstanceDefinitions<set> definitions(){
            return new InstanceDefinitions<set>() {
                @Override
                public <T, R> Functor<set> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<set> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<set> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<set> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<set>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<set>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<set> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<set>> monadPlus(MonoidK<set> m) {
                    return Maybe.just(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Traverse<set> traverse() {
                    return  Instances.traverse();
                }

                @Override
                public <T> Foldable<set> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<set>> comonad() {
                    return Maybe.nothing();
                }
                @Override
                public <T> Maybe<Unfoldable<set>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };

        }
        public static Unfoldable<set> unfoldable(){
            return new Unfoldable<set>() {
                @Override
                public <R, T> Higher<set, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
                    return SetX.unfold(b,fn);
                }
            };
        }
        /**
         *
         * Transform a set, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  SetX<Integer> set = Sets.functor().map(i->i*2, SetX.widen(Arrays.asSet(1,2,3));
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
         *   SetX<Integer> set = Sets.unit()
        .unit("hello")
        .applyHKT(h->Sets.functor().map((String v) ->v.length(), h))
        .convert(SetX::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Sets
         */
        public static <T,R>Functor<set> functor(){
            BiFunction<SetX<T>,Function<? super T, ? extends R>,SetX<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * SetX<String> set = Sets.unit()
        .unit("hello")
        .convert(SetX::narrowK3);

        //Arrays.asSet("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Sets
         */
        public static <T> Pure<set> unit(){
            return General.<set,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.oath.cyclops.hkt.jdk.SetX.widen;
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
         * SetX<Function<Integer,Integer>> setFn =Sets.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(SetX::narrowK3);

        SetX<Integer> set = Sets.unit()
        .unit("hello")
        .applyHKT(h->Sets.functor().map((String v) ->v.length(), h))
        .applyHKT(h->Sets.zippingApplicative().ap(setFn, h))
        .convert(SetX::narrowK3);

        //Arrays.asSet("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Sets
         */
        public static <T,R> Applicative<set> zippingApplicative(){
            BiFunction<SetX< Function<T, R>>,SetX<T>,SetX<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.oath.cyclops.hkt.jdk.SetX.widen;
         * SetX<Integer> set  = Sets.monad()
        .flatMap(i->widen(SetX.range(0,i)), widen(Arrays.asSet(1,2,3)))
        .convert(SetX::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    SetX<Integer> set = Sets.unit()
        .unit("hello")
        .applyHKT(h->Sets.monad().flatMap((String v) ->Sets.unit().unit(v.length()), h))
        .convert(SetX::narrowK3);

        //Arrays.asSet("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Sets
         */
        public static <T,R> Monad<set> monad(){

            BiFunction<Higher<set,T>,Function<? super T, ? extends Higher<set,R>>,Higher<set,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        public static <T,R> MonadRec<set> monadRec(){

            return new MonadRec<set>(){
                @Override
                public <T, R> Higher<set, R> tailRec(T initial, Function<? super T, ? extends Higher<set,? extends Either<T, R>>> fn) {
                    return SetX.tailRec(initial,fn.andThen(SetX::narrowK));
                }
            };
        }
        /**
         *
         * <pre>
         * {@code
         *  SetX<String> set = Sets.unit()
        .unit("hello")
        .applyHKT(h->Sets.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(SetX::narrowK3);

        //Arrays.asSet("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<set> monadZero(){

            return General.monadZero(monad(), SetX.empty());
        }

        public static <T> MonadPlus<set> monadPlus(){

            return General.monadPlus(monadZero(), MonoidKs.setXConcat());
        }

        public static <T> MonadPlus<set> monadPlus(MonoidK<set> m){

            return General.monadPlus(monadZero(),m);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<set> traverse(){
            BiFunction<Applicative<C2>,SetX<Higher<C2, T>>,Higher<C2, SetX<T>>> sequenceFn = (ap, set) -> {

                Higher<C2,SetX<T>> identity = ap.unit(SetX.empty());

                BiFunction<Higher<C2,SetX<T>>,Higher<C2,T>,Higher<C2,SetX<T>>> combineToSet =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,SetX<T>>> combineSets = (a,b)-> ap.apBiFn(ap.unit((l1,l2)-> { l1.addAll(l2); return l1;}),a,b); ;

                return set.stream()
                        .reduce(identity,
                                combineToSet,
                                combineSets);


            };
            BiFunction<Applicative<C2>,Higher<set,Higher<C2, T>>,Higher<C2, Higher<set,T>>> sequenceNarrow  =
                    (a,b) -> Instances.widen2(sequenceFn.apply(a, Instances.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Sets.foldable()
        .foldLeft(0, (a,b)->a+b, SetX.widen(Arrays.asSet(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<set> foldable(){
            return new Foldable<set>() {
                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<set, T> ds) {
                    return  fromIterable(narrowK(ds)).foldRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<set, T> ds) {
                    return  fromIterable(narrowK(ds)).foldLeft(monoid);
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<set, T> nestedA) {
                    return narrowK(nestedA).<R>map(fn).foldLeft(mb);
                }
            };
        }

        private static  <T> SetX<T> concat(Set<T> l1, Set<T> l2){
            return SetX.setX(ReactiveSeq.fromStream(Stream.concat(l1.stream(),l2.stream())));
        }
        private static <T> SetX<T> of(T value){
            return SetX.of(value);
        }
        private static <T,R> SetX<R> ap(SetX<Function< T, R>> lt,  SetX<T> set){
            return SetX.fromIterable(lt).zip(set,(a,b)->a.apply(b));
        }
        private static <T,R> Higher<set,R> flatMap( Higher<set,T> lt, Function<? super T, ? extends  Higher<set,R>> fn){
            return SetX.fromIterable(Instances.narrowK(lt)).flatMap(fn.andThen(Instances::narrowK));
        }
        private static <T,R> SetX<R> map(SetX<T> lt, Function<? super T, ? extends R> fn){
            return SetX.fromIterable(lt).map(fn);
        }



        /**
         * Widen a SetType nest inside another HKT encoded type
         *
         * @param flux HTK encoded type containing  a Set to widen
         * @return HKT encoded type with a widened Set
         */
        public static <C2, T> Higher<C2, Higher<set, T>> widen2(Higher<C2, SetX<T>> flux) {
            // a functor could be used (if C2 is a functor / one exists for C2 type)
            // instead of casting
            // cast seems safer as Higher<set,T> must be a SetX
            return (Higher) flux;
        }



        /**
         * Convert the raw Higher Kinded Type for SetType types into the SetType type definition class
         *
         * @param future HKT encoded set into a SetType
         * @return SetType
         */
        public static <T> SetX<T> narrowK(final Higher<set, T> future) {
            return (SetX<T>) future;
        }

        /**
         * Convert the HigherKindedType definition for a Set into
         *
         * @param completableSet Type Constructor to convert back into narrowed type
         * @return Set from Higher Kinded Type
         */
        public static <T> SetX<T> narrow(final Higher<set, T> completableSet) {

            return ((SetX<T>) completableSet);

        }
    }
    public static  <T,R> SetX<R> tailRec(T initial, Function<? super T, ? extends SetX<? extends Either<T, R>>> fn) {
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
        return Either.sequenceRight(next).orElse(ReactiveSeq.empty()).to().setX(Evaluation.LAZY);
    }

}
