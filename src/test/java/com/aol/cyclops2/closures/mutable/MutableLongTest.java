package com.aol.cyclops2.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.box.Mutable;
import cyclops.box.MutableLong;
public class MutableLongTest {

	@Test
	public void testMutate(){
		MutableLong num = MutableLong.of(20);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).forEach(System.out::println);
		    
		assertThat(num.getAsLong(),is(120l));
	}
	@Test
	public void inClosure(){
		MutableLong myInt = new MutableLong(0);
		
	  Function<Integer,Function<Integer,MutableLong>> fn = ((Integer i)-> (Integer j)-> myInt.set(i*j));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.getAsLong(),
				is(200l));
	}
	@Test
	public void inClosure2(){
		MutableLong myInt = new MutableLong(0);
		
		BiFunction<Integer,Integer,MutableLong> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.getAsLong(),
				is(200l));
	}

	@Test
	public void testSet() {
		assertThat(new MutableLong().set(1000).getAsLong(),is(1000l));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableLong(10).getAsLong(),equalTo(10l));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableLong(10),equalTo(new MutableLong(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableLong(10),not(equalTo(new MutableLong(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableLong(10).hashCode(),equalTo(new MutableLong(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableLong(10).hashCode(),not(equalTo(new MutableLong(20).hashCode())));
	}
	long value = 0;
	
	@Test
	public void externalSet(){
		value = 0;
		MutableLong ext = MutableLong.fromExternal(()->value,v->this.value=v);
		ext.set(10l);
		assertThat(value,equalTo(10l));
	}
	
	@Test
	public void externalGet(){
		value = 100;
		MutableLong ext = MutableLong.fromExternal(()->value,v->this.value=v);
		
		assertThat(ext.get(),equalTo(100L));
	}
	@Test
	public void externalMapInputObj(){
		value = 0;
		Mutable<Long> ext = MutableLong.fromExternal(()->value,v->this.value=v)
									.mapInputToObj(s->s+10);
		ext.set(50l);
		assertThat(value,equalTo(60l));
	}
	
	@Test
	public void externalMapOutputToObj(){
		value = 200;
		Mutable<Long> ext = MutableLong.fromExternal(()->value,v->this.value=v)
									.mapOutputToObj(s->s*2);
		
		assertThat(ext.get(),equalTo(400l));
	}
	@Test
	public void externalMapInput(){
		value = 0;
		MutableLong ext = MutableLong.fromExternal(()->value,v->this.value=v)
									.mapInput(s->s+10);
		ext.set(50);
		assertThat(value,equalTo(60l));
	}
	
	@Test
	public void externalMapOutput(){
		value = 200;
		MutableLong ext = MutableLong.fromExternal(()->value,v->this.value=v)
									.mapOutput(s->s*2);
		
		assertThat(ext.get(),equalTo(400l));
	}
}
