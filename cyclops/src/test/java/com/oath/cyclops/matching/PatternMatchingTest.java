package com.oath.cyclops.matching;

import static org.junit.Assert.*;

import com.oath.cyclops.matching.Case.Case0;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class PatternMatchingTest {

    private static final Random random = new Random();

    private Long value;
    private Matching.PatternMatching<Long> sumMatching;

    @Before
    public void before() {
        value = random.nextLong();
        sumMatching = new Matching.PatternMatching<>(value);
    }

    @Test
    public void shouldReturnAny() throws Exception {
        assertEquals("any", sumMatching.with(Case("1"), Case("2"), value -> "any"));
        assertEquals("any", sumMatching.with(Case("1"), Case("2"), Case("3"), value -> "any"));
        assertEquals("any", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), value -> "any"));
        assertEquals("any", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), Case("5"), value -> "any"));
        assertEquals("any", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), Case("5"), Case("6"), value -> "any"));
        assertEquals("any", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), Case("5"), Case("6"), Case("7"), value -> "any"));
        assertEquals("any", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), Case("5"), Case("6"), Case("7"), Case("8"), value -> "any"));
    }

    @Test
    public void shouldReturnTheMatch() throws Exception {
        assertEquals("1", sumMatching.with(Case(value, "1"), Case("2"), value -> "any"));
        assertEquals("2", sumMatching.with(Case("1"), Case(value, "2"), Case("3"), value -> "any"));
        assertEquals("3", sumMatching.with(Case("1"), Case("2"), Case(value, "3"), Case("4"), value -> "any"));
        assertEquals("4", sumMatching.with(Case("1"), Case("2"), Case("3"), Case(value, "4"), Case("5"), value -> "any"));
        assertEquals("5", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), Case(value, "5"), Case(value, "6"), value -> "any"));
        assertEquals("6", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), Case("5"), Case(value, "6"), value -> "any"));
        assertEquals("7", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), Case("5"), Case("6"), Case(value, "7"), Case("8"), value -> "any"));
        assertEquals("8", sumMatching.with(Case("1"), Case("2"), Case("3"), Case("4"), Case("5"), Case("6"), Case("7"), Case(value, "8"), value -> "any"));
    }


    Case0<Long, String> Case(String out) {
        return Case(random.nextLong(), out);
    }

    Case0<Long, String> Case(Long in, String out) {
        return new Case0<>(t -> t.equals(in), (value) -> out);
    }
}
