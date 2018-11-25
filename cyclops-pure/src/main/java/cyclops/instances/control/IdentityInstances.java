package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.identity;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.control.Either;
import cyclops.control.Identity;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.MonadPlus;
import cyclops.typeclasses.monad.MonadRec;
import cyclops.typeclasses.monad.MonadZero;
import cyclops.typeclasses.monad.Traverse;
import cyclops.typeclasses.monad.TraverseByTraverse;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static cyclops.control.Identity.narrowK;

public class IdentityInstances {

  public static <W1,T> Nested<identity,W1,T> nested(Identity<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, IdentityInstances.definitions(),def2);
  }
  public <W1,T> Product<identity,W1,T> product(Identity<T> id, Active<W1,T> active){
    return Product.of(allTypeclasses(id),active);
  }
  public <W1,T> Coproduct<W1, identity,T> coproduct(Identity<T> id, InstanceDefinitions<W1> def2){
    return Coproduct.right(id,def2, IdentityInstances.definitions());
  }
  public <T> Active<identity,T> allTypeclasses(Identity<T> id){
    return Active.of(id, IdentityInstances.definitions());
  }

  public <W2,R,T> Nested<identity,W2,R> mapM(Identity<T> id, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(id.map(fn), IdentityInstances.definitions(), defs);
  }
  public static  <T> Kleisli<identity,Identity<T>,T> kindKleisli(){
    return Kleisli.of(IdentityInstances.monad(), Identity::widen);
  }

  public static  <T> Cokleisli<identity,T,Identity<T>> kindCokleisli(){
    return Cokleisli.of(Identity::narrowK);
  }
  public static InstanceDefinitions<identity> definitions(){
    return new InstanceDefinitions<identity>() {
      @Override
      public <T, R> Functor<identity> functor() {
        return IdentityInstances.functor();
      }

      @Override
      public <T> Pure<identity> unit() {
        return IdentityInstances.unit();
      }

      @Override
      public <T, R> Applicative<identity> applicative() {
        return IdentityInstances.applicative();
      }

      @Override
      public <T, R> Monad<identity> monad() {
        return IdentityInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<identity>> monadZero() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<MonadPlus<identity>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<identity> monadRec() {
        return IdentityInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<identity>> monadPlus(MonoidK<identity> m) {
        return Maybe.nothing();
      }

      @Override
      public <C2, T> Traverse<identity> traverse() {
        return IdentityInstances.traverse();
      }

      @Override
      public <T> Foldable<identity> foldable() {
        return IdentityInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<identity>> comonad() {
        return Maybe.just(IdentityInstances.comonad());
      }

      @Override
      public <T> Option<Unfoldable<identity>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

    private final static IdentityTypeclasses INSTANCE = new IdentityTypeclasses();

    @AllArgsConstructor
    public static class IdentityTypeclasses  implements Monad<identity>,
                                                        MonadRec<identity>,
                                                        TraverseByTraverse<identity>,
                                                        Foldable<identity>,
                                                        Comonad<identity>{

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<identity, T> ds) {
            return monoid.apply(narrowK(ds).get(),monoid.zero());
        }



        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<identity, T> ds) {
            return monoid.apply(monoid.zero(),narrowK(ds).get());
        }



        @Override
        public <T, R> Higher<identity, R> flatMap(Function<? super T, ? extends Higher<identity, R>> fn, Higher<identity, T> ds) {
            return Identity.narrowK(ds).flatMap(t-> Identity.narrowK(fn.apply(t)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<identity, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<identity, T> ds) {
            Identity<T> identity = Identity.narrowK(ds);
            return applicative.map(Identity::of, fn.apply(identity.get()));
        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<identity, T> ds) {
            Identity<R>  opt  = Identity.narrowK(ds).map(fn);
            return foldLeft(mb,opt);
        }

        @Override
        public <T, R> Higher<identity, R> ap(Higher<identity, ? extends Function<T, R>> fn, Higher<identity, T> apply) {
            return Identity.narrowK(apply).zip(Identity.narrowK(fn), (a, b)->b.apply(a));
        }

        @Override
        public <T> Higher<identity, T> unit(T value) {
            return Identity.of(value);
        }

        @Override
        public <T, R> Higher<identity, R> map(Function<? super T, ? extends R> fn, Higher<identity, T> ds) {
            return Identity.narrowK(ds).map(fn);
        }

        @Override
        public <T, R> Higher<identity, R> tailRec(T initial, Function<? super T, ? extends Higher<identity, ? extends Either<T, R>>> fn) {
            return Identity.tailRec(initial,t-> Identity.narrowK(fn.apply(t)));
        }

        @Override
        public <T> Higher<identity, Higher<identity, T>> nest(Higher<identity, T> ds) {
            return Identity.of(ds);
        }

        @Override
        public <T, R> Higher<identity, R> coflatMap(Function<? super Higher<identity, T>, R> mapper, Higher<identity, T> ds) {
            return Identity.of(mapper.apply(ds));
        }

        @Override
        public <T> T extract(Higher<identity, T> ds) {
            return Identity.narrowK(ds).get();
        }
    }
    public static <T,R>Functor<identity> functor(){
        return INSTANCE;
    }

    public static <T> Pure<identity> unit(){
        return INSTANCE;
    }

    public static <T,R> Applicative<identity> applicative(){
        return INSTANCE;
    }

    public static <T,R> Monad<identity> monad(){
        return INSTANCE;
    }
    public static <T,R> Comonad<identity> comonad(){
        return INSTANCE;
    }
    public static <T,R> MonadRec<identity> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<identity> traverse(){
        return INSTANCE;
    }


    public static <T,R> Foldable<identity> foldable(){
        return INSTANCE;
    }


}
