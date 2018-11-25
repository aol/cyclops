package cyclops.instances.reactive.collections.mutable;

import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.reactive.collections.mutable.SetX;
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
import lombok.experimental.UtilityClass;
import lombok.experimental.Wither;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.oath.cyclops.data.ReactiveWitness.set;

@UtilityClass
public class SetXInstances {
  public static <W1,T> Nested<set,W1,T> nested(SetX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, SetXInstances.definitions(),def2);
  }
  public static  <W1,T> Product<set,W1,T> product(SetX<T> s, Active<W1,T> active){
    return Product.of(allTypeclasses(s),active);
  }
  public static  <W1,T> Coproduct<W1,set,T> coproduct(SetX<T> s, InstanceDefinitions<W1> def2){
    return Coproduct.right(s,def2, SetXInstances.definitions());
  }
  public static  <T>  Active<set,T> allTypeclasses(SetX<T> s){
    return Active.of(s, SetXInstances.definitions());
  }
  public static  <W2,R,T> Nested<set,W2,R> mapM(SetX<T> s,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(s.map(fn), SetXInstances.definitions(), defs);
  }
  public static  <T> Kleisli<set,SetX<T>,T> kindKleisli(){
    return Kleisli.of(SetXInstances.monad(), SetX::widen);
  }

  public static  <T> Cokleisli<set,T,SetX<T>> kindCokleisli(){
    return Cokleisli.of(SetX::narrowK);
  }

  public static InstanceDefinitions<set> definitions(){

    return new InstanceDefinitions<set>() {
      @Override
      public <T, R> Functor<set> functor() {
        return SetXInstances.functor();
      }

      @Override
      public <T> Pure<set> unit() {
        return SetXInstances.unit();
      }

      @Override
      public <T, R> Applicative<set> applicative() {
        return SetXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<set> monad() {
        return SetXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<set>> monadZero() {
        return Option.some(SetXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<set>> monadPlus() {
        return Option.some(SetXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<set> monadRec() {
        return SetXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<set>> monadPlus(MonoidK<set> m) {
        return Option.some(SetXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<set> traverse() {
        return  SetXInstances.traverse();
      }

      @Override
      public <T> Foldable<set> foldable() {
        return SetXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<set>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<set>> unfoldable() {
        return Option.some(SetXInstances.unfoldable());
      }
    };

  }

    public static Pure<set> unit() {
      return INSTANCE;
    }

    private final static SetXTypeClasses INSTANCE = new SetXTypeClasses();
    @AllArgsConstructor
    @Wither
    public static class SetXTypeClasses implements MonadPlus<set>,
                                                    MonadRec<set>,
                                                    TraverseByTraverse<set>,
                                                    Foldable<set>,
                                                    Unfoldable<set>{

        private final MonoidK<set> monoidK;
        public SetXTypeClasses(){
            monoidK = MonoidKs.setXConcat();
        }
        @Override
        public <T> Higher<set, T> filter(Predicate<? super T> predicate, Higher<set, T> ds) {
            return narrowK(ds).filter(predicate);
        }

        @Override
        public <T, R> Higher<set, Tuple2<T, R>> zip(Higher<set, T> fa, Higher<set, R> fb) {
            return narrowK(fa).zip(narrowK(fb));
        }

        @Override
        public <T1, T2, R> Higher<set, R> zip(Higher<set, T1> fa, Higher<set, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb),f);
        }

        @Override
        public <T> MonoidK<set> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<set, R> flatMap(Function<? super T, ? extends Higher<set, R>> fn, Higher<set, T> ds) {
            return narrowK(ds).concatMap(i->narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<set, R> ap(Higher<set, ? extends Function<T, R>> fn, Higher<set, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn),(a,b)->b.apply(a));
        }

        @Override
        public <T> Higher<set, T> unit(T value) {
            return SetX.of(value);
        }

        @Override
        public <T, R> Higher<set, R> map(Function<? super T, ? extends R> fn, Higher<set, T> ds) {
            return narrowK(ds).map(fn);
        }


        @Override
        public <T, R> Higher<set, R> tailRec(T initial, Function<? super T, ? extends Higher<set, ? extends Either<T, R>>> fn) {
            return SetX.tailRec(initial,i->narrowK(fn.apply(i)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<set, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<set, T> ds) {
            SetX<T> v = narrowK(ds);
            return v.<Higher<C2, Higher<set,R>>>foldLeft(ap.unit(SetX.<R>empty()),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<set, T> ds) {
            SetX<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
        }

        @Override
        public <T, R> Higher<set, Tuple2<T, Long>> zipWithIndex(Higher<set, T> ds) {
            return narrowK(ds).zipWithIndex();
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<set, T> ds) {
            return narrowK(ds).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<set, T> ds) {
            return narrowK(ds).foldLeft(monoid);
        }


        @Override
        public <R, T> Higher<set, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return SetX.unfold(b,fn);
        }


    }

    public static Unfoldable<set> unfoldable(){

        return INSTANCE;
    }

    public static MonadPlus<set> monadPlus(MonoidK<set> m){

        return INSTANCE.withMonoidK(m);
    }
    public static <T,R> Applicative<set> zippingApplicative(){
        return INSTANCE;
    }
    public static <T,R>Functor<set> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<set> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<set> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<set> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<set> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<set> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<set> foldable(){
        return INSTANCE;
    }



  public static <C2, T> Higher<C2, Higher<set, T>> widen2(Higher<C2, SetX<T>> flux) {
    // a functor could be used (if C2 is a functor / one exists for C2 type)
    // instead of casting
    // cast seems safer as Higher<set,T> must be a SetX
    return (Higher) flux;
  }




  public static <T> SetX<T> narrowK(final Higher<set, T> future) {
    return (SetX<T>) future;
  }


  public static <T> SetX<T> narrow(final Higher<set, T> completableSet) {

    return ((SetX<T>) completableSet);

  }
}
