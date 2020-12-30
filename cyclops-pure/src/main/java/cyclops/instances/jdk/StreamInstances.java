package cyclops.instances.jdk;

import com.oath.cyclops.hkt.DataWitness.stream;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.companion.Streams;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.kinds.StreamKind;
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
import lombok.experimental.UtilityClass;
import lombok.With;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static cyclops.kinds.StreamKind.narrowK;
import static cyclops.kinds.StreamKind.widen;

/**
 * Companion class for creating Type Class instances for working with Streams
 * @author johnmcclean
 *
 */
@UtilityClass
public  class StreamInstances {

  public static InstanceDefinitions<stream> definitions(){
    return new InstanceDefinitions<stream>() {
      @Override
      public <T, R> Functor<stream> functor() {
        return StreamInstances.functor();
      }

      @Override
      public <T> Pure<stream> unit() {
        return StreamInstances.unit();
      }

      @Override
      public <T, R> Applicative<stream> applicative() {
        return StreamInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<stream> monad() {
        return StreamInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<stream>> monadZero() {
        return Option.some(StreamInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<stream>> monadPlus() {
        return Option.some(StreamInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<stream> monadRec() {
        return StreamInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<stream>> monadPlus(MonoidK<stream> m) {
        return Option.some(StreamInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<stream> traverse() {
        return StreamInstances.traverse();
      }

      @Override
      public <T> Foldable<stream> foldable() {
        return StreamInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<stream>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<stream>> unfoldable() {
        return Option.some(StreamInstances.unfoldable());
      }
    };
  }
    private final static StreamTypeClasses INSTANCE = new StreamTypeClasses();
    @AllArgsConstructor
    @With
    public static class StreamTypeClasses implements MonadPlus<stream>,
                                                            MonadRec<stream>,
                                                            TraverseByTraverse<stream>,
                                                            Foldable<stream>,
                                                            Unfoldable<stream>{

        private final MonoidK<stream> monoidK;
        public StreamTypeClasses(){
            monoidK = MonoidKs.combineStream();
        }
        @Override
        public <T> Higher<stream, T> filter(Predicate<? super T> predicate, Higher<stream, T> ds) {
            return widen(narrowK(ds).filter(predicate));
        }

        @Override
        public <T, R> Higher<stream, Tuple2<T, R>> zip(Higher<stream, T> fa, Higher<stream, R> fb) {
            return widen(Streams.zipStream(narrowK(fa),narrowK(fb), Tuple::tuple));
        }

        @Override
        public <T1, T2, R> Higher<stream, R> zip(Higher<stream, T1> fa, Higher<stream, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return widen(Streams.zipStream(narrowK(fa),narrowK(fb),f));
        }

        @Override
        public <T> MonoidK<stream> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<stream, R> flatMap(Function<? super T, ? extends Higher<stream, R>> fn, Higher<stream, T> ds) {
            return widen(narrowK(ds).flatMap(i-> StreamKind.narrow(fn.apply(i))));
        }

        @Override
        public <T, R> Higher<stream, R> ap(Higher<stream, ? extends Function<T, R>> fn, Higher<stream, T> apply) {
            return widen(Streams.zipStream(narrowK(apply),narrowK(fn),(a,b)->b.apply(a)));
        }

        @Override
        public <T> Higher<stream, T> unit(T value) {
            return StreamKind.of(value);
        }

        @Override
        public <T, R> Higher<stream, R> map(Function<? super T, ? extends R> fn, Higher<stream, T> ds) {
            return widen(narrowK(ds).map(fn));
        }


        @Override
        public <T, R> Higher<stream, R> tailRec(T initial, Function<? super T, ? extends Higher<stream, ? extends Either<T, R>>> fn) {
            return widen(ReactiveSeq.tailRec(initial, fn.andThen(s -> ReactiveSeq.fromStream(narrowK(s)))));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<stream, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<stream, T> ds) {
            ReactiveSeq<T> v = ReactiveSeq.fromStream(narrowK(ds));
             return v.<Higher<C2, Higher<stream,R>>>foldLeft(ap.unit(StreamKind.<R>of()),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> widen(Streams.append(narrowK(vec),sn))));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<stream, T> ds) {
            Stream<T> x = narrowK(ds);
            return x.reduce(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)),mb);
        }

        @Override
        public <T, R> Higher<stream, Tuple2<T, Long>> zipWithIndex(Higher<stream, T> ds) {
            return widen(ReactiveSeq.fromStream(narrowK(ds)).zipWithIndex());
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<stream, T> ds) {
            return narrowK(ds).reduce(monoid.zero(),monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<stream, T> ds) {
            return narrowK(ds).reduce(monoid.zero(),monoid);
        }


        @Override
        public <R, T> Higher<stream, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return widen(ReactiveSeq.unfold(b,fn));
        }


    }
    public static Unfoldable<stream> unfoldable(){
        return INSTANCE;
    }

    public static <T,R>Functor<stream> functor(){
        return INSTANCE;
    }

    public static <T> Pure<stream> unit(){
        return INSTANCE;
    }

    public static <T,R> Applicative<stream> zippingApplicative(){
        return INSTANCE;
    }

    public static <T,R> Monad<stream> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<stream> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<stream> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<stream> monadRec(){

        return INSTANCE;
    }

    public static <T> MonadPlus<stream> monadPlus(MonoidK<stream> m){
        return INSTANCE.withMonoidK(m);

    }

    public static <C2,T> Traverse<stream> traverse(){
        return INSTANCE;
    }


    public static <T,R> Foldable<stream> foldable(){
        return INSTANCE;
    }



}
