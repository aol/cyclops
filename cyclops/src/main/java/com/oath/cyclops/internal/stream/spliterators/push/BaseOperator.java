package com.oath.cyclops.internal.stream.spliterators.push;

import lombok.AllArgsConstructor;

/**
 * Created by johnmcclean on 12/01/2017.
 */
@AllArgsConstructor
public  abstract class BaseOperator<T,R> implements Operator<R> {
    final Operator<T> source;

 }
