package cyclops.streams.push.async;

import com.aol.cyclops2.streams.BaseSequentialTest;
import cyclops.CyclopsCollectors;
import cyclops.async.Queue;
import cyclops.async.QueueFactories;
import cyclops.async.Topic;
import cyclops.collections.ListX;
import cyclops.collections.SetX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import cyclops.stream.Streamable;
import org.hamcrest.Matchers;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class AsyncSequentialTest extends BaseSequentialTest {
    static Executor exec = Executors.newFixedThreadPool(10);
    @Override
    protected <U> ReactiveSeq<U> of(U... array){

        return Spouts.async(s->{
            exec.execute(()-> {

                for (U next : array) {
                    s.onNext(next);
                }
                s.onComplete();
            });

        });
    }
    @Test
    public void subscribe3ErrorOnComplete() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).subscribe(i->result.add(i),e->e.printStackTrace(),()->onComplete.set(true));

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
        Subscription s= of().subscribe(i->result.add(i),e->e.printStackTrace(),()->onComplete.set(true));
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
        Subscription s= of(1,2,3).subscribe(i->result.add(i),e->e.printStackTrace());
        s.request(3l);
        Thread.sleep(100);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }

    @Test @Ignore
    public void triplicate(){
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> tpl = of(1, 2, 3).triplicate(()->null);
        tpl.v1.forEach(System.out::println);
        tpl.v2.forEach(System.out::println);
        tpl.v3.forEach(System.out::println);
    }
    @Test
    public void spoutsCollect(){
        Integer[] array = new Integer[100];
        for(int i=0;i<array.length;i++) {
            array[i]=i;
        }
        for(int i=0;i<10;i++) {
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
        assertThat(stream1.toListX(), Matchers.equalTo(ListX.of(1,2,3)));
        assertThat(stream2.stream().toListX(), Matchers.equalTo(ListX.of(1,2,3)));

    }
    @Test
    public void mergePTest(){
        //System.out.println(of(3, 6, 9).mergeP(of(2, 4, 8), of(1, 5, 7)).toListX());

        for(int i=0;i<100;i++) {
            ListX<Integer> list = of(3, 6, 9).mergeP(of(2, 4, 8), of(1, 5, 7)).toListX();

            assertThat("List is " + list,list, hasItems(1, 2, 3, 4, 5, 6, 7, 8, 9));
            assertThat("List is " + list,list.size(), Matchers.equalTo(9));
        }

    }
    @Test
    public void prependPlay(){
        System.out.println(of(1,2,3).prepend(100,200,300).collect(Collectors.toList()));


    }
    @Test
    public void splitAtExp(){
        of(1, 2, 3).peek(e->System.out.println("Peeking! " +e)).splitAt(0).map( (a,b)->{
            a.printOut();
            b.printOut();
            return null;
        });

    }



    @Test
    public void testCycleAsync() {
      //  of(1, 2).collectAll(CyclopsCollectors.toListX())
        //        .flatMapI(i->i.cycle(3)).printOut();

       // of(1, 2).cycle().limit(6).forEach(n->System.out.println("Next " + n));


        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle().limit(6).toList());
        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).cycle().limit(6).toList());

    }

    @Test
    public void multicast(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1,2,3,4,5,6,7,8).duplicate();

//        t.v1.forEach(e->System.out.println("First " + e));
 //       t.v2.printOut();


        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
        System.out.println("Second!");
        assertThat(t.v2.limit(1).toList(),equalTo(ListX.of(1)));

    }
    @Test
    public void multicastCycle(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1,2,3,4,5,6,7,8).duplicate();

