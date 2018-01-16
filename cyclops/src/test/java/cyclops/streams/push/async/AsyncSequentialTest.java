package cyclops.streams.push.async;

import com.oath.cyclops.streams.BaseSequentialTest;
import cyclops.companion.Semigroups;
import com.oath.cyclops.async.adapters.Topic;

import cyclops.control.Option;
import cyclops.control.Maybe;
import cyclops.control.LazyEither;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.reactive.Streamable;
import org.hamcrest.Matchers;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class AsyncSequentialTest extends BaseSequentialTest {

    @Override
    protected <U> ReactiveSeq<U> of(U... array){
        int[] index = {0};
        return Spouts.async(s->{

            new Thread(()-> {

                for (U next : array) {
                    s.onNext(next);
                    if(index[0]++>100)
                        break;
                }
                s.onComplete();
            }).start();

        });
    }
    @Test
    public void testCycle() {

    }

    @Test
    public void subscribe3ErrorOnComplete() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));

        assertThat(onComplete.get(), Matchers.equalTo(false));
        s.request(4l);
        Thread.sleep(100);
        assertThat(onComplete.get(), Matchers.equalTo(true));

        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));

        assertThat(onComplete.get(), Matchers.equalTo(true));
    }
    @Test
    public void subscribeErrorEmptyOnComplete() throws InterruptedException {
        List result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of().forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));
        Thread.sleep(100);
        s.request(1l);
        assertThat(onComplete.get(), Matchers.equalTo(true));
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));

    }
    @Test
    public void subscribe3Error() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(3l);
        Thread.sleep(100);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }

    @Test @Ignore
    public void triplicate(){
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> tpl = of(1, 2, 3).triplicate(()->null);
        tpl._1().forEach(System.out::println);
        tpl._2().forEach(System.out::println);
        tpl._3().forEach(System.out::println);
    }
    @Test
    public void spoutsCollect(){
        Integer[] array = new Integer[15];
        for(int i=0;i<array.length;i++) {
            array[i]=i;
        }
        for(int i=0;i<ITERATIONS;i++) {
            List<Integer> list = of(array).collect(Collectors.toList());
            assertThat(list.size(),equalTo(array.length));
        }
    }
    @Test
    public void broadcastTest(){
        Topic<Integer> topic = of(1,2,3)
                .broadcast();


        ReactiveSeq<Integer> stream1 = topic.stream();
        ReactiveSeq<Integer> stream2 = topic.stream();
        assertThat(stream1.toList(), Matchers.equalTo(Arrays.asList(1,2,3)));
        assertThat(stream2.stream().toList(), Matchers.equalTo(Arrays.asList(1,2,3)));

    }
    @Test
    public void mergePTest(){
        //System.out.println(of(3, 6, 9).mergeP(of(2, 4, 8), of(1, 5, 7)).listX());

        for(int i=0;i<ITERATIONS;i++) {
            List<Integer> list = of(3, 6, 9).mergeP(of(2, 4, 8), of(1, 5, 7)).toList();

            assertThat("List is " + list,list, hasItems(1, 2, 3, 4, 5, 6, 7, 8, 9));
            assertThat("List is " + list,list.size(), Matchers.equalTo(9));
        }

    }
    @Test
    public void prependPlay(){
        System.out.println(of(1,2,3).prependAll(100,200,300).collect(Collectors.toList()));


    }
    @Test
    public void splitAtExp(){
        of(1, 2, 3).peek(e->System.out.println("Peeking! " +e)).splitAt(0).transform( (a, b)->{
            a.printOut();
            b.printOut();
            return null;
        });

    }





    @Test
    public void multicast(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1,2,3,4,5,6,7,8).duplicate();

