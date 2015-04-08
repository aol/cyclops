package com.aol.cyclops.javaslang;

import javaslang.collection.List;

import java.util.stream.Collector;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class Collectors {



        public static <T> Collector<T, java.util.List<T>, List<T>> toList() {

        return  Collector.of(
                java.util.ArrayList::new,
                java.util.List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                javaslang.collection.List::of
        );

    }


}
