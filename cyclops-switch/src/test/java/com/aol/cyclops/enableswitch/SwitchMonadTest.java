package com.aol.cyclops.enableswitch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.dynamic.As;


public class SwitchMonadTest {
	@Test
	public void switchTest(){
		assertThat(As.asMonad(Switch.enable("hello world")).map(o-> "2" + o).toList(),equalTo(Arrays.asList("2hello world")));
	}
	
	@Test
	public void switchDisableInStream(){
	
		List<Integer> list = As.<Stream<Integer>,Integer>asMonad(Stream.of(1,2,3))
									.bind(i ->  i==1 ? Switch.disable(i) : Switch.enable(i))
									.toList();
		
		
		assertThat(list,equalTo(Arrays.asList(4,5)));
	}

}
