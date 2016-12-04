package com.aol.cyclops.types.anyM;

import static com.aol.cyclops.internal.Utils.firstOrNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.monads.AnyMonads;
import com.aol.cyclops.types.Combiner;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;
import com.aol.cyclops.util.function.Predicates;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

/**
 * Wrapper around 'Any' scalar 'M'onad
 * 
 * @author johnmcclean
 *
 * @param <T> Data types of elements managed by wrapped scalar Monad.
 */
public interface AnyMValue<T> extends AnyM<T>, Value<T>, Filterable<T>,Combiner<T>, ApplicativeFunctor<T>, MonadicValue<T>, Matchable.ValueAndOptionalMatcher<T> {

    /**
     * Equivalence test, returns true if this Monad is equivalent to the supplied monad
     * e.g.
     * <pre>
     * {code
     *     Optional.of(1) and CompletableFuture.completedFuture(1) are equivalent
     * }
     * </pre>
     * 
     * 
     * @param t Monad to compare to
     * @return true if equivalent
     */
    default boolean eqv(final AnyMValue<T> t) {
        return Predicates.eqv(t)
                         .test(this);
    }
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#combine(java.util.function.BinaryOperator, com.aol.cyclops.types.Applicative)
     */
    @Override
    default AnyMValue<T> combine(BinaryOperator<Combiner<T>> combiner, Combiner<T> app) {
        
        return (AnyMValue<T>)ApplicativeFunctor.super.combine(combiner, app);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#collect(java.util.stream.Collector)
     */
    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {

        return this.<T> toSequence()
                   .collect(collector);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> AnyMValue<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (AnyMValue<R>) ApplicativeFunctor.super.combine(app, fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> AnyMValue<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (AnyMValue<R>) ApplicativeFunctor.super.zip(app, fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> AnyMValue<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {

        return (AnyMValue<R>) ApplicativeFunctor.super.zip(fn, app);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#flatMapFirst(java.util.function.Function)
     */
    @Override
    <R> AnyMValue<R> flatMapFirst(Function<? super T, ? extends Iterable<? extends R>> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#flatMapFirstPublisher(java.util.function.Function)
     */
    @Override
    <R> AnyMValue<R> flatMapFirstPublisher(Function<? super T, ? extends Publisher<? extends R>> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    /* cojoin
     * (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
    @Override
    default AnyMValue<MonadicValue<T>> nest() {
        return unit(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue2#combine(com.aol.cyclops.Monoid, com.aol.cyclops.types.MonadicValue2)
     */
    default AnyMValue<T> combine(final Monoid<T> monoid, final AnyMValue<? extends T> v2) {
        return unit(this.<T> flatMap(t1 -> v2.map(t2 -> monoid.apply(t1, t2)))
                        .orElseGet(() -> orElseGet(() -> monoid.zero())));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#mkString()
     */
    @Override
    default String mkString() {
        final Optional<T> opt = toOptional();
        return opt.isPresent() ? "AnyMValue[" + get() + "]" : "AnyMValue[]";
    }

    /**
     * @return First value in this Monad
     */
    default Value<T> toFirstValue() {
        return () -> firstOrNull(toListX());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMValue<U> ofType(final Class<? extends U> type) {

        return (AnyMValue<U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default AnyMValue<T> filterNot(final Predicate<? super T> fn) {

        return (AnyMValue<T>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default AnyMValue<T> notNull() {

        return (AnyMValue<T>) MonadicValue.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> AnyMValue<U> cast(final Class<? extends U> type) {

        return (AnyMValue<U>) AnyM.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMValue<R>) AnyM.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> AnyMValue<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {

        return (AnyMValue<R>) AnyM.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.EmptyUnit#emptyUnit()
     */
    @Override
    <T> AnyMValue<T> emptyUnit();

    /**
    * 
    * Replicate given Monad
    * 
    * <pre>{@code 
    *  
    *   AnyM<Optional<Integer>> applied =AnyM.fromOptional(Optional.of(2)).replicateM(5);
    *   
      //AnyM[Optional[List(2,2,2,2,2)]]
      
      }</pre>
    * 
    * 
    * @param times number of times to replicate
    * @return Replicated Monad
    */
    AnyM<List<T>> replicateM(int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#stream()
     */
    @Override
    ReactiveSeq<T> stream();

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    AnyMValue<T> filter(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#map(java.util.function.Function)
     */
    @Override
    <R> AnyMValue<R> map(Function<? super T, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#peek(java.util.function.Consumer)
     */
    @Override
    AnyMValue<T> peek(Consumer<? super T> c);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#bind(java.util.function.Function)
     */
    @Override
    <R> AnyMValue<R> bind(Function<? super T, ?> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#flatten()
     */
    @Override
    <T1> AnyMValue<T1> flatten();

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#aggregate(com.aol.cyclops.monad.AnyM)
     */
    @Override
    AnyMValue<List<T>> aggregate(AnyM<T> next);

    /**
    * Convert a Stream of Monads to a Monad with a List applying the supplied function in the process
    * 
    <pre>{@code 
    Stream<CompletableFuture<Integer>> futures = createFutures();
    AnyMValue<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
     }
     </pre>
    * 
    * @param seq Stream of Monads
    * @param fn Function to apply 
    * @return Monad with a list
    */
    public static <T, R> AnyMValue<ListX<R>> traverse(final Collection<? extends AnyMValue<T>> seq, final Function<? super T, ? extends R> fn) {

        return new AnyMonads().traverse(seq, fn);
    }

    /**
     * Convert a Stream of Monads to a Monad with a Stream applying the supplied function in the process
     *
     */
    public static <T, R> AnyMValue<Stream<R>> traverse(final Stream<AnyMValue<T>> source, final Supplier<AnyMValue<Stream<T>>> unitEmpty,
            final Function<? super T, ? extends R> fn) {
        return sequence(source, unitEmpty).map(s -> s.map(fn));
    }

    /**
     * Convert a Stream of Monads to a Monad with a Stream
     *
     */
    public static <T> AnyMValue<Stream<T>> sequence(final Stream<AnyMValue<T>> source, final Supplier<AnyMValue<Stream<T>>> unitEmpty) {

        return Matchables.anyM(AnyM.sequence(source, unitEmpty))
                         .visit(v -> v, s -> {
                             throw new IllegalStateException(
                                                             "unreachable");
                         });
    }

    /**
     * Convert a Collection of Monads to a Monad with a List
     * 
     * <pre>
     * {@code
        List<CompletableFuture<Integer>> futures = createFutures();
        AnyMValue<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));
    
       //where AnyM wraps  CompletableFuture<List<Integer>>
      }</pre>
     * 
     * @param seq Collection of monads to convert
     * @return Monad with a List
     */
    public static <T1> AnyMValue<ListX<T1>> sequence(final Collection<? extends AnyMValue<T1>> seq) {
        return new AnyMonads().sequence(seq);
    }

    @Override
    default <R1, R> AnyMValue<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction){
        return For.Values.each2(this, value1,yieldingFunction);
    }

    @Override
    default <R1, R> MonadicValue<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, Boolean> filterFunction,
            final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction){
        return For.Values.each2(this, value1, filterFunction,yieldingFunction);
    }

    @Override
    default < T2, R1, R2, R>  AnyMValue<R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                    final TriFunction<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                    final TriFunction<? super T, ? super R1, ? super R2, ? extends R>  yieldingFunction){
        return For.Values.each3(this, value1, value2, filterFunction,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R> AnyMValue<R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            final TriFunction<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return For.Values.each3(this, value1, value2, yieldingFunction);
    }
    @Override
    default < T2, R1, R2,R3, R>  AnyMValue<R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            final TriFunction<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R>  yieldingFunction){
        return For.Values.each4(this, value1, value2, value3,filterFunction,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R3, R> MonadicValue<R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            final TriFunction<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return For.Values.each4(this, value1, value2, value3, yieldingFunction);
    }

    /**
     * flatMap operation
      * 
     * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
     * In particular left-identity becomes
     * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
     * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
     * only the first value is accepted.
     * 
     * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
     * <pre>
     * {@code 
     *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->AnyM.fromArray(i+1,i+2));
     *   
     *   //AnyM[Stream[2,3,3,4,4,5]]
     * }
     * </pre>
     * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
     * <pre>
     * {@code 
     *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->AnyM.fromArray(i+1,i+2));
     *   
     *   //AnyM[Optional[2]]
     * }
     * </pre>
     * @param fn flatMap function
     * @return  flatMapped AnyM
     */
    <R> AnyMValue<R> flatMap(Function<? super T, ? extends AnyMValue<? extends R>> fn);

    /**
     * Apply function/s inside supplied Monad to data in current Monad
     * 
     * e.g. with Streams
     * <pre>{@code 
     * 
     * AnyM<Integer> applied =AnyM.fromStream(Stream.of(1,2,3))
     * 								.applyM(AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
    
     	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
     }</pre>
     * 
     * with Optionals 
     * <pre>{@code
     * 
     *  Any<Integer> applied =AnyM.fromOptional(Optional.of(2)).applyM(AnyM.fromOptional(Optional.of( (Integer a)->a+1)) );
    	assertThat(applied.toList(),equalTo(Arrays.asList(3)));}
    	</pre>
     * 
     * @param fn
     * @return
     */
    <R> AnyMValue<R> applyM(AnyMValue<Function<? super T, ? extends R>> fn);

    /**
     * Sequence the contents of a Monad.  e.g.
     * Turn an <pre>
     *  {@code Optional<List<Integer>>  into Stream<Integer> }</pre>
     * 
     * <pre>{@code
     * List<Integer> list = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3,4,5,6)))
                                            .<Integer>toSequence(c->c.stream())
                                            .collect(Collectors.toList());
        
        
        assertThat(list,hasItems(1,2,3,4,5,6));
        
     * 
     * }</pre>
     * 
     * @return A Sequence that wraps a Stream
     */
    @Override
    <NT> ReactiveSeq<NT> toReactiveSeq(Function<? super T, ? extends Stream<? extends NT>> fn);

    /**
     *  <pre>
     *  {@code Optional<List<Integer>>  into Stream<Integer> }
     *  </pre>
     * Less type safe equivalent, but may be more accessible than toSequence(fn) i.e. 
     * <pre>
     * {@code 
     *    toSequence(Function<T,Stream<NT>> fn)
     *   }
     *   </pre>
     *  <pre>{@code
     * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
                                            .<Integer>toSequence()
                                            .collect(Collectors.toList());
        
        
        
     * 
     * }</pre>
    
     * @return A Sequence that wraps a Stream
     */
    <T> ReactiveSeq<T> toSequence();

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    <T> AnyMValue<T> unit(T value);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#empty()
     */
    @Override
    <T> AnyMValue<T> empty();

    @Override
    default Iterator<T> iterator() {

        return Matchable.ValueAndOptionalMatcher.super.iterator();
    }

    /**
     * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
     * AnyM view simplifies type related challenges.
     * 
     * @param fn
     * @return
     */
    public static <U, R> Function<AnyMValue<U>, AnyMValue<R>> liftM(final Function<? super U, ? extends R> fn) {
        return u -> u.map(input -> fn.apply(input));
    }

    /**
     * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
     * AnyM view simplifies type related challenges. The actual native type is not specified here.
     * 
     * e.g.
     * 
     * <pre>{@code
     * 	BiFunction<AnyMValue<Integer>,AnyMValue<Integer>,AnyMValue<Integer>> add = Monads.liftM2(this::add);
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
    public static <U1, U2, R> BiFunction<AnyMValue<U1>, AnyMValue<U2>, AnyMValue<R>> liftM2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {

        return (u1, u2) -> u1.bind(input1 -> u2.map(input2 -> fn.apply(input1, input2))
                                               .unwrap());
    }

    /**
     * Lift a jOOλ Function3  into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
     * 
     * <pre>
     * {@code
     * Function3 <AnyMValue<Double>,AnyMValue<Entity>,AnyMValue<String>,AnyMValue<Integer>> fn = liftM3(this::myMethod);
     *    
     * }
     * </pre>
     * 
     * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
     * 
     * @param fn Function to lift
     * @return Lifted function
     */
    public static <U1, U2, U3, R> Function3<AnyMValue<U1>, AnyMValue<U2>, AnyMValue<U3>, AnyMValue<R>> liftM3(
            final Function3<? super U1, ? super U2, ? super U3, ? extends R> fn) {
        return (u1, u2, u3) -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3)))
                                                   .unwrap());
    }

    /**
     * Lift a TriFunction into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
     * 
     * <pre>
     * {@code
     * TriFunction<AnyMValue<Double>,AnyMValue<Entity>,AnyMValue<String>,AnyMValue<Integer>> fn = liftM3(this::myMethod);
     *    
     * }
     * </pre>
     * 
     * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
     * 
     * @param fn Function to lift
     * @return Lifted function
     */
    public static <U1, U2, U3, R> TriFunction<AnyMValue<U1>, AnyMValue<U2>, AnyMValue<U3>, AnyMValue<R>> liftM3Cyclops(
            final TriFunction<? super U1, ? super U2, ? super U3, ? extends R> fn) {
        return (u1, u2, u3) -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3))
                                                                     .unwrap())
                                                   .unwrap());
    }

    /**
     * Lift a  jOOλ Function4 into Monadic form.
     * 
     * @param fn Quad funciton to lift
     * @return Lifted Quad function
     */
    public static <U1, U2, U3, U4, R> Function4<AnyMValue<U1>, AnyMValue<U2>, AnyMValue<U3>, AnyMValue<U4>, AnyMValue<R>> liftM4(
            final Function4<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

        return (u1, u2, u3, u4) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4))))
                                                       .unwrap());
    }

    /**
     * Lift a QuadFunction into Monadic form.
     * 
     * @param fn Quad funciton to lift
     * @return Lifted Quad function
     */
    public static <U1, U2, U3, U4, R> QuadFunction<AnyMValue<U1>, AnyMValue<U2>, AnyMValue<U3>, AnyMValue<U4>, AnyMValue<R>> liftM4Cyclops(
            final QuadFunction<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

        return (u1, u2, u3, u4) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4))
                                                                                           .unwrap())
                                                                         .unwrap())
                                                       .unwrap());
    }

    /**
     * Lift a  jOOλ Function5 (5 parameters) into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted Function
     */
    public static <U1, U2, U3, U4, U5, R> Function5<AnyMValue<U1>, AnyMValue<U2>, AnyMValue<U3>, AnyMValue<U4>, AnyMValue<U5>, AnyMValue<R>> liftM5(
            final Function5<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

        return (u1, u2, u3, u4,
                u5) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.bind(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                         input4, input5)))))
                                           .unwrap());
    }

    /**
     * Lift a QuintFunction (5 parameters) into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted Function
     */
    public static <U1, U2, U3, U4, U5, R> QuintFunction<AnyMValue<U1>, AnyMValue<U2>, AnyMValue<U3>, AnyMValue<U4>, AnyMValue<U5>, AnyMValue<R>> liftM5Cyclops(
            final QuintFunction<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

        return (u1, u2, u3, u4,
                u5) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.bind(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                         input4, input5))
                                                                                                 .unwrap())
                                                                               .unwrap())
                                                             .unwrap())
                                           .unwrap());
    }

    /**
     * Lift a Curried Function {@code(2 levels a->b->fn.apply(a,b) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, R> Function<AnyMValue<U1>, Function<AnyMValue<U2>, AnyMValue<R>>> liftM2(final Function<U1, Function<U2, R>> fn) {
        return u1 -> u2 -> u1.bind(input1 -> u2.map(input2 -> fn.apply(input1)
                                                                .apply(input2))
                                               .unwrap());

    }

    /**
     * Lift a Curried Function {@code(3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, R> Function<AnyMValue<U1>, Function<AnyMValue<U2>, Function<AnyMValue<U3>, AnyMValue<R>>>> liftM3(
            final Function<? super U1, Function<? super U2, Function<? super U3, ? extends R>>> fn) {
        return u1 -> u2 -> u3 -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1)
                                                                                        .apply(input2)
                                                                                        .apply(input3)))
                                                     .unwrap());
    }

    /**
     * Lift a Curried Function {@code(4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, U4, R> Function<AnyMValue<U1>, Function<AnyMValue<U2>, Function<AnyMValue<U3>, Function<AnyMValue<U4>, AnyMValue<R>>>>> liftM4(
            final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, ? extends R>>>> fn) {

        return u1 -> u2 -> u3 -> u4 -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.map(input4 -> fn.apply(input1)
                                                                                                                .apply(input2)
                                                                                                                .apply(input3)
                                                                                                                .apply(input4))))
                                                           .unwrap());
    }

    /**
     * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) ) }into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, U4, U5, R> Function<AnyMValue<U1>, Function<AnyMValue<U2>, Function<AnyMValue<U3>, Function<AnyMValue<U4>, Function<AnyMValue<U5>, AnyMValue<R>>>>>> liftM5(
            final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, Function<? super U5, ? extends R>>>>> fn) {

        return u1 -> u2 -> u3 -> u4 -> u5 -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.bind(input4 -> u5.map(input5 -> fn.apply(input1)
                                                                                                                                        .apply(input2)
                                                                                                                                        .apply(input3)
                                                                                                                                        .apply(input4)
                                                                                                                                        .apply(input5)))))
                                                                 .unwrap());
    }

}
