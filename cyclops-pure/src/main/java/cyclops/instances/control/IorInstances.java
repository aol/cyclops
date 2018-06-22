package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.ior;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Ior;
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

import java.util.function.Function;

import static cyclops.control.Ior.narrowK;

public class IorInstances {
  public static  <L,T> Kleisli<Higher<ior,L>,Ior<L,T>,T> kindKleisli(){
    return Kleisli.of(IorInstances.monad(), Ior::widen);
  }

  public static  <L,T> Cokleisli<Higher<ior,L>,T,Ior<L,T>> kindCokleisli(){
    return Cokleisli.of(Ior::narrowK);
  }
  public static <W1,ST,PT> Nested<Higher<ior,ST>,W1,PT> nested(Ior<ST,Higher<W1,PT>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, IorInstances.definitions(),def2);
  }
  public static <W1,LT,RT> Product<Higher<ior, LT>,W1, RT> product(Ior<LT,RT> ior, Active<W1, RT> active){
    return Product.of(allTypeclasses(ior),active);
  }
  public static <W1,LT,RT> Coproduct<W1,Higher<ior, LT>, RT> coproduct(Ior<LT,RT> ior, InstanceDefinitions<W1> def2){
    return Coproduct.right(ior,def2, IorInstances.definitions());
  }
  public static <LT,RT> Active<Higher<ior, LT>, RT> allTypeclasses(Ior<LT,RT> ior){
    return Active.of(ior, IorInstances.definitions());
  }
  public static <W2,R,LT,RT> Nested<Higher<ior, LT>,W2,R> mapM(Ior<LT,RT> ior,Function<? super RT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(ior.map(fn), IorInstances.definitions(), defs);
  }

  public static <L> InstanceDefinitions<Higher<ior, L>> definitions(){
    return new InstanceDefinitions<Higher<ior, L>>() {


      @Override
      public <T, R> Functor<Higher<ior, L>> functor() {
        return IorInstances.functor();
      }

      @Override
      public <T> Pure<Higher<ior, L>> unit() {
        return IorInstances.unit();
      }

      @Override
      public <T, R> Applicative<Higher<ior, L>> applicative() {
        return IorInstances.applicative();
      }

      @Override
      public <T, R> Monad<Higher<ior, L>> monad() {
        return IorInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<Higher<ior, L>>> monadZero() {
        return Option.some(IorInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<Higher<ior, L>>> monadPlus() {
        return Option.some(IorInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<Higher<ior, L>> monadRec() {
        return IorInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<Higher<ior, L>>> monadPlus(MonoidK<Higher<ior, L>> m) {
        return Option.some(IorInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<Higher<ior, L>> traverse() {
        return IorInstances.traverse();
      }

      @Override
      public <T> Foldable<Higher<ior, L>> foldable() {
        return IorInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<Higher<ior, L>>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<Higher<ior, L>>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }
  public static <L> Functor<Higher<ior, L>> functor() {
    return new Functor<Higher<ior, L>>() {

      @Override
      public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
        Ior<L,T> ior = narrowK(ds);
        return ior.map(fn);
      }
    };
  }
  public static <L> Pure<Higher<ior, L>> unit() {
    return new Pure<Higher<ior, L>>() {

      @Override
      public <T> Higher<Higher<ior, L>, T> unit(T value) {
        return Ior.right(value);
      }
    };
  }
  public static <L> Applicative<Higher<ior, L>> applicative() {
    return new Applicative<Higher<ior, L>>() {


      @Override
      public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
        Ior<L,T>  ior = narrowK(apply);
        Ior<L, ? extends Function<T, R>> iorFn = narrowK(fn);
        return iorFn.zip(ior,(a,b)->a.apply(b));

      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
        return IorInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<ior, L>, T> unit(T value) {
        return IorInstances.<L>unit().unit(value);
      }
    };
  }
  public static BiFunctor<ior> bifunctor(){
    return new BiFunctor<ior>() {
      @Override
      public <T, R, T2, R2> Higher2<ior, R, R2> bimap(Function<? super T, ? extends R> fn, Function<? super T2, ? extends R2> fn2, Higher2<ior, T, T2> ds) {
        return narrowK(ds).bimap(fn,fn2);
      }
    };
  }
  public static <L> Monad<Higher<ior, L>> monad() {
    return new Monad<Higher<ior, L>>() {

      @Override
      public <T, R> Higher<Higher<ior, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<ior, L>, R>> fn, Higher<Higher<ior, L>, T> ds) {
        Ior<L,T> ior = narrowK(ds);
        return ior.flatMap(fn.andThen(Ior::narrowK));
      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
        return IorInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
        return IorInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<ior, L>, T> unit(T value) {
        return IorInstances.<L>unit().unit(value);
      }
    };
  }
  public static <L> Traverse<Higher<ior, L>> traverse() {
    return new Traverse<Higher<ior, L>>() {

      @Override
      public <C2, T, R> Higher<C2, Higher<Higher<ior, L>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<ior, L>, T> ds) {
        Ior<L,T> maybe = narrowK(ds);

        return maybe.fold(left->  applicative.unit(Ior.<L,R>left(left)),
          right->applicative.map(m->Ior.right(m), fn.apply(right)),
          (l,r)-> applicative.map(m->Ior.both(l,m), fn.apply(r)));
      }

      @Override
      public <C2, T> Higher<C2, Higher<Higher<ior, L>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<ior, L>, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }



      @Override
      public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
        return IorInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
        return IorInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<ior, L>, T> unit(T value) {
        return IorInstances.<L>unit().unit(value);
      }
    };
  }
  public static <L> Foldable<Higher<ior, L>> foldable() {
    return new Foldable<Higher<ior, L>>() {


      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<ior, L>, T> ds) {
        Ior<L,T> ior = narrowK(ds);
        return ior.fold(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<ior, L>, T> ds) {
        Ior<L,T> ior = narrowK(ds);
        return ior.fold(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> nestedA) {
        return narrowK(nestedA).<R>map(fn).fold(mb);
      }
    };
  }
  public static <L> MonadZero<Higher<ior, L>> monadZero() {
    return new MonadZero<Higher<ior, L>>() {

      @Override
      public Higher<Higher<ior, L>, ?> zero() {
        return Ior.left(null);
      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<ior, L>, R>> fn, Higher<Higher<ior, L>, T> ds) {
        Ior<L,T> ior = narrowK(ds);
        return ior.flatMap(fn.andThen(Ior::narrowK));
      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
        return IorInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
        return IorInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<ior, L>, T> unit(T value) {
        return IorInstances.<L>unit().unit(value);
      }
    };
  }
  public static <X,T,R> MonadRec<Higher<ior, X>> monadRec() {

    return new MonadRec<Higher<ior, X>>(){
      @Override
      public <T, R> Higher<Higher<ior, X>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<ior, X>, ? extends Either<T, R>>> fn) {
        Ior<X,? extends Either<T, R>> next[] = new Ior[1];
        next[0] = Ior.right(Either.left(initial));
        boolean cont = true;
        do {
          cont = next[0].fold(p -> p.fold(s -> {
            next[0] = narrowK(fn.apply(s));
            return true;
          }, pr -> false), () -> false);
        } while (cont);
        return next[0].map(x->x.orElse(null));
      }


    };


  }
  public static <L> MonadPlus<Higher<ior, L>> monadPlus() {


    MonoidK<Higher<ior, L>> m = new MonoidK<Higher<ior, L>>() {
      @Override
      public <T> Higher<Higher<ior, L>, T> zero() {
        return IorInstances.<L>monadZero().zero();
      }

      @Override
      public <T> Higher<Higher<ior, L>, T> apply(Higher<Higher<ior, L>, T> t1, Higher<Higher<ior, L>, T> t2) {
        return SemigroupKs.<L>firstPrimaryIor().apply(t1,t2);
      }
    };
    return monadPlus(m);
  }
  public static <L,T> MonadPlus<Higher<ior, L>> monadPlus(MonoidK<Higher<ior, L>> m) {
    return new MonadPlus<Higher<ior, L>>() {

      @Override
      public MonoidK<Higher<ior, L>> monoid() {
        return m;
      }

      @Override
      public Higher<Higher<ior, L>, ?> zero() {
        return IorInstances.<L>monadZero().zero();
      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<ior, L>, R>> fn, Higher<Higher<ior, L>, T> ds) {
        Ior<L,T> ior = narrowK(ds);
        return ior.flatMap(fn.andThen(Ior::narrowK));
      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
        return IorInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
        return IorInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<ior, L>, T> unit(T value) {
        return IorInstances.<L>unit().unit(value);
      }
    };
  }

}
