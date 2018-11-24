package cyclops.instances.data;

import com.oath.cyclops.hkt.DataWitness.vector;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.LazySeq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.experimental.Wither;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static cyclops.data.Vector.narrowK;


/**
 * Companion class for creating Type Class instances for working with Vector's
 * @author johnmcclean
 *
 */
@UtilityClass
public class VectorInstances {

  public static  <T> Kleisli<vector,Vector<T>,T> kindKleisli(){
    return Kleisli.of(VectorInstances.monad(), Vector::widen);
  }

  public static  <T> Cokleisli<vector,T,Vector<T>> kindCokleisli(){
    return Cokleisli.of(Vector::narrowK);
  }
  public static <W1,T> Nested<vector,W1,T> nested(Vector<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, VectorInstances.definitions(),def2);
  }
  public static <W1,T> Product<vector,W1,T> product(Vector<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static <W1,T> Coproduct<W1,vector,T> coproduct(Vector<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, VectorInstances.definitions());
  }
  public static <T> Active<vector,T> allTypeclasses(Vector<T> l){
    return Active.of(l, VectorInstances.definitions());
  }
  public static <W2,R,T> Nested<vector,W2,R> mapM(Vector<T> l, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), VectorInstances.definitions(), defs);
  }

  public static InstanceDefinitions<vector> definitions(){
    return new InstanceDefinitions<vector>() {
      @Override
      public <T, R> Functor<vector> functor() {
        return INSTANCE;
      }

      @Override
      public <T> Pure<vector> unit() {
        return INSTANCE;
      }

      @Override
      public <T, R> Applicative<vector> applicative() {
        return INSTANCE;
      }

      @Override
      public <T, R> Monad<vector> monad() {
        return INSTANCE;
      }

      @Override
      public <T, R> Option<MonadZero<vector>> monadZero() {
        return Option.some(INSTANCE);
      }

      @Override
      public <T> Option<MonadPlus<vector>> monadPlus() {
        return Option.some(INSTANCE);
      }

      @Override
      public <T> MonadRec<vector> monadRec() {
        return INSTANCE;
      }

      @Override
      public <T> Option<MonadPlus<vector>> monadPlus(MonoidK<vector> m) {
        return Option.some(VectorInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<vector> traverse() {
        return INSTANCE;
      }

      @Override
      public <T> Foldable<vector> foldable() {
        return INSTANCE;
      }

      @Override
      public <T> Option<Comonad<vector>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<vector>> unfoldable() {
        return Option.some(INSTANCE);
      }
    };
  }




  private final static VectorTypeClasses INSTANCE = new VectorTypeClasses();
  @AllArgsConstructor
  @Wither
  public static class VectorTypeClasses implements MonadPlus<vector>,
                                                    MonadRec<vector>,
                                                    TraverseByTraverse<vector>,
                                                    Foldable<vector>,
                                                    Unfoldable<vector>{

      private final MonoidK<vector> monoidK;
      public VectorTypeClasses(){
          monoidK = MonoidKs.vectorConcat();
      }
      @Override
      public <T> Higher<vector, T> filter(Predicate<? super T> predicate, Higher<vector, T> ds) {
          return narrowK(ds).filter(predicate);
      }

      @Override
      public <T, R> Higher<vector, Tuple2<T, R>> zip(Higher<vector, T> fa, Higher<vector, R> fb) {
          return narrowK(fa).zip(narrowK(fb));
      }

      @Override
      public <T1, T2, R> Higher<vector, R> zip(Higher<vector, T1> fa, Higher<vector, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
          return narrowK(fa).zip(narrowK(fb),f);
      }

      @Override
      public <T> MonoidK<vector> monoid() {
          return monoidK;
      }

      @Override
      public <T, R> Higher<vector, R> flatMap(Function<? super T, ? extends Higher<vector, R>> fn, Higher<vector, T> ds) {
          return narrowK(ds).flatMap(i->narrowK(fn.apply(i)));
      }

      @Override
      public <T, R> Higher<vector, R> ap(Higher<vector, ? extends Function<T, R>> fn, Higher<vector, T> apply) {
          return narrowK(apply)
                            .zip(narrowK(fn),(a,b)->b.apply(a));
      }

      @Override
      public <T> Higher<vector, T> unit(T value) {
          return Vector.of(value);
      }

      @Override
      public <T, R> Higher<vector, R> map(Function<? super T, ? extends R> fn, Higher<vector, T> ds) {
          return narrowK(ds).map(fn);
      }


      @Override
      public <T, R> Higher<vector, R> tailRec(T initial, Function<? super T, ? extends Higher<vector, ? extends Either<T, R>>> fn) {
          return Vector.tailRec(initial,i->narrowK(fn.apply(i)));
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<vector, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<vector, T> ds) {
          Vector<T> v = narrowK(ds);
          return v.<Higher<C2, Higher<vector,R>>>foldLeft(ap.unit(Vector.<R>empty()),
              (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


     }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<vector, T> ds) {
          Vector<T> x = narrowK(ds);
          return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
      }

      @Override
      public <T, R> Higher<vector, Tuple2<T, Long>> zipWithIndex(Higher<vector, T> ds) {
          return narrowK(ds).zipWithIndex();
      }

      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<vector, T> ds) {
          return narrowK(ds).foldRight(monoid);
      }


      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<vector, T> ds) {
          return narrowK(ds).foldLeft(monoid);
      }


      @Override
      public <R, T> Higher<vector, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
          return Vector.unfold(b,fn);
      }


  }

    public static Unfoldable<vector> unfoldable(){

        return INSTANCE;
    }

  public static MonadPlus<vector> monadPlus(MonoidK<vector> m){

      return INSTANCE.withMonoidK(m);
  }
    public static <T,R> Applicative<vector> zippingApplicative(){
      return INSTANCE;
    }
    public static <T,R>Functor<vector> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<vector> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<vector> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<vector> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<vector> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<vector> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<vector> foldable(){
      return INSTANCE;
    }



}
