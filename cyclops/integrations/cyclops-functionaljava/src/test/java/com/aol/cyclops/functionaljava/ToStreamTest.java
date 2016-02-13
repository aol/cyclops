package com.aol.cyclops.functionaljava;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import fj.data.Stream;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStreamTest {



    @Test
    public void testToFluentIterable() throws Exception {
        assertThat(ToStream.toFluentIterable(Stream.stream(1, 2, 3, 4)).toList().size(), is(4));

    }

    @Test
    public void testToStream() throws Exception {
        assertThat(ToStream.toStream(Stream.stream(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));

    }

    @Test
    public void testToJooλ() throws Exception {
        assertThat(ToStream.toJooqLambda(Stream.stream(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));
    }

    @Test    @Ignore
    public void testToFutureStream() throws Exception {
        assertThat(ToStream.toFutureStream(Stream.stream(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));
    }



    @Test
    public void testToLazySeq() throws Exception {
        assertThat(ToStream.toLazySeq(Stream.stream(1, 2, 3, 4)).toList().size(), is(4));
    }

    
}