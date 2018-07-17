package com.oath.cyclops.types.foldable;

import com.oath.cyclops.hkt.Higher;
import cyclops.function.Ord;


public interface OrderedBy<W,T1,T2 extends  OrderedBy<W,T1,?>>  extends Higher<W,T1> {
    default Ord.Ordering order(Ord<W,T1> ord, T2 other){
        return ord.compare(this,other);
    }
}
