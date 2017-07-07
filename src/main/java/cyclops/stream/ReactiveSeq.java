package cyclops.stream;


import com.aol.cyclops2.data.collections.extensions.LazyFluentCollectionX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.immutable.LinkedListX;
import cyclops.control.Xor;
import cyclops.typeclasses.Active;
import cyclops.typeclasses.InstanceDefinitions;
import com.aol.cyclops2.internal.stream.OneShotStreamX;
import com.aol.cyclops2.internal.stream.spliterators.*;
import com.aol.cyclops2.internal.stream.spliterators.doubles.ReversingDoubleArraySpliterator;
import com.aol.cyclops2.internal.stream.spliterators.ints.ReversingIntArraySpliterator;
import com.aol.cyclops2.internal.stream.spliterators.ints.ReversingRangeIntSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.longs.ReversingLongArraySpliterator;
import com.aol.cyclops2.internal.stream.spliterators.longs.ReversingRangeLongSpliterator;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.factory.Unit;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.futurestream.Continuation;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.stream.*;
import com.aol.cyclops2.types.reactive.QueueBasedSubscriber;
import com.aol.cyclops2.types.reactive.QueueBasedSubscriber.Counter;
import com.aol.cyclops2.types.traversable.FoldableTraversable;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.companion.Streams;
import cyclops.async.*;
import cyclops.async.adapters.*;
import cyclops.async.adapters.Queue;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.MapX;
import cyclops.collections.immutable.VectorX;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;

import cyclops.control.lazy.Either;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.StreamT;
import cyclops.typeclasses.Nested;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.val;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.*;

import static com.aol.cyclops2.types.foldable.Evaluation.LAZY;

/**
 * A powerful extended, sequential Stream type.
 * Extends JDK 8 java.util.reactiveStream.Stream.
 * Implements the reactive-reactiveStream publisher api.
 * Replayable Stream by default, using primitive operators (ints,longs, doubles or jool results in conversion toNested a oneshot Stream
 * (as of 2.0.0-MI1)
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
 *      Parallelism via FutureStream
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
 *      Controlled iteration (forEach)
 *      Event handling (on next, on error, on complete)
 *
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements within the Stream
 */
