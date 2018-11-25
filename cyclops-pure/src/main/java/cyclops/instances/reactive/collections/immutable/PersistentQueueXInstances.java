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
import cyclops.reactive.collections.immutable.PersistentQueueX;
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

import static com.oath.cyclops.data.ReactiveWitness.persistentQueueX;
import static cyclops.reactive.collections.immutable.PersistentQueueX.narrowK;

/**
 * Companion class for creating Type Class instances for working with PersistentQueues
 * @author johnmcclean
 *
 */
@UtilityClass
public class PersistentQueueXInstances {

  public static  <T> Cokleisli<persistentQueueX,T,PersistentQueueX<T>> kindCokleisli(){
    return Cokleisli.of(PersistentQueueX::narrowK);
  }
  public static  <T> Kleisli<persistentQueueX,PersistentQueueX<T>,T> kindKleisli(){
    return Kleisli.of(PersistentQueueXInstances.monad(), PersistentQueueX::widen);
  }
  public static <W1,T> Nested<persistentQueueX,W1,T> nested(PersistentQueueX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, PersistentQueueXInstances.definitions(),def2);
  }

  public static <W1,T> Product< persistentQueueX,W1,T> product(PersistentQueueX<T> q, Active<W1,T> active){
    return Product.of(allTypeclasses(q),active);
  }
  public static <W1,T> Coproduct<W1, persistentQueueX,T> coproduct(PersistentQueueX<T> q, InstanceDefinitions<W1> def2){
    return Coproduct.right(q,def2, PersistentQueueXInstances.definitions());
  }
  public static <T> Active<persistentQueueX,T> allTypeclasses(PersistentQueueX<T> q){
    return Active.of(q, PersistentQueueXInstances.definitions());
  }
  public static <W2,R,T> Nested< persistentQueueX,W2,R> mapM(PersistentQueueX<T> q,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(q.map(fn), PersistentQueueXInstances.definitions(), defs);
  }

  public static InstanceDefinitions<persistentQueueX> definitions(){

    return new InstanceDefinitions<persistentQueueX>() {
      @Override
      public <T, R> Functor<persistentQueueX> functor() {
        return PersistentQueueXInstances.functor();
      }

      @Override
      public <T> Pure<persistentQueueX> unit() {
        return PersistentQueueXInstances.unit();
      }

      @Override
      public <T, R> Applicative<persistentQueueX> applicative() {
        return PersistentQueueXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<persistentQueueX> monad() {
        return PersistentQueueXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<persistentQueueX>> monadZero() {
        return Option.some(PersistentQueueXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<persistentQueueX>> monadPlus() {
        return Option.some(PersistentQueueXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<persistentQueueX> monadRec() {
        return PersistentQueueXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<persistentQueueX>> monadPlus(MonoidK<persistentQueueX> m) {
        return Option.some(PersistentQueueXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<persistentQueueX> traverse() {
        return PersistentQueueXInstances.traverse();
      }

      @Override
      public <T> Foldable<persistentQueueX> foldable() {
        return PersistentQueueXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<persistentQueueX>> comonad() {
        return Option.none();
      }
      @Override
      public <T> Option<Unfoldable<persistentQueueX>> unfoldable() {
        return Option.some(PersistentQueueXInstances.unfoldable());
      }
    };
  }

    public static Pure<persistentQueueX> unit() {
      return INSTANCE;
    }

    private final static PersistentQueueXTypeClasses INSTANCE = new PersistentQueueXTypeClasses();
    @AllArgsConstructor
    @Wither
    public static class PersistentQueueXTypeClasses implements MonadPlus<persistentQueueX>,
                                                                MonadRec<persistentQueueX>,
                                                                TraverseByTraverse<persistentQueueX>,
                                                                Foldable<persistentQueueX>,
                                                                Unfoldable<persistentQueueX>{

        private final MonoidK<persistentQueueX> monoidK;
        public PersistentQueueXTypeClasses(){
            monoidK = MonoidKs.persistentQueueXConcat();
        }
        @Override
        public <T> Higher<persistentQueueX, T> filter(Predicate<? super T> predicate, Higher<persistentQueueX, T> ds) {
            return narrowK(ds).filter(predicate);
        }

        @Override
        public <T, R> Higher<persistentQueueX, Tuple2<T, R>> zip(Higher<persistentQueueX, T> fa, Higher<persistentQueueX, R> fb) {
            return narrowK(fa).zip(narrowK(fb));
        }

        @Override
        public <T1, T2, R> Higher<persistentQueueX, R> zip(Higher<persistentQueueX, T1> fa, Higher<persistentQueueX, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb),f);
        }

        @Override
        public <T> MonoidK<persistentQueueX> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<persistentQueueX, R> flatMap(Function<? super T, ? extends Higher<persistentQueueX, R>> fn, Higher<persistentQueueX, T> ds) {
            return narrowK(ds).concatMap(i->narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<persistentQueueX, R> ap(Higher<persistentQueueX, ? extends Function<T, R>> fn, Higher<persistentQueueX, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn),(a,b)->b.apply(a));
        }

        @Override
        public <T> Higher<persistentQueueX, T> unit(T value) {
            return PersistentQueueX.of(value);
        }

        @Override
        public <T, R> Higher<persistentQueueX, R> map(Function<? super T, ? extends R> fn, Higher<persistentQueueX, T> ds) {
            return narrowK(ds).map(fn);
        }


        @Override
        public <T, R> Higher<persistentQueueX, R> tailRec(T initial, Function<? super T, ? extends Higher<persistentQueueX, ? extends Either<T, R>>> fn) {
            return PersistentQueueX.tailRec(initial,i->narrowK(fn.apply(i)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<persistentQueueX, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<persistentQueueX, T> ds) {
            PersistentQueueX<T> v = narrowK(ds);
            return v.<Higher<C2, Higher<persistentQueueX,R>>>foldLeft(ap.unit(PersistentQueueX.<R>empty()),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<persistentQueueX, T> ds) {
            PersistentQueueX<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
        }

        @Override
        public <T, R> Higher<persistentQueueX, Tuple2<T, Long>> zipWithIndex(Higher<persistentQueueX, T> ds) {
            return narrowK(ds).zipWithIndex();
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<persistentQueueX, T> ds) {
            return narrowK(ds).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<persistentQueueX, T> ds) {
            return narrowK(ds).foldLeft(monoid);
        }


        @Override
        public <R, T> Higher<persistentQueueX, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return PersistentQueueX.unfold(b,fn);
        }


    }

    public static Unfoldable<persistentQueueX> unfoldable(){

        return INSTANCE;
    }

    public static MonadPlus<persistentQueueX> monadPlus(MonoidK<persistentQueueX> m){

        return INSTANCE.withMonoidK(m);
    }
    public static <T,R> Applicative<persistentQueueX> zippingApplicative(){
        return INSTANCE;
    }
    public static <T,R>Functor<persistentQueueX> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<persistentQueueX> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<persistentQueueX> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<persistentQueueX> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<persistentQueueX> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<persistentQueueX> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<persistentQueueX> foldable(){
        return INSTANCE;
    }
   
}
