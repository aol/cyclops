package com.aol.cyclops2.types;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public abstract class AbstractValueTest {
    public abstract <T> Value<T> of(T element);
    
    @Test
    public void collect(){
        List<Integer> lst = of(1).collect(Collectors.toList());
        assertEquals(new Integer(1), lst.get(0));
    }
}
