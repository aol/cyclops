package cyclops.collectionx.mutable;

import com.aol.cyclops2.data.collections.extensions.lazy.LazyDequeX;
import com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.async.Future;
import cyclops.control.Either;
import cyclops.typeclasses.*;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.foldable.Evaluation;

import cyclops.collectionx.immutable.VectorX;
import cyclops.companion.CyclopsCollectors;
import cyclops.control.lazy.Maybe;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.deque;
import cyclops.stream.ReactiveSeq;
import cyclops.companion.Streams;
import cyclops.control.lazy.Trampoline;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.foldable.To;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.stream.Spouts;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;
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
import java.util.stream.Stream;

import static com.aol.cyclops2.types.foldable.Evaluation.LAZY;


/**
 * An eXtended Deque type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 * 
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this DequeX
 */
public interface DequeX<T> extends To<DequeX<T>>,
                                   Deque<T>,
                                   LazyCollectionX<T>,
                                   OnEmptySwitch<T, Deque<T>>,
                                   Higher<deque,T>{

    DequeX<T> lazy();
    DequeX<T> eager();

    public static <T> DequeX<T> defer(Supplier<DequeX<T>> s){
        return of(s)
                .map(Supplier::get)
                .flatMap(l->l);
    }


    static <T> CompletableDequeX<T> completable(){
        return new CompletableDequeX<>();
    }

    static class CompletableDequeX<T> implements InvocationHandler {
        Future<DequeX<T>> future = Future.future();
        public boolean complete(Deque<T> result){
            return future.complete(DequeX.fromIterable(result));
        }

        public DequeX<T> asDequeX(){
            DequeX f = (DequeX) Proxy.newProxyInstance(DequeX.class.getClassLoader(),
                    new Class[] { DequeX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            DequeX<T> target = future.visit(l->l,t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }

    public static  <T> Kleisli<deque,DequeX<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), DequeX::widen);
    }
    public static <T> Higher<deque, T> widen(DequeX<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<deque,T,DequeX<T>> kindCokleisli(){
        return Cokleisli.of(DequeX::narrowK);
    }
    public static <W1,T> Nested<deque,W1,T> nested(DequeX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<deque,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,deque,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<deque,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }

    default <W2,R> Nested<deque,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    /**
     * Widen a DequeType nest inside another HKT encoded type
     *
     * @param deque HTK encoded type containing  a Deque to widen
     * @return HKT encoded type with a widened Deque
     */
    public static <C2,T> Higher<C2, Higher<deque,T>> widen2(Higher<C2, DequeX<T>> list){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<DequeType.deque,T> must be a ListType
        return (Higher)list;
    }

    /**
     * Convert the raw Higher Kinded Type for Deque types into the DequeType type definition class
     *
     * @param deque HKT encoded list into a DequeType
     * @return DequeType
     */
    public static <T> DequeX<T> narrowK(final Higher<deque, T> list) {
        return (DequeX<T>)list;
    }

    /**
     * Create a DequeX that contains the Integers between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range DequeX
     */
    public static DequeX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .to()
                          .dequeX(LAZY);
    }

    /**
     * Create a DequeX that contains the Longs between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range DequeX
     */
    public static DequeX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .to()
                          .dequeX(LAZY);
    }

    /**
     * Unfold a function into a DequeX
     * 
     * <pre>
     * {@code 
     *  DequeX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return DequeX generated by unfolder function
     */
    static <U, T> DequeX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .to()
                          .dequeX(LAZY);
    }
    /**
     * Generate a DequeX from the provided value up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Value for DequeX elements
     * @return DequeX generated from the provided Supplier
     */
    public static <T> DequeX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit)
                          .to()
                          .dequeX(LAZY);
    }

    /**
     * Generate a DequeX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate DequeX elements
     * @return DequeX generated from the provided Supplier
     */
    public static <T> DequeX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .to()
                          .dequeX(LAZY);
    }

    /**
     * Create a DequeX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return DequeX generated by iterative application
     */
    public static <T> DequeX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .to()
                          .dequeX(LAZY);

    }

    /**
     * @return A Collector that generates a mutable Deque from a Collection
     */
    static <T> Collector<T, ?, Deque<T>> defaultCollector() {
        return java.util.stream.Collectors.toCollection(() -> new ArrayDeque<>());
    }

    /**
     * @return An zero DequeX
     */
    public static <T> DequeX<T> empty() {
        return fromIterable((Deque<T>) defaultCollector().supplier()
                                                         .get());
    }

    /**
     * Construct a Deque from the provided values
     * 
     * <pre>
     * {@code 
     *     DequeX<Integer> deque = DequeX.of(1,2,3,4);
     * 
     * }</pre>
     *
     * 
     * 
     * @param values to construct a Deque from
     * @return DequeX
     */
    public static <T> DequeX<T> of(final T... values) {
        return new LazyDequeX<T>(null,
                ReactiveSeq.of(values),
                defaultCollector(), LAZY);
    }
    /**
     * 
     * Construct a Deque from the provided Iterator
     * 
     * @param it Iterator to populate Deque
     * @return Newly populated DequeX
     */
    public static <T> DequeX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }

    /**
     * Construct a DequeX with a singleUnsafe value
     * 
     * <pre>
     * {@code 
     *     DequeX<Integer> deque = DequeX.of(1);
     * 
     * }</pre>
     * 
     * 
     * @param value Active value
     * @return DequeX
     */
    public static <T> DequeX<T> singleton(final T value) {
        return of(value);
    }

    /**
     * Construct a DequeX from an Publisher
     * 
     * @param publisher
     *            to construct DequeX from
     * @return DequeX
     */
    public static <T> DequeX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher)
                          .to()
                          .dequeX(LAZY);
    }

    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  DequeX<Integer> deque = dequeX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create DequeX from
     * @param <T> DequeX generated from Stream
     * @return
     */
    public static <T> DequeX<T> dequeX(ReactiveSeq<T> stream){
        return new LazyDequeX<T>(null,
                stream,
                defaultCollector(), LAZY);
    }

    /**
     * Construct a DequeX from an Iterable
     * 
     * @param iterable
     *            to construct DequeX from
     * @return DequeX
     */
    public static <T> DequeX<T> fromIterable(final Iterable<T> it) {
    
        if (it instanceof DequeX)
            return (DequeX) it;
        if (it instanceof Deque)
            return new LazyDequeX<T>(
                                     (Deque) it, defaultCollector(), LAZY);
        return new LazyDequeX<T>(null,
                                    ReactiveSeq.fromIterable(it),
                                    defaultCollector(), LAZY);
    }

    /**
     * Construct a Deque from the provided Collector and Iterable.
     * 
     * @param collector To construct DequeX from
     * @param it Iterable to construct DequeX
     * @return DequeX
     */
    public static <T> DequeX<T> fromIterable(final Collector<T, ?, Deque<T>> collector, final Iterable<T> it) {
        if (it instanceof DequeX)
            return ((DequeX) it).type(collector);
        if (it instanceof Deque)
            return new LazyDequeX<T>(
                                     (Deque) it, collector, LAZY);
        return new LazyDequeX<T>(
                                 Streams.stream(it)
                                            .collect(collector),
                                 collector, LAZY);
    }

    @Override
    default DequeX<T> materialize() {
        return (DequeX<T>)LazyCollectionX.super.materialize();
    }
    default AnyMSeq<deque,T> anyM(){
        return AnyM.fromDeque(this);
    }
    DequeX<T> type(Collector<T, ?, Deque<T>> collector);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> DequeX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (DequeX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> DequeX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (DequeX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> DequeX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (DequeX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> DequeX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (DequeX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> DequeX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (DequeX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> DequeX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (DequeX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);

    }



    /**
     * @return The collector for this DequeX
     */
    public <T> Collector<T, ?, Deque<T>> getCollector();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#from(java.util.Collection)
     */
    @Override
    default <T1> DequeX<T1> from(final Collection<T1> c) {
        return DequeX.<T1> fromIterable(getCollector(), c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#fromStream(java.util.stream.Stream)
     */
    @Override
    default <X> DequeX<X> fromStream(final ReactiveSeq<X> stream) {
        return new LazyDequeX<>(
                                ReactiveSeq.fromStream(stream), getCollector(), LAZY);
    }

    /**
     * Combine two adjacent elements in a DequeX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  DequeX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .listX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced DequeX
     */
    @Override
    default DequeX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (DequeX<T>) LazyCollectionX.super.combine(predicate, op);
    }

    @Override
    default DequeX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (DequeX<T>)LazyCollectionX.super.combine(op,predicate);
    }

    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *     DequeX.of(1,2,3)
     *           .transform(i->i*2)
     *           .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *      //DequeX[12]
     * }
     * </pre>
     * 
     * 
     * @param fn mapping function
     * @return Transformed Deque
     */
    default <R> DequeX<R> coflatMap(Function<? super DequeX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
     */
    @Override
    default <R> DequeX<R> unit(final Collection<R> col) {
        return fromIterable(col);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Pure#unit(java.lang.Object)
     */
    @Override
    default <R> DequeX<R> unit(final R value) {
        return singleton(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <R> DequeX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#reactiveStream()
     */
    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#reverse()
     */
    @Override
    default DequeX<T> reverse() {

        return (DequeX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> filter(final Predicate<? super T> pred) {

        return (DequeX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> DequeX<R> map(final Function<? super T, ? extends R> mapper) {

        return (DequeX<R>) LazyCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> DequeX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (DequeX<R>) LazyCollectionX.super.flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limit(long)
     */
    @Override
    default DequeX<T> limit(final long num) {

        return (DequeX<T>) LazyCollectionX.super.limit(num);
    }

    @Override
    default DequeX<T> take(final long num) {

        return (DequeX<T>) LazyCollectionX.super.limit(num);
    }
    @Override
    default DequeX<T> drop(final long num) {

        return (DequeX<T>) LazyCollectionX.super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#skip(long)
     */
    @Override
    default DequeX<T> skip(final long num) {

        return (DequeX<T>) LazyCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> takeWhile(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#takeRight(int)
     */
    @Override
    default DequeX<T> takeRight(final int num) {
        return (DequeX<T>) LazyCollectionX.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#dropRight(int)
     */
    @Override
    default DequeX<T> dropRight(final int num) {
        return (DequeX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> dropWhile(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> takeUntil(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> dropUntil(final Predicate<? super T> p) {
        return (DequeX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#trampoline(java.util.function.Function)
     */
    @SuppressWarnings("unchecked")
    @Override
    default <R> DequeX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (DequeX<R>) LazyCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#slice(long, long)
     */
    @Override
    default DequeX<T> slice(final long from, final long to) {
        return (DequeX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#grouped(int)
     */
    @Override
    default DequeX<ListX<T>> grouped(final int groupSize) {
        return (DequeX<ListX<T>>) LazyCollectionX.super.grouped(groupSize);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable)
     */
    @Override
    default <U> DequeX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (DequeX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> DequeX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (DequeX<R>) LazyCollectionX.super.zip(other, zipper);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> DequeX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (DequeX<R>) LazyCollectionX.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#sliding(int)
     */
    @Override
    default DequeX<VectorX<T>> sliding(final int windowSize) {
        return (DequeX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#sliding(int, int)
     */
    @Override
    default DequeX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (DequeX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    default DequeX<T> scanLeft(final Monoid<T> monoid) {
        return (DequeX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> DequeX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (DequeX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#scanRight(cyclops2.function.Monoid)
     */
    @Override
    default DequeX<T> scanRight(final Monoid<T> monoid) {
        return (DequeX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> DequeX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (DequeX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> DequeX<T> sorted(final Function<? super T, ? extends U> function) {

        return (DequeX<T>) LazyCollectionX.super.sorted(function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#plus(java.lang.Object)
     */
    @Override
    default DequeX<T> plus(final T e) {
        add(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#plusAll(java.util.Collection)
     */
    @Override
    default DequeX<T> plusAll(final Collection<? extends T> list) {
        addAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#minus(java.lang.Object)
     */
    @Override
    default DequeX<T> minus(final Object e) {
        remove(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#minusAll(java.util.Collection)
     */
    @Override
    default DequeX<T> minusAll(final Collection<?> list) {
        removeAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default DequeX<T> peek(final Consumer<? super T> c) {
        return (DequeX<T>) LazyCollectionX.super.peek(c);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycle(int)
     */
    @Override
    default DequeX<T> cycle(final long times) {

        return (DequeX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default DequeX<T> cycle(final Monoid<T> m, final long times) {

        return (DequeX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (DequeX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (DequeX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> DequeX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (DequeX) LazyCollectionX.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> DequeX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (DequeX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> DequeX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (DequeX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zipWithIndex()
     */
    @Override
    default DequeX<Tuple2<T, Long>> zipWithIndex() {
        //
        return (DequeX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#distinct()
     */
    @Override
    default DequeX<T> distinct() {

        return (DequeX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#sorted()
     */
    @Override
    default DequeX<T> sorted() {

        return (DequeX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default DequeX<T> sorted(final Comparator<? super T> c) {

        return (DequeX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> skipWhile(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> skipUntil(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> limitWhile(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> limitUntil(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default DequeX<T> intersperse(final T value) {

        return (DequeX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#shuffle()
     */
    @Override
    default DequeX<T> shuffle() {

        return (DequeX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipLast(int)
     */
    @Override
    default DequeX<T> skipLast(final int num) {

        return (DequeX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#limitLast(int)
     */
    @Override
    default DequeX<T> limitLast(final int num) {

        return (DequeX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default DequeX<T> onEmpty(final T value) {

        return (DequeX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default DequeX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (DequeX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> DequeX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (DequeX<T>) LazyCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#shuffle(java.util.Random)
     */
    @Override
    default DequeX<T> shuffle(final Random random) {

        return (DequeX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#permutations()
     */
    @Override
    default DequeX<ReactiveSeq<T>> permutations() {

        return (DequeX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#combinations(int)
     */
    @Override
    default DequeX<ReactiveSeq<T>> combinations(final int size) {

        return (DequeX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#combinations()
     */
    @Override
    default DequeX<ReactiveSeq<T>> combinations() {

        return (DequeX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> DequeX<U> cast(final Class<? extends U> type) {

        return (DequeX<U>) LazyCollectionX.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> DequeX<U> ofType(final Class<? extends U> type) {

        return (DequeX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> filterNot(final Predicate<? super T> fn) {

        return (DequeX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#notNull()
     */
    @Override
    default DequeX<T> notNull() {

        return (DequeX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default DequeX<T> removeAllS(final Stream<? extends T> stream) {

        return (DequeX<T>) LazyCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default DequeX<T> removeAllI(final Iterable<? extends T> it) {

        return (DequeX<T>) LazyCollectionX.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default DequeX<T> removeAll(final T... values) {

        return (DequeX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default DequeX<T> retainAllI(final Iterable<? extends T> it) {

        return (DequeX<T>) LazyCollectionX.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default DequeX<T> retainAllS(final Stream<? extends T> seq) {

        return (DequeX<T>) LazyCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default DequeX<T> retainAll(final T... values) {

        return (DequeX<T>) LazyCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> DequeX<C> grouped(final int size, final Supplier<C> supplier) {

        return (DequeX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (DequeX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (DequeX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> DequeX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (DequeX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> DequeX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (DequeX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default DequeX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (DequeX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }





    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default DequeX<T> onEmptySwitch(final Supplier<? extends Deque<T>> supplier) {
        if (isEmpty())
            return DequeX.fromIterable(supplier.get());
        return this;
    }


    @Override
    default <R> DequeX<R> retry(final Function<? super T, ? extends R> fn) {
        return (DequeX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> DequeX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (DequeX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> DequeX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (DequeX<R>)LazyCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> DequeX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (DequeX<R>)LazyCollectionX.super.flatMapP(fn);
    }

    @Override
    default DequeX<T> prependS(Stream<? extends T> stream) {
        return (DequeX<T>)LazyCollectionX.super.prependS(stream);
    }

    @Override
    default DequeX<T> append(T... values) {
        return (DequeX<T>)LazyCollectionX.super.append(values);
    }

    @Override
    default DequeX<T> append(T value) {
        return (DequeX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default DequeX<T> prepend(T value) {
        return (DequeX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default DequeX<T> prepend(T... values) {
        return (DequeX<T>)LazyCollectionX.super.prepend(values);
    }

    @Override
    default DequeX<T> insertAt(int pos, T... values) {
        return (DequeX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default DequeX<T> deleteBetween(int start, int end) {
        return (DequeX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default DequeX<T> insertAtS(int pos, Stream<T> stream) {
        return (DequeX<T>)LazyCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default DequeX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (DequeX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> DequeX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (DequeX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }
    @Override
    default DequeX<T> plusLoop(int max, IntFunction<T> value) {
        return (DequeX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default DequeX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (DequeX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

    /**
     * Narrow a covariant Deque
     * 
     * <pre>
     * {@code 
     * DequeX<? extends Fruit> deque = DequeX.of(apple,bannana);
     * DequeX<Fruit> fruitDeque = DequeX.narrow(deque);
     * }
     * </pre>
     * 
     * @param setX to narrow generic type
     * @return SetX with narrowed type
     */
    public static <T> DequeX<T> narrow(final DequeX<? extends T> dequeX) {
        return (DequeX<T>) dequeX;
    }

    @Override
    default DequeX<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return (DequeX<T>)LazyCollectionX.super.zip(combiner,app);
    }

    @Override
    default <R> DequeX<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (DequeX<R>)LazyCollectionX.super.zipWith(fn);
    }

    @Override
    default <R> DequeX<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (DequeX<R>)LazyCollectionX.super.zipWithS(fn);
    }

    @Override
    default <R> DequeX<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (DequeX<R>)LazyCollectionX.super.zipWithP(fn);
    }

    @Override
    default <T2, R> DequeX<R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (DequeX<R>)LazyCollectionX.super.zipP(publisher,fn);
    }



    @Override
    default <U> DequeX<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (DequeX)LazyCollectionX.super.zipP(other);
    }


    @Override
    default <S, U, R> DequeX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (DequeX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> DequeX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (DequeX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }


    @UtilityClass
    public static class Instances {

        public static InstanceDefinitions<deque> definitions(){
            return new InstanceDefinitions<deque>() {
                @Override
                public <T, R> Functor<deque> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<deque> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<deque> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<deque> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<deque>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<deque>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<deque> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<deque>> monadPlus(Monoid<Higher<deque, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Traverse<deque> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<deque> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<deque>> comonad() {
                    return Maybe.nothing();
                }
                @Override
                public <T> Maybe<Unfoldable<deque>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };
        }
        public static Unfoldable<deque> unfoldable(){
            return new Unfoldable<deque>() {
                @Override
                public <R, T> Higher<deque, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return DequeX.unfold(b,fn);
                }
            };
        }
        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  DequeX<Integer> list = Deques.functor().transform(i->i*2, DequeX.of(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Deques
         * <pre>
         * {@code
         *   DequeX<Integer> list = Deques.unit()
        .unit("hello")
        .applyHKT(h->Deques.functor().transform((String v) ->v.length(), h))
        .convert(DequeX::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Deques
         */
        public static <T,R>Functor<deque> functor(){
            BiFunction<DequeX<T>,Function<? super T, ? extends R>,DequeX<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * DequeX<String> list = Deques.unit()
        .unit("hello")
        .convert(DequeX::narrowK3);

        //DequeX.of("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Deques
         */
        public static <T> Pure<deque> unit(){
            return General.<deque,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.DequeX.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.DequeX.of;
         *
        Deques.zippingApplicative()
        .ap(widen(asDeque(l1(this::multiplyByTwo))),widen(asDeque(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * DequeX<Function<Integer,Integer>> listFn =Deques.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(DequeX::narrowK3);

        DequeX<Integer> list = Deques.unit()
        .unit("hello")
        .applyHKT(h->Deques.functor().transform((String v) ->v.length(), h))
        .applyHKT(h->Deques.zippingApplicative().ap(listFn, h))
        .convert(DequeX::narrowK3);

        //DequeX.of("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Deques
         */
        public static <T,R> Applicative<deque> zippingApplicative(){
            BiFunction<DequeX< Function<T, R>>,DequeX<T>,DequeX<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.DequeX.widen;
         * DequeX<Integer> list  = Deques.monad()
        .flatMap(i->widen(DequeX.range(0,i)), widen(DequeX.of(1,2,3)))
        .convert(DequeX::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    DequeX<Integer> list = Deques.unit()
        .unit("hello")
        .applyHKT(h->Deques.monad().flatMap((String v) ->Deques.unit().unit(v.length()), h))
        .convert(DequeX::narrowK3);

        //DequeX.of("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Deques
         */
        public static <T,R> Monad<deque> monad(){

            BiFunction<Higher<deque,T>,Function<? super T, ? extends Higher<deque,R>>,Higher<deque,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  DequeX<String> list = Deques.unit()
        .unit("hello")
        .applyHKT(h->Deques.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(DequeX::narrowK3);

        //DequeX.of("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<deque> monadZero(){

            return General.monadZero(monad(), DequeX.empty());
        }
        /**
         * <pre>
         * {@code
         *  DequeX<Integer> list = Deques.<Integer>monadPlus()
        .plus(DequeX.of()), DequeX.of(10)))
        .convert(DequeX::narrowK3);
        //DequeX.of(10))
         *
         * }
         * </pre>
         * @return Type class for combining Deques by concatenation
         */
        public static <T> MonadPlus<deque> monadPlus(){
            Monoid<DequeX<T>> m = Monoid.of(DequeX.empty(), Instances::concat);
            Monoid<Higher<deque,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T,R> MonadRec<deque> monadRec(){

            return new MonadRec<deque>(){
                @Override
                public <T, R> Higher<deque, R> tailRec(T initial, Function<? super T, ? extends Higher<deque,? extends Either<T, R>>> fn) {
                    return DequeX.tailRec(initial,fn.andThen(DequeX::narrowK));
                }
            };
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<DequeX<Integer>> m = Monoid.of(DequeX.of()), (a,b)->a.isEmpty() ? b : a);
        DequeX<Integer> list = Deques.<Integer>monadPlus(m)
        .plus(DequeX.of(5)), DequeX.of(10)))
        .convert(DequeX::narrowK3);
        //DequeX.of(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Deques
         * @return Type class for combining Deques
         */
        public static <T> MonadPlus<deque> monadPlus(Monoid<DequeX<T>> m){
            Monoid<Higher<deque,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<deque> traverse(){
            BiFunction<Applicative<C2>,DequeX<Higher<C2, T>>,Higher<C2, DequeX<T>>> sequenceFn = (ap,list) -> {

                Higher<C2,DequeX<T>> identity = ap.unit(DequeX.of());

                BiFunction<Higher<C2,DequeX<T>>,Higher<C2,T>,Higher<C2,DequeX<T>>> combineToDeque =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,DequeX<T>>> combineDeques = (a,b)-> ap.apBiFn(ap.unit((l1,l2)-> { l1.addAll(l2); return l1;}),a,b); ;

                return list.stream()
                        .reduce(identity,
                                combineToDeque,
                                combineDeques);


            };
            BiFunction<Applicative<C2>,Higher<deque,Higher<C2, T>>,Higher<C2, Higher<deque,T>>> sequenceNarrow  =
                    (a,b) -> DequeX.widen2(sequenceFn.apply(a, DequeX.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Deques.foldable()
        .foldLeft(0, (a,b)->a+b, DequeX.of(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<deque> foldable(){
            return new Foldable<deque>() {
                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<deque, T> ds) {
                    return  DequeX.fromIterable(narrowK(ds)).foldRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<deque, T> ds) {
                    return  DequeX.fromIterable(narrowK(ds)).foldLeft(monoid);
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<deque, T> nestedA) {
                    return narrowK(nestedA).<R>map(fn).foldLeft(mb);
                }
            };

        }

        private static  <T> DequeX<T> concat(Deque<T> l1, Deque<T> l2){
            return Stream.concat(l1.stream(),l2.stream()).collect(CyclopsCollectors.toDequeX());
        }
        private <T> DequeX<T> of(T value){
            return DequeX.of(value);
        }
        private static <T,R> DequeX<R> ap(DequeX<Function< T, R>> lt,  DequeX<T> list){
            return lt.zip(list,(a,b)->a.apply(b));
        }
        private static <T,R> Higher<deque,R> flatMap( Higher<deque,T> lt, Function<? super T, ? extends  Higher<deque,R>> fn){
            return DequeX.narrowK(lt).flatMap(fn.andThen(DequeX::narrowK));
        }
        private static <T,R> DequeX<R> map(DequeX<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);
        }
    }

    public static  <T,R> DequeX<R> tailRec(T initial, Function<? super T, ? extends DequeX<? extends Either<T, R>>> fn) {
        ListX<Either<T, R>> lazy = ListX.of(Either.left(initial));
        ListX<Either<T, R>> next = lazy.eager();
        boolean newValue[] = {true};
        for(;;){

            next = next.flatMap(e -> e.visit(s -> {
                        newValue[0]=true;
                        return fn.apply(s); },
                    p -> {
                        newValue[0]=false;
                        return ListX.of(e);
                    }));
            if(!newValue[0])
                break;

        }
        return Either.sequenceRight(next).orElse(ListX.empty()).to().dequeX(Evaluation.LAZY);
    }
}
