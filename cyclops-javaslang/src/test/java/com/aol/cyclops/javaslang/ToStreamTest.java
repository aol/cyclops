package com.aol.cyclops.javaslang;

import javaslang.collection.Stream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStreamTest {



    @Test
    public void testToFluentIterable() throws Exception {
        assertThat(ToStream.toFluentIterable(Stream.of(1, 2, 3, 4)).toList().size(), is(4));

    }

    @Test
    public void testToStream() throws Exception {
        assertThat(ToStream.toStream(Stream.of(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));

    }

    @Test
    public void testToJooλ() throws Exception {
        assertThat(ToStream.toJooλ(Stream.of(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));
    }

    @Test    @Ignore
    public void testToFutureStream() throws Exception {
        assertThat(ToStream.toFutureStream(Stream.of(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));
    }



    @Test
    public void testToLazySeq() throws Exception {
        assertThat(ToStream.toLazySeq(Stream.of(1, 2, 3, 4)).toList().size(), is(4));
    }

    @Test
    public void testToFunctionalJavaStream() throws Exception {
        assertThat(ToStream.toFunctionalJavaStream(Stream.of(1, 2, 3, 4)).toList().length(), is(4));
    }
}