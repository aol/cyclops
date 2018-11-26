package cyclops.instances.data;

import com.oath.cyclops.hkt.DataWitness.seq;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.Seq;
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

import static cyclops.data.Seq.narrowK;



@UtilityClass
public class SeqInstances {

  public static  <T> Kleisli<seq,Seq<T>,T> kindKleisli(){
    return Kleisli.of(SeqInstances.monad(), Seq::widen);
  }

  public static  <T> Cokleisli<seq,T,Seq<T>> kindCokleisli(){
    return Cokleisli.of(Seq::narrowK);
  }
  public static <W1,T> Nested<seq,W1,T> nested(Seq<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, SeqInstances.definitions(),def2);
  }
  public static <W1,T> Product<seq,W1,T> product(Seq<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static <W1,T> Coproduct<W1,seq,T> coproduct(Seq<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, SeqInstances.definitions());
  }
  public static <T> Active<seq,T> allTypeclasses(Seq<T> l){
    return Active.of(l, SeqInstances.definitions());
  }
  public static <W2,R,T> Nested<seq,W2,R> mapM(Seq<T> l, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), SeqInstances.definitions(), defs);
  }

  public static InstanceDefinitions<seq> definitions(){
    return new InstanceDefinitions<seq>() {
      @Override
      public <T, R> Functor<seq> functor() {
        return INSTANCE;
      }

      @Override
      public <T> Pure<seq> unit() {
        return INSTANCE;
      }

      @Override
      public <T, R> Applicative<seq> applicative() {
        return INSTANCE;
      }

      @Override
      public <T, R> Monad<seq> monad() {
        return INSTANCE;
      }

      @Override
      public <T, R> Option<MonadZero<seq>> monadZero() {
        return Option.some(INSTANCE);
      }

      @Override
      public <T> Option<MonadPlus<seq>> monadPlus() {
        return Option.some(INSTANCE);
      }

      @Override
      public <T> MonadRec<seq> monadRec() {
        return INSTANCE;
      }

      @Override
      public <T> Option<MonadPlus<seq>> monadPlus(MonoidK<seq> m) {
        return Option.some(SeqInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<seq> traverse() {
        return INSTANCE;
      }

      @Override
      public <T> Foldable<seq> foldable() {
        return INSTANCE;
      }

      @Override
      public <T> Option<Comonad<seq>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<seq>> unfoldable() {
        return Option.some(INSTANCE);
      }
    };
  }




  private final static SeqTypeClasses INSTANCE = new SeqTypeClasses();
  @AllArgsConstructor
  @Wither
  public static class SeqTypeClasses implements MonadPlus<seq>,
                                                    MonadRec<seq>,
                                                    TraverseByTraverse<seq>,
                                                    Foldable<seq>,
                                                    Unfoldable<seq>{

      private final MonoidK<seq> monoidK;
      public SeqTypeClasses(){
          monoidK = MonoidKs.seqConcat();
      }
      @Override
      public <T> Higher<seq, T> filter(Predicate<? super T> predicate, Higher<seq, T> ds) {
          return narrowK(ds).filter(predicate);
      }

      @Override
      public <T, R> Higher<seq, Tuple2<T, R>> zip(Higher<seq, T> fa, Higher<seq, R> fb) {
          return narrowK(fa).zip(narrowK(fb));
      }

      @Override
      public <T1, T2, R> Higher<seq, R> zip(Higher<seq, T1> fa, Higher<seq, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
          return narrowK(fa).zip(narrowK(fb),f);
      }

      @Override
      public <T> MonoidK<seq> monoid() {
          return monoidK;
      }

      @Override
      public <T, R> Higher<seq, R> flatMap(Function<? super T, ? extends Higher<seq, R>> fn, Higher<seq, T> ds) {
          return narrowK(ds).flatMap(i->narrowK(fn.apply(i)));
      }

      @Override
      public <T, R> Higher<seq, R> ap(Higher<seq, ? extends Function<T, R>> fn, Higher<seq, T> apply) {
          return narrowK(apply)
                            .zip(narrowK(fn),(a,b)->b.apply(a));
      }

      @Override
      public <T> Higher<seq, T> unit(T value) {
          return Seq.of(value);
      }

      @Override
      public <T, R> Higher<seq, R> map(Function<? super T, ? extends R> fn, Higher<seq, T> ds) {
          return narrowK(ds).map(fn);
      }

      @Override
      public <T, R> Higher<seq, R> tailRec(T initial, Function<? super T, ? extends Higher<seq, ? extends Either<T, R>>> fn) {
          return Seq.tailRec(initial,i->narrowK(fn.apply(i)));
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<seq, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<seq, T> ds) {
          Seq<T> v = narrowK(ds);
          Higher<C2, Higher<seq, R>> res = v.<Higher<C2, Higher<seq, R>>>foldLeft(ap.unit(Seq.<R>empty()),
              (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


          return ap.map_(res,seq->narrowK(seq).reverse());
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<seq, T> ds) {
          Seq<T> x = narrowK(ds);
          return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
      }

      @Override
      public <T, R> Higher<seq, Tuple2<T, Long>> zipWithIndex(Higher<seq, T> ds) {
          return narrowK(ds).zipWithIndex();
      }

      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<seq, T> ds) {
          return narrowK(ds).foldRight(monoid);
      }


      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<seq, T> ds) {
          return narrowK(ds).foldLeft(monoid);
      }


      @Override
      public <R, T> Higher<seq, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
          return Seq.unfold(b,fn);
      }
  }



  public static MonadPlus<seq> monadPlus(MonoidK<seq> m){

      return INSTANCE.withMonoidK(m);
  }
    public static <T,R> Applicative<seq> zippingApplicative(){
      return INSTANCE;
    }
    public static <T,R>Functor<seq> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<seq> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<seq> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<seq> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<seq> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<seq> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<seq> foldable(){
      return INSTANCE;
    }
    public static Unfoldable<seq> unfoldable(){
        return INSTANCE;
    }


}
