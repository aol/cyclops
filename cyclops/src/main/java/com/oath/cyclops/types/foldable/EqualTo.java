package com.oath.cyclops.types.foldable;


import com.oath.cyclops.hkt.Higher;
import cyclops.typeclasses.Eq;

public interface EqualTo<W, T1,T extends EqualTo<W,T1,?>> extends Higher<W,T1>{

    default boolean equalTo(T other){
        return  this.equalTo(new Eq<W>() {},other);
    }
    default boolean equalTo(Eq<W> eq, T other){
        return eq.equals(this,other);
    }
}
