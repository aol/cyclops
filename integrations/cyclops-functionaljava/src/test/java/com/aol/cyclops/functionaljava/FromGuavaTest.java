package com.aol.cyclops.functionaljava;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class FromGuavaTest {
    @Test
    public void testGuavaλ() {
        assertThat(FromGuava.f1((Integer a)->a*100).f(2),is(200));

    }
    @Test
    public void testGuavaOption(){
        assertThat(FromGuava.option(com.google.common.base.Optional.of(1)).some(),is(1));
    }
    @Test
    public void testJDKGuavaNull(){
        assertThat(FromGuava.option(com.google.common.base.Optional.fromNullable(null)).orSome(100),is(100));
    }


}
