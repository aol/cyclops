package com.aol.cyclops2.closures;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops2.types.foldable.Convertable;
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
	public void toCompletableFutureAsyncEx(){
		assertThat(Convertable.fromSupplier(()->10).toCompletableFutureAsync(Executors.newSingleThreadExecutor()).join(),equalTo(10));
	}
	@Test(expected=RuntimeException.class)
	public void orElseThrowNull(){
		Convertable.fromSupplier(()->null).orElseThrow(()->new RuntimeException());
		fail("exception expected");
	}
}
