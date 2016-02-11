package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LazySwapPTuple2<T2, T1> extends TupleImpl<T2,T1,Object,Object,Object,Object,Object,Object>{


    private final PTuple2<T1, T2> host;

    public LazySwapPTuple2(PTuple2<T1, T2> host) {
        super(Arrays.asList(), 2);
        this.host = host;
    }

    public T2 v1(){
        return host.v2();
    }

    public T1 v2(){
        return host.v1();
    }


    @Override
    public List<Object> getCachedValues() {
        return Arrays.asList(v1(), v2());
    }

    @Override
    public Iterator iterator() {
        return getCachedValues().iterator();
    }


}
