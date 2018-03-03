package com.oath.cyclops.reactor.adapter;

import com.oath.anym.AnyMValue;
import com.oath.anym.extensability.ValueAdapter;
import cyclops.companion.reactor.Monos;
import cyclops.control.Future;
import cyclops.control.Option;
import cyclops.monads.ReactorWitness.mono;
import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.monads.ReactorWitness.mono;


@AllArgsConstructor
public class MonoAdapter implements ValueAdapter<mono> {



    @Override
    public <T> Iterable<T> toIterable(AnyM<mono, T> t) {
        return Future.fromPublisher(future(t));
    }

    @Override
    public <T, R> AnyM<mono, R> ap(AnyM<mono,? extends Function<? super T,? extends R>> fn, AnyM<mono, T> apply) {
        Mono<T> f = future(apply);
        Mono<? extends Function<? super T, ? extends R>> fnF = future(fn);
        Mono<R> res = Mono.fromFuture(fnF.toFuture().thenCombine(f.toFuture(), (a, b) -> a.apply(b)));
        return Monos.anyM(res);

    }

    @Override
    public <T> AnyM<mono, T> filter(AnyM<mono, T> t, Predicate<? super T> fn) {
        return Monos.anyM(future(t).filter(fn));
    }

    <T> Mono<T> future(AnyM<mono,T> anyM){
        return anyM.unwrap();
    }
    <T> Future<T> futureW(AnyM<mono,T> anyM){
        return Future.fromPublisher(anyM.unwrap());
    }

    @Override
    public <T> AnyM<mono, T> empty() {
        return Monos.anyM(Mono.empty());
    }



    @Override
    public <T, R> AnyM<mono, R> flatMap(AnyM<mono, T> t,
                                     Function<? super T, ? extends AnyM<mono, ? extends R>> fn) {
        return Monos.anyM(Mono.from(futureW(t).flatMap(fn.andThen(a-> futureW(a)))));

    }

    @Override
    public <T> AnyM<mono, T> unitIterable(Iterable<T> it)  {
        return Monos.anyM(Mono.from(Future.fromIterable(it)));
    }

    @Override
    public <T> AnyM<mono, T> unit(T o) {
        return Monos.anyM(Mono.just(o));
    }

    @Override
    public <T> ReactiveSeq<T> toStream(AnyM<mono, T> t) {
        return ReactiveSeq.fromPublisher(mono(t));
    }

    @Override
    public <T, R> AnyM<mono, R> map(AnyM<mono, T> t, Function<? super T, ? extends R> fn) {
        return Monos.anyM(future(t).map(fn));
    }

    @Override
    public <T> Option<T> get(AnyMValue<mono, T> t) {
      try {
        return Option.some(future(t).block());
      }catch (Exception e){
        return Option.none();
      }

    }
}
