package com.aol.cyclops.featuretoggle;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.dynamic.As;
import com.aol.cyclops.lambda.monads.AnyMonads;


public class SwitchMonadTest {
	@Test
	public void switchTest(){
		assertThat(AnyMonads.anyM(FeatureToggle.enable("hello world"))
						.map(o-> "2" + o)
						.asSequence()
						.toList(),equalTo(Arrays.asList("2hello world")));
	}
	
	@Test
	public void switchDisableInStream(){
	
		List<Integer> list = AnyMonads.anyM(Stream.of(1,2,3))
									.<Integer>bind(i ->  i==1 ? FeatureToggle.disable(i) : FeatureToggle.enable(i))
									.asSequence()
									.toList();
		
		
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}

}
