package com.aol.cyclops.javaslang;

import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class CollectorsTest {

    @Test
    public void testToList(){
        assertThat(Stream.of(1, 2, 3, 4).collect(Collectors.toList())
                .map(i -> i * 100)
                .reduce((acc, next) -> acc + next), is(1000));
    }

}