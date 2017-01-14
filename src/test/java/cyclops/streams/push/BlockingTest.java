package cyclops.streams.push;

import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class BlockingTest {
    @Test
    public void blockingOrNot(){
        ReactiveSubscriber<Integer> sub = Spouts.subscriber();

        System.out.println("Starting!");
        Thread t =  new Thread(()->{
            while(!sub.isInitialized()){
                System.out.println(sub.isInitialized());
                LockSupport.parkNanos(0l);
            }
            for(int i=0;i<100;i++){
                sub.onNext(1);
            }
            System.out.println("On Complete!");
            sub.onComplete();
        });

        t.start();

        //sub.onComplete();
        sub.stream().peek(System.out::println).collect(Collectors.toList());
      //   sub.stream().forEach(System.out::println);
        sub.onNext(1);
        System.out.println("End!");
        while(true){

        }
    }

    @Test
    public void spoutCollect(){
        assertThat(Spouts.of(1,2,3).collect(Collectors.toList()),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void findFirst(){
        assertThat(Spouts.of(1,2,3).findFirst().get(),equalTo(1));
    }

    @Test
    public void testIterator(){
        assertThat(Spouts.of(1).iterator().next(),equalTo(1));
        assertThat(Spouts.of(1).iterator().hasNext(),equalTo(true));
        Iterator<Integer> it = Spouts.of(1,2).iterator();
        assertThat(it.hasNext(),equalTo(true));
        assertThat(it.next(),equalTo(1));
        assertThat(it.hasNext(),equalTo(true));
        assertThat(it.next(),equalTo(2));
        assertThat(it.hasNext(),equalTo(false));

    }

    Integer result = null;
    @Test
    public void simple(){

        Spouts.of(1).forEach(i->result = i);
        assertThat(result,equalTo(1));
        Spouts.of(1).map(i->i*2).forEach(i->result = i);
        assertThat(result,equalTo(2));
    }
    @Test
    public void simpleList(){
        List<Integer> list = new ArrayList<>();
        Spouts.of(1,2).forEach(list::add);
        assertThat(list,equalTo(ListX.of(1,2)));
        list = new ArrayList<>();
        Spouts.of(1,2).map(i->i*2).forEach(list::add);
        assertThat(list,equalTo(ListX.of(2,4)));
    }


    @Test
    public void collect(){
        ReactiveSubscriber<Integer> sub = Spouts.subscriber();
        ReactiveSeq.of(1,2,3).peek(System.out::println).subscribe(sub);
        System.out.println(sub.stream().peek(System.out::println).collect(Collectors.toList()));
    }
    @Test
    public void forEach(){
        ReactiveSubscriber<Integer> sub = Spouts.subscriber();
        ReactiveSeq.of(1,2,3).subscribe(sub);
        sub.stream().forEach(System.out::println);
    }
    @Test
    public void init(){
        ReactiveSubscriber<Integer> sub = Spouts.subscriber();
        sub.stream().forEach(System.out::println);
        assertTrue(sub.isInitialized());
    }
}
