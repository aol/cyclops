package com.aol.cyclops2.trycatch;

import cyclops.monads.Witness.*;

import static cyclops.monads.AnyM.success;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import cyclops.monads.function.AnyMFunction2;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.control.Try;


public class LiftTest {

	private Integer add(Integer a, Integer b){
		return a+b;
	}
	
	@Test
	public void testLift(){
		AnyMFunction2<tryType,Integer,Integer,Integer> add =	AnyM.liftF2(this::add);
		
		AnyM<tryType,Integer> result = add.apply(success(2), success(3));
		assertThat(result.<Try<Integer,RuntimeException>>unwrap().get(),equalTo(5));
	}
	
	@Test
	public void testLiftError(){
		AnyMFunction2<tryType,Integer,Integer,Integer> divide = AnyM.liftF2(this::divide);
		
		AnyM<tryType,Integer> result = divide.apply(success(2, ArithmeticException.class),Try.success(0,ArithmeticException.class).anyM());
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrap().isFailure(),equalTo(true));
	}
	
	@Test
	public void testLiftErrorAndStream(){
		AnyMFunction2<tryType,Integer,Integer,Integer> divide = AnyM.liftF2(this::divide);
		
		AnyM<tryType,Integer> result = divide.apply(success(20, ArithmeticException.class), Try.success(4).anyM());
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrap().isFailure(),equalTo(false));
	}
	
	@Test
	public void testLiftAndStream(){

		AnyMFunction2<tryType,Integer,Integer,Integer> divide = AnyM.liftF2(this::divide);
		
		AnyM<tryType,Integer> result = divide.apply(Try.success(2, ArithmeticException.class).anyM(), Try.success(4).anyM());
		
		assertThat(result.<Try<List<Integer>,ArithmeticException>>unwrap().get(),equalTo(0));
		
	}
	
	@Test(expected=ArithmeticException.class)
	public void testLiftNoExceptionType(){
		AnyMFunction2<tryType,Integer,Integer,Integer> divide = AnyM.liftF2(this::divide);
		
		AnyM<tryType,Integer> result = divide.apply(Try.success(2).anyM(),Try.success(0).anyM());
		System.out.println(result);
		fail("exception should be thrown");
	}
	
	
	private Integer divide(Integer a, Integer b){
		return a/b;
	}
}
