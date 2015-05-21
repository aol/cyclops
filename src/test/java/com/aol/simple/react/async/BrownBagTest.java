package com.aol.simple.react.async;

import java.util.stream.Stream;

import org.junit.Test;

public class BrownBagTest {

	@Test
	public void presentation(){
		Stream.of(6,5,1,2).map(e->e*100)
						  .filter(e->e<551)
						  .forEach(System.out::println);
	}
}
