package com.aol.cyclops.featuretoggle;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FeatureToggle;


public class SwitchMonadTest {
	@Test
	public void switchTest(){
		assertThat(AnyM.ofValue(FeatureToggle.enable("hello world"))
						.map(o-> "2" + o)
						.stream()
						.toList(),equalTo(Arrays.asList("2hello world")));
	}
	
	@Test
	public void switchDisableInStream(){
	
		List<Integer> list = AnyM.fromStream(Stream.of(1,2,3))
									.<Integer>bind(i ->  i==1 ? FeatureToggle.disable(i) : FeatureToggle.enable(i))
									.stream()
									.toList();
		
		
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}

}
