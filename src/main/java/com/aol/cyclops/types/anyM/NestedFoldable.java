package com.aol.cyclops.types.anyM;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Validator;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.control.monads.transformers.seq.OptionalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.util.stream.Streamable;

public interface NestedFoldable<T> {
    public AnyM<? extends Foldable<T>> nestedFoldables();
   
    /**
     * Destructures this Traversable into it's head and tail. If the traversable instance is not a SequenceM or Stream type,
     * whenStream may be more efficient (as it is guaranteed to be lazy).
     * 
     * <pre>
     * {@code 
     * ListX.of(1,2,3,4,5,6,7,8,9)
             .dropRight(5)
             .plus(10)
             .visit((x,xs) ->
                 xs.join(x.>2?"hello":"world")),()->"NIL"
             );
     * 
     * }
     * //2world3world4
     * 
     * </pre>
     * 
     * 
     * @param match
     * @return
     */
    default <R> AnyM<R> visit(BiFunction<? super T,? super ReactiveSeq<T>,? extends R> match, Supplier<? extends R> ifEmpty){
        return nestedFoldables().map(s->s.visit(match,ifEmpty)); 
    }
    default <R> AnyM<R> visit(BiFunction<? super Maybe<T>,? super ReactiveSeq<T>,? extends R> match){
        return nestedFoldables().map(s->s.visit(match));
        
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
    default <R> AnyM<R> mapReduce(Reducer<R> reducer){
        return nestedFoldables().map(s->s.mapReduce(reducer));
    }

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
     *      if("one".equals(s))
     *          return 1;
     *      if("two".equals(s))
     *          return 2;
     *      if("three".equals(s))
     *          return 3;
     *      if("four".equals(s))
     *          return 4;
     *      return -1;
     *     }
     *  }
     * </pre>
     * 
     * @param mapper
     *            Function to map Monad type
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    default <R> AnyM<R> mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer){
        return nestedFoldables().map(s->s.mapReduce(mapper,reducer));
    }
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
    default AnyM<T> reduce(Monoid<T> reducer){
        return nestedFoldables().map(s->s.reduce(reducer));
    }

    /*
     * <pre> {@code assertThat(ReactiveSeq.of(1,2,3,4,5).map(it -> it*100).reduce(
     * (acc,next) -> acc+next).get(),equalTo(1500)); } </pre>
     */
    default OptionalT<T> reduce(BinaryOperator<T> accumulator){
        AnyM<Optional<T>> anyM =  nestedFoldables().map(s->s.reduce(accumulator));
        return Matchables.anyM(anyM)
                         .visit(v-> OptionalT.fromValue(v.toEvalLater()), s->OptionalTSeq.of(s));
       
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.Stream#reduce(java.lang.Object,
     * java.util.function.BinaryOperator)
     */
    default AnyM<T> reduce(T identity, BinaryOperator<T> accumulator){
        return nestedFoldables().map(s->s.reduce(identity,accumulator));
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see java.util.stream.Stream#reduce(java.lang.Object,
     * java.util.function.BiFunction, java.util.function.BinaryOperator)
     */
    default <U> AnyM<U> reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner){
        return nestedFoldables().map(s->s.reduce(identity,accumulator,combiner));
    }

    /**
     * Reduce with multiple reducers in parallel NB if this Monad is an Optional
     * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
     * was one value To reduce over the values on the list, called
     * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     * 
     * <pre>
     * {
     *  &#064;code
     *  Monoid&lt;Integer&gt; sum = Monoid.of(0, (a, b) -&gt; a + b);
     *  Monoid&lt;Integer&gt; mult = Monoid.of(1, (a, b) -&gt; a * b);
     *  List&lt;Integer&gt; result = ReactiveSeq.of(1, 2, 3, 4).reduce(Arrays.asList(sum, mult).transformerStream());
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
    default ListT<T> reduce(Stream<? extends Monoid<T>> reducers){
        AnyM<ListX<T>> anyM = nestedFoldables().map(s->s.reduce(reducers));
        return Matchables.anyM(anyM)
                        .visit(v-> ListT.fromValue(v.toEvalLater()), s->ListTSeq.of(s));

    }

    /**
     * Reduce with multiple reducers in parallel NB if this Monad is an Optional
     * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
     * was one value To reduce over the values on the list, called
     * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     * 
     * <pre>
     * {@code 
     * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
     *      Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
     *      List<Integer> result = ReactiveSeq.of(1,2,3,4))
     *                                      .reduce(Arrays.asList(sum,mult) );
     *              
     *       
     *      assertThat(result,equalTo(Arrays.asList(10,24)));
     * 
     * }
     * </pre>
     * 
     * @param reducers
     * @return
     */
    default ListT<T> reduce(Iterable<? extends Monoid<T>> reducers){
        AnyM<ListX<T>> anyM = nestedFoldables().map(s->s.reduce(reducers));
        return Matchables.anyM(anyM)
                        .visit(v-> ListT.fromValue(v.toEvalLater()), s->ListTSeq.of(s));
    }


    /**
     * 
     * <pre>
     *      {@code
     *      ReactiveSeq.of("a","b","c").foldRight(Reducers.toString(""));
     *        
     *         // "cab"
     *         }
     * </pre>
     * 
     * @param reducer
     *            Use supplied Monoid to reduce values starting via foldRight
     * @return Reduced result
     */
    default AnyM<T> foldRight(Monoid<T> reducer){
        return nestedFoldables().map(s->s.foldRight(reducer));
    }

    /**
     * Immutable reduction from right to left
     * 
     * <pre>
     * {@code 
     *  assertTrue(ReactiveSeq.of("a","b","c").foldRight("", String::concat).equals("cba"));
     * }
     * </pre>
     * 
     * @param identity
     * @param accumulator
     * @return
     */
    default AnyM<T> foldRight(T identity, BinaryOperator<T> accumulator){
        return nestedFoldables().map(s->s.foldRight(identity,accumulator));
    }
    default <U> AnyM<U> foldRight(U identity, BiFunction<? super T, U,U> accumulator){
        return nestedFoldables().map(s->s.foldRight(identity,accumulator));
    }

    /**
     * Attempt to map this Monad to the same type as the supplied Monoid (using
     * mapToType on the monoid interface) Then use Monoid to reduce values
     * 
     * <pre>
     *      {@code
     *      ReactiveSeq.of(1,2,3).foldRightMapToType(Reducers.toString(""));
     *        
     *         // "321"
     *         }
     * </pre>
     * 
     * 
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    default <T> AnyM<T> foldRightMapToType(Reducer<T> reducer){
        return nestedFoldables().map(s->s.foldRightMapToType(reducer));
    }
    /**
     * <pre>
     * {@code
     *  assertEquals("123".length(),ReactiveSeq.of(1, 2, 3).join().length());
     * }
     * </pre>
     * 
     * @return Stream as concatenated String
     */
    default AnyM<String> join(){
        
        return nestedFoldables().map(s->s.join());
    }

    /**
     * <pre>
     * {@code
     * assertEquals("1, 2, 3".length(), ReactiveSeq.of(1, 2, 3).join(", ").length());
     * }
     * </pre>
     * 
     * @return Stream as concatenated String
     */
    default AnyM<String> join(String sep){
        return nestedFoldables().map(s->s.join(sep));
    }

    /**
     * <pre>
     * {@code 
     * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
     * }
     * </pre>
     * 
     * @return Stream as concatenated String
     */
    default AnyM<String> join(String sep, String start, String end){
        return nestedFoldables().map(s->s.join(sep,start,end));
    }
    
    ReactiveSeq<T> stream();
    
    default void print(PrintStream str){
        nestedFoldables().print(str);
    }
    default void print(PrintWriter writer){
        nestedFoldables().print(writer);
    }
    default void printOut(){
        nestedFoldables().printOut();
    }
    default void printErr(){
        nestedFoldables().printErr();
    }
    
    

    /**
     * Use classifier function to group elements in this Sequence into a Map
     * 
     * <pre>
     * {
     *  &#064;code
     *  Map&lt;Integer, List&lt;Integer&gt;&gt; map1 = of(1, 2, 3, 4).groupBy(i -&gt; i % 2);
     *  assertEquals(asList(2, 4), map1.get(0));
     *  assertEquals(asList(1, 3), map1.get(1));
     *  assertEquals(2, map1.size());
     * 
     * }
     * 
     * </pre>
     */
    default <K> AnyM<MapX<K, List<T>>> groupBy(Function<? super T, ? extends K> classifier){
        return nestedFoldables().map(s->s.groupBy(classifier));
    }
    

    /**
     * extract head and tail together, where head is expected to be present
     * 
     * <pre>
     * {
     *  &#064;code
     *  SequenceM&lt;String&gt; helloWorld = ReactiveSeq.of(&quot;hello&quot;, &quot;world&quot;, &quot;last&quot;);
     *  HeadAndTail&lt;String&gt; headAndTail = helloWorld.headAndTail();
     *  String head = headAndTail.head();
     *  assertThat(head, equalTo(&quot;hello&quot;));
     * 
     *  SequenceM&lt;String&gt; tail = headAndTail.tail();
     *  assertThat(tail.headAndTail().head(), equalTo(&quot;world&quot;));
     * }
     * </pre>
     * 
     * @return
     */
    default AnyM<HeadAndTail<T>> headAndTail(){
        return nestedFoldables().map(s->s.headAndTail());
    }
    
    /**
     * @return First matching element in sequential order
     * 
     *         <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5).filter(it -> it <3).findFirst().get();
     * 
     * //3
     * }
     * </pre>
     * 
     *         (deterministic)
     * 
     */
    default OptionalT<T> findFirst(){
        AnyM<Optional<T>> anyM =  nestedFoldables().map(s->s.findFirst());
        return Matchables.anyM(anyM)
                         .visit(v-> OptionalT.fromValue(v.toEvalLater()), s->OptionalTSeq.of(s));
      
    }

    
    
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
    default AnyM<Boolean> startsWithIterable(Iterable<T> iterable){
        return nestedFoldables().map(s->s.startsWithIterable(iterable));
    }

    /**
     * <pre>
     * {@code assertTrue(ReactiveSeq.of(1,2,3,4).startsWith(Arrays.asList(1,2,3).iterator())) }
     * </pre>
     * 
     * @param iterator
     * @return True if Monad starts with Iterators sequence of data
     */
    default AnyM<Boolean> startsWith(Iterator<T> iterator){
        return nestedFoldables().map(s->s.startsWith(iterator));
    }
    /**
     * <pre>
     * {@code
     *  assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
     *              .endsWith(Arrays.asList(5,6)));
     * 
     * }
     * 
     * @param iterable Values to check
     * @return true if SequenceM ends with values in the supplied iterable
     */
    default AnyM<Boolean> endsWithIterable(Iterable<T> iterable){
        return nestedFoldables().map(s->s.endsWithIterable(iterable));   
    }

    /**
     * <pre>
     * {@code
     * assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
     *              .endsWith(Stream.of(5,6))); 
     * }
     * </pre>
     * 
     * @param stream
     *            Values to check
     * @return true if SequenceM endswith values in the supplied Stream
     */
    default AnyM<Boolean> endsWith(Stream<T> stream){
        return nestedFoldables().map(s->s.endsWith(stream));
    }
    
    /**
     * Lazily converts this SequenceM into a Collection. This does not trigger
     * the Stream. E.g. Collection is not thread safe on the first iteration.
     * 
     * <pre>
     * {
     *  &#064;code
     *  Collection&lt;Integer&gt; col = ReactiveSeq.of(1, 2, 3, 4, 5).peek(System.out::println).toLazyCollection();
     * 
     *  col.forEach(System.out::println);
     * }
     * 
     * // Will print out &quot;first!&quot; before anything else
     * </pre>
     * 
     * @return
     */
    default AnyM<CollectionX<T>> toLazyCollection(){
        return nestedFoldables().map(s->s.toLazyCollection());
    }

    /**
     * Lazily converts this SequenceM into a Collection. This does not trigger
     * the Stream. E.g.
     * 
     * <pre>
     * {
     *  &#064;code
     *  Collection&lt;Integer&gt; col = ReactiveSeq.of(1, 2, 3, 4, 5).peek(System.out::println).toConcurrentLazyCollection();
     * 
     *  col.forEach(System.out::println);
     * }
     * 
     * // Will print out &quot;first!&quot; before anything else
     * </pre>
     * 
     * @return
     */
    default AnyM<CollectionX<T>> toConcurrentLazyCollection(){
        return nestedFoldables().map(s->s.toConcurrentLazyCollection());
    }

    /**
     * <pre>
     * {
     *  &#064;code
     *  Streamable&lt;Integer&gt; repeat = ReactiveSeq.of(1, 2, 3, 4, 5, 6).map(i -&gt; i + 2).toConcurrentLazyStreamable();
     * 
     *  assertThat(repeat.sequenceM().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
     *  assertThat(repeat.sequenceM().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
     * }
     * </pre>
     * 
     * @return Streamable that replay this SequenceM, populated lazily and can
     *         be populated across threads
     */
    default StreamableT<T> toConcurrentLazyStreamable(){
        AnyM<Streamable<T>> anyM =  nestedFoldables().map(s->s.toConcurrentLazyStreamable());
        return Matchables.anyM(anyM)
                         .visit(v-> StreamableT.fromValue(v.toEvalLater()), s->StreamableTSeq.of(s));
       
    }
    
    /**
     * <pre>
     * {@code 
     *  assertThat(ReactiveSeq.of(1,2,3,4)
     *                  .map(u->{throw new RuntimeException();})
     *                  .recover(e->"hello")
     *                  .firstValue(),equalTo("hello"));
     * }
     * </pre>
     * 
     * @return first value in this Stream
     */
    default AnyM<T> firstValue(){
        return nestedFoldables().map(s->s.firstValue());
    }

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
    default AnyM<T> single() {
        return nestedFoldables().map(s->s.single());

    }

    default AnyM<T> single(Predicate<? super T> predicate){
        return nestedFoldables().map(s->s.single(predicate));
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
    default OptionalT<T> singleOptional() {
        AnyM<Optional<T>> anyM =  nestedFoldables().map(s->s.singleOptional());
        return Matchables.anyM(anyM)
                         .visit(v-> OptionalT.fromValue(v.toEvalLater()), s->OptionalTSeq.of(s));
       

    }

    /**
     * Return the elementAt index or Optional.empty
     * 
     * <pre>
     * {@code
     *  assertThat(ReactiveSeq.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
     * }
     * </pre>
     * 
     * @param index
     *            to extract element from
     * @return elementAt index
     */
    default OptionalT<T> get(long index){
        AnyM<Optional<T>> anyM =  nestedFoldables().map(s->s.get(index));
        return Matchables.anyM(anyM)
                         .visit(v-> OptionalT.fromValue(v.toEvalLater()), s->OptionalTSeq.of(s));
        
    }
    
    
    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run at 8PM every night
     *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            .schedule("0 20 * * *",Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {
     *  &#064;code
     *  HotStream&lt;Data&gt; dataStream = ReactiveSeq.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
     *          .schedule(&quot;0 20 * * *&quot;, Executors.newScheduledThreadPool(1));
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
    default HotStream<T> schedule(String cron, ScheduledExecutorService ex){
        return stream().schedule(cron, ex);
    }

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run every 60 seconds after last job completes
     *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            .scheduleFixedDelay(60_000,Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {
     *  &#064;code
     *  HotStream&lt;Data&gt; dataStream = SequenceeM.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
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
    default HotStream<T> scheduleFixedDelay(long delay, ScheduledExecutorService ex){
        return stream().scheduleFixedDelay(delay, ex);
    }

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run every 60 seconds
     *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            .scheduleFixedRate(60_000,Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {
     *  &#064;code
     *  HotStream&lt;Data&gt; dataStream = SequenceeM.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
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
    default HotStream<T> scheduleFixedRate(long rate, ScheduledExecutorService ex){
        return stream().scheduleFixedRate(rate, ex);
    }
   
    default <S, F> AnyM<Ior<ReactiveSeq<F>, ReactiveSeq<S>>> validate(Validator<T, S, F> validator) {
        return nestedFoldables().map(s->s.validate(validator));
    }
}
