package com.aol.cyclops2.react.lazy;

import com.aol.cyclops2.react.ThreadPools;
import com.aol.cyclops2.react.async.subscription.Subscription;
import cyclops.async.LazyReact;
import cyclops.async.adapters.Queue;
import cyclops.async.QueueFactories;
import cyclops.stream.StreamSource;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by johnmcclean on 13/04/2017.
 *
 * See https://github.com/aol/cyclops-react/issues/521
 */
@Ignore
public class BatchingInvestigationsTest {

    @Test
    public void streamBatch(){
        Queue<String> queue = QueueFactories.<String>unboundedQueue().build();
        new Thread(()->{
            for(int i=0;i<10;i++){

                queue.offer("New message " + i);
                sleep(10000);
            }
            queue.close();
        }).start();

        long toRun = TimeUnit.MILLISECONDS.toNanos(500l);

        queue.streamBatch(new Subscription(), source->{

            return ()->{
                List<String> result = new ArrayList<>();


                long start = System.nanoTime();

                while (result.size() < 10 && (System.nanoTime() - start) < toRun) {
                    try {
                        String next = source.apply(1l, TimeUnit.MILLISECONDS);
                        if (next != null) {
                            result.add(next);
                        }
                    }catch(Queue.QueueTimeoutException e){

                    }


                }

                if(result.size()>0){
                    System.out.println("Result " +  result);
                }


                start=System.nanoTime();


                return result;
            };
        }).filter(l->l.size()>0).to()
                .futureStream(new LazyReact(ThreadPools.getSequential()))
                .async()
                .peek(System.out::println)
                .run();


        while(true){

        }
    }
    @Test
    public void batchIssue() throws InterruptedException {
        Queue<String> queue = QueueFactories.<String>unboundedQueue().build();
        new Thread(()->{
            for(int i=0;i<10;i++){

                queue.offer("New message " + i);
                sleep(10000);
            }
        }).start();


        queue.stream()
                .groupedBySizeAndTime(10,500,TimeUnit.MILLISECONDS).to()
                .futureStream(new LazyReact(ThreadPools.getSequential()))
                .async()
                .peek(System.out::println)
                .run();

        while(true){

        }

    }
    @Test
    public void batchIssueStreamSource() throws InterruptedException {
        Queue<String> queue = QueueFactories.<String>unboundedQueue().build();
        new Thread(()->{
        while (true) {
            sleep(1000);
            queue.offer("New message " + System.currentTimeMillis());
        }
    }).start();

        StreamSource.futureStream(queue, new LazyReact(ThreadPools.getSequential()))
                .groupedBySizeAndTime(10,500,TimeUnit.MILLISECONDS)
                .forEach(i->System.out.println(i + " Batch Time:" + System.currentTimeMillis()));

        while(true){

        }

    }

    @Test
    public void groupedBySizeAndTimeNoQueue(){
        new LazyReact().generate(()-> { sleep(1000); return "New message " + System.currentTimeMillis();})
                .map(i->i+ "  t " + Thread.currentThread().getId())
                .groupedBySizeAndTime(10,500,TimeUnit.MILLISECONDS)
                .elapsed()
                .forEach(i-> {
                    System.out.println(i + " Batch Time:" + System.currentTimeMillis());
                });
    }
    private boolean sleep(int i) {

        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        return true;

    }
}
