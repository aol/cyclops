package com.aol.cyclops.functions.collections.extensions.guava;

import java.util.List;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.google.common.collect.testing.TestStringListGenerator;

public class ListXGenerator extends  TestStringListGenerator {

    
    @Override
    public List<String> create(String... elements) {
       return ListX.of(elements);
    }

}
