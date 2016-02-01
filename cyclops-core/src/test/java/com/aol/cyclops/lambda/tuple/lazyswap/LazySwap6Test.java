package com.aol.cyclops.lambda.tuple.lazyswap;

import com.aol.cyclops.lambda.tuple.PowerTuples;
import org.junit.Test;

import static com.aol.cyclops.lambda.tuple.LazySwap.lazySwap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 5/21/15.
 */
public class LazySwap6Test {
    @Test
    public void swap6(){
        assertThat(lazySwap(PowerTuples.tuple(1, 2, 3, 4, 5, 6)),equalTo(PowerTuples.tuple(6,5,4,3,2,1)));
    }
    @Test
    public void swap6_larger(){
        assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5,6,7).tuple6()),equalTo(PowerTuples.tuple(6,5,4,3,2,1)));
    }
}
