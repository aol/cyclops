package com.aol.cyclops.control.anym;

import cyclops.monads.AnyM;
import cyclops.control.Maybe;
import cyclops.box.Mutable;
import cyclops.monads.Witness;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class MaybeAnyMValueTest extends BaseAnyMValueTest<Witness.maybe> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromMaybe(Maybe.of(10));
        none = AnyM.fromMaybe(Maybe.none());
    }
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));
        assertNull(capture.get());
        
        just.get();
        assertThat(capture.get(),equalTo(10));
    }
}
