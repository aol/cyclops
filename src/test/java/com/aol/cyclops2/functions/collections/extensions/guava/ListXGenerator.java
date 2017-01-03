package com.aol.cyclops2.functions.collections.extensions.guava;

import java.util.List;

import cyclops.collections.ListX;
import com.google.common.collect.testing.TestStringListGenerator;

public class ListXGenerator extends  TestStringListGenerator {

    
    @Override
    public List<String> create(String... elements) {
       return ListX.of(elements);
    }

}
