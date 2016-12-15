package com.aol.cyclops.control;


import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.*;

import com.aol.cyclops.util.function.Lambda;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.internal.stream.spliterators.FillSpliterator;
import com.aol.cyclops.internal.stream.spliterators.LazySingleSpliterator;
import com.aol.cyclops.internal.stream.spliterators.PushingSpliterator;
import com.aol.cyclops.internal.stream.spliterators.ReversingArraySpliterator;
import com.aol.cyclops.internal.stream.spliterators.ReversingListSpliterator;
import com.aol.cyclops.internal.stream.spliterators.ReversingRangeIntSpliterator;
import com.aol.cyclops.internal.stream.spliterators.ReversingRangeLongSpliterator;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.OnEmptySwitch;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.applicative.zipping.ApplyingZippingApplicativeBuilder;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.PausableHotStream;
import com.aol.cyclops.types.stream.reactive.ReactiveSubscriber;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;
import com.aol.cyclops.util.ExceptionSoftener;
import com.aol.cyclops.util.function.F4;
import com.aol.cyclops.util.function.F3;

import lombok.val;

/**
 * A powerful extended, sequential Stream type.
 * Extends JDK 8 java.util.stream.Stream.
 * Extends org.jooq.lambda.Seq.
 * Implements the reactive-stream api.
 * 
 * Features include
 *      Asynchronous execution
 *      Scheduling
 *      Error handling
 *      Retries
 *      Zipping
 *      Duplication
 *      Cartesian product operations (e.g. crossJoin, forEach2)
 *      Subscriptions and fined grained control
 *      Interoperability
 *      Parallelism via LazyFutureStream
 *      Lazy grouping (group by size, time, state)
 *      Sliding windows
 *      
 *      Efficient reversal
 *      foldRight / scanLeft / scanRight
 *      Zipping and Combining
 *      Data insertion and removal
 *      Time based operations (debouncing, onePer, xPer)
 *      SQL style Window operations
 *      Reduction and partial reduction
 *      Mathematical terminal operations
 *      Lazy execution
 *      Empty handling
 *      Cycling / repeating
 *      Controlled iteration (forEachX)
 *      Event handling (on next, on error, on complete)
 *      
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements within the Stream
 */
