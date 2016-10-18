package com.aol.cyclops.control.monads.transformers.values;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad Transformer for Cyclops Streams
 * 
 * StreamT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Stream
 * <pre>
 * {@code 
 * StreamT<AnyM<*SOME_MONAD_TYPE*<Stream<T>>>>
 * }</pre>
 * 
 * StreamT allows the deeply wrapped Stream to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class StreamTValue<T> implements StreamT<T> {

    private final AnyMValue<ReactiveSeq<T>> run;

    private StreamTValue(final AnyMValue<? extends Stream<T>> run) {
        this.run = run.map(s -> ReactiveSeq.fromStream(s));
    }

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyMValue<ReactiveSeq<T>> unwrap() {
        return run;
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    /**
     * Peek at the current value of the Stream
     * <pre>
     * {@code 
     *    StreamT.of(AnyM.fromStream(Arrays.asStream(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Stream
     * @return StreamT with peek call
     */
    @Override
    public StreamTValue<T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });
    }

    /**
     * Filter the wrapped Stream
     * <pre>
     * {@code 
     *    StreamT.of(AnyM.fromStream(Arrays.asStream(10,11))
     *             .filter(t->t!=10);
     *             
     *     //StreamT<AnyM<Stream<Stream[11]>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped Stream
     * @return StreamT that applies the provided filter
     */
    @Override
    public StreamTValue<T> filter(final Predicate<? super T> test) {
        return of(run.map(stream -> stream.filter(test)));
    }

    /**
     * Map the wrapped Stream
     * 
     * <pre>
     * {@code 
     *  StreamT.of(AnyM.fromStream(Arrays.asStream(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //StreamT<AnyM<Stream<Stream[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Stream
     * @return StreamT that applies the map function to the wrapped Stream
     */
    @Override
    public <B> StreamTValue<B> map(final Function<? super T, ? extends B> f) {
        return new StreamTValue<B>(
                                   run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Stream
      * <pre>
     * {@code 
     *  StreamT.of(AnyM.fromStream(Arrays.asStream(10))
     *             .flatMap(t->Stream.empty();
     *  
     *  
     *  //StreamT<AnyM<Stream<Stream.empty>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return StreamT that applies the flatMap function to the wrapped Stream
     */
    public <B> StreamTValue<B> flatMapT(final Function<? super T, StreamTValue<? extends B>> f) {
        return of(run.map(stream -> stream.flatMap(a -> f.apply(a).run.stream())
                                          .<B> flatMap(a -> a)));
    }

    @Override
    public <B> StreamTValue<B> flatMap(final Function<? super T, ? extends Stream<? extends B>> f) {

        return new StreamTValue<B>(
                                   run.map(o -> o.flatMap(f)));

    }

    /**
     * Lift a function into one that accepts and returns an StreamT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add iteration handling (via Stream) and nullhandling (via Optional) to an existing function
     * <pre>
     * {@code 
    	Function<Integer,Integer> add2 = i -> i+2;
    	Function<StreamT<Integer>, StreamT<Integer>> optTAdd2 = StreamT.lift(add2);
    	
    	Stream<Integer> nums = Stream.of(1,2);
    	AnyM<Stream<Integer>> stream = AnyM.fromOptional(Optional.of(nums));
    	
    	List<Integer> results = optTAdd2.apply(StreamT.of(stream))
    									.unwrap()
    									.<Optional<Stream<Integer>>>unwrap()
    									.get()
    									.collect(Collectors.toList());
    	//Stream.of(3,4);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from Stream and another monad type
     * @return Function that accepts and returns an StreamT
     */
    public static <U, R> Function<StreamTValue<U>, StreamTValue<R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Construct an StreamT from an AnyM that contains a monad type that contains type other than Stream
     * The values in the underlying monad will be mapped to Stream<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Stream
     * @return StreamT
     */
    public static <A> StreamTValue<A> fromAnyM(final AnyMValue<A> anyM) {
        return of(anyM.map(Stream::of));
    }

    /**
     * Create a StreamT from an AnyM that wraps a monad containing a Stream
     * 
     * @param monads
     * @return
     */
    public static <A> StreamTValue<A> of(final AnyMValue<? extends Stream<A>> monads) {
        return new StreamTValue<>(
                                  monads);
    }

    public static <A> StreamTValue<A> of(final Stream<A> monads) {
        return StreamT.fromOptional(Optional.of(monads));
    }

    public static <A, V extends MonadicValue<? extends Stream<A>>> StreamTValue<A> fromValue(final V monadicValue) {
        return of(AnyM.ofValue(monadicValue));
    }

    public boolean isStreamPresent() {
        return !run.isEmpty();
    }

    public Stream<T> get() {
        return run.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("StreamTValue[%s]", run);
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
    public <U> StreamTValue<U> unitIterator(final Iterator<U> u) {
        return of(run.unit(StreamUtils.stream(u)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> StreamTValue<T> unit(final T unit) {
        return of(run.unit(Stream.of(unit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .flatMap(i -> i);
    }

    @Override
    public <R> StreamTValue<R> empty() {
        return of(run.empty());
    }

    public static <T> StreamTValue<T> emptyOptional() {
        return StreamT.fromOptional(Optional.empty());
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
    public <T> StreamTValue<T> unitAnyM(final AnyM<Traversable<T>> traversable) {

        return of((AnyMValue) traversable.map(t -> ReactiveSeq.fromIterable(t)));
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
    public boolean equals(final Object o) {
        if (o instanceof StreamTValue) {
            return run.equals(((StreamTValue) o).run);
        }
        return false;
    }

}