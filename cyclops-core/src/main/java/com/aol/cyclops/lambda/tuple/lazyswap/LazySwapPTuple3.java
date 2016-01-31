package com.aol.cyclops.lambda.tuple.lazyswap;

import com.aol.cyclops.lambda.tuple.PTuple3;
import com.aol.cyclops.lambda.tuple.TupleImpl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LazySwapPTuple3<T3, T2, T1> extends TupleImpl<T3,T2,T1,Object,Object,Object,Object,Object> {


    private final PTuple3<T1, T2, T3> host;

    public LazySwapPTuple3(PTuple3<T1, T2, T3> host) {
        super(Arrays.asList(), 3);
        this.host = host;
    }

    public T3 v1(){
        return host.v3();
    }

    public T2 v2(){
        return host.v2();
    }

    public T1 v3(){
    	
        return host.v1();
    }


    @Override
    public List<Object> getCachedValues() {
        return Arrays.asList(v1(), v2(), v3());
    }
    @Override
    public Iterator iterator() {
        return getCachedValues().iterator();
    }


}