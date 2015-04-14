package com.aol.cyclops.totallylazy;

import java.util.ArrayList;
import java.util.stream.Collector;

import com.googlecode.totallylazy.collections.PersistentList;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class Collectors {

	/**
    public <T> Collector<T, ArrayList<T>, PersistentList<T>> toList() {

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
    **/


}

