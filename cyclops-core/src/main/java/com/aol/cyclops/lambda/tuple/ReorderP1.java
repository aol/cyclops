package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ReorderP1<T1,NT1> extends TupleImpl {
    private final Function<PTuple1<T1>, NT1> v1S;
    private final PTuple1<T1> host;

    public ReorderP1(Function<PTuple1<T1>, NT1> v1S, PTuple1<T1> host) {
        super(Arrays.asList(), 1);
        this.v1S = v1S;
        this.host = host;
    }

    public NT1 v1(){
        return v1S.apply(host);
    }


    @Override
    public List<Object> getCachedValues() {
        return Arrays.asList(v1());
    }

    @Override
    public Iterator iterator() {
        return getCachedValues().iterator();
    }


}