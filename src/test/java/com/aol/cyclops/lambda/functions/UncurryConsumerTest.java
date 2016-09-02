package com.aol.cyclops.lambda.functions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.util.function.UncurryConsumer;

public class UncurryConsumerTest {
	Integer value;
	
	@Before
	public void setup(){
		value = null;
	}
	@Test
	public void testUncurry2() {
		UncurryConsumer.uncurryC2((Integer a)->(Integer b) -> value = a+b ).accept(2,3);
		assertThat(value,equalTo(5));
	}

	@Test
	public void testUncurry3() {
		UncurryConsumer.uncurryC3((Integer a)->(Integer b) -> (Integer c)-> value = a+b+c ).accept(2,3,4);
		assertThat(value,equalTo(9));
	}

	@Test
	public void testUncurry4() {
		UncurryConsumer.uncurryC4((Integer a)->(Integer b) -> (Integer c)->  (Integer d)->value = a+b+c+d )
		.accept(2,3,4,5);
		assertThat(value,equalTo(14));
	}

	@Test
	public void testUncurry5() {
		UncurryConsumer.uncurryC5((Integer a)->(Integer b) -> (Integer c)->  (Integer d)->(Integer e)->value = 
				a+b+c+d+e )
		.accept(2,3,4,5,6);
		assertThat(value,equalTo(20));
	}

}
