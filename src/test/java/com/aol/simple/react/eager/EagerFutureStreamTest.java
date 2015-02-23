package com.aol.simple.react.eager;

import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.util.SimpleTimer;

public class EagerFutureStreamTest {

	@Test
	public void quorum(){
		
		EagerFutureStream.parallel(array)
		assertThat(EagerFutureStream.of(1,2,3,4).limit(2).block(),equalTo(Arrays.asList(1,2)));
	}
	
	@Test
	public void timedQuorum(){
		SimpleTimer timer = new SimpleTimer();
		EagerFutureStream.of(1,2,3,4).limit(4).peek(System.out::println).map(it->)
	}
}
