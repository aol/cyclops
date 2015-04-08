package com.aol.cyclops.javaslang;

import org.junit.Test;

import java.util.Optional;

import static com.aol.cyclops.javaslang.JavasLangConverter.*;
import static com.aol.cyclops.javaslang.FromJDK.λ2;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class FromJDKTest {

    @Test
    public void testJDKλ() {
        assertThat(FromJDK.λ((Integer a)->a*100).apply(2),is(200));

    }
    @Test
    public void testJDKλ2(){
        assertThat(λ2((Integer a,Integer b)->a*b).curried().apply(100).apply(5),is(500));
    }
    @Test
    public void testJDKOption(){
        assertThat(FromJDK.option(Optional.of(1)).get(),is(1));
    }
    @Test
    public void testJDKOptionNull(){
        assertThat(FromJDK.option(Optional.ofNullable(null)).orElse(100),is(100));
    }

}
