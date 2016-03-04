package com.aol.cyclops.control.anym;

import org.junit.Before;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;

public class FutureWAnyMValueTest extends BaseAnyMValueTest {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromFutureW(FutureW.ofResult(10));
        none = AnyM.fromFutureW(FutureW.ofError(new RuntimeException()));
    }
    
}
