package com.aol.cyclops.control.monads.transformers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.seq.XorTSeq;
import com.aol.cyclops.control.monads.transformers.values.XorTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.MonadicValue2;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

/**
 * Monad transformer for JDK Xor
 * 
 * XorT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Xor
 * 
 * XorT<AnyM<*SOME_MONAD_TYPE*<Xor<T>>>>
 * 
 * XorT allows the deeply wrapped Xor to be manipulating within it's nested
 * /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            The type contained on the Xor within
 */
public interface XorT<ST,T>  extends Publisher<T>{
    public <R> XorT<ST,R> unit(R value);
    public <R> XorT<ST,R> empty();
    
    <ST2,B> XorT<ST2,B> flatMap(Function<? super T, ? extends MonadicValue2<? extends ST2,? extends B>> f);

    /**
     * @return The wrapped AnyM
     */
    AnyM<Xor<ST,T>> unwrap();

    /**
     * Peek at the current value of the Xor
     * 
     * <pre>
     * {@code 
     *    XorT.of(AnyM.fromStream(Xor.of(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek
     *            Consumer to accept current value of Xor
     * @return XorT with peek call
     */
    public XorT<ST,T> peek(Consumer<? super T> peek);

    /**
     * Filter the wrapped Xor
     * 
     * <pre>
     * {@code 
     *    XorT.of(AnyM.fromStream(Xor.of(10))
     *             .filter(t->t!=10);
     *             
     *     //XorT<AnyM<Stream<Xor.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Xor
     * @return XorT that applies the provided filter
     */
    public XorT<ST,T> filter(Predicate<? super T> test);
    /**
     * Map the wrapped Xor
     * 
     * <pre>
     * {@code 
     *  XorT.of(AnyM.fromStream(Xor.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //XorT<AnyM<Stream<Xor[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Xor
     * @return XorT that applies the map function to the wrapped Xor
     */
    public <B> XorT<ST,B> map(Function<? super T, ? extends B> f);

    /**
     * Flat Map the wrapped Xor
     * 
     * <pre>
    * {@code 
    *  XorT.of(AnyM.fromStream(Xor.of(10))
    *             .flatMap(t->Xor.empty();
    *  
    *  
    *  //XorT<AnyM<Stream<Xor.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return XorT that applies the flatMap function to the wrapped Xor
     */
    default <B> XorT<ST,B> bind(Function<? super T, XorT<ST,? extends B>> f) {

        return of(unwrap().bind(opt -> {
            if (opt.isPrimary())
                return f.apply(opt.get()).unwrap().unwrap();
            return this;
        }));

    }

