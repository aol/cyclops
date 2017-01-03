package com.aol.cyclops2.control.anym;

import java.util.Optional;

import cyclops.monads.Witness;
import org.junit.Before;

import cyclops.monads.AnyM;

public class OptionalAnyMValueTest extends BaseAnyMValueTest<Witness.optional> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromOptional(Optional.of(10));
        none = AnyM.fromOptional(Optional.empty());
    }
    
}
