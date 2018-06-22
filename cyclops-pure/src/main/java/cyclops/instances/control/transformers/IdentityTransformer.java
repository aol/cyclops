package cyclops.instances.control.transformers;

import com.oath.cyclops.hkt.DataWitness.identity;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Identity;
import cyclops.hkt.Nested;
import cyclops.transformers.Transformer;
import cyclops.transformers.TransformerFactory;
import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentityTransformer<W1,T> implements Transformer<W1,identity,T> {
  private final Nested<W1,identity,T> nested;
  private final Monad<W1> monad1;

  private final  static <W1> TransformerFactory<W1,identity> factory(){
    return IdentityTransformer::identityT;
  }
  public static <W1,T> IdentityTransformer<W1,T> identityT(Nested<W1,identity,T> nested){
    return new IdentityTransformer<>(nested,nested.def1.monad());
  }
  @Override
  public <R> Nested<W1, identity, R> flatMap(Function<? super T, ? extends Nested<W1, identity, R>> fn) {
    Higher<W1, Higher<identity, R>> r = monad1.flatMap(m -> Identity.narrowK(m).fold(t -> fn.apply(t).nested),
      nested.nested);

    return Nested.of(r, nested.def1, nested.def2);



  }
  @Override
  public <R> Nested<W1, identity, R> flatMapK(Function<? super T, ? extends Higher<W1, Higher<identity, R>>> fn) {
    return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
  }

}
