package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.function.Function;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;

import lombok.Value;

public class InvokeDynamicTest {

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
       MyStream<Integer> res = AnyM.<Integer>ofValue(MyStream.of(1)).map(i->i+2).unwrap();
       assertThat(res.get(),equalTo(3));
    }
    @Test
    public void filter(){
       MyStream<Integer> res = AnyM.<Integer>ofValue(MyStream.of(1))
               .filter(i->i>0).unwrap();
       assertThat(res.get(),equalTo(1));
    }
    @Test
    public void filterEmpty(){
       MyStream<Integer> res = AnyM.<Integer>ofValue(MyStream.of(1))
               .filter(i->i>10).unwrap();
       assertThat(res.get(),nullValue());
    }
    @Test
    public void flatMap(){
       MyStream<Integer> res = AnyM.<Integer>ofValue(MyStream.of(1))
               .flatMap(i-> AnyM.ofValue(MyStream.of(2))).unwrap();
       assertThat(res.get(),equalTo(2));
    }
    @Test
    public void flatMapCrossType(){
       Maybe<Integer> res = AnyM.<Integer>ofValue(Maybe.of(1))
               .flatMap(i-> AnyM.ofValue(MyStream.of(2))).unwrap();
       assertThat(res.get(),equalTo(2));
    }
    @Test
    public void flatMapCrossTypeEmpty(){
       Maybe<Integer> res = AnyM.<Integer>ofValue(Maybe.of(1))
               .flatMap(i-> AnyM.ofValue(MyStream.of(null))).unwrap();
       assertThat(res.isPresent(),equalTo(false));
    }
    @Test
    public void flatMapCrossTypeEmptyException(){
       Maybe<Integer> res = AnyM.<Integer>ofValue(Maybe.of(1))
               .flatMap(i-> AnyM.ofValue(MyStream2.of(null))).unwrap();
       assertThat(res.isPresent(),equalTo(false));
    }
    @Value
    static class MyStream2<T>{
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
            throw new RuntimeException();
        }
    }
}

