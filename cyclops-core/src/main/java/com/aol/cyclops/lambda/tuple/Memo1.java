package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Memo1<T1> extends TupleImpl {


    private final Map<Integer,Object> values = new ConcurrentHashMap<>();
    private final PTuple1<T1> host;

    public Memo1(PTuple1<T1> host) {
        super(Arrays.asList(), 1);
     
        this.host = host;
    }

    public T1 v1(){
        return ( T1) values.computeIfAbsent(new Integer(0), key -> host.v1());
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