package com.aol.cyclops.guava;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class FromJDKTest {

    @Test
    public void testJDKf() {
        assertThat(FromJDK.f1((Integer a)->a*100).apply(2),is(200));

    }
    
    @Test
    public void testJDKOption(){
        assertThat(FromJDK.option(Optional.of(1)).get(),is(1));
    }
    @Test
    public void testJDKOptionNull(){
        assertThat(FromJDK.option(Optional.ofNullable(null)).or(100),is(100));
    }

}
