package com.aol.cyclops.control;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;
import org.reactivestreams.Subscription;

import com.aol.cyclops.CyclopsCollectors;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.internal.stream.FutureStreamUtils;
import com.aol.cyclops.internal.stream.PausableHotStreamImpl;
import com.aol.cyclops.internal.stream.ReactiveSeqFutureOpterationsImpl;
import com.aol.cyclops.internal.stream.ReactiveSeqImpl;
import com.aol.cyclops.internal.stream.ReversedIterator;
import com.aol.cyclops.internal.stream.SeqUtils;
import com.aol.cyclops.internal.stream.operators.BatchBySizeOperator;
import com.aol.cyclops.internal.stream.operators.BatchByTimeAndSizeOperator;
import com.aol.cyclops.internal.stream.operators.BatchByTimeOperator;
import com.aol.cyclops.internal.stream.operators.BatchWhileOperator;
import com.aol.cyclops.internal.stream.operators.DebounceOperator;
import com.aol.cyclops.internal.stream.operators.LimitLastOperator;
import com.aol.cyclops.internal.stream.operators.LimitWhileOperator;
import com.aol.cyclops.internal.stream.operators.LimitWhileTimeOperator;
import com.aol.cyclops.internal.stream.operators.MultiReduceOperator;
import com.aol.cyclops.internal.stream.operators.OnePerOperator;
import com.aol.cyclops.internal.stream.operators.RecoverOperator;
import com.aol.cyclops.internal.stream.operators.SkipLastOperator;
import com.aol.cyclops.internal.stream.operators.SkipWhileOperator;
import com.aol.cyclops.internal.stream.operators.SkipWhileTimeOperator;
import com.aol.cyclops.internal.stream.operators.WindowStatefullyWhileOperator;
import com.aol.cyclops.internal.stream.spliterators.ReversableSpliterator;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.anyM.Witness.stream;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.NonPausableHotStream;
import com.aol.cyclops.types.stream.PausableHotStream;
import com.aol.cyclops.types.stream.future.FutureOperations;
import com.aol.cyclops.util.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * Static utility methods for working with Java  8 Streams
 * 
 * @author johnmcclean
 *
 */
@UtilityClass
public class StreamUtils {

    /**
     * Create an Optional containing a List materialized from a Stream
     * 
     * <pre>
     * {@code 
     *   Optional<ListX<Integer>> opt = StreamUtils.streamToOptional(Stream.of(1,2,3));
     *   
     *   //Optional[[1,2,3]]
     * 
     * }
     * </pre>
     * 
     * 
     * @param stream To convert into an Optional
     * @return Optional with a List of values
     */
    public final static <T> Optional<ListX<T>> streamToOptional(final Stream<T> stream) {
        
        final List<T> collected = stream.collect(Collectors.toList());
        if (collected.size() == 0)
            return Optional.empty();
        return Optional.of(ListX.fromIterable(collected));
    }

    /**
     * Convert an Optional to a Stream
     * 
     * <pre>
     * {@code 
     *     Stream<Integer> stream = StreamUtils.optionalToStream(Optional.of(1));
     *     //Stream[1]
     *     
     *     Stream<Integer> empty = StreamUtils.optionalToStream(Optional.empty());
     *     //Stream[]
     * }
     * </pre>
     * 
     * @param optional Optional to convert to a Stream
     * @return Stream with a single value (if present) created from an Optional
     */
    public final static <T> Stream<T> optionalToStream(final Optional<T> optional) {
        if (optional.isPresent())
            return Stream.of(optional.get());
        return Stream.of();
    }

    /**
     * Create a CompletableFuture containing a List materialized from a Stream
     * 
     * @param stream To convert into an Optional
     * @return CompletableFuture with a List of values
     */
    public final static <T> CompletableFuture<List<T>> streamToCompletableFuture(final Stream<T> stream) {
        return CompletableFuture.completedFuture(stream.collect(CyclopsCollectors.toListX()));

    }

    /**
     * Convert a CompletableFuture to a Stream
     * 
     * @param future CompletableFuture to convert
     * @return  Stream with a single value created from a CompletableFuture
     */
    public final static <T> Stream<T> completableFutureToStream(final CompletableFuture<T> future) {
        return Stream.of(future.join());

    }

    /**
     * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
     * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * 
     * <pre>
     * @{code
     *     Subscription next = StreamUtils.forEachX(Stream.of(1,2,3,4),2,System.out::println);
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     3
     *     4 
     *     Second batch processed!
     * }
     * </pre>
     * 
     * @param Stream - the Stream to consume data from
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming events from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Subscription forEachX(final Stream<T> stream, final long x, final Consumer<? super T> consumerElement) {
        val t2 = FutureStreamUtils.forEachX(stream, x, consumerElement);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription 
     * <pre>
     * @{code
     *     Subscription next = StreamUtils.forEachXWithError(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get),System.out::println, e->e.printStackTrace());
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     
     *     RuntimeException Stack Trace on System.err
     *     
     *     4 
     *     Second batch processed!
     * }
     * </pre>	 
     * 
     * @param Stream - the Stream to consume data from
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Subscription forEachXWithError(final Stream<T> stream, final long x,
            final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        val t2 = FutureStreamUtils.forEachXWithError(stream, x, consumerElement, consumerError);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * @{code
     *     Subscription next = StreamUtils.forEachXEvents(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get) ,System.out::println, e->e.printStackTrace(),()->System.out.println("the end!"));
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     
     *     RuntimeException Stack Trace on System.err
     *     
     *     4 
     *     Second batch processed!
     *     The end!
     * }
     * </pre>
     * @param Stream - the Stream to consume data from	 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Subscription forEachXEvents(final Stream<T> stream, final long x,
            final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        val t2 = FutureStreamUtils.forEachXEvents(stream, x, consumerElement, consumerError, onComplete);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,  
     * <pre>
     * @{code
     *     Subscription next = StreanUtils.forEachWithError(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get),System.out::println, e->e.printStackTrace());
     *          
     *     System.out.println("processed!");
     *     
     *    
     *      
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *     4
     *     processed!
     *     
     * }
     * </pre>
     * @param Stream - the Stream to consume data from	 
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     */
    public static <T, X extends Throwable> void forEachWithError(final Stream<T> stream, final Consumer<? super T> consumerElement,
            final Consumer<? super Throwable> consumerError) {

        val t2 = FutureStreamUtils.forEachWithError(stream, consumerElement, consumerError);
        t2.v2.run();

    }

