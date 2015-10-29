package com.aol.cyclops.guava;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.FluentIterable;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStreamTest {



    @Test
    public void testToFJStream() throws Exception {
        assertThat(ToStream.toFJStream(FluentIterable.of(new Integer[]{1, 2, 3, 4})).toList().length(), is(4));

    }

    @Test
    public void testToStream() throws Exception {
        assertThat(ToStream.toStream(FluentIterable.of(new Integer[]{1, 2, 3, 4})).collect(Collectors.toList()).size(), is(4));

    }
    @Test
    public void testToSequenceM() throws Exception {
        assertThat(ToStream.toSequenceM(FluentIterable.of(new Integer[]{1, 2, 3, 4})).collect(Collectors.toList()).size(), is(4));
    }

    @Test
    public void testToJooqLambda() throws Exception {
        assertThat(ToStream.toJooqLambda(FluentIterable.of(new Integer[]{1, 2, 3, 4})).collect(Collectors.toList()).size(), is(4));
    }

    @Test    @Ignore
    public void testToFutureStream() throws Exception {
        assertThat(ToStream.toFutureStream(FluentIterable.of(new Integer[]{1, 2, 3, 4})).collect(Collectors.toList()).size(), is(4));
    }



    @Test
    public void testToLazySeq() throws Exception {
        assertThat(ToStream.toLazySeq(FluentIterable.of(new Integer[]{1, 2, 3, 4})).toList().size(), is(4));
    }

    
}