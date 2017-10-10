package cyclops.data;


import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LazySeqTest {


    @Test
    public void prependAllTest(){
        assertThat(LazySeq.of(1,2,3).prependAll(LazySeq.of(4,5,6)),equalTo(LazySeq.of(4,5,6,1,2,3)));
    }

    @Test
    public void scanRight(){
        LazySeq.of(1,2,3).scanRight(0,(a, b)->a+b).printOut();
    }





}