public interface ReactiveSeq<T> extends To<ReactiveSeq<T>>,
                                        Stream<T>,
                                        OnEmptySwitch<T, Stream<T>>,
                                        FoldableTraversable<T>,
                                        Unit<T>,
                                        Higher<reactiveSeq,T> {



    default Active<reactiveSeq,T> allTypeclasses(){
        return Active.of(this, this.visit(sync->Instances.definitions(),rs->Spouts.Instances.definitions(),ac->Spouts.Instances.definitions()));
    }
    default <W2,R> Nested<reactiveSeq,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }




    /**
     * Create a Stream that accepts data via the Subsriber passed into the supplied Consumer.
     * reactive-streams susbscription can be used toNested determine demand (or ignored and data passed
     * via onNext, onError) excess supply over demand is enqueued
     *
     * <pre>
     *     {@code
     *      ReactiveSeq<Integer> input = ReactiveSeq.enqueued(subscriber->{
     *                                                          listener.onEvent(subscriber::onNext);
     *                                                          listener.onError(susbscriber::onError);
     *                                                          closeListener.onEvent(subscriber::onClose);
     *                                                      });
     *      }
     * </pre>
     *
     * @param sub
     * @param <T>
     * @return
     */
    static <T> ReactiveSeq<T> enqueued(Consumer<? super Subscriber<T>> sub){
        final Counter c = new Counter();
        c.active.set(1);

        QueueBasedSubscriber<T> s = QueueBasedSubscriber.subscriber(c,1);
        sub.accept(s);
        s.close();
        return s.reactiveSeq();
    }
    static <T> ReactiveSeq<T> enqueuedAll(Consumer<? super Subscriber<T>>... subs){
        final Counter c = new Counter();
        c.active.set(subs.length);
        QueueBasedSubscriber<T> s = QueueBasedSubscriber.subscriber(c,subs.length);

        for(Consumer<? super Subscriber<T>> next : subs)
            next.accept(s);
        s.close();
        return s.reactiveSeq();
    }
    static <T> ReactiveSeq<T> enqueued(Queue<T> q,Consumer<? super Subscriber<T>> sub){
        final Counter c = new Counter();
        c.active.set(1);
        QueueBasedSubscriber<T> s = QueueBasedSubscriber.subscriber(q,c,1);
        sub.accept(s);
        return s.reactiveSeq();
    }
    static <T> ReactiveSeq<T> enqueued(QueueFactory<T> factory, Consumer<? super Subscriber<T>>... subs){
        final Counter c = new Counter();
        c.active.set(subs.length);
        QueueBasedSubscriber<T> s = QueueBasedSubscriber.subscriber(factory,c,subs.length);

        for(Consumer<? super Subscriber<T>> next : subs)
            next.accept(s);
        return s.reactiveSeq();
    }

    /**
     * Construct a ReactiveSeq from a String
     *
     * @param input String toNested construct ReactiveSeq from
     * @return ReactiveSeq from a String
     */
    public static OneShotStreamX<Integer> fromCharSequence(CharSequence input){
        return Streams.<Integer>oneShotStream(input.chars().spliterator(),Optional.empty());
    }

    /**
     * @param values ints toNested populate Stream from
     * @return ReactiveSeq of multiple Integers
     */
    public static ReactiveSeq<Integer> ofInts(int... values){
        return fromSpliterator(new ReversingIntArraySpliterator<>(values,0,values.length,false));

    }
    /*
    * Fluent limit operation using primitive types
    * e.g.
    * <pre>
    *  {@code
    *    import static cyclops.ReactiveSeq.limitInts;
    *
    *    ReactiveSeq.ofInts(1,2,3)
    *               .toNested(limitInts(1));
    *
    *   //[1]
    *  }
    *  </pre>
    *
    */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> limitInts(long maxSize){

        return a->a.ints(i->i,s->s.limit(maxSize));
    }
    /*
   * Fluent limit operation using primitive types
   * e.g.
   * <pre>
   *  {@code
   *    import static cyclops.ReactiveSeq.skipInts;
   *
   *    ReactiveSeq.ofInts(1,2,3)
   *               .toNested(limitInts(1));
   *
   *   //[1]
   *  }
   *  </pre>
   *
   */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> skipInts(long skip){

        return a->a.ints(i->i,s->s.skip(skip));
    }
    /*
     * Fluent map operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.mapInts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .toNested(mapInts(i->i*2));
     *
     *   //[2,4,6]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> mapInts(IntUnaryOperator b){

        return a->a.ints(i->i,s->s.map(b));
    }
    /*
    * Fluent filter operation using primitive types
    * e.g.
    * <pre>
    *  {@code
    *    import static cyclops.ReactiveSeq.filterInts;
    *
    *    ReactiveSeq.ofInts(1,2,3)
    *               .toNested(filterInts(i->i>2));
    *
    *   //[3]
    *  }
    *  </pre>
    *
    */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> filterInts(IntPredicate b){

        return a->a.ints(i->i,s->s.filter(b));
    }
    /*
     * Fluent flatMap operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.flatMapInts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .toNested(flatMapInts(i->IntStream.of(i*2)));
     *
     *   //[2,4,6]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> flatMapInts(IntFunction<? extends IntStream> b){

        return a->a.ints(i->i,s->s.flatMap(b));
    }
    /*
     * Fluent integer concat operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.concatInts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .toNested(concatInts(ReactiveSeq.range(5,10)));
     *
     *   //[1,2,3,5,6,7,8,9]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> concatInts( ReactiveSeq<Integer> b){
        return a->fromSpliterator(IntStream.concat(a.mapToInt(i->i),b.mapToInt(i->i)).spliterator());
    }

       /**
     *
     * @param values longs toNested populate Stream from
     * @return ReactiveSeq of multiple Longs
     */
    public static ReactiveSeq<Long> ofLongs(long... values){
        return fromSpliterator(new ReversingLongArraySpliterator<>(values,0,values.length,false));
    }


    /*
     * Fluent limit operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.limitLongs;
     *
     *    ReactiveSeq.ofLongs(1,2,3)
     *               .toNested(limitLongs(1));
     *
     *   //[1]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> limitLongs(long maxSize){

        return a->a.longs(i->i,s->s.limit(maxSize));
    }
    /*
   * Fluent limit operation using primitive types
   * e.g.
   * <pre>
   *  {@code
   *    import static cyclops.ReactiveSeq.skipLongs;
   *
   *    ReactiveSeq.ofLongs(1,2,3)
   *               .toNested(limitLongs(1));
   *
   *   //[1l]
   *  }
   *  </pre>
   *
   */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> skipLongs(long skip){

        return a->a.longs(i->i,s->s.skip(skip));
    }
    /*
     * Fluent map operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.mapLongs;
     *
     *    ReactiveSeq.ofLongs(1l,2l,3l)
     *               .toNested(mapLongs(i->i*2));
     *
     *   //[2l,4l,6l]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> mapLongs(LongUnaryOperator b){

        return a->a.longs(i->i,s->s.map(b));
    }
    /*
    * Fluent filter operation using primitive types
    * e.g.
    * <pre>
    *  {@code
    *    import static cyclops.ReactiveSeq.filterInts;
    *
    *    ReactiveSeq.ofLongs(1l,2l,3l)
    *               .toNested(filterLongs(i->i>2));
    *
    *   //[3l]
    *  }
    *  </pre>
    *
    */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> filterLongs(LongPredicate b){

        return a->a.longs(i->i,s->s.filter(b));
    }
    /*
     * Fluent flatMap operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.flatMapLongs;
     *
     *    ReactiveSeq.ofLongs(1,2,3)
     *               .toNested(flatMapLongs(i->LongStream.of(i*2)));
     *
     *   //[2l,4l,6l]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> flatMapLongs(LongFunction<? extends LongStream> b){

        return a->a.longs(i->i,s->s.flatMap(b));
    }
    /*
     * Fluent integer concat operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.concatLongs;
     *
     *    ReactiveSeq.ofLongs(1l,2l,3l)
     *               .toNested(concatLongs(ReactiveSeq.ofLongs(5,10)));
     *
     *   //[1l,2l,3l,5l,6l,7l,8l,9l]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> concatLongs( ReactiveSeq<Long> b){
        return a->fromSpliterator(LongStream.concat(a.mapToLong(i->i),b.mapToLong(i->i)).spliterator());
    }

    /**
     *
     * @param values longs toNested populate Stream from
     * @return ReactiveSeq of multiple Longs
     */
    public static ReactiveSeq<Double> ofDoubles(double... values){
        return fromSpliterator(new ReversingDoubleArraySpliterator<>(values,0,values.length,false));
    }

    /*
 * Fluent limit operation using primitive types
 * e.g.
 * <pre>
 *  {@code
 *    import static cyclops.ReactiveSeq.limitDoubles;
 *
 *    ReactiveSeq.ofDoubles(1d,2d,3d)
 *               .toNested(limitDoubles(1));
 *
 *   //[1]
 *  }
 *  </pre>
 *
 */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> limitDouble(long maxSize){

        return a->a.doubles(i->i,s->s.limit(maxSize));
    }
    /*
   * Fluent limit operation using primitive types
   * e.g.
   * <pre>
   *  {@code
   *    import static cyclops.ReactiveSeq.skipDoubles;
   *
   *    ReactiveSeq.ofDoubles(1d,2d,3d)
   *               .toNested(limitDoubles(1));
   *
   *   //[1d]
   *  }
   *  </pre>
   *
   */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> skipDoubles(long skip){

        return a->a.doubles(i->i,s->s.skip(skip));
    }
    /*
     * Fluent map operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.mapDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .toNested(mapDoubles(i->i*2));
     *
     *   //[2d,4d,6d]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> mapDoubles(DoubleUnaryOperator b){

        return a->a.doubles(i->i,s->s.map(b));
    }
    /*
    * Fluent filter operation using primitive types
    * e.g.
    * <pre>
    *  {@code
    *    import static cyclops.ReactiveSeq.filterDoubles;
    *
    *    ReactiveSeq.ofDoubles(1d,2d,3d)
    *               .toNested(filterDoubles(i->i>2));
    *
    *   //[3d]
    *  }
    *  </pre>
    *
    */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> filterLongs(DoublePredicate b){

        return a->a.doubles(i->i,s->s.filter(b));
    }
    /*
     * Fluent flatMap operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.flatMapDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .toNested(flatMapDoubles(i->DoubleStream.of(i*2)));
     *
     *   //[2d,4d,6d]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> flatMapDoubles(DoubleFunction<? extends DoubleStream> b){

        return a->a.doubles(i->i,s->s.flatMap(b));
    }
    /*
     * Fluent integer concat operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.concatDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .toNested(concatDoubles(ReactiveSeq.ofDoubles(5,6,7,8,9)));
     *
     *   //[1d,2d,3d,5d,6d,7d,8d,9d]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> concatDoubles( ReactiveSeq<Double> b){

        return a->fromSpliterator(DoubleStream.concat(a.mapToDouble(i->i),b.mapToDouble(i->i)).spliterator());
    }

    /**
     * Efficiently construct a ReactiveSeq from a singleUnsafe value
     *
     * @param value Value toNested construct ReactiveSeq from
     * @return ReactiveSeq of one value
     */
    public static <T> ReactiveSeq<T> of(T value){
        return fromSpliterator(new SingleSpliterator<>(value));
    }
    /**
     * Construct a ReactiveSeq from the Supplied Spliterator
     *
     * @param spliterator Spliterator toNested construct a Stream from
     * @return ReactiveSeq created from Spliterator
     */
    public static <T> ReactiveSeq<T> fromSpliterator(Spliterator<T> spliterator){
        return Streams.reactiveSeq(spliterator, Optional.empty());
    }

    /**
     * Peform intermediate operations on a primitive IntStream (gives improved performance when working with Integers)
     * If this ReactiveSeq has an OfInt Spliterator it will be converted directly toNested an IntStream,
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
        return ReactiveSeq.fromSpliterator(mapper.apply(mapToInt(fn)).spliterator());
    }
    default <R> ReactiveSeq<R> jool(Function<? super Seq<T>, ? extends Seq<R>> mapper){
        return ReactiveSeq.fromSpliterator(foldJool(mapper).spliterator());
    }
    default <R> R foldJool(Function<? super Seq<T>, ? extends R> mapper){
        Spliterator<T> split = this.spliterator();
        return mapper.apply(Seq.seq(split));
    }


    @Override
    default IntStream mapToInt(ToIntFunction<? super T> fn){
        Spliterator<T> split = this.spliterator();
        IntStream s = (split instanceof Spliterator.OfInt)? StreamSupport.intStream((Spliterator.OfInt)split,false) : StreamSupport.stream(split,false).mapToInt(fn);
        return s;
    }


    /**
     * Peform intermediate operations on a primitive IntStream (gives improved performance when working with Integers)
     * If this ReactiveSeq has an OfInt Spliterator it will be converted directly toNested an IntStream,
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
        return ReactiveSeq.fromSpliterator(mapper.apply(mapToLong(fn)).spliterator());
    }

    @Override
    default LongStream mapToLong(ToLongFunction<? super T> fn){
        Spliterator<T> split = this.spliterator();
        return (split instanceof Spliterator.OfLong)? StreamSupport.longStream((Spliterator.OfLong)split,false) : StreamSupport.stream(split,false).mapToLong(fn);

    }

    /**
     * Peform intermediate operations on a primitive IntStream (gives improved performance when working with Integers)
     * If this ReactiveSeq has an OfInt Spliterator it will be converted directly toNested an IntStream,
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
        return ReactiveSeq.fromSpliterator(mapper.apply(mapToDouble(fn)).spliterator());
    }

    @Override
    default DoubleStream mapToDouble(ToDoubleFunction<? super T> fn){
        Spliterator<T> split = this.spliterator();
        return (split instanceof Spliterator.OfDouble) ? StreamSupport.doubleStream((Spliterator.OfDouble)split,false) : StreamSupport.stream(split,false).mapToDouble(fn);

    }



    /**
     * Construct a Stream consisting of a singleUnsafe value repeatedly infinitely (use take / drop etc toNested
     * switch toNested a finite Stream)
     *
     * @param t Value toNested fill Stream with
     * @return Infinite ReactiveSeq consisting of a singleUnsafe value
     */
    public static <T> ReactiveSeq<T> fill(T t){
        return ReactiveSeq.fromSpliterator(new FillSpliterator<T>(t));
    }
    /**
     * coflatMap pattern, can be used toNested perform maybe reductions / collections / folds and other terminal operations
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
    <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn);



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Pure#unit(java.lang.Object)
     */
    @Override
    public <T> ReactiveSeq<T> unit(T unit);

    default <R> ReactiveSeq<R> parallel(Function<? super Stream<T>,? extends Stream<? extends R>> fn){
        Queue<R> queue = QueueFactories.<R>unboundedNonBlockingQueue()
                                                                  .build();

        ReactiveSeq<Iterator<? extends R>> stream = ReactiveSeq.<Stream<? extends R>>generate(() -> foldParallel(fn))
                                                                    .take(1)
                                                                    .map(s->s.iterator());
        Iterator[] it = {null};
        Continuation[] store = {null};
        Continuation cont =
                new Continuation(()->{
                    if(it[0]==null)
                        it[0] = stream.asFunction().apply(0l);
                    Iterator<R> local = it[0];
                    try {
                        if (!local.hasNext()) {
                            queue.close();
                            return Continuation.empty();
                        } else {
                            queue.offer(local.next());
                        }
                    }catch(Throwable t){
                        queue.close();
                        throw ExceptionSoftener.throwSoftenedException(t);
                    }
                    return store[0];


                });
        ;
        store[0]=cont;
        queue.addContinuation(cont);
        return queue.stream();


    }
    default <R> ReactiveSeq<R> parallel(ForkJoinPool fj,Function<? super Stream<T>,? extends Stream<? extends R>> fn){
        Queue<R> queue = QueueFactories.<R>unboundedNonBlockingQueue()
                .build();

        ReactiveSeq<? extends Iterator<? extends R>> stream = ReactiveSeq.<Stream<? extends R>>generate(() -> foldParallel(fj,fn))
                .take(1)
                .map(s->s.iterator());
        Iterator[] it = {null};
        Continuation[] store = {null};
        Continuation cont =
                new Continuation(()->{
                    if(it[0]==null)
                        it[0] = stream.asFunction().apply(0l);
                    Iterator<R> local = it[0];
                    try {
                        if (!local.hasNext()) {
                            queue.close();

                            return Continuation.empty();
                        } else {

                            queue.offer(local.next());
                        }
                    }catch(Throwable t){
                        queue.close();
                        throw ExceptionSoftener.throwSoftenedException(t);
                    }
                    return store[0];


                });
        ;
        store[0]=cont;
        queue.addContinuation(cont);
        return queue.stream();


    }
    default <R> R foldParallel(Function<? super Stream<T>,? extends R> fn){


        Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue().build().withTimeout(1);


        AtomicReference<Continuation> ref = new AtomicReference<>(null);
        Continuation cont =
                new Continuation(()->{

                    if(ref.get()==null && ref.compareAndSet(null,Continuation.empty())){
                        try {
                            //use the takeOne consuming thread toNested tell this Stream onto the Queue
                            this.spliterator().forEachRemaining(queue::offer);
                        }finally {
                            queue.close();
                        }

                    }


                        return Continuation.empty();
                    });
        ;

        queue.addContinuation(cont);
        return fn.apply(queue.jdkStream().parallel());

    }
    default <R> R foldParallel(ForkJoinPool fj,Function<? super Stream<T>,? extends R> fn){

        return fj.submit(() -> foldParallel(fn)).join();

    }


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
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> ReactiveSeq<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ReactiveSeq<R>)zipS(ReactiveSeq.fromIterable(other),zipper);
    }

    @Override
    default <U, R> ReactiveSeq<R> zipP(final Publisher<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return zipS(ReactiveSeq.fromPublisher(other), zipper);
    }




    @Override
    <U, R> ReactiveSeq<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper);



    /**
     * join / flatten one level of a nest hierarchy
     *
     * <pre>
     * {@code
     *  ReactiveSeq.of(Arrays.asList(1,2))
     *             .toNested(ReactiveSeq::flatten));
     *
     *  //reactiveStream of (1,  2);
     *
     *
     *
     * }
     *
     * </pre>
     *
     * @return Flattened / joined one level
     */
    static <T1> ReactiveSeq<T1> flatten(ReactiveSeq<? extends ReactiveSeq<T1>> nested){
        return nested.flatMap(Function.identity());
    }
    static <T1> ReactiveSeq<T1> flattenI(ReactiveSeq<? extends Iterable<T1>> nested){
        return nested.flatMapI(Function.identity());
    }
    static <T1> ReactiveSeq<T1> flattenO(ReactiveSeq<? extends Optional<T1>> nested){
        return nested.flatMap(Streams::optionalToStream);
    }



    /**
     * Convert toNested a Stream with the values infinitely cycled
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
     * positions so a limit can be safely applied toNested the leading reactiveStream. Not
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
     * @return duplicated reactiveStream
     */
    Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate();
    Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate(Supplier<Deque<T>> bufferFactory);

    /**
     * Triplicates a Stream Buffers intermediate values, leaders may change
     * positions so a limit can be safely applied toNested the leading reactiveStream. Not
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

    Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate(Supplier<Deque<T>> bufferFactory);

    /**
     * Makes four copies of a Stream Buffers intermediate values, leaders may
     * change positions so a limit can be safely applied toNested the leading reactiveStream.
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
    Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate(Supplier<Deque<T>> bufferFactory);

    /**
     * Split a Stream at it's head (similar toNested headAndTail)
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
    Tuple2<Optional<T>, ReactiveSeq<T>> splitAtHead();

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
     * Split reactiveStream at point where predicate no longer holds
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
    Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(Predicate<? super T> splitter);

    /**
     * Convert toNested a Stream with the result of a reduction operation repeated
     * specified times
     *
     * <pre>
     * {@code
     *   List<Integer> list = ReactiveSeq.of(1,2,2))
     *                                 .cycle(Reducers.toCountInt(),3)
     *                                 .collect(CyclopsCollectors.toList());
     *   //List[3,3,3];
     *   }
     * </pre>
     *
     * @param m
     *            Monoid toNested be used in reduction
     * @param times
     *            Number of times value should be repeated
     * @return Stream with reduced values repeated
     */
    @Override
    default ReactiveSeq<T> cycle(Monoid<T> m, long times){
        return unit(m.reduce(this)).cycle(times);
    }

    /**
     * Repeat in a Stream while specified predicate holds
     *
     * <pre>
     * {@code
     *
     * 	MutableInt count = MutableInt.of(0);
     * 	ReactiveSeq.of(1, 2, 2).cycleWhile(next -> count++ < 6)
     *             .collect(CyclopsCollectors.toList());
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
    default ReactiveSeq<T> cycleWhile(Predicate<? super T> predicate){
        return cycle().limitWhile(predicate);
    }

    /**
     * Repeat in a Stream until specified predicate holds
     *
     * <pre>
     * {@code
     * 	MutableInt count =MutableInt.of(0);
     * 		ReactiveSeq.of(1,2,2)
     * 		 		.cycleUntil(next -> count.get()>6)
     * 		 		.peek(i-> count.mutate(i->i+1))
     * 		 		.collect(CyclopsCollectors.toList());
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
    default ReactiveSeq<T> cycleUntil(Predicate<? super T> predicate){
        return cycleWhile(predicate.negate());

    }





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
    <U> ReactiveSeq<Tuple2<T, U>> zipS(final Stream<? extends U> other);

    default <U, R> ReactiveSeq<R> zipLatest(final Publisher<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
         return zipP(other, zipper);
    }

    @Override
    default <U> ReactiveSeq<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return zipS(ReactiveSeq.fromIterable(other));
    }



    /**
     * zip 3 Streams into one
     *
     * <pre>
     * {@code
     *  List<Tuple3<Integer, Integer, Character>> list = of(1, 2, 3, 4, 5, 6).zip3(of(100, 200, 300, 400), of('a', 'b', 'c')).collect(CyclopsCollectors.toList());
     *
     *  // [[1,100,'a'],[2,200,'b'],[3,300,'c']]
     * }
     *
     * </pre>
     */
    @Override
    <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third);

    /**
     * zip 4 Streams into 1
     *
     * <pre>
     * {@code
     *  List<Tuple4<Integer, Integer, Character, String>> list = of(1, 2, 3, 4, 5, 6).zip4(of(100, 200, 300, 400), of('a', 'b', 'c'), of("hello", "world"))
     *          .collect(CyclopsCollectors.toList());
     *
     * }
     * // [[1,100,'a',"hello"],[2,200,'b',"world"]]
     * </pre>
     */
    @Override
    <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth);

    default Seq<T> seq(){
        return Seq.seq((Stream<T>)this);
    }
    @Override
   default ReactiveSeq<T> shuffle(final Random random) {
        return coflatMap(r->{ List<T> list = r.toList(); Collections.shuffle(list,random); return list;})
                .flatMap(c->c.stream());

    }

    @Override
    default ReactiveSeq<T> slice(final long from, final long to) {

        return skip(Math.max(from, 0)).limit(Math.max(to - Math.max(from, 0), 0));

    }

    @Override
    default <U extends Comparable<? super U>> ReactiveSeq<T> sorted(final Function<? super T, ? extends U> function) {
        return sorted(Comparator.comparing(function));
    }



    @Override
    default ReactiveSeq<T> shuffle() {
        return coflatMap(r->{ List<T> list = r.toList(); Collections.shuffle(list); return list;})
                .flatMap(c->c.stream());

    }
    @Override
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        return seq().foldLeft(identity, accumulator);

    }


    default <U> ReactiveSeq<T> sorted(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
        return sorted(Comparator.comparing(function, comparator));

    }






    /**
     * Add an index toNested the current Stream
     *
     * <pre>
     * {@code
     * assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
     * }
     * </pre>
     */
    @Override
    default ReactiveSeq<Tuple2<T, Long>> zipWithIndex() {
        return zipS(ReactiveSeq.rangeLong(0,Long.MAX_VALUE));
    }



    /**
     * Create a sliding view over this Sequence
     *
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(2).collect(CyclopsCollectors.toList());
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
    default ReactiveSeq<VectorX<T>> sliding(int windowSize){
        return sliding(windowSize,1);
    }

    /**
     * Create a sliding view over this Sequence
     *
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(CyclopsCollectors.toList());
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
    ReactiveSeq<VectorX<T>> sliding(int windowSize, int increment);

    /**
     * Group elements in a Stream
     *
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).grouped(3).collect(CyclopsCollectors.toList());
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
     * current value and can choose toNested aggregate the current value or create a
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
    <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyUntil(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                      Function<? super C, ? extends R> finalizer);
    ReactiveSeq<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate);
    <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyWhile(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                      Function<? super C, ? extends R> finalizer);
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
     * @param time (Max) time period toNested build a singleUnsafe batch in
     * @param t time unit for batch
     * @return ReactiveSeq batched by size and time
     */
    ReactiveSeq<ListX<T>> groupedBySizeAndTime(int size, long time, TimeUnit t);

    /**
     * Batch elements by size into a toX created by the supplied factory
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
     *            (Max) time period toNested build a singleUnsafe batch in
     * @param unit
     *            time unit for batch
     * @param factory
     *            Collection factory
     * @return ReactiveSeq batched by size and time
     */
    <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory);

    <C extends Collection<? super T>,R> ReactiveSeq<R> groupedBySizeAndTime(final int size, final long time,
                                                                                   final TimeUnit unit,
                                                                                   final Supplier<C> factory,
                                                                                   Function<? super C, ? extends R> finalizer
    );
    <C extends Collection<? super T>,R> ReactiveSeq<R> groupedByTime(final long time, final TimeUnit unit,
                                                                     final Supplier<C> factory, Function<? super C, ? extends R> finalizer);
    /**
     * Batch elements in a Stream by time period
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.SECONDS).collect(CyclopsCollectors.toList()).size(),is(1));
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.NANOSECONDS).collect(CyclopsCollectors.toList()).size(),greaterThan(5));
     * }
     * </pre>
     *
     * @param time
     *            - time period toNested build a singleUnsafe batch in
     * @param t
     *            time unit for batch
     * @return ReactiveSeq batched into lists by time period
     */
    ReactiveSeq<ListX<T>> groupedByTime(long time, TimeUnit t);

    /**
     * Batch elements by time into a toX created by the supplied factory
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
     *            - time period toNested build a singleUnsafe batch in
     * @param unit
     *            time unit for batch
     * @param factory
     *            Collection factory
     * @return ReactiveSeq batched into toX types by time period
     */
    <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(long time, TimeUnit unit, Supplier<C> factory);

    /**
     * Batch elements in a Stream by size into a toX created by the
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
     * @return ReactiveSeq batched into toX types by size
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
     *            Batch until predicate holds, applyHKT open next batch
     * @return ReactiveSeq batched into lists determined by the predicate supplied
     */
    @Override
    default ReactiveSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate){
        return groupedWhile(predicate.negate());

    }
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
     *            Batch while predicate holds, applyHKT open next batch
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
     *            Batch while predicate holds, applyHKT open next batch
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
     *            Batch until predicate holds, applyHKT open next batch
     * @param factory
     *            Collection factory
     * @return ReactiveSeq batched into collections determined by the predicate
     *         supplied
     */
    @Override
    default <C extends Collection<? super T>> ReactiveSeq<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory){
        return groupedWhile(predicate.negate(),factory);
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#grouped(java.util.function.Function, java.util.reactiveStream.Collector)
     */
    @Override
    default <K, A, D> ReactiveSeq<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return fromStream(seq().grouped(classifier, downstream));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#grouped(java.util.function.Function)
     */
    @Override
    default <K> ReactiveSeq<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        Seq<? extends Tuple2<? extends K, ReactiveSeq<T>>> grouped = seq().grouped(classifier).map(t -> t.map2(s -> ReactiveSeq.<T>fromStream(s)));
        return fromStream((Seq)grouped);
    }

    /**
     * Use classifier function toNested group elements in this Sequence into a Map
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
        return (MapX<K, ListX<T>>)this.collect(Collectors.groupingBy(classifier, (Supplier)MapX::empty, ListX.<T>listXCollector()));
    }

    /*
     * Return the distinct Stream of elements
     *
     * <pre> {@code List<Integer> list = ReactiveSeq.of(1,2,2,2,5,6) .distinct()
     * .collect(CyclopsCollectors.toList()); }</pre>
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
    default ReactiveSeq<T> scanLeft(Monoid<T> monoid){

            return scanLeft(monoid.zero(),monoid);



    }

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
    default ReactiveSeq<T> scanRight(Monoid<T> monoid){
        return reverse().scanLeft(monoid.zero(), (u, t) -> monoid.apply(t, u));
    }


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
    default <U> ReactiveSeq<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner){
        return reverse().scanLeft(identity,(u,t)->combiner.apply(t,u));
    }

    /**
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
     * </pre>
     *
     */
    @Override
    ReactiveSeq<T> sorted();


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default ReactiveSeq<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return fromStream(Streams.combine(this, predicate, op));
    }


    @Override
    default ReactiveSeq<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (ReactiveSeq<T>)FoldableTraversable.super.combine(op,predicate);
    }

    /**
     * <pre>
     * {@code
     * 	assertThat(ReactiveSeq.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
     * }
     * </pre>
     *
     * @param c
     *            Compartor toNested sort with
     * @return Sorted Stream
     */
    @Override
   default  ReactiveSeq<T> sorted(Comparator<? super T> c){

            return coflatMap(r-> {
		    List<T> list = r.collect(Collectors.toList());
		    list.sort(c);
		    return list;
		    })
                    .flatMap(col->col.stream());


    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default ReactiveSeq<T> takeWhile(final Predicate<? super T> p) {

        return (ReactiveSeq<T>) FoldableTraversable.super.takeWhile(p);
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
     * @see com.aol.cyclops2.types.traversable.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default ReactiveSeq<T> dropWhile(final Predicate<? super T> p) {

        return (ReactiveSeq<T>) FoldableTraversable.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default ReactiveSeq<T> takeUntil(final Predicate<? super T> p) {

        return (ReactiveSeq<T>) FoldableTraversable.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default ReactiveSeq<T> dropUntil(final Predicate<? super T> p) {

        return (ReactiveSeq<T>) FoldableTraversable.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#dropRight(int)
     */
    @Override
    default ReactiveSeq<T> dropRight(final int num) {

        return (ReactiveSeq<T>) FoldableTraversable.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeRight(int)
     */
    @Override
    default ReactiveSeq<T> takeRight(final int num) {

        return (ReactiveSeq<T>) FoldableTraversable.super.takeRight(num);
    }

    /**
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
     * </pre>
     *
     *
     *
     * @param num
     *            Number of elemenets toNested skip
     * @return Stream with specified number of elements skipped
     */
    @Override
    ReactiveSeq<T> skip(long num);

    /**
     * Performs an action for each element of this Stream.
     *
     * For potentially non-blocking analogs see {@link ReactiveSeq#forEachAsync(Consumer)}   and forEach overloads
     * such as {@link ReactiveSeq#forEach(Consumer, Consumer)} and {@link ReactiveSeq#forEach(Consumer, Consumer,Runnable)}
     *
     * This method overrides the JDK {@link java.util.stream.Stream#forEach(Consumer)}  and maintains it's blocking
     * semantics. Other forEach overloads in ReactiveSeq are non-blocking for asynchronously executing Streams.
     *
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     * <p>
     * <p>The behavior of this operation is explicitly nondeterministic.
     * For parallel reactiveStream pipelines, this operation does <em>not</em>
     * guarantee toNested respect the encounter order of the reactiveStream, as doing so
     * would sacrifice the benefit of parallelism.  For any given element, the
     * action may be performed at whatever time and in whatever thread the
     * library chooses.  If the action accesses shared state, it is
     * responsible for providing the required synchronization.
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action toNested perform on the elements
     */
    @Override
    void forEach(Consumer<? super T> action);

    /**
     * A potentially non-blocking analog of {@link ReactiveSeq#forEach}.
     * For push based reactive Stream types (created via Spouts or FutureStream)
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action toNested perform on the elements
     */
    default void forEachAsync(final Consumer<? super T> action){
        forEach(action);
    }
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
     *            Predicate toNested skip while true
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
     *            Predicate toNested skip until true
     * @return Stream with elements skipped until predicate holds
     */
    @Override
    default ReactiveSeq<T> skipUntil(Predicate<? super T> p) {
        return skipWhile(p.negate());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#skipUntilClosed(java.util.function.Predicate)
     */
    default ReactiveSeq<T> skipUntilClosed(final Predicate<? super T> p) {
        return skipWhileClosed(p.negate());
    }

    /**
     *
     *
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
     * </pre>
     *
     * @param num
     *            Limit element size toNested num
     * @return Monad converted toNested Stream with elements up toNested num
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
     * True if predicate matches all elements when Monad converted toNested a Stream
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     *
     * @param c
     *            Predicate toNested check if all match
     */
    @Override
    boolean allMatch(Predicate<? super T> c);

    /**
     * True if a singleUnsafe element matches when Monad converted toNested a Stream
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
     * }
     * </pre>
     *
     * @param c
     *            Predicate toNested check if any match
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
     * Lazy / reactive analogue of findFirst / findAny from JDK
     * For push based reactive-streams (created via Spouts.XXX) data will be pushed toNested the returned Maybe on arrival.
     * For pull based Streams (created via ReactiveSeq.XXX) the Stream will be executed when the Maybe is takeOne accessed.
     *
     * @return
     */
    Maybe<T> findOne();

    /**
     * Lazy / reactive look up of takeOne value , capturing the takeOne error, if one occurs. If no values are
     * present a NoSuchElementException is returned.
     *
     * For push based reactive-streams (created via Spouts.XXX) data will be pushed toNested the returned Either on arrival.
     * For pull based Streams (created via ReactiveSeq.XXX) the Stream will be executed when the Either is takeOne accessed.

     *
     * @return
     */
    Either<Throwable,T> findFirstOrError();
    /**
     * @return takeOne matching element, but order is not guaranteed
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
     * Attempt toNested map this Sequence toNested the same type as the supplied Monoid
     * (Reducer) Then use Monoid toNested reduce values
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
     *            Monoid toNested reduce values
     * @return Reduce result
     */
    @Override
    <R> R mapReduce(Reducer<R> reducer);

    /**
     * Attempt toNested map this Monad toNested the same type as the supplied Monoid, using
     * supplied function Then use Monoid toNested reduce values
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
     *            Function toNested map Monad type
     * @param reducer
     *            Monoid toNested reduce values
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
     *            Use supplied Monoid toNested reduce values
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
     * @see java.util.reactiveStream.Stream#reduce(java.lang.Object,
     * java.util.function.BinaryOperator)
     */
    @Override
    T reduce(T identity, BinaryOperator<T> accumulator);

    default ReactiveSeq<T> reduceAll(T identity, BinaryOperator<T> accumulator){
        return coflatMap(s->s.reduce(identity,accumulator));
    }
    /*
     * (non-Javadoc)
     *
     * @see java.util.reactiveStream.Stream#reduce(java.lang.Object,
     * java.util.function.BiFunction, java.util.function.BinaryOperator)
     */
    @Override
    <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this reactiveStream.  A mutable
     * reduction is one in which the reduced value is a mutable result container,
     * such as an {@code ArrayList}, and elements are incorporated by updating
     * the state of the result rather than by replacing the result.  This
     * produces a result equivalent toNested:
     * <pre>{@code
     *     R result = supplier.get();
     *     for (T element : this reactiveStream)
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
     * well-suited for use with method references as arguments toNested {@code collect()}.
     * For example, the following will accumulate strings into an {@code ArrayList}:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(ArrayList::new, ArrayList::add,
     *                                                ArrayList::addAll);
     * }</pre>
     * <p>
     * <p>The following will take a reactiveStream of strings and concatenates them into a
     * singleUnsafe string:
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

    default <R, A> ReactiveSeq<R> collectStream(Collector<? super T, A, R> collector){
        return coflatMap(s->s.collect(collector));
    }
    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this reactiveStream using a
     * {@code Collector}.  A {@code Collector}
     * encapsulates the function used as arguments toNested
     * {@link #collect(Supplier, BiConsumer, BiConsumer)}, allowing for reuse of
     * toX strategies and composition of collect operations such as
     * multiple-level grouping or partitioning.
     * <p>
     * <p>If the reactiveStream is parallel, and the {@code Collector}
     * is {@link Collector.Characteristics#CONCURRENT concurrent}, and
     * lazy the reactiveStream is unordered or the collector is
     * {@link Collector.Characteristics#UNORDERED unordered},
     * applyHKT a concurrent reduction will be performed (see {@link Collector} for
     * details on concurrent reduction.)
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     * <p>
     * <p>When executed in parallel, multiple intermediate results may be
     * instantiated, populated, and merged so as toNested maintain isolation of
     * mutable data structures.  Therefore, even when executed in parallel
     * with non-thread-safe data structures (such as {@code ArrayList}), no
     * additional synchronization is needed for a parallel reduction.
     *
     * @param collector the {@code Collector} describing the reduction
     * @return the result of the reduction
     * @apiNote The following will accumulate strings into an ArrayList:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(CyclopsCollectors.toList());
     * }</pre>
     * <p>
     * <p>The following will classify {@code Person} objects by city:
     * <pre>{@code
     *     Map<String, List<Person>> peopleByCity
     *         = personStream.collect(CyclopsCollectors.groupingBy(Person::getCity));
     * }</pre>
     * <p>
     * <p>The following will classify {@code Person} objects by state and city,
     * cascading two {@code Collector}s together:
     * <pre>{@code
     *     Map<String, Map<String, List<Person>>> peopleByStateAndCity
     *         = personStream.collect(CyclopsCollectors.groupingBy(Person::getState,
     *                                                      CyclopsCollectors.groupingBy(Person::getCity)));
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
     * streamedMonad() takeOne. I.e. streamedMonad().reduce(reducer)
     *
     * <pre>
     * {@code
     *  Monoid<Integer> sum = Monoid.of(0, (a, b) -> a + b);
     *  Monoid<Integer> mult = Monoid.of(1, (a, b) -> a * b);
     *  List<Integer> result = ReactiveSeq.of(1, 2, 3, 4).reduce(Arrays.asList(sum, mult).reactiveStream());
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
     * streamedMonad() takeOne. I.e. streamedMonad().reduce(reducer)
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
     *            Use supplied Monoid toNested reduce values starting via foldRight
     * @return Reduced result
     */
    @Override
    T foldRight(Monoid<T> reducer);

    /**
     * Immutable reduction from right toNested left
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
     * Attempt toNested map this Monad toNested the same type as the supplied Monoid (using
     * mapToType on the monoid interface) Then use Monoid toNested reduce values
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
     *            Monoid toNested reduce values
     * @return Reduce result
     **/
    @Override
    public <T> T foldRightMapToType(Reducer<T> reducer);


    /**
     * @return This Stream converted toNested a set
     */
    default Set<T> toSet(){
        return collect(Collectors.toSet());
    }

    /**
     * @return this Stream converted toNested a list
     */
    default List<T> toList(){
        return collect(Collectors.toList());
    }



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
     * @return this ReactiveSeq converted toNested AnyM format
     */
    public AnyMSeq<reactiveSeq,T> anyM();

    /*
     * (non-Javadoc)
     *
     * @see java.util.reactiveStream.Stream#map(java.util.function.Function)
     */
    @Override
    <R> ReactiveSeq<R> map(Function<? super T, ? extends R> fn);


    /*
     * (non-Javadoc)
     *
     * @see java.util.reactiveStream.Stream#peek(java.util.function.Consumer)
     */
    @Override
    default ReactiveSeq<T> peek(Consumer<? super T> c){

            return map(i->{c.accept(i); return i;});

    }

    /**
     * flatMap operation
     *
     * <pre>
     * {@code
     * 	assertThat(ReactiveSeq.of(1,2)
     * 						.flatMap(i -> asList(i, -i).reactiveStream())
     * 						.toList(),equalTo(asList(1, -1, 2, -2)));
     *
     * }
     * </pre>
     *
     * @param fn
     *            toNested be applied
     * @return new stage in Sequence with flatMap operation toNested be lazily applied
     */
    @Override
    <R> ReactiveSeq<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn);

    /**
     * Allows flatMap return type toNested be any Monad type
     *
     * <pre>
     * {@code
     * 	assertThat(ReactiveSeq.of(1,2,3)).flatMapAnyM(i-> fromEither5(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));
     *
     * }
     * </pre>
     *
     *
     * @param fn
     *            toNested be applied
     * @return new stage in Sequence with flatMap operation toNested be lazily applied
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
    <R> ReactiveSeq<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn);

    <R> ReactiveSeq<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn);
    <R> ReactiveSeq<R> flatMapP(int maxConcurrency,Function<? super T, ? extends Publisher<? extends R>> fn);
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
     *            toNested be applied
     * @return new stage in Sequence with flatMap operation toNested be lazily applied
     */
    <R> ReactiveSeq<R> flatMapStream(Function<? super T, BaseStream<? extends R, ?>> fn);

    /*
     * (non-Javadoc)
     *
     * @see java.util.reactiveStream.Stream#filter(java.util.function.Predicate)
     */
    @Override
    ReactiveSeq<T> filter(Predicate<? super T> fn);



    /**
     * Returns a spliterator for the elements of this reactiveStream.
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return the element spliterator for this reactiveStream
     */
    @Override
    Spliterator<T> spliterator();

    /* (non-Javadoc)
         * @see java.util.reactiveStream.BaseStream#sequential()
         */
    @Override
    ReactiveSeq<T> sequential();

    /*
     * (non-Javadoc)
     *
     * @see java.util.reactiveStream.BaseStream#unordered()
     */
    @Override
    ReactiveSeq<T> unordered();

    /**
     * Returns a reactiveStream with a given value interspersed between any two values
     * of this reactiveStream.
     *
     *
     * // (1, 0, 2, 0, 3, 0, 4) ReactiveSeq.of(1, 2, 3, 4).intersperse(0)
     *
     */
    @Override
    default ReactiveSeq<T> intersperse(T value){
        return flatMap(t -> Stream.of(value, t)).skip(1l);
    }

    /**
     * Keep only those elements in a reactiveStream that are of a given type.
     *
     *
     * // (1, 2, 3) ReactiveSeq.of(1, "a", 2, "b",3).ofType(Integer.class)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    default <U> ReactiveSeq<U> ofType(Class<? extends U> type){
        return (ReactiveSeq<U>)FoldableTraversable.super.ofType(type);
    }

    /**
     * Cast all elements in a reactiveStream toNested a given type, possibly throwing a
     * {@link ClassCastException}.
     *
     *
     * // ClassCastException ReactiveSeq.of(1, "a", 2, "b", 3).cast(Integer.class)
     *
     */
    @Override
    default <U> ReactiveSeq<U> cast(Class<? extends U> type){
        return (ReactiveSeq<U>)FoldableTraversable.super.cast(type);
    }



    /*
     * Potentially efficient Sequence reversal. Is efficient if
     *
     * - Sequence created via a range - Sequence created via a List - Sequence
     * created via an Array / var args
     *
     * Otherwise Sequence collected into a Collection prior toNested reversal
     *
     * <pre> {@code assertThat( of(1, 2, 3).reverse().toList(),
     * equalTo(asList(3, 2, 1))); } </pre>
     */
    @Override
    public ReactiveSeq<T> reverse();

    /*
     * (non-Javadoc)
     *
     * @see java.util.reactiveStream.BaseStream#onClose(java.lang.Runnable)
     */
    @Override
    public ReactiveSeq<T> onClose(Runnable closeHandler);






    /**
     * Prepend Stream toNested this ReactiveSeq
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3)
     *                                   .prependS(of(100, 200, 300))
     *                                   .map(it -> it + "!!")
     *                                   .collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("100!!", "200!!", "300!!", "1!!", "2!!", "3!!")));
     * }
     * </pre>
     *
     * @param stream
     *            toNested Prepend
     * @return ReactiveSeq with Stream prepended
     */
    ReactiveSeq<T> prependS(Stream<? extends T> stream);

    /**
     * Append values toNested the take of this ReactiveSeq
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).append(100, 200, 300).map(it -> it + "!!").collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "3!!", "100!!", "200!!", "300!!")));     * }
     * </pre>
     *
     * @param values
     *            toNested append
     * @return ReactiveSeq with appended values
     */
    ReactiveSeq<T> append(T... values);


    ReactiveSeq<T> append(T value);


    ReactiveSeq<T> prepend(T value);

    /**
     * Prepend given values toNested the skip of the Stream
     *
     * <pre>
     * {@code
     * List<String> result = 	ReactiveSeq.of(1,2,3)
     * 									 .prepend(100,200,300)
     * 										 .map(it ->it+"!!")
     * 										 .collect(CyclopsCollectors.toList());
     *
     * 			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * @param values toNested prepend
     * @return ReactiveSeq with values prepended
     */

    ReactiveSeq<T> prepend(T... values);

    /**
     * Insert data into a reactiveStream at given position
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).insertAt(1, 100, 200, 300).map(it -> it + "!!").collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));     *
     * }
     * </pre>
     *
     * @param pos
     *            toNested insert data at
     * @param values
     *            toNested insert
     * @return Stream with new data inserted
     */
    default ReactiveSeq<T> insertAt(int pos, T... values){
        if(pos==0){
            return prepend(values);
        }
        long check =  new Long(pos);
        boolean added[] = {false};

        return  zipWithIndex().flatMap(t-> {
                    if (t.v2 < check && !added[0])
                        return ReactiveSeq.of(t.v1);
                    if (!added[0]) {
                        added[0] = true;
                        return ReactiveSeq.concat(ReactiveSeq.of(values),ReactiveSeq.of(t.v1));
                    }
                    return Stream.of(t.v1);
                }
        );


    }

    /**
     * Delete elements between given indexes in a Stream
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3, 4, 5, 6).deleteBetween(2, 4).map(it -> it + "!!").collect(CyclopsCollectors.toList());
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
    default ReactiveSeq<T> deleteBetween(int start, int end){
        long check =  new Long(start);
        long endCheck = new Long(end);

        return  zipWithIndex().flatMap(t-> {
                    if (t.v2 < check)
                        return ReactiveSeq.of(t.v1);
                    if (t.v2 < endCheck) {

                        return ReactiveSeq.of();
                    }
                    return ReactiveSeq.of(t.v1);
                }
        );
    }

    /**
     * Insert a Stream into the middle of this reactiveStream at the specified position
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).insertAtS(1, of(100, 200, 300)).map(it -> it + "!!").collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));
     * }
     * </pre>
     *
     * @param pos
     *            toNested insert Stream at
     * @param stream
     *            toNested insert
     * @return newly conjoined ReactiveSeq
     */
    default ReactiveSeq<T> insertAtS(int pos, Stream<T> stream){
        if(pos==0){
            return prependS(stream);
        }
        long check =  new Long(pos);
        boolean added[] = {false};

        return  zipWithIndex().flatMap(t-> {
                    if (t.v2 < check && !added[0])
                        return ReactiveSeq.of(t.v1);
                    if (!added[0]) {
                        added[0] = true;
                        return ReactiveSeq.concat(stream,ReactiveSeq.of(t.v1));
                    }
                    return Stream.of(t.v1);
                }
        );
    }



    /**
     * <pre>
     * {@code
     *  assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
     * 				.endsWith(Arrays.asList(5,6)));
     *
     * }
     *
     * @param iterable Values toNested check
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
     *            Values toNested check
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
     * .collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1,2,3)));
     *
     * @param num
     * @return
     */
    @Override
    ReactiveSeq<T> skipLast(int num);

    /**
     * Limit results toNested the last x elements in a ReactiveSeq
     *
     * <pre>
     * {@code
     * 	assertThat(ReactiveSeq.of(1,2,3,4,5)
     * 							.limitLast(2)
     * 							.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(4,5)));
     *
     * }
     *
     * @param num of elements toNested return (last elements)
     * @return ReactiveSeq limited toNested last num elements
     */
    @Override
    ReactiveSeq<T> limitLast(int num);

    /**
     * Turns this ReactiveSeq into a HotStream, a connectable Stream, being executed on a thread on the
     * supplied executor, that is producing data. Note this method creates a HotStream that starts emitting data
     * immediately. For a hotStream that waits until the takeOne user streams connect @see {@link ReactiveSeq#primedHotStream(Executor)}.
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
     *            Executor toNested execute this ReactiveSeq on
     * @return a Connectable HotStream
     */
    default HotStream<T> hotStream(final Executor e) {
        return Streams.hotStream(this, e);
    }

    /**
     * Return a HotStream that will skip emitting data when the takeOne connecting Stream connects.
     * Note this method creates a HotStream that starts emitting data only when the takeOne connecting Stream connects.
     *  For a hotStream that starts toNested emitted data immediately @see {@link ReactiveSeq#hotStream(Executor)}.
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
    default HotStream<T> primedHotStream(Executor e){
        return Streams.primedHotStream(this, e);
    }




    /**
     * Turns this ReactiveSeq into a HotStream, a connectable & pausable Stream, being executed on a thread on the
     * supplied executor, that is producing data. Note this method creates a HotStream that starts emitting data
     * immediately. For a hotStream that waits until the takeOne user streams connect @see {@link ReactiveSeq#primedPausableHotStream(Executor)}.
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
     * @param e Executor toNested execute this ReactiveSeq on
     * @return a Connectable HotStream
     */
    default PausableHotStream<T> pausableHotStream(Executor e){
        return Streams.pausableHotStream(this, e);
    }

    /**
     * Return a pausable HotStream that will skip emitting data when the takeOne connecting Stream connects.
     * Note this method creates a HotStream that starts emitting data only when the takeOne connecting Stream connects.
     *  For a hotStream that starts toNested emitted data immediately @see {@link ReactiveSeq#pausableHotStream(Executor)}.
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
    default PausableHotStream<T> primedPausableHotStream(Executor e){
        return Streams.primedPausableHotStream(this, e);
    }

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
     * @return takeOne value in this Stream
     */
    @Override
    T firstValue();

    /**
     * <pre>
     * {@code
     *
     *    //1
     *    ReactiveSeq.of(1).singleUsafe();
     *
     *    //UnsupportedOperationException
     *    ReactiveSeq.of().singleUnsafe();
     *
     *     //UnsupportedOperationException
     *    ReactiveSeq.of(1,2,3).singleUnsafe();
     * }
     * </pre>
     *
     * @return a singleUnsafe value or an UnsupportedOperationException if 0/1 values
     *         in this Stream
     */
    @Override
    default T singleUnsafe() {
        final Iterator<T> it = iterator();
        if (it.hasNext()) {
            final T result = it.next();
            if (!it.hasNext())
                return result;
        }
        throw new UnsupportedOperationException(
                                                "singleUnsafe only works for Streams with a singleUnsafe value");

    }

    @Override
    default Maybe<T> single(final Predicate<? super T> predicate) {
        return this.filter(predicate)
                   .single();

    }

    /**
     * <pre>
     * {@code
     *
     *    //Maybe[1]
     *    ReactiveSeq.of(1).singleUnsafe();
     *
     *    //Maybe.none
     *    ReactiveSeq.of().singleUnsafe();
     *
     *     //Maybe.none
     *    ReactiveSeq.of(1,2,3).singleUnsafe();
     * }
     * </pre>
     *
     * @return An Maybe with singleUnsafe value if this Stream has exactly one
     *         element, otherwise Maybe.none
     */
    @Override
    default Maybe<T> single() {
        final Iterator<T> it = iterator();

        return Maybe.<Object>fromEvalNullable(Eval.later(() -> {
            if(it.hasNext()) {
                Object res = it.next();
                if(it.hasNext())
                    return null;
                if(res==null)
                    res = Queue.NILL;
                return res;
            }
            else
             return null;
        })).<T>map(i->{
            if(i==Queue.NILL)
                return null;
            return (T)i;
        });



    }
    default Maybe<T> takeOne() {
        return Maybe.fromIterable(this);

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
     *            toNested extract element from
     * @return elementAt index
     */
    @Override
    default Maybe<T> get(final long index) {
        return this.zipWithIndex()
                   .filter(t -> t.v2 == index)
                   .takeOne()
                   .map(t -> t.v1());
    }

    /**
     * Gets the element at index, and returns a Tuple containing the element (it
     * must be present) and a maybe copy of the Sequence for further processing.
     *
     * <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5).get(2).v1
     * //3
     * }
     * </pre>
     *
     * @param index
     *            toNested extract element from
     * @return Element and Sequence
     */
    default Tuple2<T, ReactiveSeq<T>> elementAt(final long index) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> tuple = this.duplicate();
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
     * @return Sequence that adds the time between elements in millis toNested each
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
     * @return Sequence that adds a timestamp toNested each element
     */
    default ReactiveSeq<Tuple2<T, Long>> timestamp() {
        return zip(ReactiveSeq.generate(() -> System.currentTimeMillis()));
    }


    public static <T> ReactiveSeq<T> empty() {
        return fromStream(Stream.empty());
    }

    public static <T> ReactiveSeq<T> ofNullable(T nullable){
        if(nullable==null){
            return empty();
        }
        return of(nullable);
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
                                                                                    elements,0, elements.length, false);
        return Streams.reactiveSeq(array, Optional.ofNullable(array));

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
                                                                                    elements,0,elements.length, false).invert();
        return Streams.reactiveSeq(array, Optional.ofNullable(array));

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
        return Streams.reactiveSeq(list, Optional.ofNullable(list));

    }

    /**
     * Create an efficiently reversable Sequence that produces the integers
     * between skip and take
     *
     * @param start
     *            Number of range toNested skip from
     * @param end
     *            Number for range toNested take at
     * @return Range ReactiveSeq
     */
    public static ReactiveSeq<Integer> range(final int start, final int end) {
        if(start>end)
            return range(end,start);
        final ReversingRangeIntSpliterator range = new ReversingRangeIntSpliterator(
                                                                                    start, end, false);
        return Streams.reactiveSeq(range, Optional.ofNullable(range));

    }

    /**
     * Create an efficiently reversable Sequence that produces the integers
     * between skip and take
     *
     * @param start
     *            Number of range toNested skip from
     * @param end
     *            Number for range toNested take at
     * @return Range ReactiveSeq
     */
    public static ReactiveSeq<Long> rangeLong(final long start, final long end) {
        if(start>end)
            return rangeLong(end,start);
        final ReversingRangeLongSpliterator range = new ReversingRangeLongSpliterator(
                                                                                      start, end, false);
        return Streams.reactiveSeq(range, Optional.ofNullable(range));

    }

    default boolean isReplayable(){
        return getClass()!=OneShotStreamX.class;
    }

    /**
     * Construct a ReactiveSeq from a Stream
     *
     * @param stream
     *            Stream toNested construct Sequence from
     * @return
     */
    public static <T> ReactiveSeq<T> fromStream(final Stream<T> stream) {
        Objects.requireNonNull(stream);
        if (stream instanceof ReactiveSeq)
            return (ReactiveSeq<T>) stream;
        return Streams.reactiveSeq(stream, Optional.empty());
    }
    public static <T> ReactiveSeq<T> oneShotStream(final Stream<T> stream) {
        Objects.requireNonNull(stream);
        if (stream instanceof ReactiveSeq)
            return (ReactiveSeq<T>) stream;
        return Streams.oneShotStream(stream);
    }

    /**
     * Construct a ReactiveSeq from a Stream
     *
     * @param stream
     *            Stream toNested construct Sequence from
     * @return
     */
    public static ReactiveSeq<Integer> fromIntStream(final IntStream stream) {
        Objects.requireNonNull(stream);
        return Streams.reactiveSeq(stream.boxed(), Optional.empty());

    }

    /**
     * Construct a ReactiveSeq from a Stream
     *
     * @param stream
     *            Stream toNested construct Sequence from
     * @return
     */
    public static ReactiveSeq<Long> fromLongStream(final LongStream stream) {
        Objects.requireNonNull(stream);
        return Streams.reactiveSeq(stream.boxed(), Optional.empty());
    }

    /**
     * Construct a ReactiveSeq from a Stream
     *
     * @param stream
     *            Stream toNested construct Sequence from
     * @return
     */
    public static ReactiveSeq<Double> fromDoubleStream(final DoubleStream stream) {
        Objects.requireNonNull(stream);
        return Streams.reactiveSeq(stream.boxed(), Optional.empty());
    }

    /**
     * Construct a ReactiveSeq from a List (prefer this method if the source is a
     * list, as it allows more efficient Stream reversal).
     *
     * @param list
     *            toNested construct Sequence from
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> fromList(final List<T> list) {
        Objects.requireNonNull(list);
        final ReversingListSpliterator array = new ReversingListSpliterator<T>(
                                                                               list, false);
        return Streams.reactiveSeq(array, Optional.ofNullable(array));
    }
    public static <T> ReactiveSeq<T> oneShotList(final List<T> list) {
        Objects.requireNonNull(list);
        final ReversingListSpliterator array = new ReversingListSpliterator<T>(
                list, false);
        return Streams.oneShotStream(array, Optional.ofNullable(array));
    }

    /**
     * Construct a ReactiveSeq from an Publisher
     *
     * @param publisher
     *            toNested construct ReactiveSeq from
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> fromPublisher(final Publisher<? extends T> publisher) {
        Objects.requireNonNull(publisher);
       if(publisher instanceof ReactiveSeq){
            return (ReactiveSeq)publisher;
        }
        return Spouts.from(publisher);
    }

    public static <T> ReactiveSeq<T> generate(Generator<T> gen){
        return gen.stream();
    }
    /**
     * Construct a ReactiveSeq from an Iterable
     *
     * @param iterable
     *            toNested construct Sequence from
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> fromIterable(final Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        if (iterable instanceof ReactiveSeq) {
            return (ReactiveSeq<T>)iterable;

        }
        if( iterable instanceof LazyFluentCollectionX){
            return ((LazyFluentCollectionX<T>)iterable).stream();
        }

        //we can't just use the Iterable's Spliteratable as it might not be repeatable / copyable.
        return Streams.reactiveSeq(new IteratableSpliterator<T>(iterable), Optional.empty());


    }
    public static <T> ReactiveSeq<T> reactiveSeq(final Iterable<T> iterable) {
        return fromIterable(iterable);


    }
    /**
     * Construct a ReactiveSeq from an Iterator
     *
     * @param iterator
     *            toNested construct Sequence from
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
        return Streams.reactiveSeq(new IterateSpliterator<T>(seed,f),Optional.empty());

    }
    static <T> ReactiveSeq<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return Streams.reactiveSeq(new IteratePredicateSpliterator<T>(seed,f,pred),Optional.empty());

    }

    static <T> ReactiveSeq<T> deferredI(Supplier<? extends Iterable<? extends T>> lazy){
        return ReactiveSeq.of(1).flatMapI(i->lazy.get());
    }
    static <T> ReactiveSeq<T> deferredP(Supplier<? extends Publisher<? extends T>> lazy){
        return ReactiveSeq.of(1).flatMapP(i->lazy.get());
    }
    static <T> ReactiveSeq<T> deferred(Supplier<? extends Stream<? extends T>> lazy){
        return ReactiveSeq.of(1).flatMap(i->lazy.get());
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
        return ReactiveSeq.fromSpliterator(new UnfoldSpliterator<>(seed, unfolder));
    }

    /**
     * @see Stream#generate(Supplier)
     */
    static <T> ReactiveSeq<T> generate(final Supplier<T> s) {
        return Streams.reactiveSeq(Stream.generate(s).spliterator(), Optional.empty());

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
        final Tuple2<ReactiveSeq<Tuple2<T, U>>, ReactiveSeq<Tuple2<T, U>>> tuple2 = sequence.duplicate();
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
     * unzip4(ReactiveSeq.of(new Tuple4(1, "a", 2l,'reactiveSeq'), new Tuple4(2, "b", 3l,'y'), new Tuple4(3,
     * 						"c", 4l,'x')));
     * 		}
     * 		// ReactiveSeq[1,2,3], ReactiveSeq[a,b,c], ReactiveSeq[2l,3l,4l], ReactiveSeq[reactiveSeq,y,x]
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
     * @return ReactiveSeq that will switch toNested an alternative Stream if empty
     */
    @Override
    ReactiveSeq<T> onEmptySwitch(final Supplier<? extends Stream<T>> switchTo) ;



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







    /**
     * emit x elements per time period
     *
     * <pre>
     * {@code
     * 	code
     * 	SimpleTimer timer = new SimpleTimer();
     * 	ReactiveSeq.of(1, 2, 3, 4, 5, 6)
     *             .xPer(6, 100000000, TimeUnit.NANOSECONDS)
     *             .collect(CyclopsCollectors.toList())
     *             .size();
     * //6
     *
     * }
     * </pre>
     *
     * @param x
     *            number of elements toNested emit
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
     * 				.flatMap(Collection::reactiveStream)
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
     * @param time Time toNested applyHKT debouncing over
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
     *             .collect(CyclopsCollectors.toList())
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
     *             .collect(CyclopsCollectors.toList());
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
    ReactiveSeq<T> recover(final Function<? super Throwable, ? extends T> fn);

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
     *            Type toNested recover from
     * @param fn
     *            That accepts an error and returns an alternative value
     * @return ReactiveSeq that can recover from a particular exception
     */
    <EX extends Throwable> ReactiveSeq<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn);

    /**
     * Retry a transformation if it fails. Default settings are toNested retry up toNested 7
     * times, with an doubling backoff period starting @ 2 seconds delay before
     * retry.
     *
     * <pre>
     * {@code
     * given(serviceMock.applyHKT(anyInt())).willThrow(
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
     *            Function toNested retry if fails
     *
     */
    default <R> ReactiveSeq<R> retry(final Function<? super T, ? extends R> fn) {
        return retry(fn, 7, 2, TimeUnit.SECONDS);
    }

    /**
     * Retry a transformation if it fails. Retries up toNested <b>retries</b>
     * times, with an doubling backoff period starting @ <b>delay</b> TimeUnits delay before
     * retry.
     *
     * <pre>
     * {@code
     * given(serviceMock.applyHKT(anyInt())).willThrow(
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
     *            Function toNested retry if fails
     * @param retries
     *            Number of retries
     * @param delay
     *            Delay in TimeUnits
     * @param timeUnit
     *            TimeUnit toNested use for delay
     */
    default <R> ReactiveSeq<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (ReactiveSeq) FoldableTraversable.super.retry(fn, retries, delay, timeUnit);
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
     *            element toNested remove
     * @return Filtered Stream / ReactiveSeq
     */
    default ReactiveSeq<T> remove(final T t) {
        return this.filter(v -> v != t);
    }

    /**
     * Generate the permutations based on values in the ReactiveSeq Makes use of
     * Streamable toNested store intermediate stages in a toX
     *
     *
     * @return Permutations from this ReactiveSeq
     */
    @Override
    default ReactiveSeq<ReactiveSeq<T>> permutations() {
        final Streamable<ReactiveSeq<T>> streamable = Streamable.fromStream(this)
                                                               .permutations();
        return streamable.reactiveSeq();
    }
    default StreamT<reactiveSeq,T> permutationsT() {
        return StreamT.fromReactiveSeq(permutations());
    }

    /**
     * Return a Stream with elements before the provided skip index removed,
     * and elements after the provided take index removed
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
     * @return All combinations of the elements in this reactiveStream of the specified
     *         size
     */
    @Override
    default ReactiveSeq<ReactiveSeq<T>> combinations(final int size) {
        return Streams.combinations(size,toArray());
    }
    default StreamT<reactiveSeq,T> combinationsT(final int size) {
        return StreamT.fromReactiveSeq(combinations(size));
    }

    default <W extends WitnessType<W>> StreamT<W, T> liftM(W witness) {
        return StreamT.of(witness.adapter().unit(this));
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
     * @return All combinations of the elements in this reactiveStream
     */
    @Override
    default ReactiveSeq<ReactiveSeq<T>> combinations() {
        Object[] a = toArray();
        return range(1, a.length+1).map(size->Streams.<T>combinations(size,a))
                                 .flatMap(s -> s)
                                 .prepend(ReactiveSeq.<T>empty());

    }
    default StreamT<reactiveSeq,T> combinationsT() {
        return StreamT.fromReactiveSeq(combinations());
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
     * Connect toNested the Scheduled Stream
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
     * @return Connectable HotStream of emitted from scheduled Stream
     */
    @Override
    default HotStream<T> schedule(String cron, ScheduledExecutorService ex){
        return Streams.schedule(this, cron, ex);

    }


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
     * Connect toNested the Scheduled Stream
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
     * @return Connectable HotStream of emitted from scheduled Stream
     */
    @Override
    default HotStream<T> scheduleFixedDelay(long delay, ScheduledExecutorService ex){
        return Streams.scheduleFixedDelay(this, delay, ex);
    }

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
     * Connect toNested the Scheduled Stream
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
     * @return Connectable HotStream of emitted from scheduled Stream
     */
    @Override
    default HotStream<T> scheduleFixedRate(long rate, ScheduledExecutorService ex){
        return Streams.scheduleFixedRate(this, rate, ex);
    }

    /**
     * [equivalent toNested count]
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
     *            Nested Stream toNested iterate over
     * @param stream2
     *            Nested Stream toNested iterate over
     * @param stream3
     *            Nested Stream toNested iterate over
     * @param yieldingFunction
     *            Function with pointers toNested the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nest iteration
     */
    default <R1, R2, R3,R> ReactiveSeq<R> forEach4(final Function<? super T, ? extends BaseStream<R1, ?>> stream1,
                        final BiFunction<? super T,? super R1, ? extends BaseStream<R2, ?>> stream2,
                            final Fn3<? super T, ? super R1, ? super R2, ? extends BaseStream<R3, ?>> stream3,
                            final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){
        return this.flatMap(in -> {

            ReactiveSeq<R1> a = stream1 instanceof ReactiveSeq ? (ReactiveSeq)stream1 :  ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = stream2 instanceof ReactiveSeq ? (ReactiveSeq)stream2 :  ReactiveSeq.fromIterable(()->stream2.apply(in, ina).iterator());
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = stream3 instanceof ReactiveSeq ? (ReactiveSeq)stream3 :  ReactiveSeq.fromIterable(()->stream3.apply(in, ina, inb).iterator());
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
     *            Nested Stream toNested iterate over
     * @param stream2
     *            Nested Stream toNested iterate over
     * @param stream3
     *            Nested Stream toNested iterate over
     * @param filterFunction
     *            Filter toNested applyHKT over elements before passing non-filtered
     *            values toNested the yielding function
     * @param yieldingFunction
     *            Function with pointers toNested the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nest iteration
     */
    default <R1, R2, R3, R> ReactiveSeq<R> forEach4(final Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            final BiFunction<? super T, ? super R1, ? extends BaseStream<R2, ?>> stream2,
            final Fn3<? super T, ? super R1, ? super R2, ? extends BaseStream<R3, ?>> stream3,
            final Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){

        return this.flatMap(in -> {

            ReactiveSeq<R1> a = stream1 instanceof ReactiveSeq ? (ReactiveSeq)stream1 :
                                ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = stream2 instanceof ReactiveSeq ? (ReactiveSeq)stream2 :
                                    ReactiveSeq.fromIterable(()->stream2.apply(in, ina).iterator());
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = stream3 instanceof ReactiveSeq ? (ReactiveSeq)stream3 :
                                    ReactiveSeq.fromIterable(()->stream3.apply(in, ina, inb).iterator());
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
     *            Nested Stream toNested iterate over
     * @param stream2
     *            Nested Stream toNested iterate over
     * @param yieldingFunction
     *            Function with pointers toNested the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nest iteration
     */
    default <R1, R2, R> ReactiveSeq<R> forEach3(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            BiFunction<? super T,? super R1, ? extends BaseStream<R2, ?>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){

        return this.flatMap(in -> {

            ReactiveSeq<R1> a = stream1 instanceof ReactiveSeq ? (ReactiveSeq)stream1 :
                                ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return ReactiveSeq.fromIterable(a)
                              .flatMap(ina -> {
                ReactiveSeq<R2> b = stream2 instanceof ReactiveSeq ? (ReactiveSeq)stream2 :
                        ReactiveSeq.fromIterable(()->stream2.apply(in, ina).iterator());
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
     *            Nested Stream toNested iterate over
     * @param stream2
     *            Nested Stream toNested iterate over
     * @param filterFunction
     *            Filter toNested applyHKT over elements before passing non-filtered
     *            values toNested the yielding function
     * @param yieldingFunction
     *            Function with pointers toNested the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nest iteration
     */
   default <R1, R2, R> ReactiveSeq<R> forEach3(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            BiFunction<? super T,? super R1, ? extends BaseStream<R2, ?>> stream2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){
       return this.flatMap(in -> {

           ReactiveSeq<R1> a = stream1 instanceof ReactiveSeq ? (ReactiveSeq)stream1 :
                                    ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
           return ReactiveSeq.fromIterable(a)
                             .flatMap(ina -> {
               ReactiveSeq<R2> b = stream2 instanceof ReactiveSeq ? (ReactiveSeq)stream2 :
                                    ReactiveSeq.fromIterable(()->stream2.apply(in, ina).iterator());
               return b.filter(in2 -> filterFunction.apply(in, ina, in2))
                       .map(in2 -> yieldingFunction.apply(in, ina, in2));
           });

       });
    }

    /**
     * Perform a two level nested internal iteration over this Stream and the
     * supplied reactiveStream
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
     *            Nested Stream toNested iterate over
     * @param yieldingFunction
     *            Function with pointers toNested the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nest iteration
     */
    default <R1, R> ReactiveSeq<R> forEach2(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            BiFunction<? super T,? super R1, ? extends R> yieldingFunction){
        return this.flatMap(in-> {


            ReactiveSeq<R1> b = stream1 instanceof ReactiveSeq ? (ReactiveSeq)stream1 :
                                    ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return b.map(in2->yieldingFunction.apply(in, in2));
        });

    }

    /**
     * crossJoin two Streams forming a cartesian product over both
     * @param other Stream toNested crossJoin
     * @return Active Stream with each pair across both Streams in a Tuple
     */
    default <U> ReactiveSeq<Tuple2<T, U>> crossJoin(ReactiveSeq<? extends U> other) {
        return forEach2(a->other, Tuple::tuple);
    }

    /**
     * Perform a two level nested internal iteration over this Stream and the
     * supplied reactiveStream
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
     *            Nested Stream toNested iterate over
     * @param filterFunction
     *            Filter toNested applyHKT over elements before passing non-filtered
     *            values toNested the yielding function
     * @param yieldingFunction
     *            Function with pointers toNested the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nest iteration
     */
    default <R1, R> ReactiveSeq<R> forEach2(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            BiFunction<? super T,? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction){
        return this.flatMap(in-> {


            ReactiveSeq<R1> b = stream1 instanceof ReactiveSeq ? (ReactiveSeq)stream1 :  ReactiveSeq.fromIterable(()->stream1.apply(in).iterator());
            return b.filter(in2-> filterFunction.apply(in,in2))
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }




    @Override
    default Optional<T> max(final Comparator<? super T> comparator) {

        return Streams.max(this, comparator);

    }

    /**
     * Returns the count of elements in this reactiveStream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a> and is
     * equivalent toNested:
     * <pre>{@code
     *     return mapToLong(e -> 1L).sum();
     * }</pre>
     * <p>
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the count of elements in this reactiveStream
     */
    @Override
    long count();

    @Override
    default Optional<T> min(final Comparator<? super T> comparator) {
        return Streams.min(this, comparator);
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

    /**
     * Append Stream toNested this ReactiveSeq
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).appendStream(ReactiveSeq.of(100, 200, 300)).map(it -> it + "!!").collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "3!!", "100!!", "200!!", "300!!")));     * }
     * </pre>
     *
     * @param stream
     *            toNested append
     * @return ReactiveSeq with Stream appended
     */
     ReactiveSeq<T> appendS(Stream<? extends T> other);
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#append(java.lang.Iterable)
     */

    ReactiveSeq<T> append(Iterable<? extends T> other);


    ReactiveSeq<T> prepend(Iterable<? extends T> other);



    /**
     * Convert toNested a Stream with the values repeated specified times
     *
     * <pre>
     * {@code
     * 		ReactiveSeq.of(1,2,2)
     * 								.cycle(3)
     * 								.collect(CyclopsCollectors.toList());
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
    ReactiveSeq<T> cycle(long times);


    ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate);

    ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate);


    String format();

    @Override
    default ReactiveSeq<T> removeAllS(final Stream<? extends T> stream) {
        return (ReactiveSeq<T>)FoldableTraversable.super.removeAllS(stream);
    }

    @Override
    default ReactiveSeq<T> removeAllI(final Iterable<? extends T> it) {
        return (ReactiveSeq<T>)FoldableTraversable.super.removeAllI(it);
    }

    @Override
    default ReactiveSeq<T> removeAll(final T... values) {
        return (ReactiveSeq<T>)FoldableTraversable.super.removeAll(values);
    }

    @Override
    default ReactiveSeq<T> retainAllI(final Iterable<? extends T> it) {
        return (ReactiveSeq<T>)FoldableTraversable.super.retainAllI(it);
    }

    @Override
    default ReactiveSeq<T> retainAllS(final Stream<? extends T> stream) {
        return (ReactiveSeq<T>)FoldableTraversable.super.retainAllS(stream);
    }

    @Override
    default ReactiveSeq<T> retainAll(final T... values) {
        return (ReactiveSeq<T>)FoldableTraversable.super.retainAll(values);
    }

    @Override
    default ReactiveSeq<T> filterNot(final Predicate<? super T> predicate) {
        return (ReactiveSeq<T>)FoldableTraversable.super.filterNot(predicate);
    }

    @Override
    default ReactiveSeq<T> notNull() {
        return (ReactiveSeq<T>)FoldableTraversable.super.notNull();
    }



    default boolean isEmpty(){
        return !findAny().isPresent();
    }

    @Override
    default ReactiveSeq<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return (ReactiveSeq<T>)FoldableTraversable.super.zip(combiner,app);
    }

    @Override
    default <R> ReactiveSeq<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (ReactiveSeq<R>)FoldableTraversable.super.zipWith(fn);
    }

    @Override
    default <R> ReactiveSeq<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (ReactiveSeq<R>)FoldableTraversable.super.zipWithS(fn);
    }

    @Override
    default <R> ReactiveSeq<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (ReactiveSeq<R>)FoldableTraversable.super.zipWithP(fn);
    }

    @Override
    default <U> ReactiveSeq<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (ReactiveSeq)FoldableTraversable.super.zipP(other, Tuple::tuple);
    }

    @Override
    default <S, U, R> ReactiveSeq<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (ReactiveSeq<R>)FoldableTraversable.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> ReactiveSeq<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (ReactiveSeq<R>)FoldableTraversable.super.zip4(second,third,fourth,fn);
    }
    /**
     A potentially asynchronous merge operation where data from each publisher may arrive out of order (if publishers
     * are configured toNested publish asynchronously, users can use the overloaded @see {@link IterableFunctor#mergePublisher(Collection, QueueFactory)}
     * method toNested forEachAsync asynchronously also. Max concurrency is determined by the publishers toX size, along with a default limit of 5k queued values before
     * backpressure is applied.
     *
     * @param publishers Publishers toNested merge
     * @return Return Stream of merged data
     */
    default ReactiveSeq<T> mergeP(final Publisher<T>... publishers) {
        return mergeP(QueueFactories.boundedQueue(5_000),publishers);
    }

    default ReactiveSeq<T> backpressureAware(){
        return this;
    }

    /**
     * A potentially asynchronous merge operation where data from each publisher may arrive out of order (if publishers
     * are configured toNested publish asynchronously.
     * The QueueFactory parameter can be used by pull based Streams toNested control the maximum queued elements @see {@link QueueFactories}
     * Push based reactive-streams signal demand via their subscription.
     *
     *
     */
    default ReactiveSeq<T> mergeP(final QueueFactory<T> factory,final Publisher<T>... publishers) {
        final Counter c = new Counter();
        c.active.set(publishers.length + 1);
        final QueueBasedSubscriber<T> init = QueueBasedSubscriber.subscriber(factory, c, publishers.length);

        final Supplier<Continuation> sp = () -> {
            subscribe(init);
            for (final Publisher next : publishers) {
                next.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(), c, publishers.length));
            }

            init.close();

            return Continuation.empty();
        };
        final Continuation continuation = new Continuation(
                sp);
        init.addContinuation(continuation);
        return ReactiveSeq.fromStream(init.jdkStream());
    }
    default ReactiveSeq<T> publishTo(Adapter<T>... adapters){
        return peek(e->{
            for(Adapter<T> next:  adapters){
                 next.offer(e);
            }
        });
    }
    default ReactiveSeq<T> publishTo(Signal<T>... signals){
        return peek(e->{
            for(Signal<T> next:  signals){
                  next.set(e);
            }
        });
    }
    default ReactiveSeq<T> merge(Adapter<T>... adapters){
        Publisher<T>[] publishers = ReactiveSeq.of(adapters).map(a->a.stream()).toArray(n->new Publisher[n]);

        final Counter c = new Counter();
        c.active.set(publishers.length + 1);
        final QueueBasedSubscriber<T> init = QueueBasedSubscriber.subscriber(QueueFactories.boundedQueue(5_000), c, publishers.length);

        final Supplier<Continuation> sp = () -> {
            backpressureAware().subscribe(init);
            for (final Publisher next : publishers) {
                next.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(), c, publishers.length));
            }

            init.close();

            return Continuation.empty();
        };
        final Continuation continuation = new Continuation(
                sp);
        init.addContinuation(continuation);
        return ReactiveSeq.fromStream(init.jdkStream());
    }
    <R> R visit(Function<? super ReactiveSeq<T>,? extends R> sync,Function<? super ReactiveSeq<T>,? extends R> reactiveStreams,
                       Function<? super ReactiveSeq<T>,? extends R> asyncNoBackPressure);
    /**
     * Broadcast the contents of this Stream toNested multiple downstream Streams (determined by supplier parameter).
     * For pull based Streams this Stream will be buffered.
     * For push based Streams elements are broadcast downstream on receipt, the emitted downstream Streams remain asynchonous
     *
     * This contrasts with
     *  {@link ReactiveSeq#duplicate}
     *  {@link ReactiveSeq#triplate}
     *  {@link ReactiveSeq#quadruplicate()}
     * Which buffer all Stream types and produce a synchronous downstream stream.
     *
     *
     * @param num Number of downstream Streams toNested multicast toNested
     * @return List of Streams that recieve data from this Stream
     */
    default ListX<ReactiveSeq<T>> multicast(int num){
        return Streams.toBufferingCopier(() -> iterator(),num,()->new ArrayDeque<T>(100))
                .map(ReactiveSeq::fromIterable);
    }
    default <R1,R2,R3> ReactiveSeq<R3> fanOutZipIn(Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R1>> path1,
                                                    Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R2>> path2,
                                                     BiFunction<? super R1, ? super R2, ? extends R3> zipFn){
        ListX<ReactiveSeq<T>> list = multicast(2);
        return path1.apply(list.get(0)).zip(path2.apply(list.get(1)),zipFn);

    }
    default <R1,R2,R3> ReactiveSeq<R3> parallelFanOutZipIn(ForkJoinPool fj, Function<? super Stream<T>, ? extends Stream<? extends R1>> path1,
                                                   Function<? super Stream<T>, ? extends Stream<? extends R2>> path2,
                                                   BiFunction<? super R1, ? super R2, ? extends R3> zipFn){
        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> d = duplicate(()->new ArrayDeque<T>(100));
        Tuple2<? extends Stream<? extends R1>, ? extends Stream<? extends R2>> d2 = d.map1(path1).map2(path2);

        ReactiveSeq<R1> res1 = d.v1.parallel(fj, path1);
        ReactiveSeq<R2> res2 = d.v2.parallel(fj, path2);
        return res1.zip(res2,zipFn);

    }
    default <R> ReactiveSeq<R> fanOut(Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path1,
                                      Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path2){
        ListX<ReactiveSeq<T>> list = multicast(2);
        Publisher<R> pub = (Publisher<R>)path2.apply(list.get(1));
        ReactiveSeq<R> seq = (ReactiveSeq<R>)path1.apply(list.get(0));
        return  seq.mergeP(pub);

    }

    default <R> ReactiveSeq<R> parallelFanOut(ForkJoinPool fj,Function<? super Stream<T>, ? extends Stream<? extends R>> path1,
                                      Function<? super Stream<T>, ? extends Stream<? extends R>> path2){

        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> d = duplicate(()->new ArrayDeque<T>(100));
        Tuple2<? extends Stream<? extends R>, ? extends Stream<? extends R>> d2 = d.map1(path1).map2(path2);

        ReactiveSeq<R> res1 = d.v1.parallel(fj, path1);
        ReactiveSeq<R> res2 = d.v2.parallel(fj, path2);
        return res1.mergeP(res2);



    }
    default <R> ReactiveSeq<R> fanOut(Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path1,
                                      Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path2,
                                      Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path3){


        ListX<ReactiveSeq<T>> list = multicast(3);
        Publisher<R> pub2 = (Publisher<R>)path2.apply(list.get(1));
        Publisher<R> pub3 = (Publisher<R>)path3.apply(list.get(2));
        ReactiveSeq<R> seq = (ReactiveSeq<R>)path1.apply(list.get(0));
        return  seq.mergeP(pub2,pub3);



    }
    default <R> ReactiveSeq<R> parallelFanOut(ForkJoinPool fj,Function<? super Stream<T>, ? extends Stream<? extends R>> path1,
                                              Function<? super Stream<T>, ? extends Stream<? extends R>> path2,
                                              Function<? super Stream<T>, ? extends Stream<? extends R>> path3){


        Tuple3<ReactiveSeq<T>, ReactiveSeq<T>,ReactiveSeq<T>> d = triplicate(()->new ArrayDeque<T>(100));
        val res = d.map1(path1).map2(path2).map3(path3);

        ReactiveSeq<R> res1 = d.v1.parallel(fj, path1);
        ReactiveSeq<R> res2 = d.v2.parallel(fj, path2);
        ReactiveSeq<R> res3 = d.v3.parallel(fj, path3);
        return res1.mergeP(res2,res3);



    }
    default <R1,R2,R3,R4> ReactiveSeq<R4> parallelFanOutZipIn(ForkJoinPool fj,Function<? super Stream<T>, ? extends Stream<? extends R1>> path1,
                                                      Function<? super Stream<T>, ? extends Stream<? extends R2>> path2,
                                                      Function<? super Stream<T>, ? extends Stream<? extends R3>> path3,
                                                      Fn3<? super R1, ? super R2, ? super R3, ? extends R4> zipFn){

        Tuple3<ReactiveSeq<T>, ReactiveSeq<T>,ReactiveSeq<T>> d = triplicate(()->new ArrayDeque<T>(100));
        val res = d.map1(path1).map2(path2).map3(path3);
        ReactiveSeq<R1> res1 = d.v1.parallel(fj, path1);
        ReactiveSeq<R2> res2 = d.v2.parallel(fj, path2);
        ReactiveSeq<R3> res3 = d.v3.parallel(fj, path3);
        return res1.zip3(res2,res3,zipFn);

    }
    default <R1,R2,R3,R4> ReactiveSeq<R4> fanOutZipIn(Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R1>> path1,
                                      Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R2>> path2,
                                      Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R3>> path3,
                                            Fn3<? super R1, ? super R2, ? super R3, ? extends R4> zipFn){

        ListX<ReactiveSeq<T>> list = multicast(3);
        return path1.apply(list.get(0)).zip3(path2.apply(list.get(1)),path3.apply(list.get(2)),zipFn);

    }
    default <R> ReactiveSeq<R> fanOut(Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path1,
                                      Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path2,
                                      Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path3,
                                      Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R>> path4){

        ListX<ReactiveSeq<T>> list = multicast(4);
        Publisher<R> pub2 = (Publisher<R>)path2.apply(list.get(1));
        Publisher<R> pub3 = (Publisher<R>)path3.apply(list.get(2));
        Publisher<R> pub4 = (Publisher<R>)path4.apply(list.get(3));
        ReactiveSeq<R> seq = (ReactiveSeq<R>)path1.apply(list.get(0));
        return  seq.mergeP(pub2,pub3,pub4);

    }
    default <R> ReactiveSeq<R> parallelFanOut(ForkJoinPool fj,Function<? super Stream<T>, ? extends Stream<? extends R>> path1,
                                      Function<? super Stream<T>, ? extends Stream<? extends R>> path2,
                                      Function<? super Stream<T>, ? extends Stream<? extends R>> path3,
                                      Function<? super Stream<T>, ? extends Stream<? extends R>> path4){

        val d = quadruplicate(()->new ArrayDeque<T>(100));
        val res = d.map1(path1).map2(path2).map3(path3).map4(path4);
        ReactiveSeq<R> res1 = d.v1.parallel(fj, path1);
        ReactiveSeq<R> res2 = d.v2.parallel(fj, path2);
        ReactiveSeq<R> res3 = d.v3.parallel(fj, path3);
        ReactiveSeq<R> res4 = d.v4.parallel(fj, path4);
        return res1.mergeP(res2,res3,res4);

    }

    default <R1,R2,R3,R4,R5> ReactiveSeq<R5> fanOutZipIn(Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R1>> path1,
                                                    Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R2>> path2,
                                                    Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R3>> path3,
                                                    Function<? super ReactiveSeq<T>, ? extends ReactiveSeq<? extends R4>> path4,
                                                    Fn4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R5> zipFn){


        ListX<ReactiveSeq<T>> list = multicast(4);
        return path1.apply(list.get(0)).zip4(path2.apply(list.get(1)),path3.apply(list.get(2)),path4.apply(list.get(3)),zipFn);

    }
    default <R1,R2,R3,R4,R5> ReactiveSeq<R5> parallelFanOutZipIn(ForkJoinPool fj,Function<? super Stream<T>, ? extends Stream<? extends R1>> path1,
                                                         Function<? super Stream<T>, ? extends Stream<? extends R2>> path2,
                                                         Function<? super Stream<T>, ? extends Stream<? extends R3>> path3,
                                                         Function<? super Stream<T>, ? extends Stream<? extends R4>> path4,
                                                         Fn4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R5> zipFn){

        val d = quadruplicate(()->new ArrayDeque<T>(100));
        val res = d.map1(path1).map2(path2).map3(path3).map4(path4);
        ReactiveSeq<R1> res1 = d.v1.parallel(fj, path1);
        ReactiveSeq<R2> res2 = d.v2.parallel(fj, path2);
        ReactiveSeq<R3> res3 = d.v3.parallel(fj, path3);
        ReactiveSeq<R4> res4 = d.v4.parallel(fj, path4);
        return res1.zip4(res2,res3,res4,zipFn);

    }

    /**
     * @return A Stream that contains only changes in the values in the current Stream, useful for converting a Continuous sequence into one with discrete steps
     */
    ReactiveSeq<T> changes();

    default Topic<T> broadcast(){
        Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                                                    .build()
                                                    .withTimeout(1);


        Topic<T> topic = new Topic<T>(queue,QueueFactories.<T>unboundedNonBlockingQueue());
        AtomicBoolean wip = new AtomicBoolean(false);
        Spliterator<T> split = this.spliterator();
        Continuation ref[] = {null};
        Continuation cont =
                new Continuation(()->{

                    if(wip.compareAndSet(false,true)){
                        try {

                            //use the takeOne consuming thread toNested tell this Stream onto the Queue
                            if(!split.tryAdvance(topic::offer)){
                                topic.close();
                                return Continuation.empty();

                            }
                        }finally {
                            wip.set(false);
                        }

                    }


                    return ref[0];
                });

        ref[0]=cont;
        queue.addContinuation(cont);
        return topic;
    }

    default ReactiveSeq<T> ambWith(Publisher<T> racer){
        return Spouts.amb(this,racer);
    }
    default ReactiveSeq<T> ambWith(Publisher<T>... racers){
        ListX<Publisher<T>> list = ListX.of(racers);
        list.add(0, this);
        return Spouts.amb(list);
    }

    static <T> ReactiveSeq<T> concat(Stream<? extends T>...streams){
        Spliterator[] array = new Spliterator[streams.length];
        int index = 0;
        for(Stream<? extends T> next : streams){
            array[index++] = next.spliterator();
        }
        return Streams.reactiveSeq(new ArrayConcatonatingSpliterator<T,T>(array),Optional.empty());
    }
    static <T> ReactiveSeq<T> concat(Spliterator<? extends T>...array){

        return Streams.reactiveSeq(new ArrayConcatonatingSpliterator<T,T>((Spliterator[])array),Optional.empty());
    }
    static <T> ReactiveSeq<T> concat(Stream<? extends T> left, Stream<? extends T> right){

        return Streams.reactiveSeq(new ConcatonatingSpliterator<T,T>((Spliterator<T>)left.spliterator(),
                (Spliterator<T>)right.spliterator()),Optional.empty());
    }
    static <T> ReactiveSeq<T> concat(Spliterator<? extends T> left, Spliterator<? extends T> right){

        return Streams.reactiveSeq(new ConcatonatingSpliterator<T,T>((Spliterator<T>)left,
                (Spliterator<T>)right),Optional.empty());
    }



    static class Instances {
        public static InstanceDefinitions<reactiveSeq> definitions(){
            return new InstanceDefinitions<reactiveSeq>() {
                @Override
                public <T, R> Functor<reactiveSeq> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<reactiveSeq> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<reactiveSeq> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<reactiveSeq> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<reactiveSeq>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<reactiveSeq>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<reactiveSeq> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<reactiveSeq>> monadPlus(Monoid<Higher<reactiveSeq, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Maybe<Traverse<reactiveSeq>> traverse() {
                    return Maybe.just(Instances.traverse());
                }

                @Override
                public <T> Maybe<Foldable<reactiveSeq>> foldable() {
                    return Maybe.just(Instances.foldable());
                }

                @Override
                public <T> Maybe<Comonad<reactiveSeq>> comonad() {
                    return Maybe.none();
                }
                @Override
                public <T> Maybe<Unfoldable<reactiveSeq>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };
        }
        public static Unfoldable<reactiveSeq> unfoldable(){
            return new Unfoldable<reactiveSeq>() {
                @Override
                public <R, T> Higher<reactiveSeq, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return ReactiveSeq.unfold(b,fn);
                }
            };
        }
        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  ReactiveSeq<Integer> list = Lists.functor().map(i->i*2, ReactiveSeq.widen(Arrays.asList(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Lists
         * <pre>
         * {@code
         *   ReactiveSeq<Integer> list = ReactiveSeq.Instances.unit()
        .unit("hello")
        .applyHKT(h->Lists.functor().map((String v) ->v.length(), h))
        .convert(ReactiveSeq::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Lists
         */
        public static <T,R>Functor<reactiveSeq> functor(){
            BiFunction<ReactiveSeq<T>,Function<? super T, ? extends R>,ReactiveSeq<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * ReactiveSeq<String> list = Lists.unit()
        .unit("hello")
        .convert(ReactiveSeq::narrowK3);

        //Arrays.asList("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Lists
         */
        public static <T> Pure<reactiveSeq> unit(){
            return General.<reactiveSeq,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops2.hkt.jdk.ReactiveSeq.widen;
         * import static com.aol.cyclops2.util.function.Lambda.l1;
         * import static java.util.Arrays.asList;
         *
        Lists.zippingApplicative()
        .ap(widen(asList(l1(this::multiplyByTwo))),widen(asList(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * ReactiveSeq<Function<Integer,Integer>> listFn =Lists.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(ReactiveSeq::narrowK3);

        ReactiveSeq<Integer> list = Lists.unit()
        .unit("hello")
        .applyHKT(h->Lists.functor().map((String v) ->v.length(), h))
        .applyHKT(h->Lists.zippingApplicative().ap(listFn, h))
        .convert(ReactiveSeq::narrowK3);

        //Arrays.asList("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Lists
         */
        public static <T,R> Applicative<reactiveSeq> zippingApplicative(){
            BiFunction<ReactiveSeq< Function<T, R>>,ReactiveSeq<T>,ReactiveSeq<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops2.hkt.jdk.ReactiveSeq.widen;
         * ReactiveSeq<Integer> list  = Lists.monad()
        .flatMap(i->widen(ReactiveSeq.range(0,i)), widen(Arrays.asList(1,2,3)))
        .convert(ReactiveSeq::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    ReactiveSeq<Integer> list = Lists.unit()
        .unit("hello")
        .applyHKT(h->Lists.monad().flatMap((String v) ->Lists.unit().unit(v.length()), h))
        .convert(ReactiveSeq::narrowK3);

        //Arrays.asList("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Lists
         */
        public static <T,R> Monad<reactiveSeq> monad(){

            BiFunction<Higher<reactiveSeq,T>,Function<? super T, ? extends Higher<reactiveSeq,R>>,Higher<reactiveSeq,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  ReactiveSeq<String> list = Lists.unit()
        .unit("hello")
        .applyHKT(h->Lists.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(ReactiveSeq::narrowK3);

        //Arrays.asList("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<reactiveSeq> monadZero(){

            return General.monadZero(monad(), ReactiveSeq.empty());
        }
        /**
         * <pre>
         * {@code
         *  ReactiveSeq<Integer> list = Lists.<Integer>monadPlus()
        .plus(ReactiveSeq.widen(Arrays.asList()), ReactiveSeq.widen(Arrays.asList(10)))
        .convert(ReactiveSeq::narrowK3);
        //Arrays.asList(10))
         *
         * }
         * </pre>
         * @return Type class for combining Lists by concatenation
         */
        public static <T> MonadPlus<reactiveSeq> monadPlus(){
            Monoid<ReactiveSeq<T>> m = Monoid.of(ReactiveSeq.empty(), Instances::concat);
            Monoid<Higher<reactiveSeq,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T,R> MonadRec<reactiveSeq> monadRec(){

            return new MonadRec<reactiveSeq>(){
                @Override
                public <T, R> Higher<reactiveSeq, R> tailRec(T initial, Function<? super T, ? extends Higher<reactiveSeq,? extends Xor<T, R>>> fn) {
                    return ReactiveSeq.deferred(()-> {
                        ReactiveSeq<Xor<T, R>> next = ReactiveSeq.of(Xor.secondary(initial));
                        boolean newValue[] = {false};
                        for (; ; ) {
                            next = next.flatMap(e -> e.visit(s -> {
                                newValue[0] = true;
                                return narrowK(fn.apply(s));
                            }, p -> ReactiveSeq.of(e)));
                            if (!newValue[0])
                                break;
                        }
                        return Xor.sequencePrimary(next.to().listX(LAZY)).map(l -> l.stream()).get();
                    });
                }
            };
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<ReactiveSeq<Integer>> m = Monoid.of(ReactiveSeq.widen(Arrays.asList()), (a,b)->a.isEmpty() ? b : a);
        ReactiveSeq<Integer> list = Lists.<Integer>monadPlus(m)
        .plus(ReactiveSeq.widen(Arrays.asList(5)), ReactiveSeq.widen(Arrays.asList(10)))
        .convert(ReactiveSeq::narrowK3);
        //Arrays.asList(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid toNested use for combining Lists
         * @return Type class for combining Lists
         */
        public static <T> MonadPlus<reactiveSeq> monadPlus(Monoid<ReactiveSeq<T>> m){
            Monoid<Higher<reactiveSeq,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<reactiveSeq> traverse(){
            BiFunction<Applicative<C2>,ReactiveSeq<Higher<C2, T>>,Higher<C2, ReactiveSeq<T>>> sequenceFn = (ap,list) -> {

                Higher<C2,ReactiveSeq<T>> identity = ap.unit(ReactiveSeq.empty());

                BiFunction<Higher<C2,ReactiveSeq<T>>,Higher<C2,T>,Higher<C2,ReactiveSeq<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.append(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,ReactiveSeq<T>>> combineLists = (a,b)-> ap.apBiFn(ap.unit((l1,l2)-> { l1.appendS(l2); return l1;}),a,b); ;

                return list.stream()
                        .reduce(identity,
                                combineToList,
                                combineLists);


            };
            BiFunction<Applicative<C2>,Higher<reactiveSeq,Higher<C2, T>>,Higher<C2, Higher<reactiveSeq,T>>> sequenceNarrow  =
                    (a,b) -> Instances.widen2(sequenceFn.apply(a, ReactiveSeq.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Lists.foldable()
        .foldLeft(0, (a,b)->a+b, ReactiveSeq.of(1,2,3,4));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<reactiveSeq> foldable(){
            BiFunction<Monoid<T>,Higher<reactiveSeq,T>,T> foldRightFn =  (m,l)-> narrow(l).foldRight(m);
            BiFunction<Monoid<T>,Higher<reactiveSeq,T>,T> foldLeftFn = (m,l)-> narrow(l).reduce(m);
            return General.foldable(foldRightFn, foldLeftFn);
        }

        private static  <T> ReactiveSeq<T> concat(ReactiveSeq<T> l1, ReactiveSeq<T> l2){
            return ReactiveSeq.concat(l1.stream(),l2.stream());
        }
        private static <T> ReactiveSeq<T> of(T value){
            return ReactiveSeq.of(value);
        }
        private static <T,R> ReactiveSeq<R> ap(ReactiveSeq<Function< T, R>> lt,  ReactiveSeq<T> list){
            return lt.zip(list,(a,b)->a.apply(b));
        }
        private static <T,R> Higher<reactiveSeq,R> flatMap( Higher<reactiveSeq,T> lt, Function<? super T, ? extends  Higher<reactiveSeq,R>> fn){
            return ReactiveSeq.narrowK(lt).flatMap(fn.andThen(ReactiveSeq::narrowK));
        }
        private static <T,R> ReactiveSeq<R> map(ReactiveSeq<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);
        }



        /**
         * Widen a ReactiveSeq nest inside another HKT encoded type
         *
         * @param flux HTK encoded type containing  a List toNested widen
         * @return HKT encoded type with a widened List
         */
        public static <C2, T> Higher<C2, Higher<reactiveSeq, T>> widen2(Higher<C2, ReactiveSeq<T>> flux) {
            // a functor could be used (if C2 is a functor / one exists for C2 type)
            // instead of casting
            // cast seems safer as Higher<reactiveSeq,T> must be a ReactiveSeq
            return (Higher) flux;
        }





        /**
         * Convert the HigherKindedType definition for a List into
         *
         * @param List Type Constructor toNested convert back into narrowed type
         * @return List from Higher Kinded Type
         */
        public static <T> ReactiveSeq<T> narrow(final Higher<reactiveSeq, T> completableList) {

            return ((ReactiveSeq<T>) completableList);//.narrow();

        }
    }
    /**
     * Convert the raw Higher Kinded Type for ReactiveSeq types into the ReactiveSeq type definition class
     *
     * @param future HKT encoded list into a ReactiveSeq
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> narrowK(final Higher<reactiveSeq, T> future) {
        return (ReactiveSeq<T>) future;
    }

}
