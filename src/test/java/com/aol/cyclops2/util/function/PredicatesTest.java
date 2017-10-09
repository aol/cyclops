package com.aol.cyclops2.util.function;

import static cyclops.function.Predicates.anyOf;
import static cyclops.function.Predicates.eq;
import static cyclops.function.Predicates.eqv;
import static cyclops.function.Predicates.eqv2;
import static cyclops.function.Predicates.greaterThan;
import static cyclops.function.Predicates.greaterThanOrEquals;
import static cyclops.function.Predicates.in;
import static cyclops.function.Predicates.lessThan;
import static cyclops.function.Predicates.lessThanOrEquals;
import static cyclops.function.Predicates.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.function.Predicates;
import org.junit.Test;

import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
public class PredicatesTest {

    @Test
    public void testSampleTime(){
        assertThat(Stream.of(1,2,3,4).filter(Predicates.sample(1, TimeUnit.MILLISECONDS)).collect(Collectors.toList()).size(),equalTo(1));
        assertThat(Stream.of(1,2,3,4).filter(Predicates.sample(1, TimeUnit.NANOSECONDS)).collect(Collectors.toList()).size(),equalTo(4));

    }

    @Test
    public void testSample(){
        assertThat(Stream.of(1,2,3,4).filter(Predicates.sample(2)).collect(Collectors.toList()).size(),equalTo(2));
        assertThat(Stream.of(1,2,3,4).filter(Predicates.sample(4)).collect(Collectors.toList()).size(),equalTo(1));
    }

    @Test
    public void testFilter(){
        
        ReactiveSeq.of(1,2,3).filter(anyOf(not(in(2,3,4)),in(1,10,20)));
        ReactiveSeq.of(1,2,3).filter(anyOf(not(greaterThan(2)),in(1,10,20)));
        ReactiveSeq.of(1,2,3).filter(anyOf(not(greaterThanOrEquals(2)),in(1,10,20)));
        ReactiveSeq.of(1,2,3).filter(anyOf(not(lessThan(2)),in(1,10,20)));
        ReactiveSeq.of(1,2,3).filter(anyOf(not(lessThanOrEquals(2)),in(1,10,20)));
        ReactiveSeq.of(1,2,3).filter(anyOf(not(eq(2)),in(1,10,20)));
        Stream.of(Maybe.of(2)).filter(eqv(Eval.now(2)));
        
        
    }
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
        assertThat(Stream.of(2).filter(eqv2(Maybe.of(2))).collect(Collectors.toList()).get(0),equalTo(2));
        
    }
    @Test
    public void testEqvNone() {
        assertTrue(eqv(Maybe.nothing()).test(null));
        
    }
    @Test
    public void testEqvNull() {
        assertTrue(eqv(null).test(null));
        
    }
    @Test
    public void hasItems(){
        assertTrue(Predicates.hasItems(Arrays.asList(1,2,3)).test(Arrays.asList(10,1,23,2,3,4,5,6)));
    }
    @Test
    public void startsWith(){
        assertFalse(Predicates.startsWith(1,2,3).test(Arrays.asList(10,1,23,2,3,4,5,6)));
    }
    @Test
    public void endsWith(){
        assertFalse(Predicates.startsWith(1,2,3).test(Arrays.asList(10,1,23,2,3,4,5,6)));
    }
    @Test
    public void startsWithTrue(){
        assertTrue(Predicates.startsWith(1,2,3).test(Arrays.asList(1,2,3,10,1,23,2,3,4,5,6)));
    }
    @Test
    public void endsWithTrue(){
        assertTrue(Predicates.endsWith(1,2,3).test(Arrays.asList(10,1,23,2,3,4,5,6,1,2,3)));
    }

}
