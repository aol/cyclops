package cyclops.typeclasses.functions;


import com.oath.cyclops.hkt.Higher;
import cyclops.function.Semigroup;

@FunctionalInterface
public interface SemigroupK<W,T>  extends Semigroup<Higher<W,T>> {

    @Override
   Higher<W,T> apply(Higher<W,T> t1, Higher<W,T> t2);

}

