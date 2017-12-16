package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.tryType;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Try;
import cyclops.function.Monoid;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

import static cyclops.control.Try.narrowK;

@UtilityClass
public class TryInstances {

  public static <W1,X extends Throwable,T> Nested<Higher<tryType,X>,W1,T> nested(Try<Higher<W1,T>,X> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, TryInstances.definitions(),def2);
  }
  public static <W1,T, X extends Throwable> Product<Higher<tryType,X>,W1,T> product(Try<T,X> t,Active<W1,T> active){
    return Product.of(allTypeclasses(t),active);
  }
  public static <W1,T, X extends Throwable> Coproduct<W1,Higher<tryType,X>,T> coproduct(Try<T,X> t,InstanceDefinitions<W1> def2){
    return Coproduct.right(t,def2, TryInstances.definitions());
  }
  public static  <X extends Throwable,T> Kleisli<Higher<tryType,X>,Try<T,X>,T> kindKleisli(){
    return Kleisli.of(TryInstances.monad(), Try::widen);
  }

  public static  <X extends Throwable,T> Cokleisli<Higher<tryType,X>,T,Try<T,X>> kindCokleisli(){
    return Cokleisli.of(Try::narrowK);
  }

  public static <T, X extends Throwable> Active<Higher<tryType,X>,T> allTypeclasses(Try<T,X> t){
    return Active.of(t, TryInstances.definitions());
  }
  public static <W2,R,T, X extends Throwable> Nested<Higher<tryType,X>,W2,R> mapM(Try<T,X> t,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(t.map(fn), TryInstances.definitions(), defs);
  }
  public static <L extends Throwable> InstanceDefinitions<Higher<tryType, L>> definitions(){
    return new InstanceDefinitions<Higher<tryType, L>>() {
      @Override
      public <T, R> Functor<Higher<tryType, L>> functor() {
        return TryInstances.functor();
      }

      @Override
      public <T> Pure<Higher<tryType, L>> unit() {
        return TryInstances.unit();
      }

      @Override
      public <T, R> Applicative<Higher<tryType, L>> applicative() {
        return TryInstances.applicative();
      }

      @Override
      public <T, R> Monad<Higher<tryType, L>> monad() {
        return TryInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<Higher<tryType, L>>> monadZero() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<MonadPlus<Higher<tryType, L>>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<Higher<tryType, L>> monadRec() {
        return TryInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<Higher<tryType, L>>> monadPlus(MonoidK<Higher<tryType, L>> m) {
        return Maybe.nothing();
      }


      @Override
      public <C2, T> Traverse<Higher<tryType, L>> traverse() {
        return TryInstances.traverse();
      }

      @Override
      public <T> Foldable<Higher<tryType, L>> foldable() {
        return TryInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<Higher<tryType, L>>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<Higher<tryType, L>>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }
  public static <L extends Throwable> Functor<Higher<tryType, L>> functor() {
    return new Functor<Higher<tryType, L>>() {

      @Override
      public <T, R> Higher<Higher<tryType, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tryType, L>, T> ds) {
        Try<T,L> tryType = narrowK(ds);
        return tryType.map(fn);
      }
    };
  }
  public static <L extends Throwable> Pure<Higher<tryType, L>> unit() {
    return new Pure<Higher<tryType, L>>() {

      @Override
      public <T> Higher<Higher<tryType, L>, T> unit(T value) {
        return Try.success(value);
      }
    };
  }
  public static <L extends Throwable> Applicative<Higher<tryType, L>> applicative() {
    return new Applicative<Higher<tryType, L>>() {


      @Override
      public <T, R> Higher<Higher<tryType, L>, R> ap(Higher<Higher<tryType, L>, ? extends Function<T, R>> fn, Higher<Higher<tryType, L>, T> apply) {
        Try<T,L>  tryType = narrowK(apply);
        Try<? extends Function<T, R>, L> tryTypeFn = narrowK(fn);
        return tryTypeFn.zip(tryType,(a,b)->a.apply(b));

      }

      @Override
      public <T, R> Higher<Higher<tryType, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tryType, L>, T> ds) {
        return TryInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<tryType, L>, T> unit(T value) {
        return TryInstances.<L>unit().unit(value);
      }
    };
  }
  public static <L extends Throwable> Monad<Higher<tryType, L>> monad() {
    return new Monad<Higher<tryType, L>>() {

      @Override
      public <T, R> Higher<Higher<tryType, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<tryType, L>, R>> fn, Higher<Higher<tryType, L>, T> ds) {
        Try<T,L> tryType = narrowK(ds);
        return tryType.flatMap(fn.andThen(Try::narrowK));
      }

      @Override
      public <T, R> Higher<Higher<tryType, L>, R> ap(Higher<Higher<tryType, L>, ? extends Function<T, R>> fn, Higher<Higher<tryType, L>, T> apply) {
        return TryInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<tryType, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tryType, L>, T> ds) {
        return TryInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<tryType, L>, T> unit(T value) {
        return TryInstances.<L>unit().unit(value);
      }
    };
  }
  public static <X extends Throwable,T,R> MonadRec<Higher<tryType, X>> monadRec() {

    return new MonadRec<Higher<tryType, X>>(){
      @Override
      public <T, R> Higher<Higher<tryType, X>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<tryType, X>, ? extends Either<T, R>>> fn) {
        Try<? extends Either<T, R>,X> next[] = new Try[1];
        next[0] = Try.success(Either.left(initial));
        boolean cont = true;
        do {
          cont = next[0].visit(p -> p.visit(s -> {
            next[0] = narrowK(fn.apply(s));
            return true;
          }, pr -> false), () -> false);
        } while (cont);
        return next[0].map(x->x.orElse(null));
      }


    };


  }

  public static <L extends Throwable> Traverse<Higher<tryType, L>> traverse() {
    return new Traverse<Higher<tryType, L>>() {

      @Override
      public <C2, T, R> Higher<C2, Higher<Higher<tryType, L>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<tryType, L>, T> ds) {
        Try<T, L> maybe = narrowK(ds);
        Function<R, Try<R, L>> rightFn = r -> Try.success(r);

        return maybe.fold(r->applicative.map(rightFn, fn.apply(r)),l->applicative.unit(Try.failure(l)));

      }

      @Override
      public <C2, T> Higher<C2, Higher<Higher<tryType, L>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<tryType, L>, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }



      @Override
      public <T, R> Higher<Higher<tryType, L>, R> ap(Higher<Higher<tryType, L>, ? extends Function<T, R>> fn, Higher<Higher<tryType, L>, T> apply) {
        return TryInstances.<L>applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<Higher<tryType, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tryType, L>, T> ds) {
        return TryInstances.<L>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<tryType, L>, T> unit(T value) {
        return TryInstances.<L>unit().unit(value);
      }
    };
  }
  public static <L extends Throwable> Foldable<Higher<tryType, L>> foldable() {
    return new Foldable<Higher<tryType, L>>() {


      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<tryType, L>, T> ds) {
        Try<T,L> tryType = narrowK(ds);
        return tryType.fold(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<tryType, L>, T> ds) {
        Try<T,L> tryType = narrowK(ds);
        return tryType.fold(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<tryType, L>, T> nestedA) {
        return foldLeft(mb, narrowK(nestedA).<R>map(fn));
      }
    };
  }


}
