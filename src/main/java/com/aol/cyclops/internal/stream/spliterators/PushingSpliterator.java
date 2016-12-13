package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.Setter;

public class PushingSpliterator<T> implements Spliterator<T> {

   
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
    @Getter @Setter
    Consumer<? super Throwable> error;
    @Getter
    Runnable onComplete =()->{
        hold=false;
    };
    @Setter
    volatile boolean hold = true;
    
    public void setOnComplete(Runnable onComplete){
        this.onComplete = ()->{
            onComplete.run();
            hold=false;
        };
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
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        
        return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
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
