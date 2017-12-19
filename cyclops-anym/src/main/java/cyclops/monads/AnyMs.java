package cyclops.monads;

import com.oath.anym.AnyMSeq;
import com.oath.anym.AnyMValue;
import cyclops.control.Future;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.companion.Functions;
import cyclops.companion.Streams;
import cyclops.control.Either;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.monads.transformers.*;
import cyclops.reactive.ReactiveSeq;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public interface AnyMs {
  /**
   * KleisliM arrow : A function that takes an input value t and embeds it inside a monadic context.
   * arrowM makes use of Witness Types to simulate higher-kinded types, and wraps the new monadic type
   * inside an AnyM. AnyM makes use of sub-type polymorphism (Object Orientd inheritance) to define monadic
   * arrow (transform / flatMap etc) on the returned Object (for parametric polymorphism use {@link Functions#arrow}
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
  public static <W extends WitnessType<W>,T> Function<W,ListT<W, T>> liftM(VectorX<T> s) {
    return w->liftM(s,w);
  }
  public static  <W extends WitnessType<W>,T> ListT<W, T> liftM(ListX<T> l, W witness) {
    return ListT.of(witness.adapter().unit(l));
  }
  public static <W extends WitnessType<W>,T> Function<W,ListT<W, T>> liftM(ListX<T> s) {
    return w->liftM(s,w);
  }
  public static  <W extends WitnessType<W>,ST,PT> EitherT<W, ST,PT> liftM(Either<ST,PT> e, W witness) {
    return EitherT.of(witness.adapter().unit(e));
  }
  public static <W extends WitnessType<W>,ST,PT> Function<W,EitherT<W, ST,PT>> liftM(Either<ST,PT>  s) {
    return w->liftM(s,w);
  }
  public static  <W extends WitnessType<W>,T> EvalT<W, T> liftM(Eval<T> e, W witness) {
    return EvalT.of(witness.adapter().unit(e));
  }
  public static <W extends WitnessType<W>,T> Function<W,EvalT<W, T>> liftM(Eval<T> s) {
    return w->liftM(s,w);
  }
  public static  <W extends WitnessType<W>,T> MaybeT<W, T> liftM(Maybe<T> m, W witness) {
    return MaybeT.of(witness.adapter().unit(m));
  }
  public static <W extends WitnessType<W>,T> Function<W,MaybeT<W, T>> liftM(Maybe<T> s) {
    return w->liftM(s,w);
  }
  public static <W extends WitnessType<W>,T> FutureT<W, T> liftM(Future<T> f, W witness) {
    return FutureT.of(witness.adapter().unit(f));
  }
  public static <W extends WitnessType<W>,T> Function<W,FutureT<W, T>> liftM(Future<T> s) {
    return w->liftM(s,w);
  }
  public static <W extends WitnessType<W>,T> ListT<W, T> liftM(LinkedListX<T> l, W witness) {
    return ListT.of(witness.adapter().unit(l));
  }
  public static <W extends WitnessType<W>,T> Function<W,ListT<W, T>> liftM(LinkedListX<T> s) {
    return w->liftM(s,w);
  }
  public static <T,W extends WitnessType<W>> CompletableFutureT<W, T> liftM(CompletableFuture<T> opt, W witness) {
    return CompletableFutureT.of(witness.adapter().unit(opt));
  }
  public static <W extends WitnessType<W>,T> Function<W,CompletableFutureT<W, T>> liftM(CompletableFuture<T> s) {
    return w->liftM(s,w);
  }
  public static <T,W extends WitnessType<W>> OptionalT<W, T> liftM(Optional<T> opt, W witness) {
    return OptionalT.of(witness.adapter().unit(opt));
  }
  public static <T,W extends WitnessType<W>> Function<W,OptionalT<W, T>> liftM(Optional<T> opt) {
    return w->liftM(opt,w);
  }
  public static <T> StreamT<Witness.reactiveSeq,T> combinationsT(ReactiveSeq<T> s,final int size) {
    return StreamT.fromReactiveSeq(s.combinations(size));
  }

  public static <W extends WitnessType<W>,T> Function<W,StreamT<W, T>> liftM(ReactiveSeq<T> s) {
    return w->liftM(s,w);
  }
  public static <W extends WitnessType<W>,T> StreamT<W, T> liftM(ReactiveSeq<T> s, W witness) {
    return StreamT.of(witness.adapter().unit(s));
  }
  public static <T> StreamT<Witness.reactiveSeq,T> combinationsT(ReactiveSeq<T> s) {
    return StreamT.fromReactiveSeq(s.combinations());
  }

  public static  <T> StreamT<Witness.reactiveSeq,T> permutationsT(ReactiveSeq<T> s) {
    return StreamT.fromReactiveSeq(s.permutations());
  }
  /**
   *  Generic zip function. E.g. Zipping a Stream and an Optional
   *
   * <pre>
   * {@code
   * Stream<List<Integer>> zipped = Streams.zip(Stream.of(1,2,3)
  ,fromEither5(Optional.of(2)),
  (a,b) -> Arrays.asList(a,b));


  List<Integer> zip = zipped.collect(CyclopsCollectors.toList()).getValue(0);
  assertThat(zip.getValue(0),equalTo(1));
  assertThat(zip.getValue(1),equalTo(2));
   *
   * }
   * </pre>

   */
  public  static <T, S, R> Stream<R> zipAnyM(final Stream<T> stream, final AnyM<Witness.stream,? extends S> second,
                                                  final BiFunction<? super T, ? super S, ? extends R> zipper) {
    return Streams.zipSequence(stream, second.to(Witness::stream), zipper);
  }
  public static <W extends WitnessType<W>,T> Either<AnyMValue<W,T>, AnyMSeq<W,T>> anyM(final AnyM<W,T> anyM) {
    return anyM instanceof AnyMValue ? Either.left((AnyMValue<W,T>) anyM) : Either.right((AnyMSeq<W,T>) anyM);
  }





}
