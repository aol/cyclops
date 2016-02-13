package com.aol.cyclops.lambda.functions;

import org.junit.Test;

import com.aol.cyclops.util.function.*;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.aol.cyclops.util.function.PartialApplicator.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PartialApplicatorTest {

    @Test
    public void testArity3() {
        TriFunction<String, String, String, String> concat = (a, b, c) -> a + b + c;

        Function<String, String> concatStrings = partial3("Hello", " World", concat);
        assertThat(concatStrings.apply("!!!"), equalTo("Hello World!!!"));

        BiFunction<String, String, String> concatMoreStrings = partial3("How", concat);
        assertThat(concatMoreStrings.apply(" are", " you?"), equalTo("How are you?"));
    }

    @Test
    public void testArity4() {
        QuadFunction<Integer, Integer, Integer, Integer, String> average = (a, b, c, d) -> ((a + b + c + d) / 4) + "";

        Function<Integer, String> getAverageP1 = partial4(10, 10, 10, average);
        assertThat(getAverageP1.apply(10), equalTo("10"));

        BiFunction<Integer, Integer, String> getAverageP2 = partial4(10, 10, average);
        assertThat(getAverageP2.apply(10, 10), equalTo("10"));

        TriFunction<Integer, Integer, Integer, String> getAverageP3 = partial4(10, average);
        assertThat(getAverageP3.apply(10, 10, 10), equalTo("10"));
    }

    @Test
    public void testArity5() {
        QuintFunction<Integer, Integer, Integer, Integer, Integer, Integer> sum = (a, b, c, d, e) -> a + b + c + d + e;

        Function<Integer, Integer> getSumP1 = partial5(4, 5, 6, 7, sum);
        assertThat(getSumP1.apply(10), equalTo(32));

        BiFunction<Integer, Integer, Integer> getSumP2 = partial5(5, 20, 50, sum);
        assertThat(getSumP2.apply(10, 20), equalTo(105));

        TriFunction<Integer, Integer, Integer, Integer> getSumP3 = partial5(5, 20, sum);
        assertThat(getSumP3.apply(30, 10, 50), equalTo(115));

        QuadFunction<Integer, Integer, Integer, Integer, Integer> getSumP4 = partial5(5, sum);
        assertThat(getSumP4.apply(10, 20, 30, 40), equalTo(105));


    }

    @Test
    public void testArity6() {
        HexFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer> sum = (a, b, c, d, e, f) -> a + b + c + d + e + f;

        Function<Integer, Integer> getSumP1 = partial6(4, 5, 6, 7, 8, sum);
        assertThat(getSumP1.apply(10), equalTo(40));

        BiFunction<Integer, Integer, Integer> getSumP2 = partial6(5, 20, 50, 60, sum);
        assertThat(getSumP2.apply(10, 20), equalTo(165));

        TriFunction<Integer, Integer, Integer, Integer> getSumP3 = partial6(5, 20, 20, sum);
        assertThat(getSumP3.apply(30, 10, 50), equalTo(135));

        QuadFunction<Integer, Integer, Integer, Integer, Integer> getSumP4 = partial6(5, 30, sum);
        assertThat(getSumP4.apply(10, 20, 30, 40), equalTo(135));

        QuintFunction<Integer, Integer, Integer, Integer, Integer, Integer> getSumP5 = partial6(30, sum);
        assertThat(getSumP5.apply(10, 20, 30, 40, 50), equalTo(180));
    }

    @Test
    public void testArity7() {
        HeptFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> sum = (a, b, c, d, e, f, g) -> a + b + c + d + e + f + g;

        Function<Integer, Integer> getSumP1 = partial7(4, 5, 6, 7, 8, 9, sum);
        assertThat(getSumP1.apply(10), equalTo(49));

        BiFunction<Integer, Integer, Integer> getSumP2 = partial7(5, 20, 50, 60, 50, sum);
        assertThat(getSumP2.apply(10, 20), equalTo(215));

        TriFunction<Integer, Integer, Integer, Integer> getSumP3 = partial7(5, 20, 20, 20, sum);
        assertThat(getSumP3.apply(30, 10, 50), equalTo(155));

        QuadFunction<Integer, Integer, Integer, Integer, Integer> getSumP4 = partial7(5, 30, 40, sum);
        assertThat(getSumP4.apply(10, 20, 30, 40), equalTo(175));

        QuintFunction<Integer, Integer, Integer, Integer, Integer, Integer> getSumP5 = partial7(30, 45, sum);
        assertThat(getSumP5.apply(10, 20, 30, 40, 50), equalTo(225));

        HexFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer> getSumP6 = partial7(30, sum);
        assertThat(getSumP6.apply(10, 20, 30, 40, 50, 60), equalTo(240));
    }

    @Test
    public void testArity8() {
        OctFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> sum = (a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h;
        Function<Integer, Integer> getSumP1 = partial8(4, 5, 6, 7, 8, 9, 10, sum);
        assertThat(getSumP1.apply(10), equalTo(59));

        BiFunction<Integer,Integer,Integer> getSumP2 = partial8(5, 20, 50, 60, 50, 40, sum);
        assertThat(getSumP2.apply(10,20), equalTo(255));

        TriFunction<Integer,Integer,Integer,Integer> getSumP3 = partial8(5, 20, 20, 20, 10, sum);
        assertThat(getSumP3.apply(30,10,50), equalTo(165));

        QuadFunction<Integer,Integer,Integer,Integer,Integer> getSumP4 = partial8(5, 30, 40, 70 , sum);
        assertThat(getSumP4.apply(10,20,30,40), equalTo(245));

        QuintFunction<Integer,Integer,Integer,Integer,Integer,Integer> getSumP5 = partial8(30, 45,65 , sum);
        assertThat(getSumP5.apply(10,20,30,40,50), equalTo(290));

        HexFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer> getSumP6 = partial8(30, 90, sum);
        assertThat(getSumP6.apply(10,20,30,40,50,60), equalTo(330));

        HeptFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> getSumP7 = partial8(30, sum);
        assertThat(getSumP7.apply(10,20,30,40,50,60,70), equalTo(310));
    }

}
