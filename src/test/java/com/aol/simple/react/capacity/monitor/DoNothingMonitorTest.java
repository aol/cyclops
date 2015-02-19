package com.aol.simple.react.capacity.monitor;

import static org.junit.Assert.*;

import org.junit.Test;

public class DoNothingMonitorTest {

	@Test
	public void testAccept() {
		DoNothingMonitor monitor = new DoNothingMonitor();
		monitor.accept(null); //no npe, nothing happens
	}

}
