package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import lombok.Value;

public class InvokeDynamicGuavaTest {

    @Value
    static class MyStream<T>{
        T value;
        public static <T> MyStream<T> of(T value){
            return new MyStream<>(value);
        }
        public static <T> MyStream<T> empty(){
            return new MyStream<>(null);
        }
        public <R> MyStream<R> map(Function<? super T, ? extends R> fn){
            return new MyStream<R>(fn.apply(value));
        }
        public <R> MyStream<R> flatMap(Function<? super T, ? extends MyStream<R>> fn){
            return fn.apply(value);
        }
        public T get(){
            return value;
        }
    }
    @Test
    public void map(){
       FluentIterable<Integer> res = AnyM.<Integer>ofSeq(FluentIterable.from(Arrays.asList(1,2,3))).map(i->i+2).unwrap();
       assertThat(res.toList(),equalTo(Arrays.asList(3,4,5)));
    }
    @Test
    public void filter(){
     
       FluentIterable<Integer> res = AnyM.<Integer>ofSeq(FluentIterable.from(Arrays.asList(1)))
               .filter(i->i>0).unwrap();
       assertThat(res.toList(),equalTo(ListX.of(1)));
    }
    @Test
    public void filterEmpty(){
       FluentIterable<Integer> res = AnyM.<Integer>ofSeq(FluentIterable.from(Arrays.asList()))
               .filter(i->i>10).unwrap();
       assertThat(res.toList(),equalTo(Arrays.asList()));
    }
    @Test
    public void flatMap2(){
      List<Integer> res = AnyM.<Integer>ofSeq(Arrays.asList(1))
                                         .flatMap(i-> AnyM.ofSeq(FluentIterable.from(Arrays.asList(2)))).unwrap();
      
       System.out.println(res);
       assertThat(res,equalTo(Arrays.asList(2)));
    }
    @Test @Ignore
    public void safeCrossTypeFlatMapflatMap3(){
       FluentIterable res = AnyM.<Integer>ofSeq(FluentIterable.from(Arrays.asList(1)))
                                         .flatMap(i-> AnyM.ofSeq(Arrays.asList(2))).unwrap();
      
       System.out.println(res);
       assertThat(res.toList(),equalTo(Arrays.asList(2)));
    }
    @Test
    public void flatMap(){
       FluentIterable res = AnyM.<Integer>ofSeq(FluentIterable.from(Arrays.asList(1)))
           
                                      .flatMap((Integer i)-> { System.out.println(i);
                                            return AnyM.ofSeq(FluentIterable.from(Arrays.asList(2)));
                                         }).unwrap();
      
       System.out.println(res);
       assertThat(res.toList(),equalTo(Arrays.asList(2)));
    }
    @Test
    public void flatMapCrossType(){
       Maybe<Integer> res = AnyM.<Integer>ofValue(Maybe.of(1))
               .flatMap(i-> AnyM.ofValue(Optional.of(2))).unwrap();
       assertThat(res.get(),equalTo(2));
    }
    @Test
    public void flatMapCrossTypeEmpty(){
       Maybe<Integer> res = AnyM.<Integer>ofValue(Maybe.of(1))
               .flatMap(i-> AnyM.ofValue(Optional.fromNullable(null))).unwrap();
       assertThat(res.isPresent(),equalTo(false));
    }
   
   
}

