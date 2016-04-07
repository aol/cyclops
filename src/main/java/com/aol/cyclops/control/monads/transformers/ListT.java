package com.aol.cyclops.control.monads.transformers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;



/**
 * Monad Transformer for Java Lists
 * 
 * ListT consists of an AnyM instance that in turns wraps anoter Monad type that contains an List
 * 
 * ListT<AnyM<*SOME_MONAD_TYPE*<List<T>>>>
 * 
 * ListT allows the deeply wrapped List to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public interface ListT<T>  extends Publisher<T>{
    public <R> ListT<R> unitIterator(Iterator<R> it);
    public <R> ListT<R> unit(R t);
    public <R> ListT<R> empty();
   
   /**
	 * @return The wrapped AnyM
	 */
   public AnyM<List<T>> unwrap();
   /**
	 * Peek at the current value of the List
	 * <pre>
	 * {@code 
	 *    ListT.of(AnyM.fromStream(Arrays.asList(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of List
	 * @return ListT with peek call
	 */
   public ListT<T> peek(Consumer<? super T> peek);
   
   /**
	 * Filter the wrapped List
	 * <pre>
	 * {@code 
	 *    ListT.of(AnyM.fromStream(Arrays.asList(10,11))
	 *             .filter(t->t!=10);
	 *             
	 *     //ListT<AnyM<Stream<List[11]>>>
	 * }
	 * </pre>
	 * @param test Predicate to filter the wrapped List
	 * @return ListT that applies the provided filter
	 */
   public ListT<T> filter(Predicate<? super T> test);
   
   /**
	 * Map the wrapped List
	 * 
	 * <pre>
	 * {@code 
	 *  ListT.of(AnyM.fromStream(Arrays.asList(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //ListT<AnyM<Stream<List[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped List
	 * @return ListT that applies the map function to the wrapped List
	 */
   public <B> ListT<B> map(Function<? super T,? extends B> f);
   public <B> ListT<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f);
   /**
	 * Flat Map the wrapped List
	  * <pre>
	 * {@code 
	 *  ListT.of(AnyM.fromStream(Arrays.asList(10))
	 *             .flatMap(t->List.empty();
	 *  
	 *  
	 *  //ListT<AnyM<Stream<List.empty>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return ListT that applies the flatMap function to the wrapped List
	 */
   default <B> ListT<B> bind(Function<? super T,ListT<B>> f){
	  
	   return of( unwrap().map(stream-> ReactiveSeq.fromList(stream).flatMap(a-> f.apply(a).unwrap().stream()).flatMap(a->a.stream())
			   .toList()));
   }
   /**
	 * Lift a function into one that accepts and returns an ListT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add list handling (via List) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	 * Function<Integer,Integer> add2 = i -> i+2;
		Function<ListT<Integer>, ListT<Integer>> optTAdd2 = ListT.lift(add2);
		
		Stream<Integer> nums = Stream.of(1,2);
		AnyM<Stream<Integer>> stream = AnyM.ofMonad(Arrays.asList(nums));
		
		List<Integer> results = optTAdd2.apply(ListT.fromStream(stream))
										.unwrap()
										.<Optional<List<Integer>>>unwrap().get();
		
		
		//Arrays.asList(3,4);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from List and another monad type
	 * @return Function that accepts and returns an ListT
	 */
   public static <U, R> Function<ListT<U>, ListT<R>> lift(Function<? super U, ? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
   }
   /**
	 * Lift a BiFunction into one that accepts and returns  ListTs
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add list handling (via List), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
	 * to an existing function
	 * 
	 * <pre>
	 * {@code 
	 *BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<ListT<Integer>,ListT<Integer>, ListT<Integer>> optTAdd2 = ListT.lift2(add);
		
		Streamable<Integer> threeValues = Streamable.of(1,2,3);
		AnyM<Integer> stream = AnyM.fromStreamable(threeValues);
		AnyM<List<Integer>> streamOpt = stream.map(Arrays::asList);
		
		CompletableFuture<List<Integer>> two = CompletableFuture.completedFuture(Arrays.asList(2));
		AnyM<List<Integer>> future=  AnyM.fromCompletableFuture(two);
		List<Integer> results = optTAdd2.apply(ListT.of(streamOpt),ListT.of(future))
										.unwrap()
										.<Stream<List<Integer>>>unwrap()
										.flatMap(i->i.stream())
										.collect(Collectors.toList());
			//Arrays.asList(3,4);							
	  }
	  </pre>
	 * @param fn BiFunction to enhance with functionality from List and another monad type
	 * @return Function that accepts and returns an ListT
	 */
	public static <U1, U2, R> BiFunction<ListT<U1>, ListT<U2>, ListT<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an ListT from an AnyM that contains a monad type that contains type other than List
	 * The values in the underlying monad will be mapped to List<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an List
	 * @return ListT
	 */
   public static <A> ListT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Arrays::asList));
   }
   /**
	 * Construct an ListT from an AnyM that wraps a monad containing  Lists
	 * 
	 * @param monads AnyM that contains a monad wrapping an List
	 * @return ListT
	 */
   public static <A> ListT<A> of(AnyM<List<A>> monads){
       return Matchables.anyM(monads).visit(v-> ListTValue.of(v), s->ListTSeq.of(s));
   }

	/**
	 * Create a ListT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
	public static <A> ListT<A> fromStreamAnyM(AnyM<Stream<A>> monads) {
		return of(monads.map(s -> s.collect(Collectors.toList())));
	}
   
 
    public static <A> ListTValue<A> fromAnyMValue(AnyMValue<A> anyM) {
        return ListTValue.fromAnyM(anyM);
    }

    public static <A> ListTSeq<A> fromAnyMSeq(AnyMSeq<A> anyM) {
        return ListTSeq.fromAnyM(anyM);
    }

    public static <A> ListTSeq<A> fromIterable(
            Iterable<List<A>> iterableOfLists) {
        return ListTSeq.of(AnyM.fromIterable(iterableOfLists));
    }

    public static <A> ListTSeq<A> fromStream(Stream<? extends List<A>> streamOfLists) {
        return ListTSeq.of(AnyM.fromStream(streamOfLists));
    }

    public static <A> ListTSeq<A> fromPublisher(
            Publisher<List<A>> publisherOfLists) {
        return ListTSeq.of(AnyM.fromPublisher(publisherOfLists));
    }

    public static <A, V extends MonadicValue<List<A>>> ListTValue<A> fromValue(
            V monadicValue) {
        return ListTValue.fromValue(monadicValue);
    }

    public static <A> ListTValue<A> fromOptional(Optional<List<A>> optional) {
        return ListTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> ListTValue<A> fromFuture(CompletableFuture<List<A>> future) {
        return ListTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> ListTValue<A> fromIterablListue(
            Iterable<List<A>> iterableOfLists) {
        return ListTValue.of(AnyM.fromIterableValue(iterableOfLists));
    }
 
}