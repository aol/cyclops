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
import cyclops.reactive.collections.mutable.ListX;
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

import static com.oath.cyclops.data.ReactiveWitness.list;

@UtilityClass
public class ListXInstances {
  public static  <T> Kleisli<list,ListX<T>,T> kindKleisli(){
    return Kleisli.of(ListXInstances.monad(), ListXInstances::widen);
  }
  public static  <T> Cokleisli<list,T,ListX<T>> kindCokleisli(){
    return Cokleisli.of(ListXInstances::narrowK);
  }
  public static <W1,T> Nested<list,W1,T> nested(ListX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, ListXInstances.definitions(),def2);
  }
  public static  <W1,T> Product<list,W1,T> product(ListX<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static  <W1,T> Coproduct<W1,list,T> coproduct(ListX<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, ListXInstances.definitions());
  }
  public static  <T> Active<list,T> allTypeclasses(ListX<T> l){
    return Active.of(l, ListXInstances.definitions());
  }
  public static  <W2,R,T> Nested<list,W2,R> mapM(ListX<T> l,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), ListXInstances.definitions(), defs);
  }
  public static InstanceDefinitions<list> definitions(){
    return new InstanceDefinitions<list>() {
      @Override
      public <T, R> Functor<list> functor() {
        return ListXInstances.functor();
      }

      @Override
      public <T> Pure<list> unit() {
        return ListXInstances.unit();
      }

      @Override
      public <T, R> Applicative<list> applicative() {
        return ListXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<list> monad() {
        return ListXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<list>> monadZero() {
        return Option.some(ListXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<list>> monadPlus() {
        return Option.some(ListXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<list> monadRec() {
        return ListXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<list>> monadPlus(MonoidK<list> m) {
        MonadPlus<list> x = ListXInstances.monadPlus(m);
        return Maybe.just(x);
      }

      @Override
      public <C2, T> Traverse<list> traverse() {
        return ListXInstances.traverse();
      }

      @Override
      public <T> Foldable<list> foldable() {
        return ListXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<list>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<list>> unfoldable() {
        return Option.some(ListXInstances.unfoldable());
      }
    };

  }

    public static Pure<list> unit() {
      return INSTANCE;
    }

    private final static ListXTypeClasses INSTANCE = new ListXTypeClasses();
    @AllArgsConstructor
    @Wither
    public static class ListXTypeClasses implements MonadPlus<list>,
        MonadRec<list>,
        TraverseByTraverse<list>,
        Foldable<list>,
        Unfoldable<list>{

        private final MonoidK<list> monoidK;
        public ListXTypeClasses(){
            monoidK = MonoidKs.listXConcat();
        }
        @Override
        public <T> Higher<list, T> filter(Predicate<? super T> predicate, Higher<list, T> ds) {
            return narrowK(ds).filter(predicate);
        }

        @Override
        public <T, R> Higher<list, Tuple2<T, R>> zip(Higher<list, T> fa, Higher<list, R> fb) {
            return narrowK(fa).zip(narrowK(fb));
        }

        @Override
        public <T1, T2, R> Higher<list, R> zip(Higher<list, T1> fa, Higher<list, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb),f);
        }

        @Override
        public <T> MonoidK<list> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<list, R> flatMap(Function<? super T, ? extends Higher<list, R>> fn, Higher<list, T> ds) {
            return narrowK(ds).concatMap(i->narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<list, R> ap(Higher<list, ? extends Function<T, R>> fn, Higher<list, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn),(a,b)->b.apply(a));
        }

        @Override
        public <T> Higher<list, T> unit(T value) {
            return ListX.of(value);
        }

        @Override
        public <T, R> Higher<list, R> map(Function<? super T, ? extends R> fn, Higher<list, T> ds) {
            return narrowK(ds).map(fn);
        }


        @Override
        public <T, R> Higher<list, R> tailRec(T initial, Function<? super T, ? extends Higher<list, ? extends Either<T, R>>> fn) {
            return ListX.tailRec(initial,i->narrowK(fn.apply(i)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<list, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<list, T> ds) {
            ListX<T> v = narrowK(ds);
            return v.<Higher<C2, Higher<list,R>>>foldLeft(ap.unit(ListX.<R>empty()),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<list, T> ds) {
            ListX<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
        }

        @Override
        public <T, R> Higher<list, Tuple2<T, Long>> zipWithIndex(Higher<list, T> ds) {
            return narrowK(ds).zipWithIndex();
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<list, T> ds) {
            return narrowK(ds).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<list, T> ds) {
            return narrowK(ds).foldLeft(monoid);
        }


        @Override
        public <R, T> Higher<list, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return ListX.unfold(b,fn);
        }


    }

    public static Unfoldable<list> unfoldable(){

        return INSTANCE;
    }

    public static MonadPlus<list> monadPlus(MonoidK<list> m){

        return INSTANCE.withMonoidK(m);
    }
    public static <T,R> Applicative<list> zippingApplicative(){
        return INSTANCE;
    }
    public static <T,R>Functor<list> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<list> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<list> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<list> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<list> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<list> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<list> foldable(){
        return INSTANCE;
    }





  public static <C2, T> Higher<C2, Higher<list, T>> widen2(Higher<C2, ListX<T>> flux) {
    // a functor could be used (if C2 is a functor / one exists for C2 type)
    // instead of casting
    // cast seems safer as Higher<list,T> must be a ListX
    return (Higher) flux;
  }
  public static <T> Higher<list, T> widen(ListX<T> flux) {

    return flux;
  }




  public static <T> ListX<T> narrowK(final Higher<list, T> future) {
    return (ListX<T>) future;
  }



  public static <T> ListX<T> narrow(final Higher<list, T> completableList) {

    return ((ListX<T>) completableList);//.narrow();

  }
}
