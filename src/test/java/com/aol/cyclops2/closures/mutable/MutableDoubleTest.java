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
import cyclops.box.MutableDouble;
public class MutableDoubleTest {

	@Test
	public void testMutate(){
		MutableDouble num = MutableDouble.of(20);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).forEach(System.out::println);
		    
		assertThat(num.getAsDouble(),is(120.0));
	}
	@Test
	public void inClosure(){
		MutableDouble myInt = new MutableDouble(0);
		
	  Function<Integer,Function<Integer,MutableDouble>> fn = ((Integer i)-> (Integer j)-> myInt.set(i*j));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.getAsDouble(),
				is(200.0));
	}
	@Test
	public void inClosure2(){
		MutableDouble myInt = new MutableDouble(0);
		
		BiFunction<Integer,Integer,MutableDouble> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.getAsDouble(),
				is(200.0));
	}

	@Test
	public void testSet() {
		assertThat(new MutableDouble().set(1000).getAsDouble(),is(1000.0));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableDouble(10).getAsDouble(),equalTo(10.0));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableDouble(10),equalTo(new MutableDouble(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableDouble(10),not(equalTo(new MutableDouble(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableDouble(10).hashCode(),equalTo(new MutableDouble(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableDouble(10).hashCode(),not(equalTo(new MutableDouble(20).hashCode())));
	}
	double value = 0;
	
	@Test
	public void externalSet(){
		value = 0;
		MutableDouble ext = MutableDouble.fromExternal(()->value,v->this.value=v);
		ext.set(10l);
		assertThat(value,equalTo(10d));
	}
	
	@Test
	public void externalGet(){
		value = 100;
		MutableDouble ext = MutableDouble.fromExternal(()->value,v->this.value=v);
		
		assertThat(ext.get(),equalTo(100d));
	}
	@Test
	public void externalMapInputObj(){
		value = 0;
		Mutable<Double> ext = MutableDouble.fromExternal(()->value,v->this.value=v)
									.mapInputToObj(s->s+10);
		ext.set(50d);
		assertThat(value,equalTo(60d));
	}
	
	@Test
	public void externalMapOutputToObj(){
		value = 200;
		Mutable<Double> ext = MutableDouble.fromExternal(()->value,v->this.value=v)
									.mapOutputToObj(s->s*2);
		
		assertThat(ext.get(),equalTo(400d));
	}
	@Test
	public void externalMapInput(){
		value = 0;
		MutableDouble ext = MutableDouble.fromExternal(()->value,v->this.value=v)
									.mapInput(s->s+10);
		ext.set(50d);
		assertThat(value,equalTo(60d));
	}
	
	@Test
	public void externalMapOutput(){
		value = 200;
		MutableDouble ext = MutableDouble.fromExternal(()->value,v->this.value=v)
									.mapOutput(s->s*2);
		
		assertThat(ext.get(),equalTo(400d));
	}
}
