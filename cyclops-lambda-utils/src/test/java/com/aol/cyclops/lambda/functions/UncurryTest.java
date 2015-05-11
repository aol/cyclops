package com.aol.cyclops.lambda.functions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops.comprehensions.functions.Uncurry;

public class UncurryTest {

	@Test
	public void testUncurry2() {
		assertThat(Uncurry.uncurry2((Integer a)->(Integer b)->a+b).apply(1,2),equalTo(3));
	}

	@Test
	public void testUncurry3() {
		assertThat(Uncurry.uncurry3((Integer a)->(Integer b)->(Integer c)->a+b+c)
								.apply(1,2,3),equalTo(6));
	}

	@Test
	public void testUncurry4() {
		assertThat(Uncurry.uncurry4((Integer a)->(Integer b)->(Integer c)->(Integer d)->a+b+c+d)
				.apply(1,2,3,4),equalTo(10));
	}

	@Test
	public void testUncurry5() {
		assertThat(Uncurry.uncurry5((Integer a)->(Integer b)->(Integer c)->(Integer d)-> (Integer e)->
						a+b+c+d+e)
				.apply(1,2,3,4,5),equalTo(15));
	}

	@Test
	public void testUncurry6() {
		assertThat(Uncurry.uncurry6((Integer a)->(Integer b)->(Integer c)->(Integer d)-> (Integer e)->
				(Integer f) -> a+b+c+d+e +f)
						.apply(1,2,3,4,5,6),equalTo(21));
	}

	@Test
	public void testUncurry7() {
		assertThat(Uncurry.uncurry7((Integer a)->(Integer b)->(Integer c)->(Integer d)-> (Integer e)->
		(Integer f) -> (Integer g) -> a+b+c+d+e+f+g)
				.apply(1,2,3,4,5,6,7),equalTo(28));
	}
	@Test
	public void testUncurry8() {
		assertThat(Uncurry.uncurry8((Integer a)->(Integer b)->(Integer c)->(Integer d)-> (Integer e)->
		(Integer f) -> (Integer g) ->(Integer h) -> a+b+c+d+e+f+g+h)
				.apply(1,2,3,4,5,6,7,8),equalTo(36));
	}

}
