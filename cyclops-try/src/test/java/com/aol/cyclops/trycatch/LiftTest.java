package com.aol.cyclops.trycatch;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.AnyMonads;


public class LiftTest {

	private Integer add(Integer a, Integer b){
		return a+b;
	}
	
	@Test
	public void testLift(){
		val add =	AnyMonads.liftM2(this::add);
		
		AnyM<Integer> result = add.apply(anyM(Try.of(2, RuntimeException.class)), anyM(Try.of(3,RuntimeException.class)));
		assertThat(result.<Try<Integer,RuntimeException>>unwrap().get(),equalTo(5));
	}
	
	@Test
	public void testLiftError(){
		val divide = AnyMonads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(2, ArithmeticException.class)), anyM(Try.of(0,ArithmeticException.class)));
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrap().isFailure(),equalTo(true));
	}
	
	@Test
	public void testLiftErrorAndStream(){
		val divide = AnyMonads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(20, ArithmeticException.class)), anyM(Stream.of(4,1,2,3,0)));
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrap().isFailure(),equalTo(true));
	}
	
	@Test
	public void testLiftAndStream(){
		val divide = AnyMonads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(2, ArithmeticException.class)), anyM(Stream.of(10,1,2,3)));
		
		assertThat(result.<Try<List<Integer>,ArithmeticException>>unwrap().get(),equalTo(Arrays.asList(0, 2, 1, 0)));
	}
	
	@Test(expected=ArithmeticException.class)
	public void testLiftNoExceptionType(){
		val divide = AnyMonads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(2)), anyM(Try.of(0)));
		System.out.println(result);
		fail("exception should be thrown");
	}
	
	
	private Integer divide(Integer a, Integer b){
		return a/b;
	}
}
