package com.aol.cyclops.lambda.tuple.matching;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.aol.cyclops.lambda.tuple.PowerTuples;
import com.aol.cyclops.matcher.builders.CheckValues;


public class MatchingTest {

	@Test
	public void test(){
		assertThat(PowerTuples.tuple(1,2,3).matches(
									c->c.hasValues(1,2,3).then(i->"hello"),
										c->c.hasValues(4,5,6).then(i->"goodbye")
					),equalTo("hello"));
		
	}

	
}
