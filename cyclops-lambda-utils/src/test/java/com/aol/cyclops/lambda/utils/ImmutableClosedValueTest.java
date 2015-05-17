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
		LazyImmutable<Integer> value = new LazyImmutable<>();
		Supplier s= () -> value.setOnce(10).get();
		assertThat(s.get(),is(10));
		assertThat(value.get(),is(10));
	}
	@Test
	public void testSetOnce2Attempts() {
		LazyImmutable<Integer> value = new LazyImmutable<>();
		Supplier s= () -> value.setOnce(10);
		value.setOnce(20); //first time set
		try{
			s.get();
		}catch(LazyImmutableSetMoreThanOnceException e){
			
		}
		
		assertThat(value.get(),is(20));
	}

	@Test(expected=LazyImmutableSetMoreThanOnceException.class)
	public void testSetOnce2AttemptsException() {
		LazyImmutable<Integer> value = new LazyImmutable<>();
		Supplier s= () -> value.setOnce(10);
		value.setOnce(20); //first time set
		s.get();
		
		fail("Exception expected");
		
	}

	@Test
	public void setOnceLazy(){
		LazyImmutable<Integer> value = new LazyImmutable<>();
		Supplier s= () -> value.getOrSet(()->10);
		assertThat(s.get(),is(10));
		assertThat(value.getOrSet(()->20),is(10));
		
	}
	
	@Test
	public void testEqualsFalse(){
		val value = new LazyImmutable<Integer>();
		value.setOnce(10);
		val value2 = new LazyImmutable<Integer>();
		value2.setOnce(20);
		assertThat(value,not(equalTo(value2)));
	}
	@Test
	public void testEqualsTrue(){
		val value = new LazyImmutable<Integer>();
		value.setOnce(10);
		val value2 = new LazyImmutable<Integer>();
		value2.setOnce(10);
		assertThat(value,equalTo(value2));
	}
	@Test
	public void testHashcode(){
		val value = new LazyImmutable<Integer>();
		value.setOnce(10);
		val value2 = new LazyImmutable<Integer>();
		value2.setOnce(10);
		assertThat(value.hashCode(),equalTo(value2.hashCode()));
	}
	@Test
	public void testHashcodeFalse(){
		val value = new LazyImmutable<Integer>();
		value.setOnce(10);
		val value2 = new LazyImmutable<Integer>();
		value2.setOnce(20);
		assertThat(value.hashCode(),not(equalTo(value2.hashCode())));
	}
	
	@Test
	public void testMapUninitialised(){
		val value = new LazyImmutable<Integer>();
		val value2 = value.map(i->i+10);
		assertThat(value,equalTo(value2));
	}
	@Test
	public void testMap2(){
		val value = new LazyImmutable<Integer>();
		value.setOnce(10);
		val value2 = value.map(i->i+10);
		assertThat(value2.get(),equalTo(20));
	}
	@Test
	public void testFlatMapUninitialised(){
		val value = new LazyImmutable<Integer>();
		val value2 = value.flatMap(i->LazyImmutable.of(i+10));
		assertThat(value,equalTo(value2));
	}
	@Test
	public void testFlatMap2(){
		val value = new LazyImmutable<Integer>();
		value.setOnce(10);
		val value2 = value.flatMap(i->LazyImmutable.of(i+10));
		assertThat(value2.get(),equalTo(20));
	}
	@Test
	public void testLeftIdentity(){
		int a = 10;
		Function<Integer,LazyImmutable<Integer> >f = i->LazyImmutable.of(i+10);
		assertThat(LazyImmutable.of(a).flatMap(f), equalTo( f.apply(10)));
		
	}
	@Test
	public void testRightIdentity(){
		int a = 10;
		val m = LazyImmutable.of(a);
		
		assertThat(m.flatMap(LazyImmutable::of), equalTo( m));
		
	}
	@Test
	public void associativity(){
		int a = 10;
		val m = LazyImmutable.of(a);
		Function<Integer,LazyImmutable<Integer> >f = i->LazyImmutable.of(i+10);
		Function<Integer,LazyImmutable<Integer> >g = i->LazyImmutable.of(i*10);
		assertThat(m.flatMap(f).flatMap(g), equalTo( m.flatMap(x -> f.apply(x).flatMap(g))));
	}
	
	@Test
	public void testRightIdentityUninitialised(){
		
		val m = new LazyImmutable<Integer>();
		
		assertThat(m.flatMap(LazyImmutable::of), equalTo( m));
		
	}
	@Test
	public void associativityUninitialised(){
		int a = 10;
		val m = new LazyImmutable<Integer>();
		Function<Integer,LazyImmutable<Integer> >f = i->LazyImmutable.of(i+10);
		Function<Integer,LazyImmutable<Integer> >g = i->LazyImmutable.of(i*10);
		assertThat(m.flatMap(f).flatMap(g), equalTo( m.flatMap(x -> f.apply(x).flatMap(g))));
	}
}
