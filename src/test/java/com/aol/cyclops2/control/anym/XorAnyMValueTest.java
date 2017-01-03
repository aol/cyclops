package com.aol.cyclops2.control.anym;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import cyclops.monads.Witness;
import org.junit.Before;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.control.Xor;
import cyclops.box.Mutable;

public class XorAnyMValueTest extends BaseAnyMValueTest<Witness.xor> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromXor(Xor.primary(10));
        none = AnyM.fromXor(Xor.secondary(null));
    }
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));
        
        
        just.get();
        assertThat(capture.get(),equalTo(10));
    }
}
