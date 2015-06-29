package com.aol.cyclops.lambda.monads;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.api.AsGenericMonad;


public class Monads extends AsAnyM{

	/**
	 * Lift a function so it accepts a Monad and returns a Monad (native / unwrapped in Monad wrapper interface)
	 * 
	 * @param fn
	 * @return
	 */
	public static <U1,R1,U2,R2> Function<U2,R2> liftMNative(Function<U1,R1> fn){
		return u2 -> (R2)asMonad(u2).map( input -> fn.apply((U1)input)  ).unwrap();
	}
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
	public static <MONAD1,U,MONAD2,R> Function<Monad<MONAD1,U>,Monad<MONAD2,R>> liftMonad(Function<U,R> fn){
		return u -> (Monad)u.map( input -> fn.apply(input)  );
	}
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (native / unwrapped in Monad wrapper interface)
	 * 
	 * @param fn
	 * @return
	 */
	public static <U1,R1,U2,R2,U3,U4> BiFunction<U2,U3,R2> liftMNative2(BiFunction<U1,U4,R1> fn){
		return (u2,u3) -> (R2)asMonad(u2).bind( input1 ->  asMonad(u3).map(input2 -> fn.apply((U1)input1,(U4)input2)  ).unwrap()).unwrap();
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
	 *  Lift a function so it accepts Monads and returns a Monads. Allows Monad functionality to be 'injected' into plain methods or code.
	 *  
	 *  e.g. Add both looping error handling to a basic divide method 
	 *  <pre>
	 *  {@code 
	 *  
	 *  	val divide = Monads.liftM2(this::divide);
		
			AnyM<Integer> result = divide.apply(monad(Try.of(20, ArithmeticException.class)), monad(Stream.of(4,0,2,3)));
		
			assertThat(result.<Try<Integer,ArithmeticException>>unwrapMonad().isFailure(),equalTo(true));
	 *  
	 *  
	 *  	private Integer divide(Integer a, Integer b){
				return a/b;
			}
		}
	 *  </pre>
	 * 
	 * @param fn BiFunction to lift
	 * @return Lifted BiFunction
	 */
	public static <MONAD1,U1,MONAD2,U2,MONAD3,R> BiFunction<Monad<MONAD1,U1>,Monad<MONAD2,U2>,Monad<MONAD3,R>> liftMonad2(BiFunction<U1,U2,R> fn){
		return (u1,u2) -> (Monad)u1.bind( input1 -> u2.map(input2 -> fn.apply(input1,input2)  ).unwrap());
	}
	
	
	
	/**
	 * Convert a list of Monads to a Monad with a List
	 * 
	 * <pre>{@code
	 * List<CompletableFuture<Integer>> futures;

        
        CompletableFuture<List<Integer>> futureList = Monads.sequence(CompletableFuture.class, futures);

	  
	  }
	 * </pre>
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

        
        CompletableFuture<List<String>> futureList = Monads.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i);
        }
        </pre>

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

        
        AnyM<List<String>> futureList = Monads.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i).anyM();
        }
		</pre>
	 * 
	 * @param c Monad type to traverse
	 * @param seq List of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	public static <T,R> AnyM<List<R>> traverse(Class<?> c,List<T> seq, Function<?,R> fn){
		return asMonad(new ComprehenderSelector().selectComprehender(c).of(1))
								.flatMap(in-> asMonad(seq.stream()).flatMap(m-> m).flatMap((Function)fn).unwrap()
									).anyM();
	}

	
	/**
	 * Convert a list of Monads to a Monad with a List
	 * 
	 * <pre>{@code
	 * List<CompletableFuture<Integer>> futures;

       
        AnyM<List<Integer>> futureList = Monads.sequence(CompletableFuture.class, futures).anyM();

	   //where Simplex wraps  CompletableFuture<List<Integer>>
	  }</pre>
	 * 
	 * @param c The type of Monad to convert
	 * @param seq List of monads to convert
	 * @return Monad with a List
	 */ 
	public static <T,T1>  AnyM<List<T>> sequence(Class<?> c, List<T1> seq){
		return AsGenericMonad.asMonad(new ComprehenderSelector().selectComprehender(c).of(1))
				.flatMap(in-> asMonad(seq.stream()).flatMap(m-> m).unwrap()
							).anyM();
	}
}
