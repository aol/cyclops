package com.aol.cyclops.control.monads.transformers.values;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.util.stream.Streamable;

/**
 * Monad Transformer for Cyclops Streamables
 * 
 * StreamableT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Streamable
 * 
 * StreamableT<AnyM<*SOME_MONAD_TYPE*<Streamable<T>>>>
 * 
 * StreamableT allows the deeply wrapped Streamable to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class StreamableTValue<T> implements StreamableT<T> {

    final AnyMValue<Streamable<T>> run;

    private StreamableTValue(final AnyMValue<Streamable<T>> run) {
        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyM<Streamable<T>> unwrap() {
        return run;
    }

    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    /**
     * Peek at the current value of the Streamable
     * <pre>
     * {@code 
     *    StreamableT.of(AnyM.fromStream(Streamable.of(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Streamable
     * @return StreamableT with peek call
     */
    public StreamableTValue<T> peek(Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }

    /**
     * Filter the wrapped Streamable
     * <pre>
     * {@code 
     *    StreamableT.of(AnyM.fromStream(Streamable.of(10,11))
     *             .filter(t->t!=10);
     *             
     *     //StreamableT<AnyM<Stream<Streamable[11]>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped Streamable
     * @return StreamableT that applies the provided filter
     */
    public StreamableTValue<T> filter(Predicate<? super T> test) {
        return of(run.map(stream -> stream.filter(test)));
    }

    /**
     * Map the wrapped Streamable
     * 
     * <pre>
     * {@code 
     *  StreamableT.of(AnyM.fromStream(Streamable.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //StreamableT<AnyM<Stream<Streamable[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Streamable
     * @return StreamableT that applies the map function to the wrapped Streamable
     */
    public <B> StreamableTValue<B> map(Function<? super T, ? extends B> f) {
        return new StreamableTValue<B>(
                                       run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Streamable
      * <pre>
     * {@code 
     *  StreamableT.of(AnyM.fromStream(Arrays.asStreamable(10))
     *             .flatMap(t->Streamable.of(2));
     *  
     *  
     *  //StreamableT<AnyM<Stream<Streamable.[2]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return StreamableT that applies the flatMap function to the wrapped Streamable
     */
    public <B> StreamableTValue<B> flatMapT(Function<? super T, StreamableTValue<? extends B>> f) {
        return of(run.map(stream -> stream.flatMap(a -> Streamable.fromStream(f.apply(a).run.stream()))
                                          .<B> flatMap(a -> a)));
    }

    public <B> StreamableTValue<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f) {

        return new StreamableTValue<B>(
                                       run.map(o -> o.flatMapIterable(f)));

    }

    /**
     * Lift a function into one that accepts and returns an StreamableT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling  / iteration (via Streamable) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
     *Function<Integer,Integer> add2 = i -> i+2;
    	Function<StreamableT<Integer>, StreamableT<Integer>> optTAdd2 = StreamableT.lift(add2);
    	
    	Stream<Integer> nums = Stream.of(1,2);
    	AnyM<Stream<Integer>> stream = AnyM.ofMonad(Optional.of(nums));
    	
    	List<Integer> results = optTAdd2.apply(StreamableT.fromStream(stream))
    									.unwrap()
    									.<Optional<Streamable<Integer>>>unwrap()
    									.get()
    									.collect(Collectors.toList());
    	
    	
    	//Streamable.of(3,4);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from Streamable and another monad type
     * @return Function that accepts and returns an StreamableT
     */
    public static <U, R> Function<StreamableTValue<U>, StreamableTValue<R>> lift(Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  StreamableTs
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling / iteration (via Streamable), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
     * to an existing function
     * 
     * <pre>
     * {@code 
     * BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<StreamableT<Integer>,StreamableT<Integer>, StreamableT<Integer>> optTAdd2 = StreamableT.lift2(add);
    	
    	Streamable<Integer> threeValues = Streamable.of(1,2,3);
    	AnyM<Integer> stream = AnyM.ofMonad(threeValues);
    	AnyM<Streamable<Integer>> streamOpt = stream.map(Streamable::of);
    	
    	CompletableFuture<Streamable<Integer>> two = CompletableFuture.completedFuture(Streamable.of(2));
    	AnyM<Streamable<Integer>> future=  AnyM.fromCompletableFuture(two);
    	List<Integer> results = optTAdd2.apply(StreamableT.of(streamOpt),StreamableT.of(future))
    									.unwrap()
    									.<Stream<Streamable<Integer>>>unwrap()
    									.flatMap(i->i.sequenceM())
    									.collect(Collectors.toList());
    		//Streamable.of(3,4);							
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Streamable and another monad type
     * @return Function that accepts and returns an StreamableT
     */
    public static <U1, U2, R> BiFunction<StreamableTValue<U1>, StreamableTValue<U2>, StreamableTValue<R>> lift2(
            BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an StreamableT from an AnyM that contains a monad type that contains type other than Streamable
     * The values in the underlying monad will be mapped to Streamable<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Streamable
     * @return StreamableT
     */
    public static <A> StreamableTValue<A> fromAnyM(AnyMValue<A> anyM) {
        return of(anyM.map(Streamable::of));
    }

    /**
     * Construct an StreamableT from an AnyM that wraps a monad containing  Streamables
     * 
     * @param monads AnyM that contains a monad wrapping an Streamable
     * @return StreamableT
     */
    public static <A> StreamableTValue<A> of(AnyMValue<Streamable<A>> monads) {
        return new StreamableTValue<>(
                                      monads);
    }

    public static <A> StreamableTValue<A> of(Streamable<A> monads) {
        return StreamableT.fromOptional(Optional.of(monads));
    }

    /**
     * Create a StreamableT from an AnyM that wraps a monad containing a Stream
     * 
     * @param monads
     * @return
     */
    public static <A> StreamableTValue<A> fromStream(AnyMValue<Stream<A>> monads) {
        return new StreamableTValue<>(
                                      monads.map(Streamable::fromStream));
    }

    public static <A, V extends MonadicValue<Streamable<A>>> StreamableTValue<A> fromValue(V monadicValue) {
        return of(AnyM.ofValue(monadicValue));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return String.format("StreamableTValue[%s]", run);
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    public <U> StreamableTValue<U> unitIterator(Iterator<U> u) {
        return of(run.unit(Streamable.fromIterator(u)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> StreamableTValue<T> unit(T unit) {
        return of(run.unit(Streamable.of(unit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .flatMap(i -> i.stream());
    }

    @Override
    public <R> StreamableTValue<R> empty() {
        return of(run.empty());
    }

    public static <T> StreamableTValue<T> emptyOptional() {
        return StreamableT.emptyOptional();
    }

    public boolean isStreamablePresent() {
        return !run.isEmpty();
    }

    public Streamable<T> get() {
        return run.get();
    }

    @Override
    public AnyM<? extends IterableFoldable<T>> nestedFoldables() {
        return run;

    }

    @Override
    public AnyM<? extends CyclopsCollectable<T>> nestedCollectables() {
        return run;

    }

    @Override
    public <T> StreamableTValue<T> unitAnyM(AnyM<Traversable<T>> traversable) {

        return of((AnyMValue) traversable.map(t -> Streamable.fromIterable(t)));
    }

    @Override
    public AnyM<? extends Traversable<T>> transformerStream() {

        return run;
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StreamableTValue) {
            return run.equals(((StreamableTValue) o).run);
        }
        return false;
    }

}