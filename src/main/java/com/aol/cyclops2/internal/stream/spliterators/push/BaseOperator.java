package com.aol.cyclops2.internal.stream.spliterators.push;

import lombok.AllArgsConstructor;
import org.reactivestreams.Subscription;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
@AllArgsConstructor
public  abstract class BaseOperator<T,R> implements Operator<R> {
    final Operator<T> source;

 }
