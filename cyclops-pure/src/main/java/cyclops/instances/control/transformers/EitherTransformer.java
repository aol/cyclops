package cyclops.instances.control.transformers;

import com.oath.cyclops.hkt.DataWitness.either;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Either;
import cyclops.hkt.Nested;
import cyclops.transformers.Transformer;
import cyclops.transformers.TransformerFactory;
import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EitherTransformer<W1,L,R> implements Transformer<W1,Higher<either,L>,R> {
  private final Nested<W1,Higher<either,L>,R> nested;
  private final Monad<W1> monad1;

  private final  static <W1,L> TransformerFactory<W1,Higher<either,L>> factory(){
    return EitherTransformer::eitherT;
  }
  public static <W1,L,R> EitherTransformer<W1,L,R> eitherT(Nested<W1,Higher<either,L>,R> nested){
    return new EitherTransformer<W1,L,R>(nested,nested.def1.monad());
  }


  @Override
  public <R1> Nested<W1, Higher<either, L>, R1> flatMap(Function<? super R, ? extends Nested<W1, Higher<either, L>, R1>> fn) {
    Higher<W1, Higher<Higher<either, L>, R1>> res = monad1.flatMap(m -> Either.narrowK(m).fold(l -> monad1.unit(Either.left(l)),

      r -> fn.apply(r).nested),
      nested.nested);

    return Nested.of(res, nested.def1, nested.def2);
  }

  @Override
  public <R1> Nested<W1, Higher<either, L>, R1> flatMapK(Function<? super R, ? extends Higher<W1, Higher<Higher<either, L>, R1>>> fn) {
    return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
  }
}
