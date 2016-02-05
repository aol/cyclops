package com.aol.cyclops.lambda.monads;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;

public class UnitTest {
	@Test
	public void unitOptional() {
	    AnyM<Integer> empty = AnyM.ofMonad(Optional.empty());
	    AnyM<Integer> unit = empty.unit(1);
	    Optional<Integer> unwrapped = unit.unwrap();
	    assertEquals(Integer.valueOf(1), unwrapped.get());
	}

	
}
