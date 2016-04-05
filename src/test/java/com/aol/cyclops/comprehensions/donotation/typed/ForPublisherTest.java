package com.aol.cyclops.comprehensions.donotation.typed;

import static com.aol.cyclops.control.For.Publishers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Test;


import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;

import lombok.val;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ForPublisherTest {

    @Test
    public void forGen2(){
        
        val list = each2(Flux.range(1,10), 
                          i-> ReactiveSeq.iterate(i,a->a+1).limit(10),
                          Tuple::tuple).toListX();
        
        val list2 = Flux.range(1,10).flatMap(i-> ReactiveSeq.iterate(i,a->a+1)
                                                            .limit(10)
                                                            .map(a->Tuple.tuple(i,a))).toList().get();
                                    
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGenFilter2(){
        
        val list = each2(Flux.range(1,10), 
                          i-> ReactiveSeq.iterate(i,a->a+1).limit(10),
                          (a,b)->a+b<10,
                          Tuple::tuple).toListX();
        
        val list2 = Flux.range(1,10).flatMap(i-> ReactiveSeq.iterate(i,a->a+1)
                                                            .limit(10)
                                                            .filter(a->i+a<10)
                                                            .map(a->Tuple.tuple(i,a))).toList().get();
                                    
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGen3(){
        val list = each3(Flux.range(1,10), 
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>of(a+b),
                            Tuple::tuple).toListX();
        
        
        val list2 = Flux.range(1,10).flatMap(a-> ReactiveSeq.iterate(a,i->i+1)
                                                            .limit(10)
                                                            .flatMapPublisher(b->
                                                                Maybe.<Integer>of(a+b)
                                                                     .<Tuple3<Integer,Integer,Integer>>map(c->Tuple.tuple(a,b,c))))
                                                                .toList().get();
                                
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGenFilter3(){
        
        val list = each3(Flux.range(1,10), 
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>of(a+b),
                            (a,b,c) ->a+b+c<10,
                            Tuple::tuple).toListX();


        val list2 = Flux.range(1,10).flatMap(a-> ReactiveSeq.iterate(a,i->i+1)
                                                .limit(10)
                                                .flatMapPublisher(b->
                                                    Maybe.<Integer>of(a+b)
                                                        .filter(c->a+b+c<10)
                                                         .<Tuple3<Integer,Integer,Integer>>map(c->Tuple.tuple(a,b,c))))
                                                    .toList().get();
                    
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGen4(){
        val list = each4(Flux.range(1,10), 
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>of(a+b),
                            (a,b,c) -> Mono.<Integer>just(a+b+c),
                            Tuple::tuple)
                            .toListX();
        
        
          
        val list2 = Flux.range(1,10).flatMap(a-> ReactiveSeq.iterate(a,i->i+1)
                                                            .limit(10)
                                                            .flatMapPublisher(b->
                                                                Mono.<Integer>just(a+b)
                                                                      .flatMap(c->
                                                                            Maybe.<Integer>just(a+b+c)
                                                                     .<Tuple4<Integer,Integer,Integer,Integer>>map(d->Tuple.tuple(a,b,c,d)))))
                                                                .toList().get();
                            
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGenFilter4(){
        
        val list = each4(Flux.range(1,10), 
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>of(a+b),
                            (a,b,c) -> Mono.<Integer>just(a+b+c),
                            (a,b,c,d) -> a+b+c+d <100,
                                Tuple::tuple)
                            .toListX();
        
        System.out.println(list);



        val list2 = Flux.range(1,10).flatMap(a-> ReactiveSeq.iterate(a,i->i+1)
                                                .limit(10)
                                                .flatMapPublisher(b->
                                                    Maybe.<Integer>just(a+b)
                                                          .flatMap(c->
                                                                Maybe.<Integer>just(a+b+c)
                                                          .filter(d->a+b+c+d<100)
                                                         .<Tuple4<Integer,Integer,Integer,Integer>>map(d->Tuple.tuple(a,b,c,d)))))
                                                    .toList().get();
                
        assertThat(list,equalTo(list2));
                    
    }
}