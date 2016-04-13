package com.aol.cyclops.control.anym;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.Mutable;

import reactor.core.publisher.Mono;

public class MonoAnyMValueTest extends BaseAnyMValueTest {
    @Before
    public void setUp() throws Exception {
        just = AnyM.ofValue(Mono.just(10));
        none = AnyM.ofValue(Mono.empty());
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
