package com.aol.cyclops.enableswitch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DisabledTest {

	Disabled<Integer> disabled;
	Object value = null;
	@Before
	public void setup(){
		value = null;
		disabled = Switch.disable(100);
	}
	
	@Test
	public void testPeek(){
		disabled.peek(it -> value=it);
		assertThat(value,is(100));
	}
	
	@Test
	public void testFlip(){
		assertThat(disabled.flip().isDisabled(),is(false));
	}
	@Test
	public void testFlipTwice(){
		assertThat(disabled.flip().flip().isDisabled(),is(true));
	}
	@Test
	public void testFilter(){
		assertThat(disabled.filter(i->i<200).map(i->i+1).get(),is(100));
	}
	@Test
	public void testFilterTrue(){
		assertThat(disabled.filter(i->i<200).map(i->i+1).isEnabled(),is(false));
	}
	@Test
	public void testFilterFalse(){
		assertThat(disabled.filter(i->i>200).map(i->i+1).isDisabled() ,is(true));
	}
	
	@Test
	public void testForEach(){
		disabled.forEach(i->value=i*100);
		assertThat(value,is(nullValue()));
	}
	@Test
	public void testFlatMap(){
		assertThat(disabled.flatMap(i->Switch.disable(100)).isEnabled(),is(false));
	}
	
	@Test
	public void testMap(){
		assertThat(disabled.map(i->i+1).get(),is(100));
	}
	@Test
	public void testEnabled() {
		assertThat(disabled.get(),is(100));
	}

	@Test
	public void testIsEnabled() {
		assertFalse(disabled.isEnabled());
	}

	@Test
	public void testIsDisabled() {
		assertTrue(disabled.isDisabled());
	}

}
