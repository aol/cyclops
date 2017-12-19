package cyclops.arrow;

import com.oath.cyclops.hkt.Higher;
import cyclops.function.Function1;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Monad;

import java.util.function.Function;

public interface FunctionsHK {
  public static  <W1,W2,T,R> Function1<Higher<W1,T>,Higher<W2,R>> liftNT(Function<? super T, ? extends R> fn,
                                                                         Function<? super Higher<W1,T>,? extends Higher<W2,T>> hktTransform,
                                                                         Functor<W2> functor){
    return (T1)-> functor.map(fn,hktTransform.apply(T1));
  }

  public static  <T,CRE> Function1<? super T,? extends Higher<CRE,T>> arrow(Monad<CRE> monad){
    return t-> monad.unit(t);
  }
}
