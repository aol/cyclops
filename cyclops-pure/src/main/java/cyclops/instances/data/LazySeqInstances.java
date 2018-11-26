package cyclops.instances.data;

import com.oath.cyclops.hkt.DataWitness.lazySeq;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.LazySeq;
import cyclops.data.tuple.Tuple2;
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
import lombok.experimental.UtilityClass;
import lombok.experimental.Wither;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.data.LazySeq.narrowK;


@UtilityClass
public class LazySeqInstances {

  public static  <T> Kleisli<lazySeq,LazySeq<T>,T> kindKleisli(){
    return Kleisli.of(LazySeqInstances.monad(), LazySeq::widen);
  }

  public static  <T> Cokleisli<lazySeq,T,LazySeq<T>> kindCokleisli(){
    return Cokleisli.of(LazySeq::narrowK);
  }
  public static <W1,T> Nested<lazySeq,W1,T> nested(LazySeq<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, LazySeqInstances.definitions(),def2);
  }
  public static <W1,T> Product<lazySeq,W1,T> product(LazySeq<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static <W1,T> Coproduct<W1,lazySeq,T> coproduct(LazySeq<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, LazySeqInstances.definitions());
  }
  public static <T> Active<lazySeq,T> allTypeclasses(LazySeq<T> l){
    return Active.of(l, LazySeqInstances.definitions());
  }
  public static <W2,R,T> Nested<lazySeq,W2,R> mapM(LazySeq<T> l, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), LazySeqInstances.definitions(), defs);
  }

  public static InstanceDefinitions<lazySeq> definitions(){
    return new InstanceDefinitions<lazySeq>() {
      @Override
      public <T, R> Functor<lazySeq> functor() {
        return INSTANCE;
      }

      @Override
      public <T> Pure<lazySeq> unit() {
        return INSTANCE;
      }

      @Override
      public <T, R> Applicative<lazySeq> applicative() {
        return INSTANCE;
      }

      @Override
      public <T, R> Monad<lazySeq> monad() {
        return INSTANCE;
      }

      @Override
      public <T, R> Option<MonadZero<lazySeq>> monadZero() {
        return Option.some(INSTANCE);
      }

      @Override
      public <T> Option<MonadPlus<lazySeq>> monadPlus() {
        return Option.some(INSTANCE);
      }

      @Override
      public <T> MonadRec<lazySeq> monadRec() {
        return INSTANCE;
      }

      @Override
      public <T> Option<MonadPlus<lazySeq>> monadPlus(MonoidK<lazySeq> m) {
        return Option.some(LazySeqInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<lazySeq> traverse() {
        return INSTANCE;
      }

      @Override
      public <T> Foldable<lazySeq> foldable() {
        return INSTANCE;
      }

      @Override
      public <T> Option<Comonad<lazySeq>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<lazySeq>> unfoldable() {
        return Option.some(INSTANCE);
      }
    };
  }




  private final static LazySeqTypeClasses INSTANCE = new LazySeqTypeClasses();
  @AllArgsConstructor
  @Wither
  public static class LazySeqTypeClasses implements MonadPlus<lazySeq>,
                                                    MonadRec<lazySeq>,
                                                    TraverseByTraverse<lazySeq>,
                                                    Foldable<lazySeq>,
                                                    Unfoldable<lazySeq>{

      private final MonoidK<lazySeq> monoidK;
      public LazySeqTypeClasses(){
          monoidK = MonoidKs.lazySeqConcat();
      }
      @Override
      public <T> Higher<lazySeq, T> filter(Predicate<? super T> predicate, Higher<lazySeq, T> ds) {
          return narrowK(ds).filter(predicate);
      }

      @Override
      public <T, R> Higher<lazySeq, Tuple2<T, R>> zip(Higher<lazySeq, T> fa, Higher<lazySeq, R> fb) {
          return narrowK(fa).zip(narrowK(fb));
      }

      @Override
      public <T1, T2, R> Higher<lazySeq, R> zip(Higher<lazySeq, T1> fa, Higher<lazySeq, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
          return narrowK(fa).zip(narrowK(fb),f);
      }

      @Override
      public <T> MonoidK<lazySeq> monoid() {
          return monoidK;
      }

      @Override
      public <T, R> Higher<lazySeq, R> flatMap(Function<? super T, ? extends Higher<lazySeq, R>> fn, Higher<lazySeq, T> ds) {
          return narrowK(ds).flatMap(i->narrowK(fn.apply(i)));
      }

      @Override
      public <T, R> Higher<lazySeq, R> ap(Higher<lazySeq, ? extends Function<T, R>> fn, Higher<lazySeq, T> apply) {
          return narrowK(apply)
                            .zip(narrowK(fn),(a,b)->b.apply(a));
      }

      @Override
      public <T> Higher<lazySeq, T> unit(T value) {
          return LazySeq.of(value);
      }

      @Override
      public <T, R> Higher<lazySeq, R> map(Function<? super T, ? extends R> fn, Higher<lazySeq, T> ds) {
          return narrowK(ds).map(fn);
      }

      @Override
      public <T, R> Higher<lazySeq, R> tailRec(T initial, Function<? super T, ? extends Higher<lazySeq, ? extends Either<T, R>>> fn) {
          return LazySeq.tailRec(initial,i->narrowK(fn.apply(i)));
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<lazySeq, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<lazySeq, T> ds) {
          LazySeq<T> v = narrowK(ds);

          return v.<Higher<C2, Higher<lazySeq,R>>>lazyFoldRight(ap.unit(LazySeq.<R>empty()),
              (b, a) ->  ap.zip(fn.apply(b), a.get(), (sn, vec) -> narrowK(vec).plus(sn)));

      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<lazySeq, T> ds) {
          LazySeq<T> x = narrowK(ds);
          return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
      }

      @Override
      public <T, R> Higher<lazySeq, Tuple2<T, Long>> zipWithIndex(Higher<lazySeq, T> ds) {
          return narrowK(ds).zipWithIndex();
      }

      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<lazySeq, T> ds) {
          return narrowK(ds).foldRight(monoid);
      }


      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<lazySeq, T> ds) {
          return narrowK(ds).foldLeft(monoid);
      }


      @Override
      public <R, T> Higher<lazySeq, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
          return LazySeq.unfold(b,fn);
      }
  }



  public static MonadPlus<lazySeq> monadPlus(MonoidK<lazySeq> m){

      return INSTANCE.withMonoidK(m);
  }
    public static <T,R> Applicative<lazySeq> zippingApplicative(){
      return INSTANCE;
    }
    public static <T,R>Functor<lazySeq> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<lazySeq> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<lazySeq> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<lazySeq> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<lazySeq> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<lazySeq> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<lazySeq> foldable(){
      return INSTANCE;
    }
    public static Unfoldable<lazySeq> unfoldable(){
        return INSTANCE;
    }


}
