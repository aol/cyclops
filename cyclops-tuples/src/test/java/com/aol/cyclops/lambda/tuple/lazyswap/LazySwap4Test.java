package com.aol.cyclops.lambda.tuple.lazyswap;

import com.aol.cyclops.lambda.tuple.PowerTuples;
import org.junit.Test;

import static com.aol.cyclops.lambda.tuple.LazySwap.lazySwap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 5/21/15.
 */
public class LazySwap4Test {
    @Test
    public void swap4(){
        assertThat(lazySwap(PowerTuples.tuple(1, 2, 3, 4)),equalTo(PowerTuples.tuple(4,3,2,1)));
    }
    @Test
    public void swap4_larger(){
        assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5).tuple4()),equalTo(PowerTuples.tuple(4,3,2,1)));
    }

}
