package cyclops.collections.adt;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class ListTest {

    @Test
    public void testMap(){
        assertThat(SList.of(1,2,3).map(i->1*2),equalTo(SList.of(2,4,6)));
        assertThat(SList.empty().map(i->1*2),equalTo(SList.empty()));
    }
    @Test
    public void testFlatMap(){
        assertThat(SList.of(1,2,3).flatMap(i-> SList.of(1*2)),equalTo(SList.of(2,4,6)));
        assertThat(SList.empty().flatMap(i-> SList.of(1*2)),equalTo(SList.empty()));
    }
}
