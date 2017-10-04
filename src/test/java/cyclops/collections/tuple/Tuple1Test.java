package cyclops.collections.tuple;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 04/10/2017.
 */
public class Tuple1Test {
    Tuple1<Integer> t1;
    @Before
    public void setUp() throws Exception {
        t1 = Tuple.tuple(10);
        called=  0;
    }

    @Test
    public void of() throws Exception {
        assertThat(Tuple1.of(10),equalTo(t1));
    }

    int called;
    @Test
    public void lazy() throws Exception {
        Tuple1<String> lazyT1 = Tuple.lazy(()->{
            called++;
            return "hello";
        });
        assertThat(called,equalTo(0));
        assertThat(lazyT1._1(),equalTo("hello"));
        assertThat(called++,equalTo(1));
    }

    @Test
    public void _1() throws Exception {
    }

    @Test
    public void toIdentity() throws Exception {
    }

    @Test
    public void cast() throws Exception {
    }

    @Test
    public void map() throws Exception {
    }

    @Test
    public void peek() throws Exception {
    }

    @Test
    public void trampoline() throws Exception {
    }

    @Test
    public void retry() throws Exception {
    }

    @Test
    public void eager() throws Exception {
    }

    @Test
    public void retry1() throws Exception {
    }

    @Test
    public void lazyMap() throws Exception {
    }

    @Test
    public void zip() throws Exception {
    }

    @Test
    public void lazyZip() throws Exception {
    }

    @Test
    public void flatMap() throws Exception {
    }

    @Test
    public void lazyFlatMap() throws Exception {
    }

    @Test
    public void visit() throws Exception {
    }

    @Test
    public void testToString() throws Exception {
        assertThat(t1.toString(),equalTo("[10]"));
    }

    @Test
    public void filter() throws Exception {
    }

    @Test
    public void ofType() throws Exception {
    }

    @Test
    public void filterNot() throws Exception {
    }

    @Test
    public void notNull() throws Exception {
    }

}