package com.aol.cyclops2.types.foldable;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.tuple.Tuple1;
import cyclops.monads.Witness;
import cyclops.typeclasses.Ord;


public interface OrderedBy<W,T1,T2 extends  OrderedBy<W,T1,?>>  extends Higher<W,T1> {
    default Ord.Ordering order(Ord<W,T1> ord, T2 other){
        return ord.compare(this,other);
    }
}
