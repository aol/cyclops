package com.aol.cyclops.internal.stream.spliterators.push;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.Setter;

public class PushingSpliterator<T> implements Spliterator<T> {

    //TODO push based analoguges for the following Spliterators / operators
    //recover, iterator, foreach (error, oncomplete), zipXXX, groupXXX
    //limitXXX, skipXXX, cycleXXXX, flatMap

   
    public static void main(String[] args) throws InterruptedException{
        PushingSpliterator<String> push =new PushingSpliterator<String>();
        Stream<String> s = StreamSupport.stream(push,false);
        new  Thread(()->s.forEach(System.out::println)).start();
        Thread.sleep(1000);
        push.action.accept("hello");
        push.action.accept("world");
    }
    
    @Getter
    Consumer<? super T> action;
    @Getter @Setter //error handlers should be chained together, and recoverable errors pushed to the appropriate spliterator
                    //with the recovered value (may or may not be possible, we will find out)
    Consumer<? super Throwable> error;
    @Getter
    Runnable onComplete =()->{
        hold=false;
    };
    @Setter
    volatile boolean hold = true;
    volatile List<T> capture;
    public void setOnComplete(Runnable onComplete){
        this.onComplete = ()->{
            onComplete.run();
            hold=false;
        };
    }
    public void capture(T next){
        
        if(!hold)
            return;
        if(capture==null){
            capture = new ArrayList<>(10);
        }
        capture.add(next);
    
    }
    /* (non-Javadoc)
     * @see java.util.Spliterator#forEachRemaining(java.util.function.Consumer)
     */
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        this.action = action;
        while(hold){
            LockSupport.parkNanos(10l);
        }
        
        if(capture!=null){
            capture.stream().forEach(c->action.accept(c));
            capture=null;
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(this.action==null)
            this.action = action;
        if(capture!=null && capture.size()>0){
            action.accept(capture.remove(0));
        }
        return (capture!=null && capture.size()>0) || hold;
    }

    @Override
    public Spliterator<T> trySplit() {
        return this;
    }

    @Override
    public long estimateSize() {
        
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        
        return 0;
    }

}
