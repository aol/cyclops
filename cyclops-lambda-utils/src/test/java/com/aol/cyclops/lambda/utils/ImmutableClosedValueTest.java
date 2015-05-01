package com.aol.cyclops.lambda.utils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.function.Supplier;

import lombok.val;

import org.junit.Test;
public class ImmutableClosedValueTest {

	@Test
	public void testSetOnce() {
		ImmutableClosedValue<Integer> value = new ImmutableClosedValue<>();
		Supplier s= () -> value.setOnce(10).get();
		assertThat(s.get(),is(10));
		assertThat(value.get(),is(10));
	}
	@Test
	public void testSetOnce2Attempts() {
		ImmutableClosedValue<Integer> value = new ImmutableClosedValue<>();
		Supplier s= () -> value.setOnce(10);
		value.setOnce(20); //first time set
		try{
			s.get();
		}catch(ImmutableClosedValueSetMoreThanOnceException e){
			
		}
		
		assertThat(value.get(),is(20));
	}

	@Test(expected=ImmutableClosedValueSetMoreThanOnceException.class)
	public void testSetOnce2AttemptsException() {
		ImmutableClosedValue<Integer> value = new ImmutableClosedValue<>();
		Supplier s= () -> value.setOnce(10);
		value.setOnce(20); //first time set
		s.get();
		
		fail("Exception expected");
		
	}

	@Test
	public void setOnceLazy(){
		ImmutableClosedValue<Integer> value = new ImmutableClosedValue<>();
		Supplier s= () -> value.getOrSet(()->10);
		assertThat(s.get(),is(10));
		assertThat(value.getOrSet(()->20),is(10));
		
	}
	
	@Test
	public void testEqualsFalse(){
		val value = new ImmutableClosedValue<Integer>();
		value.setOnce(10);
		val value2 = new ImmutableClosedValue<Integer>();
		value2.setOnce(20);
		assertThat(value,not(equalTo(value2)));
	}
	@Test
	public void testEqualsTrue(){
		val value = new ImmutableClosedValue<Integer>();
		value.setOnce(10);
		val value2 = new ImmutableClosedValue<Integer>();
		value2.setOnce(10);
		assertThat(value,equalTo(value2));
	}
	@Test
	public void testHashcode(){
		val value = new ImmutableClosedValue<Integer>();
		value.setOnce(10);
		val value2 = new ImmutableClosedValue<Integer>();
		value2.setOnce(10);
		assertThat(value.hashCode(),equalTo(value2.hashCode()));
	}
	@Test
	public void testHashcodeFalse(){
		val value = new ImmutableClosedValue<Integer>();
		value.setOnce(10);
		val value2 = new ImmutableClosedValue<Integer>();
		value2.setOnce(20);
		assertThat(value.hashCode(),not(equalTo(value2.hashCode())));
	}
}
