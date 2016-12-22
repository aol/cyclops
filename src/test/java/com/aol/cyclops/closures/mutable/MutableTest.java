package com.aol.cyclops.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.box.Mutable;
public class MutableTest {

	@Test
	public void testMutate(){
		Mutable<Integer> num = Mutable.of(20);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).forEach(System.out::println);
		    
		assertThat(num.get(),is(120));
	}
	@Test
	public void inClosure(){
		Mutable<Integer> myInt = new Mutable<>(0);
		
	  Function<Integer,Function<Integer,Mutable<Integer>>> fn = ((Integer i)-> (Integer j)-> myInt.set(i*j));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.get(),
				is(200));
	}
	@Test
	public void inClosure2(){
		Mutable<Integer> myInt = new Mutable<>(0);
		
		BiFunction<Integer,Integer,Mutable<Integer>> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.get(),
				is(200));
	}

	@Test
	public void testSet() {
		assertThat(new Mutable().set("hello").get(),is("hello"));
	}

	@Test
	public void testClosedVar() {
		assertThat(new Mutable(10).get(),equalTo(10));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new Mutable(10),equalTo(new Mutable(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new Mutable(10),not(equalTo(new Mutable(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new Mutable(10).hashCode(),equalTo(new Mutable(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new Mutable(10).hashCode(),not(equalTo(new Mutable(20).hashCode())));
	}
	
	@Test
	public void toIterator(){
		assertThat(Mutable.of(10).iterator().next(),equalTo(10));
	}
	@Test
	public void toIteratorNull(){
		assertThat(Mutable.of(null).iterator().hasNext(),equalTo(false));
	}



	
	
	String value = "";
	
	@Test
	public void externalSet(){
		value = "";
		Mutable<String> ext = Mutable.fromExternal(()->value,v->this.value=v);
		ext.set("hello");
		assertThat(value,equalTo("hello"));
	}
	
	@Test
	public void externalGet(){
		value = "world";
		Mutable<String> ext = Mutable.fromExternal(()->value,v->this.value=v);
		
		assertThat(ext.get(),equalTo("world"));
	}
	@Test
	public void externalMapInput(){
		value = "";
		Mutable<String> ext = Mutable.fromExternal(()->value,v->this.value=v)
									.mapInput(s->s+"!");
		ext.set("hello");
		assertThat(value,equalTo("hello!"));
	}
	
	@Test
	public void externalMapOutputs(){
		value = "world";
		Mutable<String> ext = Mutable.fromExternal(()->value,v->this.value=v)
									.mapOutput(s->s+"?");
		
		assertThat(ext.get(),equalTo("world?"));
	}
}
