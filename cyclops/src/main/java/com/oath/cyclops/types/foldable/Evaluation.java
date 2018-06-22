package com.oath.cyclops.types.foldable;


import java.util.function.Consumer;

public enum Evaluation {
    EAGER, LAZY;

    public void fold(Runnable eager, Runnable lazy){
        if(this==EAGER){
            eager.run();
        }
        lazy.run();
    }
}