public interface ReactiveSeq<T> extends To<ReactiveSeq<T>>,
                                        Unwrapable, 
                                        Stream<T>, 
                                        OnEmptySwitch<T, Stream<T>>,
                                        ExtendedTraversable<T>,
                                        ZippingApplicativable<T>,
                                        Unit<T>,
                                        ConvertableSequence<T> {



    <A,R> ReactiveSeq<R> collectSeq(Collector<? super T,A,R> c);



    ReactiveSeq<T> fold(Monoid<T> monoid);
    
    public static  <T> ReactiveSubscriber<T> pushable(){
        return new ReactiveSubscriber<>();
    }
    /**
     * Construct a ReactiveSeq from a String
     * 
     * @param input String to construct ReactiveSeq from
     * @return ReactiveSeq from a String
     */
    public static  ReactiveSeq<Integer> fromString(String input){
        return fromSpliterator(input.chars().spliterator());
    }
    
    /**
     * @param values ints to populate Stream from
     * @return ReactiveSeq of multiple Integers
     */
    public static ReactiveSeq<Integer> ofInts(int... values){
        return fromSpliterator(IntStream.of(values).spliterator());
    }
   
    /**
     * Efficiently construct a ReactiveSeq from an int (will stored an processed as a primitive where possible).
     * 
     * @param value Value to construct ReactiveSeq from
     * @return ReactiveSeq of one Integer
     */
    public static ReactiveSeq<Integer> ofInts(int value){
        return fromSpliterator(IntStream.of(value).spliterator());
    }
    /**
     * 
     * @param values longs to populate Stream from
     * @return ReactiveSeq of multiple Longs
     */
    public static ReactiveSeq<Long> ofLongs(long... values){
        return fromSpliterator(LongStream.of(values).spliterator());
    }
    /**
     * Efficiently construct a ReactiveSeq from an long (will stored an processed as a primitive where possible).
     * 
     * @param value Value to construct ReactiveSeq from
     * @return ReactiveSeq of one Long
     */
    public static ReactiveSeq<Long> ofLongs(long value){
        return fromSpliterator(LongStream.of(value).spliterator());
    }
    /**
     * 
     * @param values longs to populate Stream from
     * @return ReactiveSeq of multiple Longs
     */
    public static ReactiveSeq<Double> ofDoubles(double... values){
        return fromSpliterator(DoubleStream.of(values).spliterator());
    }
    /**
     * Efficiently construct a ReactiveSeq from an long (will stored an processed as a primitive where possible).
     * 
     * @param value Value to construct ReactiveSeq from
     * @return ReactiveSeq of one Long
     */
    public static ReactiveSeq<Double> ofDouble(double value){
        return fromSpliterator(DoubleStream.of(value).spliterator());
    }
    /**
     * Efficiently construct a ReactiveSeq from a single value
     * 
     * @param value Value to construct ReactiveSeq from
     * @return ReactiveSeq of one value
     */
    public static <T> ReactiveSeq<T> of(T value){
        return fromStream(Stream.of(value));
    }
    /**
     * Construct a ReactiveSeq from the Supplied Spliterator
     * 
     * @param spliterator Spliterator to construct a Stream from
     * @return ReactiveSeq created from Spliterator
     */
    public static <T> ReactiveSeq<T> fromSpliterator(Spliterator<T> spliterator){
        return fromStream(StreamSupport.stream(spliterator, false));
    }
    public static <T> ReactiveSeq<T> fromSpliterator(PushingSpliterator<T> spliterator){
        return StreamUtils.reactiveSeq(StreamSupport.stream(spliterator, false), Optional.empty(),Optional.of(spliterator));
        
    }
   
    /**
     * Peform intermediate operations on a primitive IntStream (gives improved performance when working with Integers)
     * If this ReactiveSeq has an OfInt Spliterator it will be converted directly to an IntStream,
     * otherwise the provided conversion function will be used.
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.range(1, 1000)
     *            .ints(i->i,s->s.map(i->i*2).filter(i->i<500))
                  .size(),
       //249
     * 
     * </pre>
     * 
     * 
     * @param fn
     * @param mapper
     * @return
     */
    default ReactiveSeq<Integer> ints(ToIntFunction<? super T> fn,Function<? super IntStream, ? extends IntStream> mapper){
        return ReactiveSeq.fromSpliterator(foldInt(fn,mapper).spliterator());
    }
    default <R> ReactiveSeq<R> jooλ(Function<? super Seq<T>, ? extends Seq<R>> mapper){
        return ReactiveSeq.fromSpliterator(foldJooλ(mapper).spliterator());
    }
    default <R> R foldJooλ(Function<? super Seq<T>, ? extends R> mapper){
        Spliterator<T> split = this.spliterator();
        return mapper.apply(Seq.seq((Stream<T>)this));
    }
    /**
     * Perform a fold on a primitive IntStream (this is much faster than working with Integer Objects).
     * If this ReactiveSeq has an OfInt Spliterator it will be converted directly to an IntStream,
     * otherwise the provided conversion function will be used.
     * 
     * <pre>
     * {@code 
     *     ReactiveSeq.range(1, 1000)
     *                .foldInt(i->i,s->s.map(i->i*2)
     *                                  .filter(i->i<500)
     *                                  .average());
     *                                  
     *     //OptionalDouble[250.0] 
     * }
     * </pre>
     * 
     * 
     * @param fn Conversion function
     * @param mapper Folding function
     * @return Fold result
     */
    default <R> R foldInt(ToIntFunction<? super T> fn,Function<? super IntStream, ? extends R> mapper){
        Spliterator<T> split = this.spliterator();
        IntStream s = (split instanceof Spliterator.OfInt)? StreamSupport.intStream((Spliterator.OfInt)split,false) : StreamSupport.stream(split,false).mapToInt(fn);
        return mapper.apply(s);
    }
    /**
     * Peform intermediate operations on a primitive IntStream (gives improved performance when working with Integers)
     * If this ReactiveSeq has an OfInt Spliterator it will be converted directly to an IntStream,
     * otherwise the provided conversion function will be used.
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.range(1, 1000)
     *            .longs(i->i.longValue(),s->s.map(i->i*2).filter(i->i<500))
                  .size(),
       //249
     * 
     * </pre>
     * 
     * 
     * @param fn
     * @param mapper
     * @return
     */
    default ReactiveSeq<Long> longs(ToLongFunction<? super T> fn,Function<? super LongStream, ? extends LongStream> mapper){
        return ReactiveSeq.fromSpliterator(foldLong(fn,mapper).spliterator());
    }
    /**
     * Perform a fold on a primitive LongStream (this is much faster than working with Long Objects).
     * If this ReactiveSeq has an OfLong Spliterator it will be converted directly to an LongStream,
     * otherwise the provided conversion function will be used.
     * 
     * <pre>
     * {@code 
     *     ReactiveSeq.range(1, 1000)
     *                .foldLong(i->i.longValue(),s->s.map(i->i*2)
     *                                  .filter(i->i<500)
     *                                  .average());
     *                                  
     *     //OptionalDouble[250.0] 
     * }
     * </pre>
     * 
     * 
     * @param fn Conversion function
     * @param mapper Folding function
     * @return Fold result
     */
    default <R> R foldLong(ToLongFunction<? super T> fn,Function<? super LongStream, ? extends R> mapper){
        Spliterator<T> split = this.spliterator();
        LongStream s = (split instanceof Spliterator.OfLong)? StreamSupport.longStream((Spliterator.OfLong)split,false) : StreamSupport.stream(split,false).mapToLong(fn);
        return mapper.apply(s);
    }
    /**
     * Peform intermediate operations on a primitive IntStream (gives improved performance when working with Integers)
     * If this ReactiveSeq has an OfInt Spliterator it will be converted directly to an IntStream,
     * otherwise the provided conversion function will be used.
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.range(1, 1000)
     *            .doubles(i->i.doubleValue(),s->s.map(i->i*2).filter(i->i<500))
                  .size(),
       //249
     * 
     * </pre>
     * 
     * 
     * @param fn
     * @param mapper
     * @return
     */
    default ReactiveSeq<Double> doubles(ToDoubleFunction<? super T> fn,Function<? super DoubleStream, ? extends DoubleStream> mapper){
        return ReactiveSeq.fromSpliterator(foldDouble(fn,mapper).spliterator());
    }
    /**
     * Perform a fold on a primitive DoubleStream (this is much faster than working with Double Objects).
     * If this ReactiveSeq has an OfDouble Spliterator it will be converted directly to an DoubleStream,
     * otherwise the provided conversion function will be used.
     * 
     * <pre>
     * {@code 
     *     ReactiveSeq.range(1, 1000)
     *                .foldDouble(i->i.doubleValue(),s->s.map(i->i*2)
     *                                                   .filter(i->i<500)
     *                                                  .average());
     *                                  
     *     //OptionalDouble[250.0] 
     * }
     * </pre>
     * 
     * 
     * @param fn Conversion function
     * @param mapper Folding function
     * @return Fold result
     */
    default <R> R foldDouble(ToDoubleFunction<? super T> fn,Function<? super DoubleStream, ? extends R> mapper){
        Spliterator<T> split = this.spliterator();
        DoubleStream s = (split instanceof Spliterator.OfDouble) ? StreamSupport.doubleStream((Spliterator.OfDouble)split,false) : StreamSupport.stream(split,false).mapToDouble(fn);
        return mapper.apply(s);
    }
    
    /**
     * Construct a Stream consisting of a single value repeatedly infinitely (use take / drop etc to
     * switch to a finite Stream)
     * 
     * @param t Value to fill Stream with
     * @return Infinite ReactiveSeq consisting of a single value
     */
    public static <T> ReactiveSeq<T> fill(T t){
        return ReactiveSeq.fromStream(StreamSupport.stream(new FillSpliterator<T>(t), false));
    }
    /**
     * coflatMap pattern, can be used to perform lazy reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *      ReactiveSeq.of(1,2,3)
     *                 .map(i->i*2)
     *                 .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *      //ReactiveSeq[12]
     * }
     * </pre>
     * 
     * 
     * @param fn
     * @return
     */
    default <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn){
        return ReactiveSeq.fromStream(StreamSupport.<R>stream(new LazySingleSpliterator<T,ReactiveSeq<T>,R>(this,fn), false));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    public <T> ReactiveSeq<T> unitIterator(Iterator<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> ReactiveSeq<T> unit(T unit);

   
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#foldRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> U foldRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> accumulator);

    /* (non-Javadoc)
    * @see org.jooq.lambda.Seq#printOut()
    */
    @Override
    default void printOut() {
        Seq.seq((Stream)this).printOut();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.zipping.ZippingApplicativable#applicatives()
     */
    @Override
    default <R> ApplyingZippingApplicativeBuilder<T, R, ZippingApplicativable<R>> applicatives() {
        final Streamable<T> streamable = toStreamable();
        return new ApplyingZippingApplicativeBuilder<T, R, ZippingApplicativable<R>>(
                                                                                     streamable, streamable);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.zipping.ZippingApplicativable#ap1(java.util.function.Function)
     */
    @Override
    default <R> ZippingApplicativable<R> ap1(final Function<? super T, ? extends R> fn) {
        val dup = this.duplicateSequence();
        final Streamable<T> streamable = dup.v1.toStreamable();
        return new ApplyingZippingApplicativeBuilder<T, R, ZippingApplicativable<R>>(
                                                                                     streamable, streamable).applicative(fn)
                                                                                                            .ap(dup.v2);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> ReactiveSeq<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return ReactiveSeq.fromStream(Seq.zip(this, other, zipper));
    }

    @Override
    default <U, R> ReactiveSeq<R> zipP(final Publisher<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return zip((Iterable<? extends U>) ReactiveSeq.fromPublisher(other), zipper);
    }
 

    @Override
    default <U, R> ReactiveSeq<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return zip((Iterable<? extends U>) ReactiveSeq.fromStream(other), zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unwrapable#unwrap()
     */
    @Override
    <R> R unwrap();

    /**
     * join / flatten one level of a nested hierarchy
     * 
     * <pre>
     * {@code 
     *  ReactiveSeq.of(Arrays.asList(1,2))
     *             .to(ReactiveSeq::flatten));
     *  
     *  //stream of (1,  2);		
     *  
     * 
     * 
     * }
     * 
     * </pre>
     * 
     * @return Flattened / joined one level
     */
    static <T1> ReactiveSeq<T1> flatten(ReactiveSeq<ReactiveSeq<T1>> nested){
        return nested.flatMap(Function.identity());
    }

    /**
     * Convert to a Stream with the values repeated specified times
     * 
     * <pre>
     * {@code 
     * 		ReactiveSeq.of(1,2,2)
     * 								.cycle(3)
     * 								.collect(Collectors.toList());
     * 								
     * 		//List[1,2,2,1,2,2,1,2,2]
     * 
     * }
     * </pre>
     * 
     * @param times
     *            Times values should be repeated within a Stream
     * @return Stream with values repeated
     */
    ReactiveSeq<T> cycle(int times);

    /**
     * Convert to a Stream with the values infinitely cycled
     * 
     * <pre>
     * {@code 
     *      ReactiveSeq.of(1).cycle().limit(6).toList());
     *      //List[1, 1, 1, 1, 1,1]
     *   }
     * </pre>
     * 
     * @return Stream with values repeated
     */
    ReactiveSeq<T> cycle();

    /**
     * Duplicate a Stream, buffers intermediate values, leaders may change
     * positions so a limit can be safely applied to the leading stream. Not
     * thread-safe.
     * 
     * <pre>
     * {@code
     * 
     * 	Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies = of(1, 2, 3, 4, 5, 6)
     *                                                                  .duplicate();
     * 	assertTrue(copies.v1.anyMatch(i > i == 2));
     * 	assertTrue(copies.v2.anyMatch(i > i == 2));
     * 
     * }
     * </pre>
     * 
     * @return duplicated stream
     */
    Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicateSequence();

    /**
     * Triplicates a Stream Buffers intermediate values, leaders may change
     * positions so a limit can be safely applied to the leading stream. Not
     * thread-safe.
     * 
     * <pre>
     * {@code
     * 	Tuple3<ReactiveSeq<Tuple3<T1, T2, T3>>, ReactiveSeq<Tuple3<T1, T2, T3>>, ReactiveSeq<Tuple3<T1, T2, T3>>> Tuple3 = sequence.triplicate();
     * 
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate();

    /**
     * Makes four copies of a Stream Buffers intermediate values, leaders may
     * change positions so a limit can be safely applied to the leading stream.
     * Not thread-safe.
     * 
     * <pre>
     * {@code
     * 	Tuple4<ReactiveSeq<Tuple4<T1, T2, T3, T4>>, 
     *          ReactiveSeq<Tuple4<T1, T2, T3, T4>>, 
     *          ReactiveSeq<Tuple4<T1, T2, T3, T4>>, 
     *          ReactiveSeq<Tuple4<T1, T2, T3, T4>>> quad = sequence.quadruplicate();
     * 
     * }
     * </pre>
     * 
     * @return Tuple4 containing 4 duplicated ReactiveSeqs
     */
    @SuppressWarnings("unchecked")
    Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate();

    /**
     * Split a Stream at it's head (similar to headAndTail)
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3)
     *            .splitAtHead()
     * 
     *  //Optional[1], ReactiveSeq[2,3]
     * }
     * 
     * </pre>
     * 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    Tuple2<Optional<T>, ReactiveSeq<T>> splitSequenceAtHead();

    /**
     * Split at supplied location
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3)
     *            .splitAt(1)
     * 
     *  //ReactiveSeq[1], ReactiveSeq[2,3]
     * }
     * 
     * </pre>
     */
    Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(int where);

    /**
     * Split stream at point where predicate no longer holds
     * 
     * <pre>
     * {@code
     *   ReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
     *   
     *   //ReactiveSeq[1,2,3] ReactiveSeq[4,5,6]
     * }
     * </pre>
     */
    Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(Predicate<T> splitter);

    /**
     * Partition a Stream into two one a per element basis, based on predicate's
     * boolean value
     * 
     * <pre>
     * {@code 
     *  ReactiveSeq.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0) 
     *  
     *  //ReactiveSeq[1,3,5], ReactiveSeq[2,4,6]
     * }
     *
     * </pre>
     */
    Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partitionSequence(Predicate<T> splitter);

    /**
     * Convert to a Stream with the result of a reduction operation repeated
     * specified times
     * 
     * <pre>
     * {@code 
     *   List<Integer> list = ReactiveSeq.of(1,2,2))
     *                                 .cycle(Reducers.toCountInt(),3)
     *                                 .collect(Collectors.toList());
     *   //List[3,3,3];
     *   }
     * </pre>
     * 
     * @param m
     *            Monoid to be used in reduction
     * @param times
     *            Number of times value should be repeated
     * @return Stream with reduced values repeated
     */
    @Override
    ReactiveSeq<T> cycle(Monoid<T> m, int times);

    /**
     * Repeat in a Stream while specified predicate holds
     * 
     * <pre>
     * {@code
     * 	
     * 	MutableInt count = MutableInt.of(0);
     * 	ReactiveSeq.of(1, 2, 2).cycleWhile(next -> count++ < 6)
     *             .collect(Collectors.toList());
     * 
     * 	// List(1,2,2,1,2,2)
     * }
     * </pre>
     * 
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    @Override
    ReactiveSeq<T> cycleWhile(Predicate<? super T> predicate);

    /**
     * Repeat in a Stream until specified predicate holds
     * 
     * <pre>
     * {@code 
     * 	MutableInt count =MutableInt.of(0);
     * 		ReactiveSeq.of(1,2,2)
     * 		 		.cycleUntil(next -> count.get()>6)
     * 		 		.peek(i-> count.mutate(i->i+1))
     * 		 		.collect(Collectors.toList());
     * 
     * 		//List[1,2,2,1,2,2,1]	
     * }
     * 
     * 
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    @Override
    ReactiveSeq<T> cycleUntil(Predicate<? super T> predicate);

    /**
     * Zip 2 streams into one
     * 
     * <pre>
     * {@code 
     *  List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();
     *  // [[1,"a"],[2,"b"]]
     * }
     * </pre>
     * 
     */
    @Override
    default <U> ReactiveSeq<Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        return zip(Seq.seq(other));
    }
    
    

    @Override
    default <U> ReactiveSeq<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return zip(Seq.seq(other));
    }



    /**
     * zip 3 Streams into one
     * 
     * <pre>
     * {@code
     *  List<Tuple3<Integer, Integer, Character>> list = of(1, 2, 3, 4, 5, 6).zip3(of(100, 200, 300, 400), of('a', 'b', 'c')).collect(Collectors.toList());
     * 
     *  // [[1,100,'a'],[2,200,'b'],[3,300,'c']]
     * }
     * 
     * </pre>
     */
    @Override
    <S, U> ReactiveSeq<Tuple3<T, S, U>> zipS3(Iterable<? extends S> second, Iterable<? extends U> third);

    /**
     * zip 4 Streams into 1
     * 
     * <pre>
     * {@code
     *  List<Tuple4<Integer, Integer, Character, String>> list = of(1, 2, 3, 4, 5, 6).zip4(of(100, 200, 300, 400), of('a', 'b', 'c'), of("hello", "world"))
     *          .collect(Collectors.toList());
     * 
     * }
     * // [[1,100,'a',"hello"],[2,200,'b',"world"]]
     * </pre>
     */
    @Override
    <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zipS4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth);

    default Seq<T> seq(){
        return Seq.seq((Stream<T>)this);
    }
    /**
     * Add an index to the current Stream
     * 
     * <pre>
     * {@code 
     * assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
     * }
     * </pre>
     */
    @Override
    default ReactiveSeq<Tuple2<T, Long>> zipWithIndex() {
        return fromStream(seq().zipWithIndex());
    }

  

    /**
     * Create a sliding view over this Sequence
     * 
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());
     * 
     *  assertThat(list.get(0), hasItems(1, 2));
     *  assertThat(list.get(1), hasItems(2, 3));
     * 
     * }
     * 
     * </pre>
     * 
     * @param windowSize
     *            Size of sliding window
     * @return ReactiveSeq with sliding view
     */
    @Override
    ReactiveSeq<ListX<T>> sliding(int windowSize);

    /**
     * Create a sliding view over this Sequence
     * 
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());
     * 
     *  assertThat(list.get(0), hasItems(1, 2, 3));
     *  assertThat(list.get(1), hasItems(3, 4, 5));
     * 
     * }
     * 
     * </pre>
     * 
     * @param windowSize
     *            number of elements in each batch
     * @param increment
     *            for each window
     * @return ReactiveSeq with sliding view
     */
    @Override
    ReactiveSeq<ListX<T>> sliding(int windowSize, int increment);

    /**
     * Group elements in a Stream
     * 
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList());
     * 
     *  assertThat(list.get(0), hasItems(1, 2, 3));
     *  assertThat(list.get(1), hasItems(4, 5, 6));
     * 
     * }
     * </pre>
     * 
     * @param groupSize
     *            Size of each Group
     * @return Stream with elements grouped by size
     */
    @Override
    ReactiveSeq<ListX<T>> grouped(int groupSize);

    /**
     * Create ReactiveSeq of ListX where
     * each ListX is populated while the supplied bipredicate holds. The
     * bipredicate recieves the ListX from the last window as well as the
     * current value and can choose to aggregate the current value or create a
     * new window
     * 
     * <pre>
     * {@code 
     *    ReactiveSeq.of(1,2,3,4,5,6)
     *               .groupedStatefullyUntil((s,i)-> s.contains(4) ? true : false)
     *               .toList()
     *               .size()
     *     //5
     * }
     * </pre>
     * 
     * @param predicate
     *            Window while true
     * @return ReactiveSeq windowed while predicate holds
     */
    @Override
    ReactiveSeq<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate);

    /**
     * Batch elements by size into a List
     * 
     * <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedBySizeAndTime(3,10,TimeUnit.SECONDS)
     *              .toList();
     *          
     * //[[1,2,3],[4,5,6]] 
     * }
     * </pre>
     * 
     * @param size Max size of a batch
     * @param time (Max) time period to build a single batch in
     * @param t time unit for batch
     * @return ReactiveSeq batched by size and time
     */
    ReactiveSeq<ListX<T>> groupedBySizeAndTime(int size, long time, TimeUnit t);

    /**
     * Batch elements by size into a collection created by the supplied factory
     * 
     * <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedBySizeAndTime(3,10,TimeUnit.SECONDS,()->SetX.empty())
     *              .toList();
     *          
     * //[[1,2,3],[4,5,6]] 
     * }
     * </pre>
     * 
     * @param size
     *            Max size of a batch
     * @param time
     *            (Max) time period to build a single batch in
     * @param unit
     *            time unit for batch
     * @param factory
     *            Collection factory
     * @return ReactiveSeq batched by size and time
     */
    <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory);

    /**
     * Batch elements in a Stream by time period
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
     * }
     * </pre>
     * 
     * @param time
     *            - time period to build a single batch in
     * @param t
     *            time unit for batch
     * @return ReactiveSeq batched into lists by time period
     */
    ReactiveSeq<ListX<T>> groupedByTime(long time, TimeUnit t);

    /**
     * Batch elements by time into a collection created by the supplied factory
     * 
     * <pre>
     * {@code 
     *   assertThat(ReactiveSeq.of(1,1,1,1,1,1)
     *                       .batchByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>())
     *                       .toList()
     *                       .get(0)
     *                       .size(),is(1));
     * }
     * </pre>
     * 
     * @param time
     *            - time period to build a single batch in
     * @param unit
     *            time unit for batch
     * @param factory
     *            Collection factory
     * @return ReactiveSeq batched into collection types by time period
     */
    <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(long time, TimeUnit unit, Supplier<C> factory);

    /**
     * Batch elements in a Stream by size into a collection created by the
     * supplied factory
     * 
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,1,1,1,1,1)
     *                      .batchBySize(3,()->new TreeSet<>())
     *                      .toList()
     *                      .get(0)
     *                      .size(),is(1));
     * }
     * </pre>
     * @param size batch size
     * @param supplier Collection factory
     * @return ReactiveSeq batched into collection types by size
     */
    @Override
    <C extends Collection<? super T>> ReactiveSeq<C> grouped(int size, Supplier<C> supplier);

    /**
     * Create a ReactiveSeq batched by List, where each batch is populated until
     * the predicate holds
     * 
     * <pre>
     * {@code 
     *  assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .batchUntil(i->i%3==0)
     *              .toList()
     *              .size(),equalTo(2));
     * }
     * </pre>
     * 
     * @param predicate
     *            Batch until predicate holds, then open next batch
     * @return ReactiveSeq batched into lists determined by the predicate supplied
     */
    @Override
    ReactiveSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate);

    /**
     * Create a ReactiveSeq batched by List, where each batch is populated while
     * the predicate holds
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .batchWhile(i->i%3!=0)
     *              .toList().size(),equalTo(2));
     *  
     * }
     * </pre>
     * 
     * @param predicate
     *            Batch while predicate holds, then open next batch
     * @return ReactiveSeq batched into lists determined by the predicate supplied
     */
    @Override
    ReactiveSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate);

    /**
     * Create a ReactiveSeq batched by a Collection, where each batch is populated
     * while the predicate holds
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .batchWhile(i->i%3!=0)
     *              .toList()
     *              .size(),equalTo(2));
     * }
     * </pre>
     * 
     * @param predicate
     *            Batch while predicate holds, then open next batch
     * @param factory
     *            Collection factory
     * @return ReactiveSeq batched into collections determined by the predicate
     *         supplied
     */
    @Override
    <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory);

    /**
     * Create a ReactiveSeq batched by a Collection, where each batch is populated
     * until the predicate holds
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .batchUntil(i->i%3!=0)
     *              .toList()
     *              .size(),equalTo(2));
     * }
     * </pre>
     * 
     * 
     * @param predicate
     *            Batch until predicate holds, then open next batch
     * @param factory
     *            Collection factory
     * @return ReactiveSeq batched into collections determined by the predicate
     *         supplied
     */
    @Override
    <C extends Collection<? super T>> ReactiveSeq<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory);

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#grouped(java.util.function.Function, java.util.stream.Collector)

    @Override
    default <K, A, D> ReactiveSeq<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return fromStream(seq().grouped(classifier, downstream));
    }*/

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#grouped(java.util.function.Function)

    @Override
    default <K> ReactiveSeq<Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return fromStream(seq().grouped(classifier));
    } */

    /**
     * Use classifier function to group elements in this Sequence into a Map
     * 
     * <pre>
     * {@code 
     *  Map<Integer, List<Integer>> map1 = of(1, 2, 3, 4).groupBy(i -> i % 2);
     *  assertEquals(asList(2, 4), map1.get(0));
     *  assertEquals(asList(1, 3), map1.get(1));
     *  assertEquals(2, map1.size());
     * 
     * }
     * 
     * </pre>
     */
    @Override
    default <K> MapX<K, ListX<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return collect(Collectors.groupingBy(classifier, (Supplier)MapX::empty, ListX.<T>defaultCollector()));
    }

    /*
     * Return the distinct Stream of elements
     * 
     * <pre> {@code List<Integer> list = ReactiveSeq.of(1,2,2,2,5,6) .distinct()
     * .collect(Collectors.toList()); }</pre>
     */
    @Override
    ReactiveSeq<T> distinct();

    /**
     * Scan left using supplied Monoid
     * 
     * <pre>
     * {@code  
     * 
     * 	assertEquals(asList("", "a", "ab", "abc"),ReactiveSeq.of("a", "b", "c")
     * 													.scanLeft(Reducers.toString("")).toList());
     *         
     *         }
     * </pre>
     * 
     * @param monoid
     * @return ReactiveSeq with values combined scanning left
     */
    @Override
    ReactiveSeq<T> scanLeft(Monoid<T> monoid);

    /**
     * Scan left
     * 
     * <pre>
     * {@code 
     *  assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
     *         		is(4));
     * }
     * </pre>
     */
    @Override
    <U> ReactiveSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function);

    /**
     * Scan right
     * 
     * <pre>
     * {@code 
     * assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
     *             is(asList("", "c", "bc", "abc").size()));
     * }
     * </pre>
     */
    @Override
    ReactiveSeq<T> scanRight(Monoid<T> monoid);

    /**
     * Scan right
     * 
     * <pre>
     * {@code 
     * assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
     *             is(asList(0, 3, 5, 6).size()));
     * 
     * }
     * </pre>
     */
    @Override
    <U> ReactiveSeq<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner);

    /**
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
     * </pre>
     * 
     */
    @Override
    ReactiveSeq<T> sorted();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default ReactiveSeq<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return fromStream(StreamUtils.combine(this, predicate, op));
    }

    /**
     * <pre>
     * {@code 
     * 	assertThat(ReactiveSeq.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
     * }
     * </pre>
     * 
     * @param c
     *            Compartor to sort with
     * @return Sorted Stream
     */
    @Override
    ReactiveSeq<T> sorted(Comparator<? super T> c);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default ReactiveSeq<T> takeWhile(final Predicate<? super T> p) {

        return (ReactiveSeq<T>) ExtendedTraversable.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#drop(long)
     */
    @Override
    default ReactiveSeq<T> drop(final long drop) {
        return skip(drop);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#drop(long)
     */
    @Override
    default ReactiveSeq<T> take(final long take) {
        return limit(take);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default ReactiveSeq<T> dropWhile(final Predicate<? super T> p) {

        return (ReactiveSeq<T>) ExtendedTraversable.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default ReactiveSeq<T> takeUntil(final Predicate<? super T> p) {

        return (ReactiveSeq<T>) ExtendedTraversable.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default ReactiveSeq<T> dropUntil(final Predicate<? super T> p) {

        return (ReactiveSeq<T>) ExtendedTraversable.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropRight(int)
     */
    @Override
    default ReactiveSeq<T> dropRight(final int num) {

        return (ReactiveSeq<T>) ExtendedTraversable.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeRight(int)
     */
    @Override
    default ReactiveSeq<T> takeRight(final int num) {

        return (ReactiveSeq<T>) ExtendedTraversable.super.takeRight(num);
    }

    /**
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
     * </pre>
     * 
     * 
     * 
     * @param num
     *            Number of elemenets to skip
     * @return Stream with specified number of elements skipped
     */
    @Override
    ReactiveSeq<T> skip(long num);

    /**
     * Performs an action for each element of this stream.
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     * <p>
     * <p>The behavior of this operation is explicitly nondeterministic.
     * For parallel stream pipelines, this operation does <em>not</em>
     * guarantee to respect the encounter order of the stream, as doing so
     * would sacrifice the benefit of parallelism.  For any given element, the
     * action may be performed at whatever time and in whatever thread the
     * library chooses.  If the action accesses shared state, it is
     * responsible for providing the required synchronization.
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action to perform on the elements
     */
    @Override
    void forEach(Consumer<? super T> action);

    /**
     * 
     * SkipWhile drops elements from the Stream while the predicate holds, once
     * the predicte returns true all subsequent elements are included *
     * 
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(4,3,6,7).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
     * }
     * </pre>
     * 
     * @param p
     *            Predicate to skip while true
     * @return Stream with elements skipped while predicate holds
     */
    @Override
    ReactiveSeq<T> skipWhile(Predicate<? super T> p);

    /**
     * Drop elements from the Stream until the predicate returns true, after
     * which all elements are included
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}
     * </pre>
     * 
     * 
     * @param p
     *            Predicate to skip until true
     * @return Stream with elements skipped until predicate holds
     */
    @Override
    ReactiveSeq<T> skipUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#skipUntilClosed(java.util.function.Predicate)
     */
    default ReactiveSeq<T> skipUntilClosed(final Predicate<? super T> p) {
        return fromStream(seq().skipUntilClosed(p));
    }

    /**
     * 
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
     * </pre>
     * 
     * @param num
     *            Limit element size to num
     * @return Monad converted to Stream with elements up to num
     */
    @Override
    ReactiveSeq<T> limit(long num);

    /**
     * Take elements from the Stream while the predicate holds, once the
     * predicate returns false all subsequent elements are excluded
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}
     * </pre>
     * 
     * @param p
     *            Limit while predicate is true
     * @return Stream with limited elements
     */
    @Override
    ReactiveSeq<T> limitWhile(Predicate<? super T> p);

    /**
     * Take elements from the Stream until the predicate returns true, after
     * which all elements are excluded.
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }
     * </pre>
     * 
     * @param p
     *            Limit until predicate is true
     * @return Stream with limited elements
     */
    @Override
    ReactiveSeq<T> limitUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#limitUntilClosed(java.util.function.Predicate)
     */
    default ReactiveSeq<T> limitUntilClosed(final Predicate<? super T> p) {
        return fromStream(seq().limitUntilClosed(p));
    }

    /**
     * @return Does nothing ReactiveSeq is for Sequential Streams
     * 
     */
    @Override
    ReactiveSeq<T> parallel();

    /**
     * True if predicate matches all elements when Monad converted to a Stream
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate to check if all match
     */
    @Override
    boolean allMatch(Predicate<? super T> c);

    /**
     * True if a single element matches when Monad converted to a Stream
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate to check if any match
     */
    @Override
    boolean anyMatch(Predicate<? super T> c);

    /**
     * Check that there are specified number of matches of predicate in the
     * Stream
     * 
     * <pre>
     * {@code 
     *  assertTrue(ReactiveSeq.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
     * }
     * </pre>
     * 
     */
    @Override
    boolean xMatch(int num, Predicate<? super T> c);

    /*
     * <pre> {@code assertThat(of(1,2,3,4,5).noneMatch(it->
     * it==5000),equalTo(true));
     * 
     * } </pre>
     */
    @Override
    boolean noneMatch(Predicate<? super T> c);

    /**
     * <pre>
     * {@code
     *  assertEquals("123".length(),ReactiveSeq.of(1, 2, 3).join().length());
     * }
     * </pre>
     * 
     * @return Stream as concatenated String
     */
    @Override
    String join();

    /**
     * <pre>
     * {@code
     * assertEquals("1, 2, 3".length(), ReactiveSeq.of(1, 2, 3).join(", ").length());
     * }
     * </pre>
     * 
     * @return Stream as concatenated String
     */
    @Override
    String join(String sep);

    /**
     * <pre>
     * {@code 
     * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
     * }
     * </pre>
     * 
     * @return Stream as concatenated String
     */
    @Override
    String join(String sep, String start, String end);

   
    @Override
    HeadAndTail<T> headAndTail();

    /**
     * @return First matching element in sequential order
     * 
     * <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5).filter(it -> it <3).findFirst().get();
     * 
     * //3
     * }
     * </pre>
     * 
     * (deterministic)
     * 
     */
    @Override
    Optional<T> findFirst();

    /**
     * @return first matching element, but order is not guaranteed
     * 
     *         <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5).filter(it -> it <3).findAny().get();
     * 
     * //3
     * }
     * </pre>
     * 
     * 
     *         (non-deterministic)
     */
    @Override
    Optional<T> findAny();

    /**
     * Performs a map operation that can call a recursive method without running out of stack space
     * <pre>
     * {@code
     * ReactiveSeq.of(10,20,30,40)
    		 .trampoline(i-> fibonacci(i))
    		 .forEach(System.out::println); 
    		 
    Trampoline<Long> fibonacci(int i){
    	return fibonacci(i,1,0);
    }
    Trampoline<Long> fibonacci(int n, long a, long b) {
       	return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
    }		 
    		 
     * 55
    6765
    832040
    102334155
     * 
     * 
     * ReactiveSeq.of(10_000,200_000,3_000_000,40_000_000)
    		 .trampoline(i-> fibonacci(i))
    		 .forEach(System.out::println);
    		 
    		 
     * completes successfully
     * }
     * 
    * @param mapper
    * @return
    */
    @Override
    default <R> ReactiveSeq<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return map(in -> mapper.apply(in)
                               .result());
    }

    /**
     * Attempt to map this Sequence to the same type as the supplied Monoid
     * (Reducer) Then use Monoid to reduce values
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
     * 
     * //4
     * }
     * </pre>
     * 
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    @Override
    <R> R mapReduce(Reducer<R> reducer);

    /**
     * Attempt to map this Monad to the same type as the supplied Monoid, using
     * supplied function Then use Monoid to reduce values
     * 
     * <pre>
     *  {@code
     *  ReactiveSeq.of("one","two","three","four")
     *           .mapReduce(this::toInt,Reducers.toTotalInt());
     *  
     *  //10
     *  
     *  int toInt(String s){
     * 		if("one".equals(s))
     * 			return 1;
     * 		if("two".equals(s))
     * 			return 2;
     * 		if("three".equals(s))
     * 			return 3;
     * 		if("four".equals(s))
     * 			return 4;
     * 		return -1;
     * 	   }
     *  }
     * </pre>
     * 
     * @param mapper
     *            Function to map Monad type
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    @Override
    <R> R mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer);

    /**
     * <pre>
     * {@code 
     * ReactiveSeq.of("hello","2","world","4").reduce(Reducers.toString(","));
     * 
     * //hello,2,world,4
     * }
     * </pre>
     * 
     * @param reducer
     *            Use supplied Monoid to reduce values
     * @return reduced values
     */
    @Override
    T reduce(Monoid<T> reducer);

    /*
     * <pre> {@code assertThat(ReactiveSeq.of(1,2,3,4,5).map(it -> it*100).reduce(
     * (acc,next) -> acc+next).get(),equalTo(1500)); } </pre>
     */
    @Override
    Optional<T> reduce(BinaryOperator<T> accumulator);

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.Stream#reduce(java.lang.Object,
     * java.util.function.BinaryOperator)
     */
    @Override
    T reduce(T identity, BinaryOperator<T> accumulator);

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.Stream#reduce(java.lang.Object,
     * java.util.function.BiFunction, java.util.function.BinaryOperator)
     */
    @Override
    <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream.  A mutable
     * reduction is one in which the reduced value is a mutable result container,
     * such as an {@code ArrayList}, and elements are incorporated by updating
     * the state of the result rather than by replacing the result.  This
     * produces a result equivalent to:
     * <pre>{@code
     *     R result = supplier.get();
     *     for (T element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     * <p>
     * <p>Like {@link #reduce(Object, BinaryOperator)}, {@code collect} operations
     * can be parallelized without requiring additional synchronization.
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @param supplier    a function that creates a new result container. For a
     *                    parallel execution, this function may be called
     *                    multiple times and must return a fresh value each time.
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for incorporating an additional element into a result
     * @param combiner    an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values, which must be
     *                    compatible with the accumulator function
     * @return the result of the reduction
     * @apiNote There are many existing classes in the JDK whose signatures are
     * well-suited for use with method references as arguments to {@code collect()}.
     * For example, the following will accumulate strings into an {@code ArrayList}:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(ArrayList::new, ArrayList::add,
     *                                                ArrayList::addAll);
     * }</pre>
     * <p>
     * <p>The following will take a stream of strings and concatenates them into a
     * single string:
     * <pre>{@code
     *     String concat = stringStream.collect(StringBuilder::new, StringBuilder::append,
     *                                          StringBuilder::append)
     *                                 .toString();
     * }</pre>
     */
    @Override
    default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return seq().collect(supplier,accumulator,combiner);
    }

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream using a
     * {@code Collector}.  A {@code Collector}
     * encapsulates the functions used as arguments to
     * {@link #collect(Supplier, BiConsumer, BiConsumer)}, allowing for reuse of
     * collection strategies and composition of collect operations such as
     * multiple-level grouping or partitioning.
     * <p>
     * <p>If the stream is parallel, and the {@code Collector}
     * is {@link Collector.Characteristics#CONCURRENT concurrent}, and
     * either the stream is unordered or the collector is
     * {@link Collector.Characteristics#UNORDERED unordered},
     * then a concurrent reduction will be performed (see {@link Collector} for
     * details on concurrent reduction.)
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     * <p>
     * <p>When executed in parallel, multiple intermediate results may be
     * instantiated, populated, and merged so as to maintain isolation of
     * mutable data structures.  Therefore, even when executed in parallel
     * with non-thread-safe data structures (such as {@code ArrayList}), no
     * additional synchronization is needed for a parallel reduction.
     *
     * @param collector the {@code Collector} describing the reduction
     * @return the result of the reduction
     * @apiNote The following will accumulate strings into an ArrayList:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(Collectors.toList());
     * }</pre>
     * <p>
     * <p>The following will classify {@code Person} objects by city:
     * <pre>{@code
     *     Map<String, List<Person>> peopleByCity
     *         = personStream.collect(Collectors.groupingBy(Person::getCity));
     * }</pre>
     * <p>
     * <p>The following will classify {@code Person} objects by state and city,
     * cascading two {@code Collector}s together:
     * <pre>{@code
     *     Map<String, Map<String, List<Person>>> peopleByStateAndCity
     *         = personStream.collect(Collectors.groupingBy(Person::getState,
     *                                                      Collectors.groupingBy(Person::getCity)));
     * }</pre>
     * @see #collect(Supplier, BiConsumer, BiConsumer)
     * @see Collectors
     */
    @Override
    default <R, A> R collect(Collector<? super T, A, R> collector) {
        return seq().collect(collector);
    }

    /**
     * Reduce with multiple reducers in parallel NB if this Monad is an Optional
     * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
     * was one value To reduce over the values on the list, called
     * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     * 
     * <pre>
     * {@code
     *  Monoid<Integer> sum = Monoid.of(0, (a, b) -> a + b);
     *  Monoid<Integer> mult = Monoid.of(1, (a, b) -> a * b);
     *  List<Integer> result = ReactiveSeq.of(1, 2, 3, 4).reduce(Arrays.asList(sum, mult).stream());
     * 
     *  assertThat(result, equalTo(Arrays.asList(10, 24)));
     * 
     * }
     * </pre>
     * 
     * 
     * @param reducers
     * @return
     */
    @Override
    ListX<T> reduce(Stream<? extends Monoid<T>> reducers);

    /**
     * Reduce with multiple reducers in parallel NB if this Monad is an Optional
     * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
     * was one value To reduce over the values on the list, called
     * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     * 
     * <pre>
     * {@code 
     * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
     * 		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
     * 		List<Integer> result = ReactiveSeq.of(1,2,3,4))
     * 										.reduce(Arrays.asList(sum,mult) );
     * 				
     * 		 
     * 		assertThat(result,equalTo(Arrays.asList(10,24)));
     * 
     * }
     * 
     * @param reducers
     * @return
     */
    @Override
    ListX<T> reduce(Iterable<? extends Monoid<T>> reducers);

    /**
     * 
     * <pre>
     * 		{@code
     * 		ReactiveSeq.of("a","b","c").foldRight(Reducers.toString(""));
     *        
     *         // "cab"
     *         }
     * </pre>
     * 
     * @param reducer
     *            Use supplied Monoid to reduce values starting via foldRight
     * @return Reduced result
     */
    @Override
    T foldRight(Monoid<T> reducer);

    /**
     * Immutable reduction from right to left
     * 
     * <pre>
     * {@code 
     *  assertTrue(ReactiveSeq.of("a","b","c").foldRight("", String::concat).equals("cba"));
     * }
     * </pre>
     * 
     */
    @Override
    public T foldRight(T identity, BinaryOperator<T> accumulator);

    /**
     * Attempt to map this Monad to the same type as the supplied Monoid (using
     * mapToType on the monoid interface) Then use Monoid to reduce values
     * 
     * <pre>
     * 		{@code
     * 		ReactiveSeq.of(1,2,3).foldRightMapToType(Reducers.toString(""));
     *        
     *         // "321"
     *         }
     * </pre>
     * 
     * 
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     **/
    @Override
    public <T> T foldRightMapToType(Reducer<T> reducer);

    /**
     * <pre>
     * {@code 
     * 	Streamable<Integer> repeat = ReactiveSeq.of(1,2,3,4,5,6)
     * 												.map(i->i*2)
     * 												.toStreamable();
     * 		
     * 		repeat.stream().toList(); //Arrays.asList(2,4,6,8,10,12));
     * 		repeat.stream().toList() //Arrays.asList(2,4,6,8,10,12));
     * 
     * }
     * 
     * @return Lazily Convert to a repeatable Streamable
     * 
     */
    @Override
    public Streamable<T> toStreamable();

    /**
     * @return This Stream converted to a set
     */
    @Override
    public Set<T> toSet();

    /**
     * @return this Stream converted to a list
     */
    @Override
    public List<T> toList();

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#toCollection(java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory);

    /**
     * Convert this ReactiveSeq into a Stream
     * 
     * @return calls to stream() but more flexible on type for inferencing
     *         purposes.
     */
    public <T> Stream<T> toStream();

    /**
     * Convert this ReactiveSeq into a Stream
     */
    @Override
    public ReactiveSeq<T> stream();

    /**
     * 
     * <pre>
     * {@code 
     *  assertTrue(ReactiveSeq.of(1,2,3,4).startsWith(Arrays.asList(1,2,3)));
     * }
     * </pre>
     * 
     * @param iterable
     * @return True if Monad starts with Iterable sequence of data
     */
    @Override
    boolean startsWithIterable(Iterable<T> iterable);

    /**
     * <pre>
     * {@code assertTrue(ReactiveSeq.of(1,2,3,4).startsWith(Stream.of(1,2,3))) }
     * </pre>
     * 
     * @param stream
     * @return True if Monad starts with Iterators sequence of data
     */
    @Override
    boolean startsWith(Stream<T> stream);

    /**
     * @return this ReactiveSeq converted to AnyM format
     */
    public AnyMSeq<Witness.stream,T> anyM();

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.Stream#map(java.util.function.Function)
     */
    @Override
    <R> ReactiveSeq<R> map(Function<? super T, ? extends R> fn);

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.Stream#peek(java.util.function.Consumer)
     */
    @Override
    ReactiveSeq<T> peek(Consumer<? super T> c);

    /**
     * flatMap operation
     * 
     * <pre>
     * {@code
     * 	assertThat(ReactiveSeq.of(1,2)
     * 						.flatMap(i -> asList(i, -i).stream())
     * 						.toList(),equalTo(asList(1, -1, 2, -2)));		
     *  
     * }
     * </pre>
     * 
     * @param fn
     *            to be applied
     * @return new stage in Sequence with flatMap operation to be lazily applied
     */
    @Override
    <R> ReactiveSeq<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn);

    /**
     * Allows flatMap return type to be any Monad type
     * 
     * <pre>
     * {@code 
     * 	assertThat(ReactiveSeq.of(1,2,3)).flatMapAnyM(i-> anyM(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));
     * 
     * }
     * </pre>
     * 
     * 
     * @param fn
     *            to be applied
     * @return new stage in Sequence with flatMap operation to be lazily applied
     */
    <R> ReactiveSeq<R> flatMapAnyM(Function<? super T, AnyM<Witness.stream,? extends R>> fn);

    /**
     * FlatMap where the result is a Collection, flattens the resultant
     * collections into the host ReactiveSeq
     * 
     * <pre>
     * {@code 
     * 	ReactiveSeq.of(1,2)
     * 			.flatMap(i -> asList(i, -i))
     *          .toList();
     *          
     *   //1,-1,2,-2       
     * }
     * </pre>
     * 
     * @param fn
     * @return
     */
    <R> ReactiveSeq<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> fn);

    /**
     * flatMap operation
     * 
     * <pre>
     * {@code 
     * 	assertThat(ReactiveSeq.of(1,2,3)
     *                      .flatMapStream(i->IntStream.of(i))
     *                      .toList(),equalTo(Arrays.asList(1,2,3)));
     * 
     * }
     * </pre>
     * 
     * @param fn
     *            to be applied
     * @return new stage in Sequence with flatMap operation to be lazily applied
     */
    <R> ReactiveSeq<R> flatMapStream(Function<? super T, BaseStream<? extends R, ?>> fn);

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.Stream#filter(java.util.function.Predicate)
     */
    @Override
    ReactiveSeq<T> filter(Predicate<? super T> fn);

    /**
     * Returns a spliterator for the elements of this stream.
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return the element spliterator for this stream
     */
    @Override
    Spliterator<T> spliterator();

    /* (non-Javadoc)
         * @see java.util.stream.BaseStream#sequential()
         */
    @Override
    ReactiveSeq<T> sequential();

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.BaseStream#unordered()
     */
    @Override
    ReactiveSeq<T> unordered();

    /**
     * Returns a stream with a given value interspersed between any two values
     * of this stream.
     * 
     * 
     * // (1, 0, 2, 0, 3, 0, 4) ReactiveSeq.of(1, 2, 3, 4).intersperse(0)
     * 
     */
    @Override
    ReactiveSeq<T> intersperse(T value);

    /**
     * Keep only those elements in a stream that are of a given type.
     * 
     * 
     * // (1, 2, 3) ReactiveSeq.of(1, "a", 2, "b",3).ofType(Integer.class)
     * 
     */
    @Override
    @SuppressWarnings("unchecked")
    <U> ReactiveSeq<U> ofType(Class<? extends U> type);

    /**
     * Cast all elements in a stream to a given type, possibly throwing a
     * {@link ClassCastException}.
     * 
     * 
     * // ClassCastException ReactiveSeq.of(1, "a", 2, "b", 3).cast(Integer.class)
     * 
     */
    @Override
    <U> ReactiveSeq<U> cast(Class<? extends U> type);

    /**
     * Lazily converts this ReactiveSeq into a Collection. This does not trigger
     * the Stream. E.g. Collection is not thread safe on the first iteration.
     * 
     * <pre>
     * {@code
     *  Collection<Integer> col = ReactiveSeq.of(1, 2, 3, 4, 5)
     *                                       .peek(System.out::println)
     *                                       .toLazyCollection();
     * 
     *  col.forEach(System.out::println);
     * }
     * 
     * // Will print out "first!" before anything else
     * </pre>
     * 
     * @return
     */
    @Override
    CollectionX<T> toLazyCollection();

    /**
     * Lazily converts this ReactiveSeq into a Collection. This does not trigger
     * the Stream. E.g.
     * 
     * <pre>
     * {@code
     *  Collection<Integer> col = ReactiveSeq.of(1, 2, 3, 4, 5).peek(System.out::println).toConcurrentLazyCollection();
     * 
     *  col.forEach(System.out::println);
     * }
     * 
     * // Will print out "first!" before anything else
     * </pre>
     * 
     * @return
     */
    @Override
    CollectionX<T> toConcurrentLazyCollection();

    /**
     * <pre>
     * {@code
     *  Streamable<Integer> repeat = ReactiveSeq.of(1, 2, 3, 4, 5, 6).map(i -> i + 2).toConcurrentLazyStreamable();
     * 
     *  assertThat(repeat.stream().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
     *  assertThat(repeat.stream().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
     * }
     * </pre>
     * 
     * @return Streamable that replay this ReactiveSeq, populated lazily and can
     *         be populated across threads
     */
    @Override
    public Streamable<T> toConcurrentLazyStreamable();

    /*
     * Potentially efficient Sequence reversal. Is efficient if
     * 
     * - Sequence created via a range - Sequence created via a List - Sequence
     * created via an Array / var args
     * 
     * Otherwise Sequence collected into a Collection prior to reversal
     * 
     * <pre> {@code assertThat( of(1, 2, 3).reverse().toList(),
     * equalTo(asList(3, 2, 1))); } </pre>
     */
    @Override
    public ReactiveSeq<T> reverse();

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.BaseStream#onClose(java.lang.Runnable)
     */
    @Override
    public ReactiveSeq<T> onClose(Runnable closeHandler);

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#shuffle()
     */
    @Override
    public ReactiveSeq<T> shuffle();

    /**
     * Append Stream to this ReactiveSeq
     * 
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).appendStream(ReactiveSeq.of(100, 200, 300)).map(it -> it + "!!").collect(Collectors.toList());
     * 
     *  assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "3!!", "100!!", "200!!", "300!!")));     * }
     * </pre>
     * 
     * @param stream
     *            to append
     * @return ReactiveSeq with Stream appended
     */
    public ReactiveSeq<T> appendStream(Stream<T> stream);

    /**
     * Prepend Stream to this ReactiveSeq
     * 
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3)
     *                                   .prependStream(of(100, 200, 300))
     *                                   .map(it -> it + "!!")
     *                                   .collect(Collectors.toList());
     * 
     *  assertThat(result, equalTo(Arrays.asList("100!!", "200!!", "300!!", "1!!", "2!!", "3!!")));
     * }
     * </pre>
     * 
     * @param stream
     *            to Prepend
     * @return ReactiveSeq with Stream prepended
     */
    ReactiveSeq<T> prependStream(Stream<T> stream);

    /**
     * Append values to the end of this ReactiveSeq
     * 
     * <pre>
     * {@code 
     *  List<String> result = ReactiveSeq.of(1, 2, 3).append(100, 200, 300).map(it -> it + "!!").collect(Collectors.toList());
     * 
     *  assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "3!!", "100!!", "200!!", "300!!")));     * }
     * </pre>
     * 
     * @param values
     *            to append
     * @return ReactiveSeq with appended values
     */

    ReactiveSeq<T> append(T... values);


    ReactiveSeq<T> append(T value);


    ReactiveSeq<T> prepend(T value);

    /**
     * Prepend given values to the start of the Stream
     * 
     * <pre>
     * {@code 
     * List<String> result = 	ReactiveSeq.of(1,2,3)
     * 									 .prepend(100,200,300)
     * 										 .map(it ->it+"!!")
     * 										 .collect(Collectors.toList());
     * 
     * 			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * @param values to prepend
     * @return ReactiveSeq with values prepended
     */

    ReactiveSeq<T> prepend(T... values);

    /**
     * Insert data into a stream at given position
     * 
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).insertAt(1, 100, 200, 300).map(it -> it + "!!").collect(Collectors.toList());
     * 
     *  assertThat(result, equalTo(Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));     * 
     * }
     * </pre>
     * 
     * @param pos
     *            to insert data at
     * @param values
     *            to insert
     * @return Stream with new data inserted
     */
    ReactiveSeq<T> insertAt(int pos, T... values);

    /**
     * Delete elements between given indexes in a Stream
     * 
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3, 4, 5, 6).deleteBetween(2, 4).map(it -> it + "!!").collect(Collectors.toList());
     * 
     *  assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "5!!", "6!!")));     * }
     * </pre>
     * 
     * @param start
     *            index
     * @param end
     *            index
     * @return Stream with elements removed
     */
    ReactiveSeq<T> deleteBetween(int start, int end);

    /**
     * Insert a Stream into the middle of this stream at the specified position
     * 
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).insertStreamAt(1, of(100, 200, 300)).map(it -> it + "!!").collect(Collectors.toList());
     * 
     *  assertThat(result, equalTo(Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));
     * }
     * </pre>
     * 
     * @param pos
     *            to insert Stream at
     * @param stream
     *            to insert
     * @return newly conjoined ReactiveSeq
     */
    ReactiveSeq<T> insertStreamAt(int pos, Stream<T> stream);



    /**
     * <pre>
     * {@code
     *  assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
     * 				.endsWith(Arrays.asList(5,6)));
     * 
     * }
     * 
     * @param iterable Values to check
     * @return true if ReactiveSeq ends with values in the supplied iterable
     */
    @Override
    boolean endsWithIterable(Iterable<T> iterable);

    /**
     * <pre>
     * {@code
     * assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
     * 				.endsWith(Stream.of(5,6))); 
     * }
     * </pre>
     * 
     * @param stream
     *            Values to check
     * @return true if ReactiveSeq endswith values in the supplied Stream
     */
    @Override
    boolean endsWith(Stream<T> stream);

    /**
     * Skip all elements until specified time period has passed
     * 
     * <pre>
     * {@code
     *  List<Integer> result = ReactiveSeq.of(1, 2, 3, 4, 5, 6).peek(i -> sleep(i * 100)).skip(1000, TimeUnit.MILLISECONDS).toList();
     * 
     *  // [4,5,6]

     * 
     * }
     * </pre>
     * 
     * @param time
     *            Length of time
     * @param unit
     *            Time unit
     * @return ReactiveSeq that skips all elements until time period has elapsed
     */
    ReactiveSeq<T> skip(long time, final TimeUnit unit);

    /**
     * Return all elements until specified time period has elapsed
     * 
     * <pre>
     * {@code
     *  List<Integer> result = ReactiveSeq.of(1, 2, 3, 4, 5, 6).peek(i -> sleep(i * 100)).limit(1000, TimeUnit.MILLISECONDS).toList();
     * 
     *  // [1,2,3,4]

     * }
     * </pre>
     * 
     * @param time
     *            Length of time
     * @param unit
     *            Time unit
     * @return ReactiveSeq that returns all elements until time period has elapsed
     */
    ReactiveSeq<T> limit(long time, final TimeUnit unit);

    /**
     * assertThat(ReactiveSeq.of(1,2,3,4,5) .skipLast(2)
     * .collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
     * 
     * @param num
     * @return
     */
    @Override
    ReactiveSeq<T> skipLast(int num);

    /**
     * Limit results to the last x elements in a ReactiveSeq
     * 
     * <pre>
     * {@code 
     * 	assertThat(ReactiveSeq.of(1,2,3,4,5)
     * 							.limitLast(2)
     * 							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
     * 
     * }
     * 
     * @param num of elements to return (last elements)
     * @return ReactiveSeq limited to last num elements
     */
    @Override
    ReactiveSeq<T> limitLast(int num);

    /**
     * Turns this ReactiveSeq into a HotStream, a connectable Stream, being executed on a thread on the 
     * supplied executor, that is producing data. Note this method creates a HotStream that starts emitting data
     * immediately. For a hotStream that waits until the first user streams connect @see {@link ReactiveSeq#primedHotStream(Executor)}.
     * The generated HotStream is not pausable, for a pausable HotStream @see {@link ReactiveSeq#pausableHotStream(Executor)}.
     * Turns this ReactiveSeq into a HotStream, a connectable Stream, being
     * executed on a thread on the supplied executor, that is producing data
     * 
     * <pre>
     * {@code 
     *  HotStream<Integer> ints = ReactiveSeq.range(0,Integer.MAX_VALUE)
     * 											.hotStream(exec)
     * 											
     * 		
     * 		ints.connect().forEach(System.out::println);									
     *  //print out all the ints
     *  //multiple consumers are possible, so other Streams can connect on different Threads
     *  
     * }
     * </pre>
     * 
     * @param e
     *            Executor to execute this ReactiveSeq on
     * @return a Connectable HotStream
     */
    HotStream<T> hotStream(Executor e);

    /**
     * Return a HotStream that will start emitting data when the first connecting Stream connects.
     * Note this method creates a HotStream that starts emitting data only when the first connecting Stream connects.
     *  For a hotStream that starts to output data immediately @see {@link ReactiveSeq#hotStream(Executor)}.
     * The generated HotStream is not pausable, for a pausable HotStream @see {@link ReactiveSeq#primedPausableHotStream(Executor)}.
     * <pre>
      * <pre>
     * {@code 
     *  HotStream<Integer> ints = ReactiveSeq.range(0,Integer.MAX_VALUE)
    										.hotStream(exec)
    										
    	
    	ints.connect().forEach(System.out::println);									
     *  //print out all the ints - starting when connect is called.
     *  //multiple consumers are possible, so other Streams can connect on different Threads
     *  
     * }
     * </pre>
     * @param e
     * @return
     */
    HotStream<T> primedHotStream(Executor e);

    /**
     * Turns this ReactiveSeq into a HotStream, a connectable & pausable Stream, being executed on a thread on the 
     * supplied executor, that is producing data. Note this method creates a HotStream that starts emitting data
     * immediately. For a hotStream that waits until the first user streams connect @see {@link ReactiveSeq#primedPausableHotStream(Executor)}.
     * The generated HotStream is pausable, for a unpausable HotStream (slightly faster execution) @see {@link ReactiveSeq#hotStream(Executor)}.
     * <pre>
     * {@code 
     *  HotStream<Integer> ints = ReactiveSeq.range(0,Integer.MAX_VALUE)
    										.hotStream(exec)
    										
    	
    	ints.connect().forEach(System.out::println);
    	
    	ints.pause(); //on a separate thread pause the generating Stream
    										
     *  //print out all the ints
     *  //multiple consumers are possible, so other Streams can connect on different Threads
     *  
     * }
     * </pre>
     * @param e Executor to execute this ReactiveSeq on
     * @return a Connectable HotStream
     */
    PausableHotStream<T> pausableHotStream(Executor e);

    /**
     * Return a pausable HotStream that will start emitting data when the first connecting Stream connects.
     * Note this method creates a HotStream that starts emitting data only when the first connecting Stream connects.
     *  For a hotStream that starts to output data immediately @see {@link ReactiveSeq#pausableHotStream(Executor)}.
     * The generated HotStream is pausable, for a unpausable HotStream @see {@link ReactiveSeq#primedHotStream(Executor)}.
     * <pre>
      * <pre>
     * {@code 
     *  HotStream<Integer> ints = ReactiveSeq.range(0,Integer.MAX_VALUE)
    										.hotStream(exec)
    										
    	
    	ints.connect().forEach(System.out::println);									
     *  //print out all the ints - starting when connect is called.
     *  //multiple consumers are possible, so other Streams can connect on different Threads
     *  
     * }
     * </pre>
     * @param e
     * @return
     */
    PausableHotStream<T> primedPausableHotStream(Executor e);

    /**
     * <pre>
     * {@code 
     * 	assertThat(ReactiveSeq.of(1,2,3,4)
     * 					.map(u->{throw new RuntimeException();})
     * 					.recover(e->"hello")
     * 					.firstValue(),equalTo("hello"));
     * }
     * </pre>
     * 
     * @return first value in this Stream
     */
    @Override
    T firstValue();

    /**
     * <pre>
     * {@code 
     *    
     *    //1
     *    ReactiveSeq.of(1).single(); 
     *    
     *    //UnsupportedOperationException
     *    ReactiveSeq.of().single();
     *     
     *     //UnsupportedOperationException
     *    ReactiveSeq.of(1,2,3).single();
     * }
     * </pre>
     * 
     * @return a single value or an UnsupportedOperationException if 0/1 values
     *         in this Stream
     */
    @Override
    default T single() {
        final Iterator<T> it = iterator();
        if (it.hasNext()) {
            final T result = it.next();
            if (!it.hasNext())
                return result;
        }
        throw new UnsupportedOperationException(
                                                "single only works for Streams with a single value");

    }

    @Override
    default T single(final Predicate<? super T> predicate) {
        return this.filter(predicate)
                   .single();

    }

    /**
     * <pre>
     * {@code 
     *    
     *    //Optional[1]
     *    ReactiveSeq.of(1).singleOptional(); 
     *    
     *    //Optional.empty
     *    ReactiveSeq.of().singleOpional();
     *     
     *     //Optional.empty
     *    ReactiveSeq.of(1,2,3).singleOptional();
     * }
     * </pre>
     * 
     * @return An Optional with single value if this Stream has exactly one
     *         element, otherwise Optional Empty
     */
    @Override
    default Optional<T> singleOptional() {
        final Iterator<T> it = iterator();
        if (it.hasNext()) {
            final T result = it.next();
            if (!it.hasNext())
                return Optional.of(result);
        }
        return Optional.empty();

    }

    /**
     * Return the elementAt index or Optional.empty
     * 
     * <pre>
     * {@code
     * 	assertThat(ReactiveSeq.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
     * }
     * </pre>
     * 
     * @param index
     *            to extract element from
     * @return elementAt index
     */
    @Override
    default Optional<T> get(final long index) {
        return this.zipWithIndex()
                   .filter(t -> t.v2 == index)
                   .findFirst()
                   .map(t -> t.v1());
    }

    /**
     * Gets the element at index, and returns a Tuple containing the element (it
     * must be present) and a lazy copy of the Sequence for further processing.
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3,4,5).get(2).v1
     * //3
     * }
     * </pre>
     * 
     * @param index
     *            to extract element from
     * @return Element and Sequence
     */
    default Tuple2<T, ReactiveSeq<T>> elementAt(final long index) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> tuple = this.duplicateSequence();
        return tuple.map1(s -> s.zipWithIndex()
                                .filter(t -> t.v2 == index)
                                .findFirst()
                                .map(t -> t.v1())
                                .get());
    }

    /**
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3,4,5)
     * 				 .elapsed()
     * 				 .forEach(System.out::println);
     * }
     * </pre>
     * 
     * @return Sequence that adds the time between elements in millis to each
     *         element
     */
    default ReactiveSeq<Tuple2<T, Long>> elapsed() {
        final AtomicLong last = new AtomicLong(
                                               System.currentTimeMillis());

        return zip(ReactiveSeq.generate(() -> {
            final long now = System.currentTimeMillis();

            final long result = now - last.get();
            last.set(now);
            return result;
        }));
    }

    /**
     * <pre>
     * {@code
     *    ReactiveSeq.of(1,2,3,4,5)
     * 				   .timestamp()
     * 				   .forEach(System.out::println)
     * 
     * }
     * 
     * </pre>
     * 
     * @return Sequence that adds a timestamp to each element
     */
    default ReactiveSeq<Tuple2<T, Long>> timestamp() {
        return zip(ReactiveSeq.generate(() -> System.currentTimeMillis()));
    }

    /**
     * Create a subscriber that can listen to Reactive Streams (simple-react,
     * RxJava AkkaStreams, Kontraktor, QuadarStreams etc)
     * 
     * <pre>
     * {@code
     *     SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
     * 		ReactiveSeq.of(1,2,3).subscribe(sub);
     * 		sub.stream().forEach(System.out::println);
     * 		
     * 		  1 
     * 		  2
     * 		  3
     * }
     * 
     * </pre>
     * 
     * @return A reactive-streams Subscriber
     */
    public static <T> SeqSubscriber<T> subscriber() {
        return SeqSubscriber.subscriber();
    }

    public static <T> ReactiveSeq<T> empty() {
        return fromStream(Stream.empty());
    }

    /**
     * Create an efficiently reversable Sequence from the provided elements
     * 
     * @param elements
     *            To Construct sequence from
     * @return
     */
    @SafeVarargs
    public static <T> ReactiveSeq<T> of(final T... elements) {
        final ReversingArraySpliterator<T> array = new ReversingArraySpliterator<T>(
                                                                                    elements, false, 0);
        return StreamUtils.reactiveSeq(StreamSupport.stream(array, false), Optional.ofNullable(array),Optional.empty());

    }

    /**
     * Construct a Reveresed Sequence from the provided elements Can be reversed
     * (again) efficiently
     * 
     * @param elements
     *            To Construct sequence from
     * @return
     */
    @SafeVarargs
    public static <T> ReactiveSeq<T> reversedOf(final T... elements) {
        final ReversingArraySpliterator<T> array = new ReversingArraySpliterator<T>(
                                                                                    elements, false, 0).invert();
        return StreamUtils.reactiveSeq(StreamSupport.stream(array, false), Optional.ofNullable(array),Optional.empty());

    }

    /**
     * Construct a Reveresed Sequence from the provided elements Can be reversed
     * (again) efficiently
     * 
     * @param elements
     *            To Construct sequence from
     * @return
     */
    public static <T> ReactiveSeq<T> reversedListOf(final List<T> elements) {
        Objects.requireNonNull(elements);
        final ReversingListSpliterator<T> list = new ReversingListSpliterator<T>(
                                                                              elements, false).invert();
        return StreamUtils.reactiveSeq(StreamSupport.stream(list, false), Optional.ofNullable(list),Optional.empty());

    }

    /**
     * Create an efficiently reversable Sequence that produces the integers
     * between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ReactiveSeq
     */
    public static ReactiveSeq<Integer> range(final int start, final int end) {
        final ReversingRangeIntSpliterator range = new ReversingRangeIntSpliterator(
                                                                                    start, end, false);
        return StreamUtils.reactiveSeq(StreamSupport.stream(range, false), Optional.ofNullable(range),Optional.empty());

    }

    /**
     * Create an efficiently reversable Sequence that produces the integers
     * between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ReactiveSeq
     */
    public static ReactiveSeq<Long> rangeLong(final long start, final long end) {
        final ReversingRangeLongSpliterator range = new ReversingRangeLongSpliterator(
                                                                                      start, end, false);
        return StreamUtils.reactiveSeq(StreamSupport.stream(range, false), Optional.ofNullable(range),Optional.empty());

    }

    /**
     * Construct a ReactiveSeq from a Stream
     * 
     * @param stream
     *            Stream to construct Sequence from
     * @return
     */
    public static <T> ReactiveSeq<T> fromStream(final Stream<T> stream) {
        Objects.requireNonNull(stream);
        if (stream instanceof ReactiveSeq)
            return (ReactiveSeq<T>) stream;
        return StreamUtils.reactiveSeq(stream, Optional.empty(),Optional.empty());
    }

    /**
     * Construct a ReactiveSeq from a Stream
     * 
     * @param stream
     *            Stream to construct Sequence from
     * @return
     */
    public static ReactiveSeq<Integer> fromIntStream(final IntStream stream) {
        Objects.requireNonNull(stream);
        return StreamUtils.reactiveSeq(stream.boxed(), Optional.empty(),Optional.empty());

    }

    /**
     * Construct a ReactiveSeq from a Stream
     * 
     * @param stream
     *            Stream to construct Sequence from
     * @return
     */
    public static ReactiveSeq<Long> fromLongStream(final LongStream stream) {
        Objects.requireNonNull(stream);
        return StreamUtils.reactiveSeq(stream.boxed(), Optional.empty(),Optional.empty());
    }

    /**
     * Construct a ReactiveSeq from a Stream
     * 
     * @param stream
     *            Stream to construct Sequence from
     * @return
     */
    public static ReactiveSeq<Double> fromDoubleStream(final DoubleStream stream) {
        Objects.requireNonNull(stream);
        return StreamUtils.reactiveSeq(stream.boxed(), Optional.empty(),Optional.empty());
    }

    /**
     * Construct a ReactiveSeq from a List (prefer this method if the source is a
     * list, as it allows more efficient Stream reversal).
     * 
     * @param list
     *            to construct Sequence from
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> fromList(final List<T> list) {
        Objects.requireNonNull(list);
        final ReversingListSpliterator array = new ReversingListSpliterator<T>(
                                                                               list, false);
        return StreamUtils.reactiveSeq(StreamSupport.stream(array, false), Optional.ofNullable(array),Optional.empty());
    }

    /**
     * Construct a ReactiveSeq from an Publisher
     * 
     * @param publisher
     *            to construct ReactiveSeq from
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> fromPublisher(final Publisher<? extends T> publisher) {
        Objects.requireNonNull(publisher);
        final SeqSubscriber<T> sub = SeqSubscriber.subscriber();
        publisher.subscribe(sub);
        return sub.stream();
    }

    /**
     * Construct a ReactiveSeq from an Iterable
     * 
     * @param iterable
     *            to construct Sequence from
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> fromIterable(final Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        return StreamUtils.reactiveSeq(StreamSupport.stream(iterable.spliterator(), false), Optional.empty(), Optional.empty());
    }

    /**
     * Construct a ReactiveSeq from an Iterator
     * 
     * @param iterator
     *            to construct Sequence from
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> fromIterator(final Iterator<T> iterator) {
        Objects.requireNonNull(iterator);
        return fromIterable(() -> iterator);
    }

    /**
     * @see Stream#iterate(Object, UnaryOperator)
     */
    static <T> ReactiveSeq<T> iterate(final T seed, final UnaryOperator<T> f) {
        return StreamUtils.reactiveSeq(Stream.iterate(seed, f), Optional.empty(),Optional.empty());

    }

    /**
     * Unfold a function into a ReactiveSeq
     * 
     * <pre>
     * {@code 
     *  ReactiveSeq.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</code>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return ReactiveSeq generated by unfolder function
     */
    static <U, T> ReactiveSeq<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.fromStream(Seq.unfold(seed, unfolder));
    }

    /**
     * @see Stream#generate(Supplier)
     */
    static <T> ReactiveSeq<T> generate(final Supplier<T> s) {
        return StreamUtils.reactiveSeq(Stream.generate(s), Optional.empty(),Optional.empty());

    }

    /**
     * Unzip a zipped Stream
     * 
     * <pre>
     * {@code 
     *  unzip(ReactiveSeq.of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c")))
     *  
     *  // ReactiveSeq[1,2,3], ReactiveSeq[a,b,c]
     * }
     * 
     * </pre>
     * 
     */
    public static <T, U> Tuple2<ReactiveSeq<T>, ReactiveSeq<U>> unzip(final ReactiveSeq<Tuple2<T, U>> sequence) {
        final Tuple2<ReactiveSeq<Tuple2<T, U>>, ReactiveSeq<Tuple2<T, U>>> tuple2 = sequence.duplicateSequence();
        return new Tuple2(
                          tuple2.v1.map(Tuple2::v1), tuple2.v2.map(Tuple2::v2));
    }

    /**
     * Unzip a zipped Stream into 3
     * 
     * <pre>
     * {@code 
     *    unzip3(ReactiveSeq.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3,"c", 4l)))
     * }
     * // ReactiveSeq[1,2,3], ReactiveSeq[a,b,c], ReactiveSeq[2l,3l,4l]
     * </pre>
     */
    public static <T1, T2, T3> Tuple3<ReactiveSeq<T1>, ReactiveSeq<T2>, ReactiveSeq<T3>> unzip3(final ReactiveSeq<Tuple3<T1, T2, T3>> sequence) {
        final Tuple3<ReactiveSeq<Tuple3<T1, T2, T3>>, ReactiveSeq<Tuple3<T1, T2, T3>>, ReactiveSeq<Tuple3<T1, T2, T3>>> tuple3 = sequence.triplicate();
        return new Tuple3(
                          tuple3.v1.map(Tuple3::v1), tuple3.v2.map(Tuple3::v2), tuple3.v3.map(Tuple3::v3));
    }

    /**
     * Unzip a zipped Stream into 4
     * 
     * <pre>
     * {@code 
     * unzip4(ReactiveSeq.of(new Tuple4(1, "a", 2l,'z'), new Tuple4(2, "b", 3l,'y'), new Tuple4(3,
     * 						"c", 4l,'x')));
     * 		}
     * 		// ReactiveSeq[1,2,3], ReactiveSeq[a,b,c], ReactiveSeq[2l,3l,4l], ReactiveSeq[z,y,x]
     * </pre>
     */
    public static <T1, T2, T3, T4> Tuple4<ReactiveSeq<T1>, ReactiveSeq<T2>, ReactiveSeq<T3>, ReactiveSeq<T4>> unzip4(
            final ReactiveSeq<Tuple4<T1, T2, T3, T4>> sequence) {
        final Tuple4<ReactiveSeq<Tuple4<T1, T2, T3, T4>>, ReactiveSeq<Tuple4<T1, T2, T3, T4>>, ReactiveSeq<Tuple4<T1, T2, T3, T4>>, ReactiveSeq<Tuple4<T1, T2, T3, T4>>> quad = sequence.quadruplicate();
        return new Tuple4(
                          quad.v1.map(Tuple4::v1), quad.v2.map(Tuple4::v2), quad.v3.map(Tuple4::v3), quad.v4.map(Tuple4::v4));
    }



    /**
     * If this ReactiveSeq is empty replace it with a another Stream
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(4,5,6)
     * 							.onEmptySwitch(()->ReactiveSeq.of(1,2,3))
     * 							.toList(),
     * 							equalTo(Arrays.asList(4,5,6)));
     * }
     * </pre>
     * 
     * @param switchTo
     *            Supplier that will generate the alternative Stream
     * @return ReactiveSeq that will switch to an alternative Stream if empty
     */
    @Override
    default ReactiveSeq<T> onEmptySwitch(final Supplier<? extends Stream<T>> switchTo) {
        final AtomicBoolean called = new AtomicBoolean(
                                                       false);
        return ReactiveSeq.fromStream(onEmptyGet((Supplier) () -> {
            called.set(true);
            return switchTo.get();
        }).flatMap(s -> {
            if (called.get())
                return (Stream) s;
            return Stream.of(s);
        }));
    }


    /*
         * (non-Javadoc)
         *
         * @see org.jooq.lambda.Seq#onEmpty(java.lang.Object)
         */
    @Override
    default ReactiveSeq<T> onEmpty(T value){
        return onEmptyGet(()->value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    ReactiveSeq<T> onEmptyGet(Supplier<? extends T> supplier);

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    <X extends Throwable> ReactiveSeq<T> onEmptyThrow(final Supplier<? extends X> supplier);



    <U> ReactiveSeq<T> distinct(Function<? super T, ? extends U> keyExtractor);

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#shuffle(java.util.Random)
     */
    @Override
    ReactiveSeq<T> shuffle(Random random);

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#slice(long, long)
     */
    @Override
    ReactiveSeq<T> slice(long from, long to);

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
     */
    @Override
    <U extends Comparable<? super U>> ReactiveSeq<T> sorted(Function<? super T, ? extends U> function);

    /**
     * emit x elements per time period
     * 
     * <pre>
     * {@code
     * 	code
     * 	SimpleTimer timer = new SimpleTimer();
     * 	ReactiveSeq.of(1, 2, 3, 4, 5, 6)
     *             .xPer(6, 100000000, TimeUnit.NANOSECONDS)
     *             .collect(Collectors.toList())
     *             .size();
     * //6            
     * 
     * }
     * </pre>
     * 
     * @param x
     *            number of elements to emit
     * @param time
     *            period
     * @param t
     *            Time unit
     * @return ReactiveSeq that emits x elements per time period
     */
    ReactiveSeq<T> xPer(int x, long time, TimeUnit t);

    /**
     * emit one element per time period
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.iterate("", last -> "next")
     * 				.limit(100)
     * 				.batchBySize(10)
     * 				.onePer(1, TimeUnit.MICROSECONDS)
     * 				.peek(batch -> System.out.println("batched : " + batch))
     * 				.flatMap(Collection::stream)
     * 				.peek(individual -> System.out.println("Flattened : "
     * 						+ individual))
     * 				.forEach(a->{});
     * }
     * @param time period
     * @param t Time unit
     * @return ReactiveSeq that emits 1 element per time period
     */
    ReactiveSeq<T> onePer(long time, TimeUnit t);

    /**
     * Allow one element through per time period, drop all other elements in
     * that time period
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3,4,5,6)
     *          .debounce(1000,TimeUnit.SECONDS).toList();
     *          
     * // 1 
     * }
     * </pre>
     * 
     * @param time Time to apply debouncing over
     * @param t Time unit for debounce period
     * @return ReactiveSeq with debouncing applied
     */
    ReactiveSeq<T> debounce(long time, TimeUnit t);

    /**
     * emit elements after a fixed delay
     * 
     * <pre>
     * {@code
     * 	SimpleTimer timer = new SimpleTimer();
     * 	ReactiveSeq.of(1, 2, 3, 4, 5, 6)
     *             .fixedDelay(10000, TimeUnit.NANOSECONDS)
     *             .collect(Collectors.toList())
     *             .size();
     *  //6           
     * 	assertThat(timer.getElapsedNanoseconds(), greaterThan(60000l));
     * }
     * </pre>
     * 
     * @param l
     *            time length in nanos of the delay
     * @param unit
     *            for the delay
     * @return ReactiveSeq that emits each element after a fixed delay
     */
    ReactiveSeq<T> fixedDelay(long l, TimeUnit unit);

    /**
     * Introduce a random jitter / time delay between the emission of elements
     * 
     * <pre>
     * { @code
     * 	SimpleTimer timer = new SimpleTimer();
     * 	ReactiveSeq.of(1, 2, 3, 4, 5, 6)
     *             .jitter(10000)
     *             .collect(Collectors.toList());
     *             
     * 	assertThat(timer.getElapsedNanoseconds(), greaterThan(20000l));
     * }
     * </pre>
     * 
     * @param maxJitterPeriodInNanos
     *            - random number less than this is used for each jitter
     * @return ReactiveSeq with a random jitter between element emission
     */
    ReactiveSeq<T> jitter(long maxJitterPeriodInNanos);

    /**
     * Recover from an exception with an alternative value
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4)
     * 						   .map(i->i+2)
     * 						   .map(u->{throw new RuntimeException();})
     * 						   .recover(e->"hello")
     * 						   .firstValue(),equalTo("hello"));
     * }
     * </pre>
     * 
     * @param fn
     *            Function that accepts a Throwable and returns an alternative
     *            value
     * @return ReactiveSeq that can recover from an Exception
     */
    ReactiveSeq<T> recover(final Function<Throwable, ? extends T> fn);

    /**
     * Recover from a particular exception type
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4)
     * 					.map(i->i+2)
     * 					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
     * 					.recover(IOException.class,e->"hello")
     * 					.firstValue(),equalTo("hello"));
     * 
     * }
     * </pre>
     * 
     * @param exceptionClass
     *            Type to recover from
     * @param fn
     *            That accepts an error and returns an alternative value
     * @return ReactiveSeq that can recover from a particular exception
     */
    <EX extends Throwable> ReactiveSeq<T> recover(Class<EX> exceptionClass, final Function<EX, ? extends T> fn);

    /**
     * Retry a transformation if it fails. Default settings are to retry up to 7
     * times, with an doubling backoff period starting @ 2 seconds delay before
     * retry.
     * 
     * <pre>
     * {@code 
     * given(serviceMock.apply(anyInt())).willThrow(
     * 				new RuntimeException(new SocketException("First")),
     * 				new RuntimeException(new IOException("Second"))).willReturn(
     * 				"42");
     * 
     * 	
     * 		String result = ReactiveSeq.of( 1,  2, 3)
     * 				.retry(serviceMock)
     * 				.firstValue();
     * 
     * 		//result = 42
     * }
     * </pre>
     * 
     * @param fn
     *            Function to retry if fails
     * 
     */
    default <R> ReactiveSeq<R> retry(final Function<? super T, ? extends R> fn) {
        return retry(fn, 7, 2, TimeUnit.SECONDS);
    }

    /**
     * Retry a transformation if it fails. Retries up to <b>retries</b>
     * times, with an doubling backoff period starting @ <b>delay</b> TimeUnits delay before
     * retry.
     * 
     * <pre>
     * {@code 
     * given(serviceMock.apply(anyInt())).willThrow(
     * 				new RuntimeException(new SocketException("First")),
     * 				new RuntimeException(new IOException("Second"))).willReturn(
     * 				"42");
     * 
     * 	
     * 		String result = ReactiveSeq.of( 1,  2, 3)
     * 				.retry(serviceMock, 7, 2, TimeUnit.SECONDS)
     * 				.firstValue();
     * 
     * 		//result = 42
     * }
     * </pre>
     * 
     * @param fn
     *            Function to retry if fails
     * @param retries 
     *            Number of retries
     * @param delay
     *            Delay in TimeUnits
     * @param timeUnit
     *            TimeUnit to use for delay
     */
    default <R> ReactiveSeq<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        final Function<T, R> retry = t -> {
            int count = retries;
            final long[] sleep = { timeUnit.toMillis(delay) };
            Throwable exception = null;
            while (count-- > 0) {
                ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep[0]))
                                 .run();
                try {
                    return fn.apply(t);
                } catch (final Throwable e) {
                    exception = e;
                }

                sleep[0] = sleep[0] * 2;
            }
            ExceptionSoftener.throwSoftenedException(exception);
            return null;
        };
        return map(retry);
    }

    /**
     * Remove all occurances of the specified element from the ReactiveSeq
     * 
     * <pre>
     * {@code
     * 	ReactiveSeq.of(1,2,3,4,5,1,2,3).remove(1)
     * 
     *  //Streamable[2,3,4,5,2,3]
     * }
     * </pre>
     * 
     * @param t
     *            element to remove
     * @return Filtered Stream / ReactiveSeq
     */
    default ReactiveSeq<T> remove(final T t) {
        return this.filter(v -> v != t);
    }

    /**
     * Generate the permutations based on values in the ReactiveSeq Makes use of
     * Streamable to store intermediate stages in a collection
     * 
     * 
     * @return Permutations from this ReactiveSeq
     */
    @Override
    default ReactiveSeq<ReactiveSeq<T>> permutations() {
        final Streamable<Streamable<T>> streamable = Streamable.fromStream(this)
                                                               .permutations();
        return streamable.map(s -> s.reactiveSeq())
                         .reactiveSeq();
    }

    /**
     * Return a Stream with elements before the provided start index removed,
     * and elements after the provided end index removed
     * 
     * <pre>
     * {@code 
     *   ReactiveSeq.of(1,2,3,4,5,6).subStream(1,3);
     *   
     *   
     *   //ReactiveSeq[2,3]
     * }
     * </pre>
     * 
     * @param start
     *            index inclusive
     * @param end
     *            index exclusive
     * @return Sequence between supplied indexes of original Sequence
     */
    default ReactiveSeq<T> subStream(final int start, final int end) {
        return this.limit(end)
                   .deleteBetween(0, start);
    }

    /**
     * <pre>
     * {@code
     *   ReactiveSeq.of(1,2,3).combinations(2)
     *   
     *   //ReactiveSeq[ReactiveSeq[1,2],ReactiveSeq[1,3],ReactiveSeq[2,3]]
     * }
     * </pre>
     * 
     * 
     * @param size
     *            of combinations
     * @return All combinations of the elements in this stream of the specified
     *         size
     */
    @Override
    default ReactiveSeq<ReactiveSeq<T>> combinations(final int size) {
        final Streamable<Streamable<T>> streamable = Streamable.fromStream(this)
                                                               .combinations(size);
        return streamable.map(s -> s.reactiveSeq())
                         .reactiveSeq();
    }

    /**
     * <pre>
     * {@code
     *   ReactiveSeq.of(1,2,3).combinations()
     *   
     *   //ReactiveSeq[ReactiveSeq[],ReactiveSeq[1],ReactiveSeq[2],ReactiveSeq[3].ReactiveSeq[1,2],ReactiveSeq[1,3],ReactiveSeq[2,3]
     *   			,ReactiveSeq[1,2,3]]
     * }
     * </pre>
     * 
     * 
     * @return All combinations of the elements in this stream
     */
    @Override
    default ReactiveSeq<ReactiveSeq<T>> combinations() {
        final Streamable<Streamable<T>> streamable = Streamable.fromStream(this)
                                                               .combinations();
        return streamable.map(s -> s.reactiveSeq())
                         .reactiveSeq();
    }

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run at 8PM every night
     *  ReactiveSeq.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            .schedule("0 20 * * *",Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code
     
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
     *                                          .schedule("0 20 * * *", Executors.newScheduledThreadPool(1));
     * 
     *  data.connect().forEach(this::logToDB);
     * }
     * </pre>
     * 
     * 
     * 
     * @param cron
     *            Expression that determines when each job will run
     * @param ex
     *            ScheduledExecutorService
     * @return Connectable HotStream of output from scheduled Stream
     */
    @Override
    HotStream<T> schedule(String cron, ScheduledExecutorService ex);

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run every 60 seconds after last job completes
     *  ReactiveSeq.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            .scheduleFixedDelay(60_000,Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
     *          .scheduleFixedDelay(60_000, Executors.newScheduledThreadPool(1));
     * 
     *  data.connect().forEach(this::logToDB);

     * }
     * </pre>
     * 
     * 
     * @param delay
     *            Between last element completes passing through the Stream
     *            until the next one starts
     * @param ex
     *            ScheduledExecutorService
     * @return Connectable HotStream of output from scheduled Stream
     */
    @Override
    HotStream<T> scheduleFixedDelay(long delay, ScheduledExecutorService ex);

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run every 60 seconds
     *  ReactiveSeq.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            .scheduleFixedRate(60_000,Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
     *          .scheduleFixedRate(60_000, Executors.newScheduledThreadPool(1));
     * 
     *  data.connect().forEach(this::logToDB);
     * }
     * </pre>
     * 
     * @param rate
     *            Time in millis between job runs
     * @param ex
     *            ScheduledExecutorService
     * @return Connectable HotStream of output from scheduled Stream
     */
    @Override
    HotStream<T> scheduleFixedRate(long rate, ScheduledExecutorService ex);

    /**
     * [equivalent to count]
     * 
     * @return size
     */
    default int size() {
        return this.toList()
                   .size();
    }
    /**
     * Perform a four level nested internal iteration over this Stream and the
     * supplied streams
     *
     * <pre>
     * {@code 
     *   
     *   //ReactiveSeq [1,2]
     *   
     *   reactiveSeq.forEach4(a->ListX.range(10,13),
     *                        (a,b)->ListX.of(""+(a+b),"hello world"),
     *                        (a,b,c)->ListX.of(a,b,c)),
     *                        (a,b,c,d)->c+":"a+":"+b);
     *                                  

     * }
     * </pre>
     * 
     * @param stream1
     *            Nested Stream to iterate over
     * @param stream2
     *            Nested Stream to iterate over
     * @param stream3
     *            Nested Stream to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R2, R3,R> ReactiveSeq<R> forEach4(final Function<? super T, ? extends BaseStream<R1, ?>> stream1,
                        final BiFunction<? super T,? super R1, ? extends BaseStream<R2, ?>> stream2,
                            final F3<? super T, ? super R1, ? super R2, ? extends BaseStream<R3, ?>> stream3,
                            final F4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){
        return this.flatMap(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(()->stream2.apply(in, ina).iterator());
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = ReactiveSeq.fromIterable(()->stream3.apply(in, ina, inb).iterator());
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }
    /**
     * Perform a four level nested internal iteration over this Stream and the
     * supplied streams
     * 
     * <pre>
     * {@code 
     *  //ReactiveSeq [1,2,3]
     *  
     * seq.forEach4(a->ReactiveSeq.range(10,13),
     *                     (a,b)->Stream.of(""+(a+b),"hello world"),
     *                     (a,b,c)->Stream.of(a,b,c),
     *                     (a,b,c,d)-> c!=3,
     *                      (a,b,c)->c+":"a+":"+b);
     *                                  
     * 
     *  
     * }
     * </pre>
     * 
     * 
     * @param stream1
     *            Nested Stream to iterate over
     * @param stream2
     *            Nested Stream to iterate over
     * @param stream3
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R2, R3, R> ReactiveSeq<R> forEach4(final Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            final BiFunction<? super T, ? super R1, ? extends BaseStream<R2, ?>> stream2,
            final F3<? super T, ? super R1, ? super R2, ? extends BaseStream<R3, ?>> stream3,
            final F4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            final F4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){

        return this.flatMap(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(()->stream2.apply(in, ina).iterator());
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = ReactiveSeq.fromIterable(()->stream3.apply(in, ina, inb).iterator());
                    return c.filter(in2 -> filterFunction.apply(in, ina, inb, in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }
    /**
     * Perform a three level nested internal iteration over this Stream and the
     * supplied streams
     *
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2)
     *                      .forEach3(a->IntStream.range(10,13),
     *                               (a,b)->Stream.of(""+(a+b),"hello world"),
     *                               (a,b,c)->c+":"a+":"+b);
     *                                  
     * 
     *  //ReactiveSeq[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
     * </pre>
     * 
     * @param stream1
     *            Nested Stream to iterate over
     * @param stream2
     *            Nested Stream to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R2, R> ReactiveSeq<R> forEach3(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            BiFunction<? super T,? super R1, ? extends BaseStream<R2, ?>> stream2,
            F3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){

        return this.flatMap(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return ReactiveSeq.fromIterable(a)
                              .flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(()->stream2.apply(in, ina).iterator());
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });

        });
    }

    /**
     * Perform a three level nested internal iteration over this Stream and the
     * supplied streams
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3)
     *                      .forEach3(a->IntStream.range(10,13),
     *                                (a,b)->Stream.of(""+(a+b),"hello world"),
     *                                (a,b,c)-> c!=3,
     *                                (a,b,c)->c+":"a+":"+b);
     *                                  
     * 
     *  //ReactiveSeq[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
     * </pre>
     * 
     * 
     * @param stream1
     *            Nested Stream to iterate over
     * @param stream2
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
   default <R1, R2, R> ReactiveSeq<R> forEach3(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            BiFunction<? super T,? super R1, ? extends BaseStream<R2, ?>> stream2,
            F3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            F3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){
       return this.flatMap(in -> {

           ReactiveSeq<R1> a = ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
           return ReactiveSeq.fromIterable(a)
                             .flatMap(ina -> {
               ReactiveSeq<R2> b = ReactiveSeq.fromIterable(()->stream2.apply(in, ina).iterator());
               return b.filter(in2 -> filterFunction.apply(in, ina, in2))
                       .map(in2 -> yieldingFunction.apply(in, ina, in2));
           });

       });
    }

    /**
     * Perform a two level nested internal iteration over this Stream and the
     * supplied stream
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3)
     *                      .forEach2(a->IntStream.range(10,13),
     *                                (a,b)->a+b);
     *                                  
     * 
     *  //ReactiveSeq[11,14,12,15,13,16]
     * }
     * </pre>
     * 
     * 
     * @param stream1
     *            Nested Stream to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R> ReactiveSeq<R> forEach2(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            BiFunction<? super T,? super R1, ? extends R> yieldingFunction){
        return this.flatMap(in-> { 
            
            
            ReactiveSeq<R1> b = ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }

    /**
     * Perform a two level nested internal iteration over this Stream and the
     * supplied stream
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3)
     *                      .forEach2(a->IntStream.range(10,13),
     *                                (a,b)-> a<3 && b>10,
     *                                (a,b)->a+b);
     *                                  
     * 
     *  //ReactiveSeq[14,15]
     * }
     * </pre>
     * 
     * @param stream1
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R> ReactiveSeq<R> forEach2(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            BiFunction<? super T,? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction){
        return this.flatMap(in-> { 
            
            
            ReactiveSeq<R1> b = ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return b.filter(in2-> filterFunction.apply(in,in2))
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }




    @Override
    default Optional<T> max(final Comparator<? super T> comparator) {

        return StreamUtils.max(this, comparator);

    }

    /**
     * Returns the count of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a> and is
     * equivalent to:
     * <pre>{@code
     *     return mapToLong(e -> 1L).sum();
     * }</pre>
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the count of elements in this stream
     */
    @Override
    default long count() {
        return seq().count();
    }

    @Override
    default Optional<T> min(final Comparator<? super T> comparator) {
        return StreamUtils.min(this, comparator);
    }

    @Override
    default void printErr() {

        seq().printErr();
    }

    @Override
    default void print(final PrintWriter writer) {

        seq().print(writer);
    }

    @Override
    default void print(final PrintStream stream) {

        seq().print(stream);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#append(java.util.stream.Stream)
     */
    default ReactiveSeq<T> appendS(Stream<? extends T> other) {
        
        return fromStream(seq().append(other));
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#append(java.lang.Iterable)
     */
    default ReactiveSeq<T> append(Iterable<? extends T> other) {
        
        return fromStream(seq().append(other));
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#prepend(java.util.stream.Stream)
     */
    default ReactiveSeq<T> prependS(Stream<? extends T> other) {
        
        return fromStream(seq().prepend(other));
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#prepend(java.lang.Iterable)
     */
    default ReactiveSeq<T> prepend(Iterable<? extends T> other) {
        
        return fromStream(seq().prepend(other));
    }


  

    ReactiveSeq<T> cycle(long times);
  

    ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate);

    ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate);
    <U> ReactiveSeq<T> sorted(Function<? super T, ? extends U> function, Comparator<? super U> comparator);


  

    
}
