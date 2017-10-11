package com.aol.cyclops2.react.lazy.sequence;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static cyclops.data.tuple.Tuple.tuple;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.aol.cyclops2.types.foldable.Folds;

import org.junit.Before;
import org.junit.Test;

public abstract class CollectableTest {
    public abstract <T> Folds<T> of(T... values);
    Folds<Integer> empty;
    Folds<Integer> nonEmpty;
    static final Executor exec = Executors.newFixedThreadPool(1);
    @Before
    public void setup(){
        empty = of();
        nonEmpty = of(1);
        
    }
    @Test
    public void testMax(){
        assertThat(of(1,2,3,4,5).max((t1,t2) -> t1-t2)
                .get(),is(5));
    }
    @Test
    public void testMin(){
        assertThat(of(1,2,3,4,5).min((t1,t2) -> t1-t2)
                .get(),is(1));
    }
 
    
    
 
    
   
    @Test
    public void testAnyMatch(){
        assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(3)),is(true));
    }
    @Test
    public void testAllMatch(){
        assertThat(of(1,2,3,4,5).allMatch(it-> it>0 && it <6),is(true));
    }
    @Test
    public void testNoneMatch(){
        assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),is(true));
    }
    
    
    @Test
    public void testAnyMatchFalse(){
        assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(8)),is(false));
    }
    @Test
    public void testAllMatchFalse(){
        assertThat(of(1,2,3,4,5).allMatch(it-> it<0 && it >6),is(false));
    }
   
    @Test
    public void testToCollection() {
        assertThat( Arrays.asList(1,2,3,4,5),equalTo(of(1,2,3,4,5)
                .toCollection(()->new ArrayList())));
    }

    @Test
    public void testCount(){
        assertThat(of(1,5,3,4,2).count(),is(5L));
    }

    
    @Test
    public void collect(){
        assertThat(of(1,2,3,4,5).collect(Collectors.toList()).size(),is(5));
        assertThat(of(1,1,1,2).collect(Collectors.toSet()).size(),is(2));
    }
    
    protected Object value() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "jello";
    }
    protected int value2() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 200;
    }
    
    
       
       
        

        

        
       
      
        @Test
        public void testMinByMaxBy() {
            Supplier<Folds<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

            assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
            assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

            assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
            assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
        }

      
       

       
        
    
    protected Object sleep(int i) {
        try {
            Thread.currentThread().sleep(i);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return i;
    }



}
