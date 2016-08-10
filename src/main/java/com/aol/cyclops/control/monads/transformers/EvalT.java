package com.aol.cyclops.control.monads.transformers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.seq.EvalTSeq;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.ToStream;

/**
 * Monad transformer for Eval
 * 
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            The type contained on the Maybe within
 */
public interface EvalT<T>  extends Publisher<T>,
                                   Functor<T>,
                                   Filterable<T>,
                                   ToStream<T>{

    public <R> EvalT<R> unit(R value);
    public <R> EvalT<R> empty();

    /**
     * @return The wrapped AnyM
     */
    public AnyM<Eval<T>> unwrap() ;

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
    public EvalT<T> peek(Consumer<? super T> peek);

    /**
     * Filter the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .filter(t->t!=10);
     *             
     *     //MaybeT<AnyM<Stream<Maybe.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Maybe
     * @return MaybeT that applies the provided filter
     */
    public MaybeT<T> filter(Predicate<? super T> test) ;
    /**
     * Map the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //MaybeT<AnyM<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Maybe
     * @return MaybeT that applies the map function to the wrapped Maybe
     */
    public <B> EvalT<B> map(Function<? super T, ? extends B> f) ;

    /**
     * Flat Map the wrapped Maybe
     * 
     * <pre>
    * {@code 
    *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
    *             .bind(t->Maybe.empty();
    *  
    *  
    *  //MaybeT<AnyM<Stream<Maybe.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return MaybeT that applies the flatMap function to the wrapped Maybe
     */
    default <B> EvalT<B> bind(Function<? super T, EvalT<? extends B>> f) {

        return of(unwrap().bind(opt -> {
            
                return f.apply(opt.get()).unwrap().unwrap();
           
        }));

    }
    
    public <B> EvalT<B> flatMap(Function<? super T, ? extends Eval<? extends B>> f);

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
     *     AnyM<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyM<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
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
    public static <U, R> Function<EvalT<U>, EvalT<R>> lift(Function<? super U, ? extends R> fn) {
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
     *     AnyM<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyM<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
     * 
     *     CompletableFuture<Maybe<Integer>> two = CompletableFuture.supplyAsync(() -> Maybe.of(2));
     *     AnyM<Maybe<Integer>> future = AnyM.ofMonad(two);
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
    public static <U1, U2, R> BiFunction<EvalT<U1>, EvalT<U2>, EvalT<R>> lift2(BiFunction<? super U1,? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
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
    public static <A> EvalT<A> fromAnyM(AnyM<A> anyM) {
        return of(anyM.map(a->Eval.later(()->a)));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> EvalT<A> of(AnyM<Eval<A>> monads) {
        return Matchables.anyM(monads).visit(v-> EvalTValue.of(v), s->EvalTSeq.of(s));

    }
 

    public static <A> EvalTValue<A> fromAnyMValue(AnyMValue<A> anyM) {
        return EvalTValue.fromAnyM(anyM);
    }

    public static <A> EvalTSeq<A> fromAnyMSeq(AnyMSeq<A> anyM) {
        return EvalTSeq.fromAnyM(anyM);
    }

    public static <A> EvalTSeq<A> fromIterable(
            Iterable<Eval<A>> iterableOfEvals) {
        return EvalTSeq.of(AnyM.fromIterable(iterableOfEvals));
    }

    public static <A> EvalTSeq<A> fromStream(Stream<Eval<A>> streamOfEvals) {
        return EvalTSeq.of(AnyM.fromStream(streamOfEvals));
    }

    public static <A> EvalTSeq<A> fromPublisher(
            Publisher<Eval<A>> publisherOfEvals) {
        return EvalTSeq.of(AnyM.fromPublisher(publisherOfEvals));
    }

    public static <A, V extends MonadicValue<Eval<A>>> EvalTValue<A> fromValue(
            V monadicValue) {
        return EvalTValue.fromValue(monadicValue);
    }

    public static <A> EvalTValue<A> fromOptional(Optional<Eval<A>> optional) {
        return EvalTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> EvalTValue<A> fromFuture(CompletableFuture<Eval<A>> future) {
        return EvalTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> EvalTValue<A> fromIterableValue(
            Iterable<Eval<A>> iterableOfEvals) {
        return EvalTValue.of(AnyM.fromIterableValue(iterableOfEvals));
    }
    public static <T> EvalTValue<T> emptyMaybe() {
        return fromValue(Maybe.none());
     }
    public static <T> EvalTSeq<T> emptyList(){
        return EvalT.fromIterable(ListX.of());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> EvalT<U> cast(Class<? extends U> type) {
        return (EvalT<U>)Functor.super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> EvalT<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (EvalT<R>)Functor.super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> EvalT<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       return (EvalT<R>)Functor.super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> MaybeT<U> ofType(Class<? extends U> type) {
        
        return (MaybeT<U>)Filterable.super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default MaybeT<T> filterNot(Predicate<? super T> fn) {
       
        return (MaybeT<T>)Filterable.super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default MaybeT<T> notNull() {
       
        return (MaybeT<T>)Filterable.super.notNull();
    }


}