    XorT<T,ST> swap();
    /**
     * Lift a function into one that accepts and returns an XorT This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Xor) and iteration (via Stream) to an
     * existing function
     * 
     * <pre>
     * {
     *     &#64;code
     *     Function<Integer, Integer> add2 = i -> i + 2;
     *     Function<XorT<Integer>, XorT<Integer>> optTAdd2 = XorT.lift(add2);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyM<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyM<Xor<Integer>> streamOpt = stream.map(Xor::ofNullable);
     *     List<Integer> results = optTAdd2.apply(XorT.of(streamOpt)).unwrap().<Stream<Xor<Integer>>> unwrap()
     *             .filter(Xor::isPresent).map(Xor::get).collect(Collectors.toList());
     * 
     *     // Arrays.asList(3,4);
     * 
     * }
     * </pre>
     * 
     * 
     * @param fn
     *            Function to enhance with functionality from Xor and another
     *            monad type
     * @return Function that accepts and returns an XorT
     */
    public static <ST,U, R> Function<XorT<ST,U>, XorT<ST,R>> lift(Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns XorTs This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Xor), iteration (via Stream) and
     * asynchronous execution (CompletableFuture) to an existing function
     * 
     * <pre>
     * {
     *     &#64;code
     *     BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
     *     BiFunction<XorT<Integer>, XorT<Integer>, XorT<Integer>> optTAdd2 = XorT.lift2(add);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyM<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyM<Xor<Integer>> streamOpt = stream.map(Xor::ofNullable);
     * 
     *     CompletableFuture<Xor<Integer>> two = CompletableFuture.supplyAsync(() -> Xor.of(2));
     *     AnyM<Xor<Integer>> future = AnyM.ofMonad(two);
     *     List<Integer> results = optTAdd2.apply(XorT.of(streamOpt), XorT.of(future)).unwrap()
     *             .<Stream<Xor<Integer>>> unwrap().filter(Xor::isPresent).map(Xor::get)
     *             .collect(Collectors.toList());
     *     // Arrays.asList(3,4);
     * }
     * </pre>
     * 
     * @param fn
     *            BiFunction to enhance with functionality from Xor and
     *            another monad type
     * @return Function that accepts and returns an XorT
     */
    public static <ST,U1, U2, R> BiFunction<XorT<ST,U1>, XorT<ST,U2>, XorT<ST,R>> lift2(BiFunction<? super U1,? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an XorT from an AnyM that contains a monad type that contains
     * type other than Xor The values in the underlying monad will be mapped
     * to Xor<A>
     * 
     * @param anyM
     *            AnyM that doesn't contain a monad wrapping an Xor
     * @return XorT
     */
    public static <ST,A> XorT<ST,A> fromAnyM(AnyM<A> anyM) {
        return of(anyM.map(Xor::primary));
    }

    /**
     * Construct an XorT from an AnyM that wraps a monad containing Xors
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Xor
     * @return XorT
     */
    public static <ST,A> XorT<ST,A> of(AnyM<Xor<ST,A>> monads) {
        return Matchables.anyM(monads).visit(v-> XorTValue.of(v), s->XorTSeq.of(s));
    }

    
    public static <ST,A> XorTValue<ST,A> fromAnyMValue(AnyMValue<A> anyM) {
        return XorTValue.fromAnyM(anyM);
    }

    public static <ST,A> XorTSeq<ST,A> fromAnyMSeq(AnyMSeq<A> anyM) {
        return XorTSeq.fromAnyM(anyM);
    }

    public static <ST,A> XorTSeq<ST,A> fromIterable(
            Iterable<Xor<ST,A>> iterableOfXors) {
        return XorTSeq.of(AnyM.fromIterable(iterableOfXors));
    }

    public static <ST,A> XorTSeq<ST,A> fromStream(Stream<Xor<ST,A>> streamOfXors) {
        return XorTSeq.of(AnyM.fromStream(streamOfXors));
    }

    public static <ST,A> XorTSeq<ST,A> fromPublisher(
            Publisher<Xor<ST,A>> publisherOfXors) {
        return XorTSeq.of(AnyM.fromPublisher(publisherOfXors));
    }

    public static <A, ST, V extends MonadicValue<Xor<ST,A>>> XorTValue<ST,A> fromValue(
            V monadicValue) {
        return XorTValue.fromValue(monadicValue);
    }

    public static <ST,A> XorTValue<ST,A> fromOptional(Optional<Xor<ST,A>> optional) {
        return XorTValue.of(AnyM.fromOptional(optional));
    }

    public static <ST,A> XorTValue<ST,A> fromFuture(CompletableFuture<Xor<ST,A>> future) {
        return XorTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <ST,A> XorTValue<ST,A> fromIterablXorue(
            Iterable<Xor<ST,A>> iterableOfXors) {
        return XorTValue.of(AnyM.fromIterableValue(iterableOfXors));
    }
    public static<ST,PT>  XorTValue<ST,PT> emptyOptional() {
        return XorT.fromOptional(Optional.empty());
    }
    public static <ST,T> XorTSeq<ST,T> emptyList(){
        return XorT.fromIterable(ListX.of());
    }

}
