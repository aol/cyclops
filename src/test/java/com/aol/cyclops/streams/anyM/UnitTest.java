package com.aol.cyclops.streams.anyM;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aol.cyclops.internal.monads.AnyMonads;
import com.aol.cyclops.control.AnyM;

public class UnitTest {
	@Test
	public void unitOptional() {
	    AnyM<Integer> empty = AnyM.fromOptional(Optional.empty());
	    AnyM<Integer> unit = empty.unit(1);
	    Optional<Integer> unwrapped = unit.unwrap();
	    assertEquals(Integer.valueOf(1), unwrapped.get());
	}

	@Test
	public void unitList() {
	    AnyM<Integer> empty = AnyM.fromIterable(Collections.emptyList());
	    AnyM<Integer> unit = empty.unit(1);
	    List<Integer> unwrapped = unit.asSequence().toList();
	    assertEquals(Integer.valueOf(1), unwrapped.get(0));
	}
}
