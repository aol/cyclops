package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.lazyEither3;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.LazyEither3;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Nested;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;

import java.util.function.Function;

import static cyclops.control.LazyEither3.narrowK;

public  interface LazyEither3Instances {
  public static  <LT1,LT2,T> Kleisli<Higher<Higher<lazyEither3, LT1>, LT2>,LazyEither3<LT1,LT2,T>,T> kindKleisli(){
    return Kleisli.of(LazyEither3Instances.monad(), LazyEither3::widen);
  }

  public static  <LT1,LT2,T> Cokleisli<Higher<Higher<lazyEither3, LT1>,LT2>,T,LazyEither3<LT1,LT2,T>> kindCokleisli(){
    return Cokleisli.of(LazyEither3::narrowK);
  }
  public static <LT1,LT2,RT> Active<Higher<Higher<lazyEither3, LT1>, LT2>,RT> allTypeclasses(LazyEither3<LT1,LT2,RT> l3){
    return Active.of(l3, LazyEither3Instances.definitions());
  }
  public static  <W2,LT1,LT2,RT,R> Nested<Higher<Higher<lazyEither3, LT1>, LT2>,W2,R> mapM(LazyEither3<LT1,LT2,RT> l3, Function<? super RT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l3.map(fn), LazyEither3Instances.definitions(), defs);
  }
  public static <L1,L2> InstanceDefinitions<Higher<Higher<lazyEither3, L1>, L2>> definitions() {
    return new InstanceDefinitions<Higher<Higher<lazyEither3, L1>, L2>> () {


      @Override
      public <T, R> Functor<Higher<Higher<lazyEither3, L1>, L2>> functor() {
        return LazyEither3Instances.functor();
      }

      @Override
      public <T> Pure<Higher<Higher<lazyEither3, L1>, L2>> unit() {
        return LazyEither3Instances.unit();
      }

      @Override
      public <T, R> Applicative<Higher<Higher<lazyEither3, L1>, L2>> applicative() {
        return LazyEither3Instances.applicative();
      }

      @Override
      public <T, R> Monad<Higher<Higher<lazyEither3, L1>, L2>> monad() {
        return LazyEither3Instances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<Higher<Higher<lazyEither3, L1>, L2>>> monadZero() {
        return Option.some(LazyEither3Instances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<Higher<Higher<lazyEither3, L1>, L2>>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<Higher<Higher<lazyEither3, L1>, L2>> monadRec() {
        return LazyEither3Instances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<Higher<Higher<lazyEither3, L1>, L2>>> monadPlus(MonoidK<Higher<Higher<lazyEither3, L1>, L2>> m) {
        return Maybe.nothing();
      }

      @Override
      public <C2, T> Traverse<Higher<Higher<lazyEither3, L1>, L2>> traverse() {
        return LazyEither3Instances.traverse();
      }

      @Override
      public <T> Foldable<Higher<Higher<lazyEither3, L1>, L2>> foldable() {
        return LazyEither3Instances.foldable();
      }

      @Override
      public <T> Option<Comonad<Higher<Higher<lazyEither3, L1>, L2>>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<Higher<Higher<lazyEither3, L1>, L2>>> unfoldable() {
        return Maybe.nothing();
      }
    };

  }
  public static <L1,L2> Functor<Higher<Higher<lazyEither3, L1>, L2>> functor() {
    return new Functor<Higher<Higher<lazyEither3, L1>, L2>>() {

      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return narrowK(ds).map(fn);
      }
    };
  }
  public static <L1,L2> Pure<Higher<Higher<lazyEither3, L1>, L2>> unit() {
    return new Pure<Higher<Higher<lazyEither3, L1>, L2>>() {

      @Override
      public <T> Higher<Higher<Higher<lazyEither3, L1>, L2>, T> unit(T value) {
        return LazyEither3.right(value);
      }
    };
  }
  public static <L1,L2> Applicative<Higher<Higher<lazyEither3, L1>, L2>> applicative() {
    return new Applicative<Higher<Higher<lazyEither3, L1>, L2>>() {


      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> ap(Higher<Higher<Higher<lazyEither3, L1>, L2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> apply) {
        return  narrowK(fn).flatMap(x -> narrowK(apply).map(x));

      }

      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return LazyEither3Instances.<L1,L2>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<Higher<lazyEither3, L1>, L2>, T> unit(T value) {
        return LazyEither3Instances.<L1,L2>unit().unit(value);
      }
    };
  }
  public static <L1,L2> Monad<Higher<Higher<lazyEither3, L1>, L2>> monad() {
    return new Monad<Higher<Higher<lazyEither3, L1>, L2>>() {


      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> ap(Higher<Higher<Higher<lazyEither3, L1>, L2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> apply) {
        return LazyEither3Instances.<L1,L2>applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return LazyEither3Instances.<L1,L2>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<Higher<lazyEither3, L1>, L2>, T> unit(T value) {
        return LazyEither3Instances.<L1,L2>unit().unit(value);
      }

      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<lazyEither3, L1>, L2>, R>> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return narrowK(ds).flatMap(fn.andThen(m->narrowK(m)));
      }
    };
  }
  public static <L1,L2,T,R> MonadRec<Higher<Higher<lazyEither3, L1>, L2>> monadRec(){

    return new MonadRec<Higher<Higher<lazyEither3, L1>, L2>>(){

      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<Higher<lazyEither3, L1>, L2>, ? extends Either<T, R>>> fn) {
        return narrowK(fn.apply(initial)).flatMap( eval ->
          eval.fold(s->narrowK(tailRec(s,fn)), p-> LazyEither3.right(p)));
      }


    };
  }
  public static <L1,L2> MonadZero<Higher<Higher<lazyEither3, L1>, L2>> monadZero() {
    return new MonadZero<Higher<Higher<lazyEither3, L1>, L2>>() {


      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> ap(Higher<Higher<Higher<lazyEither3, L1>, L2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> apply) {
        return LazyEither3Instances.<L1,L2>applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return LazyEither3Instances.<L1,L2>functor().map(fn,ds);
      }

      @Override
      public Higher<Higher<Higher<lazyEither3, L1>, L2>, ?> zero() {
        return LazyEither3.left1(null);
      }

      @Override
      public <T> Higher<Higher<Higher<lazyEither3, L1>, L2>, T> unit(T value) {
        return LazyEither3Instances.<L1,L2>unit().unit(value);
      }

      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<lazyEither3, L1>, L2>, R>> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return LazyEither3Instances.<L1,L2>monad().flatMap(fn,ds);
      }
    };
  }
  public static  <L1,L2> Traverse<Higher<Higher<lazyEither3, L1>, L2>> traverse() {
    return new Traverse<Higher<Higher<lazyEither3, L1>, L2>> () {


      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> ap(Higher<Higher<Higher<lazyEither3, L1>, L2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> apply) {
        return LazyEither3Instances.<L1,L2>applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<Higher<Higher<lazyEither3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return LazyEither3Instances.<L1,L2>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<Higher<lazyEither3, L1>, L2>, T> unit(T value) {
        return LazyEither3Instances.<L1, L2>unit().unit(value);
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<Higher<Higher<lazyEither3, L1>, L2>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        LazyEither3<L1,L2,T> maybe = narrowK(ds);
        return maybe.fold(left->  applicative.unit(LazyEither3.<L1,L2,R>left1(left)),
          middle->applicative.unit(LazyEither3.<L1,L2,R>left2(middle)),
          right->applicative.map(m-> LazyEither3.right(m), fn.apply(right)));
      }

      @Override
      public <C2, T> Higher<C2, Higher<Higher<Higher<lazyEither3, L1>, L2>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<Higher<lazyEither3, L1>, L2>, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }


    };
  }
  public static <L1,L2> Foldable<Higher<Higher<lazyEither3, L1>, L2>> foldable() {
    return new Foldable<Higher<Higher<lazyEither3, L1>, L2>>() {


      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> nestedA) {
        return foldLeft(mb,narrowK(nestedA).<R>map(fn));
      }

      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return narrowK(ds).fold(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<lazyEither3, L1>, L2>, T> ds) {
        return narrowK(ds).fold(monoid);
      }

    };
  }


}
