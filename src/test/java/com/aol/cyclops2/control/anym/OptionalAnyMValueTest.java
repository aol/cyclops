package com.aol.cyclops2.control.anym;

import java.util.Optional;

import cyclops.control.anym.Witness;
import org.junit.Before;

import cyclops.control.anym.AnyM;

public class OptionalAnyMValueTest extends BaseAnyMValueTest<Witness.optional> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromOptional(Optional.of(10));
        none = AnyM.fromOptional(Optional.empty());
    }
    
}
