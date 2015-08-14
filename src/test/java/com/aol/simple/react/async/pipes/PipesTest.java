package com.aol.simple.react.async.pipes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.async.Queue;

public class PipesTest {
	@Before
	public void setup() {
		Pipes.clear();
	}
	@Test
	public void testGetAbsent() {
		Pipes.clear();
		assertFalse(Pipes.get("hello").isPresent());
	}
	@Test
	public void testGetPresent() {
		Pipes.register("hello", new Queue());
		assertTrue(Pipes.get("hello").isPresent());
	}

	
}

