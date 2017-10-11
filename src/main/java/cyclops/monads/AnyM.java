package cyclops.monads;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops2.internal.adapters.StreamAdapter;
import com.aol.cyclops2.internal.monads.AnyMValue2Impl;
import com.aol.cyclops2.internal.stream.ReactiveStreamX;
import com.aol.cyclops2.types.anyM.AnyMValue2;
import com.aol.cyclops2.types.factory.EmptyUnit;
import com.aol.cyclops2.types.factory.Unit;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.collections.immutable.*;
import cyclops.collections.mutable.*;
import cyclops.companion.Streams;
import cyclops.control.*;
import cyclops.control.lazy.*;
import cyclops.monads.function.AnyMFunction1;
import cyclops.monads.function.AnyMFunction2;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import com.aol.cyclops2.types.*;
import cyclops.async.Future;
import cyclops.function.*;
import cyclops.stream.*;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import org.reactivestreams.Publisher;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.internal.monads.AnyMSeqImpl;
import com.aol.cyclops2.internal.monads.AnyMValueImpl;

import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.anyM.AnyMValue;
import cyclops.monads.Witness.completableFuture;
import cyclops.monads.Witness.eval;
import cyclops.monads.Witness.ior;
import cyclops.monads.Witness.list;
import cyclops.monads.Witness.maybe;
import cyclops.monads.Witness.optional;
import cyclops.monads.Witness.set;
import cyclops.monads.Witness.stream;
import cyclops.monads.Witness.streamable;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.monads.Witness.tryType;
import cyclops.monads.Witness.either;
import cyclops.monads.Witness.*;
import cyclops.monads.Witness.future;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.stream.ToStream;
import cyclops.companion.Optionals;

import static com.aol.cyclops2.types.foldable.Evaluation.LAZY;

/**
 * 
 * Wrapper for Any Monad type
 * 
 * There are two subsclass of AnyM - @see {@link AnyMValue} and  @see {@link AnyMSeq}. 
 * AnyMValue is used to represent Monads that wrap a singleUnsafe value such as {@link Optional}, {@link CompletableFuture}, {@link Maybe}, {@link Eval}, {@link Either}, {@link Try}, {@link Ior}, {@link FeatureToggle}
 * AnyMSeq is used to represent Monads that wrap an aggregation of values such as {@link Stream}, {@link FutureStream}, {@link List}, {@link Set}, {@link Streamable}
 * 
 * Use AnyM to create your monad wrapper.
 * AnyM.fromXXXX methods can create the appropriate AnyM type for a range of known monad types.
 * 
 * <pre>
 * {@code 
 *    AnyMValue<optional,String> monad1 = AnyM.fromOptional(Optional.of("hello"));
 *    
 *    AnyMSeq<stream,String> monad2 = AnyM.fromStream(Stream.of("hello","world"));
 *  
 * }
 * </pre>
 * 
 * Wrapped monads can be unwrapped via the unwrap method, or converted to the desired type via toXXXX methods
 * 
 *
 * 
 * @author johnmcclean
 *
 * @param <T> type data wrapped by the underlying monad
 */
