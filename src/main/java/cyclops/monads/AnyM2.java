package cyclops.monads;

import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.stream.ToStream;
import cyclops.Streams;
import cyclops.async.Future;
import cyclops.collections.ListX;
import cyclops.control.*;
import cyclops.function.*;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;

/**
 * 
 * Wrapper for Any Monad type
 * 
 * There are two subsclass of AnyM - @see {@link AnyMValue} and  @see {@link AnyMSeq}. 
 * AnyMValue is used to represent Monads that wrap a single value such as {@link Optional}, {@link CompletableFuture}, {@link Maybe}, {@link Eval}, {@link Xor}, {@link Try}, {@link Ior}, {@link FeatureToggle}
 * AnyMSeq is used to represent Monads that wrap an aggregation of values such as {@link Stream}, {@link FutureStream}, {@link List}, {@link Set}, {@link Streamable}
 * 
 * Use AnyM to create your monad wrapper.
 * AnyM.fromXXXX methods can create the appropriate AnyM type for a range of known monad types.
 * 
 * <pre>
 * {@code 
 *    AnyMValue<String> monad1 = AnyM.fromOptional(Optional.of("hello"));
 *    
 *    AnyMSeq<String> monad2 = AnyM.fromStream(Stream.of("hello","world"));
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
public interface AnyM2<W extends WitnessType<W>,T,T2> extends   AnyM<W,T>,
                                                                Unwrapable,
                                                                EmptyUnit<T>,
                                                                Unit<T>,
                                                                Folds<T>,
                                                                Transformable<T>,
                                                                ToStream<T>,
                                                                Zippable<T>,
                                                                Publisher<T> {
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
     *      List<Integer> list1 = monad1.collect(Collectors.toList());
     *      List<Integer> list2 = monad2.collect(Collectors.toList());
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

    <R> AnyM2<W,R,T2> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn);
    <R> AnyM2<W,R,T2> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn);
    <R> AnyM2<W,R,T2> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn);
    default <R> AnyM2<W,R,T2> flatMapA(Function<? super T, ? extends AnyM<W, ? extends R>> fn){
        return (AnyM2<W,R,T2>)adapter().flatMap(this, fn);
    }
    default <R> AnyM2<W,R,T2> map(Function<? super T, ? extends R> fn){
        return (AnyM2<W,R,T2>)adapter().map(this, fn);
    }
    default <T> AnyM2<W,T,T2> fromIterable(Iterable<T> t){
        return  (AnyM2<W,T,T2>)adapter().unitIterable(t);
    }

    @Override
    default <R> AnyM2<W,R,T2> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (AnyM2<W,R,T2>)AnyM.super.zipWith(fn);
    }

    @Override
    default <R> AnyM2<W,R,T2> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (AnyM2<W,R,T2>)AnyM.super.zipWithS(fn);
    }

    @Override
    default <R> AnyM2<W,R,T2> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (AnyM2<W,R,T2>)AnyM.super.zipWithP(fn);
    }

    @Override
    default <R> AnyM2<W,R,T2> retry(final Function<? super T, ? extends R> fn) {
        return (AnyM2<W,R,T2>)AnyM.super.retry(fn);
    }

    @Override
    default <U> AnyM2<W,Tuple2<T, U>,T2> zipP(final Publisher<? extends U> other) {
        return (AnyM2)AnyM.super.zipP(other);
    }

    @Override
    default <R> AnyM2<W,R,T2> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (AnyM2<W,R,T2>)AnyM.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> AnyM2<W,Tuple3<T, S, U>,T2> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (AnyM2)AnyM.super.zip3(second,third);
    }

    @Override
    default <S, U, R> AnyM2<W,R,T2> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (AnyM2<W,R,T2>)AnyM.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> AnyM2<W,Tuple4<T, T2, T3, T4>,T2> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (AnyM2)AnyM.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> AnyM2<W,R,T2> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (AnyM2<W,R,T2>)AnyM.super.zip4(second,third,fourth,fn);
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
    default <T> AnyM2<W,T,T2> unit(T t){
        return (AnyM2<W,T,T2>)adapter().unit(t);
    }
    
    /**
     * Applicative 'ap' method to use fluently
     * 
     * <pre>
     * {@code 
     *    AnyM<optional,Function<Integer,Integer>> add = AnyM.fromNullable(this::add2);
     *    add.to(AnyM::ap)
     *       .apply(AnyM.ofNullable(10));
     *   
     *    //AnyM[12] //add 2
     * 
     * }
     * </pre>
     * 
     * @param fn Function inside an Applicative
     * @return Function to apply an Applicative's value to function
     */
    public static <W extends WitnessType<W>,T,T2,R> Function<AnyM2<W,T,T2>,AnyM2<W,R,T2>> ap(AnyM2<W, Function<T, R>,T2> fn){
        return apply->(AnyM2<W,R,T2>)apply.adapter().ap(fn,apply);
    }
    /**
     * Applicative ap2 method to use fluently to apply to a curried function
     * <pre>
     * {@code 
     *    AnyM<optional,Function<Integer,Function<Integer,Integer>>> add = AnyM.fromNullable(Curry.curry2(this::add));
     *    add.to(AnyM::ap2)
     *       .apply(AnyM.ofNullable(10),AnyM.ofNullable(20));
     *   
     *    //AnyM[30] //add together
     * 
     * }
     * </pre>
     * @param fn Curried function inside an Applicative
     * @return Function to apply two Applicative's values to a function
     */
    public static <W extends WitnessType<W>,T,T2,R,T3> BiFunction<AnyM2<W,T,T3>,AnyM2<W,T2,T3>,AnyM2<W,R,T3>> ap2(AnyM2<W, Function<T, Function<T2, R>>,T3> fn){
        return (apply1,apply2)->(AnyM2<W,R,T3>)apply1.adapter().ap2(fn,apply1,apply2);
    }

    /**
     * Perform a filter operation on the wrapped monad instance e.g.
     * 
     * <pre>
     * {@code
     *   AnyM.fromOptional(Optional.of(10)).filter(i->i<10);
     * 
     *   //AnyM[Optional.empty()]
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
    default AnyM2<W,T,T2> filter(Predicate<? super T> fn){
        return (AnyM2<W,T,T2>)adapter().filter(this, fn);
    }


    default <R> AnyM2<W,R,T2> coflatMapA(final Function<? super AnyM<W, T>, R> mapper) {
        return unit(Lambda.λ(()->mapper.apply(this))).map(Supplier::get);
    }
    
    
    default AnyM2<W,AnyM<W,T>,T2> nestA() {
        return unit(this);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.EmptyUnit#emptyUnit()
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
    default boolean eqv(final AnyM2<?, T,T2> t) {
        return Predicates.eqvIterable(t)
                         .test(this);
    }

    /**
     * Allows structural matching on the value / seq nature of this AnyM.
     * If this AnyM can only store a single value an Xor.secondary with type AnyMValue is returned
     * If this AnyM can  store one or many values an Xor.primary with type AnyMSeq is returned
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
     * @return An Xor for pattern matching either an AnyMValue or AnyMSeq
     */
    Xor<AnyMValue<W,T>, AnyMSeq<W,T>> matchable();




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
        return ReactiveSeq.fromIterable(this);
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
    default AnyM2<W,T,T2> peek(Consumer<? super T> c){
        return (AnyM2<W, T,T2>) AnyM.super.peek(c);
    }



    /**
     * join / flatten one level of a nested hierarchy
     * 
     * @return Flattened / joined one level
     */ 
    static <W extends WitnessType<W>,T1,T2> AnyM2<W,T1,T2> flatten(AnyM2<W, ? extends AnyM2<W, T1,T2>,T2> nested){
        return nested.flatMapA(Function.identity());
    }
    static <W extends WitnessType<W>,T1,T2> AnyM2<W,T1,T2> flattenI(AnyM2<W, ? extends Iterable<T1>,T2> nested){
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
    default AnyM2<W,List<T>,T2> aggregate(AnyM2<W, T,T2> next){
        return unit(Stream.concat(matchable().visit(value -> value.stream(), seq -> seq.stream()), next.matchable()
                                  .visit(value -> value.stream(),
                                         seq -> seq.stream()))
                    .collect(Collectors.toList()));
    }

   
    

    /**
     * Construct an AnyM wrapping a new empty instance of the wrapped type 
     * 
     * e.g.
     * <pre>
     * {@code 
     * Any<Integer> ints = AnyM.fromStream(Stream.of(1,2,3));
     * AnyM<Integer> empty=ints.empty();
     * }
     * </pre>
     * @return Empty AnyM
     */
    default <T> AnyM2<W,T,T2> empty(){
        return (AnyM2<W,T,T2>)adapter().empty();
    }

    
    /**
     * @return String representation of this AnyM
     */
    @Override
    public String toString();



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
                            .map(i -> AnyM.fromIterable(() -> i))
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
    public static <W extends WitnessType<W>,T1,T2> AnyM2<W,ListX<T1>,T2> sequence(final Collection<? extends AnyM2<W, T1,T2>> seq, W w) {
        return sequence(seq.stream(),w).map(ListX::fromStreamS);
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
     * @param fn Function to apply 
     * @return Monad with a list
     */
    public static <W extends WitnessType<W>,T, R,T2> AnyM2<W,ListX<R>,T2> traverse(final Collection<? extends AnyM2<W, T,T2>> seq, final Function<? super T, ? extends R> fn, W w) {
        return sequence(seq,w).map(l->l.map(fn));
    }

    


    public static  <W extends WitnessType<W>,T,T2> AnyM2<W,Stream<T>,T2> sequence(Stream<? extends AnyM2<W, T,T2>> stream, W witness) {
        FunctionalAdapter<W> c = witness.adapter();
        AnyM2<W,Stream<T>,T2> identity = ( AnyM2)c.unit(ReactiveSeq.empty());
       
        BiFunction<AnyM2<W,Stream<T>,T2>,AnyM2<W,T,T2>,AnyM2<W,Stream<T>,T2>> combineToStream = (acc, next) -> (AnyM2)c.ap2(c.unit(Lambda.l2((Stream<T> a)->(T b)->ReactiveSeq.concat(a,ReactiveSeq.of(b)))),acc,next);

        BinaryOperator<AnyM2<W,Stream<T>,T2>> combineStreams = (a, b)-> (AnyM2<W,Stream<T>,T2>)a.zip(b,(z1, z2)->(Stream<T>)ReactiveSeq.concat(z1,z2)); // a.apply(b, (s1,s2)->s1);

        return stream.reduce(identity,combineToStream,combineStreams);
    }
    public static  <W extends WitnessType<W>,T,R,T2> AnyM2<W,Stream<R>,T2> traverse(Function<T, R> fn, Stream<AnyM2<W, T,T2>> stream, W witness) {
       return sequence(stream.map(h->h.map(fn)),witness);
    }
    FunctionalAdapter<W> adapter();

    public static <W extends WitnessType<W>,T,T2> AnyM2<W, T,T2> narrow(AnyM2<W, ? extends T,? extends T2> anyM){
        return (AnyM2<W,T,T2>)anyM;
    }
    
    /**
   * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
   * AnyM view simplifies type related challenges.
   * 
   * @param fn
   * @return
   */
  public static <W extends WitnessType<W>,U, R> AnyMFn1<W,U,R> liftF(final Function<? super U, ? extends R> fn) {
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
   *  Optional<Integer> result = add.apply(getBase(),getIncrease());
   *  
   *   private Integer add(Integer a, Integer b){
              return a+b;
      }
   * }</pre>
   * The add method has no null handling, but we can lift the method to Monadic form, and use Optionals to automatically handle null / empty value cases.
   * 
   * 
   * @param fn BiFunction to lift
   * @return Lifted BiFunction
   */
  public static <W extends WitnessType<W>,U1, U2, R> AnyMFn2<W,U1,U2,R> liftF2(
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
  public static <W extends WitnessType<W>,U1, U2, U3, R,T2> Fn3<AnyM2<W,U1,T2>, AnyM2<W,U2,T2>, AnyM2<W,U3,T2>, AnyM2<W,R,T2>> liftF3(
          final Function3<? super U1, ? super U2, ? super U3, ? extends R> fn) {
      return (u1, u2, u3) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3))));
  }


  /**
   * Lift a QuadFunction into Monadic form.
   * 
   * @param fn Quad funciton to lift
   * @return Lifted Quad function
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, R,T2> Fn4<AnyM2<W,U1,T2>, AnyM2<W,U2,T2>, AnyM2<W,U3,T2>, AnyM2<W,U4,T2>, AnyM2<W,R,T2>> liftF4(
          final Function4<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

      return (u1, u2, u3, u4) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4)))));
  }

  /**
   * Lift a  jOOλ Function5 (5 parameters) into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted Function
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, U5, R, T2> Fn5<AnyM2<W,U1,T2>, AnyM2<W,U2,T2>, AnyM2<W,U3,T2>, AnyM2<W,U4,T2>, AnyM2<W,U5,T2>, AnyM2<W,R,T2>> liftF5(
          final Function5<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

      return (u1, u2, u3, u4,
              u5) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.flatMapA(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                       input4, input5))))));
  }

  

  /**
   * Lift a Curried Function {@code(2 levels a->b->fn.apply(a,b) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, R, T2> Function<AnyM2<W,U1,T2>, Function<AnyM2<W,U2,T2>, AnyM2<W,R,T2>>> liftF2(final Function<U1, Function<U2, R>> fn) {
      return u1 -> u2 -> u1.flatMapA(input1 -> u2.map(input2 -> fn.apply(input1)
                                                              .apply(input2)));

  }

  /**
   * Lift a Curried Function {@code(3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, R, T2> Function<AnyM2<W,U1,T2>, Function<AnyM2<W,U2,T2>, Function<AnyM2<W,U3,T2>, AnyM2<W,R,T2>>>> liftF3(
          final Function<? super U1, Function<? super U2, Function<? super U3, ? extends R>>> fn) {
      return u1 -> u2 -> u3 -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.map(input3 -> fn.apply(input1)
                                                                                      .apply(input2)
                                                                                      .apply(input3))));
  }

  /**
   * Lift a Curried Function {@code(4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, R,T2> Function<AnyM2<W,U1,T2>, Function<AnyM2<W,U2,T2>, Function<AnyM2<W,U3,T2>, Function<AnyM2<W,U4,T2>, AnyM2<W,R,T2>>>>> liftF4(
          final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, ? extends R>>>> fn) {

      return u1 -> u2 -> u3 -> u4 -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.map(input4 -> fn.apply(input1)
                                                                                                              .apply(input2)
                                                                                                              .apply(input3)
                                                                                                              .apply(input4)))));
  }

  /**
   * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) ) }into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, U5, R, T2> Function<AnyM2<W,U1,T2>, Function<AnyM2<W,U2,T2>, Function<AnyM2<W,U3,T2>, Function<AnyM2<W,U4,T2>, Function<AnyM2<W,U5,T2>, AnyM2<W,R,T2>>>>>> liftF5(
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

    /**
     * Fluent api for type conversion
     *
     * @param reduce Funtion to convert this type
     * @return Converted type
     */
    default <R> R to2(Function<? super AnyM2<W,T,T2>,? extends R> reduce){
        return reduce.apply(this);
    }

}