//        t._1.forEach(e->System.out.println("First " + e));
 //       t._2.printOut();


        assertThat(t._1().limit(1).toList(),equalTo(Arrays.asList(1)));
        System.out.println("Second!");
        assertThat(t._2().limit(1).toList(),equalTo(Arrays.asList(1)));

    }

    @Test
    public void duplicateReplay(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();
        assertThat(t._1().limit(1).toList(),equalTo(Arrays.asList(1)));
//        assertThat(t._1.limit(1).toList(),equalTo(ListX.of(1)));
    }
    @Test
    public void limitSkip(){
        ReactiveSeq<Integer> stream = of(1);
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = stream.duplicate();


        assertThat(Streamable.fromStream(dup._1().limit(1)).toList(),equalTo(Arrays.asList(1)));
        assertThat(Streamable.fromStream(dup._2().skip(1)).toList(),equalTo(Arrays.asList()));

    }@Test
    public void limitSkip2(){
        ReactiveSeq<Integer> stream = of(1);
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = stream.duplicate();
        assertThat(dup._1().limit(1).toList(),equalTo(Arrays.asList(1)));
        assertThat(dup._2().skip(1).toList(),equalTo(Arrays.asList()));




    }


    @Test
    public void splitAtHeadImpl(){

        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();



        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = new Tuple2(
                t._1().limit(1), t._2().skip(1));
        assertThat(t._1().limit(1).toList(),equalTo(Arrays.asList(1)));
        assertThat(t._2().skip(1).toList(),equalTo(Arrays.asList()));


    }
    @Test
    public void splitAtHeadImpl2(){

        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();



        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = new Tuple2(
                t._1().limit(1), t._2().skip(1));
        assertThat(t._1().limit(1).toList(),equalTo(Arrays.asList(1)));
        assertThat(t._2().skip(1).toList(),equalTo(Arrays.asList()));


    }
    @Test
    public void splitLimit(){
        ReactiveSeq<Integer> stream = of(1);
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(),equalTo(Arrays.asList(1)));
        assertThat(t._1().limit(1).toList(),equalTo(Arrays.asList(1)));

    }
    @Test
    public void splitLimit2(){
        ReactiveSeq<Integer> stream = of(1);
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(),equalTo(Arrays.asList(1)));

        assertThat(t._1().limit(1).toList(),equalTo(Arrays.asList(1)));
    }

    @Test
    public void duplicateDuplicate(){
        for(int k=0;k<ITERATIONS;k++) {
            assertThat(of(1, 2, 3).duplicate()
                    ._1().duplicate()._1().duplicate()._1().toList(), equalTo(Arrays.asList(1, 2, 3)));
        }

    }
    @Test
    public void duplicateDuplicateDuplicate(){
        for(int k=0;k<ITERATIONS;k++) {
            assertThat(of(1, 2, 3).duplicate()
                    ._1().duplicate()._1().duplicate()._1().duplicate()._1().toList(), equalTo(Arrays.asList(1, 2, 3)));
        }

    }
    @Test
    public void skipDuplicateSkip() {
        assertThat(of(1, 2, 3).duplicate()._1().skip(1).duplicate()._1().skip(1).toList(), equalTo(Arrays.asList(3)));
        assertThat(of(1, 2, 3).duplicate()._2().skip(1).duplicate()._2().skip(1).toList(), equalTo(Arrays.asList(3)));
    }

    @Test
    public void skipLimitDuplicateLimitSkip() {
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = of(1, 2, 3).duplicate();
        Option<Integer> head1 = dup._1().limit(1).to().option().flatMap(l -> {
            return l.size() > 0 ? l.get(0) : Option.<Integer>none();
        });
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup2 = dup._2().skip(1).duplicate();
        Option<Integer> head2 = dup2._1().limit(1).to().option().flatMap(l -> {
            return l.size() > 0 ? l.get(0) : Option.<Integer>none();
        });
        assertThat(dup2._2().skip(1).toList(),equalTo(Arrays.asList(3)));

        assertThat(of(1, 2, 3).duplicate()._1().skip(1).duplicate()._1().skip(1).toList(), equalTo(Arrays.asList(3)));
    }
    @Test
    public void skipLimitTriplicateLimitSkip() {
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup = of(1, 2, 3).triplicate();
        Option<Integer> head1 = dup._1().limit(1).to().option().flatMap(l -> {
            return l.size() > 0 ? l.get(0) : Option.<Integer>none();
        });
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup2 = dup._2().skip(1).triplicate();
        Option<Integer> head2 = dup2._1().limit(1).to().option().flatMap(l -> {
            return l.size() > 0 ? l.get(0) : Option.<Integer>none();
        });
        assertThat(dup2._2().skip(1).toList(),equalTo(Arrays.asList(3)));

        assertThat(of(1, 2, 3).duplicate()._1().skip(1).duplicate()._1().skip(1).toList(), equalTo(Arrays.asList(3)));
    }
    @Test
    public void skipLimitQuadruplicateLimitSkip() {
        Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup = of(1, 2, 3).quadruplicate();
        Option<Integer> head1 = dup._1().limit(1).to().option().flatMap(l -> {
            return l.size() > 0 ? l.get(0) : Option.<Integer>none();
        });
        Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup2 = dup._2().skip(1).quadruplicate();
        Option<Integer> head2 = dup2._1().limit(1).to().option().flatMap(l -> {
            return l.size() > 0 ? l.get(0) : Option.<Integer>none();
        });
        assertThat(dup2._2().skip(1).toList(),equalTo(Arrays.asList(3)));

        assertThat(of(1, 2, 3).duplicate()._1().skip(1).duplicate()._1().skip(1).toList(), equalTo(Arrays.asList(3)));
    }


    @Test
    public void splitThenSplit(){
        assertThat(of(1,2,3).to().option(),equalTo(Option.of(Arrays.asList(1,2,3))));
       // System.out.println(of(1, 2, 3).splitAtHead()._2.listX());
        System.out.println("split " + of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList());
        assertEquals(Option.of(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
    }
    @Test
    public void testSplitAtHead() {

        assertEquals(Option.none(), of().splitAtHead()._1());
        assertEquals(asList(), of().splitAtHead()._2().toList());

        assertEquals(Option.of(1), of(1).splitAtHead()._1());
        assertEquals(asList(), of(1).splitAtHead()._2().toList());

        assertEquals(Option.of(1), of(1, 2).splitAtHead()._1());
        assertEquals(asList(2), of(1, 2).splitAtHead()._2().toList());

        assertEquals(Option.of(1), of(1, 2, 3).splitAtHead()._1());
        assertEquals(Option.of(2), of(1, 2, 3).splitAtHead()._2().splitAtHead()._1());
        assertEquals(Option.of(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
        assertEquals(asList(2, 3), of(1, 2, 3).splitAtHead()._2().toList());
         assertEquals(asList(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList());
        assertEquals(asList(), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._2().toList());
    }
    @Test
    public void testZipWithIndex() {

        assertEquals(asList(), of().zipWithIndex().toList());
        assertEquals(asList(tuple("a", 0L)), of("a").zip(of(0L)).toList());
      }

    @Test
    public void zipWithIndex2(){
        System.out.println("First..");
        assertEquals(asList(tuple("a", 0L)), of("a").zipWithIndex().toList());

        System.out.println("Second..");
        assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
        System.out.println("Third..");
        assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L), new Tuple2("c", 2L)), of("a", "b", "c").zipWithIndex().toList());


    }
    @Test
    public void triplicateParallelFanOut2() {
        for (int k = 0; k < ITERATIONS; k++) {

            assertThat(of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    .parallelFanOut(ForkJoinPool.commonPool(), s1 -> s1.filter(i -> i % 3 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 3 == 1).map(i -> i * 100),
                            s3 -> s3.filter(i -> i % 3 == 2).map(i -> i * 1000))
                    .toList(), Matchers.hasItems(6, 100, 2000, 12, 400, 5000, 18, 700, 8000));

        }
    }

    @Test
    public void subscribeEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().forEachSubscribe(i->result.add(i));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));

    }
    @Test
    public void subscribe() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i));
        s.request(1l);
        Thread.sleep(100);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribe3() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i));
        s.request(3l);
        Thread.sleep(100);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribeErrorEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));

    }
    @Test
    public void subscribeError() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(1l);

        Thread.sleep(100);

        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }


    @Test
    public void subscribeErrorOnComplete() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));


        s.request(1l);
        Thread.sleep(100);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
        s.request(1l);
        assertThat(onComplete.get(), Matchers.equalTo(true));
    }

    @Test
    public void combine() {
        assertThat(ofWait(1, 2, 3, 4, 5, 6, 7, 8).combine((a, b) -> a < 5, Semigroups.intSum)
                .takeOne(), Matchers.equalTo(Maybe.of(6)));
    }

    @Test
    public void combineOneFirstOrError() {
        assertThat(ofWait(1)
                .combine((a, b) -> a < 5, Semigroups.intSum)
                .findFirstOrError(), Matchers.equalTo(LazyEither.right(1)));
    }
    @Test
    public void combineTwo() {

            assertThat(ofWait(1, 2)
                    .combine((a, b) -> a < 5, Semigroups.intSum)
                    .takeOne(), Matchers.equalTo(Maybe.of(3)));
    }

    @Test
    public void combineOne() {
        assertThat(ofWait(1)
                .combine((a, b) -> a < 5, Semigroups.intSum)
                .takeOne(), Matchers.equalTo(Maybe.of(1)));
    }

    protected <U> ReactiveSeq<U> ofWait(U... array){
        int[] index = {0};
        return Spouts.async(s->{

            new Thread(()-> {
                System.out.println("Pushing data from " + Thread.currentThread().getId());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (U next : array) {
                    s.onNext(next);
                    if(index[0]++>100)
                        break;
                }
                s.onComplete();
            }).start();

        });
    }

}
