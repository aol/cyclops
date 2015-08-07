package com.aol.simple.react.monad;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.Do;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public class DoTest {

	@Test
	public void doTestLazy(){
		for(int i=0;i<100;i++){
		LazyFutureStream<Integer> result =
				Do.with(LazyFutureStream.of(1,2,3))
												.with(Optional.of(2))
												.yield((Integer a) -> (Integer b) -> a+b);
												
		List<Integer> blocked = result.block();
		
		assertThat(blocked,equalTo(Arrays.asList(3,4,5)));
		}
	}
	@Test
	public void doTestSimple(){
		for(int i=0;i<1000;i++){
		SimpleReactStream<Integer> result = Do.with(SimpleReactStream.of(1,2,3))
												.with(Optional.of(2))
												.yield((Integer a) -> (Integer b) -> a+b);
		assertThat(result.block(),equalTo(Arrays.asList(3,4,5)));
		}
	}
	@Test
	public void doTestEager(){
		for(int i=0;i<10;i++){
		EagerFutureStream<Integer> result = Do.with(EagerFutureStream.of(1,2,3))
												.with(Optional.of(2))
												.yield((Integer a) -> (Integer b) -> a+b);
		assertThat(result.block(),equalTo(Arrays.asList(3,4,5)));
		}
	}
	@Test
	public void doTestEagerOptional(){
		for(int i=0;i<1000;i++){
		Optional<List<Integer>> result = Do.with(lookup("empty"))
												.with(EagerFutureStream.of(1,2,3))
												.yield((Integer a) -> (Integer b) -> a+b);
		assertThat(result.isPresent(),equalTo(false));
		}
	}
	@Test
	public void doTestLazyOptional(){
		Optional<List<Integer>> result = Do.with(lookup("empty"))
												.with(LazyFutureStream.of(1,2,3))
												.yield((Integer a) -> (Integer b) -> a+b);
		assertThat(result.isPresent(),equalTo(false));
	}
	@Test
	public void doTestSimpleOptional(){
		Optional<List<Integer>> result = Do.with(lookup("empty"))
												.with(SimpleReactStream.of(1,2,3))
												.yield((Integer a) -> (Integer b) -> a+b);
		assertThat(result.isPresent(),equalTo(false));
	}
	@Test
	public void doTestEagerOptionalEmptyStream(){
		Optional<List<Integer>> result = Do.with(lookup("1"))
												.with(EagerFutureStream.of())
												.yield((Integer a) -> (Integer b) -> a+b);
		assertThat(result.get().size(),equalTo(0));
	}
	@Test
	public void doTestLazyOptionalEmptyStream(){
		Optional<List<Integer>> result = Do.with(lookup("1"))
												.with(LazyFutureStream.of())
												.yield((Integer a) -> (Integer b) -> a+b);
		System.out.println(result);
		assertThat(result.get().size(),equalTo(0));
	}
	@Test
	public void doTestSimpleOptionalEmptyStream(){
		Optional<List<Integer>> result = Do.with(lookup("1"))
												.with(SimpleReactStream.of())
												.yield((Integer a) -> (Integer b) -> a+b);
		assertThat(result.get().size(),equalTo(0));
	}
	
	private Optional<Integer> lookup(String key){
		if("empty".equals(key))
			return Optional.empty();
		else
			return Optional.of(1);
	}
}

