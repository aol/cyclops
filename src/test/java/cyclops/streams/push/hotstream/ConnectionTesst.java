package cyclops.streams.push.hotstream;

import com.aol.cyclops2.types.FoldableTraversable;
import com.aol.cyclops2.types.stream.PausableHotStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class ConnectionTesst {
    volatile boolean active;
    static final Executor exec2 = Executors.newFixedThreadPool(5);
    static final Executor exec3 = Executors.newFixedThreadPool(5);
    volatile AtomicInteger value = new AtomicInteger(-1);
    @Test
    public void hotStreamConnectPausableConnect() throws InterruptedException{
        value.set(-1);
        active=true;
        CountDownLatch latch = new CountDownLatch(1);
        PausableHotStream<Integer> s = Spouts.range(0,Integer.MAX_VALUE)
                .limitWhile(i->active)
                .peek(v->value.set(v))
                .peek(v->latch.countDown())
                .pausableHotStream(exec2);
       Integer oldValue = value.get();
       s.connect()
                .limit(10000)
                .runFuture(exec3,x->x.forEach(System.out::println,System.err::println));

        
        
        s.pause();
        s.unpause();

        while( value.get()<10_000){
            Thread.sleep(1000);
        }
        s.pause();
        assertTrue("value= " +  value + " old value " + oldValue,value.get()!=oldValue);
        s.unpause();
        latch.await();
        assertTrue(value!=null);
        active=false;
    }
}
