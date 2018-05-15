package com.oath.cyclops.rx2.adapter;


import com.oath.cyclops.anym.extensability.AbstractMonadAdapter;
import cyclops.companion.rx2.Observables;
import cyclops.monads.Rx2Witness.observable;

import cyclops.monads.AnyM;
import lombok.AllArgsConstructor;
import io.reactivex.Observable;


import java.util.function.Function;
import java.util.function.Predicate;


@AllArgsConstructor
public class ObservableAdapter extends AbstractMonadAdapter<observable> {





    @Override
    public <T> Iterable<T> toIterable(AnyM<observable, T> t) {
        return ()-> observable(t).blockingIterable().iterator();
    }

    @Override
    public <T, R> AnyM<observable, R> ap(AnyM<observable,? extends Function<? super T,? extends R>> fn, AnyM<observable, T> apply) {
        Observable<T> f = observable(apply);
        Observable<? extends Function<? super T, ? extends R>> fnF = observable(fn);
        Observable<R> res = fnF.zipWith(f, (a, b) -> a.apply(b));
        return Observables.anyM(res);

    }

    @Override
    public <T> AnyM<observable, T> filter(AnyM<observable, T> t, Predicate<? super T> fn) {
        return Observables.anyM(observable(t).filter(e->fn.test(e)));
    }

    <T> Observable<T> observable(AnyM<observable,T> anyM){
        ObservableReactiveSeqImpl<T> seq = anyM.unwrap();
        return seq.observable;
    }

    @Override
    public <T> AnyM<observable, T> empty() {
        return Observables.anyM(Observable.empty());
    }



    @Override
    public <T, R> AnyM<observable, R> flatMap(AnyM<observable, T> t,
                                              Function<? super T, ? extends AnyM<observable, ? extends R>> fn) {
        return Observables.anyM(observable(t).flatMap(x->observable(fn.apply(x))));

    }

    @Override
    public <T> AnyM<observable, T> unitIterable(Iterable<T> it)  {
        return Observables.anyM(Observable.fromIterable(it));
    }

    @Override
    public <T> AnyM<observable, T> unit(T o) {
        return Observables.anyM(Observable.just(o));
    }

    @Override
    public <T, R> AnyM<observable, R> map(AnyM<observable, T> t, Function<? super T, ? extends R> fn) {
        return Observables.anyM(observable(t).map(i->fn.apply(i)));
    }

}
