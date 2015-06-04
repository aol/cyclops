package com.aol.cyclops.lambda.monads;


import static com.aol.cyclops.lambda.api.AsGenericMonad.asMonad;
import static com.aol.cyclops.lambda.api.AsGenericMonad.monad;


import java.util.List;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.AsGenericMonad;
import com.aol.cyclops.lambda.api.AsAnyM;

import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.streams.Pair;

public interface MonadFunctions<MONAD,T> extends AsGenericMonad,AsAnyM{
	public <R> Monad<MONAD,T> bind(Function<T,R> fn);
	public Stream<T> stream();
	public <MONAD,T> MONAD unit(T value);
	public Monad<Stream<T>,T> cycle(int times);
	//Optional(1) Optional (a+2) = Optional(3)
	/**
	 * Apply function/s inside supplied Monad to data in current Monad
	 * 
	 * e.g. with Streams
	 * <pre>{@code 
	 * 
	 * Simplex<Integer> applied =monad(Stream.of(1,2,3)).applyM(monad(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2))).simplex();
	
	 	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 }
	 * 
	 * with Optionals 
	 * <pre>{@code
	 * 
	 *  Simplex<Integer> applied =monad(Optional.of(2)).applyM(monad(Optional.of( (Integer a)->a+1)) ).simplex();
		assertThat(applied.toList(),equalTo(Arrays.asList(3)));}
	 * 
	 * @param fn
	 * @return
	 */
	default <NT,R> Monad<NT,R> applyM(Monad<?,Function<T,R>> fn){
		return (Monad)this.bind(v-> fn.map(innerFn -> innerFn.apply(v))
							.unwrap());
		
	}
	/**
	 * Filter current monad by each element in supplied Monad
	 * 
	 * e.g.
	 * 
	 * <pre>{@code
	 *  Simplex<Stream<Integer>> applied = monad(Stream.of(1,2,3))
	 *    									.filterM(monad(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3)))
	 *    									.simplex();
	 * 
	 * //results in Stream.of(Stream.of(1),Stream.of(2),Stream.of(())
	 * }
	 * 
	 * @param fn
	 * @return
	 */
	default <NT,R> Monad<NT,R> simpleFilter(Monad<?,Predicate<T>> fn){
		return  (Monad)this.bind(v-> fn.map(innerFn -> new Pair(v,innerFn.test(v)))
													.filter(p->(boolean)p._2())
													.map(Pair::_1))
													.map(m -> ((Monad) m).unwrap());
		
		
	
	//	filterM((a: Int) => List(a > 2, a % 2 == 0), List(1, 2, 3), ListMonad),
	//List(List(3), Nil, List(2, 3), List(2), List(3),
	//	  Nil, List(2, 3), List(2))												
	}
	/**
	 * 
	 * Replicate given Monad
	 * 
	 * <pre>{@code 
	 * 	
	 *   Simplex<Optional<Integer>> applied =monad(Optional.of(2)).replicateM(5).simplex();
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
		 
		 }
	 * 
	 * 
	 * @param times number of times to replicate
	 * @return Replicated Monad
	 */
	default <NT,R> Monad<NT,R> replicateM(int times){
		
		return (Monad)asMonad (unit(1))
						.flatten()
						.bind(v-> cycle(times).unwrap());		
	}
	/**
	 * Perform a reduction where NT is a (native) Monad type
	 * e.g. 
	 * <pre>{@code 
	 * Monoid<Optional<Integer>> optionalAdd = Monoid.of(Optional.of(0), (a,b)-> Optional.of(a.get()+b.get()));
		
		assertThat(monad(Stream.of(2,8,3,1)).reduceM(optionalAdd).unwrap(),equalTo(Optional.of(14)));
		}
	 * 
	 * 
	 * @param reducer
	 * @return
	 */
	default <NT,R> Monad<NT,R> reduceM(Monoid<NT> reducer){
	//	List(2, 8, 3, 1).foldLeftM(0) {binSmalls} -> Optional(14)
	//	convert to list Optionals
		
		return asMonad(monad(stream()).map(value ->new ComprehenderSelector()
							.selectComprehender(reducer.zero().getClass()).of(value))
							.reduce((Monoid)reducer));		
	}
	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (native / unwrapped in Monad wrapper interface)
	 * 
	 * @param fn
	 * @return
	 */
	static <U1,R1,U2,R2> Function<U2,R2> liftMNative(Function<U1,R1> fn){
		return u2 -> (R2)asMonad(u2).map( input -> fn.apply((U1)input)  ).unwrap();
	}
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * Simplex view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * @param fn
	 * @return
	 */
	static <U,R> Function<AnyM<U>,AnyM<R>> liftM(Function<U,R> fn){
		return u -> u.map( input -> fn.apply(input)  ).anyM();
	}
	static <MONAD1,U,MONAD2,R> Function<Monad<MONAD1,U>,Monad<MONAD2,R>> liftMonad(Function<U,R> fn){
		return u -> (Monad)u.map( input -> fn.apply(input)  );
	}
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (native / unwrapped in Monad wrapper interface)
	 * 
	 * @param fn
	 * @return
	 */
	static <U1,R1,U2,R2,U3,U4> BiFunction<U2,U3,R2> liftMNative2(BiFunction<U1,U4,R1> fn){
		return (u2,u3) -> (R2)asMonad(u2).bind( input1 ->  asMonad(u3).map(input2 -> fn.apply((U1)input1,(U4)input2)  ).unwrap()).unwrap();
	}
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * Simplex view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * @param fn
	 * @return
	 */
	static <U1,U2,R> BiFunction<AnyM<U1>,AnyM<U2>,AnyM<R>> liftM2(BiFunction<U1,U2,R> fn){
		return (u1,u2) -> u1.bind( input1 -> u2.map(input2 -> fn.apply(input1,input2)  ).unwrap()).anyM();
	}
	static <MONAD1,U1,MONAD2,U2,MONAD3,R> BiFunction<Monad<MONAD1,U1>,Monad<MONAD2,U2>,Monad<MONAD3,R>> liftMonad2(BiFunction<U1,U2,R> fn){
		return (u1,u2) -> (Monad)u1.bind( input1 -> u2.map(input2 -> fn.apply(input1,input2)  ).unwrap());
	}
	
	
	
