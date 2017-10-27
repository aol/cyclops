package com.oath.cyclops.types.futurestream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Val<T> {
    enum Pos {
        left,
        right
    };

    Pos pos;
    T val;
}
