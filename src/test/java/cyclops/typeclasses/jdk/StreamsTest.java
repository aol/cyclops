package com.aol.cyclops.hkt.instances.jdk;
import static cyclops.higherkindedtypes.StreamKind.widen;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops2.hkt.Higher;
import cyclops.Streams;
import cyclops.collections.ListX;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.higherkindedtypes.StreamKind;
import cyclops.stream.ReactiveSeq;
import org.junit.Test;



public class StreamsTest {

    @Test
    public void unit(){
        
        StreamKind<String> list = Streams.Instances.unit()
                                     .unit("hello")
                                     .convert(StreamKind::narrowK);
        
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void functor(){
        
        StreamKind<Integer> list = Streams.Instances.unit()
                                     .unit("hello")
                                     .transform(h->Streams.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(StreamKind::narrowK);
        
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void apSimple(){
        Streams.Instances.zippingApplicative()
            .ap(widen(Stream.of(l1(this::multiplyByTwo))),widen(Stream.of(1,2,3)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        StreamKind<Fn1<Integer,Integer>> listFn =Streams.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(StreamKind::narrowK);
        
        StreamKind<Integer> list = Streams.Instances.unit()
                                     .unit("hello")
                                     .transform(h->Streams.Instances.functor().map((String v) ->v.length(), h))
                                     .transform(h->Streams.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(StreamKind::narrowK);
        
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       StreamKind<Integer> list  = Streams.Instances.monad()
                                      .flatMap(i->widen(ReactiveSeq.range(0,i)), widen(Stream.of(1,2,3)))
                                      .convert(StreamKind::narrowK);
    }
    @Test
    public void monad(){
        
        StreamKind<Integer> list = Streams.Instances.unit()
                                     .unit("hello")
                                     .transform(h->Streams.Instances.monad().flatMap((String v) ->Streams.Instances.unit().unit(v.length()), h))
                                     .convert(StreamKind::narrowK);
        
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        StreamKind<String> list = Streams.Instances.unit()
                                     .unit("hello")
                                     .transform(h->Streams.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(StreamKind::narrowK);
        
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        StreamKind<String> list = Streams.Instances.unit()
                                     .unit("hello")
                                     .transform(h->Streams.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(StreamKind::narrowK);
        
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList()));
    }
    
    @Test
    public void monadPlus(){
        StreamKind<Integer> list = Streams.Instances.<Integer>monadPlus()
                                      .plus(StreamKind.widen(Stream.of()), StreamKind.widen(Stream.of(10)))
                                      .convert(StreamKind::narrowK);
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList(10)));
    }
    /**
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<StreamKind<Integer>> m = Monoid.of(StreamKind.widen(Stream.of()), (a,b)->a.isEmpty() ? b : a);
        StreamKind<Integer> list = Streams.<Integer>monadPlus(m)
                                      .plus(StreamKind.widen(Stream.of(5)), StreamKind.widen(Stream.of(10)))
                                      .convert(StreamKind::narrowK);
        assertThat(list,equalTo(Arrays.asList(5)));
    }
**/
    @Test
    public void  foldLeft(){
        int sum  = Streams.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, StreamKind.widen(Stream.of(1,2,3,4)));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = Streams.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, StreamKind.widen(Stream.of(1,2,3,4)));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){
       Maybe<Higher<StreamKind.µ, Integer>> res = Streams.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), StreamKind.of(1,2,3))
                                                         .convert(Maybe::narrowK);
       
       
       assertThat(res.map(i->i.convert(StreamKind::narrowK).collect(Collectors.toList())),
                  equalTo(Maybe.just(ListX.of(2,4,6))));
    }
    
}