//        t.v1.forEach(e->System.out.println("First " + e));
        //       t.v2.printOut();


        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
        System.out.println("Second!");
        assertThat(t.v2.cycle().limit(1).toList(),equalTo(ListX.of(1)));

    }
    @Test
    public void duplicateReplay(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();
        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
//        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
    }
    @Test
    public void limitSkip(){
        ReactiveSeq<Integer> stream = of(1);
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = stream.duplicate();


        assertThat(Streamable.fromStream(dup.v1.limit(1)).toList(),equalTo(ListX.of(1)));
        assertThat(Streamable.fromStream(dup.v2.skip(1)).toList(),equalTo(ListX.of()));

    }@Test
    public void limitSkip2(){
        ReactiveSeq<Integer> stream = of(1);
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = stream.duplicate();
        assertThat(dup.v1.limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(dup.v2.skip(1).toList(),equalTo(ListX.of()));




    }


    @Test
    public void splitAtHeadImpl(){

        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();



        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = new Tuple2(
                t.v1.limit(1), t.v2.skip(1));
        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t.v2.skip(1).toList(),equalTo(ListX.of()));


    }
    @Test
    public void splitAtHeadImpl2(){

        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();



        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = new Tuple2(
                t.v1.limit(1), t.v2.skip(1));
        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t.v2.skip(1).toList(),equalTo(ListX.of()));


    }
    @Test
    public void splitLimit(){
        ReactiveSeq<Integer> stream = of(1);
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));

    }
    @Test
    public void splitLimit2(){
        ReactiveSeq<Integer> stream = of(1);
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(),equalTo(ListX.of(1)));

        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
    }

    @Test
    public void duplicateDuplicate(){
        for(int k=0;k<ITERATIONS;k++) {
            assertThat(of(1, 2, 3).duplicate()
                    .v1.duplicate().v1.duplicate().v1.toListX(), equalTo(ListX.of(1, 2, 3)));
        }

    }
    @Test
    public void duplicateDuplicateDuplicate(){
        for(int k=0;k<ITERATIONS;k++) {
            assertThat(of(1, 2, 3).duplicate()
                    .v1.duplicate().v1.duplicate().v1.duplicate().v1.toListX(), equalTo(ListX.of(1, 2, 3)));
        }

    }
    @Test
    public void skipDuplicateSkip() {
        assertThat(of(1, 2, 3).duplicate().v1.skip(1).duplicate().v1.skip(1).toListX(), equalTo(ListX.of(3)));
        assertThat(of(1, 2, 3).duplicate().v2.skip(1).duplicate().v2.skip(1).toListX(), equalTo(ListX.of(3)));
    }

    @Test
    public void skipLimitDuplicateLimitSkip() {
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = of(1, 2, 3).duplicate();
        Optional<Integer> head1 = dup.v1.limit(1).toOptional().flatMap(l -> {
            return l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty();
        });
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup2 = dup.v2.skip(1).duplicate();
        Optional<Integer> head2 = dup2.v1.limit(1).toOptional().flatMap(l -> {
            return l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty();
        });
        assertThat(dup2.v2.skip(1).toListX(),equalTo(ListX.of(3)));

        assertThat(of(1, 2, 3).duplicate().v1.skip(1).duplicate().v1.skip(1).toListX(), equalTo(ListX.of(3)));
    }
    @Test
    public void skipLimitTriplicateLimitSkip() {
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup = of(1, 2, 3).triplicate();
        Optional<Integer> head1 = dup.v1.limit(1).toOptional().flatMap(l -> {
            return l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty();
        });
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup2 = dup.v2.skip(1).triplicate();
        Optional<Integer> head2 = dup2.v1.limit(1).toOptional().flatMap(l -> {
            return l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty();
        });
        assertThat(dup2.v2.skip(1).toListX(),equalTo(ListX.of(3)));

        assertThat(of(1, 2, 3).duplicate().v1.skip(1).duplicate().v1.skip(1).toListX(), equalTo(ListX.of(3)));
    }
    @Test
    public void skipLimitQuadruplicateLimitSkip() {
        Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup = of(1, 2, 3).quadruplicate();
        Optional<Integer> head1 = dup.v1.limit(1).toOptional().flatMap(l -> {
            return l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty();
        });
        Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup2 = dup.v2.skip(1).quadruplicate();
        Optional<Integer> head2 = dup2.v1.limit(1).toOptional().flatMap(l -> {
            return l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty();
        });
        assertThat(dup2.v2.skip(1).toListX(),equalTo(ListX.of(3)));

        assertThat(of(1, 2, 3).duplicate().v1.skip(1).duplicate().v1.skip(1).toListX(), equalTo(ListX.of(3)));
    }


    @Test
    public void splitThenSplit(){
        assertThat(of(1,2,3).toOptional(),equalTo(Optional.of(ListX.of(1,2,3))));
       // System.out.println(of(1, 2, 3).splitAtHead().v2.toListX());
        System.out.println("split " + of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.toListX());
        assertEquals(Optional.of(3), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v1);
    }
    @Test
    public void testSplitAtHead() {

        assertEquals(Optional.empty(), of().splitAtHead().v1);
        assertEquals(asList(), of().splitAtHead().v2.toList());

        assertEquals(Optional.of(1), of(1).splitAtHead().v1);
        assertEquals(asList(), of(1).splitAtHead().v2.toList());

        assertEquals(Optional.of(1), of(1, 2).splitAtHead().v1);
        assertEquals(asList(2), of(1, 2).splitAtHead().v2.toList());

        assertEquals(Optional.of(1), of(1, 2, 3).splitAtHead().v1);
        assertEquals(Optional.of(2), of(1, 2, 3).splitAtHead().v2.splitAtHead().v1);
        assertEquals(Optional.of(3), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v1);
        assertEquals(asList(2, 3), of(1, 2, 3).splitAtHead().v2.toList());
         assertEquals(asList(3), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.toList());
        assertEquals(asList(), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v2.toList());
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
        for (int k = 0; k < 10; k++) {

            assertThat(of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    .parallelFanOut(ForkJoinPool.commonPool(), s1 -> s1.filter(i -> i % 3 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 3 == 1).map(i -> i * 100),
                            s3 -> s3.filter(i -> i % 3 == 2).map(i -> i * 1000))
                    .toListX(), Matchers.hasItems(6, 100, 2000, 12, 400, 5000, 18, 700, 8000));

        }
    }

    @Test
    public void subscribeEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().subscribe(i->result.add(i));
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
        Subscription s= of(1,2,3).subscribe(i->result.add(i));
        s.request(1l);
        Thread.sleep(100);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribe3() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).subscribe(i->result.add(i));
        s.request(3l);
        Thread.sleep(100);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribeErrorEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().subscribe(i->result.add(i),e->e.printStackTrace());
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
        Subscription s= of(1,2,3).subscribe(i->result.add(i),e->e.printStackTrace());
        s.request(1l);

        Thread.sleep(100);

        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }


    @Test
    public void subscribeErrorOnComplete() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).subscribe(i->result.add(i),e->e.printStackTrace(),()->onComplete.set(true));

        assertThat(onComplete.get(), Matchers.equalTo(false));
        s.request(1l);
        Thread.sleep(100);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
        s.request(1l);
        assertThat(onComplete.get(), Matchers.equalTo(true));
    }

}
