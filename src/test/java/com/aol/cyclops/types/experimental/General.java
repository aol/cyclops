package com.aol.cyclops.types.experimental;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.experimental.TypeClasses._Functor;
import com.aol.cyclops.types.higherkindedtypes.type.constructors.ListType;

public class General {

    @Test
    public void functor(){
        _Functor<ListType.listx> f = TypeClasses.General
                .<ListType.listx,ListX<?>>functor(ListType::narrow,
                                                                (list,fn)->list.map(fn));
        System.out.println(f.map(a->a+1, ListX.of(1,2,3)));
    }
    
}
