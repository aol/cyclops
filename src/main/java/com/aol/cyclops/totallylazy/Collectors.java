package com.aol.cyclops.totallylazy;

import com.google.common.collect.Iterables;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.totallylazy.collections.PersistentSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class Collectors {

    public <T> Collector<T, List<T>, PersistentList<T>> toList() {

        return  Collector.of(
                java.util.ArrayList::new,
                java.util.List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                PersistentList.constructors::list
        );


    }


}

