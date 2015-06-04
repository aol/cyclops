package com.aol.cyclops.functions;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.MonadFunctions;


public interface LiftMFunctions {
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * Simplex view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * @param fn
	 * @return
	 */
	public static <U,R> Function<AnyM<U>,AnyM<R>> liftM(Function<U,R> fn){
		return MonadFunctions.liftM(fn);
	}
	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * AnyM view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * Example of lifting an existing method to add error handling (Try Monad) and iteration (Stream monad)
	 * <pre>
	 * {@code
	 	//using Lombok val to simplify verbose generics
	    val divide = LiftMFunctions.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(2, ArithmeticException.class)), anyM(Stream.of(10,1,2,3)));
		
		assertThat(result.<Try<List<Integer>,ArithmeticException>>unwrapMonad().get(),equalTo(Arrays.asList(0, 2, 1, 0)));
	  
	  	private Integer divide(Integer a, Integer b){
			return a/b;
		}
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	static <U1,U2,R> BiFunction<AnyM<U1>,AnyM<U2>,AnyM<R>> liftM2(BiFunction<U1,U2,R> fn){
		return MonadFunctions.liftM2(fn);
	}
	
	static <U1,U2,U3,R> TriFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<R>> liftM3(TriFunction<U1,U2,U3,R> fn){
		return (u1,u2,u3) -> u1.bind( input1 -> 
									u2.bind(input2 -> 
										u3.map(input3->fn.apply(input1,input2,input3)  )).unwrap()).anyM();
	}
	
	static <U1,U2,U3,U4,R> QuadFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<R>> liftM4(QuadFunction<U1,U2,U3,U4,R> fn){
		
		return (u1,u2,u3,u4) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.map(input4->fn.apply(input1,input2,input3,input4)  ))).unwrap()).anyM();
	}
	static <U1,U2,U3,U4,U5,R> QuintFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<U5>,AnyM<R>> liftM5(QuintFunction<U1,U2,U3,U4,U5,R> fn){
		
		return (u1,u2,u3,u4,u5) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1,input2,input3,input4,input5)  )))).unwrap()).anyM();
	}
	
	static <U1,U2,R> Function<AnyM<U1>,Function<AnyM<U2>,AnyM<R>>> liftM2(Function<U1,Function<U2,R>> fn){
		return u1 -> u2 -> u1.bind( input1 -> u2.map(input2 -> fn.apply(input1).apply(input2)  ).unwrap()).anyM();

	}
	static <U1,U2,U3,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,AnyM<R>>>> liftM3(Function<U1,Function<U2,Function<U3,R>>> fn){
		return u1 -> u2 ->u3 -> u1.bind( input1 -> 
									u2.bind(input2 -> 
										u3.map(input3->fn.apply(input1).apply(input2).apply(input3)  )).unwrap()).anyM();
	}
	
	static <U1,U2,U3,U4,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,Function<AnyM<U4>,AnyM<R>>>>> liftM4(Function<U1,Function<U2,Function<U3,Function<U4,R>>>> fn){
		
		return u1->u2->u3->u4 -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.map(input4->fn.apply(input1).apply(input2).apply(input3).apply(input4)  ))).unwrap()).anyM();
	}
	static <U1,U2,U3,U4,U5,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,Function<AnyM<U4>,Function<AnyM<U5>,AnyM<R>>>>>> liftM5(Function<U1,Function<U2,Function<U3,Function<U4,Function<U5,R>>>>> fn){
		
		return u1 ->u2 ->u3 ->u4 ->u5  -> u1.bind( input1 -> 
										   u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1).apply(input2).apply(input3).apply(input4).apply(input5)  )))).unwrap()).anyM();
	}
}
