package cyclops.typeclasses;

import com.aol.cyclops2.hkt.Higher;

@FunctionalInterface
public interface NaturalTransformation<W1,W2>{

    <T> Higher<W2, T> apply(Higher<W1, T> a) ;
}