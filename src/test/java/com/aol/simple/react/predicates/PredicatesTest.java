package com.aol.simple.react.predicates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.aol.simple.react.predicates.Predicates.Take;

public class PredicatesTest {

	@Test
	public void testLimit1() {
		assertTrue(Predicates.limit(10).test(1));
	}
	@Test
	public void testLimit11() {
		Take t = Predicates.limit(10);
		for(int i=0;i<10;i++)
			assertTrue(t.test(i));
		assertFalse(t.test(11));
	}

	@Test
	public void testConstructor(){
		new Predicates();
	}
}
