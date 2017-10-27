package cyclops.typeclasses.functions;


import com.oath.cyclops.hkt.Higher;
import cyclops.function.Monoid;

public interface MonoidK<W,T> extends SemigroupK<W,T>, Monoid<Higher<W,T>> {

     @Override
     Higher<W,T> zero();

     public static <W,T> MonoidK<W,T> of(Higher<W,T> zero, SemigroupK<W,T> sg){
         return new MonoidK<W, T>() {
             @Override
             public Higher<W, T> zero() {
                 return zero;
             }

             @Override
             public Higher<W, T> apply(Higher<W, T> t1, Higher<W, T> t2) {
                 return sg.apply(t1,t2);
             }
         };
     }

}
