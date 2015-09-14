package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.internal.AsGenericMonad.asMonad;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.internal.AsGenericMonad;
import com.aol.cyclops.lambda.api.AsAnyMList;
import com.aol.cyclops.monad.AnyM;

public class AnyMonadsFunctions extends AsAnyMList{
	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * Simplex view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * @param fn
	 * @return
	 */
	public static <U,R> Function<AnyM<U>,AnyM<R>> liftM(Function<U,R> fn){
		return u -> u.map( input -> fn.apply(input)  );
	}
	
	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * AnyM view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * e.g.
	 * 
	 * <pre>{@code
	 * 	BiFunction<AnyM<Integer>,AnyM<Integer>,AnyM<Integer>> add = Monads.liftM2(this::add);
	 *   
	 *  Optional<Integer> result = add.apply(getBase(),getIncrease());
	 *  
	 *   private Integer add(Integer a, Integer b){
				return a+b;
			}
	 * }</pre>
	 * The add method has no null handling, but we can lift the method to Monadic form, and use Optionals to automatically handle null / empty value cases.
	 * 
	 * 
	 * @param fn BiFunction to lift
	 * @return Lifted BiFunction
	 */
	public static <U1,U2,R> BiFunction<AnyM<U1>,AnyM<U2>,AnyM<R>> liftM2(BiFunction<U1,U2,R> fn){
		
		return (u1,u2) -> u1.bind( input1 -> u2.map(input2 -> fn.apply(input1,input2)  ).unwrap());
	}
	
	
	

	/**
	 * Convert a list of Monads to a Monad with a List applying the supplied function in the process
	 * 
	 * <pre>{@code 
	 *    List<CompletableFuture<Integer>> futures;

        
        AnyM<List<String>> futureList = Monads.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i).anyM();
        }
		</pre>
	 * 
	 * @param seq List of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	public static <T,R> AnyM<List<R>> traverse(Collection<AnyM<T>> seq, Function<T,R> fn){
		if(seq.size()==0)
			return anyM(Optional.empty());
		return asMonad(new ComprehenderSelector().selectComprehender(seq.iterator().next().unwrap().getClass()).of(1))
								.flatMap(in-> asMonad(seq.stream().map(it->it.unwrap())).flatten().flatMap((Function)fn).unwrap()
									).anyM();
	}
	public static <T,R> AnyM<List<R>> traverse(Stream<AnyM<T>> seq, Function<T,R> fn){
		
		return asMonad(Stream.of(1))
								.flatMap(in-> asMonad(seq).flatten().flatMap((Function)fn).unwrap()
									).anyM();
	}

	
	/**
	 * Convert a list of Monads to a Monad with a List
	 * 
	 * <pre>{@code
	 * List<CompletableFuture<Integer>> futures;
	 * List<AnyM<Integer>> anyMs = anyMList(futures);

       
        AnyM<List<Integer>> futureList = Monads.sequence(CompletableFuture.class, anyMs).anyM();

	   //where Simplex wraps  CompletableFuture<List<Integer>>
	  }</pre>
	 * 
	 * @see com.aol.cyclops.lambda.api.AsAnyMList for helper methods to convert a List of Monads / Collections to List of AnyM
	 * @param seq List of monads to convert
	 * @return Monad with a List
	 */ 
	public static <T1>  AnyM<Stream<T1>> sequence(Collection<AnyM<T1>> seq){
	
		if(seq.size()==0)
			return anyM(Optional.empty());
		else
			return asMonad(new ComprehenderSelector().selectComprehender(seq.iterator().next().unwrap().getClass()).of(1))
				.flatMap(in-> AsGenericMonad.asMonad(seq.stream().map(it->it.unwrap())).flatten().unwrap()).anyM();
	}
	public static <T1>  AnyM<Stream<T1>> sequence(Stream<AnyM<T1>> seq){
			return AsGenericMonad.asMonad(Stream.of(1))
										.flatMap(in-> AsGenericMonad.asMonad(seq.map(it->it.unwrap()))
												.flatten().unwrap())
												.anyM();
	}
	
	
}
