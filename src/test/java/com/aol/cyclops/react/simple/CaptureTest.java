package com.aol.cyclops.react.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import cyclops.async.LazyReact;
import cyclops.async.SimpleReact;
import com.aol.cyclops.react.SimpleReactFailedStageException;

public class CaptureTest {

    private String exception(String in){
        throw new InternalException();
    }
    @Test
    public void capture() throws InterruptedException{
        t=null;
        new SimpleReact().of("hello","world")
                         .capture(e->t=e)
                         .peek(System.out::println)
                         .then(this::exception)
                         .peek(System.out::println)
                         .then(s->"hello"+s)
                         .run();
                         
        Thread.sleep(500);                 
        assertNotNull(t);
        assertFalse(t.toString(),t instanceof SimpleReactFailedStageException);
        assertTrue(t.toString(),t instanceof InternalException);
    }
    @Test
    public void captureLast() throws InterruptedException{
        t=null;
        new SimpleReact().of("hello","world")
                         .capture(e->t=e)
                         .peek(System.out::println)
                         .peek(System.out::println)
                         .then(s->"hello"+s)
                         .then(this::exception)
                         .run();
        
                         
        Thread.sleep(500);                 
        assertNotNull(t);
        assertFalse(t.toString(),t instanceof SimpleReactFailedStageException);
        assertTrue(t.toString(),t instanceof InternalException);
    }
    AtomicInteger count;
    @Test
    public void captureErrorOnce() throws InterruptedException{
       count = new AtomicInteger(0);
        new SimpleReact().of("hello","world")
                         .capture(e->count.incrementAndGet())
                         .peek(System.out::println)
                         .then(this::exception)
                         .peek(System.out::println)
                         .then(s->"hello"+s)
                         .run();
                         
        Thread.sleep(500);                 
        assertEquals(count.get(),2);
    }
    @Test
    public void captureBlock(){
        t=null;
        new SimpleReact().of("hello","world").capture(e->t=e)
                         .peek(System.out::println)
                         .then(this::exception)
                         .peek(System.out::println)
                         .block();
                         
        assertNotNull(t);
        t.printStackTrace();
        assertFalse(t.toString(),t instanceof SimpleReactFailedStageException);
        assertTrue(t.toString(),t instanceof InternalException);
    }
    Throwable t;
    @Test
    public void captureLazy(){
        t=null;
        new LazyReact().of("hello","world")
                        .capture(e->t=e)
                        .peek(System.out::println)
                        .then(this::exception)
                        .forEach(System.out::println);
                         
        assertNotNull(t);
        assertFalse(t.toString(),t instanceof SimpleReactFailedStageException);
        assertTrue(t.toString(),t instanceof InternalException);
    }
    
    private static class InternalException extends RuntimeException{
        
    }
}
