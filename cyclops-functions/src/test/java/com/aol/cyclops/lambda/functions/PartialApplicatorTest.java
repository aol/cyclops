package com.aol.cyclops.lambda.functions;

import com.aol.cyclops.functions.*;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.aol.cyclops.functions.PartialApplicator.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PartialApplicatorTest {

    @Test
    public void testArity3() {
        TriFunction<String, String, String, String> concat = (a, b, c) -> a + b + c;
        Function<String, String> concatStrings = partial3("Hello", " World", concat);
        assertThat(concatStrings.apply("!!!"), equalTo("Hello World!!!"));
    }

    @Test
    public void testArity4() {
        QuadFunction<Integer, Integer, Integer, Integer, String> average = (a, b, c, d) -> ((a + b + c + d) / 4) + "";
        BiFunction<Integer, Integer, String> getAveragePartial = partial4(10, 10, average);
        assertThat(getAveragePartial.apply(10, 10), equalTo("10"));
    }

    @Test
    public void testArity5() {
        QuintFunction<String, String, String, String, String, String> concat = (a, b, c, d, e) -> a + b + c + d + e;
        TriFunction<String, String, String, String> concatString = partial5("Life,", " will", concat);
        assertThat(concatString.apply(" find", " a", " way"), equalTo("Life, will find a way"));

    }

    @Test
    public void testArity6() {
        HexFunction<String, String, String, String, String, String, String> concat = (a, b, c, d, e, f) -> a + b + c + d + e + f;
        QuadFunction<String, String, String, String, String> concatString = partial6("We\'re", " on", concat);
        assertThat(concatString.apply(" a", " mission", " from", " God"), equalTo("We\'re on a mission from God"));
    }

    @Test
    public void testArity7() {
        HeptFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> sum = (a, b, c, d, e, f, g) -> a + b + c + d + e + f + g;
        QuintFunction<Integer, Integer, Integer, Integer, Integer, Integer> sumAll = partial7(1, 2, sum);
        Integer value = sumAll.apply(3, 4, 5, 6, 7);
        assertThat(value, equalTo(28));
    }

    @Test
    public void testArity8() {
        OctFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> sum = (a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h;
        HexFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer> sumAll = partial8(1, 2, sum);
        Integer value = sumAll.apply(3, 4, 5, 6, 7, 8);
        assertThat(value, equalTo(36));
    }

}
