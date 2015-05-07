package com.aol.cyclops.comprehensions;

import org.junit.Test;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;

public class PCollectionTest {

	@Test
	public void test(){
		PStack s = ConsPStack.singleton("org");
		
		System.out.println(s.plus("hello").plus("world"));
		System.out.println(s.plus("hello2").minus("hello2"));
	}
}
