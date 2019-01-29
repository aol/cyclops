package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.either;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.companion.Monoids;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.SemigroupKs;
import cyclops.typeclasses.functor.BiFunctor;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static cyclops.control.Either.narrowK;

public class EitherInstances {

  public static  <W1,LT,RT> Product<Higher<either, LT>,W1, RT> product(Either<LT,RT> e, Active<W1, RT> active){
    return Product.of(allTypeclasses(e),active);
  }
  public static  <W1,LT,RT> Coproduct<W1,Higher<either, LT>, RT> coproduct(Either<LT,RT> e, InstanceDefinitions<W1> def2){
    return Coproduct.right(e,def2, EitherInstances.definitions());
  }
  public static  <LT,RT> Active<Higher<either, LT>, RT> allTypeclasses(Either<LT,RT> e){
    return Active.of(e, EitherInstances.definitions());
  }
  public static  <W2,R,LT,RT> Nested<Higher<either, LT>,W2,R> mapM(Either<LT,RT> e, Function<? super RT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(e.map(fn), EitherInstances.definitions(), defs);
  }

  public static  <L,T> Kleisli<Higher<either,L>,Either<L,T>,T> kindKleisli(){
    return Kleisli.of(getInstance(), Either::widen);
  }
  public static  <L,T> Cokleisli<Higher<either,L>,T,Either<L,T>> kindCokleisli(){
    return Cokleisli.of(Either::narrowK);
  }
  public static <W1,ST,PT> Nested<Higher<either,ST>,W1,PT> nested(Either<ST,Higher<W1,PT>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, EitherInstances.definitions(),def2);
  }

  public static <L> InstanceDefinitions<Higher<either, L>> definitions(){
    return new InstanceDefinitions<Higher<either, L>>() {
      @Override
      public <T, R> Functor<Higher<either, L>> functor() {
        return EitherInstances.INSTANCE;
      }

      @Override
      public <T> Pure<Higher<either, L>> unit() {
        return EitherInstances.INSTANCE;
      }

      @Override
      public <T, R> Applicative<Higher<either, L>> applicative() {
        return EitherInstances.INSTANCE;
      }

      @Override
      public <T, R> Monad<Higher<either, L>> monad() {
        return EitherInstances.INSTANCE;
      }

      @Override
      public <T, R> Option<MonadZero<Higher<either, L>>> monadZero() {
        return Option.none();
      }

      @Override
      public <T> Option<MonadPlus<Higher<either, L>>> monadPlus() {
        return Option.none();
      }

      @Override
      public <T> MonadRec<Higher<either, L>> monadRec() {
        return EitherInstances.INSTANCE;
      }


      @Override
      public <T> Option<MonadPlus<Higher<either, L>>> monadPlus(MonoidK<Higher<either, L>> m) {
        return Option.none();
      }

      @Override
      public <C2, T> Traverse<Higher<either, L>> traverse() {
        return EitherInstances.INSTANCE;
      }

      @Override
      public <T> Foldable<Higher<either, L>> foldable() {
        return EitherInstances.INSTANCE;
      }

      @Override
      public <T> Option<Comonad<Higher<either, L>>> comonad() {
        return Option.none();
      }

      @Override
      public <T> Option<Unfoldable<Higher<either, L>>> unfoldable() {
        return Option.none();
      }
    };
  }
    private final static EitherTypeclasses INSTANCE = new EitherTypeclasses<>();

    public static final <L> EitherTypeclasses<L> getInstance(){
        return INSTANCE;
    }

    @AllArgsConstructor
    public static class EitherTypeclasses<L>  implements Monad<Higher<either, L>>,
                                                        MonadRec<Higher<either, L>>,
                                                        TraverseByTraverse<Higher<either, L>>,
                                                        Foldable<Higher<either, L>>,
                                                        ApplicativeError<Higher<either, L>,L>,
                                                        BiFunctor<either> {


        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<Higher<either, L>, T> ds) {
            Either<L, T> xor = narrowK(ds);
            return xor.fold(monoid);
        }

        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<either, L>, T> ds) {
            Either<L, T> xor = narrowK(ds);
            return xor.fold(monoid);
        }
        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> nestedA) {
            return foldLeft(mb, narrowK(nestedA).<R>map(fn));
        }

        @Override
        public <T, R, T2, R2> Higher2<either, R, R2> bimap(Function<? super T, ? extends R> fn, Function<? super T2, ? extends R2> fn2, Higher2<either, T, T2> ds) {
            return narrowK(ds).bimap(fn, fn2);
        }

        @Override
        public <T> Higher<Higher<either, L>, T> raiseError(L l) {
            return Either.left(l);
        }

        @Override
        public <T> Higher<Higher<either, L>, T> handleErrorWith(Function<? super L, ? extends Higher<Higher<either, L>, ? extends T>> fn, Higher<Higher<either, L>, T> ds) {
            Function<? super L, ? extends Either<L, T>> fn2 = fn.andThen(s -> {

                Higher<Higher<either, L>, T> x = (Higher<Higher<either, L>, T>) s;
                Either<L, T> r = narrowK(x);
                return r;
            });
            return narrowK(ds).flatMapLeftToRight(fn2);
        }


        @Override
        public <T, R> Higher<Higher<either, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<either, L>, R>> fn, Higher<Higher<either, L>, T> ds) {
            Either<L,T> xor = narrowK(ds);
            return xor.flatMap(fn.andThen(Either::narrowK));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<Higher<either, L>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<either, L>, T> ds) {
            Either<L,T> maybe = narrowK(ds);
            return maybe.fold(left->  applicative.unit(Either.<L,R>left(left)),
                right->applicative.map(m-> Either.right(m), fn.apply(right)));
        }

        @Override
        public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
            Either<L,T> xor = narrowK(apply);
            Either<L, ? extends Function<T, R>> xorFn = narrowK(fn);
            return xorFn.zip(xor,(a,b)->a.apply(b));
        }

        @Override
        public <T> Higher<Higher<either, L>, T> unit(T value) {
            return Either.right(value);
        }

        @Override
        public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
            Either<L,T> xor = narrowK(ds);
            return xor.map(fn);
        }

        @Override
        public <T, R> Higher<Higher<either, L>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<either, L>, ? extends Either<T, R>>> fn) {
            return Either.tailRec(initial,fn.andThen(Either::narrowK));
        }
    }




}
