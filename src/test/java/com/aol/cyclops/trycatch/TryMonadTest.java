package com.aol.cyclops.trycatch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Try;



public class TryMonadTest {

	@Test
	public void tryTest(){
		assertThat(AnyM.fromTry(Try.withCatch(()->"hello world"))
								.map(o-> "2" + o)
								.stream()
								.toList(),equalTo(Arrays.asList("2hello world")));
	}
	

}
