package com.aol.cyclops.lambda.api;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;



public class AsFunctorTest {


	@Test
	public void asFunctor(){
		Object mappedStream = AsFunctor.<Integer>asFunctor(Stream.of(1,2,3)).map( i->i*2).unwrap();
		assertThat(((Stream)mappedStream).collect(Collectors.toList()),equalTo(Arrays.asList(2,4,6)));
	}

}
