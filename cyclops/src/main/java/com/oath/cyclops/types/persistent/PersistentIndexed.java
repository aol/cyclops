package com.oath.cyclops.types.persistent;

import com.oath.cyclops.types.persistent.views.ListView;
import cyclops.control.Option;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface PersistentIndexed<T> extends PersistentCollection<T> {

    Option<T> get(int index);
    T getOrElse(int index, T alt);
    T getOrElseGet(int index, Supplier<? extends T> alt);



}
