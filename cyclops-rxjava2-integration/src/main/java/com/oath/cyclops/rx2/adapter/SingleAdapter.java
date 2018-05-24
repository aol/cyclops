package com.oath.cyclops.rx2.adapter;

import com.oath.cyclops.anym.extensability.AbstractMonadAdapter;


import cyclops.companion.rx2.Functions;
import cyclops.companion.rx2.Singles;
import cyclops.control.Future;
import cyclops.monads.AnyM;
import cyclops.monads.Rx2Witness.single;
import cyclops.monads.SingleAnyM;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.Single;
import lombok.AllArgsConstructor;


import java.util.function.Function;
import java.util.function.Predicate;




@AllArgsConstructor
public class SingleAdapter extends AbstractMonadAdapter<single> {

    @Override
    public <T> Iterable<T> toIterable(AnyM<single, T> t) {
        return Future.fromPublisher(future(t).toFlowable());
    }

    @Override
    public <T, R> AnyM<single, R> ap(AnyM<single,? extends Function<? super T,? extends R>> fn, AnyM<single, T> apply) {
        Single<T> f = future(apply);

        Single<? extends Function<? super T, ? extends R>> fnF = future(fn);

        Future<T> crF1 = Future.fromPublisher(f.toFlowable());
        Future<? extends Function<? super T, ? extends R>> crFnF = Future.fromPublisher(fnF.toFlowable());

        Single<R> res = Single.fromPublisher(crF1.zip(crFnF,(a,b)->b.apply(a)));
        return SingleAnyM.anyM(res);

    }

    @Override
    public <T> AnyM<single, T> filter(AnyM<single, T> t, Predicate<? super T> fn) {

        return SingleAnyM.anyM(future(t).filter(Functions.rxPredicate(fn)).toSingle());
    }

    <T> Single<T> future(AnyM<single,T> anyM){
        return anyM.unwrap();
    }
    <T> Future<T> futureW(AnyM<single,T> anyM){
        return Future.fromPublisher(anyM.unwrap());
    }

    @Override
    public <T> AnyM<single, T> empty() {
        return SingleAnyM.anyM(Single.never());
    }



    @Override
    public <T, R> AnyM<single, R> flatMap(AnyM<single, T> t,
                                     Function<? super T, ? extends AnyM<single, ? extends R>> fn) {
        return SingleAnyM.anyM(Single.fromPublisher(futureW(t).flatMap(fn.andThen(a-> futureW(a)))));

    }

    @Override
    public <T> AnyM<single, T> unitIterable(Iterable<T> it)  {
        return SingleAnyM.anyM(Single.fromPublisher(Future.fromIterable(it)));
    }

    @Override
    public <T> AnyM<single, T> unit(T o) {
        return SingleAnyM.anyM(Single.just(o));
    }

    @Override
    public <T> ReactiveSeq<T> toStream(AnyM<single, T> t) {
        return ReactiveSeq.fromPublisher(future(t).toFlowable());
    }

    @Override
    public <T, R> AnyM<single, R> map(AnyM<single, T> t, Function<? super T, ? extends R> fn) {
        return SingleAnyM.anyM(future(t).map(x->fn.apply(x)));
    }
}
