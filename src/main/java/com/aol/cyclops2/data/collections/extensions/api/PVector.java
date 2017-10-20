package com.aol.cyclops2.data.collections.extensions.api;

import java.util.Collection;

 public interface PVector<T> extends PIndexed<T> {



     PVector<T> plus(T e);

    
     PVector<T> plusAll(Iterable<? extends T> list);

    
     PVector<T> with(int i, T e);

    
     PVector<T> plus(int i, T e);

     default PVector<T> plusAll(int i, Iterable<? extends T> e){
         PVector<T> res = this;
         for(T  next : e){
             res = plus(i++,next);
         }
         return res;
     }

    


   
     PVector<T> removeValue(T e);

     PVector<T> minus(int i);

   
     PVector<T> subList(int start, int end);
}
