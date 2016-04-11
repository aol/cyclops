package com.aol.cyclops.control.monads.transformers;

import static org.hamcrest.Matchers.equalTo;

import static org.junit.Assert.assertThat;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public class ListTSeqTest {

    ListTSeq<Integer> trans;
    @Before
    public void setup(){
        trans = ListT.fromIterable(Arrays.asList(ListX.of(1,2,3),ListX.of(1,2,3)));
        
    }
    
    @Test
    public void cycle(){
        assertThat(trans.cycle(3).toString(),equalTo("AnyM([[1, 2, 3, 1, 2, 3, 1, 2, 3], [1, 2, 3, 1, 2, 3, 1, 2, 3]])"));
    }
   
}
