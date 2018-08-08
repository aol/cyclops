package cyclops.data.base;

import cyclops.data.base.BAMT.ArrayIterator2D;
import cyclops.reactive.ReactiveSeq;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArrayIterator2DTest {

    ArrayIterator2D<Integer> hasEmpty;
    ArrayIterator2D<Integer> endsEmpty;
    @Before
    public void setup(){
        hasEmpty = new ArrayIterator2D<Integer>(new Object[][]{new Object[]{1,2,3},new Object[]{},new Object[]{4,5,6},new Object[]{1}});
        endsEmpty = new ArrayIterator2D<Integer>(new Object[][]{new Object[]{1,2,3},new Object[]{},new Object[]{4,5,6},new Object[]{},new Object[]{},new Object[]{}});
    }
    @Test
    public void hasEmpty(){
        assertThat(ReactiveSeq.fromIterable(hasEmpty).toList(),equalTo(Arrays.asList(1,2,3,4,5,6,1)));
    }
    @Test
    public void endsEmpty(){
        assertThat(ReactiveSeq.fromIterable(endsEmpty).toList(),equalTo(Arrays.asList(1,2,3,4,5,6)));

    }
}
