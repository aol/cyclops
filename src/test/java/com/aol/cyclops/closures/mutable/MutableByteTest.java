package com.aol.cyclops.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.*;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.data.MutableByte;
public class MutableByteTest {

	@Test
	public void testMutate(){
		MutableByte num = MutableByte.of(twenty);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->new Byte((byte)(n+i)))).forEach(System.out::println);
		    
		assertThat(num.getAsByte(),is((byte)120));
	}
	@Test
	public void inClosure(){
		MutableByte myInt = new MutableByte(zero);
		
	  Function<Integer,Function<Integer,MutableByte>> fn = ((Integer i)-> (Integer j)-> myInt.set(new Byte((byte)(i*j))));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.getAsByte(),
				is((byte)200));
	}
	@Test
	public void inClosure2(){
		MutableByte myInt = new MutableByte(zero);
		
		BiFunction<Byte,Byte,MutableByte> fn = (i,j)-> myInt.set(new Byte((byte)(i*j)));
		fn.apply(ten,twenty);
		
		assertThat(myInt.getAsByte(),
				is((byte)200));
	}

	@Test
	public void testSet() {
		assertThat(new MutableByte().set(twenty).getAsByte(),is(twenty));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableByte(ten).getAsByte(),equalTo(ten));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableByte(ten),equalTo(new MutableByte(ten)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableByte(ten),not(equalTo(new MutableByte(twenty))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableByte(ten).hashCode(),equalTo(new MutableByte(ten).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableByte(ten).hashCode(),not(equalTo(new MutableByte(twenty).hashCode())));
	}
	byte value = 0;
	byte ten = 10;
	byte twenty = 20;
	byte  zero =0;
	@Test
	public void externalSet(){
		value = 0;
		MutableByte ext = MutableByte.fromExternal(()->value,v->this.value=v);
		ext.set(ten);
		assertThat(value,equalTo((byte)10));
	}
	
	@Test
	public void externalGet(){
		value = 100;
		MutableByte ext = MutableByte.fromExternal(()->value,v->this.value=v);
		
		assertThat(ext.get(),equalTo((byte)100));
	}
	@Test
	public void externalMapInputObj(){
		value = 0;
		Mutable<Byte> ext = MutableByte.fromExternal(()->value,v->this.value=v)
									.mapInputToObj(s->new Byte((byte)(s+ten)));
		ext.set((byte)50);
		assertThat(value,equalTo((byte)60));
	}
	
	@Test
	public void externalMapOutputToObj(){
		value = 20;
		Mutable<Integer> ext = MutableByte.fromExternal(()->value,v->this.value=v)
									.mapOutputToObj(s->s*2);
		
		assertThat(ext.get(),equalTo(40));
	}
	
}
