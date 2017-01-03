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
import cyclops.box.MutableFloat;
public class MutableFloatTest {

	@Test
	public void testMutate(){
		MutableFloat num = MutableFloat.of(twenty);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->new Float((float)(n+i)))).forEach(System.out::println);
		    
		assertThat(num.getAsFloat(),is((float)120));
	}
	@Test
	public void inClosure(){
		MutableFloat myInt = new MutableFloat(zero);
		
	  Function<Integer,Function<Integer,MutableFloat>> fn = ((Integer i)-> (Integer j)-> myInt.set(new Float((float)(i*j))));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.getAsFloat(),
				is((float)200));
	}
	@Test
	public void inClosure2(){
		MutableFloat myInt = new MutableFloat(zero);
		
		BiFunction<Float,Float,MutableFloat> fn = (i,j)-> myInt.set(new Float((float)(i*j)));
		fn.apply(ten,twenty);
		
		assertThat(myInt.getAsFloat(),
				is((float)200));
	}

	@Test
	public void testSet() {
		assertThat(new MutableFloat().set(twenty).getAsFloat(),is(twenty));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableFloat(ten).getAsFloat(),equalTo(ten));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableFloat(ten),equalTo(new MutableFloat(ten)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableFloat(ten),not(equalTo(new MutableFloat(twenty))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableFloat(ten).hashCode(),equalTo(new MutableFloat(ten).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableFloat(ten).hashCode(),not(equalTo(new MutableFloat(twenty).hashCode())));
	}
	float value = 0;
	float ten = 10;
	float twenty = 20;
	float  zero =0;
	@Test
	public void externalSet(){
		value = 0;
		MutableFloat ext = MutableFloat.fromExternal(()->value,v->this.value=v);
		ext.set(ten);
		assertThat(value,equalTo((float)10));
	}
	
	@Test
	public void externalGet(){
		value = 100;
		MutableFloat ext = MutableFloat.fromExternal(()->value,v->this.value=v);
		
		assertThat(ext.get(),equalTo((float)100));
	}
	@Test
	public void externalMapInputObj(){
		value = 0;
		Mutable<Float> ext = MutableFloat.fromExternal(()->value,v->this.value=v)
									.mapInputToObj(s->new Float((float)(s+ten)));
		ext.set((float)50);
		assertThat(value,equalTo((float)60));
	}
	
	@Test
	public void externalMapOutputToObj(){
		value = 200;
		Mutable<Float> ext = MutableFloat.fromExternal(()->value,v->this.value=v)
									.mapOutputToObj(s->s*2);
		
		assertThat(ext.get(),equalTo(400f));
	}
	
}
