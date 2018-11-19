package cyclops.reactive.collections.immutable;


import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.ReactiveWitness.persistentQueueX;
import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.LazyPQueueX;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentQueue;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.companion.Reducers;
import cyclops.control.Either;
import cyclops.control.Future;
import cyclops.control.Option;
import cyclops.data.BankersQueue;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.reactive.collections.mutable.ListX;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

import static cyclops.data.tuple.Tuple.tuple;

/**
 * An eXtended Persistent Queue type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface PersistentQueueX<T> extends To<PersistentQueueX<T>>,
                                              PersistentQueue<T>,
                                             LazyCollectionX<T>,
                                             OnEmptySwitch<T, PersistentQueue<T>>,
                                             Higher<persistentQueueX,T>{

    PersistentQueueX<T> lazy();
    PersistentQueueX<T> eager();

    public static <T> PersistentQueueX<T> defer(Supplier<PersistentQueueX<T>> s){
      return of(s)
                .map(Supplier::get)
                .concatMap(l->l);
    }
    static <T> CompletablePersistentQueueX<T> completable(){
        return new CompletablePersistentQueueX<>();
    }

    static class CompletablePersistentQueueX<T> implements InvocationHandler {
        Future<PersistentQueueX<T>> future = Future.future();
        public boolean complete(PersistentQueue<T> result){
            return future.complete(PersistentQueueX.fromIterable(result));
        }

        public PersistentQueueX<T> asPersistentQueueX(){
            PersistentQueueX f = (PersistentQueueX) Proxy.newProxyInstance(PersistentQueueX.class.getClassLoader(),
                    new Class[] { PersistentQueueX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            PersistentQueueX<T> target = future.fold(l->l, t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }


    public static <T> Higher<persistentQueueX, T> widen(PersistentQueueX<T> narrow) {
        return narrow;
    }


    static <T> PersistentQueueX<T> fromIterator(Iterator<T> iterator) {
        return fromIterable(()->iterator);
    }


    /**
     * Narrow a covariant PersistentQueueX
     *
     * <pre>
     * {@code
     *  PersistentQueueX<? extends Fruit> set = PersistentQueueX.of(apple,bannana);
     *  PersistentQueueX<Fruit> fruitSet = PersistentQueueX.narrow(set);
     * }
     * </pre>
     *
     * @param queueX to narrow generic type
     * @return OrderedSetX with narrowed type
     */
    public static <T> PersistentQueueX<T> narrow(final PersistentQueueX<? extends T> queueX) {
        return (PersistentQueueX<T>) queueX;
    }

    /**
     * Widen a PersistentQueueX nest inside another HKT encoded type
     *
     * @param list HTK encoded type containing  a PQueue to widen
     * @return HKT encoded type with a widened PQueue
     */
    public static <C2,T> Higher<C2, Higher<persistentQueueX,T>> widen2(Higher<C2, PersistentQueueX<T>> list){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<persistentQueueX,T> must be a PQueueType
        return (Higher)list;
    }
    /**
     * Convert the raw Higher Kinded Type for PQueue types into the PQueueType type definition class
     *
     * @param list HKT encoded list into a PQueueType
     * @return PQueueType
     */
    public static <T> PersistentQueueX<T> narrowK(final Higher<persistentQueueX, T> list) {
        return (PersistentQueueX<T>)list;
    }
    /**
     * Create a PersistentQueueX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range PersistentQueueX
     */
    public static PersistentQueueX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to(ReactiveConvertableSequence::converter)
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Create a PersistentQueueX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range PersistentQueueX
     */
    public static PersistentQueueX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to(ReactiveConvertableSequence::converter)
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Unfold a function into a PersistentQueueX
     *
     * <pre>
     * {@code
     *  PersistentQueueX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</code>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return PersistentQueueX generated by unfolder function
     */
    static <U, T> PersistentQueueX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to(ReactiveConvertableSequence::converter)
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Generate a PersistentQueueX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate PersistentQueueX elements
     * @return PersistentQueueX generated from the provided Supplier
     */
    public static <T> PersistentQueueX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit).to(ReactiveConvertableSequence::converter)
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Generate a PersistentQueueX from the provided value up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Value for PersistentQueueX elements
     * @return PersistentQueueX generated from the provided Supplier
     */
    public static <T> PersistentQueueX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit).to(ReactiveConvertableSequence::converter)
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Create a PersistentQueueX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return PersistentQueueX generated by iterative application
     */
    public static <T> PersistentQueueX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit).to(ReactiveConvertableSequence::converter)
                .persistentQueueX(Evaluation.LAZY);

    }

    public static <T> PersistentQueueX<T> of(final T... values) {
        return new LazyPQueueX<>(null,ReactiveSeq.of(values),Reducers.toPersistentQueue(),Evaluation.LAZY);
    }

    public static <T> PersistentQueueX<T> empty() {
        return new LazyPQueueX<>(
                                 BankersQueue.empty(),null,Reducers.toPersistentQueue(),Evaluation.LAZY);
    }

    public static <T> PersistentQueueX<T> singleton(final T value) {
        return PersistentQueueX.<T> empty()
                      .plus(value);
    }

    /**
     * Construct a PersistentQueueX from an Publisher
     *
     * @param publisher
     *            to construct PersistentQueueX from
     * @return PersistentQueueX
     */
    public static <T> PersistentQueueX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to(ReactiveConvertableSequence::converter)
                          .persistentQueueX(Evaluation.LAZY);
    }
    PersistentQueueX<T> type(Reducer<? extends PersistentQueue<T>,T> reducer);

    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  PersistentQueueX<Integer> bag = persistentQueueX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create a PersistentQueueX from
     * @param <T> PersistentQueueX generated from Stream
     * @return
     */
    public static <T> PersistentQueueX<T> persistentQueueX(ReactiveSeq<T> stream) {
        return new LazyPQueueX<>(null,stream,Reducers.toPersistentQueue(),Evaluation.LAZY);
    }

    public static <T> PersistentQueueX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof PersistentQueueX)
            return (PersistentQueueX) iterable;
        if (iterable instanceof PersistentQueue)
            return new LazyPQueueX<>(
                                     (PersistentQueue) iterable,null,Reducers.toPersistentQueue(),Evaluation.LAZY);


        return new LazyPQueueX<>(null,
                ReactiveSeq.fromIterable(iterable),
                Reducers.toPersistentQueue(),Evaluation.LAZY);
    }

    default Tuple2<PersistentQueueX<T>, PersistentQueueX<T>> splitAt(int n) {
        materialize();
        return Tuple.tuple(take(n), drop(n));
    }
    default Tuple2<PersistentQueueX<T>, PersistentQueueX<T>> span(Predicate<? super T> pred) {
        return tuple(takeWhile(pred), dropWhile(pred));
    }

    default Tuple2<PersistentQueueX<T>,PersistentQueueX<T>> splitBy(Predicate<? super T> test) {
        return span(test.negate());
    }
    default Tuple2<PersistentQueueX<T>, PersistentQueueX<T>> partition(final Predicate<? super T> splitter) {

        return tuple(filter(splitter), filter(splitter.negate()));

    }
    default <T> PersistentQueueX<T> fromStream(final ReactiveSeq<T> stream) {
        return persistentQueueX(stream);
    }
    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *     PersistentQueueX.of(1,2,3)
     *            .map(i->i*2)
     *            .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *     //PersistentQueueX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed PersistentQueueX
     */
    default <R> PersistentQueueX<R> coflatMap(Function<? super PersistentQueueX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> PersistentQueueX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                         BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                         Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                         Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (PersistentQueueX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }




    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> PersistentQueueX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                         BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                         Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                         Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                         Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (PersistentQueueX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> PersistentQueueX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                     BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (PersistentQueueX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> PersistentQueueX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                     BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                     Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (PersistentQueueX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> PersistentQueueX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                                 BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (PersistentQueueX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> PersistentQueueX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                                 BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                 BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (PersistentQueueX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);

    }

    @Override
    default boolean containsValue(T item) {
        return LazyCollectionX.super.containsValue(item);
    }


    /**
     * Combine two adjacent elements in a PersistentQueueX using the supplied
     * BinaryOperator This is a stateful grouping & reduction operation. The
     * emitted of a combination may in turn be combined with it's neighbor
     *
     * <pre>
     * {@code
     *  PersistentQueueX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .listX()

     *  //ListX(3,4)
     * }
     * </pre>
     *
     * @param predicate
     *            Test to see if two neighbors should be joined
     * @param op
     *            Reducer to combine neighbors
     * @return Combined / Partially Reduced PersistentQueueX
     */
    @Override
    default PersistentQueueX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (PersistentQueueX<T>) LazyCollectionX.super.combine(predicate, op);
    }
    @Override
    default PersistentQueueX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (PersistentQueueX<T>) LazyCollectionX.super.combine(op, predicate);
    }
    @Override
    default boolean isEmpty() {
        return PersistentQueue.super.isEmpty();
    }
    @Override
    default <R> PersistentQueueX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> PersistentQueueX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> PersistentQueueX<R> unitIterable(final Iterable<R> it) {
        return fromIterable(it);
    }

  //  @Override
    default <R> PersistentQueueX<R> emptyUnit() {
        return empty();
    }

    @Override
    default PersistentQueueX<T> materialize() {
        return (PersistentQueueX<T>)LazyCollectionX.super.materialize();
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    default PersistentQueue<T> toPSet() {
        return this;
    }

    @Override
    default <X> PersistentQueueX<X> from(final Iterable<X> col) {
        return fromIterable(col);
    }

   // @Override
    default <T> Reducer<PersistentQueue<T>,T> monoid() {
        return Reducers.toPersistentQueue();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcollections.PSet#plus(java.lang.Object)
     */
    @Override
    public PersistentQueueX<T> plus(T e);

    /*
     * (non-Javadoc)
     *
     * @see org.pcollections.PSet#insertAt(java.util.Collection)
     */
    @Override
    public PersistentQueueX<T> plusAll(Iterable<? extends T> list);

    @Override
    public PersistentQueueX<T> minus();
    /*
     * (non-Javadoc)
     *
     * @see org.pcollections.PSet#removeValue(java.lang.Object)
     */
    @Override
    public PersistentQueueX<T> removeValue(T e);

    /*
     * (non-Javadoc)
     *
     * @see org.pcollections.PSet#removeAll(java.util.Collection)
     */
    @Override
    public PersistentQueueX<T> removeAll(Iterable<? extends T> list);

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * reverse()
     */
    @Override
    default PersistentQueueX<T> reverse() {
        return (PersistentQueueX<T>) LazyCollectionX.super.reverse();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * filter(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> filter(final Predicate<? super T> pred) {
        return (PersistentQueueX<T>) LazyCollectionX.super.filter(pred);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * transform(java.util.function.Function)
     */
    @Override
    default <R> PersistentQueueX<R> map(final Function<? super T, ? extends R> mapper) {
        return (PersistentQueueX<R>) LazyCollectionX.super.map(mapper);
    }


    @Override
    default <R> PersistentQueueX<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (PersistentQueueX<R>) LazyCollectionX.super.concatMap(mapper);
    }


    default PersistentQueueX<T> take(final long num) {
        return (PersistentQueueX<T>) LazyCollectionX.super.take(num);
    }


    default PersistentQueueX<T> drop(final long num) {
        return (PersistentQueueX<T>) LazyCollectionX.super.drop(num);
    }


    @Override
    default <R> PersistentQueueX<R> mergeMap(int maxConcurency, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (PersistentQueueX<R>) LazyCollectionX.super.mergeMap(maxConcurency,fn);
    }

    @Override
    default <R> PersistentQueueX<R> retry(final Function<? super T, ? extends R> fn) {
        return (PersistentQueueX<R>) LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> PersistentQueueX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (PersistentQueueX<R>) LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> PersistentQueueX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (PersistentQueueX<R>) LazyCollectionX.super.flatMap(fn);
    }

    @Override
    default <R> PersistentQueueX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (PersistentQueueX<R>) LazyCollectionX.super.mergeMap(fn);
    }

    @Override
    default PersistentQueueX<T> prependStream(Stream<? extends T> stream) {
        return (PersistentQueueX<T>) LazyCollectionX.super.prependStream(stream);
    }

    @Override
    default PersistentQueueX<T> appendAll(T... values) {
        return (PersistentQueueX<T>) LazyCollectionX.super.appendAll(values);
    }

    @Override
    default PersistentQueueX<T> append(T value) {
        return (PersistentQueueX<T>) LazyCollectionX.super.append(value);
    }

    @Override
    default PersistentQueueX<T> prepend(T value) {
        return (PersistentQueueX<T>) LazyCollectionX.super.prepend(value);
    }

    @Override
    default PersistentQueueX<T> prependAll(T... values) {
        return (PersistentQueueX<T>) LazyCollectionX.super.prependAll(values);
    }

    @Override
    default PersistentQueueX<T> insertAt(int pos, T... values) {
        return (PersistentQueueX<T>) LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default PersistentQueueX<T> insertAt(int pos, T value) {
        return (PersistentQueueX<T>) LazyCollectionX.super.insertAt(pos,value);
    }

    @Override
    default PersistentQueueX<T> deleteBetween(int start, int end) {
        return (PersistentQueueX<T>) LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default PersistentQueueX<T> insertStreamAt(int pos, Stream<T> stream) {
        return (PersistentQueueX<T>) LazyCollectionX.super.insertStreamAt(pos,stream);
    }

    @Override
    default PersistentQueueX<T> removeAt(long pos) {
        return (PersistentQueueX<T>) LazyCollectionX.super.removeAt(pos);
    }

    @Override
    default PersistentQueueX<T> removeFirst(Predicate<? super T> pred) {
        return (PersistentQueueX<T>) LazyCollectionX.super.removeFirst(pred);
    }

    @Override
    default PersistentQueueX<T> appendAll(Iterable<? extends T> value) {
        return (PersistentQueueX<T>) LazyCollectionX.super.appendAll(value);
    }

    @Override
    default PersistentQueueX<T> prependAll(Iterable<? extends T> value) {
        return (PersistentQueueX<T>) LazyCollectionX.super.prependAll(value);
    }

    @Override
    default PersistentQueueX<T> updateAt(int pos, T value) {
        return (PersistentQueueX<T>) LazyCollectionX.super.updateAt(pos,value);
    }

    @Override
    default PersistentQueueX<T> insertAt(int pos, Iterable<? extends T> values) {
        return (PersistentQueueX<T>) LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default PersistentQueueX<T> insertAt(int pos, ReactiveSeq<? extends T> values) {
        return (PersistentQueueX<T>) LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default PersistentQueueX<T> slice(final long from, final long to) {
        return (PersistentQueueX<T>) LazyCollectionX.super.slice(from, to);
    }


    @Override
    default <U extends Comparable<? super U>> PersistentQueueX<T> sorted(final Function<? super T, ? extends U> function) {
        return (PersistentQueueX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    default PersistentQueueX<Vector<T>> grouped(final int groupSize) {
        return (PersistentQueueX<Vector<T>>) LazyCollectionX.super.grouped(groupSize);
    }


    @Override
    default <U> PersistentQueueX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (PersistentQueueX) LazyCollectionX.super.zip(other);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> PersistentQueueX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (PersistentQueueX<R>) LazyCollectionX.super.zip(other, zipper);
    }


  /*
   * (non-Javadoc)
   *
   * @see
   * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
   * permutations()
   */
    @Override
    default PersistentQueueX<ReactiveSeq<T>> permutations() {

        return (PersistentQueueX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * combinations(int)
     */
    @Override
    default PersistentQueueX<ReactiveSeq<T>> combinations(final int size) {

        return (PersistentQueueX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * combinations()
     */
    @Override
    default PersistentQueueX<ReactiveSeq<T>> combinations() {

        return (PersistentQueueX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    @Override
    default PersistentQueueX<Seq<T>> sliding(final int windowSize) {
        return (PersistentQueueX<Seq<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default PersistentQueueX<Seq<T>> sliding(final int windowSize, final int increment) {
        return (PersistentQueueX<Seq<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default PersistentQueueX<T> scanLeft(final Monoid<T> monoid) {
        return (PersistentQueueX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> PersistentQueueX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (PersistentQueueX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default PersistentQueueX<T> scanRight(final Monoid<T> monoid) {
        return (PersistentQueueX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> PersistentQueueX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (PersistentQueueX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * plusInOrder(java.lang.Object)
     */
    @Override
    default PersistentQueueX<T> plusInOrder(final T e) {

        return (PersistentQueueX<T>) LazyCollectionX.super.plusInOrder(e);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * cycle(int)
     */
    @Override
    default PersistentQueueX<T> cycle(final long times) {

        return (PersistentQueueX<T>) LazyCollectionX.super.cycle(times);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default PersistentQueueX<T> cycle(final Monoid<T> m, final long times) {

        return (PersistentQueueX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * cycleWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (PersistentQueueX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * cycleUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (PersistentQueueX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * zip(java.util.stream.Stream)
     */
    @Override
    default <U> PersistentQueueX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (PersistentQueueX) LazyCollectionX.super.zipWithStream(other);
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> PersistentQueueX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (PersistentQueueX) LazyCollectionX.super.zip3(second, third);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * zip4(java.util.stream.Stream, java.util.stream.Stream,
     * java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> PersistentQueueX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                      final Iterable<? extends T4> fourth) {

        return (PersistentQueueX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * zipWithIndex()
     */
    @Override
    default PersistentQueueX<Tuple2<T, Long>> zipWithIndex() {

        return (PersistentQueueX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * distinct()
     */
    @Override
    default PersistentQueueX<T> distinct() {

        return (PersistentQueueX<T>) LazyCollectionX.super.distinct();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * sorted()
     */
    @Override
    default PersistentQueueX<T> sorted() {

        return (PersistentQueueX<T>) LazyCollectionX.super.sorted();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * sorted(java.util.Comparator)
     */
    @Override
    default PersistentQueueX<T> sorted(final Comparator<? super T> c) {

        return (PersistentQueueX<T>) LazyCollectionX.super.sorted(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * skipWhile(java.util.function.Predicate)
     */
    default PersistentQueueX<T> dropWhile(final Predicate<? super T> p) {

        return (PersistentQueueX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * skipUntil(java.util.function.Predicate)
     */
    default PersistentQueueX<T> dropUntil(final Predicate<? super T> p) {

        return (PersistentQueueX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * limitWhile(java.util.function.Predicate)
     */
    default PersistentQueueX<T> takeWhile(final Predicate<? super T> p) {

        return (PersistentQueueX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * limitUntil(java.util.function.Predicate)
     */
    default PersistentQueueX<T> takeUntil(final Predicate<? super T> p) {

        return (PersistentQueueX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * intersperse(java.lang.Object)
     */
    @Override
    default PersistentQueueX<T> intersperse(final T value) {

        return (PersistentQueueX<T>) LazyCollectionX.super.intersperse(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * shuffle()
     */
    @Override
    default PersistentQueueX<T> shuffle() {

        return (PersistentQueueX<T>) LazyCollectionX.super.shuffle();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * skipLast(int)
     */
    default PersistentQueueX<T> dropRight(final int num) {

        return (PersistentQueueX<T>) LazyCollectionX.super.dropRight(num);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * limitLast(int)
     */
    default PersistentQueueX<T> takeRight(final int num) {

        return (PersistentQueueX<T>) LazyCollectionX.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default PersistentQueueX<T> onEmptySwitch(final Supplier<? extends PersistentQueue<T>> supplier) {
        if (isEmpty())
            return PersistentQueueX.fromIterable(supplier.get());
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * onEmpty(java.lang.Object)
     */
    @Override
    default PersistentQueueX<T> onEmpty(final T value) {

        return (PersistentQueueX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default PersistentQueueX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (PersistentQueueX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> PersistentQueueX<T>  onEmptyError(final Supplier<? extends X> supplier) {

        return (PersistentQueueX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * shuffle(java.util.Random)
     */
    @Override
    default PersistentQueueX<T> shuffle(final Random random) {

        return (PersistentQueueX<T>) LazyCollectionX.super.shuffle(random);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * ofType(java.lang.Class)
     */
    @Override
    default <U> PersistentQueueX<U> ofType(final Class<? extends U> type) {

        return (PersistentQueueX<U>) LazyCollectionX.super.ofType(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * filterNot(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> filterNot(final Predicate<? super T> fn) {

        return (PersistentQueueX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * notNull()
     */
    @Override
    default PersistentQueueX<T> notNull() {

        return (PersistentQueueX<T>) LazyCollectionX.super.notNull();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#
     * removeAll(java.util.stream.Stream)
     */
    @Override
    default PersistentQueueX<T> removeStream(final Stream<? extends T> stream) {

        return (PersistentQueueX<T>) LazyCollectionX.super.removeStream(stream);
    }



    @Override
    default PersistentQueueX<T> removeAll(final T... values) {

        return (PersistentQueueX<T>) LazyCollectionX.super.removeAll(values);
    }
    @Override
    default PersistentQueueX<T> removeAll(CollectionX<? extends T> it) {
        return removeAll((Iterable<T>)it);
    }


    @Override
    default PersistentQueueX<T> retainAll(final Iterable<? extends T> it) {

        return (PersistentQueueX<T>) LazyCollectionX.super.retainAll(it);
    }


    @Override
    default PersistentQueueX<T> retainStream(final Stream<? extends T> seq) {

        return (PersistentQueueX<T>) LazyCollectionX.super.retainStream(seq);
    }


    @Override
    default PersistentQueueX<T> retainAll(final T... values) {

        return (PersistentQueueX<T>) LazyCollectionX.super.retainAll(values);
    }


    @Override
    default <C extends PersistentCollection<? super T>> PersistentQueueX<C> grouped(final int size, final Supplier<C> supplier) {

        return (PersistentQueueX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default PersistentQueueX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (PersistentQueueX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default PersistentQueueX<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (PersistentQueueX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default PersistentQueueX<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (PersistentQueueX<Vector<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends PersistentCollection<? super T>> PersistentQueueX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (PersistentQueueX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends PersistentCollection<? super T>> PersistentQueueX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (PersistentQueueX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default PersistentQueueX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (PersistentQueueX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> PersistentQueueX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (PersistentQueueX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }
    @Override
    default PersistentQueueX<T> plusLoop(int max, IntFunction<T> value) {
        return (PersistentQueueX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default PersistentQueueX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (PersistentQueueX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

  @Override
    default <T2, R> PersistentQueueX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zip(fn, publisher);
    }



    @Override
    default <U> PersistentQueueX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (PersistentQueueX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> PersistentQueueX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> PersistentQueueX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }



    public static  <T,R> PersistentQueueX<R> tailRec(T initial, Function<? super T, ? extends PersistentQueueX<? extends Either<T, R>>> fn) {
        return ListX.tailRec(initial,fn).to().persistentQueueX(Evaluation.LAZY);
    }
}
