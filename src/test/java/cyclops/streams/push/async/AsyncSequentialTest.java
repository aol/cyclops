package cyclops.streams.push.async;

import com.aol.cyclops2.streams.BaseSequentialTest;
import cyclops.CyclopsCollectors;
import cyclops.async.QueueFactories;
import cyclops.async.Topic;
import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.hamcrest.Matchers;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class AsyncSequentialTest extends BaseSequentialTest {
    @Override
    protected <U> ReactiveSeq<U> of(U... array){

        return Spouts.async(s->{
            Thread t = new Thread(()-> {
                for (U next : array) {
                    s.onNext(next);
                }
                s.onComplete();
            });
            t.start();
        });
    }
    @Test
    public void mergeAdapterTest1(){
        for(int k=0;k<10;k++) {
            cyclops.async.Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
                    .build();

            Thread t = new Thread(() -> {

                while (true) {

                    queue.add(1);
                    queue.add(2);
                    queue.add(3);
                    try {
                        System.out.println("Sleeping!");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Waking!");
                    System.out.println("Closing! " + queue.size());
                    queue.close();

                }
            });
            t.start();
            assertThat(this.<Integer>of(10).peek(i -> System.out.println("publishing " + i))
                    .merge(queue).collect(Collectors.toList()), equalTo(ListX.of(1, 2, 3)));
        }
    }
    @Test
    public void spoutsCollect(){
        Integer[] array = new Integer[100];
        for(int i=0;i<array.length;i++) {
            array[i]=i;
        }
        for(int i=0;i<100;i++) {
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
        for(int i=0;i<1_000;i++) {
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
    public void publishToAndMerge(){
        cyclops.async.Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
                .build();

        Thread t=  new Thread( ()-> {

            while(true) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Closing! " + queue.size() + " " + queue.get());
              //  queue.close();

            }
        });
        t.start();

        AtomicBoolean  complete = new AtomicBoolean(false);
        of(1,2,3)
                .publishTo(queue)
                .peek(System.out::println)
                .merge(queue)
                .forEach(e->System.out.println("Result " + e),e->{},()->complete.set(true));
        while(!complete.get()){

        }

        /**
        assertThat(of(1,2,3)
                .publishTo(queue)
                .peek(System.out::println)
                .merge(queue)
                .toListX(), Matchers.equalTo(ListX.of(1,1,2,2,3,3)));
                **/
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
    public void fanOut2(){
        assertThat(of(1,2,3,4)
                .fanOut(s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100))
                .toListX(), Matchers.equalTo(ListX.of(4,100,8,300)));
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
        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
    }
}
