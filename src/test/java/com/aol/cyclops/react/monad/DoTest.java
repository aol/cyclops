package com.aol.cyclops.react.monad;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.types.futurestream.BaseSimpleReactStream;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.futurestream.SimpleReactStream;

public class DoTest {

	@Test
	public void doTestLazy(){
		for(int i=0;i<100;i++){
		LazyFutureStream<Integer> result =
				For.stream(LazyFutureStream.of(1,2,3))
												.optional(a->Optional.of(2))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
												
		List<Integer> blocked = result.block();
		
		assertThat(blocked,equalTo(Arrays.asList(3,4,5)));
		}
	}
	public <T> AnyM<T> anyM(BaseSimpleReactStream<T> stream){
		return AnyM.ofSeq(stream);
	}
	@Test
	public void doTestSimple(){
		for(int i=0;i<1000;i++){
		SimpleReactStream<Integer> result = For.anyM(anyM(BaseSimpleReactStream.of(1,2,3)))
												.optional(a->Optional.of(2))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.block(),equalTo(Arrays.asList(3,4,5)));
		}
	}
	
	
	@Test
	public void doTestLazyOptional(){
		Optional<List<Integer>> result = For.optional(lookup("empty"))
												.stream(a->LazyFutureStream.of(1,2,3))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.isPresent(),equalTo(false));
	}
	@Test
	public void doTestSimpleOptional(){
		Optional<List<Integer>> result = For.optional(lookup("empty"))
												.anyM(a->anyM(BaseSimpleReactStream.of(1,2,3)))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.isPresent(),equalTo(false));
	}
	
	@Test
	public void doTestLazyOptionalEmptyStream(){
		
		Optional<List<Integer>> result = For.optional(lookup("1"))
												.stream(a->LazyFutureStream.<Integer>of())
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		System.out.println(result);
		assertTrue(!result.isPresent());
	}
	@Test
	public void doTestSimpleOptionalEmptyStream(){
		Optional<SimpleReactStream<Integer>> result = For.optional(lookup("1"))
												.anyM(a->anyM(BaseSimpleReactStream.<Integer>of()))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.get().block().size(),equalTo(0));
	}
	
	private Optional<Integer> lookup(String key){
		if("empty".equals(key))
			return Optional.empty();
		else
			return Optional.of(1);
	}
}

