package com.aol.cyclops.control.monads.transformers.seq;


import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.CyclopsCollectable;


/**
 * Monad Transformer for Cyclops Streams
 * 
 * StreamT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Stream
 * 
 * StreamT<AnyM<*SOME_MONAD_TYPE*<Stream<T>>>>
 * 
 * StreamT allows the deeply wrapped Stream to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class StreamTSeq<T> implements StreamT<T>{
  
    private final AnyMSeq<ReactiveSeq<T>> run;

   private StreamTSeq(final AnyMSeq<? extends Stream<T>> run){
       this.run = run.map(s->ReactiveSeq.fromStream(s));
   }
   public boolean isSeqPresent() {
       return !run.isEmpty();
     }
   /**
	 * @return The wrapped AnyM
	 */
   public AnyMSeq<ReactiveSeq<T>> unwrap(){
	   return run;
   }
   /**
  	 * Peek at the current value of the Stream
  	 * <pre>
  	 * {@code 
  	 *    StreamT.fromIterable(ListX.of(Stream.of(10))
  	 *             .peek(System.out::println);
  	 *             
  	 *     //prints 10        
  	 * }
  	 * </pre>
  	 * 
  	 * @param peek  Consumer to accept current value of Stream
  	 * @return StreamT with peek call
  	 */
   public StreamTSeq<T> peek(Consumer<? super T> peek){
	   return map(a-> {peek.accept(a); return a;});
   }
   /**
 	 * Filter the wrapped Stream
 	 * <pre>
 	 * {@code 
 	 *   StreamT.fromIterable(ListX.of(Stream.of(10,11))
 	 *          .filter(t->t!=10);
 	 *             
 	 *     //StreamT<[11]>>
 	 * }
 	 * </pre>
 	 * @param test Predicate to filter the wrapped Stream
 	 * @return StreamT that applies the provided filter
 	 */
   public StreamTSeq<T> filter(Predicate<? super T> test){
       return of(run.map(stream-> stream.filter(test)));
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
   public <B> StreamTSeq<B> map(Function<? super T,? extends B> f){
       return new StreamTSeq<B>(run.map(o-> o.map(f)));
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
   public <B> StreamTSeq<B> flatMapT(Function<? super T,StreamTSeq<? extends B>> f){
	   return of(run.map(stream-> stream.flatMap(a-> f.apply(a).run.stream())
			   							.<B>flatMap(a->a)));
   }
   public <B> StreamTSeq<B> flatMap(Function<? super T, ? extends Stream<? extends B>> f) {

       return new StreamTSeq<B>(run.map(o -> o.flatMap(f)));

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
   public static <U, R> Function<StreamTSeq<U>, StreamTSeq<R>> lift(Function<? super U,? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}
   /**
	 * Construct an StreamT from an AnyM that contains a monad type that contains type other than Stream
	 * The values in the underlying monad will be mapped to Stream<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Stream
	 * @return StreamT
	 */
   public static <A> StreamTSeq<A> fromAnyM(AnyMSeq<A> anyM){
	   return of(anyM.map(Stream::of));
   }
   /**
	 * Create a StreamT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
   public static <A> StreamTSeq<A> of(AnyMSeq<? extends Stream<A>> monads){
	   return new StreamTSeq<>(monads);
   }
   public static <A> StreamTSeq<A> of(Stream<A> monads){
       return StreamT.fromIterable(ReactiveSeq.of(monads));
   }
   
   /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
   public String toString() {
       return String.format("StreamTSeq[%s]", run );
	}
 
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> StreamTSeq<T> unit(T unit) {
        return of(run.unit(Stream.of(unit)));
    }
  
    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMap(e->e);
    }

    @Override
    public Iterator<T> iterator() {
       return stream().iterator();
    }


    public <R> StreamTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->Stream.of(i)));
    }
    @Override
    public <R> StreamT<R> empty() {
       return of(run.empty());
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
    public <T> StreamTSeq<T> unitAnyM(AnyM<Traversable<T>> traversable) {
        
        return of((AnyMSeq)traversable.map(t->ReactiveSeq.fromIterable(t)));
    }
    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {
        
        return run;
    }
    public static <T> StreamTSeq<T> emptyStream(){
        return StreamT.fromIterable(ReactiveSeq.empty());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public StreamTSeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (StreamTSeq<T>)StreamT.super.combine(predicate, op);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#cycle(int)
     */
    @Override
    public StreamTSeq<T> cycle(int times) {
       
        return (StreamTSeq<T>)StreamT.super.cycle(times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public StreamTSeq<T> cycle(Monoid<T> m, int times) {
       
        return (StreamTSeq<T>)StreamT.super.cycle(m, times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> cycleWhile(Predicate<? super T> predicate) {
       
        return (StreamTSeq<T>)StreamT.super.cycleWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> cycleUntil(Predicate<? super T> predicate) {
       
        return (StreamTSeq<T>)StreamT.super.cycleUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> StreamTSeq<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (StreamTSeq<R>)StreamT.super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> StreamTSeq<Tuple2<T, U>> zip(Stream<? extends U> other) {
       
        return (StreamTSeq)StreamT.super.zip(other);
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> StreamTSeq<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (StreamTSeq)StreamT.super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> StreamTSeq<Tuple4<T, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third,
            Stream<? extends T4> fourth) {
       
        return (StreamTSeq)StreamT.super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#zipWithIndex()
     */
    @Override
    public StreamTSeq<Tuple2<T, Long>> zipWithIndex() {
       
        return (StreamTSeq<Tuple2<T, Long>>)StreamT.super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#sliding(int)
     */
    @Override
    public StreamTSeq<ListX<T>> sliding(int windowSize) {
       
        return (StreamTSeq<ListX<T>>)StreamT.super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#sliding(int, int)
     */
    @Override
    public StreamTSeq<ListX<T>> sliding(int windowSize, int increment) {
       
        return (StreamTSeq<ListX<T>>)StreamT.super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamTSeq<C> grouped(int size, Supplier<C> supplier) {
       
        return (StreamTSeq<C> )StreamT.super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (StreamTSeq<ListX<T>>)StreamT.super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public StreamTSeq<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
       
        return (StreamTSeq<ListX<T>>)StreamT.super.groupedStatefullyWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (StreamTSeq<ListX<T>>)StreamT.super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamTSeq<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
       
        return (StreamTSeq<C>)StreamT.super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamTSeq<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
       
        return (StreamTSeq<C>)StreamT.super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#grouped(int)
     */
    @Override
    public StreamTSeq<ListX<T>> grouped(int groupSize) {
       
        return ( StreamTSeq<ListX<T>>)StreamT.super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> StreamTSeq<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (StreamTSeq)StreamT.super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#grouped(java.util.function.Function)
     */
    @Override
    public <K> StreamTSeq<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (StreamTSeq)StreamT.super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#distinct()
     */
    @Override
    public StreamTSeq<T> distinct() {
       
        return (StreamTSeq<T>)StreamT.super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public StreamTSeq<T> scanLeft(Monoid<T> monoid) {
       
        return (StreamTSeq<T>)StreamT.super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> StreamTSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
       
        return (StreamTSeq<U>)StreamT.super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public StreamTSeq<T> scanRight(Monoid<T> monoid) {
       
        return (StreamTSeq<T>)StreamT.super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> StreamTSeq<U> scanRight(U identity, BiFunction<? super T, ? super U,? extends U> combiner) {
       
        return (StreamTSeq<U>)StreamT.super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#sorted()
     */
    @Override
    public StreamTSeq<T> sorted() {
       
        return (StreamTSeq<T>)StreamT.super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#sorted(java.util.Comparator)
     */
    @Override
    public StreamTSeq<T> sorted(Comparator<? super T> c) {
       
        return (StreamTSeq<T>)StreamT.super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#takeWhile(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> takeWhile(Predicate<? super T> p) {
       
        return (StreamTSeq<T>)StreamT.super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#dropWhile(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> dropWhile(Predicate<? super T> p) {
       
        return (StreamTSeq<T>)StreamT.super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#takeUntil(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> takeUntil(Predicate<? super T> p) {
       
        return (StreamTSeq<T>)StreamT.super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#dropUntil(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> dropUntil(Predicate<? super T> p) {
       
        return (StreamTSeq<T>)StreamT.super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#dropRight(int)
     */
    @Override
    public StreamTSeq<T> dropRight(int num) {
       
        return (StreamTSeq<T>)StreamT.super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#takeRight(int)
     */
    @Override
    public StreamTSeq<T> takeRight(int num) {
       
        return (StreamTSeq<T>)StreamT.super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#skip(long)
     */
    @Override
    public StreamTSeq<T> skip(long num) {
       
        return (StreamTSeq<T>)StreamT.super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#skipWhile(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> skipWhile(Predicate<? super T> p) {
       
        return (StreamTSeq<T>)StreamT.super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#skipUntil(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> skipUntil(Predicate<? super T> p) {
       
        return (StreamTSeq<T>)StreamT.super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#limit(long)
     */
    @Override
    public StreamTSeq<T> limit(long num) {
       
        return (StreamTSeq<T>)StreamT.super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#limitWhile(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> limitWhile(Predicate<? super T> p) {
       
        return (StreamTSeq<T>)StreamT.super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#limitUntil(java.util.function.Predicate)
     */
    @Override
    public StreamTSeq<T> limitUntil(Predicate<? super T> p) {
       
        return (StreamTSeq<T>)StreamT.super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#intersperse(java.lang.Object)
     */
    @Override
    public StreamTSeq<T> intersperse(T value) {
       
        return (StreamTSeq<T>)StreamT.super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#reverse()
     */
    @Override
    public StreamTSeq<T> reverse() {
       
        return (StreamTSeq<T>)StreamT.super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#shuffle()
     */
    @Override
    public StreamTSeq<T> shuffle() {
       
        return (StreamTSeq<T>)StreamT.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#skipLast(int)
     */
    @Override
    public StreamTSeq<T> skipLast(int num) {
       
        return (StreamTSeq<T>)StreamT.super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#limitLast(int)
     */
    @Override
    public StreamTSeq<T> limitLast(int num) {
       
        return (StreamTSeq<T>)StreamT.super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#onEmpty(java.lang.Object)
     */
    @Override
    public StreamTSeq<T> onEmpty(T value) {
       
        return (StreamTSeq<T>)StreamT.super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public StreamTSeq<T> onEmptyGet(Supplier<? extends T> supplier) {
       
        return (StreamTSeq<T>)StreamT.super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> StreamTSeq<T> onEmptyThrow(Supplier<? extends X> supplier) {
       
        return (StreamTSeq<T>)StreamT.super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#shuffle(java.util.Random)
     */
    @Override
    public StreamTSeq<T> shuffle(Random random) {
       
        return (StreamTSeq<T>)StreamT.super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#slice(long, long)
     */
    @Override
    public StreamTSeq<T> slice(long from, long to) {
       
        return (StreamTSeq<T>)StreamT.super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamT#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> StreamTSeq<T> sorted(Function<? super T, ? extends U> function) {
        return (StreamTSeq)StreamT.super.sorted(function);
    }
    @Override
    public int hashCode(){
        return run.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof StreamTSeq){
            return run.equals( ((StreamTSeq)o).run);
        }
        return false;
    }
}