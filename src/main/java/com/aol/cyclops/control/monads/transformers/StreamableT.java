package com.aol.cyclops.control.monads.transformers;

import java.util.Iterator;
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
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.control.monads.transformers.values.TransformerSeq;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.ConvertableSequence;
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
public interface StreamableT<T>  extends  ConvertableSequence<T>, 
                                          TransformerSeq<T>,  
                                          Publisher<T>{
   
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
  default <U> StreamableT<U> cast(Class<U> type) {
      return (StreamableT<U>)TransformerSeq.super.cast(type);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
   */
  @Override
  default <R> StreamableT<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
      return (StreamableT<R>)TransformerSeq.super.trampoline(mapper);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
   */
  @Override
  default <R> StreamableT<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
          Supplier<? extends R> otherwise) {
     return (StreamableT<R>)TransformerSeq.super.patternMatch(case1, otherwise);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
   */
  @Override
  default <U> StreamableT<U> ofType(Class<U> type) {
      
      return (StreamableT<U>)TransformerSeq.super.ofType(type);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
   */
  @Override
  default StreamableT<T> filterNot(Predicate<? super T> fn) {
     
      return (StreamableT<T>)TransformerSeq.super.filterNot(fn);
  }
  /* (non-Javadoc)
   * @see com.aol.cyclops.types.Filterable#notNull()
   */
  @Override
  default StreamableT<T> notNull() {
     
      return (StreamableT<T>)TransformerSeq.super.notNull();
  }
}