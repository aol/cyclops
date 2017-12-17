package cyclops.companion;

import com.oath.cyclops.hkt.Higher;
import cyclops.function.Function1;

public interface FunctionsHK {
  public static final  <T,CRE> Function1<? super T,? extends Higher<CRE,T>> arrow(Monad<CRE> monad){
    return t-> monad.unit(t);
  }
}
