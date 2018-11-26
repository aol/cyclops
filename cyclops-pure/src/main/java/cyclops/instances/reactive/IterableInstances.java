package cyclops.instances.reactive;

import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.react.ThreadPools;
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
import cyclops.reactive.ReactiveSeq;
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
import lombok.experimental.Wither;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.reactive.ReactiveSeq.narrowK;

//typeclass instances for iterable sequences
//via ReactiveSeq's iterable factory methods (pull based synchronous stream)
public class IterableInstances {

  public static <T> Higher<reactiveSeq,T> connectHKT(Iterable<T> p){
    return ReactiveSeq.fromIterable(p);
  }
  public static <T,R extends Iterable<T>> R convertHKT(Higher<reactiveSeq,T> hkt, Function<? super Iterable<T>,? extends R> fn){
    return fn.apply(narrowK(hkt));
  }
  public static  <T> Kleisli<reactiveSeq,ReactiveSeq<T>,T> kindKleisli(){
    return Kleisli.of(IterableInstances.monad(), ReactiveSeq::widen);
  }

  public static  <T> Cokleisli<reactiveSeq,T,ReactiveSeq<T>> kindCokleisli(){
    return Cokleisli.of(ReactiveSeq::narrowK);
  }

  public static <W1,T> Nested<reactiveSeq,W1,T> nested(ReactiveSeq<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, IterableInstances.definitions(),def2);
  }
  public static  <W1,T> Product<reactiveSeq,W1,T> product(Iterable<T> it, Active<W1,T> active){
    return Product.of(allTypeclasses(it),active);
  }
  public static  <W1,T> Product<reactiveSeq,W1,T> product(Iterable<T> it,Active<W1,T> active, Executor ex){
    return Product.of(allTypeclasses(it,ex),active);
  }
  public static  <W1,T> Coproduct<W1,reactiveSeq,T> coproduct(Iterable<T> it, InstanceDefinitions<W1> def2){
    ReactiveSeq<T> r = ReactiveSeq.fromIterable(it);
    return Coproduct.right(r,def2, IterableInstances.definitions());
  }
  public static  <T> Active<reactiveSeq,T> allTypeclasses(Iterable<T> it,Executor ex){
    ReactiveSeq<T> r = ReactiveSeq.fromIterable(it);
    return Active.of(r, r.fold(sync-> IterableInstances.definitions(), rs-> PublisherInstances.definitions(ex), ac-> PublisherInstances.definitions(ex)));
  }
  public static  <T> Active<reactiveSeq,T> allTypeclasses(Iterable<T> it){
    ReactiveSeq<T> r = ReactiveSeq.fromIterable(it);
    return Active.of(r, r.fold(sync-> IterableInstances.definitions(), rs-> PublisherInstances.definitions(ThreadPools.getCurrentThreadExecutor()), ac-> PublisherInstances.definitions(ThreadPools.getCurrentThreadExecutor())));
  }
  public static  <W2,R,T> Nested<reactiveSeq,W2,R> mapM(Iterable<T> it,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    ReactiveSeq<T> r = ReactiveSeq.fromIterable(it);
    return Nested.of(r.map(fn), IterableInstances.definitions(), defs);
  }

  public static InstanceDefinitions<reactiveSeq> definitions(){
    return new InstanceDefinitions<reactiveSeq>() {
      @Override
      public <T, R> Functor<reactiveSeq> functor() {
        return IterableInstances.functor();
      }

      @Override
      public <T> Pure<reactiveSeq> unit() {
        return IterableInstances.unit();
      }

      @Override
      public <T, R> Applicative<reactiveSeq> applicative() {
        return IterableInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<reactiveSeq> monad() {
        return IterableInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<reactiveSeq>> monadZero() {
        return Option.some(IterableInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<reactiveSeq>> monadPlus() {
        return Option.some(IterableInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<reactiveSeq> monadRec() {
        return IterableInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<reactiveSeq>> monadPlus(MonoidK<reactiveSeq> m) {
        return Option.some(IterableInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<reactiveSeq> traverse() {
        return  IterableInstances.traverse();
      }

      @Override
      public <T> Foldable<reactiveSeq> foldable() {
        return IterableInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<reactiveSeq>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<reactiveSeq>> unfoldable() {
        return Option.some(IterableInstances.unfoldable());
      }
    };
  }

    private final static ReactiveSeqTypeClasses INSTANCE = new ReactiveSeqTypeClasses();
    @AllArgsConstructor
    @Wither
    public static class ReactiveSeqTypeClasses implements MonadPlus<reactiveSeq>,
                                                            MonadRec<reactiveSeq>,
                                                            TraverseByTraverse<reactiveSeq>,
                                                            Foldable<reactiveSeq>,
                                                            Unfoldable<reactiveSeq>{

        private final MonoidK<reactiveSeq> monoidK;
        public ReactiveSeqTypeClasses(){
            monoidK = MonoidKs.combineReactiveSeq();
        }
        @Override
        public <T> Higher<reactiveSeq, T> filter(Predicate<? super T> predicate, Higher<reactiveSeq, T> ds) {
            return narrowK(ds).filter(predicate);
        }

        @Override
        public <T, R> Higher<reactiveSeq, Tuple2<T, R>> zip(Higher<reactiveSeq, T> fa, Higher<reactiveSeq, R> fb) {
            return narrowK(fa).zip(narrowK(fb));
        }

        @Override
        public <T1, T2, R> Higher<reactiveSeq, R> zip(Higher<reactiveSeq, T1> fa, Higher<reactiveSeq, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb),f);
        }

        @Override
        public <T> MonoidK<reactiveSeq> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<reactiveSeq, R> flatMap(Function<? super T, ? extends Higher<reactiveSeq, R>> fn, Higher<reactiveSeq, T> ds) {
            return narrowK(ds).flatMap(i->narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<reactiveSeq, R> ap(Higher<reactiveSeq, ? extends Function<T, R>> fn, Higher<reactiveSeq, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn),(a,b)->b.apply(a));
        }

        @Override
        public <T> Higher<reactiveSeq, T> unit(T value) {
            return ReactiveSeq.of(value);
        }

        @Override
        public <T, R> Higher<reactiveSeq, R> map(Function<? super T, ? extends R> fn, Higher<reactiveSeq, T> ds) {
            return narrowK(ds).map(fn);
        }


        @Override
        public <T, R> Higher<reactiveSeq, R> tailRec(T initial, Function<? super T, ? extends Higher<reactiveSeq, ? extends Either<T, R>>> fn) {
            return ReactiveSeq.tailRec(initial,i->narrowK(fn.apply(i)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<reactiveSeq, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<reactiveSeq, T> ds) {
            ReactiveSeq<T> v = narrowK(ds);
            return v.<Higher<C2, Higher<reactiveSeq,R>>>foldLeft(ap.unit(ReactiveSeq.<R>empty()),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<reactiveSeq, T> ds) {
            ReactiveSeq<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
        }

        @Override
        public <T, R> Higher<reactiveSeq, Tuple2<T, Long>> zipWithIndex(Higher<reactiveSeq, T> ds) {
            return narrowK(ds).zipWithIndex();
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<reactiveSeq, T> ds) {
            return narrowK(ds).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<reactiveSeq, T> ds) {
            return narrowK(ds).foldLeft(monoid);
        }


        @Override
        public <R, T> Higher<reactiveSeq, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return ReactiveSeq.unfold(b,fn);
        }


    }
  public static Unfoldable<reactiveSeq> unfoldable(){
    return INSTANCE;
  }

  public static <T,R>Functor<reactiveSeq> functor(){
      return INSTANCE;
  }

  public static <T> Pure<reactiveSeq> unit(){
    return INSTANCE;
  }

  public static <T,R> Applicative<reactiveSeq> zippingApplicative(){
      return INSTANCE;
  }

  public static <T,R> Monad<reactiveSeq> monad(){
        return INSTANCE;
  }

  public static <T,R> MonadZero<reactiveSeq> monadZero(){

      return INSTANCE;
  }

  public static <T> MonadPlus<reactiveSeq> monadPlus(){

      return INSTANCE;
  }
  public static <T,R> MonadRec<reactiveSeq> monadRec(){

      return INSTANCE;
  }

  public static <T> MonadPlus<reactiveSeq> monadPlus(MonoidK<reactiveSeq> m){
      return INSTANCE.withMonoidK(m);

  }

  public static <C2,T> Traverse<reactiveSeq> traverse(){
      return INSTANCE;
  }


  public static <T,R> Foldable<reactiveSeq> foldable(){
      return INSTANCE;
  }


  public static <T>  Higher<reactiveSeq, T> widen(ReactiveSeq<T> stream) {
    return stream;
  }


  public static <C2, T> Higher<C2, Higher<reactiveSeq, T>> widen2(Higher<C2, ReactiveSeq<T>> flux) {
    return (Higher) flux;
  }



  public static <T> ReactiveSeq<T> narrow(final Higher<reactiveSeq, T> completableList) {

    return ((ReactiveSeq<T>) completableList);

  }
}
