package com.aol.cyclops.lambda.tuple.matching;

import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import com.aol.cyclops.lambda.tuple.PowerTuples;
import com.aol.cyclops.matcher.builders._MembersMatchBuilder;

public class MatchingTest {

	@Test
	public void test(){
		assertThat(PowerTuples.tuple(1,2,3).matchValues(c -> cases(c)   ),equalTo("hello"));
		
	}

	private <I,T> _MembersMatchBuilder<Object, T> cases(_MembersMatchBuilder<I, T> c) {
		return c.with(1,2,3).then(i->"hello")
				.with(4,5,6).then(i->"goodbye");
	}
}
