package cyclops.instances.control.transformers;


import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.tryType;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Try;
import cyclops.hkt.Nested;
import cyclops.transformers.Transformer;
import cyclops.transformers.TransformerFactory;
import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TryTransformer<W1,X extends Throwable,T> implements Transformer<W1,Higher<tryType,X>,T> {
  private final Nested<W1,Higher<tryType,X>,T> nested;
  private final Monad<W1> monad1;

  private final  static <W1,X extends Throwable,T> TransformerFactory<W1,Higher<tryType,X>> factory(){
    return TryTransformer::tryT;
  }
  public static <W1,X extends Throwable,T> TryTransformer<W1,X,T> tryT(Nested<W1,Higher<tryType,X>,T> nested){
    return new TryTransformer<>(nested,nested.def1.monad());
  }


  @Override
  public <R1> Nested<W1, Higher<tryType, X>, R1> flatMap(Function<? super T, ? extends Nested<W1, Higher<tryType, X>, R1>> fn) {
    Higher<W1, Higher<Higher<tryType, X>, R1>> res = monad1.flatMap(m -> Try.narrowK(m).visit(r -> fn.apply(r).nested, l -> monad1.unit(Try.failure(l))),
      nested.nested);

    return Nested.of(res, nested.def1, nested.def2);
  }

  @Override
  public <R> Nested<W1, Higher<tryType, X>, R> flatMapK(Function<? super T, ? extends Higher<W1, Higher<Higher<tryType, X>, R>>> fn) {
    return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
  }


}
