package com.aol.cyclops.trycatch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.monad.AnyMonads;


public class LiftTest {

	private Integer add(Integer a, Integer b){
		return a+b;
	}
	
	@Test
	public void testLift(){
		val add =	AnyMonads.liftM2(this::add);
		
		AnyM<Integer> result = add.apply(AnyM.fromIterable(Try.of(2, RuntimeException.class)), AnyM.fromIterable(Try.of(3,RuntimeException.class)));
		assertThat(result.<Try<Integer,RuntimeException>>unwrap().get(),equalTo(5));
	}
	
	@Test
	public void testLiftError(){
		val divide = AnyMonads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(AnyM.fromIterable(Try.of(2, ArithmeticException.class)),AnyM.fromIterable(Try.of(0,ArithmeticException.class)));
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrap().isFailure(),equalTo(true));
	}
	
	@Test
	public void testLiftErrorAndStream(){
		val divide = AnyMonads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(AnyM.fromIterable(Try.of(20, ArithmeticException.class)), AnyM.fromStream(Stream.of(4,1,2,3,0)));
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrap().isFailure(),equalTo(true));
	}
	
	@Test
	public void testLiftAndStream(){
		
		val divide = AnyMonads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(AnyM.fromIterable(Try.of(2, ArithmeticException.class)), AnyM.fromStream(Stream.of(10,1,2,3)));
		
		assertThat(result.<Try<List<Integer>,ArithmeticException>>unwrap().get(),equalTo(Arrays.asList(0, 2, 1, 0)));
		
	}
	
	@Test(expected=ArithmeticException.class)
	public void testLiftNoExceptionType(){
		val divide = AnyMonads.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(AnyM.fromIterable(Try.of(2)),AnyM.fromIterable(Try.of(0)));
		System.out.println(result);
		fail("exception should be thrown");
	}
	
	
	private Integer divide(Integer a, Integer b){
		return a/b;
	}
}
