package cyclops.instances.reactive.collections.immutable;

import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.reactive.collections.immutable.LinkedListX;
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
import lombok.With;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.oath.cyclops.data.ReactiveWitness.linkedListX;
import static cyclops.reactive.collections.immutable.LinkedListX.narrowK;

/**
 * Companion class for creating Type Class instances for working with reactive LinkedListX's
 * @author johnmcclean
 *
 */
@UtilityClass
public class LinkedListXInstances {

  public static  <T> Kleisli<linkedListX,LinkedListX<T>,T> kindKleisli(){
    return Kleisli.of(LinkedListXInstances.monad(), LinkedListX::widen);
  }

  public static  <T> Cokleisli<linkedListX,T,LinkedListX<T>> kindCokleisli(){
    return Cokleisli.of(LinkedListX::narrowK);
  }
  public static <W1,T> Nested<linkedListX,W1,T> nested(LinkedListX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, LinkedListXInstances.definitions(),def2);
  }
  public static <W1,T> Product<linkedListX,W1,T> product(LinkedListX<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static <W1,T> Coproduct<W1,linkedListX,T> coproduct(LinkedListX<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, LinkedListXInstances.definitions());
  }
  public static <T> Active<linkedListX,T> allTypeclasses(LinkedListX<T> l){
    return Active.of(l, LinkedListXInstances.definitions());
  }
  public static <W2,R,T> Nested<linkedListX,W2,R> mapM(LinkedListX<T> l, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), LinkedListXInstances.definitions(), defs);
  }
  public static InstanceDefinitions<linkedListX> definitions(){
    return new InstanceDefinitions<linkedListX>() {
      @Override
      public <T, R> Functor<linkedListX> functor() {
        return LinkedListXInstances.functor();
      }

      @Override
      public <T> Pure<linkedListX> unit() {
        return LinkedListXInstances.unit();
      }

      @Override
      public <T, R> Applicative<linkedListX> applicative() {
        return LinkedListXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<linkedListX> monad() {
        return LinkedListXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<linkedListX>> monadZero() {
        return Option.some(LinkedListXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<linkedListX>> monadPlus() {
        return Option.some(LinkedListXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<linkedListX> monadRec() {
        return LinkedListXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<linkedListX>> monadPlus(MonoidK<linkedListX> m) {
        return Option.some(LinkedListXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<linkedListX> traverse() {
        return LinkedListXInstances.traverse();
      }

      @Override
      public <T> Foldable<linkedListX> foldable() {
        return LinkedListXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<linkedListX>> comonad() {
        return Option.none();
      }

      @Override
      public <T> Option<Unfoldable<linkedListX>> unfoldable() {
        return Option.some(LinkedListXInstances.unfoldable());
      }
    };
  }

    public static Pure<linkedListX> unit() {
        return INSTANCE;
    }

    private final static LinkedListXTypeClasses INSTANCE = new LinkedListXTypeClasses();
    @AllArgsConstructor
    @With
    public static class LinkedListXTypeClasses implements MonadPlus<linkedListX>,
        MonadRec<linkedListX>,
        TraverseByTraverse<linkedListX>,
        Foldable<linkedListX>,
        Unfoldable<linkedListX>{

        private final MonoidK<linkedListX> monoidK;
        public LinkedListXTypeClasses(){
            monoidK = MonoidKs.linkedListXConcat();
        }
        @Override
        public <T> Higher<linkedListX, T> filter(Predicate<? super T> predicate, Higher<linkedListX, T> ds) {
            return narrowK(ds).filter(predicate);
        }

        @Override
        public <T, R> Higher<linkedListX, Tuple2<T, R>> zip(Higher<linkedListX, T> fa, Higher<linkedListX, R> fb) {
            return narrowK(fa).zip(narrowK(fb));
        }

        @Override
        public <T1, T2, R> Higher<linkedListX, R> zip(Higher<linkedListX, T1> fa, Higher<linkedListX, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb),f);
        }

        @Override
        public <T> MonoidK<linkedListX> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<linkedListX, R> flatMap(Function<? super T, ? extends Higher<linkedListX, R>> fn, Higher<linkedListX, T> ds) {
            return narrowK(ds).concatMap(i->narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<linkedListX, R> ap(Higher<linkedListX, ? extends Function<T, R>> fn, Higher<linkedListX, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn),(a,b)->b.apply(a));
        }

        @Override
        public <T> Higher<linkedListX, T> unit(T value) {
            return LinkedListX.of(value);
        }

        @Override
        public <T, R> Higher<linkedListX, R> map(Function<? super T, ? extends R> fn, Higher<linkedListX, T> ds) {
            return narrowK(ds).map(fn);
        }


        @Override
        public <T, R> Higher<linkedListX, R> tailRec(T initial, Function<? super T, ? extends Higher<linkedListX, ? extends Either<T, R>>> fn) {
            return LinkedListX.tailRec(initial,i->narrowK(fn.apply(i)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<linkedListX, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<linkedListX, T> ds) {
            LinkedListX<T> v = narrowK(ds);
            return v.<Higher<C2, Higher<linkedListX,R>>>foldRight(ap.unit(LinkedListX.<R>empty()),
                (b,a) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<linkedListX, T> ds) {
            LinkedListX<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
        }

        @Override
        public <T, R> Higher<linkedListX, Tuple2<T, Long>> zipWithIndex(Higher<linkedListX, T> ds) {
            return narrowK(ds).zipWithIndex();
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<linkedListX, T> ds) {
            return narrowK(ds).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<linkedListX, T> ds) {
            return narrowK(ds).foldLeft(monoid);
        }


        @Override
        public <R, T> Higher<linkedListX, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return LinkedListX.unfold(b,fn);
        }


    }

    public static Unfoldable<linkedListX> unfoldable(){

        return INSTANCE;
    }

    public static MonadPlus<linkedListX> monadPlus(MonoidK<linkedListX> m){

        return INSTANCE.withMonoidK(m);
    }
    public static <T,R> Applicative<linkedListX> zippingApplicative(){
        return INSTANCE;
    }
    public static <T,R>Functor<linkedListX> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<linkedListX> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<linkedListX> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<linkedListX> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<linkedListX> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<linkedListX> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<linkedListX> foldable(){
        return INSTANCE;
    }






}
