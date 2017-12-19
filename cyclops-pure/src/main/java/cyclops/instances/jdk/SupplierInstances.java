package cyclops.instances.jdk;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.supplier;
import com.oath.cyclops.hkt.Higher;
import cyclops.free.Free;
import cyclops.function.Function0;
import cyclops.kinds.SupplierKind;
import cyclops.typeclasses.functor.Functor;

import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierInstances {


  public static final Functor<supplier> functor =
    new Functor<supplier>() {
      @Override
      public <T, R> SupplierKind<R> map(Function<? super T, ? extends R> f, Higher<supplier, T> fa) {
        return ((SupplierKind<T>) fa).mapFn(f);
      }
    };
}
