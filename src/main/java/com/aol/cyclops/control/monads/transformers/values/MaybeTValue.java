package com.aol.cyclops.control.monads.transformers.values;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.Applicativable;

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
public class MaybeTValue<T> implements MaybeT<T>, 
                                    TransformerValue<T>,
                                    MonadicValue<T>,
                                    Supplier<T>, 
                                    ConvertableFunctor<T>, 
                                    Filterable<T>,
                                    Applicativable<T>,
                                    Matchable.ValueAndOptionalMatcher<T>
                                    {

    
    private final AnyMValue<Maybe<T>> run;

    private MaybeTValue(final AnyMValue<Maybe<T>> run) {
        this.run = run;
    }

    public Maybe<T> value(){
        return run.get();
    }
    public boolean isValuePresent(){
        return !run.isEmpty();
    }
    /**
     * @return The wrapped AnyM
     */
    public AnyMValue<Maybe<T>> unwrap() {
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
    public MaybeTValue<T> peek(Consumer<? super T> peek) {
        return map(in->{
            peek.accept(in);
            return in;
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
        return of(run.map(opt -> opt.filter(test)));
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
    public <B> MaybeTValue<B> map(Function<? super T, ? extends B> f) {
        return new MaybeTValue<B>(run.map(o -> o.map(f)));
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
    public <B> MaybeTValue<B> flatMapT(Function<? super T, MaybeTValue<? extends B>> f) {

        return of(run.bind(opt -> {
            if (opt.isPresent())
                return f.apply(opt.get()).run.unwrap();
            return run.unit(Maybe.<B> none()).unwrap();
        }));

    }
    public <B> MaybeTValue<B> flatMap(Function<? super T, ? extends MonadicValue<? extends B>> f) {

        return new MaybeTValue<B>(run.map(o -> o.flatMap(f)));

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
    public static <U, R> Function<MaybeTValue<U>, MaybeTValue<R>> lift(Function<? super U, ? extends R> fn) {
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
    public static <U1, U2, R> BiFunction<MaybeTValue<U1>, MaybeTValue<U2>, MaybeTValue<R>> lift2(BiFunction<? super U1,? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }
    public static <A,V extends MonadicValue<Maybe<A>>> MaybeTValue<A> fromValue(V monadicValue){
        return of(AnyM.ofValue(monadicValue));
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
    public static <A> MaybeTValue<A> fromAnyM(AnyMValue<A> anyM) {
        return of(anyM.map(Maybe::ofNullable));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> MaybeTValue<A> of(AnyMValue<Maybe<A>> monads) {
        
        return new MaybeTValue<>(monads);
    }
    public static <A> MaybeTValue<A> of(Maybe<A> maybe) {
        
        return fromValue(Maybe.just(maybe));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return run.toString();
    }

    @Override
    public T get() {
        return run.get().get();
    }
    
    
    
    public boolean isPresent(){
        return run.orElse(Maybe.none()).isPresent();
    }

    @Override
    public ReactiveSeq<T> stream() {
      return run.orElse(Maybe.none()).stream();
    }

    @Override
    public Iterator<T> iterator() {
       return run.orElse(Maybe.none()).iterator();
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        run.orElse(Maybe.none()).subscribe(s);
        
    }

    @Override
    public boolean test(T t) {
       return run.get().test(t);
    }


    public <R> MaybeTValue<R> unit(R value){
       return of(run.unit(Maybe.of(value)));
    }
    public <R> MaybeTValue<R> empty(){
        return of(run.unit(Maybe.none()));
     }

    public static<T>  MaybeTValue<T> emptyOptional() {
        return MaybeT.fromOptional(Optional.empty());
    }
 
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> MaybeTValue<U> cast(Class<U> type) {
       
        return (MaybeTValue<U>)TransformerValue.super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    public <R> MaybeTValue<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       
        return (MaybeTValue<R>)TransformerValue.super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> MaybeTValue<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (MaybeTValue<R>)TransformerValue.super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> MaybeTValue<U> ofType(Class<U> type) {
       
        return (MaybeTValue<U>)MaybeT.super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public MaybeTValue<T> filterNot(Predicate<? super T> fn) {
       
        return (MaybeTValue<T>)MaybeT.super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    public MaybeTValue<T> notNull() {
       
        return (MaybeTValue<T>)MaybeT.super.notNull();
    }
}
