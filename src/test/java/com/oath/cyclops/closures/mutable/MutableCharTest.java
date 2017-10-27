package com.oath.cyclops.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import com.oath.cyclops.util.box.Mutable;
import com.oath.cyclops.util.box.MutableChar;
import org.junit.Test;

public class MutableCharTest {

	@Test
	public void testMutate(){
		MutableChar num = MutableChar.of(twenty);

		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->new Character((char)(n+i)))).forEach(System.out::println);

		assertThat(num.getAsChar(),is((char)120));
	}
	@Test
	public void inClosure(){
		MutableChar myInt = new MutableChar(zero);

	  Function<Integer,Function<Integer,MutableChar>> fn = ((Integer i)-> (Integer j)-> myInt.set(new Character((char)(i*j))));
	  fn.apply(10).apply(20);

		assertThat(myInt.getAsChar(),
				is((char)200));
	}
	@Test
	public void inClosure2(){
		MutableChar myInt = new MutableChar(zero);

		BiFunction<Character,Character,MutableChar> fn = (i,j)-> myInt.set(new Character((char)(i*j)));
		fn.apply(ten,twenty);

		assertThat(myInt.getAsChar(),
				is((char)200));
	}

	@Test
	public void testSet() {
		assertThat(new MutableChar().set(twenty).getAsChar(),is(twenty));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableChar(ten).getAsChar(),equalTo(ten));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableChar(ten),equalTo(new MutableChar(ten)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableChar(ten),not(equalTo(new MutableChar(twenty))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableChar(ten).hashCode(),equalTo(new MutableChar(ten).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableChar(ten).hashCode(),not(equalTo(new MutableChar(twenty).hashCode())));
	}
	char value = 0;
	char ten = 10;
	char twenty = 20;
	char  zero =0;
	@Test
	public void externalSet(){
		value = 0;
		MutableChar ext = MutableChar.fromExternal(()->value,v->this.value=v);
		ext.set(ten);
		assertThat(value,equalTo((char)10));
	}

	@Test
	public void externalGet(){
		value = 100;
		MutableChar ext = MutableChar.fromExternal(()->value,v->this.value=v);

		assertThat(ext.get(),equalTo((char)100));
	}
	@Test
	public void externalMapInputObj(){
		value = 0;
		Mutable<Character> ext = MutableChar.fromExternal(()->value, v->this.value=v)
									.mapInputToObj(s->new Character((char)(s+ten)));
		ext.set((char)50);
		assertThat(value,equalTo((char)60));
	}

	@Test
	public void externalMapOutputToObj(){
		value = 200;
		Mutable<Integer> ext = MutableChar.fromExternal(()->value,v->this.value=v)
									.mapOutputToObj(s->s*2);

		assertThat(ext.get(),equalTo(400));
	}

}
