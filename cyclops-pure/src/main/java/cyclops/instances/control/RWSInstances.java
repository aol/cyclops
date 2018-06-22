package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.rws;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.ReaderWriterState;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Nested;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

import static cyclops.control.ReaderWriterState.narrowK;
import static cyclops.control.ReaderWriterState.rws;
import static cyclops.data.tuple.Tuple.tuple;

@UtilityClass
public  class RWSInstances {
  public <R,W,S,T> Active<Higher<Higher<Higher<rws,R>,W>,S>,T> allTypeclasses(ReaderWriterState<R,W,S,T> rws, R val1, S val2, Monoid<W> monoid){
    return Active.of(rws, RWSInstances.definitions(val1,val2,monoid));
  }
  public <R,W,S,T,W2,R2> Nested<Higher<Higher<Higher<rws, R>, W>, S>, W2, R2> mapM(ReaderWriterState<R,W,S,T> rws, R val1, S val2, Monoid<W> monoid, Function<? super T,? extends Higher<W2,R2>> fn, InstanceDefinitions<W2> defs){
    InstanceDefinitions<Higher<Higher<Higher<rws,R>, W>, S>> def1 = RWSInstances.definitions(val1,val2,monoid);
    ReaderWriterState<R, W, S, Higher<W2, R2>> r = rws.map(fn);
    Higher<Higher<Higher<Higher<rws,R>,W>,S>,Higher<W2,R2>> hkt = r;

    Nested<Higher<Higher<Higher<rws, R>, W>, S>, W2, R2> res = Nested.of(hkt, def1, defs);
    return res;
  }

  public static  <R,W,S,T> Kleisli<Higher<Higher<Higher<rws, R>, W>, S>,ReaderWriterState<R,W,S,T>,T> kindKleisli(Monoid<W> m){
    return Kleisli.of(RWSInstances.monad(m), ReaderWriterState::widen);
  }

  public static  <R,W,S,T> Cokleisli<Higher<Higher<Higher<rws, R>, W>, S>,T,ReaderWriterState<R,W,S,T>> kindCokleisli(){
    return Cokleisli.of(ReaderWriterState::narrowK);
  }

