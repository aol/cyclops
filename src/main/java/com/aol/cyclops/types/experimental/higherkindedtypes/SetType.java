package com.aol.cyclops.types.experimental.higherkindedtypes;

import java.util.Set;

import com.aol.cyclops.data.collections.extensions.standard.SetX;



public interface SetType<T> extends  Higher2<SetType.setx,T>,Set<T>{
    
    static class setx{}
    
    default SetX<T> set(){
        return SetX.fromIterable(this);
    }
}

