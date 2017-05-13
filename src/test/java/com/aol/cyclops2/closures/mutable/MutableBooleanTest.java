package com.aol.cyclops2.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.collections.box.Mutable;
import cyclops.collections.box.MutableBoolean;

public class MutableBooleanTest {

	@Test
	public void testMutate(){
		MutableBoolean num = MutableBoolean.of(falseB);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->!n)).forEach(System.out::println);
		    
		assertThat(num.getAsBoolean(),is(false));
	}
	@Test
	public void inClosure(){
		MutableBoolean myInt = new MutableBoolean(zero);
		
	  Function<Integer,Function<Integer,MutableBoolean>> fn = ( (Integer i)-> (Integer j)-> myInt.set(true));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.getAsBoolean(),
				is(true));
	}
	@Test
	public void inClosure2(){
		MutableBoolean myInt = new MutableBoolean(zero);
		
		BiFunction<Boolean,Boolean,MutableBoolean> fn = (i,j)-> myInt.set(true);
		fn.apply(trueB,falseB);
		
		assertThat(myInt.getAsBoolean(),
				is(true));
	}

	@Test
	public void testSet() {
		assertThat(new MutableBoolean().set(falseB).getAsBoolean(),is(falseB));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableBoolean(trueB).getAsBoolean(),equalTo(trueB));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableBoolean(trueB),equalTo(new MutableBoolean(trueB)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableBoolean(trueB),not(equalTo(new MutableBoolean(falseB))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableBoolean(trueB).hashCode(),equalTo(new MutableBoolean(trueB).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableBoolean(trueB).hashCode(),not(equalTo(new MutableBoolean(falseB).hashCode())));
	}
	boolean value = false;
	boolean trueB =true;
	boolean falseB = false;
	boolean  zero =false;
	@Test
	public void externalSet(){
		value = false;
		MutableBoolean ext = MutableBoolean.fromExternal(()->value,v->this.value=v);
		ext.set(false);
		assertThat(value,equalTo(false));
	}
	
	@Test
	public void externalGet(){
		value = false;
		MutableBoolean ext = MutableBoolean.fromExternal(()->value,v->this.value=v);
		
		assertThat(ext.get(),equalTo(false));
	}
	@Test
	public void externalMapInputObj(){
		value = false;
		Mutable<Boolean> ext = MutableBoolean.fromExternal(()->value,v->this.value=v)
									.mapInputToObj(s->!s);
		ext.set(true);
		assertThat(value,equalTo(false));
	}
	
	@Test
	public void externalMapOutputToObj(){
		value = false;
		Mutable<Boolean> ext = MutableBoolean.fromExternal(()->value,v->this.value=v)
									.mapOutputToObj(s->!s);
		
		assertThat(ext.get(),equalTo(true));
	}
	
}
