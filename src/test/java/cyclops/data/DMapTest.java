package cyclops.data;

import cyclops.control.lazy.LazyEither;
import cyclops.control.lazy.LazyEither3;
import org.junit.Test;

import java.util.Date;

import static cyclops.control.Either.right;
import static cyclops.control.lazy.LazyEither.left;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class DMapTest {
    @Test
    public void two() throws Exception {

    }

    @Test
    public void three() throws Exception {
    }

    @Test
    public void twoEmpty() throws Exception {
        DMap.Two<Integer, String, Long, Date> dmap = DMap.<Integer, String, Long, Date>twoEmpty();
        dmap =dmap.put1(10,"hello");
        dmap = dmap.put2(10l,new Date(10));

        assertThat(dmap.get(left(10)),equalTo(LazyEither3.left1("hello")));
        assertThat(dmap.get(right(10l)),equalTo(LazyEither3.left2(new Date(10))));
    }

    @Test
    public void threeEmpty() throws Exception {
    }

}