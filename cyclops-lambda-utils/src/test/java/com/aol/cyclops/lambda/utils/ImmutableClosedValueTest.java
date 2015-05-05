package com.aol.cyclops.lambda.utils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.function.Function;
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
	
	@Test
	public void testMapUninitialised(){
		val value = new ImmutableClosedValue<Integer>();
		val value2 = value.map(i->i+10);
		assertThat(value,equalTo(value2));
	}
	@Test
	public void testMap2(){
		val value = new ImmutableClosedValue<Integer>();
		value.setOnce(10);
		val value2 = value.map(i->i+10);
		assertThat(value2.get(),equalTo(20));
	}
	@Test
	public void testFlatMapUninitialised(){
		val value = new ImmutableClosedValue<Integer>();
		val value2 = value.flatMap(i->ImmutableClosedValue.of(i+10));
		assertThat(value,equalTo(value2));
	}
	@Test
	public void testFlatMap2(){
		val value = new ImmutableClosedValue<Integer>();
		value.setOnce(10);
		val value2 = value.flatMap(i->ImmutableClosedValue.of(i+10));
		assertThat(value2.get(),equalTo(20));
	}
	@Test
	public void testLeftIdentity(){
		int a = 10;
		Function<Integer,ImmutableClosedValue<Integer> >f = i->ImmutableClosedValue.of(i+10);
		assertThat(ImmutableClosedValue.of(a).flatMap(f), equalTo( f.apply(10)));
		
	}
	@Test
	public void testRightIdentity(){
		int a = 10;
		val m = ImmutableClosedValue.of(a);
		
		assertThat(m.flatMap(ImmutableClosedValue::of), equalTo( m));
		
	}
	@Test
	public void associativity(){
		int a = 10;
		val m = ImmutableClosedValue.of(a);
		Function<Integer,ImmutableClosedValue<Integer> >f = i->ImmutableClosedValue.of(i+10);
		Function<Integer,ImmutableClosedValue<Integer> >g = i->ImmutableClosedValue.of(i*10);
		assertThat(m.flatMap(f).flatMap(g), equalTo( m.flatMap(x -> f.apply(x).flatMap(g))));
	}
	
	@Test
	public void testRightIdentityUninitialised(){
		
		val m = new ImmutableClosedValue<Integer>();
		
		assertThat(m.flatMap(ImmutableClosedValue::of), equalTo( m));
		
	}
	@Test
	public void associativityUninitialised(){
		int a = 10;
		val m = new ImmutableClosedValue<Integer>();
		Function<Integer,ImmutableClosedValue<Integer> >f = i->ImmutableClosedValue.of(i+10);
		Function<Integer,ImmutableClosedValue<Integer> >g = i->ImmutableClosedValue.of(i*10);
		assertThat(m.flatMap(f).flatMap(g), equalTo( m.flatMap(x -> f.apply(x).flatMap(g))));
	}
}
