package com.aol.simple.react;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SimpleTimerTest {

	SimpleTimer timer;

	@Before
	public void setUp() throws Exception {
		timer = new SimpleTimer();
	}

	@Test
	public void testNonZero() {
		assertThat(timer.getElapsedMilliseconds(), is(greaterThanOrEqualTo(0l)));
	}
	
	@Test
	public void testContinueCounting() {
		Long elapsed = timer.getElapsedMilliseconds();
		assertThat(timer.getElapsedMilliseconds(), is(greaterThanOrEqualTo(elapsed)));
		
	}
	
	@Test
	public void testLessThanFifty() {
		assertThat(timer.getElapsedMilliseconds(), is(lessThan(50l)));
	}

}