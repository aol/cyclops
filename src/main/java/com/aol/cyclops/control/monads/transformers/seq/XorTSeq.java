package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.FilterableFunctor;
import com.aol.cyclops.types.IterableCollectable;
import com.aol.cyclops.types.MonadicValue2;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad transformer for JDK Xor
 * 
 * XorT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Xor
 * 
 * XorT<AnyMSeq<*SOME_MONAD_TYPE*<Xor<T>>>>
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
public class XorTSeq<ST,T> implements ConvertableSequence<T>,
                                    ExtendedTraversable<T>,
                                    Sequential<T>,
                                    CyclopsCollectable<T>,
                                    IterableCollectable<T>,
                                    FilterableFunctor<T>,
                                    ZippingApplicativable<T>,
                                    Publisher<T>{

    private final AnyMSeq<Xor<ST,T>> run;

    private XorTSeq(final AnyMSeq<Xor<ST,T>> run) {
        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyMSeq<Xor<ST,T>> unwrap() {
        return run;
    }

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
    public XorTSeq<ST,T> peek(Consumer<? super T> peek) {
        return of(run.peek(opt -> opt.map(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Filter the wrapped Xor
     * 
     * <pre>
     * {@code 
     *    XorT.of(AnyM.fromStream(Xor.of(10))
     *             .filter(t->t!=10);
     *             
     *     //XorT<AnyMSeq<Stream<Xor.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Xor
     * @return XorT that applies the provided filter
     */
    public XorTSeq<ST,T> filter(Predicate<? super T> test) {
        return XorTSeq.of(run.map(opt -> opt.filter(test)));
    }
    /**
     * Map the wrapped Xor
     * 
     * <pre>
     * {@code 
     *  XorT.of(AnyM.fromStream(Xor.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //XorT<AnyMSeq<Stream<Xor[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Xor
     * @return XorT that applies the map function to the wrapped Xor
     */
    public <B> XorTSeq<ST,B> map(Function<? super T, ? extends B> f) {
        return new XorTSeq<ST,B>(run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Xor
     * 
     * <pre>
    * {@code 
    *  XorT.of(AnyM.fromStream(Xor.of(10))
    *             .flatMap(t->Xor.empty();
    *  
    *  
    *  //XorT<AnyMSeq<Stream<Xor.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return XorT that applies the flatMap function to the wrapped Xor
     */
    public <ST2,B> XorTSeq<ST,B> flatMapT(Function<? super T, XorTSeq<ST2,? extends B>> f) {

        return of(run.bind(opt -> {
            if (opt.isPrimary())
                return f.apply(opt.get()).run.unwrap();
            return this;
        }));

    }
    public <ST2,B> XorTSeq<ST2,B> flatMap(Function<? super T, ? extends MonadicValue2<? extends ST2,? extends B>> f) {
        
         
        return new XorTSeq<ST2,B>(run.map(o -> o.flatMap(f)));

    }

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
     *     AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMSeq<Xor<Integer>> streamOpt = stream.map(Xor::ofNullable);
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
    public static <ST,U, R> Function<XorTSeq<ST,U>, XorTSeq<ST,R>> lift(Function<? super U, ? extends R> fn) {
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
     *     AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMSeq<Xor<Integer>> streamOpt = stream.map(Xor::ofNullable);
     * 
     *     CompletableFuture<Xor<Integer>> two = CompletableFuture.supplyAsync(() -> Xor.of(2));
     *     AnyMSeq<Xor<Integer>> future = AnyM.ofMonad(two);
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
    public static <ST,U1, U2, R> BiFunction<XorTSeq<ST,U1>, XorTSeq<ST,U2>, XorTSeq<ST,R>> lift2(BiFunction<? super U1,? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
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
    public static <ST,A> XorTSeq<ST,A> fromAnyM(AnyMSeq<A> anyM) {
        return of(anyM.map(Xor::primary));
    }

    /**
     * Construct an XorT from an AnyM that wraps a monad containing Xors
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Xor
     * @return XorT
     */
    public static <ST,A> XorTSeq<ST,A> of(AnyMSeq<Xor<ST,A>> monads) {
        return new XorTSeq<>(monads);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return run.toString();
    }
   
    
    public boolean isPrimary(){
        return run.allMatch(x->x.isPrimary());
    }
    public boolean isSecondary(){
        return run.allMatch(x->x.isSecondary());
    }


    
    public <R> R visit(Function<?,? extends R> secondary, 
            Function<? super T,? extends R> primary, Monoid<R> combiner){
        
        return run.map(t->t.<R>visit((Function)secondary,primary)).reduce(combiner);   
    }
    
   
    

   
    
    public <R> XorTSeq<ST,R> unit(R value){
       return of(run.unit(Xor.primary(value)));
    }
    public <R> XorTSeq<ST,R> empty(){
        return of(run.unit(Xor.secondary(null)));
     }
    
    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMapIterable(e->e);
    }

    @Override
    public Iterator<T> iterator() {
       return stream().iterator();
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
       run.forEach(e->ListX.fromIterable(e).subscribe(s));   
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
       return this;
    } 
    public <R> XorTSeq<ST,R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->Xor.primary(i)));
    }
 
}
