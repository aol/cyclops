package cyclops.collections.mutable;

import com.aol.cyclops2.data.collections.extensions.lazy.LazyQueueX;
import com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.control.Xor;
import cyclops.typeclasses.*;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.immutable.VectorX;
import cyclops.companion.CyclopsCollectors;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.queue;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
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
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * An eXtended Queue type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this QueueX
 */
public interface QueueX<T> extends To<QueueX<T>>,Queue<T>,
                                   LazyCollectionX<T>,
                                    OnEmptySwitch<T, Queue<T>>,
                                    Higher<queue,T>{

    public static <T> QueueX<T> defer(Supplier<QueueX<T>> s){
        return of(s)
                .map(Supplier::get)
                .flatMap(l->l);
    }

    static <T> CompletableQueueX<T> completable(){
        return new CompletableQueueX<>();
    }

    static class CompletableQueueX<T> implements InvocationHandler {
        cyclops.async.Future<QueueX<T>> future = cyclops.async.Future.future();
        public boolean complete(Queue<T> result){
            return future.complete(QueueX.fromIterable(result));
        }

        public QueueX<T> asQueueX(){
            QueueX f = (QueueX) Proxy.newProxyInstance(QueueX.class.getClassLoader(),
                    new Class[] { QueueX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            QueueX<T> target =  future.visit(l->l,t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }

    QueueX<T> lazy();
    QueueX<T> eager();
    public static  <T> Kleisli<queue,QueueX<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), QueueX::widen);
    }
    public static <T> Higher<queue, T> widen(QueueX<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<queue,T,QueueX<T>> kindCokleisli(){
        return Cokleisli.of(QueueX::narrowK);
    }
    public static <W1,T> Nested<queue,W1,T> nested(QueueX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, QueueX.Instances.definitions(),def2);
    }
    default <W1> Product<queue,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,queue,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, QueueX.Instances.definitions());
    }
    default Active<queue,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R> Nested<queue,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    static <T> Collector<T, ?, Queue<T>> defaultCollector() {
        return java.util.stream.Collectors.toCollection(() -> new LinkedList<>());
    }

    /**
     * Widen a QueueType nest inside another HKT encoded type
     *
     * @param  queue HTK encoded type containing  a List to widen
     * @return HKT encoded type with a widened List
     */
    public static <C2,T> Higher<C2, Higher<queue,T>> widen2(Higher<C2, QueueX<T>> queue){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<QueueType.queue,T> must be a ListType
        return (Higher)queue;
    }
    /**
     * Convert the raw Higher Kinded Type for Queue types into the QueueType type definition class
     *
     * @param queue HKT encoded list into a QueueType
     * @return QueueType
     */
    public static <T> QueueX<T> narrowK(final Higher<queue, T> queue) {
        return (QueueX<T>)queue;
    }

    /**
    * Create a QueueX that contains the Integers between skip and take
    * 
    * @param start
    *            Number of range to skip from
    * @param end
    *            Number for range to take at
    * @return Range QueueX
    */
    public static QueueX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to()
                          .queueX(Evaluation.LAZY);
    }

    /**
     * Create a QueueX that contains the Longs between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range QueueX
     */
    public static QueueX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to()
                          .queueX(Evaluation.LAZY);
    }

    /**
     * Unfold a function into a QueueX
     * 
     * <pre>
     * {@code 
     *  QueueX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     * 
     * //(1,2,3,4,5)
     * 
     * }</code>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return QueueX generated by unfolder function
     */
    static <U, T> QueueX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to()
                          .queueX(Evaluation.LAZY);
    }

    /**
     * Generate a QueueX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate QueueX elements
     * @return QueueX generated from the provided Supplier
     */
    public static <T> QueueX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit).to()
                          .queueX(Evaluation.LAZY);
    }
    /**
     * Generate a QueueX from the provided value up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Value for QueueX elements
     * @return QueueX generated from the provided Supplier
     */
    public static <T> QueueX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit).to()
                          .queueX(Evaluation.LAZY);
    }
    /**
     * Create a QueueX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return QueueX generated by iterative application
     */
    public static <T> QueueX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit).to()
                          .queueX(Evaluation.LAZY);

    }

    public static <T> QueueX<T> empty() {
        return fromIterable((Queue<T>) defaultCollector().supplier()
                                                         .get());
    }

    @SafeVarargs
    public static <T> QueueX<T> of(final T... values) {
        return new LazyQueueX<T>(null,
                ReactiveSeq.of(values),
                defaultCollector(),Evaluation.LAZY);
    }
    /**
     * 
     * Construct a QueueX from the provided Iterator
     * 
     * @param it Iterator to populate QueueX
     * @return Newly populated QueueX
     */
    public static <T> QueueX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }
    public static <T> QueueX<T> singleton(final T value) {
        return QueueX.<T> of(value);
    }

    /**
     * Construct a QueueX from an Publisher
     * 
     * @param publisher
     *            to construct QueueX from
     * @return QueueX
     */
    public static <T> QueueX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to()
                          .queueX(Evaluation.LAZY);
    }

    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  QueueX<Integer> queue = queueX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create QueueX from
     * @param <T> QueueX generated from Stream
     * @return
     */
    public static <T> QueueX<T> queueX(ReactiveSeq<T> stream){
        return new LazyQueueX<T>(null,
                stream,
                defaultCollector(),Evaluation.LAZY);
    }
    default AnyMSeq<queue,T> anyM(){
        return AnyM.fromQueue(this);
    }

    QueueX<T> type(Collector<T, ?, Queue<T>> collector);


    public static <T> QueueX<T> fromIterable(final Iterable<T> it) {
        
        if (it instanceof QueueX)
            return (QueueX) it;
        if (it instanceof Queue)
            return new LazyQueueX<T>(
                                     (Queue) it, defaultCollector(),Evaluation.LAZY);
        return new LazyQueueX<T>(null,
                                 ReactiveSeq.fromIterable(it),
                                            defaultCollector(),Evaluation.LAZY);
    }

    public static <T> QueueX<T> fromIterable(final Collector<T, ?, Queue<T>> collector, final Iterable<T> it) {
        if (it instanceof QueueX)
            return ((QueueX) it).withCollector(collector);
        if (it instanceof Deque)
            return new LazyQueueX<T>(
                                     (Queue) it, collector,Evaluation.LAZY);
        return new LazyQueueX<T>(
                null,
                ReactiveSeq.fromIterable(it),
                collector,Evaluation.LAZY);
    }

    QueueX<T> withCollector(Collector<T, ?, Queue<T>> collector);

    public <T> Collector<T, ?, Queue<T>> getCollector();

    @Override
    default QueueX<T> take(final long num) {

        return (QueueX<T>) LazyCollectionX.super.limit(num);
    }
    @Override
    default QueueX<T> drop(final long num) {

        return (QueueX<T>) LazyCollectionX.super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> QueueX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (QueueX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> QueueX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (QueueX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> QueueX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (QueueX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> QueueX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (QueueX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> QueueX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (QueueX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> QueueX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (QueueX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    

  
    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *     QueueX.of(1,2,3)
     *           .transform(i->i*2)
     *           .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *      //QueueX[12]
     * }
     * </pre>
     * 
     * 
     * @param fn mapping function
     * @return Transformed Queue
     */
    default <R> QueueX<R> coflatMap(Function<? super QueueX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#from(java.util.Collection)
     */
    @Override
    default <T1> QueueX<T1> from(final Collection<T1> c) {
        return QueueX.<T1> fromIterable(getCollector(), c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#fromStream(java.util.stream.Stream)
     */
    @Override
    default <X> QueueX<X> fromStream(final ReactiveSeq<X> stream) {
        return new LazyQueueX<>(
                                stream.collect(getCollector()), getCollector(),Evaluation.LAZY);
    }

    /**
     * Combine two adjacent elements in a QueueX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  QueueX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .listX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced QueueX
     */
    @Override
    default QueueX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (QueueX<T>) LazyCollectionX.super.combine(predicate, op);
    }
    @Override
    default QueueX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (QueueX<T>)LazyCollectionX.super.combine(op,predicate);
    }

    @Override
    default <R> QueueX<R> unit(final Collection<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> QueueX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> QueueX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }
    @Override
    default QueueX<T> materialize() {
        return (QueueX<T>)LazyCollectionX.super.materialize();
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#reverse()
     */
    @Override
    default QueueX<T> reverse() {

        return (QueueX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> filter(final Predicate<? super T> pred) {

        return (QueueX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> QueueX<R> map(final Function<? super T, ? extends R> mapper) {

        return (QueueX<R>) LazyCollectionX.super.<R> map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> QueueX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (QueueX<R>) LazyCollectionX.super.<R> flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limit(long)
     */
    @Override
    default QueueX<T> limit(final long num) {

        return (QueueX<T>) LazyCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#skip(long)
     */
    @Override
    default QueueX<T> skip(final long num) {

        return (QueueX<T>) LazyCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> takeWhile(final Predicate<? super T> p) {

        return (QueueX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> dropWhile(final Predicate<? super T> p) {

        return (QueueX<T>) LazyCollectionX.super.dropWhile(p);
    }

    @Override
    default QueueX<T> takeRight(final int num) {
        return (QueueX<T>) LazyCollectionX.super.takeRight(num);
    }

    @Override
    default QueueX<T> dropRight(final int num) {
        return (QueueX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> takeUntil(final Predicate<? super T> p) {

        return (QueueX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> dropUntil(final Predicate<? super T> p) {

        return (QueueX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> QueueX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (QueueX<R>) LazyCollectionX.super.<R> trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#slice(long, long)
     */
    @Override
    default QueueX<T> slice(final long from, final long to) {

        return (QueueX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> QueueX<T> sorted(final Function<? super T, ? extends U> function) {

        return (QueueX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    default QueueX<ListX<T>> grouped(final int groupSize) {
        return (QueueX<ListX<T>>) LazyCollectionX.super.grouped(groupSize);
    }


    @Override
    default <U> QueueX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (QueueX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> QueueX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (QueueX<R>) LazyCollectionX.super.zip(other, zipper);
    }



    @Override
    default <U, R> QueueX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (QueueX<R>) LazyCollectionX.super.zipS(other, zipper);
    }

    @Override
    default QueueX<VectorX<T>> sliding(final int windowSize) {
        return (QueueX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default QueueX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (QueueX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default QueueX<T> scanLeft(final Monoid<T> monoid) {
        return (QueueX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> QueueX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (QueueX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default QueueX<T> scanRight(final Monoid<T> monoid) {
        return (QueueX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> QueueX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (QueueX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    @Override
    default QueueX<T> plus(final T e) {
        add(e);
        return this;
    }

    @Override
    default QueueX<T> plusAll(final Collection<? extends T> list) {
        addAll(list);
        return this;
    }

    @Override
    default QueueX<T> minus(final Object e) {
        remove(e);
        return this;
    }

    @Override
    default QueueX<T> minusAll(final Collection<?> list) {
        removeAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default QueueX<T> peek(final Consumer<? super T> c) {

        return (QueueX<T>) LazyCollectionX.super.peek(c);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> QueueX<U> cast(final Class<? extends U> type) {

        return (QueueX<U>) LazyCollectionX.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#cycle(int)
     */
    @Override
    default QueueX<T> cycle(final long times) {

        return (QueueX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default QueueX<T> cycle(final Monoid<T> m, final long times) {

        return (QueueX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (QueueX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (QueueX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#zip(java.util.stream.Stream)
     */
    @Override
    default <U> QueueX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (QueueX) LazyCollectionX.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> QueueX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (QueueX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> QueueX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (QueueX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#zipWithIndex()
     */
    @Override
    default QueueX<Tuple2<T, Long>> zipWithIndex() {

        return (QueueX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#distinct()
     */
    @Override
    default QueueX<T> distinct() {

        return (QueueX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#sorted()
     */
    @Override
    default QueueX<T> sorted() {

        return (QueueX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default QueueX<T> sorted(final Comparator<? super T> c) {

        return (QueueX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> skipWhile(final Predicate<? super T> p) {

        return (QueueX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> skipUntil(final Predicate<? super T> p) {

        return (QueueX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> limitWhile(final Predicate<? super T> p) {

        return (QueueX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> limitUntil(final Predicate<? super T> p) {

        return (QueueX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default QueueX<T> intersperse(final T value) {

        return (QueueX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#shuffle()
     */
    @Override
    default QueueX<T> shuffle() {

        return (QueueX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#skipLast(int)
     */
    @Override
    default QueueX<T> skipLast(final int num) {

        return (QueueX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limitLast(int)
     */
    @Override
    default QueueX<T> limitLast(final int num) {

        return (QueueX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default QueueX<T> onEmptySwitch(final Supplier<? extends Queue<T>> supplier) {
        if (isEmpty())
            return QueueX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default QueueX<T> onEmpty(final T value) {

        return (QueueX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default QueueX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (QueueX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> QueueX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (QueueX<T>) LazyCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default QueueX<T> shuffle(final Random random) {

        return (QueueX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> QueueX<U> ofType(final Class<? extends U> type) {

        return (QueueX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> filterNot(final Predicate<? super T> fn) {

        return (QueueX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#notNull()
     */
    @Override
    default QueueX<T> notNull() {

        return (QueueX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default QueueX<T> removeAllS(final Stream<? extends T> stream) {

        return (QueueX<T>) LazyCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default QueueX<T> removeAllI(final Iterable<? extends T> it) {

        return (QueueX<T>) LazyCollectionX.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default QueueX<T> removeAll(final T... values) {

        return (QueueX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default QueueX<T> retainAllI(final Iterable<? extends T> it) {

        return (QueueX<T>) LazyCollectionX.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default QueueX<T> retainAllS(final Stream<? extends T> seq) {

        return (QueueX<T>) LazyCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default QueueX<T> retainAll(final T... values) {

        return (QueueX<T>) LazyCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> QueueX<C> grouped(final int size, final Supplier<C> supplier) {

        return (QueueX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (QueueX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (QueueX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> QueueX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (QueueX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> QueueX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (QueueX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default QueueX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (QueueX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }



    @Override
    default <R> QueueX<R> retry(final Function<? super T, ? extends R> fn) {
        return (QueueX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> QueueX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (QueueX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> QueueX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (QueueX<R>)LazyCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> QueueX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (QueueX<R>)LazyCollectionX.super.flatMapP(fn);
    }

    @Override
    default QueueX<T> prependS(Stream<? extends T> stream) {
        return (QueueX<T>)LazyCollectionX.super.prependS(stream);
    }

    @Override
    default QueueX<T> append(T... values) {
        return (QueueX<T>)LazyCollectionX.super.append(values);
    }

    @Override
    default QueueX<T> append(T value) {
        return (QueueX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default QueueX<T> prepend(T value) {
        return (QueueX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default QueueX<T> prepend(T... values) {
        return (QueueX<T>)LazyCollectionX.super.prepend(values);
    }

    @Override
    default QueueX<T> insertAt(int pos, T... values) {
        return (QueueX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default QueueX<T> deleteBetween(int start, int end) {
        return (QueueX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default QueueX<T> insertAtS(int pos, Stream<T> stream) {
        return (QueueX<T>)LazyCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default QueueX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (QueueX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> QueueX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (QueueX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }
    @Override
    default QueueX<T> plusLoop(int max, IntFunction<T> value) {
        return (QueueX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default QueueX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (QueueX<T>)LazyCollectionX.super.plusLoop(supplier);
    }
    /**
     * Narrow a covariant Queue
     * 
     * <pre>
     * {@code 
     * QueueX<? extends Fruit> set = QueueX.of(apple,bannana);
     * QueueX<Fruit> fruitSet = QueueX.narrow(queue);
     * }
     * </pre>
     * 
     * @param queueX to narrow generic type
     * @return QueueX with narrowed type
     */
    public static <T> QueueX<T> narrow(final QueueX<? extends T> queueX) {
        return (QueueX<T>) queueX;
    }

    @Override
    default QueueX<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return (QueueX<T>)LazyCollectionX.super.zip(combiner,app);
    }

    @Override
    default <R> QueueX<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (QueueX<R>)LazyCollectionX.super.zipWith(fn);
    }

    @Override
    default <R> QueueX<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (QueueX<R>)LazyCollectionX.super.zipWithS(fn);
    }

    @Override
    default <R> QueueX<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (QueueX<R>)LazyCollectionX.super.zipWithP(fn);
    }

    @Override
    default <T2, R> QueueX<R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (QueueX<R>)LazyCollectionX.super.zipP(publisher,fn);
    }



    @Override
    default <U> QueueX<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (QueueX)LazyCollectionX.super.zipP(other);
    }


    @Override
    default <S, U, R> QueueX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (QueueX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> QueueX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (QueueX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }

    /**
     * Companion class for creating Type Class instances for working with Queues
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {

        public static InstanceDefinitions<queue> definitions(){
            return new InstanceDefinitions<queue>() {
                @Override
                public <T, R> Functor<queue> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<queue> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<queue> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<queue> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<queue>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<queue>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<queue> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<queue>> monadPlus(Monoid<Higher<queue, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Traverse<queue> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<queue> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<queue>> comonad() {
                    return Maybe.none();
                }
                @Override
                public <T> Maybe<Unfoldable<queue>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };
        }
        public static Unfoldable<queue> unfoldable(){
            return new Unfoldable<queue>() {
                @Override
                public <R, T> Higher<queue, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return QueueX.unfold(b,fn);
                }
            };
        }
        /**
         *
         * Transform a queue, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  QueueX<Integer> queue = Queues.functor().transform(i->i*2, QueueX.of(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Queues
         * <pre>
         * {@code
         *   QueueX<Integer> queue = Queues.unit()
        .unit("hello")
        .applyHKT(h->Queues.functor().transform((String v) ->v.length(), h))
        .convert(QueueX::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Queues
         */
        public static <T,R>Functor<queue> functor(){
            BiFunction<QueueX<T>,Function<? super T, ? extends R>,QueueX<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * QueueX<String> queue = Queues.unit()
        .unit("hello")
        .convert(QueueX::narrowK3);

        //QueueX.of("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Queues
         */
        public static <T> Pure<queue> unit(){
            return General.<queue,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.QueueX.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.QueueX.of;
         *
        Queues.zippingApplicative()
        .ap(widen(asQueue(l1(this::multiplyByTwo))),widen(asQueue(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * QueueX<Function<Integer,Integer>> queueFn =Queues.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(QueueX::narrowK3);

        QueueX<Integer> queue = Queues.unit()
        .unit("hello")
        .applyHKT(h->Queues.functor().transform((String v) ->v.length(), h))
        .applyHKT(h->Queues.zippingApplicative().ap(queueFn, h))
        .convert(QueueX::narrowK3);

        //QueueX.of("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Queues
         */
        public static <T,R> Applicative<queue> zippingApplicative(){
            BiFunction<QueueX< Function<T, R>>,QueueX<T>,QueueX<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.QueueX.widen;
         * QueueX<Integer> queue  = Queues.monad()
        .flatMap(i->widen(QueueX.range(0,i)), widen(QueueX.of(1,2,3)))
        .convert(QueueX::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    QueueX<Integer> queue = Queues.unit()
        .unit("hello")
        .applyHKT(h->Queues.monad().flatMap((String v) ->Queues.unit().unit(v.length()), h))
        .convert(QueueX::narrowK3);

        //QueueX.of("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Queues
         */
        public static <T,R> Monad<queue> monad(){

            BiFunction<Higher<queue,T>,Function<? super T, ? extends Higher<queue,R>>,Higher<queue,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  QueueX<String> queue = Queues.unit()
        .unit("hello")
        .applyHKT(h->Queues.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(QueueX::narrowK3);

        //QueueX.of("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<queue> monadZero(){

            return General.monadZero(monad(), QueueX.empty());
        }
        /**
         * <pre>
         * {@code
         *  QueueX<Integer> queue = Queues.<Integer>monadPlus()
        .plus(QueueX.of()), QueueX.of(10)))
        .convert(QueueX::narrowK3);
        //QueueX.of(10))
         *
         * }
         * </pre>
         * @return Type class for combining Queues by concatenation
         */
        public static <T> MonadPlus<queue> monadPlus(){
            Monoid<QueueX<T>> m = Monoid.of(QueueX.empty(), Instances::concat);
            Monoid<Higher<queue,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T,R> MonadRec<queue> monadRec(){

            return new MonadRec<queue>(){
                @Override
                public <T, R> Higher<queue, R> tailRec(T initial, Function<? super T, ? extends Higher<queue,? extends Xor<T, R>>> fn) {
                    return QueueX.tailRec(initial,fn.andThen(QueueX::narrowK));
                }
            };
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<QueueX<Integer>> m = Monoid.of(QueueX.of()), (a,b)->a.isEmpty() ? b : a);
        QueueX<Integer> queue = Queues.<Integer>monadPlus(m)
        .plus(QueueX.of(5)), QueueX.of(10)))
        .convert(QueueX::narrowK3);
        //QueueX.of(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Queues
         * @return Type class for combining Queues
         */
        public static <T> MonadPlus<queue> monadPlus(Monoid<QueueX<T>> m){
            Monoid<Higher<queue,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<queue> traverse(){
            BiFunction<Applicative<C2>,QueueX<Higher<C2, T>>,Higher<C2, QueueX<T>>> sequenceFn = (ap,queue) -> {

                Higher<C2,QueueX<T>> identity = ap.unit(QueueX.of());

                BiFunction<Higher<C2,QueueX<T>>,Higher<C2,T>,Higher<C2,QueueX<T>>> combineToQueue =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,QueueX<T>>> combineQueues = (a,b)-> ap.apBiFn(ap.unit((l1,l2)-> { l1.addAll(l2); return l1;}),a,b); ;

                return queue.stream()
                        .reduce(identity,
                                combineToQueue,
                                combineQueues);


            };
            BiFunction<Applicative<C2>,Higher<queue,Higher<C2, T>>,Higher<C2, Higher<queue,T>>> sequenceNarrow  =
                    (a,b) -> QueueX.widen2(sequenceFn.apply(a, QueueX.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Queues.foldable()
        .foldLeft(0, (a,b)->a+b, QueueX.of(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<queue> foldable(){
            return new Foldable<queue>() {
                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<queue, T> ds) {
                    return  fromIterable(narrowK(ds)).foldRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<queue, T> ds) {
                    return  fromIterable(narrowK(ds)).foldLeft(monoid);
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<queue, T> nestedA) {
                    return narrowK(nestedA).<R>map(fn).foldLeft(mb);
                }
            };
        }

        private static  <T> QueueX<T> concat(Queue<T> l1, Queue<T> l2){
            return Stream.concat(l1.stream(),l2.stream()).collect(CyclopsCollectors.toQueueX());
        }
        private <T> QueueX<T> of(T value){
            return QueueX.of(value);
        }
        private static <T,R> QueueX<R> ap(QueueX<Function< T, R>> lt,  QueueX<T> queue){
            return lt.zip(queue,(a,b)->a.apply(b));
        }
        private static <T,R> Higher<queue,R> flatMap( Higher<queue,T> lt, Function<? super T, ? extends  Higher<queue,R>> fn){
            return QueueX.narrowK(lt).flatMap(fn.andThen(QueueX::narrowK));
        }
        private static <T,R> QueueX<R> map(QueueX<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);
        }
    }
    public static  <T,R> QueueX<R> tailRec(T initial, Function<? super T, ? extends QueueX<? extends Xor<T, R>>> fn) {
        ListX<Xor<T, R>> lazy = ListX.of(Xor.secondary(initial));
        ListX<Xor<T, R>> next = lazy.eager();
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
        return Xor.sequencePrimary(next).orElse(ListX.empty()).to().queueX(Evaluation.LAZY);
    }

}