  public static <R,W,S> InstanceDefinitions<Higher<Higher<Higher<rws,R>,W>,S>> definitions(R val1, S val2, Monoid<W> monoid){
    return new InstanceDefinitions<Higher<Higher<Higher<rws,R>,W>,S>>() {

      @Override
      public <T, R2> Functor<Higher<Higher<Higher<rws, R>, W>, S>> functor() {
        return RWSInstances.functor();
      }

      @Override
      public <T> Pure<Higher<Higher<Higher<rws, R>, W>, S>> unit() {
        return RWSInstances.unit(monoid);
      }

      @Override
      public <T, R2> Applicative<Higher<Higher<Higher<rws, R>, W>, S>> applicative() {
        return RWSInstances.applicative(monoid);
      }

      @Override
      public <T, R2> Monad<Higher<Higher<Higher<rws, R>, W>, S>> monad() {
        return RWSInstances.monad(monoid);
      }

      @Override
      public <T, R2> Option<MonadZero<Higher<Higher<Higher<rws, R>, W>, S>>> monadZero() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<MonadPlus<Higher<Higher<Higher<rws, R>, W>, S>>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<Higher<Higher<Higher<rws, R>, W>, S>> monadRec() {
        return RWSInstances.monadRec(monoid);
      }

      @Override
      public <T> Option<MonadPlus<Higher<Higher<Higher<rws, R>, W>, S>>> monadPlus(MonoidK<Higher<Higher<Higher<rws, R>, W>, S>> m) {
        return Maybe.nothing();
      }

      @Override
      public <C2, T> Traverse<Higher<Higher<Higher<rws, R>, W>, S>> traverse() {
        return RWSInstances.traverse(val1,val2,monoid);
      }

      @Override
      public <T> Foldable<Higher<Higher<Higher<rws, R>, W>, S>> foldable() {
        return RWSInstances.foldable(val1,val2,monoid);
      }

      @Override
      public <T> Option<Comonad<Higher<Higher<Higher<rws, R>, W>, S>>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<Higher<Higher<Higher<rws, R>, W>, S>>> unfoldable() {
        return Maybe.nothing();
      }
    }  ;
  }
  public static <R,W,S> Functor<Higher<Higher<Higher<rws,R>,W>,S>> functor(){
    return new  Functor<Higher<Higher<Higher<rws,R>,W>,S>>(){

      @Override
      public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> ds) {
        ReaderWriterState<R, W, S, T> r = narrowK(ds);
        return r.map(fn);
      }
    };
  }

  public static <R,W,S>  Pure<Higher<Higher<Higher<rws,R>,W>,S>>unit(Monoid<W> monoid){
    return new Pure<Higher<Higher<Higher<rws,R>,W>,S>>() {

      @Override
      public <T> Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> unit(T value) {
        return rws((a,s)-> tuple(monoid.zero(),s,value),monoid);
      }
    };
  }

  public static <R,W,S> Applicative<Higher<Higher<Higher<rws,R>,W>,S>> applicative(Monoid<W> monoid){
    return new Applicative<Higher<Higher<Higher<rws,R>,W>,S>>(){
      @Override
      public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> ap(Higher<Higher<Higher<Higher<rws, R>, W>, S>, ? extends Function<T, R2>> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> apply) {
        ReaderWriterState<R, W, S, ? extends Function<T, R2>> f = narrowK(fn);
        ReaderWriterState<R, W, S, T> ap = narrowK(apply);
        ReaderWriterState<R, W, S, R2> mapped = f.flatMap(x -> ap.map(x));
        return mapped;
      }

      @Override
      public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> ds) {
        return RWSInstances.<R,W,S>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> unit(T value) {
        return RWSInstances.<R,W,S>unit(monoid).unit(value);
      }

    };
  }
  public static <R1,W,S> Monad<Higher<Higher<Higher<rws, R1>,W>,S>> monad(Monoid<W> monoid) {
    return new Monad<Higher<Higher<Higher<rws, R1>,W>,S>>() {


      @Override
      public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
        ReaderWriterState<R1, W, S, T> r = narrowK(ds);
        return r.flatMap(fn.andThen(h->narrowK(h)));

      }

      @Override
      public <T, R2> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R2> ap(Higher<Higher<Higher<Higher<rws, R1>, W>, S>, ? extends Function<T, R2>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> apply) {
        return RWSInstances.<R1,W,S>applicative(monoid).ap(fn,apply);
      }

      @Override
      public <T, R2> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
        return RWSInstances.<R1, W, S>functor().map(fn, ds);
      }

      @Override
      public <T> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> unit(T value) {
        return RWSInstances.<R1,W,S>unit(monoid).unit(value);
      }

    };

  }
  public static <R1,W,S> Foldable<Higher<Higher<Higher<rws, R1>,W>,S>> foldable(R1 val1, S val2, Monoid<W> monoid) {
    return new Foldable<Higher<Higher<Higher<rws, R1>, W>, S>>() {
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
        ReaderWriterState<R1, W, S, T> rws =narrowK(ds);
        return rws.run(val1, val2).transform((a, b, t) -> monoid.fold(t));
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
        ReaderWriterState<R1, W, S, T> rws =narrowK(ds);
        return rws.run(val1, val2).transform((a, b, t) -> monoid.fold(t));
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> nestedA) {
        return foldLeft(mb,narrowK(nestedA).<R>map(fn));
      }
    };
  }
  public static <R1,W,S> Traverse<Higher<Higher<Higher<rws, R1>,W>,S>> traverse(R1 val1, S val2, Monoid<W> monoid) {
    return new Traverse<Higher<Higher<Higher<rws, R1>, W>, S>>() {
      @Override
      public <C2, T, R> Higher<C2, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
        ReaderWriterState<R1,W,S,T> rws = narrowK(ds);
        Higher<C2, R> x = rws.run(val1, val2).transform((a, b, t) -> fn.apply(t));
        return applicative.map_(x,i-> rws((ra, rb) -> tuple(monoid.zero(), rb, i), monoid));
      }
      @Override
      public <C2, T> Higher<C2, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }

      @Override
      public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> ap(Higher<Higher<Higher<Higher<rws, R1>, W>, S>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> apply) {
        return RWSInstances.<R1,W,S>applicative(monoid).ap(fn,apply);
      }

      @Override
      public <T> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> unit(T value) {
        return RWSInstances.<R1,W,S>unit(monoid).unit(value);
      }

      @Override
      public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
        return RWSInstances.<R1, W, S>functor().map(fn, ds);
      }
    };
  }
  public static <R1,W,S> MonadRec<Higher<Higher<Higher<rws, R1>,W>,S>> monadRec(Monoid<W> monoid) {
    return new MonadRec<Higher<Higher<Higher<rws, R1>,W>,S>>() {


      @Override
      public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<Higher<Higher<rws, R1>, W>, S>, ? extends Either<T, R>>> fn) {
        return narrowK(fn.apply(initial)).flatMap( eval ->
          eval.fold(s->narrowK(tailRec(s,fn)), p->{
            ReaderWriterState<R1, W, S, R> k = narrowK(RWSInstances.<R1, W, S>unit(monoid).<R>unit(p));
            return k;
          }));
      }


    };
  }
}
