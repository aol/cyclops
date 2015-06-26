package com.aol.cyclops.featuretoggle;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.dynamic.As;
import com.aol.cyclops.featuretoggle.FeatureToggle;


public class SwitchMonadTest {
	@Test
	public void switchTest(){
		assertThat(As.asMonad(FeatureToggle.enable("hello world")).map(o-> "2" + o).toList(),equalTo(Arrays.asList("2hello world")));
	}
	
	@Test
	public void switchDisableInStream(){
	
		List<Integer> list = As.<Stream<Integer>,Integer>asMonad(Stream.of(1,2,3))
									.bind(i ->  i==1 ? FeatureToggle.disable(i) : FeatureToggle.enable(i))
									.toList();
		
		
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}

}
