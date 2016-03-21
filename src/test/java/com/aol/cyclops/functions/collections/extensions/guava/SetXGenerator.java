package com.aol.cyclops.functions.collections.extensions.guava;

import java.util.Set;

import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.google.common.collect.testing.TestStringSetGenerator;

public class SetXGenerator extends  TestStringSetGenerator {

    
    @Override
    public Set<String> create(String... elements) {
       return SetX.of(elements);
    }

}
