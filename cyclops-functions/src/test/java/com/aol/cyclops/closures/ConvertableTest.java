package com.aol.cyclops.closures;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.closures.mutable.Mutable;

import static org.hamcrest.Matchers.equalTo;
public class ConvertableTest {

	@Test
	public void test() {
		Convertable<Integer> c = Convertable.fromSupplier(()->10);
		assertThat(c.get(),equalTo(10));
	}
	@Test
	public void toOptional(){
		assertThat(Convertable.fromSupplier(()->10).toOptional(),equalTo(Optional.of(10)));
	}
	@Test
	public void toOptionalNull(){
		assertThat(Convertable.fromSupplier(()->null).toOptional(),equalTo(Optional.empty()));
	}
	@Test
	public void toIterator(){
		assertThat(Convertable.fromSupplier(()->10).iterator().next(),equalTo(10));
	}
	@Test
	public void toIteratorNull(){
		assertThat(Convertable.fromSupplier(()->null).iterator().hasNext(),equalTo(false));
	}
	@Test
	public void toStream(){
		assertThat(Convertable.fromSupplier(()->10).toStream().collect(Collectors.toList()),equalTo(Arrays.asList(10)));
	}
	@Test
	public void toStreamNull(){
		assertThat(Convertable.fromSupplier(()->null).toStream().collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void toList(){
		assertThat(Convertable.fromSupplier(()->10).toList(),equalTo(Arrays.asList(10)));
	}
	@Test
	public void toListNull(){
		assertThat(Convertable.fromSupplier(()->null).toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void toAtomicReference(){
		assertThat(Convertable.fromSupplier(()->10).toAtomicReference().get(),equalTo(new AtomicReference(10).get()));
	}
	@Test
	public void toOptionalAtomicReferenceNull(){
		assertThat(Convertable.fromSupplier(()->null).toOptionalAtomicReference(),equalTo(Optional.empty()));
	}
	@Test
	public void testWhenNull(){
		assertThat(Convertable.fromSupplier(()->null).when(t->t!=null).isPresent(),equalTo(false));
	}
	@Test
	public void testWhenDoesNotHold(){
		assertThat(Convertable.fromSupplier(()->10).when(t->t==20).isPresent(),equalTo(false));
	}
	@Test
	public void testWhenHolds(){
		assertThat(Convertable.fromSupplier(()->10).when(t->t==10).isPresent(),equalTo(true));
	}
	@Test
	public void testWhenNullMap(){
		assertThat(Convertable.fromSupplier(()->null).when(t->t!=null,t->"hello").isPresent(),equalTo(false));
	}
	@Test
	public void testWhenDoesNotHoldMap(){
		assertThat(Convertable.fromSupplier(()->10).when(t->t==20,t->"hello").isPresent(),equalTo(false));
	}
	@Test
	public void testWhenHoldsMap(){
		assertThat(Convertable.fromSupplier(()->10).when(t->t==10,t->"hello").get(),equalTo("hello"));
	}
	@Test
	public void testWhenHoldsMapDefault(){
		assertThat(Convertable.fromSupplier(()->10).whenOrElse("world",t->t==10,t->"hello"),equalTo("hello"));
	}
	@Test
	public void testWhenNullMapDefault(){
		assertThat(Convertable.fromSupplier(()->null).whenOrElse("world",t->t!=null,t->"hello"),equalTo("world"));
	}
	@Test
	public void testWhenDoesNotHoldMapDefault(){
		assertThat(Convertable.fromSupplier(()->10).whenOrElse("world",t->t==20,t->"hello"),equalTo("world"));
	}
	
	@Test
	public void toOptionalAtomicReference(){
		assertThat(Convertable.fromSupplier(()->10).toOptionalAtomicReference().get().get(),equalTo(10));
	}
	@Test
	public void toAtomicReferenceNull(){
		assertThat(Convertable.fromSupplier(()->null).toAtomicReference().get(),equalTo(new AtomicReference(null).get()));
	}
	
	@Test
	public void orElse(){
		assertThat(Convertable.fromSupplier(()->10).orElse(11),equalTo(10));
	}
	@Test
	public void orElseNull(){
		assertThat(Convertable.fromSupplier(()->null).orElse(11),equalTo(11));
	}
	@Test
	public void orElseThrow() throws RuntimeException{
		//Hack for JDK issue : https://bugs.openjdk.java.net/browse/JDK-8066974
		assertThat(Convertable.fromSupplier(()->10).<RuntimeException>orElseThrow(()->new RuntimeException()),equalTo(10));
	}
	@Test
	public void toCompletableFuture(){
		assertThat(Convertable.fromSupplier(()->10).toCompletableFuture().join(),equalTo(10));
	}
	@Test
	public void toCompletableFutureAsync(){
		assertThat(Convertable.fromSupplier(()->10).toCompletableFutureAsync().join(),equalTo(10));
	}
	@Test
	public void toCompletableFutureAsyncEx(){
		assertThat(Convertable.fromSupplier(()->10).toCompletableFutureAsync(Executors.newSingleThreadExecutor()).join(),equalTo(10));
	}
	@Test(expected=RuntimeException.class)
	public void orElseThrowNull(){
		Convertable.fromSupplier(()->null).orElseThrow(()->new RuntimeException());
		fail("exception expected");
	}
}
