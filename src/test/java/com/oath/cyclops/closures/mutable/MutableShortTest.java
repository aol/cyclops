package com.oath.cyclops.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import com.oath.cyclops.util.box.Mutable;
import org.junit.Test;

import com.oath.cyclops.util.box.MutableShort;
public class MutableShortTest {

	@Test
	public void testMutate(){
		MutableShort num = MutableShort.of(twenty);

		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->new Short((short)(n+i)))).forEach(System.out::println);

		assertThat(num.getAsShort(),is((short)120));
	}
	@Test
	public void inClosure(){
		MutableShort myInt = new MutableShort(zero);

	  Function<Integer,Function<Integer,MutableShort>> fn = ((Integer i)-> (Integer j)-> myInt.set(new Short((short)(i*j))));
	  fn.apply(10).apply(20);

		assertThat(myInt.getAsShort(),
				is((short)200));
	}
	@Test
	public void inClosure2(){
		MutableShort myInt = new MutableShort(zero);

		BiFunction<Short,Short,MutableShort> fn = (i,j)-> myInt.set(new Short((short)(i*j)));
		fn.apply(ten,twenty);

		assertThat(myInt.getAsShort(),
				is((short)200));
	}

	@Test
	public void testSet() {
		assertThat(new MutableShort().set(twenty).getAsShort(),is(twenty));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableShort(ten).getAsShort(),equalTo(ten));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableShort(ten),equalTo(new MutableShort(ten)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableShort(ten),not(equalTo(new MutableShort(twenty))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableShort(ten).hashCode(),equalTo(new MutableShort(ten).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableShort(ten).hashCode(),not(equalTo(new MutableShort(twenty).hashCode())));
	}
	short value = 0;
	short ten = 10;
	short twenty = 20;
	short  zero =0;
	@Test
	public void externalSet(){
		value = 0;
		MutableShort ext = MutableShort.fromExternal(()->value,v->this.value=v);
		ext.set(ten);
		assertThat(value,equalTo((short)10));
	}

	@Test
	public void externalGet(){
		value = 100;
		MutableShort ext = MutableShort.fromExternal(()->value,v->this.value=v);

		assertThat(ext.get(),equalTo((short)100));
	}
	@Test
	public void externalMapInputObj(){
		value = 0;
		Mutable<Short> ext = MutableShort.fromExternal(()->value, v->this.value=v)
									.mapInputToObj(s->new Short((short)(s+ten)));
		ext.set((short)50);
		assertThat(value,equalTo((short)60));
	}

	@Test
	public void externalMapOutputToObj(){
		value = 200;
		Mutable<Integer> ext = MutableShort.fromExternal(()->value,v->this.value=v)
									.mapOutputToObj(s->s*2);

		assertThat(ext.get(),equalTo(400));
	}

}
