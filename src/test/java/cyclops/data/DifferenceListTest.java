package cyclops.data;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 02/09/2017.
 */
public class DifferenceListTest {

    @Test
    public void append(){
        assertThat(DifferenceList.of(1,2,3).append(DifferenceList.of(4,5,6)).run(),equalTo(LazySeq.of(1,2,3,4,5,6)));
    }
    @Test
    public void map(){
        assertThat(DifferenceList.of(1,2,3).append(DifferenceList.of(4,5,6)).map(i->i*2).run(),equalTo(LazySeq.of(2,4,6,8,10,12)));
    }

    @Test
    public void flatMap(){
        assertThat(DifferenceList.of(1,2,3).append(DifferenceList.of(4,5,6)).flatMap(i-> DifferenceList.of(i*2)).run(),equalTo(LazySeq.of(2,4,6,8,10,12)));
    }

}