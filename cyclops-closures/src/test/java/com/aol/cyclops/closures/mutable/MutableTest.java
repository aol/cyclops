package com.aol.cyclops.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
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
	public void toOptional(){
		assertThat(Mutable.of(10).toOptional(),equalTo(Optional.of(10)));
	}
	@Test
	public void toOptionalNull(){
		assertThat(Mutable.of(null).toOptional(),equalTo(Optional.empty()));
	}
	@Test
	public void toIterator(){
		assertThat(Mutable.of(10).iterator().next(),equalTo(10));
	}
	@Test
	public void toIteratorNull(){
		assertThat(Mutable.of(null).iterator().hasNext(),equalTo(false));
	}
	@Test
	public void toStream(){
		assertThat(Mutable.of(10).toStream().collect(Collectors.toList()),equalTo(Arrays.asList(10)));
	}
	@Test
	public void toStreamNull(){
		assertThat(Mutable.of(null).toStream().collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void toList(){
		assertThat(Mutable.of(10).toList(),equalTo(Arrays.asList(10)));
	}
	@Test
	public void toListNull(){
		assertThat(Mutable.of(null).toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void toAtomicReference(){
		assertThat(Mutable.of(10).toAtomicReference().get(),equalTo(new AtomicReference(10).get()));
	}
	@Test
	public void toOptionalAtomicReferenceNull(){
		assertThat(Mutable.of(null).toOptionalAtomicReference(),equalTo(Optional.empty()));
	}
	@Test
	public void toOptionalAtomicReference(){
		assertThat(Mutable.of(10).toOptionalAtomicReference().get().get(),equalTo(10));
	}
	@Test
	public void toAtomicReferenceNull(){
		assertThat(Mutable.of(null).toAtomicReference().get(),equalTo(new AtomicReference(null).get()));
	}
	
	@Test
	public void orElse(){
		assertThat(Mutable.of(10).orElse(11),equalTo(10));
	}
	@Test
	public void orElseNull(){
		assertThat(Mutable.of(null).orElse(11),equalTo(11));
	}
	@Test
	public void orElseThrow(){
		assertThat(Mutable.of(10).orElseThrow(()->new RuntimeException()),equalTo(10));
	}
	@Test(expected=RuntimeException.class)
	public void orElseThrowNull(){
		Mutable.of(null).orElseThrow(()->new RuntimeException());
		fail("exception expected");
	}
	
}