    /**
     * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * @{code
     *     Subscription next = StreamUtils.forEachEvents(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get),System.out::println, e->e.printStackTrace(),()->System.out.println("the end!"));
     *          
     *     System.out.println("processed!");
     *     
     *      
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *      4 
     *     processed!
     *     
     *     
     * }
     * </pre>
     * @param Stream - the Stream to consume data from	
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> void forEachEvent(final Stream<T> stream, final Consumer<? super T> consumerElement,
            final Consumer<? super Throwable> consumerError, final Runnable onComplete) {

        val t2 = FutureStreamUtils.forEachEvent(stream, consumerElement, consumerError, onComplete);
        t2.v2.run();

    }

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run at 8PM every night
     * StreamUtils.schedule(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            ,"0 20 * * *",Executors.newScheduledThreadPool(1)));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code 
     * HotStream<Data> dataStream = StreamUtils.schedule(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            							  .map(this::processJob)
     *            							  ,"0 20 * * *",Executors.newScheduledThreadPool(1)));
     * 
     * 
     * data.connect().forEach(this::logToDB);
     * }
     * </pre>
     * 
     * 
     * @param stream the stream to schedule element processing on
     * @param cron Expression that determines when each job will run
     * @param ex ScheduledExecutorService
     * @return Connectable HotStream of output from scheduled Stream
     */
    public static <T> HotStream<T> schedule(final Stream<T> stream, final String cron, final ScheduledExecutorService ex) {
        return new NonPausableHotStream<>(
                                          stream).schedule(cron, ex);
    }

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run every 60 seconds after last job completes
     *  StreamUtils.scheduleFixedDelay(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            ,60_000,Executors.newScheduledThreadPool(1)));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code 
     * HotStream<Data> dataStream = StreamUtils.scheduleFixedDelay(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            							  .map(this::processJob)
     *            							  ,60_000,Executors.newScheduledThreadPool(1)));
     * 
     * 
     * data.connect().forEach(this::logToDB);
     * }
     * </pre>
     * 
     * 
     * @param stream the stream to schedule element processing on
     * @param delay Between last element completes passing through the Stream until the next one starts
     * @param ex ScheduledExecutorService
     * @return Connectable HotStream of output from scheduled Stream
     */
    public static <T> HotStream<T> scheduleFixedDelay(final Stream<T> stream, final long delay, final ScheduledExecutorService ex) {
        return new NonPausableHotStream<>(
                                          stream).scheduleFixedDelay(delay, ex);
    }

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run every 60 seconds
     *  StreamUtils.scheduleFixedRate(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob),
     *            60_000,Executors.newScheduledThreadPool(1)));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code 
     * HotStream<Data> dataStream = StreamUtils.scheduleFixedRate(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            							  .map(this::processJob)
     *            							  ,60_000,Executors.newScheduledThreadPool(1)));
     * 
     * 
     * data.connect().forEach(this::logToDB);
     * }
     * </pre>
     * @param stream the stream to schedule element processing on
     * @param rate Time in millis between job runs
     * @param ex ScheduledExecutorService
     * @return Connectable HotStream of output from scheduled Stream
     */
    public static <T> HotStream<T> scheduleFixedRate(final Stream<T> stream, final long rate, final ScheduledExecutorService ex) {
        return new NonPausableHotStream<>(
                                          stream).scheduleFixedRate(rate, ex);
    }

    /**
     * Split at supplied location 
     * <pre>
     * {@code 
     * ReactiveSeq.of(1,2,3).splitAt(1)
     * 
     *  //SequenceM[1], SequenceM[2,3]
     * }
     * 
     * </pre>
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> splitAt(final Stream<T> stream, final int where) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicate(stream);
        return new Tuple2(
                          Tuple2.v1.limit(where), Tuple2.v2.skip(where));
    }

    /**
     * Split stream at point where predicate no longer holds
     * <pre>
     * {@code
     *   ReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
     *   
     *   //SequenceM[1,2,3] SequenceM[4,5,6]
     * }
     * </pre>
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> splitBy(final Stream<T> stream, final Predicate<T> splitter) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicate(stream);
        return new Tuple2(
                          limitWhile(Tuple2.v1, splitter), skipWhile(Tuple2.v2, splitter));
    }

    /**
     * Partition a Stream into two one a per element basis, based on predicate's boolean value
     * <pre>
     * {@code 
     *  ReactiveSeq.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0) 
     *  
     *  //SequenceM[1,3,5], SequenceM[2,4,6]
     * }
     *
     * </pre>
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> partition(final Stream<T> stream, final Predicate<T> splitter) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicate(stream);
        return new Tuple2(
                          Tuple2.v1.filter(splitter), Tuple2.v2.filter(splitter.negate()));
    }

    /**
     * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     * <pre>
     * {@code 
     *  Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
    	 assertTrue(copies.v1.anyMatch(i->i==2));
    	 assertTrue(copies.v2.anyMatch(i->i==2));
     * 
     * }
     * </pre>
     * 
     * @return duplicated stream
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> duplicate(final Stream<T> stream) {

        final Tuple2<Iterator<T>, Iterator<T>> Tuple2 = StreamUtils.toBufferingDuplicator(stream.iterator());
        return new Tuple2(
                          StreamUtils.stream(Tuple2.v1()), StreamUtils.stream(Tuple2.v2()));
    }

    private final static <T> Tuple2<Stream<T>, Stream<T>> duplicatePos(final Stream<T> stream, final int pos) {

        final Tuple2<Iterator<T>, Iterator<T>> Tuple2 = StreamUtils.toBufferingDuplicator(stream.iterator(), pos);
        return new Tuple2(
                          StreamUtils.stream(Tuple2.v1()), StreamUtils.stream(Tuple2.v2()));
    }

    /**
     * Triplicates a Stream
     * Buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     * <pre>
     * {@code 
     * 	Tuple3<ReactiveSeq<Tuple3<T1,T2,T3>>,ReactiveSeq<Tuple3<T1,T2,T3>>,ReactiveSeq<Tuple3<T1,T2,T3>>> Tuple3 = sequence.triplicate();
    
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public final static <T> Tuple3<Stream<T>, Stream<T>, Stream<T>> triplicate(final Stream<T> stream) {

        final Stream<Stream<T>> its = StreamUtils.toBufferingCopier(stream.iterator(), 3)
                                                 .stream()
                                                 .map(it -> StreamUtils.stream(it));
        final Iterator<Stream<T>> it = its.iterator();
        return new Tuple3(
                          it.next(), it.next(), it.next());

    }

    /**
     * Makes four copies of a Stream
     * Buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe. 
     * 
     * <pre>
     * {@code
     * 
     * 		Tuple4<ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();
    
     * }
     * </pre>
     * @return
     */
    @SuppressWarnings("unchecked")
    public final static <T> Tuple4<Stream<T>, Stream<T>, Stream<T>, Stream<T>> quadruplicate(final Stream<T> stream) {
        final Stream<Stream<T>> its = StreamUtils.toBufferingCopier(stream.iterator(), 4)
                                                 .stream()
                                                 .map(it -> StreamUtils.stream(it));
        final Iterator<Stream<T>> it = its.iterator();
        return new Tuple4(
                          it.next(), it.next(), it.next(), it.next());
    }

    /**
     * Append Stream to this SequenceM
     * 
     * <pre>
     * {@code 
     * List<String> result = 	of(1,2,3).appendStream(of(100,200,300))
    									.map(it ->it+"!!")
    									.collect(Collectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     * }
     * </pre>
     * 
     * @param stream to append
     * @return SequenceM with Stream appended
     */
    public static final <T> Stream<T> appendStream(final Stream<T> stream1, final Stream<T> append) {
        return Stream.concat(stream1, append);
    }

    /**
     * Prepend Stream to this SequenceM
     * 
     * <pre>
     * {@code 
     * List<String> result = of(1,2,3).prependStream(of(100,200,300))
    			.map(it ->it+"!!").collect(Collectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * 
     * }
     * </pre>
     * 
     * @param stream to Prepend
     * @return SequenceM with Stream prepended
     */
    public static final <T> Stream<T> prependStream(final Stream<T> stream1, final Stream<T> prepend) {

        return Stream.concat(prepend, stream1);
    }

    /**
     * Append values to the end of this SequenceM
     * <pre>
     * {@code 
     * List<String> result = 	of(1,2,3).append(100,200,300)
    									.map(it ->it+"!!")
    									.collect(Collectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     * }
     * </pre>
     * @param values to append
     * @return SequenceM with appended values
     */
    public static final <T> Stream<T> append(final Stream<T> stream, final T... values) {
        return appendStream(stream, Stream.of(values));
    }

    /**
     * Prepend given values to the start of the Stream
     * <pre>
     * {@code 
     * List<String> result = 	of(1,2,3).prepend(100,200,300)
    			.map(it ->it+"!!").collect(Collectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * @param values to prepend
     * @return SequenceM with values prepended
     */
    public static final <T> Stream<T> prepend(final Stream<T> stream, final T... values) {
        return appendStream(Stream.of(values), stream);
    }

    /**
     * Insert data into a stream at given position
     * <pre>
     * {@code 
     * List<String> result = 	of(1,2,3).insertAt(1,100,200,300)
    			.map(it ->it+"!!").collect(Collectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
     * 
     * }
     * </pre>
     * @param pos to insert data at
     * @param values to insert
     * @return Stream with new data inserted
     */
    public static final <T> Stream<T> insertAt(final Stream<T> stream, final int pos, final T... values) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicatePos(stream, pos);
        return appendStream(append(Tuple2.v1.limit(pos), values), Tuple2.v2.skip(pos));
    }

    /**
     * Delete elements between given indexes in a Stream
     * <pre>
     * {@code 
     * List<String> result = 	StreamUtils.deleteBetween(Stream.of(1,2,3,4,5,6),2,4)
    										.map(it ->it+"!!")
    										.collect(Collectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
     * }
     * </pre>
     * @param start index
     * @param end index
     * @return Stream with elements removed
     */
    public static final <T> Stream<T> deleteBetween(final Stream<T> stream, final int start, final int end) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicatePos(stream, start);
        return appendStream(Tuple2.v1.limit(start), Tuple2.v2.skip(end));
    }

    /**
     * Insert a Stream into the middle of this stream at the specified position
     * <pre>
     * {@code 
     * List<String> result = 	StreamUtils.insertStreamAt(Stream.of(1,2,3),1,of(100,200,300))
    										.map(it ->it+"!!")
    										.collect(Collectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
     * }
     * </pre>
     * @param pos to insert Stream at
     * @param stream to insert
     * @return newly conjoined SequenceM
     */
    public static final <T> Stream<T> insertStreamAt(final Stream<T> stream1, final int pos, final Stream<T> insert) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicatePos(stream1, pos);

        return appendStream(appendStream(Tuple2.v1.limit(pos), insert), Tuple2.v2.skip(pos));
    }

    /**
     * Convert to a Stream with the result of a reduction operation repeated
     * specified times
     * 
     * <pre>
     * {@code 
     *   		List<Integer> list = StreamUtils.cycle(Stream.of(1,2,2),Reducers.toCountInt(),3)
     * 										.
     * 										.collect(Collectors.toList());
     * 	//is asList(3,3,3);
     *   }
     * </pre>
     * 
     * @param m
     *            Monoid to be used in reduction
     * @param times
     *            Number of times value should be repeated
     * @return Stream with reduced values repeated
     */
    public final static <T> Stream<T> cycle(final Stream<T> stream, final Monoid<T> m, final int times) {
        return StreamUtils.cycle(times, Streamable.fromObject(m.reduce(stream)));

    }

    /**
     * extract head and tail together
     * 
     * <pre>
     * {@code 
     *  Stream<String> helloWorld = Stream.of("hello","world","last");
    	HeadAndTail<String> headAndTail = StreamUtils.headAndTail(helloWorld);
    	 String head = headAndTail.head();
    	 assertThat(head,equalTo("hello"));
    	
    	ReactiveSeq<String> tail =  headAndTail.tail();
    	assertThat(tail.headAndTail().head(),equalTo("world"));
     * }
     * </pre>
     * 
     * @return
     */
    public final static <T> HeadAndTail<T> headAndTail(final Stream<T> stream) {
        final Iterator<T> it = stream.iterator();
        return new HeadAndTail<>(
                                 it);
    }

    /**
     * skip elements in Stream until Predicate holds true
     * 	<pre>
     * {@code  StreamUtils.skipUntil(Stream.of(4,3,6,7),i->i==6).collect(Collectors.toList())
     *  // [6,7]
     *  }</pre>
    
     * @param stream Stream to skip elements from 
     * @param predicate to apply
     * @return Stream with elements skipped
     */
    public static <U> Stream<U> skipUntil(final Stream<U> stream, final Predicate<? super U> predicate) {
        return skipWhile(stream, predicate.negate());
    }

    public static <U> Stream<U> skipLast(final Stream<U> stream, final int num) {
        return new SkipLastOperator<>(
                                      stream, num).skipLast();
    }

    public static <U> Stream<U> limitLast(final Stream<U> stream, final int num) {
        return new LimitLastOperator<>(
                                       stream, num).limitLast();
    }

    public static <T> Stream<T> recover(final Stream<T> stream, final Function<Throwable, ? extends T> fn) {
        return new RecoverOperator<>(
                                     stream, Throwable.class).recover(fn);
    }

    public static <T, EX extends Throwable> Stream<T> recover(final Stream<T> stream, final Class<EX> type, final Function<EX, ? extends T> fn) {
        return new RecoverOperator<T>(
                                      stream, (Class) type).recover((Function) fn);
    }

    /**
     * skip elements in a Stream while Predicate holds true
     * 
     * <pre>
     * 
     * {@code  StreamUtils.skipWhile(Stream.of(4,3,6,7).sorted(),i->i<6).collect(Collectors.toList())
     *  // [6,7]
     *  }</pre>
     * @param stream
     * @param predicate
     * @return
     */
    public static <U> Stream<U> skipWhile(final Stream<U> stream, final Predicate<? super U> predicate) {
        return new SkipWhileOperator<U>(
                                        stream).skipWhile(predicate);
    }

    public static <U> Stream<U> limit(final Stream<U> stream, final long time, final TimeUnit unit) {
        return new LimitWhileTimeOperator<U>(
                                             stream).limitWhile(time, unit);
    }

    public static <U> Stream<U> skip(final Stream<U> stream, final long time, final TimeUnit unit) {
        return new SkipWhileTimeOperator<U>(
                                            stream).skipWhile(time, unit);
    }

    public static <T> Stream<T> combine(final Stream<T> stream, final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        final Iterator<T> it = stream.iterator();
        final Object UNSET = new Object();
        return StreamUtils.stream(new Iterator<ReactiveSeq<T>>() {
            T current = (T) UNSET;

            @Override
            public boolean hasNext() {
                return it.hasNext() || current != UNSET;
            }

            @Override
            public ReactiveSeq<T> next() {
                while (it.hasNext()) {
                    final T next = it.next();

                    if (current == UNSET) {
                        current = next;

                    } else if (predicate.test(current, next)) {
                        current = op.apply(current, next);

                    } else {
                        final T result = current;
                        current = (T) UNSET;
                        return ReactiveSeq.of(result, next);
                    }
                }
                if (it.hasNext())
                    return ReactiveSeq.empty();
                final T result = current;
                current = (T) UNSET;
                return ReactiveSeq.of(result);
            }

        })
                          .flatMap(Function.identity());
    }

    /**
     * Take elements from a stream while the predicates hold
     * <pre>
     * {@code StreamUtils.limitWhile(Stream.of(4,3,6,7).sorted(),i->i<6).collect(Collectors.toList());
     * //[4,3]
     * }
     * </pre>
     * @param stream
     * @param predicate
     * @return
     */
    public static <U> Stream<U> limitWhile(final Stream<U> stream, final Predicate<? super U> predicate) {
        return new LimitWhileOperator<U>(
                                         stream).limitWhile(predicate);
    }

    /**
     * Take elements from a Stream until the predicate holds
     *  <pre>
     * {@code StreamUtils.limitUntil(Stream.of(4,3,6,7),i->i==6).collect(Collectors.toList());
     * //[4,3]
     * }
     * </pre>
     * @param stream
     * @param predicate
     * @return
     */
    public static <U> Stream<U> limitUntil(final Stream<U> stream, final Predicate<? super U> predicate) {
        return limitWhile(stream, predicate.negate());

    }

    /**
     * Reverse a Stream
     * 
     * <pre>
     * {@code 
     * assertThat(StreamUtils.reverse(Stream.of(1,2,3)).collect(Collectors.toList())
    			,equalTo(Arrays.asList(3,2,1)));
     * }
     * </pre>
     * 
     * @param stream Stream to reverse
     * @return Reversed stream
     */
    public static <U> Stream<U> reverse(final Stream<U> stream) {
        return reversedStream(stream.collect(Collectors.toList()));
    }

    /**
     * Create a reversed Stream from a List
     * <pre>
     * {@code 
     * StreamUtils.reversedStream(asList(1,2,3))
    			.map(i->i*100)
    			.forEach(System.out::println);
    	
    	
    	assertThat(StreamUtils.reversedStream(Arrays.asList(1,2,3)).collect(Collectors.toList())
    			,equalTo(Arrays.asList(3,2,1)));
     * 
     * }
     * </pre>
     * 
     * @param list List to create a reversed Stream from
     * @return Reversed Stream
     */
    public static <U> Stream<U> reversedStream(final List<U> list) {
        return new ReversedIterator<>(
                                      list).stream();
    }

    /**
     * Create a new Stream that infiniteable cycles the provided Stream
     * 
     * <pre>
     * {@code 		
     * assertThat(StreamUtils.cycle(Stream.of(1,2,3))
     * 						.limit(6)
     * 						.collect(Collectors.toList()),
     * 								equalTo(Arrays.asList(1,2,3,1,2,3)));
    	}
     * </pre>
     * @param s Stream to cycle
     * @return New cycling stream
     */
    public static <U> Stream<U> cycle(final Stream<U> s) {
        return cycle(Streamable.fromStream(s));
    }

    /**
     * Create a Stream that infiniteable cycles the provided Streamable
     * @param s Streamable to cycle
     * @return New cycling stream
     */
    public static <U> Stream<U> cycle(final Streamable<U> s) {
        return Stream.iterate(s.stream(), s1 -> s.stream())
                     .flatMap(Function.identity());
    }

    /**
     * Create a Stream that finitely cycles the provided Streamable, provided number of times
     * 
     * <pre>
     * {@code 
     * assertThat(StreamUtils.cycle(3,Streamable.of(1,2,2))
    							.collect(Collectors.toList()),
    								equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
     * }
     * </pre>
     * @param s Streamable to cycle
     * @return New cycling stream
     */
    public static <U> Stream<U> cycle(final int times, final Streamable<U> s) {
        return Stream.iterate(s.stream(), s1 -> s.stream())
                     .limit(times)
                     .flatMap(Function.identity());
    }

    /**
     * Create a stream from an iterable
     * <pre>
     * {@code 
     * 	assertThat(StreamUtils.stream(Arrays.asList(1,2,3))
     * 								.collect(Collectors.toList()),
     * 									equalTo(Arrays.asList(1,2,3)));
    
     * 
     * }
     * </pre>
     * @param it Iterable to convert to a Stream
     * @return Stream from iterable
     */
    public static <U> Stream<U> stream(final Iterable<U> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }

    public static <U> Stream<U> stream(final Spliterator<U> it) {
        return StreamSupport.stream(it, false);
    }

    /**
     * Create a stream from an iterator
     * <pre>
     * {@code 
     * 	assertThat(StreamUtils.stream(Arrays.asList(1,2,3).iterator())	
     * 							.collect(Collectors.toList()),
     * 								equalTo(Arrays.asList(1,2,3)));
    
     * }
     * </pre>
     * @param it Iterator to convert to a Stream
     * @return Stream from iterator
     */
    public static <U> Stream<U> stream(final Iterator<U> it) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
    }

    /**
     * Concat an Object and a Stream
     * If the Object is a Stream, Streamable or Iterable will be converted (or left) in Stream form and concatonated
     * Otherwise a new Stream.of(o) is created
     * 
     * @param o Object to concat
     * @param stream  Stream to concat
     * @return Concatonated Stream
     */
    public static <U> Stream<U> concat(final Object o, final Stream<U> stream) {
        Stream<U> first = null;
        if (o instanceof Stream) {
            first = (Stream) o;
        } else if (o instanceof Iterable) {
            first = stream((Iterable) o);
        } else if (o instanceof Streamable) {
            first = ((Streamable) o).stream();
        } else {
            first = Stream.of((U) o);
        }
        return Stream.concat(first, stream);

    }

    /**
     * Create a stream from a map
     * <pre>
     * {@code 
     * 	Map<String,String> map = new HashMap<>();
    	map.put("hello","world");
    	assertThat(StreamUtils.stream(map).collect(Collectors.toList()),equalTo(Arrays.asList(new AbstractMap.SimpleEntry("hello","world"))));
    
     * }</pre>
     * 
     * 
     * @param it Iterator to convert to a Stream
     * @return Stream from a map
     */
    public final static <K, V> Stream<Map.Entry<K, V>> stream(final Map<K, V> it) {
        return it.entrySet()
                 .stream();
    }

    public final static <T> FutureOperations<T> futureOperations(final Stream<T> stream, final Executor exec) {
        return new ReactiveSeqFutureOpterationsImpl<T>(
                                                       exec, reactiveSeq(stream, Optional.empty()));
    }

    public final static <T> T firstValue(final Stream<T> stream) {
        return stream.findAny()
                     .get();
    }

    /**
     * Simultaneously reduce a stream with multiple reducers
     * 
     * <pre>{@code
     * 
     *  Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
    	Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
    	val result = StreamUtils.reduce(Stream.of(1,2,3,4),Arrays.asList(sum,mult));
    			
    	 
    	assertThat(result,equalTo(Arrays.asList(10,24)));
    	}</pre>
     * 
     * @param stream Stream to reduce
     * @param reducers Reducers to reduce Stream
     * @return Reduced Stream values as List entries
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <R> ListX<R> reduce(final Stream<R> stream, final Iterable<? extends Monoid<R>> reducers) {
        return ListX.fromIterable(new MultiReduceOperator<R>(
                                                             stream).reduce(reducers));

    }

    /**
     * Simultanously reduce a stream with multiple reducers
     * 
     * <pre>
     * {@code 
     *  Monoid<String> concat = Monoid.of("",(a,b)->a+b);
    	Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
    	assertThat(StreamUtils.reduce(Stream.of("hello", "world", "woo!"),Stream.of(concat,join))
    	                 ,equalTo(Arrays.asList("helloworldwoo!",",hello,world,woo!")));
     * }
     * </pre>
     * 
     *  @param stream Stream to reduce
     * @param reducers Reducers to reduce Stream
     * @return Reduced Stream values as List entries
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <R> ListX<R> reduce(final Stream<R> stream, final Stream<? extends Monoid<R>> reducers) {
        return reduce(stream, ListX.fromIterable((List) reducers.collect(Collectors.toList())));

    }

    /**
     * Repeat in a Stream while specified predicate holds
     * <pre>
     * {@code 
     *  int count =0;
     *  
    	assertThat(StreamUtils.cycleWhile(Stream.of(1,2,2)
    										,next -> count++<6 )
    										.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
     * }
     * </pre>
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    public final static <T> Stream<T> cycleWhile(final Stream<T> stream, final Predicate<? super T> predicate) {
        return StreamUtils.limitWhile(StreamUtils.cycle(stream), predicate);
    }

    /**
     * Repeat in a Stream until specified predicate holds
     * 
     * <pre>
     * {@code 
     * 	count =0;
    	assertThat(StreamUtils.cycleUntil(Stream.of(1,2,2,3)
    										,next -> count++>10 )
    										.collect(Collectors.toList()),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));
    
     * }
     * </pre>
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    public final static <T> Stream<T> cycleUntil(final Stream<T> stream, final Predicate<? super T> predicate) {
        return StreamUtils.limitUntil(StreamUtils.cycle(stream), predicate);
    }

    /**
     * Generic zip function. E.g. Zipping a Stream and a Sequence
     * 
     * <pre>
     * {@code 
     * Stream<List<Integer>> zipped = StreamUtils.zip(Stream.of(1,2,3)
    											,ReactiveSeq.of(2,3,4), 
    												(a,b) -> Arrays.asList(a,b));
    	
    	
    	List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
    	assertThat(zip.get(0),equalTo(2));
    	assertThat(zip.get(1),equalTo(3));
     * }
     * </pre>
     * @param second
     *            Monad to zip with
     * @param zipper
     *            Zipping function
     * @return Stream zipping two Monads
    */
    public final static <T, S, R> Stream<R> zipSequence(final Stream<T> stream, final Stream<? extends S> second,
            final BiFunction<? super T, ? super S, ? extends R> zipper) {
        final Iterator<T> left = stream.iterator();
        final Iterator<? extends S> right = second.iterator();
        return StreamUtils.stream(new Iterator<R>() {

            @Override
            public boolean hasNext() {
                return left.hasNext() && right.hasNext();
            }

            @Override
            public R next() {
                return zipper.apply(left.next(), right.next());
            }

        });

    }

    /**
     *  Generic zip function. E.g. Zipping a Stream and an Optional
     * 
     * <pre>
     * {@code
     * Stream<List<Integer>> zipped = StreamUtils.zip(Stream.of(1,2,3)
    									,anyM(Optional.of(2)), 
    										(a,b) -> Arrays.asList(a,b));
    	
    	
    	List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
    	assertThat(zip.get(0),equalTo(1));
    	assertThat(zip.get(1),equalTo(2));
     * 
     * }
     * </pre>
     
     */
    public final static <T, S, R> Stream<R> zipAnyM(final Stream<T> stream, final AnyM<? extends S> second,
            final BiFunction<? super T, ? super S, ? extends R> zipper) {
        return zipSequence(stream, second.stream(), zipper);
    }

    /**
     * Zip this Monad with a Stream
     * 
       <pre>
       {@code 
       Stream<List<Integer>> zipped = StreamUtils.zipStream(Stream.of(1,2,3)
    											,Stream.of(2,3,4), 
    												(a,b) -> Arrays.asList(a,b));
    	
    	
    	List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
    	assertThat(zip.get(0),equalTo(2));
    	assertThat(zip.get(1),equalTo(3));
       }
       </pre>
     * 
     * @param second
     *            Stream to zip with
     * @param zipper
     *            Zip funciton
     * @return This monad zipped with a Stream
     */
    public final static <T, S, R> Stream<R> zipStream(final Stream<T> stream,
            final BaseStream<? extends S, ? extends BaseStream<? extends S, ?>> second, final BiFunction<? super T, ? super S, ? extends R> zipper) {
        final Iterator<T> left = stream.iterator();
        final Iterator<? extends S> right = second.iterator();
        return StreamUtils.stream(new Iterator<R>() {

            @Override
            public boolean hasNext() {
                return left.hasNext() && right.hasNext();
            }

            @Override
            public R next() {
                return zipper.apply(left.next(), right.next());
            }

        });

    }

    /**
     * Create a sliding view over this Stream
     * <pre>
     * {@code 
     * List<List<Integer>> list = StreamUtils.sliding(Stream.of(1,2,3,4,5,6)
    											,2,1)
    								.collect(Collectors.toList());
    	
    
    	assertThat(list.get(0),hasItems(1,2));
    	assertThat(list.get(1),hasItems(2,3));
     * }
     * </pre>
     * @param windowSize
     *            Size of sliding window
     * @return Stream with sliding view 
     */
    public final static <T> Stream<ListX<T>> sliding(final Stream<T> stream, final int windowSize, final int increment) {
        final Iterator<T> it = stream.iterator();
        final Mutable<PStack<T>> list = Mutable.of(ConsPStack.empty());
        return StreamUtils.stream(new Iterator<ListX<T>>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ListX<T> next() {
                for (int i = 0; i < increment && list.get()
                                                     .size() > 0; i++)
                    list.mutate(var -> var.minus(0));
                for (; list.get()
                           .size() < windowSize
                        && it.hasNext();) {
                    if (it.hasNext()) {
                        list.mutate(var -> var.plus(Math.max(0, var.size()), it.next()));
                    }

                }
                return ListX.fromIterable(list.get());
            }

        });
    }

    /**
     * Create a sliding view over this Stream
     * <pre>
     * {@code 
     * List<List<Integer>> list = StreamUtils.sliding(Stream.of(1,2,3,4,5,6)
    											,2,1)
    								.collect(Collectors.toList());
    	
    
    	assertThat(list.get(0),hasItems(1,2));
    	assertThat(list.get(1),hasItems(2,3));
     * }
     * </pre>
     * @param windowSize
     *            Size of sliding window
     * @return Stream with sliding view over monad
     */
    public final static <T> Stream<Streamable<T>> window(final Stream<T> stream, final int windowSize, final int increment) {
        final Iterator<T> it = stream.iterator();
        final Mutable<PStack<T>> list = Mutable.of(ConsPStack.empty());
        return StreamUtils.stream(new Iterator<Streamable<T>>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Streamable<T> next() {
                for (int i = 0; i < increment && list.get()
                                                     .size() > 0; i++)
                    list.mutate(var -> var.minus(0));
                for (; list.get()
                           .size() < windowSize
                        && it.hasNext();) {
                    if (it.hasNext()) {
                        list.mutate(var -> var.plus(Math.max(0, var.size()), it.next()));
                    }

                }
                return Streamable.fromIterable(list.get());
            }

        });
    }

    /**
     * Create a sliding view over this Stream
     * <pre>
     * {@code 
     * List<List<Integer>> list = StreamUtils.sliding(Stream.of(1,2,3,4,5,6)
    											,2)
    								.collect(Collectors.toList());
    	
    
    	assertThat(list.get(0),hasItems(1,2));
    	assertThat(list.get(1),hasItems(2,3));
     * }
     * </pre> 
     * 
     * @param stream Stream to create sliding view on
     * @param windowSize size of window
     * @return
     */
    public final static <T> Stream<ListX<T>> sliding(final Stream<T> stream, final int windowSize) {
        return sliding(stream, windowSize, 1);
    }

    /**
     * Group elements in a Stream by size
     * 
       <pre>
       {@code 
     * 	List<List<Integer>> list = StreamUtils.batchBySize(Stream.of(1,2,3,4,5,6)
    													,3)
    												.collect(Collectors.toList());
    	
    	
    	assertThat(list.get(0),hasItems(1,2,3));
    	assertThat(list.get(1),hasItems(4,5,6));
    	}
     * </pre>
     * @param stream Stream to group 
     * @param groupSize
     *            Size of each Group
     * @return Stream with elements grouped by size
     */
    @Deprecated
    public final static <T> Stream<ListX<T>> batchBySize(final Stream<T> stream, final int groupSize) {
        return grouped(stream,groupSize);

    }
    /**
     * Group elements in a Stream by size
     * 
       <pre>
       {@code 
     *  List<List<Integer>> list = StreamUtils.grouped(Stream.of(1,2,3,4,5,6)
                                                        ,3)
                                                    .collect(Collectors.toList());
        
        
        assertThat(list.get(0),hasItems(1,2,3));
        assertThat(list.get(1),hasItems(4,5,6));
        }
     * </pre>
     * @param stream Stream to group 
     * @param groupSize
     *            Size of each Group
     * @return Stream with elements grouped by size
     */
    public final static <T> Stream<ListX<T>> grouped(final Stream<T> stream, final int groupSize) {
        return new BatchBySizeOperator<T, ListX<T>>(
                                                    stream).batchBySize(groupSize);

    }
    /**
     * 
     * 
       <pre>
       {@code 
     *  List<SetX<Integer>> list = StreamUtils.batchBySize(Stream.of(1,2,3,4,5,6)
                                                        ,3,()->SetX.empty())
                                                    .collect(Collectors.toList());
        
        
        assertThat(list.get(0),hasItems(1,2,3));
        assertThat(list.get(1),hasItems(4,5,6));
        }
     * </pre>
     * 
     * @param stream Stream to group 
     * @param groupSize Size of each Group
     * @param factory Supplier for creating Collections for holding grouping
     * @return  Stream with elements grouped by size
     */
    @Deprecated
    public final static <T, C extends Collection<? super T>> Stream<C> batchBySize(final Stream<T> stream, final int groupSize,
            final Supplier<C> factory) {
        return new BatchBySizeOperator<T, C>(
                                             stream, factory).batchBySize(groupSize);

    }  
    
    /**
     * 
     * 
       <pre>
       {@code 
     *  List<SetX<Integer>> list = StreamUtils.grouped(Stream.of(1,2,3,4,5,6)
                                                        ,3,()->SetX.empty())
                                                    .collect(Collectors.toList());
        
        
        assertThat(list.get(0),hasItems(1,2,3));
        assertThat(list.get(1),hasItems(4,5,6));
        }
     * </pre>
     * 
     * @param stream Stream to group 
     * @param groupSize Size of each Group
     * @param factory Supplier for creating Collections for holding grouping
     * @return  Stream with elements grouped by size
     */
    @Deprecated
    public final static <T, C extends Collection<? super T>> Stream<C> grouped(final Stream<T> stream, final int groupSize,
            final Supplier<C> factory) {
        return new BatchBySizeOperator<T, C>(
                                             stream, factory).batchBySize(groupSize);

    }

    public final static <T> Streamable<T> shuffle(final Stream<T> stream) {
        final List<T> list = stream.collect(Collectors.toList());
        Collections.shuffle(list);
        return Streamable.fromIterable(list);
    }

    public final static <T> Streamable<T> toLazyStreamable(final Stream<T> stream) {
        return Streamable.fromStream(stream);
    }

    public final static <T> Streamable<T> toConcurrentLazyStreamable(final Stream<T> stream) {
        return Streamable.synchronizedFromStream(stream);
    }

    public final static <U, T> Stream<U> scanRight(final Stream<T> stream, final U identity,
            final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return Seq.seq(stream)
                  .scanRight(identity, combiner);
    }

    /**
     * Scan left using supplied Monoid
     * 
     * <pre>
     * {@code  
     * 
     * 	assertEquals(asList("", "a", "ab", "abc"),
     * 					StreamUtils.scanLeft(Stream.of("a", "b", "c"),Reducers.toString(""))
     * 			.collect(Collectors.toList());
     *         
     *         }
     * </pre>
     * 
     * @param monoid
     * @return
     */
    public final static <T> Stream<T> scanLeft(final Stream<T> stream, final Monoid<T> monoid) {

        final Iterator<T> it = stream.iterator();
        return StreamUtils.stream(new Iterator<T>() {
            boolean init = false;
            T next = monoid.zero();

            @Override
            public boolean hasNext() {
                if (!init)
                    return true;
                return it.hasNext();
            }

            @Override
            public T next() {
                if (!init) {
                    init = true;
                    return monoid.zero();
                }
                return next = monoid
                                    .apply(next, it.next());

            }

        });

    }

    /**
     * Check that there are specified number of matches of predicate in the Stream
     * 
     * <pre>
     * {@code 
     *  assertTrue(StreamUtils.xMatch(Stream.of(1,2,3,5,6,7),3, i->i>4));
     * }
     * </pre>
     * 
     */
    public static <T> boolean xMatch(final Stream<T> stream, final int num, final Predicate<? super T> c) {

        return stream.filter(t -> c.test(t))
                     .collect(Collectors.counting()) == num;
    }

    /**
     * <pre>
     * {@code 
     * assertThat(StreamUtils.noneMatch(of(1,2,3,4,5),it-> it==5000),equalTo(true));
     * }
     * </pre>
     *
     */
    public final static <T> boolean noneMatch(final Stream<T> stream, final Predicate<? super T> c) {
        return stream.allMatch(c.negate());
    }

    public final static <T> String join(final Stream<T> stream) {
        return stream.map(t -> t.toString())
                     .collect(Collectors.joining());
    }

    public final static <T> String join(final Stream<T> stream, final String sep) {
        return stream.map(t -> t.toString())
                     .collect(Collectors.joining(sep));
    }

    public final static <T> String join(final Stream<T> stream, final String sep, final String start, final String end) {
        return stream.map(t -> t.toString())
                     .collect(Collectors.joining(sep, start, end));
    }

    public final static <T, C extends Comparable<? super C>> Optional<T> minBy(final Stream<T> stream, final Function<? super T, ? extends C> f) {
        final Optional<Tuple2<C, T>> o = stream.map(in -> new Tuple2<C, T>(
                                                                           f.apply(in), in))
                                               .min(Comparator.comparing(n -> n.v1(), Comparator.naturalOrder()));
        return o.map(p -> p.v2());
    }

    public final static <T> Optional<T> min(final Stream<T> stream, final Comparator<? super T> comparator) {
        return stream.collect(Collectors.minBy(comparator));
    }

    public final static <T, C extends Comparable<? super C>> Optional<T> maxBy(final Stream<T> stream, final Function<? super T, ? extends C> f) {
        final Optional<Tuple2<C, T>> o = stream.map(in -> new Tuple2<C, T>(
                                                                           f.apply(in), in))
                                               .max(Comparator.comparing(n -> n.v1(), Comparator.naturalOrder()));
        return o.map(p -> p.v2());
    }

    public final static <T> Optional<T> max(final Stream<T> stream, final Comparator<? super T> comparator) {
        return stream.collect(Collectors.maxBy(comparator));
    }

    /**
     * Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
     * Then use Monoid to reduce values
     * 
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    public final static <T, R> R mapReduce(final Stream<T> stream, final Reducer<R> reducer) {
        return reducer.mapReduce(stream);
    }

    /**
     *  Attempt to map this Monad to the same type as the supplied Monoid, using supplied function
     *  Then use Monoid to reduce values
     *  
     * @param mapper Function to map Monad type
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    public final static <T, R> R mapReduce(final Stream<T> stream, final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return reducer.reduce(stream.map(mapper));
    }

    /**
     * 
     * 
     * @param reducer Use supplied Monoid to reduce values starting via foldLeft
     * @return Reduced result
     */
    public final static <T> T foldLeft(final Stream<T> stream, final Monoid<T> reducer) {
        return reducer.reduce(stream);
    }

    /**
     *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
     * Then use Monoid to reduce values
     * 
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    public final static <T> T foldLeftMapToType(final Stream<T> stream, final Reducer<T> reducer) {
        return reducer.mapReduce(stream);
    }

    /**
     * 
     * 
     * @param reducer Use supplied Monoid to reduce values starting via foldRight
     * @return Reduced result
     */
    public final static <T> T foldRight(final Stream<T> stream, final Monoid<T> reducer) {
        return reducer.reduce(StreamUtils.reverse(stream));
    }

    /**
     *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
     * Then use Monoid to reduce values
     * 
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    public final static <T> T foldRightMapToType(final Stream<T> stream, final Reducer<T> reducer) {
        return reducer.mapReduce(StreamUtils.reverse(stream));
    }

    /**
     * @return Underlying monad converted to a Streamable instance
     */
    public final static <T> Streamable<T> toStreamable(final Stream<T> stream) {
        return Streamable.fromStream(stream);
    }

    /**
     * @return This monad converted to a set
     */
    public final static <T> Set<T> toSet(final Stream<T> stream) {
        return stream.collect(Collectors.toSet());
    }

    /**
     * @return this monad converted to a list
     */
    public final static <T> List<T> toList(final Stream<T> stream) {

        return stream.collect(Collectors.toList());
    }

    /**
     * 
     * <pre>{@code 
     * assertTrue(StreamUtils.startsWith(Stream.of(1,2,3,4),Arrays.asList(1,2,3)));
     * }</pre>
     * 
     * @param iterable
     * @return True if Monad starts with Iterable sequence of data
     */
    public final static <T> boolean startsWith(final Stream<T> stream, final Iterable<T> iterable) {
        return startsWith(stream, iterable.iterator());

    }

    public final static <T> boolean endsWith(final Stream<T> stream, final Iterable<T> iterable) {
        final Iterator<T> it = iterable.iterator();
        final List<T> compare1 = new ArrayList<>();
        while (it.hasNext()) {
            compare1.add(it.next());
        }
        final LinkedList<T> list = new LinkedList<>();
        stream.forEach(v -> {
            list.add(v);
            if (list.size() > compare1.size())
                list.remove();
        });
        return startsWith(list.stream(), compare1.iterator());

    }

    public final static <T> boolean startsWith(final Stream<T> stream, final Stream<T> stream2) {
        return startsWith(stream, stream2.iterator());
    }

    /**
     * 	<pre>
     * {@code
     * 		 assertTrue(StreamUtils.startsWith(Stream.of(1,2,3,4),Arrays.asList(1,2,3).iterator())) 
     * }</pre>
    
     * @param iterator
     * @return True if Monad starts with Iterators sequence of data
     */
    public final static <T> boolean startsWith(final Stream<T> stream, final Iterator<T> iterator) {
        final Iterator<T> it = stream.iterator();
        while (iterator.hasNext()) {
            if (!it.hasNext())
                return false;
            if (!Objects.equals(it.next(), iterator.next()))
                return false;
        }
        return true;

    }

    public final static <T> ReactiveSeq<T> reactiveSeq(final Stream<T> stream, final Optional<ReversableSpliterator> rev) {
        if (stream instanceof ReactiveSeq)
            return (ReactiveSeq) stream;
        if (rev.isPresent())
            return new ReactiveSeqImpl<T>(
                                          stream, rev.get());
        return new ReactiveSeqImpl<T>(
                                      stream);
    }

    /**
     * Returns a stream with a given value interspersed between any two values
     * of this stream.
     * 
     * <pre>
     * {@code 
     * assertThat(Arrays.asList(1, 0, 2, 0, 3, 0, 4),
     * 			equalTo( StreamUtils.intersperse(Stream.of(1, 2, 3, 4),0));
     * }
     * </pre>
     */
    public static <T> Stream<T> intersperse(final Stream<T> stream, final T value) {
        return stream.flatMap(t -> Stream.of(value, t))
                     .skip(1);
    }

    /**
     * Keep only those elements in a stream that are of a given type.
     * 
     * 
     * assertThat(Arrays.asList(1, 2, 3), 
     *      equalTo( StreamUtils.ofType(Stream.of(1, "a", 2, "b", 3,Integer.class));
     * 
     */
    @SuppressWarnings("unchecked")
    public static <T, U> Stream<U> ofType(final Stream<T> stream, final Class<? extends U> type) {
        return stream.filter(type::isInstance)
                     .map(t -> (U) t);
    }

    /**
     * Cast all elements in a stream to a given type, possibly throwing a
     * {@link ClassCastException}.
     * 
     * <pre>
     * {@code
     * StreamUtils.cast(Stream.of(1, "a", 2, "b", 3),Integer.class)
     *  // throws ClassCastException
     *  }
     */
    public static <T, U> Stream<U> cast(final Stream<T> stream, final Class<? extends U> type) {
        return stream.map(type::cast);
    }

    /**
     * flatMap operation
     * <pre>
     * {@code 
     * 		assertThat(StreamUtils.flatMapSequenceM(Stream.of(1,2,3),
     * 							i->ReactiveSeq.of(i+2)).collect(Collectors.toList()),
     * 								equalTo(Arrays.asList(3,4,5)));
     * }
     * </pre>
     * @param fn
     * @return
     */
    public final static <T, R> Stream<R> flatMapSequenceM(final Stream<T> stream, final Function<? super T, ReactiveSeq<? extends R>> fn) {
        return stream.flatMap(fn);
    }

    public final static <T, R> Stream<R> flatMapAnyM(final Stream<T> stream, final Function<? super T, AnyM<Witness,stream,? extends R>> fn) {
        return AnyM.fromStream(stream)
                   .flatMap(fn)
                   .stream();

    }

    /**
     * flatMap operation that allows a Collection to be returned
     * <pre>
     * {@code 
     * 	assertThat(StreamUtils.flatMapCollection(Stream.of(20),i->Arrays.asList(1,2,i))
     * 								.collect(Collectors.toList()),
     * 								equalTo(Arrays.asList(1,2,20)));
    
     * }
     * </pre>
     *
     */
    public final static <T, R> Stream<R> flatMapIterable(final Stream<T> stream, final Function<? super T, ? extends Iterable<? extends R>> fn) {
        return stream.flatMap(fn.andThen(c -> stream(c)));

    }

    /**
     * <pre>
     * {@code 
     * 	assertThat(StreamUtils.flatMapStream(Stream.of(1,2,3),
     * 							i->Stream.of(i)).collect(Collectors.toList()),
     * 							equalTo(Arrays.asList(1,2,3)));
    
     * 
     * }
     * </pre>
     *
     */
    public final static <T, R> Stream<R> flatMapStream(final Stream<T> stream, final Function<? super T, ? extends BaseStream<? extends R, ?>> fn) {
        return stream.flatMap(fn.andThen(bs -> {

            if (bs instanceof Stream)
                return (Stream<R>) bs;
            else
                return StreamUtils.stream(bs.iterator());

        }));
    }

    /**
     * cross type flatMap, removes null entries
     * <pre>
     * {@code 
     * 	 assertThat(StreamUtils.flatMapOptional(Stream.of(1,2,3,null),
     * 										Optional::ofNullable)
     * 										.collect(Collectors.toList()),
     * 										equalTo(Arrays.asList(1,2,3)));
    
     * }
     * </pre>
     */
    public final static <T, R> Stream<R> flatMapOptional(final Stream<T> stream, final Function<? super T, Optional<? extends R>> fn) {
        return stream.flatMap(in -> StreamUtils.optionalToStream(fn.apply(in)));

    }

    public final static <T, R> ReactiveSeq<R> flatten(final Stream<T> stream) {
        return new MonadWrapper<>(
                                  stream).flatten()
                                         .sequence();
    }

    /**
     *<pre>
     * {@code 
     * 	assertThat(StreamUtils.flatMapCompletableFuture(Stream.of(1,2,3),
     * 								i->CompletableFuture.completedFuture(i+2))
     * 								.collect(Collectors.toList()),
     * 								equalTo(Arrays.asList(3,4,5)));
    
     * }
     *</pre>
     */
    public final static <T, R> Stream<R> flatMapCompletableFuture(final Stream<T> stream,
            final Function<? super T, CompletableFuture<? extends R>> fn) {
        return stream.flatMap(in -> StreamUtils.completableFutureToStream(fn.apply(in)));

    }

    /**
     * Perform a flatMap operation where the result will be a flattened stream of Characters
     * from the CharSequence returned by the supplied function.
     * 
     * <pre>
     * {@code 
     *   List<Character> result = StreamUtils.liftAndBindCharSequence(Stream.of("input.file"),
    								.i->"hello world")
    								.toList();
    	
    	assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
     * }
     * </pre>
     * 
     * @param fn
     * @return
     *///rename -flatMapCharSequence
    public final static <T> Stream<Character> flatMapCharSequence(final Stream<T> stream, final Function<? super T, CharSequence> fn) {
        return new MonadWrapper<T>(
                                   stream).liftAndBind(fn)
                                          .sequence();
    }

    /**
     *  Perform a flatMap operation where the result will be a flattened stream of Strings
     * from the text loaded from the supplied files.
     * 
     * <pre>
     * {@code
     * 
    	List<String> result = StreamUtils.liftAndBindFile(Stream.of("input.file")
    							.map(getClass().getClassLoader()::getResource)
    							.peek(System.out::println)
    							.map(URL::getFile)
    							,File::new)
    							.toList();
    	
    	assertThat(result,equalTo(Arrays.asList("hello","world")));
     * 
     * }
     * 
     * </pre>
     * 
     * @param fn
     * @return
     */
    public final static <T> Stream<String> flatMapFile(final Stream<T> stream, final Function<? super T, File> fn) {
        return new MonadWrapper<T>(
                                   stream).liftAndBind(fn)
                                          .sequence();
    }

    /**
     *  Perform a flatMap operation where the result will be a flattened stream of Strings
     * from the text loaded from the supplied URLs 
     * 
     * <pre>
     * {@code 
     * List<String> result = StreamUtils.liftAndBindURL(Stream.of("input.file")
    							,getClass().getClassLoader()::getResource)
    							.collect(Collectors.toList();
    	
    	assertThat(result,equalTo(Arrays.asList("hello","world")));
     * 
     * }
     * </pre>
     * 
     * @param fn
     * @return
     */
    public final static <T> Stream<String> flatMapURL(final Stream<T> stream, final Function<? super T, URL> fn) {
        return new MonadWrapper<T>(
                                   stream).liftAndBind(fn)
                                          .sequence();
    }

    /**
      *  Perform a flatMap operation where the result will be a flattened stream of Strings
     * from the text loaded from the supplied BufferedReaders
     * 
     * <pre>
     * List<String> result = StreamUtils.liftAndBindBufferedReader(Stream.of("input.file")
    							.map(getClass().getClassLoader()::getResourceAsStream)
    							.map(InputStreamReader::new)
    							,BufferedReader::new)
    							.collect(Collectors.toList();
    	
    	assertThat(result,equalTo(Arrays.asList("hello","world")));
     * 
     * </pre>
     * 
     * 
     * @param fn
     * @return
     */
    public final static <T> Stream<String> flatMapBufferedReader(final Stream<T> stream, final Function<? super T, BufferedReader> fn) {
        return new MonadWrapper<T>(
                                   stream).liftAndBind(fn)
                                          .sequence();
    }

    public static final <A> Tuple2<Iterator<A>, Iterator<A>> toBufferingDuplicator(final Iterator<A> iterator) {
        return toBufferingDuplicator(iterator, Long.MAX_VALUE);
    }

    public static final <A> Tuple2<Iterator<A>, Iterator<A>> toBufferingDuplicator(final Iterator<A> iterator, final long pos) {
        final LinkedList<A> bufferTo = new LinkedList<A>();
        final LinkedList<A> bufferFrom = new LinkedList<A>();
        return new Tuple2(
                          new DuplicatingIterator(
                                                  bufferTo, bufferFrom, iterator, Long.MAX_VALUE, 0),
                          new DuplicatingIterator(
                                                  bufferFrom, bufferTo, iterator, pos, 0));
    }

    public static final <A> ListX<Iterator<A>> toBufferingCopier(final Iterator<A> iterator, final int copies) {
        final List<Iterator<A>> result = new ArrayList<>();
        final List<CopyingIterator<A>> leaderboard = new LinkedList<>();
        final LinkedList<A> buffer = new LinkedList<>();
        for (int i = 0; i < copies; i++)
            result.add(new CopyingIterator(
                                           iterator, leaderboard, buffer, copies));
        return ListX.fromIterable(result);
    }

    @AllArgsConstructor
    static class DuplicatingIterator<T> implements Iterator<T> {

        LinkedList<T> bufferTo;
        LinkedList<T> bufferFrom;
        Iterator<T> it;
        long otherLimit = Long.MAX_VALUE;
        long counter = 0;

        @Override
        public boolean hasNext() {
            if (bufferFrom.size() > 0 || it.hasNext())
                return true;
            return false;
        }

        @Override
        public T next() {
            try {
                if (bufferFrom.size() > 0)
                    return bufferFrom.remove(0);
                else {
                    final T next = it.next();
                    if (counter < otherLimit)
                        bufferTo.add(next);
                    return next;
                }
            } finally {
                counter++;
            }
        }

    }

    static class CopyingIterator<T> implements Iterator<T> {

        LinkedList<T> buffer;
        Iterator<T> it;
        List<CopyingIterator<T>> leaderboard = new LinkedList<>();
        boolean added = false;
        int total = 0;
        int counter = 0;

        @Override
        public boolean hasNext() {

            if (isLeader())
                return it.hasNext();
            if (isLast())
                return buffer.size() > 0 || it.hasNext();
            if (it.hasNext())
                return true;
            return counter < buffer.size();
        }

        private boolean isLeader() {
            return leaderboard.size() == 0 || this == leaderboard.get(0);
        }

        private boolean isLast() {
            return leaderboard.size() == total && this == leaderboard.get(leaderboard.size() - 1);
        }

        @Override
        public T next() {

            if (!added) {

                this.leaderboard.add(this);
                added = true;
            }

            if (isLeader()) {

                return handleLeader();
            }
            if (isLast()) {

                if (buffer.size() > 0)
                    return buffer.poll();
                return it.next();
            }
            if (counter < buffer.size())
                return buffer.get(counter++);
            return handleLeader(); //exceed buffer, now leading

        }

        private T handleLeader() {
            final T next = it.next();
            buffer.offer(next);
            return next;
        }

        public CopyingIterator(final Iterator<T> it, final List<CopyingIterator<T>> leaderboard, final LinkedList<T> buffer, final int total) {

            this.it = it;
            this.leaderboard = leaderboard;
            this.buffer = buffer;
            this.total = total;
        }
    }

    /**
      * Projects an immutable collection of this stream. Initial iteration over the collection is not thread safe 
      * (can't be performed by multiple threads concurrently) subsequent iterations are.
      *
      * @return An immutable collection of this stream.
      */
    public static final <A> CollectionX<A> toLazyCollection(final Stream<A> stream) {
        return SeqUtils.toLazyCollection(stream.iterator());
    }

    public static final <A> CollectionX<A> toLazyCollection(final Iterator<A> iterator) {
        return SeqUtils.toLazyCollection(iterator);
    }

    /**
     * Lazily constructs a Collection from specified Stream. Collections iterator may be safely used
     * concurrently by multiple threads.
    * @param stream
    * @return
    */
    public static final <A> CollectionX<A> toConcurrentLazyCollection(final Stream<A> stream) {
        return SeqUtils.toConcurrentLazyCollection(stream.iterator());
    }

    public static final <A> CollectionX<A> toConcurrentLazyCollection(final Iterator<A> iterator) {
        return SeqUtils.toConcurrentLazyCollection(iterator);
    }

    public final static <T> Stream<Streamable<T>> windowByTime(final Stream<T> stream, final long time, final TimeUnit t) {
        final Iterator<T> it = stream.iterator();
        final long toRun = t.toNanos(time);
        return StreamUtils.stream(new Iterator<Streamable<T>>() {
            long start = System.nanoTime();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Streamable<T> next() {

                final List<T> list = new ArrayList<>();

                while (System.nanoTime() - start < toRun && it.hasNext()) {
                    list.add(it.next());
                }
                if (list.size() == 0 && it.hasNext()) //time unit may be too small
                    list.add(it.next());
                start = System.nanoTime();

                return Streamable.fromIterable(list);
            }

        });
    }
    
    public final static <T> Stream<ListX<T>> groupedByTime(final Stream<T> stream, final long time, final TimeUnit t) {
        return new BatchByTimeOperator<T, ListX<T>>(
                                                    stream).batchByTime(time, t);
    }
    @Deprecated
    public final static <T> Stream<ListX<T>> batchByTime(final Stream<T> stream, final long time, final TimeUnit t) {
        return groupedByTime(stream,time,t);
    }
    public final static <T, C extends Collection<? super T>> Stream<C> groupedByTime(final Stream<T> stream, final long time, final TimeUnit t,
            final Supplier<C> factory) {
        return new BatchByTimeOperator<T, C>(
                                             stream, factory).batchByTime(time, t);
    }
    @Deprecated
    public final static <T, C extends Collection<? super T>> Stream<C> batchByTime(final Stream<T> stream, final long time, final TimeUnit t,
            final Supplier<C> factory) {
        return groupedByTime(stream,time,t,factory);
    }

    private static final Object UNSET = new Object();

    /**
     * Group data in a Stream using knowledge of the current batch and the next entry to determing grouping limits
     * 
     * @see com.aol.cyclops.control.ReactiveSeq#groupedStatefullyUntil(BiPredicate)
     * 
     * @param stream Stream to group
     * @param predicate Predicate to determine grouping
     * @return Stream grouped into Lists determined by predicate
     */
    public final static <T> Stream<ListX<T>> groupedStatefullyUntil(final Stream<T> stream,
            final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return new WindowStatefullyWhileOperator<>(
                                                   stream).windowStatefullyWhile(predicate);
    }
    
    /**
     * Group a Stream while the supplied predicate holds
     * 
     * @see com.aol.cyclops.control.ReactiveSeq#groupedWhile(Predicate)
     * 
     * @param stream Stream to group
     * @param predicate Predicate to determine grouping
     * @return Stream grouped into Lists determined by predicate
     */
    public final static <T> Stream<ListX<T>> groupedWhile(final Stream<T> stream, final Predicate<? super T> predicate) {
        return new BatchWhileOperator<T, ListX<T>>(
                                                   stream).batchWhile(predicate);
    }
    @Deprecated
    public final static <T> Stream<ListX<T>> batchWhile(final Stream<T> stream, final Predicate<? super T> predicate) {
        return groupedWhile(stream,predicate);
    }
    /**
     * Group a Stream while the supplied predicate holds
     * 
     * @see com.aol.cyclops.control.ReactiveSeq#groupedWhile(Predicate, Supplier)
     * 
     * @param stream Stream to group
     * @param predicate Predicate to determine grouping
     * @param factory Supplier to create collection for groupings
     * @return Stream grouped into Collections determined by predicate
     */
    public final static <T, C extends Collection<? super T>> Stream<C> groupedWhile(final Stream<T> stream, final Predicate<? super T> predicate,
            final Supplier<C> factory) {
        return new BatchWhileOperator<T, C>(
                                            stream, factory).batchWhile(predicate);
    }
    @Deprecated
    public final static <T, C extends Collection<? super T>> Stream<C> batchWhile(final Stream<T> stream, final Predicate<? super T> predicate,
            final Supplier<C> factory) {
        return groupedWhile(stream,predicate,factory);
    }
    /**
     * Group a Stream until the supplied predicate holds
     * 
     * @see com.aol.cyclops.control.ReactiveSeq#groupedUntil(Predicate)
     * 
     * @param stream Stream to group
     * @param predicate Predicate to determine grouping
     * @return Stream grouped into Lists determined by predicate
     */
    public final static <T> Stream<ListX<T>> groupedUntil(final Stream<T> stream, final Predicate<? super T> predicate) {
        return groupedWhile(stream, predicate.negate());
    }
    @Deprecated
    public final static <T> Stream<ListX<T>> batchUntil(final Stream<T> stream, final Predicate<? super T> predicate) {
        return groupedUntil(stream, predicate);
    }
    /**
     * Group a Stream by size and time constraints
     * 
     * @see com.aol.cyclops.control.ReactiveSeq#groupedBySizeAndTime(int, long, TimeUnit)
     * 
     * @param stream Stream to group
     * @param size Max group size 
     * @param time Max group time
     * @param t Time unit for max group time
     * @return Stream grouped by time and size
     */
    public final static <T> Stream<ListX<T>> groupedBySizeAndTime(final Stream<T> stream, final int size, final long time, final TimeUnit t) {
        return new BatchByTimeAndSizeOperator<T, ListX<T>>(
                                                           stream).batchBySizeAndTime(size, time, t);
    }
    @Deprecated
    public final static <T> Stream<ListX<T>> batchBySizeAndTime(final Stream<T> stream, final int size, final long time, final TimeUnit t) {
        return groupedBySizeAndTime(stream,size,time,t);
    }
    
    /**
     * Group a Stream by size and time constraints
     * 
     * @see com.aol.cyclops.control.ReactiveSeq#groupedBySizeAndTime(int, long, TimeUnit, Supplier)
     * 
     * @param stream Stream to group
     * @param size Max group size 
     * @param time Max group time
     * @param t Time unit for max group time
     * @param factory Supplier to create collection for groupings
     * @return Stream grouped by time and size
     */
    public final static <T, C extends Collection<? super T>> Stream<C> groupedBySizeAndTime(final Stream<T> stream, final int size, final long time,
            final TimeUnit t, final Supplier<C> factory) {
        return new BatchByTimeAndSizeOperator<T, C>(
                                                    stream, factory).batchBySizeAndTime(size, time, t);
    }
    @Deprecated
    public final static <T, C extends Collection<? super T>> Stream<C> batchBySizeAndTime(final Stream<T> stream, final int size, final long time,
            final TimeUnit t, final Supplier<C> factory) {
        return groupedBySizeAndTime(stream,size,time,t,factory);
    }

    /**
     * Allow one element through per time period, drop all other elements in
     * that time period
     * 
     * @see com.aol.cyclops.control.ReactiveSeq#debounce(long, TimeUnit)
     * 
     * @param stream Stream to debounce
     * @param time Time to apply debouncing over
     * @param t Time unit for debounce period
     * @return Stream with debouncing applied
     */
    public final static <T> Stream<T> debounce(final Stream<T> stream, final long time, final TimeUnit t) {
        return new DebounceOperator<>(
                                      stream).debounce(time, t);
    }

    /**
     *  emit one element per time period
     * 
     * @see com.aol.cyclops.control.ReactiveSeq#onePer(long, TimeUnit)
     * 
     * @param stream Stream to emit one element per time period from
     * @param time  Time period
     * @param t Time Unit
     * @return Stream with slowed emission
     */
    public final static <T> Stream<T> onePer(final Stream<T> stream, final long time, final TimeUnit t) {
        return new OnePerOperator<>(
                                    stream).onePer(time, t);
    }

    /**
     *  Introduce a random jitter / time delay between the emission of elements
     *  
     *  @see com.aol.cyclops.control.ReactiveSeq#jitter(long) 
     *  
     * @param stream  Stream to introduce jitter to 
     * @param jitterInNanos Max jitter period - random number less than this is used for each jitter
     * @return Jittered Stream
     */
    public final static <T> Stream<T> jitter(final Stream<T> stream, final long jitterInNanos) {
        final Iterator<T> it = stream.iterator();
        final Random r = new Random();
        return StreamUtils.stream(new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                final T nextValue = it.next();
                try {
                    final long elapsedNanos = (long) (jitterInNanos * r.nextDouble());
                    final long millis = elapsedNanos / 1000000;
                    final int nanos = (int) (elapsedNanos - millis * 1000000);
                    Thread.sleep(Math.max(0, millis), Math.max(0, nanos));

                } catch (final InterruptedException e) {
                    ExceptionSoftener.throwSoftenedException(e);
                    return null;
                }
                return nextValue;
            }

        });
    }

    public final static <T> Stream<T> fixedDelay(final Stream<T> stream, final long time, final TimeUnit unit) {
        final Iterator<T> it = stream.iterator();

        return StreamUtils.stream(new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                final T nextValue = it.next();
                try {
                    final long elapsedNanos = unit.toNanos(time);
                    final long millis = elapsedNanos / 1000000;
                    final int nanos = (int) (elapsedNanos - millis * 1000000);
                    Thread.sleep(Math.max(0, millis), Math.max(0, nanos));

                } catch (final InterruptedException e) {
                    ExceptionSoftener.throwSoftenedException(e);
                    return null;
                }
                return nextValue;
            }

        });
    }

    public final static <T> Stream<T> xPer(final Stream<T> stream, final int x, final long time, final TimeUnit t) {
        final Iterator<T> it = stream.iterator();
        final long next = t.toNanos(time);
        return StreamUtils.stream(new Iterator<T>() {
            volatile long last = -1;
            volatile int count = 0;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                final T nextValue = it.next();
                if (++count < x)
                    return nextValue;
                count = 0;
                LockSupport.parkNanos(next - (System.nanoTime() - last));

                last = System.nanoTime();
                return nextValue;
            }

        });
    }

    public final static <T> HotStream<T> hotStream(final Stream<T> stream, final Executor exec) {
        return new NonPausableHotStream<>(
                                          stream).init(exec);
    }

    public final static <T> HotStream<T> primedHotStream(final Stream<T> stream, final Executor exec) {
        return new NonPausableHotStream<>(
                                          stream).paused(exec);
    }

    public final static <T> PausableHotStream<T> pausableHotStream(final Stream<T> stream, final Executor exec) {
        return new PausableHotStreamImpl<>(
                                           stream).init(exec);
    }

    public final static <T> PausableHotStream<T> primedPausableHotStream(final Stream<T> stream, final Executor exec) {
        return new PausableHotStreamImpl<>(
                                           stream).paused(exec);
    }
}
