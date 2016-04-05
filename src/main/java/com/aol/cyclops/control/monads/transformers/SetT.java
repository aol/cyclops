package com.aol.cyclops.control.monads.transformers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Set;
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
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.control.monads.transformers.values.SetTValue;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;



/**
 * Monad Transformer for Java Sets
 * 
 * SetT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Set
 * 
 * SetT<AnyM<*SOME_MONAD_TYPE*<Set<T>>>>
 * 
 * SetT allows the deeply wrapped Set to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public interface SetT<T> {
   
   
   /**
	 * @return The wrapped AnyM
	 */
   AnyM<Set<T>> unwrap();
   public <B> SetT<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f);
   /**
	 * Peek at the current value of the Set
	 * <pre>
	 * {@code 
	 *    SetT.of(AnyM.fromStream(Arrays.asSet(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of Set
	 * @return SetT with peek call
	 */
   public SetT<T> peek(Consumer<? super T> peek);
   
   /**
	 * Filter the wrapped Set
	 * <pre>
	 * {@code 
	 *    SetT.of(AnyM.fromStream(Arrays.asSet(10,11))
	 *             .filter(t->t!=10);
	 *             
	 *     //SetT<AnyM<Stream<Set[11]>>>
	 * }
	 * </pre>
	 * @param test Predicate to filter the wrapped Set
	 * @return SetT that applies the provided filter
	 */
   public SetT<T> filter(Predicate<? super T> test);
   /**
	 * Map the wrapped Set
	 * 
	 * <pre>
	 * {@code 
	 *  SetT.of(AnyM.fromStream(Arrays.asSet(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //SetT<AnyM<Stream<Set[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped Set
	 * @return SetT that applies the map function to the wrapped Set
	 */
   public <B> SetT<B> map(Function<? super T,? extends B> f);
   
   /**
	 * Flat Map the wrapped Set
	  * <pre>
	 * {@code 
	 *  SetT.of(AnyM.fromStream(Arrays.asSet(10))
	 *             .flatMap(t->Set.empty();
	 *  
	 *  
	 *  //SetT<AnyM<Stream<Set.empty>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return SetT that applies the flatMap function to the wrapped Set
	 */
   default <B> SetT<B> bind(Function<? super T,SetT<B>> f){
	  
	   return of( unwrap().map(stream-> ReactiveSeq.fromIterable(stream).flatMap(a-> f.apply(a).unwrap().stream()).flatMap(a->a.stream())
			   .toSet()));
   }
   /**
	 * Lift a function into one that accepts and returns an SetT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add list handling (via Set) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	 * Function<Integer,Integer> add2 = i -> i+2;
		Function<SetT<Integer>, SetT<Integer>> optTAdd2 = SetT.lift(add2);
		
		Stream<Integer> nums = Stream.of(1,2);
		AnyM<Stream<Integer>> stream = AnyM.ofMonad(asSet(nums));
		
		Set<Integer> results = optTAdd2.apply(SetT.fromStream(stream))
										.unwrap()
										.<Optional<Set<Integer>>>unwrap().get();
		
		
		//asSet(3,4);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from Set and another monad type
	 * @return Function that accepts and returns an SetT
	 */
   public static <U, R> Function<SetT<U>, SetT<R>> lift(Function<? super U,? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
   }
   /**
	 * Lift a BiFunction into one that accepts and returns  SetTs
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add list handling (via Set), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
	 * to an existing function
	 * 
	 * <pre>
	 * {@code 
	 *BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<SetT<Integer>,SetT<Integer>, SetT<Integer>> optTAdd2 = SetT.lift2(add);
		
		Streamable<Integer> threeValues = Streamable.of(1,2,3);
		AnyM<Integer> stream = AnyM.fromStreamable(threeValues);
		AnyM<Set<Integer>> streamOpt = stream.map(this::asSet);
		
		CompletableFuture<Set<Integer>> two = CompletableFuture.completedFuture(asSet(2));
		AnyM<Set<Integer>> future=  AnyM.fromCompletableFuture(two);
		Set<Integer> results = optTAdd2.apply(SetT.of(streamOpt),SetT.of(future))
										.unwrap()
										.<Stream<Set<Integer>>>unwrap()
										.flatMap(i->i.stream())
										.collect(Collectors.toSet());
			//asSet(3,4);							
	  }
	  </pre>
	 * @param fn BiFunction to enhance with functionality from Set and another monad type
	 * @return Function that accepts and returns an SetT
	 */
	public static <U1, U2, R> BiFunction<SetT<U1>, SetT<U2>, SetT<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an SetT from an AnyM that contains a monad type that contains type other than Set
	 * The values in the underlying monad will be mapped to Set<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Set
	 * @return SetT
	 */
   public static <A> SetT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(SetT::asSet));
   }
   
  static <T> Set<T> asSet(T... elements){
	   	return new HashSet<T>(Arrays.asList(elements));
   }
   /**
	 * Construct an SetT from an AnyM that wraps a monad containing  Sets
	 * 
	 * @param monads AnyM that contains a monad wrapping an Set
	 * @return SetT
	 */
   public static <A> SetT<A> of(AnyM<Set<A>> monads){
       return Matchables.anyM(monads).visit(v-> SetTValue.of(v), s->SetTSeq.of(s));
   }

	/**
	 * Create a SetT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
	public static <A> SetT<A> fromStream(AnyM<Stream<A>> monads) {
		return of(monads.map(s -> s.collect(Collectors.toSet())));
	}
   
    
    public static <A> SetTValue<A> fromAnyMValue(AnyMValue<A> anyM) {
        return SetTValue.fromAnyM(anyM);
    }

    public static <A> SetTSeq<A> fromAnyMSeq(AnyMSeq<A> anyM) {
        return SetTSeq.fromAnyM(anyM);
    }

    public static <A> SetTSeq<A> fromIterable(
            Iterable<Set<A>> iterableOfSets) {
        return SetTSeq.of(AnyM.fromIterable(iterableOfSets));
    }

    public static <A> SetTSeq<A> fromStream(Stream<Set<A>> streamOfSets) {
        return SetTSeq.of(AnyM.fromStream(streamOfSets));
    }

    public static <A> SetTSeq<A> fromPublisher(
            Publisher<Set<A>> publisherOfSets) {
        return SetTSeq.of(AnyM.fromPublisher(publisherOfSets));
    }

    public static <A, V extends MonadicValue<Set<A>>> SetTValue<A> fromValue(
            V monadicValue) {
        return SetTValue.fromValue(monadicValue);
    }

    public static <A> SetTValue<A> fromOptional(Optional<Set<A>> optional) {
        return SetTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> SetTValue<A> fromFuture(CompletableFuture<Set<A>> future) {
        return SetTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> SetTValue<A> fromIterableValue(
            Iterable<Set<A>> iterableOfSets) {
        return SetTValue.of(AnyM.fromIterableValue(iterableOfSets));
    }

   
 
}