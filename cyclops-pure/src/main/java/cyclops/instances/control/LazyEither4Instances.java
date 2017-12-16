package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.lazyEither3;
import com.oath.cyclops.hkt.DataWitness.lazyEither4;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.*;
import cyclops.function.Monoid;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;

import java.util.function.Function;

import java.util.function.Function;

import static cyclops.control.LazyEither4.narrowK;

public class LazyEither4Instances {
  public static <LT1, LT2, LT3, RT> Active<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,RT> allTypeclasses(LazyEither4<LT1,LT2,LT3,RT> l4){
    return Active.of(l4, LazyEither4Instances.definitions());
  }
  public static <W2,LT1, LT2, LT3, RT,R> Nested<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,W2,R> mapM(LazyEither4<LT1,LT2,LT3,RT> l4,Function<? super RT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l4.map(fn), LazyEither4Instances.definitions(), defs);
  }
  public static  <LT1,LT2,LT3,T> Kleisli<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,LazyEither4<LT1,LT2,LT3,T>,T> kindKleisli(){
    return Kleisli.of(LazyEither4Instances.monad(), LazyEither4::widen);
  }

  public static  <LT1,LT2,LT3,T> Cokleisli<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,T,LazyEither4<LT1,LT2,LT3,T>> kindCokleisli(){
    return Cokleisli.of(LazyEither4::narrowK);
  }
  public static <L1,L2,L3> InstanceDefinitions<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> definitions() {
    return new InstanceDefinitions<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


      @Override
      public <T, R> Functor<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> functor() {
        return LazyEither4Instances.functor();
      }

      @Override
      public <T> Pure<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> unit() {
        return LazyEither4Instances.unit();
      }

      @Override
      public <T, R> Applicative<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> applicative() {
        return LazyEither4Instances.applicative();
      }

      @Override
      public <T, R> Monad<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monad() {
        return LazyEither4Instances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> monadZero() {
        return Option.some(LazyEither4Instances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monadRec() {
        return LazyEither4Instances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> monadPlus(MonoidK<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>> m) {
        return Maybe.nothing();
      }

      @Override
      public <C2, T> Traverse<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> traverse() {
        return LazyEither4Instances.traverse();
      }

      @Override
      public <T> Foldable<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> foldable() {
        return LazyEither4Instances.foldable();
      }

      @Override
      public <T> Option<Comonad<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> unfoldable() {
        return Maybe.nothing();
      }
    };

  }
  public static <L1,L2,L3> Functor<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> functor() {
    return new Functor<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {

      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return narrowK(ds).map(fn);
      }
    };
  }
  public static <L1,L2,L3> Pure<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> unit() {
    return new Pure<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {

      @Override
      public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
        return LazyEither4.right(value);
      }
    };
  }
  public static <L1,L2,L3> Applicative<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> applicative() {
    return new Applicative<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> ap(Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> apply) {
        return  narrowK(fn).flatMap(x -> narrowK(apply).map(x));

      }

      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return LazyEither4Instances.<L1,L2,L3>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
        return LazyEither4Instances.<L1,L2,L3>unit().unit(value);
      }
    };
  }
  public static <L1,L2,L3> Monad<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monad() {
    return new Monad<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> ap(Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> apply) {
        return LazyEither4Instances.<L1,L2,L3>applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return LazyEither4Instances.<L1,L2,L3>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
        return LazyEither4Instances.<L1,L2,L3>unit().unit(value);
      }

      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return narrowK(ds).flatMap(fn.andThen(m->narrowK(m)));
      }
    };
  }
  public static <L1,L2,L3,T,R> MonadRec<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monadRec(){

    return new MonadRec<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>(){

      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Either<T, R>>> fn) {
        return narrowK(fn.apply(initial)).flatMap( eval ->
          eval.visit(s->narrowK(tailRec(s,fn)),p-> LazyEither4.right(p)));
      }


    };
  }
  public static <L1,L2,L3> MonadZero<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monadZero() {
    return new MonadZero<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> ap(Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> apply) {
        return LazyEither4Instances.<L1,L2,L3>applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return LazyEither4Instances.<L1,L2,L3>functor().map(fn,ds);
      }

      @Override
      public Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ?> zero() {
        return LazyEither4.left1(null);
      }

      @Override
      public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
        return LazyEither4Instances.<L1,L2,L3>unit().unit(value);
      }

      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return LazyEither4Instances.<L1,L2,L3>monad().flatMap(fn,ds);
      }
    };
  }
  public static  <L1,L2,L3> Traverse<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> traverse() {
    return new Traverse<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> () {


      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> ap(Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> apply) {
        return LazyEither4Instances.<L1,L2,L3>applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return LazyEither4Instances.<L1,L2,L3>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
        return LazyEither4Instances.<L1, L2,L3>unit().unit(value);
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        LazyEither4<L1,L2,L3,T> maybe = narrowK(ds);
        return maybe.visit(left->  applicative.unit(LazyEither4.<L1,L2,L3,R>left1(left)),
          middle1->applicative.unit(LazyEither4.<L1,L2,L3,R>left2(middle1)),
          middle2->applicative.unit(LazyEither4.<L1,L2,L3,R>left3(middle2)),
          right->applicative.map(m-> LazyEither4.right(m), fn.apply(right)));
      }

      @Override
      public <C2, T> Higher<C2, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }


    };
  }
  public static <L1,L2,L3> Foldable<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> foldable() {
    return new Foldable<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>, T> nestedA) {
        return foldLeft(mb, narrowK(nestedA).<R>map(fn));
      }

      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return narrowK(ds).fold(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
        return narrowK(ds).fold(monoid);
      }

    };
  }


}
