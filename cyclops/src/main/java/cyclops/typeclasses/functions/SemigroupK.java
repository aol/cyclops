package cyclops.typeclasses.functions;


import com.oath.cyclops.hkt.Higher;
import cyclops.function.Semigroup;

@FunctionalInterface
public interface SemigroupK<W>  {

   <T> Higher<W,T> apply(Higher<W,T> t1, Higher<W,T> t2);

  default <T> Semigroup<Higher<W,T>> asSemigroup(){
    return (a,b)->this.apply(a,b);
  }

}

