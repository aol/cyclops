package com.aol.cyclops.control;

import static com.aol.cyclops.util.function.Predicates.anyOf;
import static com.aol.cyclops.util.function.Predicates.greaterThan;
import static com.aol.cyclops.util.function.Predicates.hasItems;
import static com.aol.cyclops.util.function.Predicates.in;
import static com.aol.cyclops.util.function.Predicates.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.types.stream.reactive.ReactiveSubscriber;

public class ReactiveSeqTest {
    @Test
    public void push(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        Executor ex= Executors.newFixedThreadPool(1);
        FutureW<List<String>> list = stream.foldFuture(s->s.collect(Collectors.toList()),ex);
        pushable.onNext("hello");
        pushable.onComplete();
        list.printOut();
        
    }
    @Test
    public void foldInt(){
        assertThat(ReactiveSeq.range(1, 1000).foldInt(i->i,s->s.map(i->i*2).filter(i->i<500).average().getAsDouble()),equalTo(250d));
    }
    @Test
    public void intOps(){
        assertThat(ReactiveSeq.range(1, 1000).ints(i->i,s->s.map(i->i*2).filter(i->i<500))
                             .size(),equalTo(249));
    }
    @Test
    public void foldLong(){
        assertThat(ReactiveSeq.rangeLong(1, 1000).foldLong(i->i,s->s.map(i->i*2).filter(i->i<500).average().getAsDouble()),equalTo(250d));
    }
    @Test
    public void longs(){
        assertThat(ReactiveSeq.rangeLong(1, 1000).longs(i->i,s->s.map(i->i*2).filter(i->i<500))
                             .size(),equalTo(249));
    }
    @Test
    public void foldDouble(){
        assertThat(ReactiveSeq.range(1, 1000).foldDouble(i->i.doubleValue(),s->s.map(i->i*2).filter(i->i<500).average().getAsDouble()),equalTo(250d));
    }
    @Test
    public void doubles(){
        assertThat(ReactiveSeq.range(1, 1000).doubles(i->i.doubleValue(),s->s.map(i->i*2).filter(i->i<500))
                             .size(),equalTo(249));
    }
    @Test
    public void ofTestInt(){
        assertThat(ReactiveSeq.ofInts(6)
                             .single(),equalTo(6));
    }
    @Test
    public void ofTestInteger(){
        assertThat(ReactiveSeq.ofInts(new Integer(6))
                             .single(),equalTo(6));
    }
    @Test
    public void ofDouble(){
        assertThat(ReactiveSeq.ofDouble(6.0)
                             .single(),equalTo(6.0));
    }
    
    @Test
    public void ofTestObj(){
        assertThat(ReactiveSeq.of("a")
                             .single(),equalTo("a"));
    }
    @Test
    public void intOpsTest(){
        assertThat(ReactiveSeq.ofInts(6)
                             .single(),equalTo(6));
    }
    @Test
    public void coflatMap(){
        
       assertThat(ReactiveSeq.of(1,2,3)
                   .coflatMap(s->s.sum().get())
                   .single(),equalTo(6));
        
    }
    @Test
    public void test1() {
        ReactiveSeq.of(1, 2, 3).filter(anyOf(not(in(2, 3, 4)), in(1, 10, 20)));
    }

    @Test
    public void test2() {
        ReactiveSeq.of(1, 2, 3).filter(anyOf(not(in(2, 3, 4)), greaterThan(10)));
    }

    @Test
    public void test3() {
        ReactiveSeq.of(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5)).filter(hasItems(Arrays.asList(2, 3)));
    }
    
    @Test
    public void test4() {
        ReactiveSeq.of(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5)).filter(not(hasItems(Arrays.asList(2, 3))));
    }
    
    @Test
    public void test() {
        
        Predicate<? super Integer> inOne = in(2.4,3,4);
        Predicate<? super Integer> inTwo = in(1,10,20);
        ReactiveSeq.of(1,2,3).filter(anyOf(not(inOne),inTwo));
        ReactiveSeq.of(1,2,3).filter(anyOf(not(in(2.4,3,4)),in(1,10,20)));
    }
    
}
