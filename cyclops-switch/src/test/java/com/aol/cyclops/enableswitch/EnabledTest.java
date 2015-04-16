package com.aol.cyclops.enableswitch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class EnabledTest {

	Enabled<Integer> enabled;
	Object value = null;
	@Before
	public void setup(){
		value = null;
		enabled = Switch.enable(100);
	}
	
	@Test
	public void testFlatten(){
		Enabled<Enabled<Disabled<Integer>>> nested= Switch.enable(Switch.enable(Switch.disable(100)));
		Optional<Switch<Integer>> o = nested.<Integer>flatten();
		System.out.println(o.get());
		Switch<Integer> flat = nested.<Integer>flatten().get();
		assertThat(flat.get(),is(100));
	}
	@Test
	public void testFlattenSematics(){
		Enabled<Enabled<Disabled<Integer>>> nested= Switch.enable(Switch.enable(Switch.disable(100)));
		Optional<Switch<Integer>> o = nested.<Integer>flatten();
		System.out.println(o.get());
		Switch<Integer> flat = nested.<Integer>flatten().get();
		assertThat(flat,instanceOf(Enabled.class));
	}
	
	@Test
	public void testPeek(){
		enabled.peek(it -> value=it);
		assertThat(value,is(100));
	}
	
	@Test
	public void testFlip(){
		assertThat(enabled.flip().isDisabled(),is(true));
	}
	@Test
	public void testFlipTwice(){
		assertThat(enabled.flip().flip().isDisabled(),is(false));
	}
	@Test
	public void testFilter(){
		assertThat(enabled.filter(i->i<200).map(i->i+1).get(),is(101));
	}
	@Test
	public void testFilterTrue(){
		assertThat(enabled.filter(i->i<200).map(i->i+1).isEnabled(),is(true));
	}
	@Test
	public void testFilterFalse(){
		assertThat(enabled.filter(i->i>200).map(i->i+1).isDisabled() ,is(true));
	}
	
	@Test
	public void testForEach(){
		enabled.forEach(i->value=i*100);
		assertThat(value,is(100*100));
	}
	@Test
	public void testFlatMap(){
		assertThat(enabled.flatMap(i->Switch.disable(100)).isEnabled(),is(false));
	}
	
	@Test
	public void testMap(){
		assertThat(enabled.map(i->i+1).get(),is(101));
	}
	@Test
	public void testEnabled() {
		assertThat(enabled.get(),is(100));
	}

	@Test
	public void testIsEnabled() {
		assertTrue(enabled.isEnabled());
	}

	@Test
	public void testIsDisabled() {
		assertFalse(enabled.isDisabled());
	}

}
