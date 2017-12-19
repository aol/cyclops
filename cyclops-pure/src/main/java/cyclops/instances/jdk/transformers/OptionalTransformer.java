package cyclops.instances.jdk.transformers;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.optional;
import com.oath.cyclops.hkt.Higher;
import cyclops.hkt.Nested;
import cyclops.kinds.OptionalKind;
import cyclops.transformers.Transformer;
import cyclops.transformers.TransformerFactory;
import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OptionalTransformer<W1,T> implements Transformer<W1,optional,T> {
  private final Nested<W1,optional,T> nested;
  private final Monad<W1> monad1;

  private final  static <W1> TransformerFactory<W1,optional> factory(){
    return OptionalTransformer::optionalT;
  }
  public static <W1,T> OptionalTransformer<W1,T> optionalT(Nested<W1,optional,T> nested){
    return new OptionalTransformer<W1,T>(nested,nested.def1.monad());
  }
  @Override
  public <R> Nested<W1, optional, R> flatMap(Function<? super T, ? extends Nested<W1, optional, R>> fn) {
    Higher<W1, Higher<optional, R>> r = monad1.flatMap(m -> OptionalKind.narrow(m)
        .map(t -> fn.apply(t).nested)
        .orElseGet(() -> monad1.unit(OptionalKind.empty())),
      nested.nested);



    return Nested.of(r, nested.def1, nested.def2);



  }

  @Override
  public <R> Nested<W1, optional, R> flatMapK(Function<? super T, ? extends Higher<W1, Higher<optional, R>>> fn) {
    return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
  }


}
