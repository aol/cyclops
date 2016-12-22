package com.aol.cyclops.control;

import cyclops.Monoids;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.stream.reactive.ReactiveSubscriber;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static cyclops.function.Predicates.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class ReactiveSeqTest {
    AtomicBoolean active = new AtomicBoolean(true);

    @Test
    public void testParallel(){
        assertThat(ReactiveSeq.range(0,1000)
                              .parallel(s->s.map(i->i*2))
                              .count(),equalTo(1000L));
    }

    @Test
    public void fillReplay(){
        ReactiveSeq<Integer> seq = ReactiveSeq.fill(1);
        ReactiveSeq<Integer> seq1 = seq.take(100).map(i->i*2);

        seq.take(100).forEach(System.out::println);
        seq1.forEach(System.err::println);

    }

    @Test
    public void testReplay(){
        Flux<Integer> f1 = Flux.range(0,100);
        Flux<Integer> f2 = f1.map(i->i*2);
        System.out.println(f1.count().get());
        System.out.println(f2.count().get());

        ReactiveSeq<String> stream = ReactiveSeq.of("hello","world");
        ReactiveSeq<String> stream1 = stream.map(str->"hello world " + str);
        stream.forEach(System.out::println);
        stream1.forEach(System.out::println);

        ReactiveSeq<Integer> streama = ReactiveSeq.range(1,100);
        ReactiveSeq<Integer> streamb = streama.map(i->i*2);

        System.out.println(streama.count());
        System.out.println(streama.map(i->i*3).zipWithIndex().count());
        System.out.println(streamb.zipWithIndex().count());
    }
    @Test
    public void replayStream(){
       
        ReactiveSeq<String> stream = ReactiveSeq.of("hello","world");
        ReactiveSeq<String> stream1 = stream.map(str->"hello world " + str);
        Spliterator<String> sp = stream1.spliterator();
        
        ReactiveSeq.fromSpliterator(sp).forEach(System.out::println);
       
        ReactiveSeq.fromSpliterator(sp).forEach(System.err::println);
        
    }
    @Test
    public void replay(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        ReactiveSeq<String> stream1 = stream.map(str->"hello world " + str);
        Spliterator<String> sp = stream1.spliterator();
        pushable.onNext("hello");
       
        pushable.onComplete();
        ReactiveSeq.fromSpliterator(sp).forEach(System.out::println);
        pushable.onNext("world");
        
        pushable.onComplete();
        ReactiveSeq.fromSpliterator(sp).forEach(System.err::println);
        
    }
        
    @Test
    public void block(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        new Thread(()->{active.set(false); pushable.onComplete();}).run();
        stream.forEach(System.out::println);
        assertFalse(active.get());
    }
    @Test
    public void blockToList(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        new Thread(()->{active.set(false); pushable.onComplete();}).run();
        stream.toList();
        assertFalse(active.get());
    }
    @Test
    public void blockToListAddOne(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        new Thread(()->{
            pushable.onNext("hello");
            active.set(false);
            pushable.onComplete();
        }).run();
        assertThat(stream.toList().size(),equalTo(1));
        assertFalse(active.get());
    }
    
    @Test
    public void limitLast(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        pushable.onNext("hello1");
        
        pushable.onNext("hello2");
        pushable.onNext("hello3");
        pushable.onComplete();
       // stream.printOut();
        stream.limitLast(2).zipS(Stream.of(1,2)).printOut();
    }

    @Test
    public void testFlatMap(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();

        pushable.onNext("hello");
        pushable.onComplete();
        stream.map(s->s.length())
                .flatMap(s-> IntStream.range(0,s).boxed())
                .forEach(System.out::println);
        pushable.onNext("world");
    }
    @Test
    public void testIterator(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();

        pushable.onNext("hello");
        pushable.onComplete();
        Iterator<Integer> it = stream.map(s->s.length())
                                     .flatMap(s-> IntStream.range(0,s).boxed())
                                     .iterator();
        ListX<Integer> result = ListX.empty();
        while(it.hasNext()){
            result.add(it.next());
        }
       assertThat(result,equalTo(ListX.of(0,1,2,3,4)));
    }
    @Test
    public void testIteratorPull(){

        ReactiveSeq<String> stream = ReactiveSeq.of("hello");


        Iterator<Integer> it = stream.map(s->s.length())
                .flatMap(s-> IntStream.range(0,s).boxed())
                .iterator();
        ListX<Integer> result = ListX.empty();
        while(it.hasNext()){
            result.add(it.next());
        }
        assertThat(result,equalTo(ListX.of(0,1,2,3,4)));
    }
    @Test
    public void forEachWithError(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();

        pushable.onNext("hello");
        pushable.onComplete();
        stream.map(s->s.length())
                .flatMap(s-> IntStream.range(0,s).boxed())
                .forEachWithError(System.out::println,System.err::println);
        pushable.onNext("world");
    }
    
    @Test
    public void zip(){
        Stream<Integer> s = Stream.of(1,2,3);
        Iterator<Integer> it = s.iterator();
        int i = it.next();
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        new Thread(()->{
            pushable.onNext("hello");
            active.set(false);
            pushable.onComplete();
        }).run();

        assertThat(stream.zipS(Stream.of(1,2)).toList().size(),equalTo(1));
        assertFalse(active.get());
    }
    
    @Test
    public void lazy(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        
        Eval<List<String>> list = stream.peek(System.err::println)
                                        .foldLazy(s->s.collect(Collectors.toList()));
      
        pushable.onNext("hello");
        pushable.onComplete();
        assertThat(list.get().size(),equalTo(1));
        
    }
    @Test
    public void push(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        Executor ex= Executors.newFixedThreadPool(1);
        FutureW<List<String>> list = stream.peek(System.err::println)
                                           .foldFuture(s->s.collect(Collectors.toList()),ex);
      
        pushable.onNext("hello");
        pushable.onComplete();
        assertThat(list.get().size(),equalTo(1));
        
    }
   
    
    @Test
    public void fold(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        ReactiveSeq<Integer> res = stream.map(s->s.length()).fold(Monoids.intSum);
        pushable.onNext("hello");
        pushable.onNext("world");
        pushable.onComplete();
        assertThat(res.single(),equalTo(10));
        
    }
    @Test
    public void collect(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> stream = pushable.stream();
        ReactiveSeq<List<Integer>> res = stream.map(s->s.length()).collectSeq(Collectors.toList());
        pushable.onNext("hello");
        pushable.onNext("world");
        pushable.onComplete();
        assertThat(res.single().size(),equalTo(2));
        
    }
    @Test
    public void foldInt(){
        Double res= ReactiveSeq.range(1, 1000).mapToInt(i->i).map(i->i*2).filter(i->i<500).average().getAsDouble();
        assertThat(res,equalTo(250d));
    }
    @Test
    public void intOps(){
        assertThat(ReactiveSeq.range(1, 1000).ints(i->i,s->s.map(i->i*2).filter(i->i<500))
                             .size(),equalTo(249));
    }

    @Test
    public void longs(){
        assertThat(ReactiveSeq.rangeLong(1, 1000).longs(i->i,s->s.map(i->i*2).filter(i->i<500))
                             .size(),equalTo(249));
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
                   .coflatMap(s->s.sumInt(i->i))
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
