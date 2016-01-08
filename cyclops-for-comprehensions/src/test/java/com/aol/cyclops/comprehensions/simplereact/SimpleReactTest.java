package com.aol.cyclops.comprehensions.simplereact;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.simple.react.stream.traits.LazyFutureStream;
public class SimpleReactTest {
	
	@Test
	public void doTestLazyOptional(){
		Optional<List<Integer>> result = Do.add(lookup("empty"))
												.addStream(()->LazyFutureStream.of(1,2,3))
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		assertThat(result.isPresent(),equalTo(false));
	}
	
	
	@Test
	public void doTestLazyOptionalEmptyStream(){
		Optional<Integer> result = Do.add(lookup("1"))
												.addStream(()->LazyFutureStream.<Integer>of())
												.yield((Integer a) -> (Integer b) -> a+b)
												.unwrap();
		System.out.println(result);
		assertThat(result.isPresent(),equalTo(false));
	}
	
	
	private Optional<Integer> lookup(String key){
		if("empty".equals(key))
			return Optional.empty();
		else
			return Optional.of(1);
	}

}
