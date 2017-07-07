package cyclops.collections.immutable;


import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPQueueX;
import com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX;
import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Xor;
import cyclops.monads.Witness;
import cyclops.typeclasses.*;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.companion.Reducers;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.persistentQueueX;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.foldable.To;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.stream.Spouts;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.AmortizedPQueue;
import org.pcollections.PQueue;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.aol.cyclops2.types.foldable.Evaluation.LAZY;

/**
 * An eXtended Persistent Queue type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface PersistentQueueX<T> extends To<PersistentQueueX<T>>,
                                    PQueue<T>,
                                    LazyCollectionX<T>,
                                    OnEmptySwitch<T, PQueue<T>>,
                                    Higher<persistentQueueX,T>{

    public static <W1,T> Nested<persistentQueueX,W1,T> nested(PersistentQueueX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<persistentQueueX,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,persistentQueueX,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<persistentQueueX,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R> Nested<persistentQueueX,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
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
     * @param queueX toNested narrow generic type
     * @return OrderedSetX with narrowed type
     */
    public static <T> PersistentQueueX<T> narrow(final PersistentQueueX<? extends T> queueX) {
        return (PersistentQueueX<T>) queueX;
    }

    /**
     * Widen a PersistentQueueX nest inside another HKT encoded type
     *
     * @param list HTK encoded type containing  a PQueue toNested widen
     * @return HKT encoded type with a widened PQueue
     */
    public static <C2,T> Higher<C2, Higher<persistentQueueX,T>> widen2(Higher<C2, PersistentQueueX<T>> list){
        //a functor could be used (if C2 is a functor / replaceWith exists for C2 type) instead of casting
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
     *            Number of range toNested skip from
     * @param end
     *            Number for range toNested take at
     * @return Range PersistentQueueX
     */
    public static PersistentQueueX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to()
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Create a PersistentQueueX that contains the Longs between skip and take
     * 
     * @param start
     *            Number of range toNested skip from
     * @param end
     *            Number for range toNested take at
     * @return Range PersistentQueueX
     */
    public static PersistentQueueX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to()
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Unfold a function into a PersistentQueueX
     * 
     * <pre>
     * {@code 
     *  PersistentQueueX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</code>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return PersistentQueueX generated by unfolder function
     */
    static <U, T> PersistentQueueX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to()
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Generate a PersistentQueueX from the provided Supplier up toNested the provided limit number of times
     * 
     * @param limit Max number of elements toNested generate
     * @param s Supplier toNested generate PersistentQueueX elements
     * @return PersistentQueueX generated from the provided Supplier
     */
    public static <T> PersistentQueueX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit).to()
                .persistentQueueX(Evaluation.LAZY);
    }
    
    /**
     * Generate a PersistentQueueX from the provided value up toNested the provided limit number of times
     * 
     * @param limit Max number of elements toNested generate
     * @param s Value for PersistentQueueX elements
     * @return PersistentQueueX generated from the provided Supplier
     */
    public static <T> PersistentQueueX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit).to()
                .persistentQueueX(Evaluation.LAZY);
    }

    /**
     * Create a PersistentQueueX by iterative application of a function toNested an initial element up toNested the supplied limit number of times
     * 
     * @param limit Max number of elements toNested generate
     * @param seed Initial element
     * @param f Iteratively applied toNested each element toNested generate the next element
     * @return PersistentQueueX generated by iterative application
     */
    public static <T> PersistentQueueX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit).to()
                .persistentQueueX(Evaluation.LAZY);

    }

    public static <T> PersistentQueueX<T> of(final T... values) {
        return new LazyPQueueX<>(null,ReactiveSeq.of(values),Reducers.toPQueue(),Evaluation.LAZY);
    }

    public static <T> PersistentQueueX<T> empty() {
        return new LazyPQueueX<>(
                                 AmortizedPQueue.empty(),null,Reducers.toPQueue(),Evaluation.LAZY);
    }

    public static <T> PersistentQueueX<T> singleton(final T value) {
        return PersistentQueueX.<T> empty()
                      .plus(value);
    }

    /**
     * Construct a PersistentQueueX from an Publisher
     * 
     * @param publisher
     *            toNested construct PersistentQueueX from
     * @return PersistentQueueX
     */
    public static <T> PersistentQueueX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to()
                          .persistentQueueX(Evaluation.LAZY);
    }
    PersistentQueueX<T> type(Reducer<? extends PQueue<T>> reducer);

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
        return new LazyPQueueX<>(null,stream,Reducers.toPQueue(),Evaluation.LAZY);
    }

    public static <T> PersistentQueueX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof PersistentQueueX)
            return (PersistentQueueX) iterable;
        if (iterable instanceof PQueue)
            return new LazyPQueueX<>(
                                     (PQueue) iterable,null,Reducers.toPQueue(),Evaluation.LAZY);


        return new LazyPQueueX<>(null,
                ReactiveSeq.fromIterable(iterable),
                Reducers.toPQueue(),Evaluation.LAZY);
    }



    default <T> PersistentQueueX<T> fromStream(final ReactiveSeq<T> stream) {
        return persistentQueueX(stream);
    }
    /**
     * coflatMap pattern, can be used toNested perform maybe reductions / collections / folds and other terminal operations
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
    default AnyMSeq<persistentQueueX,T> anyM(){
        return AnyM.fromPersistentQueueX(this);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> PersistentQueueX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                         BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                         Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                         Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (PersistentQueueX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }




    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> PersistentQueueX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                         BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                         Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                         Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                         Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (PersistentQueueX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> PersistentQueueX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                     BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                     Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (PersistentQueueX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> PersistentQueueX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                     BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                     Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                     Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (PersistentQueueX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> PersistentQueueX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                                 BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (PersistentQueueX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> PersistentQueueX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                                 BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                 BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (PersistentQueueX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);

    }
    @Override
    default PersistentQueueX<T> take(final long num) {

        return limit(num);
    }
    @Override
    default PersistentQueueX<T> drop(final long num) {

        return skip(num);
    }
    
    /**
     * Combine two adjacent elements in a PersistentQueueX using the supplied
     * BinaryOperator This is a stateful grouping & reduction operation. The
     * emitted of a combination may in turn be combined with it's neighbor
     * 
     * <pre>
     * {@code 
     *  PersistentQueueX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .listX()
                   
     *  //ListX(3,4) 
     * }
     * </pre>
     * 
     * @param predicate
     *            Test toNested see if two neighbors should be joined
     * @param op
     *            Reducer toNested combine neighbors
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
    default <R> PersistentQueueX<R> unit(final Collection<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> PersistentQueueX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> PersistentQueueX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
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

    default PQueue<T> toPSet() {
        return this;
    }

    @Override
    default <X> PersistentQueueX<X> from(final Collection<X> col) {
        return fromIterable(col);
    }

   // @Override
    default <T> Reducer<PQueue<T>> monoid() {
        return Reducers.toPQueue();
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
     * @see org.pcollections.PSet#plusAll(java.util.Collection)
     */
    @Override
    public PersistentQueueX<T> plusAll(Collection<? extends T> list);

    @Override
    public PersistentQueueX<T> minus();
    /*
     * (non-Javadoc)
     * 
     * @see org.pcollections.PSet#minus(java.lang.Object)
     */
    @Override
    public PersistentQueueX<T> minus(Object e);

    /*
     * (non-Javadoc)
     * 
     * @see org.pcollections.PSet#minusAll(java.util.Collection)
     */
    @Override
    public PersistentQueueX<T> minusAll(Collection<?> list);

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * map(java.util.function.Function)
     */
    @Override
    default <R> PersistentQueueX<R> map(final Function<? super T, ? extends R> mapper) {
        return (PersistentQueueX<R>) LazyCollectionX.super.map(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * flatMap(java.util.function.Function)
     */
    @Override
    default <R> PersistentQueueX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (PersistentQueueX<R>) LazyCollectionX.super.flatMap(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * limit(long)
     */
    @Override
    default PersistentQueueX<T> limit(final long num) {
        return (PersistentQueueX<T>) LazyCollectionX.super.limit(num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * skip(long)
     */
    @Override
    default PersistentQueueX<T> skip(final long num) {
        return (PersistentQueueX<T>) LazyCollectionX.super.skip(num);
    }

    @Override
    default PersistentQueueX<T> takeRight(final int num) {
        return (PersistentQueueX<T>) LazyCollectionX.super.takeRight(num);
    }

    @Override
    default PersistentQueueX<T> dropRight(final int num) {
        return (PersistentQueueX<T>) LazyCollectionX.super.dropRight(num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * takeWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> takeWhile(final Predicate<? super T> p) {
        return (PersistentQueueX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * dropWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> dropWhile(final Predicate<? super T> p) {
        return (PersistentQueueX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * takeUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> takeUntil(final Predicate<? super T> p) {
        return (PersistentQueueX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * dropUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> dropUntil(final Predicate<? super T> p) {
        return (PersistentQueueX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * trampoline(java.util.function.Function)
     */
    @Override
    default <R> PersistentQueueX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (PersistentQueueX<R>) LazyCollectionX.super.trampoline(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * slice(long, long)
     */
    @Override
    default PersistentQueueX<T> slice(final long from, final long to) {
        return (PersistentQueueX<T>) LazyCollectionX.super.slice(from, to);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> PersistentQueueX<T> sorted(final Function<? super T, ? extends U> function) {
        return (PersistentQueueX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    default PersistentQueueX<ListX<T>> grouped(final int groupSize) {
        return (PersistentQueueX<ListX<T>>) LazyCollectionX.super.grouped(groupSize);
    }

    @Override
    default <K, A, D> PersistentQueueX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {
        return (PersistentQueueX) LazyCollectionX.super.grouped(classifier, downstream);
    }

    @Override
    default <K> PersistentQueueX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return (PersistentQueueX) LazyCollectionX.super.grouped(classifier);
    }

    @Override
    default <U> PersistentQueueX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (PersistentQueueX) LazyCollectionX.super.zip(other);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> PersistentQueueX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (PersistentQueueX<R>) LazyCollectionX.super.zip(other, zipper);
    }

    @Override
    default <U, R> PersistentQueueX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (PersistentQueueX<R>) LazyCollectionX.super.zipS(other, zipper);
    }



    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * combinations()
     */
    @Override
    default PersistentQueueX<ReactiveSeq<T>> combinations() {

        return (PersistentQueueX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    @Override
    default PersistentQueueX<VectorX<T>> sliding(final int windowSize) {
        return (PersistentQueueX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default PersistentQueueX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (PersistentQueueX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default PersistentQueueX<T> cycle(final Monoid<T> m, final long times) {

        return (PersistentQueueX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> PersistentQueueX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (PersistentQueueX) LazyCollectionX.super.zipS(other);
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, U> PersistentQueueX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (PersistentQueueX) LazyCollectionX.super.zip3(second, third);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream,
     * java.util.reactiveStream.Stream)
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * skipWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> skipWhile(final Predicate<? super T> p) {

        return (PersistentQueueX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * skipUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> skipUntil(final Predicate<? super T> p) {

        return (PersistentQueueX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * limitWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> limitWhile(final Predicate<? super T> p) {

        return (PersistentQueueX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * limitUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentQueueX<T> limitUntil(final Predicate<? super T> p) {

        return (PersistentQueueX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * skipLast(int)
     */
    @Override
    default PersistentQueueX<T> skipLast(final int num) {

        return (PersistentQueueX<T>) LazyCollectionX.super.skipLast(num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * limitLast(int)
     */
    @Override
    default PersistentQueueX<T> limitLast(final int num) {

        return (PersistentQueueX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default PersistentQueueX<T> onEmptySwitch(final Supplier<? extends PQueue<T>> supplier) {
        if (isEmpty())
            return PersistentQueueX.fromIterable(supplier.get());
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> PersistentQueueX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (PersistentQueueX<T>) LazyCollectionX.super.onEmptyThrow(supplier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
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
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * removeAll(java.util.reactiveStream.Stream)
     */
    @Override
    default PersistentQueueX<T> removeAllS(final Stream<? extends T> stream) {

        return (PersistentQueueX<T>) LazyCollectionX.super.removeAllS(stream);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * removeAll(java.lang.Iterable)
     */
    @Override
    default PersistentQueueX<T> removeAllI(final Iterable<? extends T> it) {

        return (PersistentQueueX<T>) LazyCollectionX.super.removeAllI(it);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * removeAll(java.lang.Object[])
     */
    @Override
    default PersistentQueueX<T> removeAll(final T... values) {

        return (PersistentQueueX<T>) LazyCollectionX.super.removeAll(values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * retainAllI(java.lang.Iterable)
     */
    @Override
    default PersistentQueueX<T> retainAllI(final Iterable<? extends T> it) {

        return (PersistentQueueX<T>) LazyCollectionX.super.retainAllI(it);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * retainAllI(java.util.reactiveStream.Stream)
     */
    @Override
    default PersistentQueueX<T> retainAllS(final Stream<? extends T> seq) {

        return (PersistentQueueX<T>) LazyCollectionX.super.retainAllS(seq);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * retainAllI(java.lang.Object[])
     */
    @Override
    default PersistentQueueX<T> retainAll(final T... values) {

        return (PersistentQueueX<T>) LazyCollectionX.super.retainAll(values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#
     * cast(java.lang.Class)
     */
    @Override
    default <U> PersistentQueueX<U> cast(final Class<? extends U> type) {

        return (PersistentQueueX<U>) LazyCollectionX.super.cast(type);
    }


    @Override
    default <C extends Collection<? super T>> PersistentQueueX<C> grouped(final int size, final Supplier<C> supplier) {

        return (PersistentQueueX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default PersistentQueueX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (PersistentQueueX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default PersistentQueueX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (PersistentQueueX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default PersistentQueueX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (PersistentQueueX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> PersistentQueueX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (PersistentQueueX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> PersistentQueueX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

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
    default PersistentQueueX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (PersistentQueueX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

    @Override
    default PersistentQueueX<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return (PersistentQueueX<T>)LazyCollectionX.super.zip(combiner,app);
    }

    @Override
    default <R> PersistentQueueX<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zipWith(fn);
    }

    @Override
    default <R> PersistentQueueX<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zipWithS(fn);
    }

    @Override
    default <R> PersistentQueueX<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zipWithP(fn);
    }

    @Override
    default <T2, R> PersistentQueueX<R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zipP(publisher,fn);
    }



    @Override
    default <U> PersistentQueueX<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (PersistentQueueX)LazyCollectionX.super.zipP(other);
    }


    @Override
    default <S, U, R> PersistentQueueX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> PersistentQueueX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (PersistentQueueX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }

    /**
     * Companion class for creating Type Class instances for working with PQueues
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {
        public static InstanceDefinitions<persistentQueueX> definitions(){
            return new InstanceDefinitions<persistentQueueX>() {
                @Override
                public <T, R> Functor<persistentQueueX> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<persistentQueueX> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<persistentQueueX> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<persistentQueueX> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<persistentQueueX>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<persistentQueueX>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<persistentQueueX> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<persistentQueueX>> monadPlus(Monoid<Higher<persistentQueueX, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Maybe<Traverse<persistentQueueX>> traverse() {
                    return Maybe.just(Instances.traverse());
                }

                @Override
                public <T> Maybe<Foldable<persistentQueueX>> foldable() {
                    return Maybe.just(Instances.foldable());
                }

                @Override
                public <T> Maybe<Comonad<persistentQueueX>> comonad() {
                    return Maybe.none();
                }
                @Override
                public <T> Maybe<Unfoldable<persistentQueueX>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };
        }

        public static Unfoldable<persistentQueueX> unfoldable(){
            return new Unfoldable<persistentQueueX>() {
                @Override
                public <R, T> Higher<persistentQueueX, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return PersistentQueueX.unfold(b,fn);
                }
            };
        }

        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  PersistentQueueX<Integer> list = PQueues.functor().map(i->i*2, Arrays.asPQueue(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with PQueues
         * <pre>
         * {@code
         *   PersistentQueueX<Integer> list = PQueues.unit()
        .unit("hello")
        .applyHKT(h->PQueues.functor().map((String v) ->v.length(), h))
        .convert(PersistentQueueX::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for PQueues
         */
        public static <T,R>Functor<persistentQueueX> functor(){
            BiFunction<PersistentQueueX<T>,Function<? super T, ? extends R>,PersistentQueueX<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * PersistentQueueX<String> list = PQueues.unit()
        .unit("hello")
        .convert(PersistentQueueX::narrowK3);

        //Arrays.asPQueue("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for PQueues
         */
        public static <T> Pure<persistentQueueX> unit(){
            return General.<persistentQueueX,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.PersistentQueueX.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asPQueue;
         *
        PQueues.zippingApplicative()
        .ap(widen(asPQueue(l1(this::multiplyByTwo))),widen(asPQueue(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * PersistentQueueX<Function<Integer,Integer>> listFn =PQueues.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(PersistentQueueX::narrowK3);

        PersistentQueueX<Integer> list = PQueues.unit()
        .unit("hello")
        .applyHKT(h->PQueues.functor().map((String v) ->v.length(), h))
        .applyHKT(h->PQueues.zippingApplicative().ap(listFn, h))
        .convert(PersistentQueueX::narrowK3);

        //Arrays.asPQueue("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for PQueues
         */
        public static <T,R> Applicative<persistentQueueX> zippingApplicative(){
            BiFunction<PersistentQueueX< Function<T, R>>,PersistentQueueX<T>,PersistentQueueX<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.PersistentQueueX.widen;
         * PersistentQueueX<Integer> list  = PQueues.monad()
        .flatMap(i->widen(PersistentQueueX.range(0,i)), widen(Arrays.asPQueue(1,2,3)))
        .convert(PersistentQueueX::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    PersistentQueueX<Integer> list = PQueues.unit()
        .unit("hello")
        .applyHKT(h->PQueues.monad().flatMap((String v) ->PQueues.unit().unit(v.length()), h))
        .convert(PersistentQueueX::narrowK3);

        //Arrays.asPQueue("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for PQueues
         */
        public static <T,R> Monad<persistentQueueX> monad(){

            BiFunction<Higher<persistentQueueX,T>,Function<? super T, ? extends Higher<persistentQueueX,R>>,Higher<persistentQueueX,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  PersistentQueueX<String> list = PQueues.unit()
        .unit("hello")
        .applyHKT(h->PQueues.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(PersistentQueueX::narrowK3);

        //Arrays.asPQueue("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<persistentQueueX> monadZero(){

            return General.monadZero(monad(), PersistentQueueX.empty());
        }
        /**
         * <pre>
         * {@code
         *  PersistentQueueX<Integer> list = PQueues.<Integer>monadPlus()
        .plus(Arrays.asPQueue()), Arrays.asPQueue(10)))
        .convert(PersistentQueueX::narrowK3);
        //Arrays.asPQueue(10))
         *
         * }
         * </pre>
         * @return Type class for combining PQueues by concatenation
         */
        public static <T> MonadPlus<persistentQueueX> monadPlus(){
            Monoid<PersistentQueueX<T>> m = Monoid.of(PersistentQueueX.empty(), Instances::concat);
            Monoid<Higher<persistentQueueX,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T,R> MonadRec<persistentQueueX> monadRec(){

            return new MonadRec<persistentQueueX>(){
                @Override
                public <T, R> Higher<persistentQueueX, R> tailRec(T initial, Function<? super T, ? extends Higher<persistentQueueX,? extends Xor<T, R>>> fn) {
                    PersistentQueueX<Xor<T, R>> next = PersistentQueueX.of(Xor.secondary(initial));
                    boolean newValue[] = {false};
                    for(;;){
                        next = next.flatMap(e -> e.visit(s -> { newValue[0]=true; return narrowK(fn.apply(s)); }, p -> PersistentQueueX.of(e)));
                        if(!newValue[0])
                            break;
                    }
                    return Xor.sequencePrimary(next).map(l->l.to().persistentQueueX(LAZY)).get();
                }
            };
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<PersistentQueueX<Integer>> m = Monoid.of(Arrays.asPQueue()), (a,b)->a.isEmpty() ? b : a);
        PersistentQueueX<Integer> list = PQueues.<Integer>monadPlus(m)
        .plus(Arrays.asPQueue(5)), Arrays.asPQueue(10)))
        .convert(PersistentQueueX::narrowK3);
        //Arrays.asPQueue(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid toNested use for combining PQueues
         * @return Type class for combining PQueues
         */
        public static <T> MonadPlus<persistentQueueX> monadPlus(Monoid<PersistentQueueX<T>> m){
            Monoid<Higher<persistentQueueX,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<persistentQueueX> traverse(){
            BiFunction<Applicative<C2>,PersistentQueueX<Higher<C2, T>>,Higher<C2, PersistentQueueX<T>>> sequenceFn = (ap, list) -> {

                Higher<C2,PersistentQueueX<T>> identity = ap.unit(PersistentQueueX.empty());

                BiFunction<Higher<C2,PersistentQueueX<T>>,Higher<C2,T>,Higher<C2,PersistentQueueX<T>>> combineToPQueue =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

                BinaryOperator<Higher<C2,PersistentQueueX<T>>> combinePQueues = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;

                return list.stream()
                        .reduce(identity,
                                combineToPQueue,
                                combinePQueues);


            };
            BiFunction<Applicative<C2>,Higher<persistentQueueX,Higher<C2, T>>,Higher<C2, Higher<persistentQueueX,T>>> sequenceNarrow  =
                    (a,b) -> PersistentQueueX.widen2(sequenceFn.apply(a, PersistentQueueX.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = PQueues.foldable()
        .foldLeft(0, (a,b)->a+b, Arrays.asPQueue(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<persistentQueueX> foldable(){
            BiFunction<Monoid<T>,Higher<persistentQueueX,T>,T> foldRightFn =  (m, l)-> PersistentQueueX.narrowK(l).foldRight(m);
            BiFunction<Monoid<T>,Higher<persistentQueueX,T>,T> foldLeftFn = (m, l)-> PersistentQueueX.narrowK(l).reduce(m);
            return General.foldable(foldRightFn, foldLeftFn);
        }

        private static  <T> PersistentQueueX<T> concat(PQueue<T> l1, PQueue<T> l2){

            return PersistentQueueX.fromIterable(l1.plusAll(l2));
        }
        private <T> PersistentQueueX<T> of(T value){
            return PersistentQueueX.of(value);
        }
        private static <T,R> PersistentQueueX<R> ap(PersistentQueueX<Function< T, R>> lt, PersistentQueueX<T> list){
            return PersistentQueueX.fromIterable(lt).zip(list,(a, b)->a.apply(b));
        }
        private static <T,R> Higher<persistentQueueX,R> flatMap(Higher<persistentQueueX,T> lt, Function<? super T, ? extends  Higher<persistentQueueX,R>> fn){
            return PersistentQueueX.fromIterable(PersistentQueueX.narrowK(lt)).flatMap(fn.andThen(PersistentQueueX::narrowK));
        }
        private static <T,R> PersistentQueueX<R> map(PersistentQueueX<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);
        }
    }
}
