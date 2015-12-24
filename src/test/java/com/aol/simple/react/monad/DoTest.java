package com.aol.simple.react.monad;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.monad.AnyM;
import com.aol.simple.react.stream.traits.BaseSimpleReactStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public class DoTest {

	@Test
	public void doTestLazy(){
		for(int i=0;i<100;i++){
		LazyFutureStream<Integer> result =
				Do.add(LazyFutureStream.of(1,2,3))
												.add(Optional.of(2))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
												
		List<Integer> blocked = result.block();
		
		assertThat(blocked,equalTo(Arrays.asList(3,4,5)));
		}
	}
	public <T> AnyM<T> anyM(BaseSimpleReactStream<T> stream){
		return AnyM.ofMonad(stream);
	}
	@Test
	public void doTestSimple(){
		for(int i=0;i<1000;i++){
		SimpleReactStream<Integer> result = Do.add(anyM(BaseSimpleReactStream.of(1,2,3)))
												.add(Optional.of(2))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.block(),equalTo(Arrays.asList(3,4,5)));
		}
	}
	
	
	@Test
	public void doTestLazyOptional(){
		Optional<List<Integer>> result = Do.add(lookup("empty"))
												.add(LazyFutureStream.of(1,2,3))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.isPresent(),equalTo(false));
	}
	@Test
	public void doTestSimpleOptional(){
		Optional<List<Integer>> result = Do.add(lookup("empty"))
												.add(anyM(BaseSimpleReactStream.of(1,2,3)))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.isPresent(),equalTo(false));
	}
	
	@Test
	public void doTestLazyOptionalEmptyStream(){
		Optional<List<Integer>> result = Do.add(lookup("1"))
												.add(LazyFutureStream.<Integer>of())
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		System.out.println(result);
		assertThat(result.get().size(),equalTo(0));
	}
	@Test
	public void doTestSimpleOptionalEmptyStream(){
		Optional<List<Integer>> result = Do.add(lookup("1"))
												.add(anyM(BaseSimpleReactStream.<Integer>of()))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.get().size(),equalTo(0));
	}
	
	private Optional<Integer> lookup(String key){
		if("empty".equals(key))
			return Optional.empty();
		else
			return Optional.of(1);
	}
}

