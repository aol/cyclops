package cyclops.instances.reactive;

import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.react.ThreadPools;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.reactivestreams.Publisher;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.reactive.ReactiveSeq.narrowK;

//typeclass instances for reactive-streams publishers
//via ReactiveSeq Spouts (push based async reactive-streams)
public class PublisherInstances {
  public static <T> Higher<reactiveSeq,T> connectHKT(Publisher<T> p){
    return Spouts.from(p);
  }
  public static <T,R extends Publisher<T>> R convertHKT(Higher<reactiveSeq,T> hkt, Function<? super Publisher<T>,? extends R> fn){
    return fn.apply(Spouts.narrowK(hkt));
  }
  public static InstanceDefinitions<reactiveSeq> definitions(Executor ex){
    return new InstanceDefinitions<reactiveSeq>() {
      @Override
      public <T, R> Functor<reactiveSeq> functor() {
        return PublisherInstances.functor();
      }

      @Override
      public <T> Pure<reactiveSeq> unit() {
        return PublisherInstances.unit();
      }

      @Override
      public <T, R> Applicative<reactiveSeq> applicative() {
        return PublisherInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<reactiveSeq> monad() {
        return PublisherInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<reactiveSeq>> monadZero() {
        return Option.some(PublisherInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<reactiveSeq>> monadPlus() {
        return Option.some(PublisherInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<reactiveSeq> monadRec() {
        return PublisherInstances.monadRec(ex);
      }

      @Override
      public <T> Option<MonadPlus<reactiveSeq>> monadPlus(MonoidK<reactiveSeq> m) {
        return Option.some(PublisherInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<reactiveSeq> traverse() {
        return PublisherInstances.traverse();
      }

      @Override
      public <T> Foldable<reactiveSeq> foldable() {
        return PublisherInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<reactiveSeq>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<reactiveSeq>> unfoldable() {
        return Maybe.just(PublisherInstances.unfoldable(ex));
      }
    };
  }
  public static InstanceDefinitions<reactiveSeq> definitions(){
    return definitions(ThreadPools.getCurrentThreadExecutor());
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
            return Spouts.of(value);
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
            return v.<Higher<C2, Higher<reactiveSeq,R>>>foldLeft(ap.unit(Spouts.<R>empty()),
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
            return Spouts.unfold(b,fn);
        }


    }
  public static Unfoldable<reactiveSeq> unfoldable(Executor ex){
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

  public static <T> MonadPlus<reactiveSeq> monadPlus(MonoidK<reactiveSeq> m){
      return INSTANCE;
  }
  public static <T,R> MonadRec<reactiveSeq> monadRec(Executor ex){

    return new ReactiveSeqTypeClasses(){
      @Override
      public <T, R> Higher<reactiveSeq, R> tailRec(T initial, Function<? super T, ? extends Higher<reactiveSeq,? extends Either<T, R>>> fn) {
        return  Spouts.reactive(ReactiveSeq.deferFromStream( ()-> ReactiveSeq.tailRec(initial, fn.andThen(ReactiveSeq::narrowK))),ex);

      }
    };
  }

  public static <C2,T> Traverse<reactiveSeq> traverse(){
      return INSTANCE;
  }


  public static <T,R> Foldable<reactiveSeq> foldable(){
      return INSTANCE;
  }

    public static <C2, T> Higher<C2, Higher<reactiveSeq, T>> widen2(Higher<C2, ReactiveSeq<T>> flux) {
    // a functor could be used (if C2 is a functor / one exists for C2 type)
    // instead of casting
    // cast seems safer as Higher<reactiveSeq,T> must be a ReactiveSeq
    return (Higher) flux;
  }


  public static <T> ReactiveSeq<T> narrow(final Higher<reactiveSeq, T> completableList) {

    return ((ReactiveSeq<T>) completableList);

  }
}
