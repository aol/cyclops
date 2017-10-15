package cyclops.data;

import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class BagTest {

    @Test
    public void testBag(){
        Bag<Integer> bag1 = Bag.of(1,2,3,4,10,1,1,2);
        Bag<Integer> bag2 = Bag.fromStream(Stream.of(1,2,3,4,10,1,1,2));

        assertThat(bag1,equalTo(bag2));
    }
    @Test
    public void minus(){
        Bag<Integer> bag1 = Bag.of(1,2,3,4,10,1,1,2);
        assertThat(bag1.minus(1).instances(1),equalTo(2));
        
    }
    
    @Test
    public void stream(){
        List<Integer> l = Bag.of(1, 2, 3, 4, 10, 1, 1, 2).stream().toList();
        assertThat(l.size(),equalTo(8));
    }

}