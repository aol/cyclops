package com.aol.cyclops.util.function;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;

import static com.aol.cyclops.util.function.Predicates.eqv;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
public class PredicatesTest {

    @Test
    public void testEqv() {
        assertThat(Stream.of(Maybe.of(2)).filter(eqv(Eval.now(2))).collect(Collectors.toList()).get(0),equalTo(Maybe.of(2)));
        
    }
    @Test
    public void testEqvFalse() {
        assertThat(Stream.of(Maybe.of(3)).filter(eqv(Maybe.of(2))).collect(Collectors.toList()).size(),equalTo(0));
        
    }
    @Test
    public void testEqvNonValue() {
        assertThat(Stream.of(2).filter(eqv(Maybe.of(2))).collect(Collectors.toList()).get(0),equalTo(2));
        
    }
    @Test
    public void testEqvNone() {
        assertTrue(eqv(Maybe.none()).test(null));
        
    }
    @Test
    public void testEqvNull() {
        assertTrue(eqv(null).test(null));
        
    }

}
