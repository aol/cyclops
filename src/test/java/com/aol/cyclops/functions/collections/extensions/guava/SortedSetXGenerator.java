package com.aol.cyclops.functions.collections.extensions.guava;

import java.util.Set;

import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.google.common.collect.testing.TestStringSetGenerator;

public class SortedSetXGenerator extends  TestStringSetGenerator {

    
    @Override
    public Set<String> create(String... elements) {
       return SortedSetX.of(elements);
    }

}
