package com.oath.cyclops.closures.immutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oath.cyclops.util.box.LazyImmutable;
import org.junit.Test;

import lombok.val;
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

		s.get();


		assertThat(value.get(),is(20));
	}

	@Test
	public void race() throws InterruptedException{
		for(int i=0;i<1_000;i++){
		LazyImmutable<Integer> value = new LazyImmutable<>();
			CountDownLatch init = new CountDownLatch(1);
			CountDownLatch finished = new CountDownLatch(1);
			CompletableFuture<Integer> readThread = new CompletableFuture<Integer>();
			Thread t = new Thread ( ()->{
				init.countDown();
				value.setOnce(20);
				readThread.complete(value.get());
				finished.countDown();
			});
			t.start();
			init.await();
			value.setOnce(10);
			int readLocal = value.get();
			finished.await();
			assertEquals(readLocal,(int)readThread.join());
		}
	}

	@Test
	public void setOnceLazy(){
		LazyImmutable<Integer> value = new LazyImmutable<>();
		Supplier s= () -> value.computeIfAbsent(()->10);
		assertThat(s.get(),is(10));
		assertThat(value.computeIfAbsent(()->20),is(10));

	}

	@Test
	public void testEqualsFalse(){
        LazyImmutable<Integer> value = new LazyImmutable<Integer>();
		value.setOnce(10);
        LazyImmutable<Integer> value2 = new LazyImmutable<Integer>();
		value2.setOnce(20);
		assertThat(value,not(equalTo(value2)));
	}
	@Test
	public void testEqualsTrue(){
        LazyImmutable<Integer> value = new LazyImmutable<Integer>();
		value.setOnce(10);
        LazyImmutable<Integer> value2 = new LazyImmutable<Integer>();
		value2.setOnce(10);
		assertThat(value.get(),equalTo(value2.get()));
	}
	@Test
	public void testHashcode(){
        LazyImmutable<Integer> value = new LazyImmutable<Integer>();
		value.setOnce(10);
        LazyImmutable<Integer> value2 = new LazyImmutable<Integer>();
		value2.setOnce(10);
		assertThat(value.get().hashCode(),equalTo(value2.get().hashCode()));
	}
	@Test
	public void testHashcodeFalse(){
        LazyImmutable<Integer> value = new LazyImmutable<Integer>();
		value.setOnce(10);
        LazyImmutable<Integer> value2 = new LazyImmutable<Integer>();
		value2.setOnce(20);
		assertThat(value.hashCode(),not(equalTo(value2.hashCode())));
	}

	@Test
	public void testMapUninitialised(){
        LazyImmutable<Integer> value = new LazyImmutable<Integer>();
        LazyImmutable<Integer> value2 = value.map(i->i+10);
		assertThat(value,equalTo(value2));
	}
	@Test
	public void testMap2(){
        LazyImmutable<Integer> value = new LazyImmutable<Integer>();
		value.setOnce(10);
        LazyImmutable<Integer> value2 = value.map(i->i+10);
		assertThat(value2.get(),equalTo(20));
	}
	@Test
	public void testFlatMapUninitialised(){
        LazyImmutable<Integer> value = new LazyImmutable<Integer>();
        LazyImmutable<? extends Integer> value2 = value.flatMap(i -> LazyImmutable.of(i + 10));
		assertThat(value,equalTo(value2));
	}
	@Test
	public void testFlatMap2(){
        LazyImmutable<Integer> value = new LazyImmutable<Integer>();
		value.setOnce(10);
        LazyImmutable<? extends Integer> value2 = value.flatMap(i -> LazyImmutable.of(i + 10));
		assertThat(value2.get(),equalTo(20));
	}
	@Test
	public void testLeftIdentity(){
		int a = 10;
		Function<Integer,LazyImmutable<Integer> >f = i->LazyImmutable.of(i+10);
		assertThat(LazyImmutable.of(a).flatMap(f).get(), equalTo( f.apply(10).get()));

	}
	@Test
	public void testRightIdentity(){
		int a = 10;
        LazyImmutable<Integer> m = LazyImmutable.of(a);

		assertThat(m.flatMap(LazyImmutable::of).get(), equalTo( m.get()));

	}
	@Test
	public void associativity(){
		int a = 10;
        LazyImmutable<Integer> m = LazyImmutable.of(a);
		Function<Integer,LazyImmutable<Integer> >f = i->LazyImmutable.of(i+10);
		Function<Integer,LazyImmutable<Integer> >g = i->LazyImmutable.of(i*10);
		assertThat(m.flatMap(f).flatMap(g).get(), equalTo( m.flatMap(x -> f.apply(x).flatMap(g)).get()));
	}

	@Test
	public void testRightIdentityUninitialised(){

        LazyImmutable<Integer> m = new LazyImmutable<Integer>();

		assertThat(m.<Integer>flatMap(LazyImmutable::of), equalTo( m));

	}
	@Test
	public void associativityUninitialised(){
		int a = 10;
        LazyImmutable<Integer> m = new LazyImmutable<Integer>();
		Function<Integer,LazyImmutable<Integer> >f = i->LazyImmutable.of(i+10);
		Function<Integer,LazyImmutable<Integer> >g = i->LazyImmutable.of(i*10);
		assertThat(m.<Integer>flatMap(f).<Integer>flatMap(g), equalTo( m.<Integer>flatMap(x -> f.apply(x).flatMap(g))));
	}
}
