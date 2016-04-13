package com.aol.cyclops.control.anym;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MonoComprehender implements ValueComprehender<Mono> {
    public Class getTargetClass(){
        return Mono.class;
    }
    @Override
    public Object filter(Mono o,Predicate p) {
        return FutureW.of(o.toCompletableFuture()).filter(p);
    }

    @Override
    public Object map(Mono o,Function fn) {
        return o.map(fn);
    }
    
    @Override
    public Flux flatMap(Mono o,Function fn) {
        return o.flatMap(fn);
    }

    @Override
    public boolean instanceOfT(Object apply) {
        return apply instanceof Mono;
    }

    @Override
    public Mono of(Object o) {
        return Mono.just(o);
    }

    @Override
    public Mono empty() {
        return Mono.empty();
    }
    @Override
    public Object resolveForCrossTypeFlatMap(Comprehender comp, Mono apply) {
        Xor<Throwable,?> res = FutureW.of(apply.toCompletableFuture()).toXor();
        return res.isPrimary() ? comp.of(res.get()) :  comp.empty();
    }
    
}
