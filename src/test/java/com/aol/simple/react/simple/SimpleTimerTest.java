package com.aol.simple.react.simple;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.util.SimpleTimer;

public class SimpleTimerTest {

	SimpleTimer timer;

	@Before
	public void setUp() throws Exception {
		timer = new SimpleTimer();
	}

	@Test
	public void testNonZero() {
		assertThat(timer.getElapsedNanoseconds(), is(greaterThanOrEqualTo(0l)));
	}
	
	@Test
	public void testContinueCounting() {
		Long elapsed = timer.getElapsedNanoseconds();
		assertThat(timer.getElapsedNanoseconds(), is(greaterThanOrEqualTo(elapsed)));
		
	}
	
	@Test
	public void testLessThanFifty() {
		assertThat(timer.getElapsedNanoseconds(), is(lessThan(50l*1000l)));
	}

}