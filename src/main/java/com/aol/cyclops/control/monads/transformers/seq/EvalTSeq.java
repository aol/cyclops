package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.FilterableFunctor;
import com.aol.cyclops.types.IterableCollectable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

import lombok.val;

/**
 * Monad transformer for JDK Maybe
 * 
 * MaybeT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Maybe
 * 
 * MaybeT<AnyMSeq<*SOME_MONAD_TYPE*<Maybe<T>>>>
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
public class EvalTSeq<T> implements ConvertableSequence<T>,
                                    ExtendedTraversable<T>,
                                    Sequential<T>,
                                    CyclopsCollectable<T>,
                                    IterableCollectable<T>,
                                    FilterableFunctor<T>,
                                    ZippingApplicativable<T>,
                                    Publisher<T>{

    private final AnyMSeq<Eval<T>> run;

    private EvalTSeq(final AnyMSeq<Eval<T>> run) {
        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyMSeq<Eval<T>> unwrap() {
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
    public EvalTSeq<T> peek(Consumer<? super T> peek) {
        return of(run.peek(opt -> opt.map(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Filter the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .filter(t->t!=10);
     *             
     *     //MaybeT<AnyMSeq<Stream<Maybe.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Maybe
     * @return MaybeT that applies the provided filter
     */
    public MaybeTSeq<T> filter(Predicate<? super T> test) {
        return MaybeTSeq.of(run.map(opt -> opt.filter(test)));
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
     *  //MaybeT<AnyMSeq<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Maybe
     * @return MaybeT that applies the map function to the wrapped Maybe
     */
    public <B> EvalTSeq<B> map(Function<? super T, ? extends B> f) {
        return new EvalTSeq<B>(run.map(o -> o.map(f)));
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
    *  //MaybeT<AnyMSeq<Stream<Maybe.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return MaybeT that applies the flatMap function to the wrapped Maybe
     */
    public <B> EvalTSeq<B> flatMapT(Function<? super T, EvalTSeq<? extends B>> f) {

        return of(run.bind(opt -> {
            
                return f.apply(opt.get()).run.unwrap();
           
        }));

    }
    public <B> EvalTSeq<B> flatMap(Function<? super T, ? extends MonadicValue<? extends B>> f) {

        return new EvalTSeq<B>(run.map(o -> o.flatMap(f)));

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
     *     AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMSeq<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
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
    public static <U, R> Function<EvalTSeq<U>, EvalTSeq<R>> lift(Function<? super U, ? extends R> fn) {
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
     *     AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMSeq<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
     * 
     *     CompletableFuture<Maybe<Integer>> two = CompletableFuture.supplyAsync(() -> Maybe.of(2));
     *     AnyMSeq<Maybe<Integer>> future = AnyM.ofMonad(two);
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
    public static <U1, U2, R> BiFunction<EvalTSeq<U1>, EvalTSeq<U2>, EvalTSeq<R>> lift2(BiFunction<? super U1,? super U2, ? extends R> fn) {
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
    public static <A> EvalTSeq<A> fromAnyM(AnyMSeq<A> anyM) {
        return of(anyM.map(a->Eval.later(()->a)));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> EvalTSeq<A> of(AnyMSeq<Eval<A>> monads) {
        return new EvalTSeq<>(monads);
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
    public ReactiveSeq<T> stream() {
        return run.stream().map(e->e.get());
    }

    @Override
    public Iterator<T> iterator() {
       return stream().iterator();
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
       run.forEach(e->e.subscribe(s));   
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
       return this;
    } 
    public <R> EvalTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->Eval.now(i)));
    }
    public <R> EvalTSeq<R> unit(R value){
       return of(run.unit(Eval.now(value)));
    }
    public <R> EvalTSeq<R> empty(){
        return of(run.unit(Eval.later(()->null)));
     }
 
}
