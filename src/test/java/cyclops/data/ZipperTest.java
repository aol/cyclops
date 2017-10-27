package cyclops.data;

import cyclops.data.tuple.Tuple;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import static cyclops.data.tuple.Tuple.tuple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class ZipperTest {

    ImmutableList<Integer> right = LazySeq.of(10,20,30);
    ImmutableList<Integer> left = LazySeq.of(1,2,3);
    Zipper<Integer> z = Zipper.of(left,5,right);
    Zipper<Integer> z2 = Zipper.of(ReactiveSeq.of(40,20,60),50,ReactiveSeq.of(1,2,3));


    @Test
    public void isStart() throws Exception {
        assertFalse(z.isStart());
        assertTrue(z.position(0).orElse(z2).isStart());
    }

    @Test
    public void isEnd() throws Exception {
        assertFalse(z.isEnd());
        assertTrue(z.position(6).orElse(z2).isEnd());
    }

    @Test
    public void map() throws Exception {

        assertThat(z.map(i->((int)0)-i),equalTo(Zipper.of(LazySeq.of(-1,-2,-3),-5,LazySeq.of(-10,-20,-30))));
    }

    @Test
    public void zip() throws Exception {
        assertThat(z.zip(z2,Tuple::tuple),equalTo(Zipper.of(LazySeq.of(tuple(1,40),tuple(2,20),tuple(3,60)),
                tuple(5,50),
                LazySeq.of(tuple(10,1),tuple(20,2),tuple(30,3)))));
    }

    @Test
    public void zip1() throws Exception {
        assertThat(z.zip(z2),equalTo(Zipper.of(LazySeq.of(tuple(1,40),tuple(2,20),tuple(3,60)),
                                            tuple(5,50),
                                                LazySeq.of(tuple(10,1),tuple(20,2),tuple(30,3)))));
    }

    @Test
    public void start() throws Exception {
        System.out.println(z.start());
        System.out.println(Zipper.of(LazySeq.empty(),1,LazySeq.of(2,3,5,10,20,30)));
        assertThat(z.start(),equalTo(Zipper.of(LazySeq.empty(),1,LazySeq.of(2,3,5,10,20,30))));
    }

    @Test
    public void end() throws Exception {
        assertThat(z.end(),equalTo(Zipper.of(LazySeq.of(1,2,3,5,10,20),30,LazySeq.empty())));
    }

    @Test
    public void index() throws Exception {
        assertThat(z.index(),equalTo(3));
        assertThat(z.next().orElse(z2).index(),equalTo(4));
        assertThat(z.previous().orElse(z2).index(),equalTo(2));
    }

    @Test
    public void position() throws Exception {
        assertThat(z.position(0).orElse(z2),equalTo(Zipper.of(LazySeq.empty(),1,LazySeq.of(2,3,5,10,20,30))));
        assertThat(z.position(1).orElse(z2),equalTo(Zipper.of(LazySeq.of(1),2,LazySeq.of(3,5,10,20,30))));
    }

    @Test
    public void next() throws Exception {
        assertThat(z.next().orElse(z2),equalTo(Zipper.of(LazySeq.of(1,2,3,5),10,LazySeq.of(20,30))));
    }

    @Test
    public void next1() throws Exception {
        assertThat(z.next(z2),equalTo(Zipper.of(LazySeq.of(1,2,3,5),10,LazySeq.of(20,30))));
    }

    @Test
    public void previous() throws Exception {
        assertThat(z.previous().orElse(z2),equalTo(Zipper.of(LazySeq.of(1,2),3,LazySeq.of(5,10,20,30))));
    }

    @Test
    public void cycleNext() throws Exception {
        assertThat(z.cycleNext(),equalTo(z.next().orElse(z2)));
        assertThat(Zipper.of(LazySeq.of(1,2,3),5,LazySeq.empty()).cycleNext(),equalTo(Zipper.of(LazySeq.empty(),1,LazySeq.of(2,3,5))));

    }

    @Test
    public void cyclePrevious() throws Exception {
        System.out.println(z.previous().orElse(z2));
        assertThat(z.cyclePrevious(),equalTo(z.previous().orElse(z2)));
        assertThat(Zipper.of(LazySeq.empty(),5,LazySeq.of(1,2,3)).cyclePrevious(),equalTo(Zipper.of(LazySeq.of(5,1,2),3,LazySeq.empty())));
    }

    @Test
    public void previous1() throws Exception {
        assertThat(z.previous(z2),equalTo(Zipper.of(LazySeq.of(1,2),3,LazySeq.of(5,10,20,30))));
    }

    @Test
    public void left() throws Exception {
        assertThat(z.left(10),equalTo(Zipper.of(LazySeq.of(1,2,3),10,LazySeq.of(5,10,20,30))));
    }

    @Test
    public void right() throws Exception {

        assertThat(z.right(10),equalTo(Zipper.of(LazySeq.of(1,2,3,5),10,LazySeq.of(10,20,30))));
    }

    @Test
    public void deleteLeftAndRight() throws Exception {
       assertThat(z.deleteAllLeftAndRight(),equalTo(Zipper.of(LazySeq.empty(),5,LazySeq.empty())));
    }

    @Test
    public void deleteLeft() throws Exception {
        assertThat(z.deleteLeft().orElse(z2),equalTo(Zipper.of(LazySeq.of(1,2),3,right)));
    }

    @Test
    public void deleteRight() throws Exception {
        assertThat(z.deleteRight().orElse(z2),equalTo(Zipper.of(left,10,LazySeq.of(20,30))));
    }

    @Test
    public void filterLeft() throws Exception {
        assertThat(z.filterLeft(i->i>2).getLeft(),equalTo(LazySeq.of(3)));
    }

    @Test
    public void filterRight() throws Exception {
        assertThat(z.filterRight(i->i<25).getRight(),equalTo(LazySeq.of(10,20)));
    }

    @Test
    public void split() throws Exception {
       assertThat(z.split(),equalTo(tuple(left,5,right)));
    }

    @Test
    public void list() throws Exception {
        assertThat(z.list(),equalTo(Seq.of(1,2,3,5,10,20,30)));
    }

    @Test
    public void withLeft() throws Exception {
        assertThat(z.withLeft(right).getRight(),equalTo(right));
    }

    @Test
    public void withPoint() throws Exception {
        assertThat(z.withPoint(10).getPoint(),equalTo(10));
    }

    @Test
    public void withRight() throws Exception {
        assertThat(z.withRight(left).getRight(),equalTo(left));
    }

    @Test
    public void getLeft() throws Exception {
        assertThat(z.getLeft(),equalTo(left));
        assertThat(z.next().orElse(z2).getLeft().seq(),equalTo(LazySeq.of(1,2,3,5).seq()));
    }

    @Test
    public void getPoint() throws Exception {
        assertThat(z.getPoint(),equalTo(5));
        assertThat(z.next().orElse(z2).getPoint(),equalTo(10));
    }

    @Test
    public void getRight() throws Exception {
        assertThat(z.getRight(),equalTo(right));
        assertThat(z.next().orElse(z2).getRight(),equalTo(LazySeq.of(20,30)));

    }

}