	/**
	 * Convert a list of Monads to a Monad with a List
	 * 
	 * <pre>{@code
	 * List<CompletableFuture<Integer>> futures;

        
        CompletableFuture<List<Integer>> futureList = Monad.sequence(CompletableFuture.class, futures);

	  
	  }
	 * 
	 * @param c The type of Monad to convert
	 * @param seq List of monads to convert
	 * @return Monad with a List
	 */ 	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <MONAD,MONAD_LIST> MONAD_LIST sequenceNative(Class c,List<MONAD> seq){
		return (MONAD_LIST)AsGenericMonad.asMonad(new ComprehenderSelector().selectComprehender(c).of(1))
								.flatMap(in-> 
											AsGenericMonad.asMonad(seq.stream()).flatMap(m-> m).unwrap()
											).unwrap();
	}
	/**
	 * Convert a list of Monads to a Monad with a List applying the supplied function in the process
	 * 
	 * <pre>{@code 
	 *    List<CompletableFuture<Integer>> futures;

        
        CompletableFuture<List<String>> futureList = Monad.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i);
        }

	 * 
	 * @param c Monad type to traverse
	 * @param seq List of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <MONAD,MONAD_LIST,R> MONAD_LIST traverseNative(Class c,List<MONAD> seq, Function<?,R> fn){
		return (MONAD_LIST)AsGenericMonad.asMonad(new ComprehenderSelector().selectComprehender(c).of(1))
								.flatMap(in-> 
											AsGenericMonad.asMonad(seq.stream()).flatMap(m-> m).flatMap((Function)fn).unwrap()
											).unwrap();
	}
	
	/**
	 * Convert a list of Monads to a Monad with a List applying the supplied function in the process
	 * 
	 * <pre>{@code 
	 *    List<CompletableFuture<Integer>> futures;

        
        Simplex<List<String>> futureList = Monad.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i).simplex();
        }

	 * 
	 * @param c Monad type to traverse
	 * @param seq List of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	public static <MONAD,R> Monad<MONAD,List<R>> traverse(Class c,List<?> seq, Function<?,R> fn){
		return (Monad)asMonad(new ComprehenderSelector().selectComprehender(c).of(1))
								.flatMap(in-> asMonad(seq.stream()).flatMap(m-> m).flatMap((Function)fn).unwrap()
									);
	}

	
	/**
	 * Convert a list of Monads to a Monad with a List
	 * 
	 * <pre>{@code
	 * List<CompletableFuture<Integer>> futures;

       
        Simplex<List<Integer>> futureList = Monad.sequence(CompletableFuture.class, futures).simplex();

	   //where Simplex wraps  CompletableFuture<List<Integer>>
	  }
	 * 
	 * @param c The type of Monad to convert
	 * @param seq List of monads to convert
	 * @return Monad with a List
	 */ 
	public static <MONAD,T>  Monad<MONAD,T> sequence(Class c, List<?> seq){
		return (Monad)AsGenericMonad.asMonad(new ComprehenderSelector().selectComprehender(c).of(1))
				.flatMap(in-> asMonad(seq.stream()).flatMap(m-> m).unwrap()
							);
	}
}
