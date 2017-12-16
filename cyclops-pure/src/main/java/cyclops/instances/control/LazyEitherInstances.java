package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.either;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import cyclops.companion.Monoids;
import cyclops.control.Either;
import cyclops.control.LazyEither;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Monoid;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.SemigroupKs;
import cyclops.typeclasses.functor.BiFunctor;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;

import java.util.function.Function;

import static cyclops.control.LazyEither.narrowK;

public class LazyEitherInstances {



  public static  <W1,LT,RT> Product<Higher<either, LT>,W1, RT> product(Either<LT,RT> e,Active<W1, RT> active){
    return Product.of(allTypeclasses(e),active);
  }
  public static  <W1,LT,RT> Coproduct<W1,Higher<either, LT>, RT> coproduct(Either<LT,RT> e,InstanceDefinitions<W1> def2){
    return Coproduct.right(e,def2, LazyEitherInstances.definitions());
  }
  public static  <LT,RT> Active<Higher<either, LT>, RT> allTypeclasses(Either<LT,RT> e){
    return Active.of(e, LazyEitherInstances.definitions());
  }
  public static  <W2,R,LT,RT> Nested<Higher<either, LT>,W2,R> mapM(Either<LT,RT> e,Function<? super RT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(e.map(fn), LazyEitherInstances.definitions(), defs);
  }

