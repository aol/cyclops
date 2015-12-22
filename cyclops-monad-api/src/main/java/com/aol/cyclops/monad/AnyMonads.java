package com.aol.cyclops.monad;

import static com.aol.cyclops.internal.AsGenericMonad.asMonad;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.internal.AsGenericMonad;
import com.aol.cyclops.lambda.monads.ComprehenderSelector;
import com.aol.cyclops.monad.AnyM;


public class AnyMonads implements AnyMFunctions{
	
	
	
	

	/**
	 * Convert a Collection of Monads to a Monad with a List applying the supplied function in the process
	 * 
	 * <pre>
	 * {@code 
       List<CompletableFuture<Integer>> futures = createFutures();
       AnyM<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
		</pre>
	 * 
	 * @param seq Collection of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	public  <T,R> AnyM<List<R>> traverse(Collection<AnyM<T>> seq, Function<T,R> fn){
		if(seq.size()==0)
			return AnyM.ofMonad(Optional.empty());
		return asMonad(new ComprehenderSelector().selectComprehender(seq.iterator().next().unwrap().getClass()).of(1))
								.flatMap(in-> asMonad(seq.stream().map(it->it.unwrap())).flatten().flatMap((Function)fn).unwrap()
									).anyM();
	}
	/**
	 * Convert a Stream of Monads to a Monad with a List applying the supplied function in the process
	 * 
	<pre>{@code 
       Stream<CompletableFuture<Integer>> futures = createFutures();
       AnyM<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
		</pre>
	 * 
	 * @param seq Stream of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	public  <T,R> AnyM<List<R>> traverse(Stream<AnyM<T>> seq, Function<T,R> fn){
		
		return asMonad(Stream.of(1))
								.flatMap(in-> asMonad(seq).flatten().flatMap((Function)fn).unwrap()
									).anyM();
	}

	
	/**
	 * Convert a Collection of Monads to a Monad with a List
	 * 
	 * <pre>
	 * {@code
		List<CompletableFuture<Integer>> futures = createFutures();
		AnyM<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));

	   //where AnyM wraps  CompletableFuture<List<Integer>>
	  }</pre>
	 * 
	 * @see com.aol.cyclops.monad.AsAnyMList for helper methods to convert a List of Monads / Collections to List of AnyM
	 * @param seq Collection of monads to convert
	 * @return Monad with a List
	 */ 
	public  <T1>  AnyM<Stream<T1>> sequence(Collection<AnyM<T1>> seq){
		if(seq.size()==0)
			return AnyM.ofMonad(Optional.empty());
		else
			return asMonad(new ComprehenderSelector().selectComprehender(seq.iterator().next().unwrap().getClass()).of(1))
				.flatMap(in-> AsGenericMonad.asMonad(seq.stream().map(it->it.unwrap())).flatten().unwrap()).anyM();
	}
	/**
	 * Convert a Stream of Monads to a Monad with a List
	 * 
	 * <pre>{@code
		Stream<CompletableFuture<Integer>> futures = createFutures();
		AnyM<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));

	   //where AnyM wraps  CompletableFuture<List<Integer>>
	  }</pre>
	 * 
	 * @see com.aol.cyclops.monad.AsAnyMList for helper methods to convert a List of Monads / Collections to List of AnyM
	 * @param seq Stream of monads to convert
	 * @return Monad with a List
	 */
	public  <T1>  AnyM<Stream<T1>> sequence(Stream<AnyM<T1>> seq){
			return AsGenericMonad.asMonad(Stream.of(1))
										.flatMap(in-> AsGenericMonad.asMonad(seq.map(it->it.unwrap()))
												.flatten().unwrap())
												.anyM();
	}
	
	
}
