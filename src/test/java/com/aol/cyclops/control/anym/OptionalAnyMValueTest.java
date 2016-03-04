package com.aol.cyclops.control.anym;

import java.util.Optional;

import org.junit.Before;

import com.aol.cyclops.control.AnyM;

public class OptionalAnyMValueTest extends BaseAnyMValueTest {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromOptional(Optional.of(10));
        none = AnyM.fromOptional(Optional.empty());
    }
    
}
