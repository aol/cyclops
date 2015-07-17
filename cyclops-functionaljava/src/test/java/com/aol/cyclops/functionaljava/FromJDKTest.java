package com.aol.cyclops.functionaljava;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class FromJDKTest {

    @Test
    public void testJDKλ() {
        assertThat(FromJDK.λ((Integer a)->a*100).f(2),is(200));

    }
    @Test
    public void testJDKλ2(){
        assertThat(FromJDK.λ2((Integer a,Integer b)->a*b).f(100,5),is(500));
    }
    @Test
    public void testJDKOption(){
        assertThat(FromJDK.option(Optional.of(1)).some(),is(1));
    }
    @Test
    public void testJDKOptionNull(){
        assertThat(FromJDK.option(Optional.ofNullable(null)).orSome(100),is(100));
    }

}
