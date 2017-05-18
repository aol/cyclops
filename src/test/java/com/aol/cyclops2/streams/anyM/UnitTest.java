package com.aol.cyclops2.streams.anyM;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import cyclops.collections.mutable.ListX;
import cyclops.monads.Witness;
import org.junit.Test;

import cyclops.monads.AnyM;

public class UnitTest {
	@Test
	public void unitOptional() {
	    AnyM<Witness.optional,Integer> empty = AnyM.fromOptional(Optional.empty());
	    AnyM<Witness.optional,Integer> unit = empty.unit(1);
	    Optional<Integer> unwrapped = unit.unwrap();
	    assertEquals(Integer.valueOf(1), unwrapped.get());
	}

	@Test
	public void unitList() {
	    AnyM<Witness.list,Integer> empty = AnyM.fromList(ListX.empty());
	    AnyM<Witness.list,Integer> unit = empty.unit(1);
	    List<Integer> unwrapped = unit.stream().toList();
	    assertEquals(Integer.valueOf(1), unwrapped.get(0));
	}
}
