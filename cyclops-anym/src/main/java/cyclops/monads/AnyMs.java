package cyclops.monads;

import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Functions;
import cyclops.control.Either;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.monads.transformers.EitherT;
import cyclops.monads.transformers.EvalT;
import cyclops.monads.transformers.ListT;
import cyclops.monads.transformers.MaybeT;

public interface AnyMs {
  /**
   * KleisliM arrow : A function that takes an input value t and embeds it inside a monadic context.
   * arrowM makes use of Witness Types to simulate higher-kinded types, and wraps the new monadic type
   * inside an AnyM. AnyM makes use of sub-type polymorphism (Object Orientd inheritance) to define monadic
   * functions (transform / flatMap etc) on the returned Object (for parametric polymorphism use {@link Functions#arrow}
   *
   * @param w WitnessType Object: defines the returned monad type (e.g. see {@link Witness.stream} for HKT encoding for Streams)
   * @param <T> Value type to be embedded inside a monad
   * @param <W> The type of the WitnessType (Witness.stream, Witness.Future, Witness.list and so on)
   * @return A function that can embed a value inisde a Monad
   */
  public static   <T,W extends WitnessType<W>> Function1<? super T,? extends AnyM<W,T>> arrowM(W w){
    return t-> w.adapter().unit(t);
  }
  public static  <W extends WitnessType<W>,T> ListT<W, T> liftM(VectorX<T> v, W witness) {
    return ListT.of(witness.adapter().unit(v));
  }
  public static  <W extends WitnessType<W>,T> ListT<W, T> liftM(ListX<T> l, W witness) {
    return ListT.of(witness.adapter().unit(l));
  }
  public static  <W extends WitnessType<W>,ST,PT> EitherT<W, ST,PT> liftM(Either<ST,PT> e, W witness) {
    return EitherT.of(witness.adapter().unit(e));
  }
  public static  <W extends WitnessType<W>,T> EvalT<W, T> liftM(Eval<T> e, W witness) {
    return EvalT.of(witness.adapter().unit(e));
  }
  public static  <W extends WitnessType<W>,T> MaybeT<W, T> liftM(Maybe<T> m, W witness) {
    return MaybeT.of(witness.adapter().unit(m));
  }

}