  public static  <L,T> Kleisli<Higher<either,L>,Either<L,T>,T> kindKleisli(){
    return Kleisli.of(LazyEitherInstances.monad(), Either::widen);
  }
  public static  <L,T> Cokleisli<Higher<either,L>,T,Either<L,T>> kindCokleisli(){
    return Cokleisli.of(Either::narrowK);
  }
  public static <W1,ST,PT> Nested<Higher<either,ST>,W1,PT> nested(Either<ST,Higher<W1,PT>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, LazyEitherInstances.definitions(),def2);
  }

  public static <L> InstanceDefinitions<Higher<either, L>> definitions(){
    return new InstanceDefinitions<Higher<either, L>>() {
      @Override
      public <T, R> Functor<Higher<either, L>> functor() {
        return LazyEitherInstances.functor();
      }

      @Override
      public <T> Pure<Higher<either, L>> unit() {
        return LazyEitherInstances.unit();
      }

      @Override
      public <T, R> Applicative<Higher<either, L>> applicative() {
        return LazyEitherInstances.applicative();
      }

      @Override
      public <T, R> Monad<Higher<either, L>> monad() {
        return LazyEitherInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<Higher<either, L>>> monadZero() {
        return Option.some(LazyEitherInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<Higher<either, L>>> monadPlus() {
        return Option.some(LazyEitherInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<Higher<either, L>> monadRec() {
        return LazyEitherInstances.monadRec();
      }


      @Override
      public <T> Option<MonadPlus<Higher<either, L>>> monadPlus(MonoidK<Higher<either, L>> m) {
        return Option.some(LazyEitherInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<Higher<either, L>> traverse() {
        return LazyEitherInstances.traverse();
      }

      @Override
      public <T> Foldable<Higher<either, L>> foldable() {
        return LazyEitherInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<Higher<either, L>>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<Higher<either, L>>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }
  public static <L> Functor<Higher<either, L>> functor() {
    return new Functor<Higher<either, L>>() {

      @Override
      public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
        Either<L,T> xor = narrowK(ds);
        return xor.map(fn);
      }
    };
  }
  public static <L> Pure<Higher<either, L>> unit() {
    return new Pure<Higher<either, L>>() {

      @Override
      public <T> Higher<Higher<either, L>, T> unit(T value) {
        return LazyEither.right(value);
      }
    };
  }
  public static <L> Applicative<Higher<either, L>> applicative() {
    return new Applicative<Higher<either, L>>() {


      @Override
      public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
        Either<L,T> xor = narrowK(apply);
        Either<L, ? extends Function<T, R>> xorFn = narrowK(fn);
        return xorFn.zip(xor,(a,b)->a.apply(b));

      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
        return LazyEitherInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<either, L>, T> unit(T value) {
        return LazyEitherInstances.<L>unit().unit(value);
      }
    };
  }
  public static <L> Monad<Higher<either, L>> monad() {
    return new Monad<Higher<either, L>>() {

      @Override
      public <T, R> Higher<Higher<either, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<either, L>, R>> fn, Higher<Higher<either, L>, T> ds) {
        Either<L,T> xor = narrowK(ds);
        return xor.flatMap(fn.andThen(Either::narrowK));
      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
        return LazyEitherInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
        return LazyEitherInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<either, L>, T> unit(T value) {
        return LazyEitherInstances.<L>unit().unit(value);
      }
    };
  }
  public static <X,T,R> MonadRec<Higher<either, X>> monadRec() {

    return new MonadRec<Higher<either, X>>(){
      @Override
      public <T, R> Higher<Higher<either, X>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<either, X>, ? extends Either<T, R>>> fn) {
        return LazyEither.tailRec(initial,fn.andThen(LazyEither::narrowK));
      }


    };


  }
  public static BiFunctor<either> bifunctor(){
    return new BiFunctor<either>() {
      @Override
      public <T, R, T2, R2> Higher2<either, R, R2> bimap(Function<? super T, ? extends R> fn, Function<? super T2, ? extends R2> fn2, Higher2<either, T, T2> ds) {
        return narrowK(ds).bimap(fn,fn2);
      }
    };
  }
  public static <L> Traverse<Higher<either, L>> traverse() {
    return new Traverse<Higher<either, L>>() {

      @Override
      public <C2, T, R> Higher<C2, Higher<Higher<either, L>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<either, L>, T> ds) {
        Either<L,T> maybe = narrowK(ds);
        return maybe.visit(left->  applicative.unit(Either.<L,R>left(left)),
          right->applicative.map(m-> LazyEither.right(m), fn.apply(right)));
      }

      @Override
      public <C2, T> Higher<C2, Higher<Higher<either, L>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<either, L>, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }



      @Override
      public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
        return LazyEitherInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
        return LazyEitherInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<either, L>, T> unit(T value) {
        return LazyEitherInstances.<L>unit().unit(value);
      }
    };
  }
  public static <L> Foldable<Higher<either, L>> foldable() {
    return new Foldable<Higher<either, L>>() {


      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<either, L>, T> ds) {
        Either<L,T> xor = narrowK(ds);
        return xor.fold(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<either, L>, T> ds) {
        Either<L,T> xor = narrowK(ds);
        return xor.fold(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> nestedA) {
        return foldLeft(mb, narrowK(nestedA).<R>map(fn));
      }
    };
  }
  public static <L> MonadZero<Higher<either, L>> monadZero() {
    return new MonadZero<Higher<either, L>>() {

      @Override
      public Higher<Higher<either, L>, ?> zero() {
        return LazyEither.left(null);
      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<either, L>, R>> fn, Higher<Higher<either, L>, T> ds) {
        Either<L,T> xor = narrowK(ds);
        return xor.flatMap(fn.andThen(Either::narrowK));
      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
        return LazyEitherInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
        return LazyEitherInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<either, L>, T> unit(T value) {
        return LazyEitherInstances.<L>unit().unit(value);
      }
    };
  }
  public static <L> MonadPlus<Higher<either, L>> monadPlus() {
    Monoid m = Monoids.firstRightEither((Either) narrowK(LazyEitherInstances.<L>monadZero().zero()));

    MonoidK<Higher<either, L>> m2 = new MonoidK<Higher<either, L>>() {
      @Override
      public <T> Higher<Higher<either, L>, T> zero() {
        return LazyEitherInstances.<L>monadPlus().zero();
      }

      @Override
      public <T> Higher<Higher<either, L>, T> apply(Higher<Higher<either, L>, T> t1, Higher<Higher<either, L>, T> t2) {
        return SemigroupKs.<L>firstRightEither().apply(t1,t2);
      }
    };

    return monadPlus(m2);
  }
  public static <L,T> MonadPlus<Higher<either, L>> monadPlus(MonoidK<Higher<either, L>> m) {
    return new MonadPlus<Higher<either, L>>() {

      @Override
      public MonoidK<Higher<either, L>> monoid() {
        return m;
      }

      @Override
      public Higher<Higher<either, L>, ?> zero() {
        return LazyEitherInstances.<L>monadZero().zero();
      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<either, L>, R>> fn, Higher<Higher<either, L>, T> ds) {
        Either<L,T> xor = narrowK(ds);
        return xor.flatMap(fn.andThen(Either::narrowK));
      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
        return LazyEitherInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
        return LazyEitherInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<either, L>, T> unit(T value) {
        return LazyEitherInstances.<L>unit().unit(value);
      }
    };
  }
  public static <L> ApplicativeError<Higher<either, L>,L> applicativeError(){
    return new ApplicativeError<Higher<either, L>,L>() {

      @Override
      public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
        return LazyEitherInstances.<L>applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
        return LazyEitherInstances.<L>applicative().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<either, L>, T> unit(T value) {
        return LazyEitherInstances.<L>applicative().unit(value);
      }

      @Override
      public <T> Higher<Higher<either, L>, T> raiseError(L l) {
        return LazyEither.left(l);
      }

      @Override
      public <T> Higher<Higher<either, L>, T> handleErrorWith(Function<? super L, ? extends Higher<Higher<either, L>, ? extends T>> fn, Higher<Higher<either, L>, T> ds) {
        Function<? super L, ? extends Either<L, T>> fn2 = fn.andThen(s -> {

          Higher<Higher<either, L>, T> x = (Higher<Higher<either, L>, T>)s;
          Either<L, T> r = narrowK(x);
          return r;
        });
        return narrowK(ds).flatMapLeftToRight(fn2);
      }
    };
  }

}
