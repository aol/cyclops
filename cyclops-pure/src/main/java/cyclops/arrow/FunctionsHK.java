package cyclops.arrow;

import com.oath.cyclops.hkt.Higher;
import cyclops.function.Function1;
import cyclops.typeclasses.monad.Monad;

public interface FunctionsHK {
  public static  <T,CRE> Function1<? super T,? extends Higher<CRE,T>> arrow(Monad<CRE> monad){
    return t-> monad.unit(t);
  }
}
