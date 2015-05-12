package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;

import lombok.AllArgsConstructor;

import org.junit.Test;
public class TupleWrappingTest {

	@Test
	public void cons(){
		Three three = new Three(1,"hello",new Date());
		assertThat(three.a,equalTo(Tuple3.ofTuple(three).v1()));
	}
	@AllArgsConstructor
	static class Three{
		int a;
		String b;
		Date c;
	}
}
