package cyclops.data;

import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZipperTest {
    @Test
    public void of() throws Exception {
        ImmutableList<Integer> s = LazySeq.of(1, 2, 3);
        Zipper.of(s,10,s);
        Zipper<Integer> z = Zipper.of(LazySeq.of(1,2,3),5,LazySeq.of(10,20,30));

        System.out.println(z);
        System.out.println(z.next());


    }

    @Test
    public void of1() throws Exception {

    }

    @Test
    public void isStart() throws Exception {
    }

    @Test
    public void isEnd() throws Exception {
    }

    @Test
    public void map() throws Exception {
    }

    @Test
    public void zip() throws Exception {
    }

    @Test
    public void zip1() throws Exception {
    }

    @Test
    public void start() throws Exception {
    }

    @Test
    public void end() throws Exception {
    }

    @Test
    public void index() throws Exception {
    }

    @Test
    public void position() throws Exception {
    }

    @Test
    public void next() throws Exception {
    }

    @Test
    public void next1() throws Exception {
    }

    @Test
    public void previous() throws Exception {
    }

    @Test
    public void cycleNext() throws Exception {
    }

    @Test
    public void cyclePrevious() throws Exception {
    }

    @Test
    public void previous1() throws Exception {
    }

    @Test
    public void left() throws Exception {
    }

    @Test
    public void right() throws Exception {
    }

    @Test
    public void deleteLeftAndRight() throws Exception {
    }

    @Test
    public void deleteLeft() throws Exception {
    }

    @Test
    public void deleteRight() throws Exception {
    }

    @Test
    public void filterLeft() throws Exception {
    }

    @Test
    public void filterRight() throws Exception {
    }

    @Test
    public void split() throws Exception {
    }

    @Test
    public void list() throws Exception {
    }

    @Test
    public void withLeft() throws Exception {
    }

    @Test
    public void withPoint() throws Exception {
    }

    @Test
    public void withRight() throws Exception {
    }

    @Test
    public void getLeft() throws Exception {
    }

    @Test
    public void getPoint() throws Exception {
    }

    @Test
    public void getRight() throws Exception {
    }

}