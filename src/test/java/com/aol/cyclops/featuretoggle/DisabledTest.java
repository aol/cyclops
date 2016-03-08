package com.aol.cyclops.featuretoggle;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.FeatureToggle;
import com.aol.cyclops.control.FeatureToggle.Disabled;

public class DisabledTest {

	Disabled<Integer> disabled;
	Object value = null;
	@Before
	public void setup(){
		value = null;
		disabled = FeatureToggle.disable(100);
	}
	
	@Test
	public void testPeek(){
		disabled.peek(it -> value=it);
		assertThat(value,is(nullValue()));
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
		assertThat(disabled.filter(i->i<200).map(i->i+1).enable().get(),is(100));
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
		assertThat(disabled.flatMap(i->FeatureToggle.disable(100)).isEnabled(),is(false));
	}
	
	@Test
	public void testMap(){
		assertThat(disabled.map(i->i+1).enable().get(),is(100));
	}
	@Test
	public void testEnabled() {
		assertThat(disabled.enable().get(),is(100));
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
