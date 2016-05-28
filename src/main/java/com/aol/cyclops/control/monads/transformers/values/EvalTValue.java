package com.aol.cyclops.control.monads.transformers.values;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.Applicativable;

import lombok.val;

/**
 * Monad transformer for JDK Maybe
 * 
 * MaybeT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Maybe
 * 
 * MaybeT<AnyMValue<*SOME_MONAD_TYPE*<Maybe<T>>>>
 * 
 * MaybeT allows the deeply wrapped Maybe to be manipulating within it's nested
 * /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            The type contained on the Maybe within
 */
public class EvalTValue<T> implements EvalT<T>,
                                    TransformerValue<T>,
                                    MonadicValue<T>,
                                    Supplier<T>, 
                                    ConvertableFunctor<T>, 
                                    Filterable<T>,
                                    Applicativable<T>,
                                    Matchable.ValueAndOptionalMatcher<T>
                                    {

    private final AnyMValue<Eval<T>> run;

    private EvalTValue(final AnyMValue<Eval<T>> run) {
        this.run = run;
    } 
    public Eval<T> value(){
        return run.get();
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyMValue<Eval<T>> unwrap() {
        return run;
    }

    /**
     * Peek at the current value of the Maybe
     * 
     * <pre>
     * {@code 
     *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek
     *            Consumer to accept current value of Maybe
     * @return MaybeT with peek call
     */
    public EvalTValue<T> peek(Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });
    }

    /**
     * Filter the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .filter(t->t!=10);
     *             
     *     //MaybeT<AnyMValue<Stream<Maybe.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Maybe
     * @return MaybeT that applies the provided filter
     */
    public MaybeTValue<T> filter(Predicate<? super T> test) {
        return MaybeTValue.of(run.map(opt -> opt.filter(test)));
    }
    /**
     * Map the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //MaybeT<AnyMValue<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Maybe
     * @return MaybeT that applies the map function to the wrapped Maybe
     */
    public <B> EvalTValue<B> map(Function<? super T, ? extends B> f) {
        return new EvalTValue<B>(run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Maybe
     * 
     * <pre>
    * {@code 
    *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
    *             .flatMap(t->Maybe.empty();
    *  
    *  
    *  //MaybeT<AnyMValue<Stream<Maybe.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return MaybeT that applies the flatMap function to the wrapped Maybe
     */
    public <B> EvalTValue<B> flatMapT(Function<? super T, EvalTValue<? extends B>> f) {

        return of(run.bind(opt -> {
            
                return f.apply(opt.get()).run.unwrap();
           
        }));

    }
    public <B> EvalTValue<B> flatMap(Function<? super T, ? extends Eval<? extends B>> f) {

        return new EvalTValue<B>(run.map(o -> o.flatMap(f)));

    }

    /**
     * Lift a function into one that accepts and returns an MaybeT This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Maybe) and iteration (via Stream) to an
     * existing function
     * 
     * <pre>
     * {
     *     &#64;code
     *     Function<Integer, Integer> add2 = i -> i + 2;
     *     Function<MaybeT<Integer>, MaybeT<Integer>> optTAdd2 = MaybeT.lift(add2);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMValue<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
     *     List<Integer> results = optTAdd2.apply(MaybeT.of(streamOpt)).unwrap().<Stream<Maybe<Integer>>> unwrap()
     *             .filter(Maybe::isPresent).map(Maybe::get).collect(Collectors.toList());
     * 
     *     // Arrays.asList(3,4);
     * 
     * }
     * </pre>
     * 
     * 
     * @param fn
     *            Function to enhance with functionality from Maybe and another
     *            monad type
     * @return Function that accepts and returns an MaybeT
     */
    public static <U, R> Function<EvalTValue<U>, EvalTValue<R>> lift(Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns MaybeTs This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Maybe), iteration (via Stream) and
     * asynchronous execution (CompletableFuture) to an existing function
     * 
     * <pre>
     * {
     *     &#64;code
     *     BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
     *     BiFunction<MaybeT<Integer>, MaybeT<Integer>, MaybeT<Integer>> optTAdd2 = MaybeT.lift2(add);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMValue<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
     * 
     *     CompletableFuture<Maybe<Integer>> two = CompletableFuture.supplyAsync(() -> Maybe.of(2));
     *     AnyMValue<Maybe<Integer>> future = AnyM.ofMonad(two);
     *     List<Integer> results = optTAdd2.apply(MaybeT.of(streamOpt), MaybeT.of(future)).unwrap()
     *             .<Stream<Maybe<Integer>>> unwrap().filter(Maybe::isPresent).map(Maybe::get)
     *             .collect(Collectors.toList());
     *     // Arrays.asList(3,4);
     * }
     * </pre>
     * 
     * @param fn
     *            BiFunction to enhance with functionality from Maybe and
     *            another monad type
     * @return Function that accepts and returns an MaybeT
     */
    public static <U1, U2, R> BiFunction<EvalTValue<U1>, EvalTValue<U2>, EvalTValue<R>> lift2(BiFunction<? super U1,? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an MaybeT from an AnyM that contains a monad type that contains
     * type other than Maybe The values in the underlying monad will be mapped
     * to Maybe<A>
     * 
     * @param anyM
     *            AnyM that doesn't contain a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> EvalTValue<A> fromAnyM(AnyMValue<A> anyM) {
        return of(anyM.map(a->Eval.later(()->a)));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> EvalTValue<A> of(AnyMValue<Eval<A>> monads) {
        return new EvalTValue<>(monads);
    }
    public static <A,V extends MonadicValue<Eval<A>>> EvalTValue<A> fromValue(V monadicValue){
        return of(AnyM.ofValue(monadicValue));
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return String.format("EvalTValue[%s]", run );
    }

    @Override
    public T get() {
        return run.get().get();
    }
    
    public boolean isValuePresent(){
        return !run.isEmpty();
    }
    
    
    @Override
    public ReactiveSeq<T> stream() {
        val maybeEval = run.toMaybe();
        return maybeEval.isPresent()? maybeEval.get().stream() : ReactiveSeq.of();
    }

    @Override
    public Iterator<T> iterator() {
       val maybeEval = run.toMaybe();
       return maybeEval.isPresent()? maybeEval.get().iterator() : Arrays.<T>asList().iterator();
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        run.toMaybe().forEach(e->e.subscribe(s));
       
        
    }

    @Override
    public boolean test(T t) {
        val maybeEval = run.toMaybe();
        return maybeEval.isPresent()? maybeEval.get().test(t) :false;
      
    }
    
    public <R> EvalTValue<R> unit(R value){
       return of(run.unit(Eval.now(value)));
    }
    public <R> EvalTValue<R> empty(){
        return of(run.unit(Eval.later(()->null)));
     }

    public static <T> EvalTValue<T> of(Eval<T> eval) {
       return fromValue(Maybe.just(eval));
    }

    public static <T> EvalTValue<T> emptyMaybe() {
       return fromValue(Maybe.none());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> EvalTValue<U> cast(Class<? extends U> type) {
       
        return (EvalTValue<U>)TransformerValue.super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    public <R> EvalTValue<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       
        return (EvalTValue<R>)TransformerValue.super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> EvalTValue<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (EvalTValue<R>)TransformerValue.super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> MaybeTValue<U> ofType(Class<? extends U> type) {
       
        return (MaybeTValue<U>)EvalT.super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public MaybeTValue<T> filterNot(Predicate<? super T> fn) {
       
        return (MaybeTValue<T>)EvalT.super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    public MaybeTValue<T> notNull() {
       
        return (MaybeTValue<T>)EvalT.super.notNull();
    }
    
    @Override
    public int hashCode(){
        return run.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof EvalTValue){
            return run.equals( ((EvalTValue)o).run);
        }
        return false;
    }
 
}
