package com.aol.cyclops2.trycatch;

import cyclops.monads.Witness.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import cyclops.monads.function.AnyMFn2;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.control.Try;


public class LiftTest {

	private Integer add(Integer a, Integer b){
		return a+b;
	}
	
	@Test
	public void testLift(){
		AnyMFn2<tryType,Integer,Integer,Integer> add =	AnyM.liftF2(this::add);
		
		AnyM<tryType,Integer> result = add.apply(Try.success(2)
								                    .anyM(), Try.success(3)
                                                                .anyM());
		assertThat(result.<Try<Integer,RuntimeException>>unwrap().get(),equalTo(5));
	}
	
	@Test
	public void testLiftError(){
		AnyMFn2<tryType,Integer,Integer,Integer> divide = AnyM.liftF2(this::divide);
		
		AnyM<tryType,Integer> result = divide.apply(Try.of(2, ArithmeticException.class).anyM(),Try.of(0,ArithmeticException.class).anyM());
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrap().isFailure(),equalTo(true));
	}
	
	@Test
	public void testLiftErrorAndStream(){
		AnyMFn2<tryType,Integer,Integer,Integer> divide = AnyM.liftF2(this::divide);
		
		AnyM<tryType,Integer> result = divide.apply(Try.of(20, ArithmeticException.class).anyM(), Try.success(4).anyM());
		System.out.println(result);
		assertThat(result.<Try<Integer,ArithmeticException>>unwrap().isFailure(),equalTo(false));
	}
	
	@Test
	public void testLiftAndStream(){

		AnyMFn2<tryType,Integer,Integer,Integer> divide = AnyM.liftF2(this::divide);
		
		AnyM<tryType,Integer> result = divide.apply(Try.of(2, ArithmeticException.class).anyM(), Try.success(4).anyM());
		
		assertThat(result.<Try<List<Integer>,ArithmeticException>>unwrap().get(),equalTo(0));
		
	}
	
	@Test(expected=ArithmeticException.class)
	public void testLiftNoExceptionType(){
		AnyMFn2<tryType,Integer,Integer,Integer> divide = AnyM.liftF2(this::divide);
		
		AnyM<tryType,Integer> result = divide.apply(Try.of(2).anyM(),Try.of(0).anyM());
		System.out.println(result);
		fail("exception should be thrown");
	}
	
	
	private Integer divide(Integer a, Integer b){
		return a/b;
	}
}
