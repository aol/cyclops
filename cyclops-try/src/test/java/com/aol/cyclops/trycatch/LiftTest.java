package com.aol.cyclops.trycatch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static com.aol.cyclops.lambda.api.AsAnyM.anyM;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.MonadFunctions;
import com.aol.cyclops.lambda.monads.Monads;


public class LiftTest {

	private Integer add(Integer a, Integer b){
		return a+b;
	}
	
	@Test
	public void testLift(){
		val add = Monads.liftM2(this::add);
		
		AnyM<Integer> result = add.apply(anyM(Try.of(2, RuntimeException.class)), anyM(Try.of(3,RuntimeException.class)));
		assertThat(result.<Try<Integer,RuntimeException>>unwrapMonad().get(),equalTo(5));
	}
	
	@Test
	public void testLiftError(){
		val divide = Monads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(2, ArithmeticException.class)), anyM(Try.of(0,ArithmeticException.class)));
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrapMonad().isFailure(),equalTo(true));
	}
	
	@Test
	public void testLiftErrorAndStream(){
		val divide = Monads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(20, ArithmeticException.class)), anyM(Stream.of(4,1,2,3,0)));
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrapMonad().isFailure(),equalTo(true));
	}
	
	@Test
	public void testLiftAndStream(){
		val divide = Monads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(2, ArithmeticException.class)), anyM(Stream.of(10,1,2,3)));
		
		assertThat(result.<Try<List<Integer>,ArithmeticException>>unwrapMonad().get(),equalTo(Arrays.asList(0, 2, 1, 0)));
	}
	
	@Test(expected=ArithmeticException.class)
	public void testLiftNoExceptionType(){
		val divide = Monads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(2)), anyM(Try.of(0)));
		System.out.println(result);
		fail("exception should be thrown");
	}
	
	
	private Integer divide(Integer a, Integer b){
		return a/b;
	}
}
