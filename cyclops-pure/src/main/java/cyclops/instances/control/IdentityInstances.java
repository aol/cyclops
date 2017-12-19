package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Identity;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;

import java.util.function.Function;

import static cyclops.control.Identity.narrowK;
import static cyclops.control.Identity.of;

public class IdentityInstances {

  public static <W1,T> Nested<DataWitness.identity,W1,T> nested(Identity<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, IdentityInstances.definitions(),def2);
  }
  public <W1,T> Product<DataWitness.identity,W1,T> product(Identity<T> id, Active<W1,T> active){
    return Product.of(allTypeclasses(id),active);
  }
  public <W1,T> Coproduct<W1,DataWitness.identity,T> coproduct(Identity<T> id, InstanceDefinitions<W1> def2){
    return Coproduct.right(id,def2, IdentityInstances.definitions());
  }
  public <T> Active<DataWitness.identity,T> allTypeclasses(Identity<T> id){
    return Active.of(id, IdentityInstances.definitions());
  }

  public <W2,R,T> Nested<DataWitness.identity,W2,R> mapM(Identity<T> id,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(id.map(fn), IdentityInstances.definitions(), defs);
  }
  public static  <T> Kleisli<DataWitness.identity,Identity<T>,T> kindKleisli(){
    return Kleisli.of(IdentityInstances.monad(), Identity::widen);
  }

  public static  <T> Cokleisli<DataWitness.identity,T,Identity<T>> kindCokleisli(){
    return Cokleisli.of(Identity::narrowK);
  }
  public static InstanceDefinitions<DataWitness.identity> definitions(){
    return new InstanceDefinitions<DataWitness.identity>() {
      @Override
      public <T, R> Functor<DataWitness.identity> functor() {
        return IdentityInstances.functor();
      }

      @Override
      public <T> Pure<DataWitness.identity> unit() {
        return IdentityInstances.unit();
      }

      @Override
      public <T, R> Applicative<DataWitness.identity> applicative() {
        return IdentityInstances.applicative();
      }

      @Override
      public <T, R> Monad<DataWitness.identity> monad() {
        return IdentityInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<DataWitness.identity>> monadZero() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<MonadPlus<DataWitness.identity>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<DataWitness.identity> monadRec() {
        return IdentityInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<DataWitness.identity>> monadPlus(MonoidK<DataWitness.identity> m) {
        return Maybe.nothing();
      }

      @Override
      public <C2, T> Traverse<DataWitness.identity> traverse() {
        return IdentityInstances.traverse();
      }

      @Override
      public <T> Foldable<DataWitness.identity> foldable() {
        return IdentityInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<DataWitness.identity>> comonad() {
        return Maybe.just(IdentityInstances.comonad());
      }

      @Override
      public <T> Option<Unfoldable<DataWitness.identity>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

  public static Functor<DataWitness.identity> functor(){
    return new Functor<DataWitness.identity>(){

      @Override
      public <T, R> Higher<DataWitness.identity, R> map(Function<? super T, ? extends R> fn, Higher<DataWitness.identity, T> ds) {
        return narrowK(ds).map(fn);
      }
    };
  }

  public static Pure<DataWitness.identity> unit(){
    return new Pure<DataWitness.identity>(){


      @Override
      public <T> Higher<DataWitness.identity, T> unit(T value) {
        return of(value);
      }
    };
  }
  public static Applicative<DataWitness.identity> applicative(){
    return new Applicative<DataWitness.identity>(){


      @Override
      public <T, R> Higher<DataWitness.identity, R> ap(Higher<DataWitness.identity, ? extends Function<T, R>> fn, Higher<DataWitness.identity, T> apply) {
        Identity<? extends Function<T, R>> f = narrowK(fn);
        Identity<T> ap = narrowK(apply);
        return f.flatMap(x -> ap.map(x));
      }

      @Override
      public <T, R> Higher<DataWitness.identity, R> map(Function<? super T, ? extends R> fn, Higher<DataWitness.identity, T> ds) {
        return functor().map(fn,ds);
      }

      @Override
      public <T> Higher<DataWitness.identity, T> unit(T value) {
        return IdentityInstances.unit().unit(value);
      }
    };
  }
  public static Monad<DataWitness.identity> monad(){
    return new Monad<DataWitness.identity>(){


      @Override
      public <T, R> Higher<DataWitness.identity, R> ap(Higher<DataWitness.identity, ? extends Function<T, R>> fn, Higher<DataWitness.identity, T> apply) {
        return applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<DataWitness.identity, R> map(Function<? super T, ? extends R> fn, Higher<DataWitness.identity, T> ds) {
        return functor().map(fn,ds);
      }

      @Override
      public <T> Higher<DataWitness.identity, T> unit(T value) {
        return IdentityInstances.unit().unit(value);
      }

      @Override
      public <T, R> Higher<DataWitness.identity, R> flatMap(Function<? super T, ? extends Higher<DataWitness.identity, R>> fn, Higher<DataWitness.identity, T> ds) {
        return narrowK(ds).flatMap(fn.andThen(i->narrowK(i)));
      }
    };
  }

  public static  MonadRec<DataWitness.identity> monadRec() {

    return new MonadRec<DataWitness.identity>(){
      @Override
      public <T, R> Higher<DataWitness.identity, R> tailRec(T initial, Function<? super T, ? extends Higher<DataWitness.identity, ? extends Either<T, R>>> fn) {
        return Identity.tailRec(initial,fn.andThen(Identity::narrowK));
      }




    };


  }
  public static Traverse<DataWitness.identity> traverse(){
    return new Traverse<DataWitness.identity>(){

      @Override
      public <T, R> Higher<DataWitness.identity, R> ap(Higher<DataWitness.identity, ? extends Function<T, R>> fn, Higher<DataWitness.identity, T> apply) {
        return applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<DataWitness.identity, R> map(Function<? super T, ? extends R> fn, Higher<DataWitness.identity, T> ds) {
        return functor().map(fn,ds);
      }

      @Override
      public <T> Higher<DataWitness.identity, T> unit(T value) {
        return IdentityInstances.unit().unit(value);
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<DataWitness.identity, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<DataWitness.identity, T> ds) {
        Identity<T> id = narrowK(ds);
        Function<R, Identity<R>> rightFn = r -> of(r);
        return applicative.map(rightFn, fn.apply(id.value));
      }

      @Override
      public <C2, T> Higher<C2, Higher<DataWitness.identity, T>> sequenceA(Applicative<C2> applicative, Higher<DataWitness.identity, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }
    };
  }
  public static Foldable<DataWitness.identity> foldable(){
    return new Foldable<DataWitness.identity>(){


      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<DataWitness.identity, T> ds) {
        return monoid.apply(narrowK(ds).get(),monoid.zero());
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<DataWitness.identity, T> ds) {
        return monoid.apply(monoid.zero(),narrowK(ds).get());
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<DataWitness.identity, T> nestedA) {
        return foldLeft(mb,narrowK(nestedA).<R>map(fn));
      }
    };
  }
  public static Comonad<DataWitness.identity> comonad(){
    return new ComonadByPure<DataWitness.identity>(){


      @Override
      public <T> T extract(Higher<DataWitness.identity, T> ds) {
        return narrowK(ds).extract();
      }

      @Override
      public <T, R> Higher<DataWitness.identity, R> map(Function<? super T, ? extends R> fn, Higher<DataWitness.identity, T> ds) {
        return functor().map(fn,ds);
      }

      @Override
      public <T> Higher<DataWitness.identity, T> unit(T value) {
        return IdentityInstances.unit().unit(value);
      }
    };
  }

}