public interface AnyM<W extends WitnessType<W>,T> extends   Unwrapable,
                                                            To<AnyM<W,T>>,
                                                            EmptyUnit<T>,
                                                            Unit<T>,
                                                            Folds<T>,
                                                            Transformable<T>,
                                                            ToStream<T>,
                                                            Zippable<T>,
                                                            Publisher<T>,
                                                            Filters<T>{
    @Override
    default ReactiveSeq<T> reactiveSeq() {
        return Streams.oneShotStream(StreamSupport.stream(this.spliterator(),false));
    }

    /**
     * Collect the contents of the monad wrapped by this AnyM into supplied collector
     * A mutable reduction operation equivalent to Stream#collect
     *
     * <pre>
     * {@code
     *      AnyM<Integer> monad1 = AnyM.fromStream(Stream.of(1,2,3));
     *      AnyM<Integer> monad2 = AnyM.fromOptional(Optional.of(1));
     *
     *      List<Integer> list1 = monad1.collect(CyclopsCollectors.toList());
     *      List<Integer> list2 = monad2.collect(CyclopsCollectors.toList());
     *
     * }
     * </pre>
     *
     *
     * @param collector JDK collector to perform mutable reduction
     * @return Reduced value
     */
    default <R, A> R collect(Collector<? super T, A, R> collector){
        return this.stream().collect(collector);
    }
    @Override
    default Iterator<T> iterator() {

        return adapter().toIterable(this).iterator();

    }
    
    default <U> AnyMSeq<W,U> unitIterator(Iterator<U> U){
        return (AnyMSeq<W,U>)adapter().unitIterable(()->U);
    }

    <R> AnyM<W,R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn);
    <R> AnyM<W,R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn);
    <R> AnyM<W,R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn);
    default <R> AnyM<W,R> flatMapA(Function<? super T, ? extends AnyM<W,? extends R>> fn){
        return adapter().flatMap(this, fn);
    }
    default <R> AnyM<W,R> map(Function<? super T,? extends R> fn){
        return adapter().map(this, fn);
    }
    default <T> AnyM<W,T> fromIterable(Iterable<T> t){
        return  (AnyM<W,T>)adapter().unitIterable(t);
    }

    @Override
    default <R> AnyM<W,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (AnyM<W,R>)Zippable.super.zipWith(fn);
    }

    @Override
    default <R> AnyM<W,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (AnyM<W,R>)Zippable.super.zipWithS(fn);
    }

    @Override
    default <R> AnyM<W,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (AnyM<W,R>)Zippable.super.zipWithP(fn);
    }

    @Override
    default <R> AnyM<W,R> retry(final Function<? super T, ? extends R> fn) {
        return (AnyM<W,R>)Zippable.super.retry(fn);
    }

    @Override
    default <U> AnyM<W,Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (AnyM)Zippable.super.zipP(other);
    }

    @Override
    default <R> AnyM<W,R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (AnyM<W,R>)Zippable.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> AnyM<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (AnyM)Zippable.super.zip3(second,third);
    }

    @Override
    default <S, U, R> AnyM<W,R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (AnyM<W,R>)Zippable.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> AnyM<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (AnyM)Zippable.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> AnyM<W,R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (AnyM<W,R>)Zippable.super.zip4(second,third,fourth,fn);
    }


    /**
     * Construct a new instanceof AnyM using the type of the underlying wrapped monad
     * 
     * <pre>
     * {@code
     *   AnyM<Integer> ints = AnyM.fromList(Arrays.asList(1,2,3);
     *   AnyM<String> string = ints.unit("hello");
     * }
     * </pre>
     * 
     * @param t to embed inside the monad wrapped by AnyM
     * @return Newly instantated AnyM
     */
    @Override
    default <T> AnyM<W,T> unit(T t){
        return adapter().unit(t);
    }
    
    /**
     * Applicative 'ap' method to use fluently
     * 
     * <pre>
     * {@code 
     *    AnyM<optional,Function<Integer,Integer>> add = AnyM.fromNullable(this::add2);
     *    add.to(AnyM::ap)
     *       .applyHKT(AnyM.ofNullable(10));
     *   
     *    //AnyM[12] //add 2
     * 
     * }
     * </pre>
     * 
     * @param fn Function inside an Applicative
     * @return Function to applyHKT an Applicative's value to function
     */
    public static <W extends WitnessType<W>,T,R> Function<AnyM<W,T>,AnyM<W,R>> ap(AnyM<W, Function<T,R>> fn){
        return apply->apply.adapter().ap(fn,apply);
    }
    /**
     * Applicative ap2 method to use fluently to applyHKT to a curried function
     * <pre>
     * {@code 
     *    AnyM<optional,Function<Integer,Function<Integer,Integer>>> add = AnyM.fromNullable(Curry.curry2(this::add));
     *    add.to(AnyM::ap2)
     *       .applyHKT(AnyM.ofNullable(10),AnyM.ofNullable(20));
     *   
     *    //AnyM[30] //add together
     * 
     * }
     * </pre>
     * @param fn Curried function inside an Applicative
     * @return Function to applyHKT two Applicative's values to a function
     */
    public static <W extends WitnessType<W>,T,T2,R> BiFunction<AnyM<W,T>,AnyM<W,T2>,AnyM<W,R>> ap2(AnyM<W, Function<T,Function<T2,R>>> fn){
        return (apply1,apply2)->apply1.adapter().ap2(fn,apply1,apply2);
    }

    /**
     * Perform a filter operation on the wrapped monad instance e.g.
     * 
     * <pre>
     * {@code
     *   AnyM.fromOptional(Optional.of(10)).filter(i->i<10);
     * 
     *   //AnyM[Optional.zero()]
     *   
     *   AnyM.fromStream(Stream.of(5,10)).filter(i->i<10);
     *   
     *   //AnyM[Stream[5]]
     * }
     * 
     * 
     * </pre>
     * 
     * @param fn Filtering predicate
     * @return Filtered AnyM
     */
    default  AnyM<W,T> filter(Predicate<? super T> fn){
        return adapter().filter(this, fn);
    }


    default <R> AnyM<W,R> coflatMapA(final Function<? super AnyM<W,T>, R> mapper) {
        return unit(Lambda.λ(()->mapper.apply(this))).map(Supplier::get);
    }
    
    
    default AnyM<W,AnyM<W,T>> nestA() {
        return unit(this);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.factory.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> Unit<T> emptyUnit(){
        return adapter().empty();
    }

    /**
     * Tests for equivalency between two AnyM types
     * 
     * <pre>
     * {@code
     *    boolean eqv = AnyM.fromOptional(Optional.of(1)).eqv(AnyM.fromStream(Stream.of(1)));
     *    //true
     *     boolean eqv = AnyM.fromOptional(Optional.of(1)).eqv(AnyM.fromStream(Stream.of(1,2)));
     *    //false
     * }
     * </pre>
     * 
     * @param t AnyM to check for equivalence with this AnyM
     * @return true if monads are equivalent
     */
    default boolean eqv(final AnyM<?,T> t) {
        return Predicates.eqvIterable(t)
                         .test(this);
    }

    /**
     * Allows structural matching on the value / seq nature of this AnyM.
     * If this AnyM can only store a singleUnsafe value an Xor.lazyLeft with type AnyMValue is returned
     * If this AnyM can  store one or many values an Xor.lazyRight with type AnyMSeq is returned
     * 
     * <pre>
     * {@code
     *    AnyM<String> monad;
     *    
     *    monad.matchable().visit(v->handleValue(v.get()),s->handleSequence(s.toList()));
     * }
     * </pre>
     * 
     * 
     * @return An Xor for pattern matching lazy an AnyMValue or AnyMSeq
     */
    Either<AnyMValue<W,T>, AnyMSeq<W,T>> matchable();




    /* 
     * Convert this AnyM to an extended Stream (ReactiveSeq)
     * 
     * <pre>
     * {@code 
     *    AnyM<Integer> monad =  AnyM.fromOptional(Optional.of(10));
     *    
     *    Stream<Integer> reactiveStream = monad.reactiveStream();
     *    //ReactiveSeq[10]
     * }
     * </pre>
     * 
     */
    @Override
    default ReactiveSeq<T> stream(){
        return this.adapter().toStream(this);
    }

    

    /**
     * Perform a peek operation on the wrapped monad e.g.
     * 
     * <pre>
     * {@code 
     *   AnyM.fromCompletableFuture(CompletableFuture.supplyAsync(()->loadData())
     *       .peek(System.out::println)
     * }
     * </pre>
     * 
     * @param c Consumer to accept current data
     * @return AnyM after peek operation
     */
    @Override
    default AnyM<W,T> peek(Consumer<? super T> c){
        return (AnyM<W, T>) Zippable.super.peek(c);
    }



    /**
     * join / flatten one level of a nest hierarchy
     * 
     * @return Flattened / joined one level
     */ 
    static <W extends WitnessType<W>,T1> AnyM<W,T1> flatten(AnyM<W,? extends AnyM<W,T1>> nested){
        return nested.flatMapA(Function.identity());
    }
    static <W extends WitnessType<W>,T1> AnyM<W,T1> flattenI(AnyM<W,? extends Iterable<T1>> nested){
        return nested.flatMapI(Function.identity());
    }

   
    /**
     * Aggregate the contents of this Monad and the supplied Monad 
     * 
     * <pre>{@code 
     * 
     * AnyM.fromStream(Stream.of(1,2,3,4))
     * 							.aggregate(fromEither5(Optional.of(5)))
     * 
     * AnyM[Stream[List[1,2,3,4,5]]
     * 
     * List<Integer> result = AnyM.fromStream(Stream.of(1,2,3,4))
     * 							.aggregate(fromEither5(Optional.of(5)))
     * 							.toSequence()
     *                          .flatten()
     * 							.toList();
    	
    	assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
    	}</pre>
     * 
     * @param next Monad to aggregate content with
     * @return Aggregated Monad
     */
    default AnyM<W,List<T>> aggregate(AnyM<W,T> next){
        return unit(Stream.concat(matchable().visit(value -> value.stream(), seq -> seq.stream()), next.matchable()
                                  .visit(value -> value.stream(),
                                         seq -> seq.stream()))
                    .collect(Collectors.toList()));
    }

   
    

    /**
     * Construct an AnyM wrapping a new zero instance of the wrapped type
     * 
     * e.g.
     * <pre>
     * {@code 
     * Any<Integer> ints = AnyM.fromStream(Stream.of(1,2,3));
     * AnyM<Integer> zero=ints.zero();
     * }
     * </pre>
     * @return Empty AnyM
     */
    default <T> AnyM<W,T> empty(){
        return adapter().empty();
    }

    
    /**
     * @return String representation of this AnyM
     */
    @Override
    public String toString();

    /**
     * Construct an AnyM instance that wraps a range from skip (inclusive) to take (exclusive) provided
     * 
     * The AnyM will contain a SequenceM over the spefied range
     * 
     * @param start Inclusive skip of the range
     * @param end Exclusive take of the range
     * @return AnyM range
     */
    public static AnyMSeq<reactiveSeq,Integer> rangeOf(final int start, final int end) {

        return AnyM.fromStream(ReactiveSeq.range(start, end));
    }

    public static <T> AnyMSeq<reactiveSeq,T> generate(final Generator<T> generator) {

        return AnyM.fromStream(ReactiveSeq.generate(generator));
    }
    /**
     * Construct an AnyM instance that wraps a range from skip (inclusive) to take (exclusive) provided
     * 
     * The AnyM will contain a SequenceM over the spefied range
     * 
     * @param start Inclusive skip of the range
     * @param end Exclusive take of the range
     * @return AnyM range
     */
    public static AnyMSeq<reactiveSeq,Long> rangeLongOf(final long start, final long end) {

        return AnyM.fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Wrap a Streamable inside an AnyM
     * 
     * @param streamable wrap
     * @return AnyMSeq generated from a ToStream type
     */
    public static <T> AnyMSeq<streamable,T> fromStreamable(final ToStream<T> streamable) {
        Objects.requireNonNull(streamable);
        
        return AnyMFactory.instance.seq(Streamable.fromIterable(streamable),Witness.streamable.INSTANCE);
    }


    /**
     * Create an AnyM from a List
     * 
     *
     * @param list to wrap inside an AnyM
     * @return AnyM wrapping a list
     */
    public static <T> AnyMSeq<list,T> fromList(final List<T> list) {
        Objects.requireNonNull(list);
        final List<T> toUse = (list instanceof CollectionX)? list : ListX.fromIterable(list);
        return AnyMFactory.instance.seq(toUse,Witness.list.INSTANCE);
    }
    public static <W extends Witness.CollectionXWitness<W>,T> AnyMSeq<W,T> fromCollectionX(final CollectionX<T> collection, W witness) {
        Objects.requireNonNull(collection);
        return AnyMFactory.instance.seq(collection,witness);

    }

    /**
     * Create an AnyM from a Set
     * @param set to wrap inside an AnyM
     * @return AnyM wrapping a Set
     */
    public static <T> AnyMSeq<set,T> fromSet(final Set<T> set) {
        Objects.requireNonNull(set);
        final Set<T> toUse = (set instanceof CollectionX)? set : SetX.fromIterable(set);
        return AnyMFactory.instance.seq(SetX.fromIterable(set), Witness.set.INSTANCE);
    }
    public static <T> AnyMSeq<queue,T> fromQueue(final Queue<T> queue) {
        Objects.requireNonNull(queue);
        final Queue<T> toUse = (queue instanceof CollectionX)? queue : QueueX.fromIterable(queue);
        return AnyMFactory.instance.seq(QueueX.fromIterable(queue), Witness.queue.INSTANCE);
    }
    public static <T> AnyMSeq<deque,T> fromDeque(final Deque<T> deque) {
        Objects.requireNonNull(deque);
        final Deque<T> toUse = (deque instanceof CollectionX)? deque : DequeX.fromIterable(deque);
        return AnyMFactory.instance.seq(DequeX.fromIterable(deque), Witness.deque.INSTANCE);
    }
    public static <T> AnyMSeq<sortedSet,T> fromSortedSet(final SortedSet<T> set) {
        Objects.requireNonNull(set);
        final SortedSet<T> toUse = (set instanceof CollectionX)? set : SortedSetX.fromIterable(set);
        return AnyMFactory.instance.seq(SortedSetX.fromIterable(set), Witness.set.INSTANCE);
    }
    public static <T> AnyMSeq<linkedListX,T> fromLinkedListX(final LinkedListX<T> list) {
        Objects.requireNonNull(list);
        return AnyMFactory.instance.seq(list, Witness.linkedListX.INSTANCE);
    }
    public static <T> AnyMSeq<vectorX,T> fromVectorX(final VectorX<T> list) {
        Objects.requireNonNull(list);
        return AnyMFactory.instance.seq(list, Witness.vectorX.INSTANCE);
    }
    public static <T> AnyMSeq<persistentSetX,T> fromPersistentSetX(final PersistentSetX<T> set) {
        Objects.requireNonNull(set);
        return AnyMFactory.instance.seq(set, Witness.persistentSetX.INSTANCE);
    }
    public static <T> AnyMSeq<persistentQueueX,T> fromPersistentQueueX(final PersistentQueueX<T> set) {
        Objects.requireNonNull(set);
        return AnyMFactory.instance.seq(set, Witness.persistentQueueX.INSTANCE);
    }
    public static <T> AnyMSeq<orderedSetX,T> fromOrderedSetX(final OrderedSetX<T> set) {
        Objects.requireNonNull(set);
        return AnyMFactory.instance.seq(set, Witness.orderedSetX.INSTANCE);
    }
    public static <T> AnyMSeq<bagX,T> fromBagX(final BagX<T> set) {
        Objects.requireNonNull(set);
        return AnyMFactory.instance.seq(set, Witness.bagX.INSTANCE);
    }
    /**
     * Create an AnyM wrapping a Stream of the supplied data
     * 
     * @param streamData values to populate a Stream
     * @return AnyMSeq wrapping a Stream that encompasses the supplied Array
     */
    public static <T> AnyMSeq<stream,T> fromArray(final T... streamData) {
        return AnyMFactory.instance.seq(Stream.of(streamData),Witness.stream.INSTANCE);
    }

    /**
     * Create an AnyM wrapping a Stream of the supplied data
     * 
     * Identical to fromArray, exists as it may appear functionally more obvious to users than fromArray (which fits the convention)
     * 
     * @param streamData values to populate a Stream
     * @return  AnyMSeq wrapping a Stream that encompasses the supplied Array
     */
    public static <T> AnyMSeq<stream,T> streamOf(final T... streamData) {
        return AnyMFactory.instance.seq(Stream.of(streamData),Witness.stream.INSTANCE);
    }
    public static <T> AnyMSeq<list,T> listOf(final T... elements) {
        return AnyMFactory.instance.seq(ListX.of(elements),Witness.list.INSTANCE);
    }

    /**
     * Construct an AnyM that wraps a reactiveBuffer-streams Publisher. If there is no registered Comprehender for the supplied Publisher, this method
     *  will recover to convert the Publisher to a type that cyclops2-react can understand.
     *  
     *  <pre>
     *  {@code 
     *       AnyMSeq<Integer> flux = AnyM.fromPublisher(Flux.just(10,20,30));
     *       
     *       //with cyclops2-reactor
     *       //AnyM[Flux[Integer]]]
     *       
     *       //without cyclops2-reactor
     *       //AnyM[ReactiveSeq[Integer]]]
     *  }
     *  </pre>
     *  It is generally safer to define a Comprehender and use a non-converting call to generate the wrapped AnyM
     *       (e.g. Reactor.Flux in cyclops2-reactor for Pivotal Reactor Publishers)
     * 
     * @param publisher Publisher to wrap inside an AnyM
     * @return AnyMSeq that wraps a Publisher
     */
    public static <T> AnyMSeq<reactiveSeq,T> fromPublisher(final Publisher<T> publisher) {
        return AnyMFactory.instance.seq(Spouts.from(publisher),reactiveSeq.REACTIVE);
    }
    /**
     * Create an AnyM instance that wraps a Stream
     *
     * @param stream Stream to wrap
     * @return AnyM that wraps the provided Stream
     */
    public static <T> AnyMSeq<reactiveSeq,T> fromStream(final ReactiveSeq<T> stream) {
        Objects.requireNonNull(stream);
        if(stream instanceof ReactiveStreamX) {
            return AnyMFactory.instance.seq(stream, reactiveSeq.REACTIVE);
        }else{
            return AnyMFactory.instance.seq(stream, reactiveSeq.CO_REACTIVE);
        }

    }
    /**
     * Create an AnyM instance that wraps a FutureStream
     *
     * @param stream FutureStream to wrap
     * @return AnyM that wraps the provided Stream
     */
    public static <T> AnyMSeq<futureStream,T> fromFutureStream(final FutureStream<T> stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream, futureStream.INSTANCE,new StreamAdapter<futureStream>(
                    ()->stream.getSimpleReact().of(), t->stream.getSimpleReact().of(t), it->(Stream)stream.getSimpleReact().from((Iterator)it),Witness.futureStream.INSTANCE));



    }
    public static <W extends Witness.StreamWitness<W>,T> AnyMSeq<W,T> fromStream(final Stream<T> stream, W witness) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream,witness);

    }
    /**
     * Create an AnyM instance that wraps a Stream
     * 
     * @param stream Stream to wrap
     * @return AnyM that wraps the provided Stream
     */
    public static <T> AnyMSeq<stream,T> fromStream(final Stream<T> stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream,Witness.stream.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an IntStream
     * 
     * @param stream IntStream to wrap
     * @return AnyM that wraps the provided IntStream
     */
    public static AnyMSeq<stream,Integer> fromIntStream(final IntStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed(),Witness.stream.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an DoubleStream
     * 
     * @param stream DoubleStream to wrap
     * @return AnyM that wraps the provided DoubleStream
     */
    public static AnyMSeq<stream,Double> fromDoubleStream(final DoubleStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed(),Witness.stream.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an LongStream
     * 
     * @param stream LongStream to wrap
     * @return AnyM that wraps the provided LongStream
     */
    public static AnyMSeq<stream,Long> fromLongStream(final LongStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed(),Witness.stream.INSTANCE);
    }

    /**
     * Create an AnyM instance that wraps an Optional
     * 
     * @param opt Optional to wrap
     * @return AnyM that wraps the provided Optonal
     */
    public static <T> AnyMValue<optional,T> fromOptional(final Optional<T> opt) {
        Objects.requireNonNull(opt);
        return AnyMFactory.instance.value(opt, Witness.optional.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an OptionalDouble
     * 
     * @param optional Optional to wrap
     * @return AnyM that wraps the provided OptonalDouble
     */
    public static AnyMValue<optional,Double> fromOptionalDouble(final OptionalDouble optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.value(Optionals.optional(optional), Witness.optional.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an OptionalLong
     * 
     * @param optional OptionalLong to wrap
     * @return AnyM that wraps the provided OptonalLong
     */
    public static AnyMValue<optional,Long> fromOptionalLong(final OptionalLong optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.value(Optionals.optional(optional), Witness.optional.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an OptionalInt
     * 
     * @param optional OptionalInt to wrap
     * @return AnyM that wraps the provided OptonalInt
     */
    public static AnyMValue<optional,Integer> fromOptionalInt(final OptionalInt optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.value(Optionals.optional(optional), Witness.optional.INSTANCE);
    }

    /**
     * Create an AnyM instance that wraps a CompletableFuture
     * 
     * @param future CompletableFuture to wrap
     * @return AnyM that wraps the provided CompletableFuture
     */
    public static <T> AnyMValue<completableFuture,T> fromCompletableFuture(final CompletableFuture<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future, Witness.completableFuture.INSTANCE);

    }

    /**
     * Create an AnyMValue instance that wraps an Xor
     * 
     * @param xor Xor to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Xor
     */
    public static <ST,T> AnyMValue2<either,ST,T> fromLazyEither(final Either<ST, T> xor) {
        Objects.requireNonNull(xor);
        return AnyMFactory.instance.value2(xor, either.INSTANCE);
    }
    public static <ST,T> AnyMValue2<either,ST,T> right(final T p) {
        Objects.requireNonNull(p);
        return fromLazyEither(Either.right(p));
    }
    public static <ST,T> AnyMValue2<either,ST,T> left(final ST s) {
        Objects.requireNonNull(s);
        return fromLazyEither(Either.left(s));
    }

    /**
     * Create an AnyMValue instance that wraps an Either3
     *
     * @param xor Xor to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Either
     */
    public static <LT1,T> AnyMValue2<lazyEither,LT1,T> fromLazyEither(final LazyEither<LT1, T> xor) {
        Objects.requireNonNull(xor);
        return AnyMFactory.instance.value2(xor, lazyEither.INSTANCE);
    }
    public static <LT1,T> AnyMValue2<lazyEither,LT1,T> lazyRight(final T right) {
        Objects.requireNonNull(right);
        return fromLazyEither(LazyEither.right(right));
    }
    public static <LT1,T> AnyMValue2<lazyEither,LT1,T> lazyLeft(final LT1 left) {
        Objects.requireNonNull(left);
        return fromLazyEither(LazyEither.left(left));
    }
    /**
     * Create an AnyMValue instance that wraps an Either3
     *
     * @param xor Xor to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Either3
     */
    public static <LT1,LT2,T> AnyMValue<lazyEither3,T> fromEither3(final LazyEither3<LT1, LT2, T> xor) {
        Objects.requireNonNull(xor);
        return AnyMFactory.instance.value(xor, lazyEither3.INSTANCE);
    }
    /**
     * Create an AnyMValue instance that wraps an Either4
     *
     * @param xor Either4 to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Either4
     */
    public static <LT1,LT2,LT3,T> AnyMValue<lazyEither4,T> fromEither4(final LazyEither4<LT1, LT2, LT3, T> xor) {
        Objects.requireNonNull(xor);
        return AnyMFactory.instance.value(xor, lazyEither4.INSTANCE);
    }
    /**
     * Create an AnyMValue instance that wraps an Either4
     *
     * @param xor Either4 to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Either4
     */
    public static <LT1,LT2,LT3,LT4,T> AnyMValue<lazyEither5,T> fromEither5(final LazyEither5<LT1, LT2, LT3, LT4, T> xor) {
        Objects.requireNonNull(xor);
        return AnyMFactory.instance.value(xor, lazyEither5.INSTANCE);
    }
    /**
     * Create an AnyMValue instance that wraps a Try
     * 
     * @param trySomething to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Try
     */
    public static <T,X extends Throwable> AnyMValue2<tryType,X,T> fromTry(final Try<T, X> trySomething) {
        Objects.requireNonNull(trySomething);
        return AnyMFactory.instance.value2(trySomething, Witness.tryType.INSTANCE);
    }
    public static <T, X extends Throwable> AnyMValue2<tryType,X,T> success(final T trySomething) {
        Objects.requireNonNull(trySomething);
        return fromTry(Try.success(trySomething));
    }
    public static <T, X extends Throwable> AnyMValue2<tryType,X,T> success(final T trySomething,final Class<? extends Throwable>... classes) {
        Objects.requireNonNull(trySomething);
        return fromTry(Try.success(trySomething,classes));
    }
    public static <T, X extends Throwable> AnyMValue2<tryType,X,T> failure(final X trySomething) {
        Objects.requireNonNull(trySomething);
        return fromTry(Try.failure(trySomething));
    }

    /**
     *  Create an AnyMValue instance that wraps an Ior
     * 
     * @param ior to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Ior
     */
    public static <ST,T> AnyMValue2<ior,ST,T> fromIor(final Ior<ST, T> ior) {
        Objects.requireNonNull(ior);
        return AnyMFactory.instance.value2(ior, Witness.ior.INSTANCE);
    }


    /**
     * Create an AnyMValue instance that wraps an Eval
     * 
     * @param eval to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Eval
     */
    public static <T> AnyMValue<eval,T> fromEval(final Eval<T> eval) {
        Objects.requireNonNull(eval);
        return AnyMFactory.instance.value(eval, Witness.eval.INSTANCE);
    }

    public static <T> AnyMValue<eval,T> later(final Supplier<T> eval) {
       return fromEval(Eval.later(eval));
    }
    public static <T> AnyMValue<eval,T> always(final Supplier<T> eval) {
        return fromEval(Eval.always(eval));
    }
    public static <T> AnyMValue<eval,T> always(final T val) {
        return fromEval(Eval.now(val));
    }

    public static <W extends Witness.MonadicValueWitness<W>,T> AnyMValue<W,T> fromMonadicValue(final MonadicValue<T> eval,W witness) {
        Objects.requireNonNull(eval);
        if(eval.arity()==2)
            return AnyMFactory.instance.value2(eval,witness);
        return AnyMFactory.instance.value(eval, witness);

    }

    /**
     * Create an AnyMValue instance that wraps a Future
     * 
     * @param future to wrap inside an AnyM
     * @return AnyM instance that wraps the provided future
     */
    public static <T> AnyMValue<future,T> fromFuture(final Future<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future, Witness.future.INSTANCE);
    }
    public static <T> AnyMValue<future,T> futureOf(final Supplier<T> task, Executor executor) {
        return fromFuture(Future.of(task,executor));
    }
    public static <T> AnyMValue<completableFuture,T> completableFutureOf(final Supplier<T> task, Executor executor) {
        return fromCompletableFuture(CompletableFuture.supplyAsync(task,executor));
    }


    /**
     * Create an AnyMValue instance that wraps a {@link Maybe}
     * 
     * @param maybe to wrap inside an AnyM
     * @return instance that wraps the provided Maybe
     */
    public static <T> AnyMValue<maybe,T> fromMaybe(final Maybe<T> maybe) {
        Objects.requireNonNull(maybe);
        return AnyMFactory.instance.value(maybe, Witness.maybe.INSTANCE);
    }
    public static <T> AnyMValue<identity,T> fromIdentity(final Identity<T> value){
        Objects.requireNonNull(value);
        return AnyMFactory.instance.value(value, Witness.identity.INSTANCE);
    }

    public static <T> AnyMValue<identity,T> identity(final T value){
        Objects.requireNonNull(value);
        return AnyMFactory.instance.value(Identity.of(value), Witness.identity.INSTANCE);
    }

    /**
     * Create an AnyMValue that wraps the untyped monad
     * 
     * @param monad to wrap inside an AnyM
     * @return AnyMValue that wraps the supplied monad
    */
    public static <W extends WitnessType<W>,T> AnyMValue<W,T> ofValue(final Object monad, W witness) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.value(monad,witness);
    }
    public static <W extends WitnessType<W>,T2,T> AnyMValue2<W,T2,T> ofValue2(final Object monad, W witness) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.value2(monad,witness);
    }
    public static <W extends WitnessType<W>,T2,T> AnyMValue2<W,T2,T> ofValue2(final Object monad,FunctionalAdapter<?> adapter) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.value2(monad,adapter);
    }
    public static <W extends WitnessType<W>,T> AnyMValue<W,T> ofValue(final Object monad,FunctionalAdapter<?> adapter) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.value(monad,adapter);
    } 

    /**
     * Create an AnyMSeq that wraps the untyped monad
     * 
     * @param monad to wrap inside an AnyM
     * @return AnyMSeq that wraps the supplied monad
     */
    public static <W extends WitnessType<W>,T> AnyMSeq<W,T> ofSeq(final Object monad, W witness) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.seq(monad,witness);
    }

    /**
     * Generate an AnyM that wraps an Optional from the provided nullable object
     * 
     * @param nullable - Nullable object to generate an optional from
     * @return AnyM wrapping an Optional created with the supplied nullable
     */
    public static <T> AnyMValue<optional,T> ofNullable(final T nullable) {
        return AnyMFactory.instance.value(Optional.ofNullable(nullable),Witness.optional.INSTANCE);
    }

    public static <T> AnyMValue<maybe,T> maybeNullable(final T nullable) {
        return AnyMFactory.instance.value(Maybe.ofNullable(nullable),Witness.maybe.INSTANCE);
    }
    public static <T> AnyMValue<maybe,T> just(final T nullable) {
        return AnyMFactory.instance.value(Maybe.just(nullable),Witness.maybe.INSTANCE);
    }
    public static <T> AnyMValue<maybe,T> none() {
        return AnyMFactory.instance.value(Maybe.nothing(),Witness.maybe.INSTANCE);
    }

    /**
     * Take an iterable containing Streamables and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Streamable.of(1,2,3),Streamable.of(10,20,30));
     *     
     *     //List[AnyM[Streamable[1,2,3],Streamable[10,20,30]]]
     * }
     * 
     * @param anyM Iterable containing Streamables
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMSeq<streamable,T>> listFromStreamable(final Iterable<Streamable<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromStreamable(i))
                            .collect(ListX.listXCollector());
    }
    /**
     * Take an iterable containing Either3s and convert them into a List of AnyMs
     * e.g.
     * {@code
     *     List<AnyM<Integer>> anyMs = anyMList(Arrays.asList(Either4.lazyRight(1),Either4.lazyLeft(10));
     *
     *     //List[AnyM[Either4:lazyRight[1],Either4:lazyLeft[10]]]
     * }
     *
     * @param anyM Iterable containing Eithers
     * @return List of AnyMs
     */
    public static <ST, LT2, LT3,LT4,T> ListX<AnyMValue<lazyEither5,T>> listFromEither5(final Iterable<LazyEither5<ST, LT2, LT3, LT4, T>> anyM) {
        return ReactiveSeq.fromIterable(anyM)
                .map(e -> fromEither5(e))
                .toListX();
    }

    /**
     * Take an iterable containing Either3s and convert them into a List of AnyMs
     * e.g.
     * {@code
     *     List<AnyM<Integer>> anyMs = anyMList(Arrays.asList(Either4.lazyRight(1),Either4.lazyLeft(10));
     *
     *     //List[AnyM[Either4:lazyRight[1],Either4:lazyLeft[10]]]
     * }
     *
     * @param anyM Iterable containing Eithers
     * @return List of AnyMs
     */
    public static <ST, LT2, LT3,T> ListX<AnyMValue<lazyEither4,T>> listFromEither4(final Iterable<LazyEither4<ST, LT2, LT3, T>> anyM) {
        return ReactiveSeq.fromIterable(anyM)
                .map(e -> fromEither4(e))
                .toListX();
    }
    /**
     * Take an iterable containing Either3s and convert them into a List of AnyMs
     * e.g.
     * {@code
     *     List<AnyM<Integer>> anyMs = listFromEither3(Arrays.asList(Either3.lazyRight(1),Either3.lazyLeft(10));
     *
     *     //List[AnyM[Either3:lazyRight[1],Either3:lazyLeft[10]]]
     * }
     *
     * @param anyM Iterable containing Eithers
     * @return List of AnyMs
     */
    public static <ST, LT2, T> ListX<AnyMValue<lazyEither3,T>> listFromEither3(final Iterable<LazyEither3<ST, LT2, T>> anyM) {
        return ReactiveSeq.fromIterable(anyM)
                .map(e -> AnyM.fromEither3(e))
                .toListX();
    }
    /**
     * Take an iterable containing Either3s and convert them into a List of AnyMs
     * e.g.
     * {@code
     *     List<AnyM<Integer>> anyMs = anyMList(Arrays.asList(Either.lazyRight(1),Either.lazyLeft(10));
     *
     *     //List[AnyM[Either.lazyRight[1],Either:lazyLeft[10]]]
     * }
     *
     * @param anyM Iterable containing Eithers
     * @return List of AnyMs
     */
    public static <ST,  T> ListX<AnyMValue2<lazyEither,ST,T>> listFromEither(final Iterable<LazyEither<ST, T>> anyM) {
        Objects.requireNonNull(anyM);
        return ReactiveSeq.fromIterable(anyM)
                .map(e -> AnyM.fromLazyEither(e))
                .toListX();
    }
    /**
     * Take an iterable containing Streams and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStream(Arrays.asList(Stream.of(1,2,3),Stream.of(10,20,30));
     *     
     *     //List[AnyM[Stream[1,2,3],Stream[10,20,30]]]
     * }
     * 
     * @param anyM Iterable containing Streams
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMSeq<stream,T>> listFromStream(final Iterable<Stream<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromStream(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Optionals and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Optional.of(1),Optional.of(10));
     *     
     *     //List[AnyM[Optional[1],Optional[10]]]
     * }
     * 
     * @param anyM Iterable containing Optional
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<optional,T>> listFromOptional(final Iterable<Optional<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromOptional(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing CompletableFutures and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(CompletableFuture.completedFuture(1),CompletableFuture.supplyAsync(()->10));
     *     
     *     //List[AnyM[CompletableFuture[1],CompleteableFuture[10]]]
     * }
     * 
     * @param anyM Iterable containing CompletableFuture
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<completableFuture,T>> listFromCompletableFuture(final Iterable<CompletableFuture<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromCompletableFuture(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Streamables and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Streamable.of(1,2,3),Streamable.of(10,20,30));
     *     
     *     //List[AnyM[Streamable[1,2,3],Streamable[10,20,30]]]
     * }
     * 
     * @param fromEither5 Iterable containing Streamables
     * @return List of AnyMs
     
    public static <T> ListX<AnyMSeq<T>> listFromIterable(final Iterable<Iterable<T>> fromEither5) {
        return StreamSupport.reactiveStream(fromEither5.spliterator(), false)
                            .transform(i -> AnyM.fromIterable(i))
                            .collect(ListX.listXCollector());
    }
*/
    /**
     * Take an iterable containing Xors and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Xor.lazyRight(1),Xor.lazyLeft(10));
     *     
     *     //List[AnyM[Xor:lazyRight[1],Xor:secondaary[10]]]
     * }
     * 
     * @param anyM Iterable containing Xors
     * @return List of AnyMs
     */
    public static <ST, T> ListX<AnyMValue2<either,ST,T>> listFromXor(final Iterable<Either<ST, T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromLazyEither(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Iors and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Ior.lazyRight(1),Ior.lazyLeft(10));
     *     
     *     //List[AnyM[Ior:lazyRight[1],Ior:secondaary[10]]]
     * }
     * 
     * @param anyM Iterable containing Iors
     * @return List of AnyMs
     */
    public static <ST, T> ListX<AnyMValue2<ior,ST,T>> listFromIor(final Iterable<Ior<ST, T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromIor(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Maybes and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Maybe.just(1),Maybe.just(10));
     *     
     *     //List[AnyM[Maybe[1],Maybe[10]]]
     * }
     * 
     * @param anyM Iterable containing Maybes
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<maybe,T>> listFromMaybe(final Iterable<Maybe<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromMaybe(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Evals and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Eval.now(1),Eval.now(10));
     *     
     *     //List[AnyM[Eval[1],Eval[10]]]
     * }
     * 
     * @param anyM Iterable containing Maybes
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<eval,T>> listFromEval(final Iterable<Eval<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromEval(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Future and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Future.ofResult(1),Future.ofResult(10));
     *     
     *     //List[AnyM[Future[1],Future[10]]]
     * }
     * 
     * @param anyM Iterable containing Maybes
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<future,T>> listFromFuture(final Iterable<Future<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromFuture(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Streamables and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Arrays.asList(1,2,3).iterator(),Arrays.asList(10,20,30)).iterator();
     *     
     *     //List[AnyM[Stream[1,2,3],Stream[10,20,30]]]
     * }
     * 
     * @param fromEither5 Iterable containing Iterators
     * @return List of AnyMs
     
    public static <T> ListX<AnyMSeq<T>> listFromIterator(final Iterable<Iterator<T>> fromEither5) {
        return StreamSupport.reactiveStream(fromEither5.spliterator(), false)
                            .transform(i -> AnyM.fromIterable(() -> i))
                            .collect(ListX.listXCollector());
    }*/

    /**
     * Convert a Collection of Monads to a Monad with a List
     * 
     * <pre>
     * {@code
        List<CompletableFuture<Integer>> futures = createFutures();
        AnyM<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));
    
       //where AnyM wraps  CompletableFuture<List<Integer>>
      }</pre>
     * 
     * 
     * @param seq Collection of monads to convert
     * @return Monad with a List
     */
    public static <W extends WitnessType<W>,T1> AnyM<W,ListX<T1>> sequence(final Collection<? extends AnyM<W,T1>> seq,W w) {
        return sequence(seq.stream(),w).map(s->ReactiveSeq.fromStream(s).to().listX(LAZY));
    }

    /**
     * Convert a Collection of Monads to a Monad with a List applying the supplied function in the process
     * 
     * <pre>
     * {@code 
       List<CompletableFuture<Integer>> futures = createFutures();
       AnyM<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
        </pre>
     * 
     * @param seq Collection of Monads
     * @param fn Function to applyHKT
     * @return Monad with a list
     */
    public static <W extends WitnessType<W>,T, R> AnyM<W,ListX<R>> traverse(final Collection<? extends AnyM<W,T>> seq, final Function<? super T, ? extends R> fn,W w) {
        return sequence(seq,w).map(l->l.map(fn));
    }



    static class AnyMFactory {
        static AnyMFactory instance = new AnyMFactory();

        public <W extends WitnessType<W>,T> AnyMValue<W,T> value(final Object o,FunctionalAdapter<?> adapter) {
            if (o instanceof AnyMValue)
                return (AnyMValue<W,T>) o;
            
            return new AnyMValueImpl<W,T>(
                                        o,(FunctionalAdapter)adapter);
        }
        public <W extends WitnessType<W>,T2,T> AnyMValue2<W,T2,T> value2(final Object o,FunctionalAdapter<?> adapter) {
            if (o instanceof AnyMValue)
                return (AnyMValue2<W,T2,T>) o;

            return new AnyMValue2Impl<W,T2,T>(
                    o,(FunctionalAdapter)adapter);
        }

        /**
         * Non-type safe way to wrap a supported monad type in an AnyMValue
         * 
         * @param o Monad to wrap
         * @return AnyMValue wrapping supplied monad
         */
        public <W extends WitnessType<W>,T> AnyMValue<W,T> value(final Object o,W comp) {
            if (o instanceof AnyMValue)
                return (AnyMValue<W,T>) o;
            
            return new AnyMValueImpl<W,T>(
                                      o,comp.adapter());
        }
        public <W extends WitnessType<W>,T,T2> AnyMValue2<W,T2,T> value2(final Object o,W comp) {
            if (o instanceof AnyMValue)
                return (AnyMValue2<W,T2,T>) o;

            return new AnyMValue2Impl<W,T2,T>(
                    o,comp.adapter());
        }

        /**
         * Non-type safe way to wrap a supported monad type in an AnyMSeq
         * 
         * @param o Monad to wrap
         * @return AnyMValue wrapping supplied monad
         */
        public <W extends WitnessType<W>,T> AnyMSeq<W,T> seq(final Object o, WitnessType comp) {
            if (o instanceof AnyMSeq)
                return (AnyMSeq<W,T>) o;
            return new AnyMSeqImpl<W,T>(o,comp.adapter());
        }
        public <W extends WitnessType<W>,T> AnyMSeq<W,T> seq(final Object o, WitnessType comp, FunctionalAdapter adapter) {
            if (o instanceof AnyMSeq)
                return (AnyMSeq<W,T>) o;
            return new AnyMSeqImpl<W,T>(o,comp.adapter());
        }

    }
    public static  <W extends WitnessType<W>,T> AnyM<W,Stream<T>> sequence(Stream<? extends AnyM<W,T>> stream, W witness) {
        FunctionalAdapter<W> c = witness.adapter();
        AnyM<W,Stream<T>> identity = c.unit(ReactiveSeq.empty());
       
        BiFunction<AnyM<W,Stream<T>>,AnyM<W,T>,AnyM<W,Stream<T>>> combineToStream = (acc,next) -> c.ap2(c.unit(Lambda.l2((Stream<T> a)->(T b)->ReactiveSeq.concat(a,ReactiveSeq.of(b)))),acc,next);

        BinaryOperator<AnyM<W,Stream<T>>> combineStreams = (a,b)-> (AnyM<W,Stream<T>>)a.zip(b,(z1,z2)->(Stream<T>)ReactiveSeq.concat(z1,z2)); // a.applyHKT(b, (s1,s2)->s1);

        return stream.reduce(identity,combineToStream,combineStreams);
    }
    public static  <W extends WitnessType<W>,T,R> AnyM<W,Stream<R>> traverse(Function<T,R> fn,Stream<AnyM<W,T>> stream, W witness) {
       return sequence(stream.map(h->h.map(fn)),witness);
    }
    FunctionalAdapter<W> adapter();

    public static <W extends WitnessType<W>,T> AnyM<W, T> narrow(AnyM<W, ? extends T> anyM){
        return (AnyM<W,T>)anyM;
    }
    
    /**
   * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
   * AnyM view simplifies type related challenges.
   * 
   * @param fn
   * @return
   */
  public static <W extends WitnessType<W>,U, R> AnyMFunction1<W,U,R> liftF(final Function<? super U, ? extends R> fn) {
      return u -> u.map(input -> fn.apply(input));
  }

  /**
   * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
   * AnyM view simplifies type related challenges. The actual native type is not specified here.
   * 
   * e.g.
   * 
   * <pre>{@code
   *  BiFunction<AnyM<Integer>,AnyM<Integer>,AnyM<Integer>> add = Monads.liftF2(this::add);
   *   
   *  Optional<Integer> result = add.applyHKT(getBase(),getIncrease());
   *  
   *   private Integer add(Integer a, Integer b){
              return a+b;
      }
   * }</pre>
   * The add method has no null handling, but we can lift the method to Monadic form, and use Optionals to automatically handle null / zero value cases.
   * 
   * 
   * @param fn BiFunction to lift
   * @return Lifted BiFunction
   */
  public static <W extends WitnessType<W>,U1, U2, R> AnyMFunction2<W,U1,U2,R> liftF2(
          final BiFunction<? super U1, ? super U2, ? extends R> fn) {

      return (u1, u2) -> u1.flatMapA(input1 -> u2.map(input2 -> fn.apply(input1, input2)));
  }


  /**
   * Lift a TriFunction into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
   * 
   * <pre>
   * {@code
   * TriFunction<AnyM<Double>,AnyM<Entity>,AnyM<W,String>,AnyM<Integer>> fn = liftF3(this::myMethod);
   *    
   * }
   * </pre>
   * 
   * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
   * 
   * @param fn Function to lift
   * @return Lifted function
   */
  public static <W extends WitnessType<W>,U1, U2, U3, R> Function3<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,R>> liftF3(
          final Function3<? super U1, ? super U2, ? super U3, ? extends R> fn) {
      return (u1, u2, u3) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3))));
  }


  /**
   * Lift a QuadFunction into Monadic form.
   * 
   * @param fn Quad funciton to lift
   * @return Lifted Quad function
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, R> Function4<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,U4>, AnyM<W,R>> liftF4(
          final Function4<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

      return (u1, u2, u3, u4) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4)))));
  }

  /**
   * Lift a  jOOλ Function5 (5 parameters) into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted Function
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, U5, R> Function5<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,U4>, AnyM<W,U5>, AnyM<W,R>> liftF5(
          final Function5<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

      return (u1, u2, u3, u4,
              u5) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.flatMapA(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                       input4, input5))))));
  }

  

  /**
   * Lift a Curried Function {@code(2 levels a->b->fn.applyHKT(a,b) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, AnyM<W,R>>> liftF2(final Function<U1, Function<U2, R>> fn) {
      return u1 -> u2 -> u1.flatMapA(input1 -> u2.map(input2 -> fn.apply(input1)
                                                              .apply(input2)));

  }

  /**
   * Lift a Curried Function {@code(3 levels a->b->c->fn.applyHKT(a,b,c) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, AnyM<W,R>>>> liftF3(
          final Function<? super U1, Function<? super U2, Function<? super U3, ? extends R>>> fn) {
      return u1 -> u2 -> u3 -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.map(input3 -> fn.apply(input1)
                                                                                      .apply(input2)
                                                                                      .apply(input3))));
  }

  /**
   * Lift a Curried Function {@code(4 levels a->b->c->d->fn.applyHKT(a,b,c,d) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, Function<AnyM<W,U4>, AnyM<W,R>>>>> liftF4(
          final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, ? extends R>>>> fn) {

      return u1 -> u2 -> u3 -> u4 -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.map(input4 -> fn.apply(input1)
                                                                                                              .apply(input2)
                                                                                                              .apply(input3)
                                                                                                              .apply(input4)))));
  }

  /**
   * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.applyHKT(a,b,c,d,e) ) }into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, U5, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, Function<AnyM<W,U4>, Function<AnyM<W,U5>, AnyM<W,R>>>>>> liftF5(
          final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, Function<? super U5, ? extends R>>>>> fn) {

      return u1 -> u2 -> u3 -> u4 -> u5 -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.flatMapA(input4 -> u5.map(input5 -> fn.apply(input1)
                                                                                                                                      .apply(input2)
                                                                                                                                      .apply(input3)
                                                                                                                                      .apply(input4)
                                                                                                                                      .apply(input5))))));
  }

    default FutureT<W, T> liftMFuture(Function<? super T, ? extends Future<T>> lift) {

        return FutureT.of(this.map(a -> lift.apply(a)));
    }

    default ListT<W, T> liftMList(Function<? super T, ? extends IndexedSequenceX<T>> lift) {
        return ListT.of(this.map(a -> lift.apply(a)));
    }

    default FutureT<W, T> liftMFuture() {
        return FutureT.of(this.map(a -> Future.ofResult(a)));
    }

    default ListT<W, T> liftMListX() {
        return ListT.of(this.map(a -> ListX.of(a)));
    }

    @Override
    default <U> AnyM<W,U> ofType(final Class<? extends U> type) {
        return (AnyM<W,U>)Filters.super.ofType(type);
    }

    @Override
    default AnyM<W,T> filterNot(final Predicate<? super T> predicate) {
        return (AnyM<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    default AnyM<W,T> notNull() {
        return (AnyM<W,T>)Filters.super.notNull();
    }

    @Override
    default AnyM<W,T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return (AnyM<W,T>)Zippable.super.zip(combiner,app);
    }

    @Override
    default <T2, R> AnyM<W,R> zip(final Iterable<? extends T2> iterable, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (AnyM<W,R>)Zippable.super.zip(iterable,fn);
    }

    @Override
    default <T2, R> AnyM<W,R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (AnyM<W,R>)Zippable.super.zipP(publisher,fn);
    }

    @Override
    default <U, R> AnyM<W,R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (AnyM<W,R>)Zippable.super.zipS(other,zipper);
    }

    @Override
    default <U> AnyM<W,Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        return (AnyM)Zippable.super.zipS(other);
    }

    @Override
    default <U> AnyM<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (AnyM)Zippable.super.zip(other);
    }

    @Override
    default <U> AnyM<W,U> cast(final Class<? extends U> type) {
        return (AnyM<W,U>)Zippable.super.cast(type);
    }

    @Override
    default <R> AnyM<W,R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (AnyM<W, R>) Zippable.super.trampoline(mapper);
    }
}