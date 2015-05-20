package com.aol.cyclops.lambda.tuple.lazyswap;

import com.aol.cyclops.lambda.tuple.PowerTuples;
import org.junit.Test;

import static com.aol.cyclops.lambda.tuple.LazySwap.lazySwap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 5/21/15.
 */
public class LazySwap3Test {
    @Test
    public void swap3(){
    	System.out.println(lazySwap(PowerTuples.tuple(1, 2, 3)));
        assertThat(lazySwap(PowerTuples.tuple(1, 2, 3)),equalTo(PowerTuples.tuple(3,2,1)));
    }
    @Test
    public void swap3_larger(){
        assertThat(lazySwap(PowerTuples.tuple(1,2,3,4).tuple3()),equalTo(PowerTuples.tuple(3,2,1)));
    }

}
