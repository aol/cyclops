package com.aol.cyclops.javaslang;

import java.util.ArrayList;
import java.util.stream.Collector;

import javaslang.collection.List;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class Collectors {



        public static <T> Collector<T, ArrayList<T>, List<T>>  toList() {

        	return  javaslang.collection.List.collector();

        }


}
