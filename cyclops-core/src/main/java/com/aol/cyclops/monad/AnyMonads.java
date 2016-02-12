package com.aol.cyclops.monad;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.lambda.monads.ComprehenderSelector;
import com.aol.cyclops.lambda.monads.MonadWrapper;
import com.aol.cyclops.sequence.SequenceM;


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
	public  <T,R> AnyM<ListX<R>> traverse(Collection<AnyM<T>> seq, Function<? super T,? extends R> fn){
		if(seq.size()==0)
			return AnyM.ofMonad(Optional.empty());
		return new MonadWrapper<>(comprehender2(seq).of(1))
								.flatMap(in-> new MonadWrapper<>(seq.stream().map(it->it.unwrap())).flatten().flatMap((Function)fn).unwrap()
									).anyM();
	}
	private <T> Comprehender<T> comprehender2(Collection<AnyM<T>> seq) {
		return new ComprehenderSelector().selectComprehender(seq.iterator().next().unwrap().getClass());
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
	public  <T,R> AnyM<ListX<R>> traverse(Stream<AnyM<T>> seq, Function<? super T,? extends R> fn){
		
		return new MonadWrapper<>(Stream.of(1))
								.flatMap(in-> new MonadWrapper<>(seq).flatten().flatMap((Function)fn).unwrap()
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
	public  <T1>  AnyM<ListX<T1>> sequence(Collection<AnyM<T1>> seq){
		if(seq.size()==0)
			return AnyM.ofMonad(Optional.empty());
		else
			return new MonadWrapper<>(comprehender(seq).of(1))
				.flatMap(in-> new MonadWrapper<>(seq.stream().map(it->it.unwrap())).flatten().unwrap()).anyM();
	}
	private <T1> Comprehender comprehender(Collection<AnyM<T1>> seq) {
		return new ComprehenderSelector().selectComprehender(seq.iterator().next().unwrap().getClass());
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
	public  <T1>  AnyM<SequenceM<T1>> sequence(Stream<AnyM<T1>> seq){
			return new MonadWrapper<>(Stream.of(1))
										.flatMap(in-> new MonadWrapper<>(seq.map(it->it.unwrap()))
												.flatten().unwrap())
												.anyM();
	}
	
	
}
