package com.aol.cyclops.control.anym.transformers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.anym.BaseAnyMValueTest;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.data.Mutable;

public class TryTAnyMValueTest extends BaseAnyMValueTest {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromTryTValue(TryT.fromValue(Maybe.just(Try.success(10))));
        
        none = AnyM.fromTryTValue(TryT.emptyOptional());
    }
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));
       
        
        just.get();
        assertThat(capture.get(),equalTo(10));
    }
    @Test
    public void testFlatMap() {
        
        assertThat(just.flatMap(i->AnyM.ofNullable(i+5)).toMaybe(),equalTo(Maybe.of(15)));
       
    }
    @Test
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(just.map(i->i+5).get(),equalTo(Maybe.of(15).get()));
        
    }

}
