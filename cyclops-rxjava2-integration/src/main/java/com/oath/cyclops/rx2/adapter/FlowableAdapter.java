package com.oath.cyclops.rx2.adapter;


import com.oath.anym.extensability.AbstractMonadAdapter;
import cyclops.companion.rx2.Flowables;
import cyclops.monads.AnyM;

import cyclops.monads.Rx2Witness;
import cyclops.monads.Rx2Witness.flowable;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.Flowable;
import lombok.AllArgsConstructor;


import java.util.function.Function;
import java.util.function.Predicate;


@AllArgsConstructor
public class FlowableAdapter extends AbstractMonadAdapter<flowable> {





    @Override
    public <T> Iterable<T> toIterable(AnyM<flowable, T> t) {
        return ()->stream(t).blockingIterable().iterator();
    }

    @Override
    public <T, R> AnyM<flowable, R> ap(AnyM<flowable,? extends Function<? super T,? extends R>> fn, AnyM<flowable, T> apply) {
        Flowable<T> f = stream(apply);
        Flowable<? extends Function<? super T, ? extends R>> fnF = stream(fn);
        Flowable<R> res = fnF.zipWith(f, (a, b) -> a.apply(b));
        return Flowables.anyM(res);

    }

    @Override
    public <T> AnyM<flowable, T> filter(AnyM<flowable, T> t, Predicate<? super T> fn) {
        return Flowables.anyM(stream(t).filter(i->fn.test(i)));
    }

    <T> Flowable<T> stream(AnyM<flowable,T> anyM){
        return anyM.unwrap();
    }

    @Override
    public <T> AnyM<flowable, T> empty() {
        return Flowables.anyM(Flowable.empty());
    }



    @Override
    public <T, R> AnyM<flowable, R> flatMap(AnyM<flowable, T> t,
                                     Function<? super T, ? extends AnyM<flowable, ? extends R>> fn) {
        return Flowables.anyM(stream(t).flatMap(i->fn.andThen(a->stream(a)).apply(i)));

    }

    @Override
    public <T> AnyM<flowable, T> unitIterable(Iterable<T> it)  {
        return Flowables.anyM(Flowable.fromIterable(it));
    }

    @Override
    public <T> AnyM<flowable, T> unit(T o) {
        return Flowables.anyM(Flowable.just(o));
    }

    @Override
    public <T> ReactiveSeq<T> toStream(AnyM<flowable, T> t) {
        return ReactiveSeq.fromPublisher(Rx2Witness.flowable(t));
    }

    @Override
    public <T, R> AnyM<flowable, R> map(AnyM<flowable, T> t, Function<? super T, ? extends R> fn) {
        return Flowables.anyM(stream(t).map(i->fn.apply(i)));
    }
}
