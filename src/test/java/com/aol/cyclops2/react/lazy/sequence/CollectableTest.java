package com.aol.cyclops2.react.lazy.sequence;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.Agg.count;
import static org.jooq.lambda.Agg.max;
import static org.jooq.lambda.Agg.min;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jooq.lambda.Collectable;
import org.junit.Before;
import org.junit.Test;

public abstract class CollectableTest {
    public abstract <T> Collectable<T> of(T... values);
    Collectable<Integer> empty;
    Collectable<Integer> nonEmpty;
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
            Supplier<Collectable<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

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
    @Test
    public void testCount2() {
        assertEquals(0L, of().count());
        assertEquals(0L, of().countDistinct());
        assertEquals(0L, this.<Integer>of().countDistinctBy(l -> l % 3));

        assertEquals(1L, of(1).count());
        assertEquals(1L, of(1).countDistinct());
        assertEquals(1L, of(1).countDistinctBy(l -> l % 3L));

        assertEquals(2L, of(1, 2).count());
        assertEquals(2L, of(1, 2).countDistinct());
        assertEquals(2L, of(1, 2).countDistinctBy(l -> l % 3L));

        assertEquals(3L, of(1, 2, 2).count());
        assertEquals(2L, of(1, 2, 2).countDistinct());
        assertEquals(2L, of(1, 2, 2).countDistinctBy(l -> l % 3L));

        assertEquals(4L, of(1, 2, 2, 4).count());
        assertEquals(3L, of(1, 2, 2, 4).countDistinct());
        assertEquals(2L, of(1, 2, 2, 4).countDistinctBy(l -> l % 3L));
    }
    
    @Test
    public void testCountWithPredicate() {
        Predicate<Integer> pi = i -> i % 2 == 0;
        Predicate<Long> pl = l -> l % 2 == 0;
        
        assertEquals(0L, this.<Integer>of().count(pi));
        assertEquals(0L, this.<Integer>of().countDistinct(pi));
        assertEquals(0L, this.<Integer>of().countDistinctBy(l -> l % 3, pi));

        assertEquals(0L, of(1).count(pi));
        assertEquals(0L, of(1).countDistinct(pi));
        assertEquals(0L, of(1).countDistinctBy(l -> l % 3L, pl));

        assertEquals(1L, of(1, 2).count(pi));
        assertEquals(1L, of(1, 2).countDistinct(pi));
        assertEquals(1L, of(1, 2).countDistinctBy(l -> l % 3L, pl));

        assertEquals(2L, of(1, 2, 2).count(pi));
        assertEquals(1L, of(1, 2, 2).countDistinct(pi));
        assertEquals(1L, of(1, 2, 2).countDistinctBy(l -> l % 3L, pl));

        assertEquals(3L, of(1, 2, 2, 4).count(pi));
        assertEquals(2L, of(1, 2, 2, 4).countDistinct(pi));
        assertEquals(1L, of(1, 2, 2, 4).countDistinctBy(l -> l % 3L, pl));
    }
    
    @Test
    public void testSum() {
        assertEquals(Optional.empty(), of().sum());
        
        assertEquals(Optional.of(1), of(1).sum());
        assertEquals(Optional.of(3), of(1, 2).sum());
        assertEquals(Optional.of(6), of(1, 2, 3).sum());
        
        assertEquals(Optional.of(1.0), of(1.0).sum());
        assertEquals(Optional.of(3.0), of(1.0, 2.0).sum());
        assertEquals(Optional.of(6.0), of(1.0, 2.0, 3.0).sum());
    }
    
    @Test
    public void testAvg() {
        assertEquals(Optional.empty(), of().avg());
        
        assertEquals(Optional.of(1), of(1).avg());
        assertEquals(Optional.of(1), of(1, 2).avg());
        assertEquals(Optional.of(2), of(1, 2, 3).avg());
        
        assertEquals(Optional.of(1.0), of(1.0).avg());
        assertEquals(Optional.of(1.5), of(1.0, 2.0).avg());
        assertEquals(Optional.of(2.0), of(1.0, 2.0, 3.0).avg());
    }

    @Test
    public void testCollect() {
        assertEquals(
            tuple(0L, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
           this.<Integer>of().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
        );

        assertEquals(
            tuple(1L, Optional.of(1), Optional.of(-1), Optional.of(1), Optional.of(-1)),
            of(1).collect(count(), min(), min(i -> -i), max(), max(i -> -i))
        );

        assertEquals(
            tuple(2L, Optional.of(1), Optional.of(-2), Optional.of(2), Optional.of(-1)),
            of(1, 2).collect(count(), min(), min(i -> -i), max(), max(i -> -i))
        );

        assertEquals(
            tuple(3L, Optional.of(1), Optional.of(-3), Optional.of(3), Optional.of(-1)),
            of(1, 2, 3).collect(count(), min(), min(i -> -i), max(), max(i -> -i))
        );

        assertEquals(
            tuple(4L, Optional.of(1), Optional.of(-4), Optional.of(4), Optional.of(-1)),
            of(1, 2, 3, 4).collect(count(), min(), min(i -> -i), max(), max(i -> -i))
        );
        
        assertEquals(
            Arrays.asList("a", "b", "c"),
            of("a", "b", "c").collect(Collectors.toList())
        );
    }

}
