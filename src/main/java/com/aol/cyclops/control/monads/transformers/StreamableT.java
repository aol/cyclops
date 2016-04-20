package com.aol.cyclops.control.monads.transformers;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
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
import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.control.monads.transformers.values.FoldableTransformerSeq;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
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
public interface StreamableT<T>  extends  FoldableTransformerSeq<T>{
   
    public <R> StreamableT<R> unitIterator(Iterator<R> it);
    public <R> StreamableT<R> unit(R t);
    public <R> StreamableT<R> empty();
   public <B> StreamableT<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f);
   /**
	 * @return The wrapped AnyM
	 */
   AnyM<Streamable<T>> unwrap();
   
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
   public StreamableT<T> peek(Consumer<? super T> peek);
   
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
   public StreamableT<T> filter(Predicate<? super T> test);
   
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
   public <B> StreamableT<B> map(Function<? super T,? extends B> f);
   
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
   default <B> StreamableT<B> bind(Function<? super T,StreamableT<? extends B>> f){
	   return of(unwrap().map(stream-> stream.flatMap(a-> Streamable.fromStream(f.apply(a).unwrap().stream()))
			   							.<B>flatMap(a->a)));
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
   public static <U, R> Function<StreamableT<U>, StreamableT<R>> lift(Function<? super U,? extends R> fn) {
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
	public static <U1, U2, R> BiFunction<StreamableT<U1>, StreamableT<U2>, StreamableT<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an StreamableT from an AnyM that contains a monad type that contains type other than Streamable
	 * The values in the underlying monad will be mapped to Streamable<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Streamable
	 * @return StreamableT
	 */
   public static <A> StreamableT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Streamable::of));
   }
   public static <A> StreamableTValue<A> fromAnyMValue(AnyMValue<A> anyM){
       return StreamableTValue.fromAnyM(anyM);
   }
   public static <A> StreamableTSeq<A> fromAnyMSeq(AnyMSeq<A> anyM){
       return StreamableTSeq.fromAnyM(anyM);
   }
   public static <A> StreamableTSeq<A> fromIterable(Iterable<Streamable<A>> iterableOfStreamables){
       return StreamableTSeq.of(AnyM.fromIterable(iterableOfStreamables));
   }
   public static <A> StreamableTSeq<A> fromStream(Stream<Streamable<A>> streamOfStreamables){
       return StreamableTSeq.of(AnyM.fromStream(streamOfStreamables));
   }
   public static <A> StreamableTSeq<A> fromPublisher(Publisher<Streamable<A>> publisherOfStreamables){
       return StreamableTSeq.of(AnyM.fromPublisher(publisherOfStreamables));
   }
   public static <A,V extends MonadicValue<Streamable<A>>> StreamableTValue<A> fromValue(V monadicValue){
       return StreamableTValue.fromValue(monadicValue);
   }
   public static <A> StreamableTValue<A> fromOptional(Optional<Streamable<A>> optional){
       return StreamableTValue.of(AnyM.fromOptional(optional));
   }
   public static <A> StreamableTValue<A> fromFuture(CompletableFuture<Streamable<A>> future){
       return StreamableTValue.of(AnyM.fromCompletableFuture(future));
   }
   public static <A> StreamableTValue<A> fromIterableValue(Iterable<Streamable<A>> iterableOfStreamables){
       return StreamableTValue.of(AnyM.fromIterableValue(iterableOfStreamables));
   }
   /**
	 * Construct an StreamableT from an AnyM that wraps a monad containing  Streamables
	 * 
	 * @param monads AnyM that contains a monad wrapping an Streamable
	 * @return StreamableT
	 */
   public static <A> StreamableT<A> of(AnyM<Streamable<A>> monads){
       return Matchables.anyM(monads).visit(v-> StreamableTValue.of(v), s->StreamableTSeq.of(s));
   }
   /**
	 * Create a StreamableT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
   public static <A> StreamableT<A> fromStream(AnyM<Stream<A>> monads){
	   return of(monads.map(Streamable::fromStream));
   }
  
   public static<T>  StreamableTValue<T> emptyOptional() {
       return StreamableT.fromOptional(Optional.empty());
   }
   public static<T>  StreamableTSeq<T> emptyStreamable() {
       return StreamableT.fromIterable(Streamable.empty());
   }
   /* (non-Javadoc)
   * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
   */
  @Override
  default <U> StreamableT<U> cast(Class<? extends U> type) {
      return (StreamableT<U>)FoldableTransformerSeq.super.cast(type);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
   */
  @Override
  default <R> StreamableT<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
      return (StreamableT<R>)FoldableTransformerSeq.super.trampoline(mapper);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
   */
  @Override
  default <R> StreamableT<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
          Supplier<? extends R> otherwise) {
     return (StreamableT<R>)FoldableTransformerSeq.super.patternMatch(case1, otherwise);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
   */
  @Override
  default <U> StreamableT<U> ofType(Class<? extends U> type) {
      
      return (StreamableT<U>)FoldableTransformerSeq.super.ofType(type);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> filterNot(Predicate<? super T> fn) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.filterNot(fn);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Filterable#notNull()
   */
  @Override
  default StreamableT<T> notNull() {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.notNull();
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
   */
  @Override
  default StreamableT<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.combine(predicate, op);
  }
  
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycle(int)
   */
  @Override
  default StreamableT<T> cycle(int times) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.cycle(times);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycle(com.aol.cyclops.Monoid, int)
   */
  @Override
  default StreamableT<T> cycle(Monoid<T> m, int times) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.cycle(m, times);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycleWhile(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> cycleWhile(Predicate<? super T> predicate) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.cycleWhile(predicate);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycleUntil(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> cycleUntil(Predicate<? super T> predicate) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.cycleUntil(predicate);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip(java.lang.Iterable, java.util.function.BiFunction)
   */
  @Override
  default <U, R> StreamableT<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
     
      return (StreamableT<R>)FoldableTransformerSeq.super.zip(other, zipper);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zipStream(java.util.stream.Stream)
   */
  @Override
  default <U> StreamableT<Tuple2<T, U>> zipStream(Stream<? extends U> other) {
     
      return (StreamableT)FoldableTransformerSeq.super.zipStream(other);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip(org.jooq.lambda.Seq)
   */
  @Override
  default <U> StreamableT<Tuple2<T, U>> zip(Seq<? extends U> other) {
     
      return (StreamableT)FoldableTransformerSeq.super.zip(other);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip3(java.util.stream.Stream, java.util.stream.Stream)
   */
  @Override
  default <S, U> StreamableT<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
     
      return (StreamableT)FoldableTransformerSeq.super.zip3(second, third);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
   */
  @Override
  default <T2, T3, T4> StreamableT<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
          Stream<T4> fourth) {
     
      return (StreamableT<Tuple4<T, T2, T3, T4>>)FoldableTransformerSeq.super.zip4(second, third, fourth);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zipWithIndex()
   */
  @Override
  default StreamableT<Tuple2<T, Long>> zipWithIndex() {
     
      return (StreamableT<Tuple2<T, Long>>)FoldableTransformerSeq.super.zipWithIndex();
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sliding(int)
   */
  @Override
  default StreamableT<ListX<T>> sliding(int windowSize) {
     
      return (StreamableT<ListX<T>>)FoldableTransformerSeq.super.sliding(windowSize);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sliding(int, int)
   */
  @Override
  default StreamableT<ListX<T>> sliding(int windowSize, int increment) {
     
      return (StreamableT<ListX<T>>)FoldableTransformerSeq.super.sliding(windowSize, increment);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(int, java.util.function.Supplier)
   */
  @Override
  default <C extends Collection<? super T>> StreamableT<C> grouped(int size, Supplier<C> supplier) {
     
      return (StreamableT<C> )FoldableTransformerSeq.super.grouped(size, supplier);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedUntil(java.util.function.Predicate)
   */
  @Override
  default StreamableT<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
     
      return (StreamableT<ListX<T>>)FoldableTransformerSeq.super.groupedUntil(predicate);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedStatefullyWhile(java.util.function.BiPredicate)
   */
  @Override
  default StreamableT<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
     
      return (StreamableT<ListX<T>>)FoldableTransformerSeq.super.groupedStatefullyWhile(predicate);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedWhile(java.util.function.Predicate)
   */
  @Override
  default StreamableT<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
     
      return (StreamableT<ListX<T>>)FoldableTransformerSeq.super.groupedWhile(predicate);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
   */
  @Override
  default <C extends Collection<? super T>> StreamableT<C> groupedWhile(Predicate<? super T> predicate,
          Supplier<C> factory) {
     
      return (StreamableT<C>)FoldableTransformerSeq.super.groupedWhile(predicate, factory);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
   */
  @Override
  default <C extends Collection<? super T>> StreamableT<C> groupedUntil(Predicate<? super T> predicate,
          Supplier<C> factory) {
     
      return (StreamableT<C>)FoldableTransformerSeq.super.groupedUntil(predicate, factory);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(int)
   */
  @Override
  default StreamableT<ListX<T>> grouped(int groupSize) {
     
      return ( StreamableT<ListX<T>>)FoldableTransformerSeq.super.grouped(groupSize);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(java.util.function.Function, java.util.stream.Collector)
   */
  @Override
  default <K, A, D> StreamableT<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
          Collector<? super T, A, D> downstream) {
     
      return (StreamableT)FoldableTransformerSeq.super.grouped(classifier, downstream);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(java.util.function.Function)
   */
  @Override
  default <K> StreamableT<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
     
      return (StreamableT)FoldableTransformerSeq.super.grouped(classifier);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#distinct()
   */
  @Override
  default StreamableT<T> distinct() {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.distinct();
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanLeft(com.aol.cyclops.Monoid)
   */
  @Override
  default StreamableT<T> scanLeft(Monoid<T> monoid) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.scanLeft(monoid);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanLeft(java.lang.Object, java.util.function.BiFunction)
   */
  @Override
  default <U> StreamableT<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
     
      return (StreamableT<U>)FoldableTransformerSeq.super.scanLeft(seed, function);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanRight(com.aol.cyclops.Monoid)
   */
  @Override
  default StreamableT<T> scanRight(Monoid<T> monoid) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.scanRight(monoid);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanRight(java.lang.Object, java.util.function.BiFunction)
   */
  @Override
  default <U> StreamableT<U> scanRight(U identity, BiFunction<? super T, ? super U,? extends U> combiner) {
     
      return (StreamableT<U>)FoldableTransformerSeq.super.scanRight(identity, combiner);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted()
   */
  @Override
  default StreamableT<T> sorted() {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.sorted();
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted(java.util.Comparator)
   */
  @Override
  default StreamableT<T> sorted(Comparator<? super T> c) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.sorted(c);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeWhile(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> takeWhile(Predicate<? super T> p) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.takeWhile(p);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropWhile(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> dropWhile(Predicate<? super T> p) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.dropWhile(p);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeUntil(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> takeUntil(Predicate<? super T> p) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.takeUntil(p);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropUntil(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> dropUntil(Predicate<? super T> p) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.dropUntil(p);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropRight(int)
   */
  @Override
  default StreamableT<T> dropRight(int num) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.dropRight(num);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeRight(int)
   */
  @Override
  default StreamableT<T> takeRight(int num) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.takeRight(num);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skip(long)
   */
  @Override
  default StreamableT<T> skip(long num) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.skip(num);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipWhile(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> skipWhile(Predicate<? super T> p) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.skipWhile(p);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipUntil(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> skipUntil(Predicate<? super T> p) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.skipUntil(p);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limit(long)
   */
  @Override
  default StreamableT<T> limit(long num) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.limit(num);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitWhile(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> limitWhile(Predicate<? super T> p) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.limitWhile(p);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitUntil(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> limitUntil(Predicate<? super T> p) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.limitUntil(p);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#intersperse(java.lang.Object)
   */
  @Override
  default StreamableT<T> intersperse(T value) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.intersperse(value);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#reverse()
   */
  @Override
  default StreamableT<T> reverse() {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.reverse();
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#shuffle()
   */
  @Override
  default StreamableT<T> shuffle() {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.shuffle();
  }

  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipLast(int)
   */
  @Override
  default StreamableT<T> skipLast(int num) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.skipLast(num);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitLast(int)
   */
  @Override
  default StreamableT<T> limitLast(int num) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.limitLast(num);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmpty(java.lang.Object)
   */
  @Override
  default StreamableT<T> onEmpty(T value) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.onEmpty(value);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmptyGet(java.util.function.Supplier)
   */
  @Override
  default StreamableT<T> onEmptyGet(Supplier<? extends T> supplier) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.onEmptyGet(supplier);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmptyThrow(java.util.function.Supplier)
   */
  @Override
  default <X extends Throwable> StreamableT<T> onEmptyThrow(Supplier<? extends X> supplier) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.onEmptyThrow(supplier);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#shuffle(java.util.Random)
   */
  @Override
  default StreamableT<T> shuffle(Random random) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.shuffle(random);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#slice(long, long)
   */
  @Override
  default StreamableT<T> slice(long from, long to) {
     
      return (StreamableT<T>)FoldableTransformerSeq.super.slice(from, to);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted(java.util.function.Function)
   */
  @Override
  default <U extends Comparable<? super U>> StreamableT<T> sorted(Function<? super T, ? extends U> function) {
      return (StreamableT)FoldableTransformerSeq.super.sorted(function);
  }
}