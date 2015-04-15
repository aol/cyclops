package com.aol.cyclops.matcher;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ChainOfResponsibility<T,R> extends Predicate<T>, Function<T,R> {

}
