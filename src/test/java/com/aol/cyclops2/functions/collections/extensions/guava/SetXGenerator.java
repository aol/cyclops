package com.aol.cyclops2.functions.collections.extensions.guava;

import java.util.Set;

import cyclops.collections.mutable.SetX;
import com.google.common.collect.testing.TestStringSetGenerator;

public class SetXGenerator extends  TestStringSetGenerator {

    
    @Override
    public Set<String> create(String... elements) {
       return SetX.of(elements);
    }

}
