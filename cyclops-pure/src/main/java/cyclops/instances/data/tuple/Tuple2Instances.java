package cyclops.instances.data.tuple;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.tuple2;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.typeclasses.*;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static cyclops.data.tuple.Tuple2.narrowK;
import static cyclops.data.tuple.Tuple2.of;

public class Tuple2Instances {
  public static <T1,T2> Active<Higher<tuple2,T1>,T2> allTypeclasses(Tuple2<T1,T2> t2, Monoid<T1> m){
    return Active.of(t2, Tuple2Instances.definitions(m));
  }

  public static <T1> InstanceDefinitions<Higher<tuple2, T1>> definitions(Monoid<T1> m){
    return new InstanceDefinitions<Higher<tuple2, T1>>() {
      @Override
      public <T, R> Functor<Higher<tuple2, T1>> functor() {
        return Tuple2Instances.functor();
      }

      @Override
      public <T> Pure<Higher<tuple2, T1>> unit() {
        return Tuple2Instances.unit(m);
      }

      @Override
      public <T, R> Applicative<Higher<tuple2, T1>> applicative() {
        return Tuple2Instances.applicative(m);
      }

      @Override
      public <T, R> Monad<Higher<tuple2, T1>> monad() {
        return Tuple2Instances.monad(m);
      }

      @Override
      public <T, R> Option<MonadZero<Higher<tuple2, T1>>> monadZero() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<MonadPlus<Higher<tuple2, T1>>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<Higher<tuple2, T1>> monadRec() {
        return Tuple2Instances.monadRec(m);
      }


      @Override
      public <C2, T> Traverse<Higher<tuple2, T1>> traverse() {
        return Tuple2Instances.traverse(m);
      }

      @Override
      public <T> Option<MonadPlus<Higher<tuple2, T1>>> monadPlus(MonoidK<Higher<tuple2, T1>> m) {
        return Maybe.nothing();
      }

      @Override
      public <T> Foldable<Higher<tuple2, T1>> foldable() {
        return Tuple2Instances.foldable();
      }

      @Override
      public <T> Option<Comonad<Higher<tuple2, T1>>> comonad() {
        return Maybe.just(Tuple2Instances.comonad(m));
      }

      @Override
      public <T> Option<Unfoldable<Higher<tuple2, T1>>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

    public static<T1> Tuple2Typeclasses<T1> create(Monoid<T1> m){
      return new Tuple2Typeclasses<T1>(m);
    }
    @AllArgsConstructor
    public static class Tuple2Typeclasses<T1>  implements Monad<Higher<tuple2, T1>>,
                                                            TraverseByTraverse<Higher<tuple2, T1>>

                                                            {


        private final Monoid<T1> m;


        @Override
        public <T, R> Higher<Higher<tuple2, T1>, R> flatMap(Function<? super T, ? extends Higher<Higher<tuple2, T1>, R>> fn, Higher<Higher<tuple2, T1>, T> ds) {
            return narrowK(ds).flatMap(m,fn.andThen(Tuple2::narrowK));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<Higher<tuple2, T1>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<tuple2, T1>, T> ds) {
            Tuple2<T1, T> id = narrowK(ds);
            Function<R, Tuple2<T1,R>> rightFn = r -> of(id._1(),r);
            return applicative.map(rightFn, fn.apply(id._2()));
        }


        @Override
        public <T, R> Higher<Higher<tuple2, T1>, R> ap(Higher<Higher<tuple2, T1>, ? extends Function<T, R>> fn, Higher<Higher<tuple2, T1>, T> apply) {
            Tuple2<T1,? extends Function<T, R>> f = narrowK(fn);
            Tuple2<T1,T> ap = narrowK(apply);
            return f.flatMap(m,x -> ap.map2(x));
        }

        @Override
        public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
            return of(m.zero(),value);
        }

        @Override
        public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
            return narrowK(ds).map2(fn);
        }


    }
    public static <T1> Functor<Higher<tuple2, T1>> functor(){
        return new Functor<Higher<tuple2, T1>>(){

            @Override
            public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
                return narrowK(ds).map2(fn);
            }


        };
    }

  public static <T1> Pure<Higher<tuple2, T1>> unit(Monoid<T1> m){
    return new Pure<Higher<tuple2, T1>>(){
      @Override
      public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
        return of(m.zero(),value);
      }
    };
  }
  public static <T1> Applicative<Higher<tuple2, T1>> applicative(Monoid<T1> m){
    return create(m);
  }
  public static <T1> Monad<Higher<tuple2, T1>> monad(Monoid<T1> m){
    return create(m);
  }
  public static <T1> MonadRec<Higher<tuple2, T1>> monadRec(Monoid<T1> m) {
    return new MonadRec<Higher<tuple2, T1>>(){
      @Override
      public <T, R> Higher<Higher<tuple2, T1>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<tuple2, T1>, ? extends Either<T, R>>> fn) {
        return Tuple2.tailRec(m,initial,fn.andThen(Tuple2::narrowK));
      }

    };


  }
  public static <T1> Traverse<Higher<tuple2, T1>> traverse(Monoid<T1> m){
    return new Traverse<Higher<tuple2, T1>>(){
      @Override
      public <T, R> Higher<Higher<tuple2, T1>, R> ap(Higher<Higher<tuple2, T1>, ? extends Function<T, R>> fn, Higher<Higher<tuple2, T1>, T> apply) {
        return Tuple2Instances.<T1>applicative(m).ap(fn,apply);
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<Higher<tuple2, T1>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<tuple2, T1>, T> ds) {
        Tuple2<T1, T> id = narrowK(ds);
        Function<R, Tuple2<T1,R>> rightFn = r -> of(id._1(),r);
        return applicative.map(rightFn, fn.apply(id._2()));
      }

      @Override
      public <C2, T> Higher<C2, Higher<Higher<tuple2, T1>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<tuple2, T1>, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }

      @Override
      public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
        return Tuple2Instances.<T1>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
        return Tuple2Instances.unit(m).unit(value);
      }
    };
  }
  public static <T1> Foldable<Higher<tuple2, T1>> foldable(){
    return new Foldable<Higher<tuple2, T1>>(){
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<tuple2, T1>, T> ds) {
        return monoid.apply(narrowK(ds)._2(),monoid.zero());
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<tuple2, T1>, T> ds) {
        return monoid.apply(monoid.zero(),narrowK(ds)._2());
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> nestedA) {
        return foldLeft(mb,narrowK(nestedA).<R>map2(fn));
      }

    };
  }
  public static <T1> Comonad<Higher<tuple2, T1>> comonad(Monoid<T1> m){
    return new ComonadByPure<Higher<tuple2, T1>>(){
      @Override
      public <T> T extract(Higher<Higher<tuple2, T1>, T> ds) {
        return narrowK(ds)._2();
      }

      @Override
      public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
        return narrowK(ds).map2(fn);
      }

      @Override
      public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
        return of(m.zero(),value);
      }

    };
  }

  public static  <T1,T2> Kleisli<Higher<tuple2,T1>,Tuple2<T1,T2>,T2> kindKleisli(Monoid<T1> m){
    return Kleisli.of(Tuple2Instances.monad(m), Tuple2::widen);
  }

  public static  <T1,T2> Cokleisli<Higher<tuple2,T1>,T2,Tuple2<T1,T2>> kindCokleisli(){
    return Cokleisli.of(Tuple2::narrowK);
  }
}
