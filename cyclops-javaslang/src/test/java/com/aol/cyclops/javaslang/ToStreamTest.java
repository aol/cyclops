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
        assertThat(ToStream.toFluentIterable(Stream.ofAll(1, 2, 3, 4)).toList().size(), is(4));

    }

    @Test
    public void testToStream() throws Exception {
        assertThat(ToStream.toStream(Stream.ofAll(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));

    }
    @Test
    public void testToSequenceM() throws Exception {
        assertThat(ToStream.toSequenceM(Stream.ofAll(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));
    }

    @Test
    public void testToJooqLambda() throws Exception {
        assertThat(ToStream.toJooqLambda(Stream.ofAll(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));
    }

    @Test    @Ignore
    public void testToFutureStream() throws Exception {
        assertThat(ToStream.toFutureStream(Stream.ofAll(1, 2, 3, 4)).collect(Collectors.toList()).size(), is(4));
    }



    @Test
    public void testToLazySeq() throws Exception {
        assertThat(ToStream.toLazySeq(Stream.ofAll(1, 2, 3, 4)).toList().size(), is(4));
    }

    @Test
    public void testToFunctionalJavaStream() throws Exception {
        assertThat(ToStream.toFunctionalJavaStream(Stream.ofAll(1, 2, 3, 4)).toList().length(), is(4));
    }
}