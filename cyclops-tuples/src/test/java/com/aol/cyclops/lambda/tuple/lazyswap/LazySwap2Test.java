package com.aol.cyclops.lambda.tuple.lazyswap;

import com.aol.cyclops.lambda.tuple.PowerTuples;
import org.junit.Test;

import static com.aol.cyclops.lambda.tuple.LazySwap.lazySwap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 5/21/15.
 */
public class LazySwap2Test {
    @Test
    public void swap2(){
        assertThat(lazySwap(PowerTuples.tuple(1, 2)),equalTo(PowerTuples.tuple(2,1)));
    }
   /** @Test
    public void swap2_larger(){
        assertThat(lazySwap(PowerTuples.tuple(1,2,3).tuple2()),equalTo(PowerTuples.tuple(2,1)));
    }**/

}
