package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.state;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.State;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

import static cyclops.control.State.narrowK;

@UtilityClass
public  class StateInstances {
  public static  <S,T> Kleisli<Higher<state, S>,State<S,T>,T> kindKleisli(){
    return Kleisli.of(StateInstances.monad(), State::widen);
  }

  public static  <S,T> Cokleisli<Higher<state, S>,T,State<S,T>> kindCokleisli(){
    return Cokleisli.of(State::narrowK);
  }
  public static <W1,T,S> Nested<Higher<state,S>,W1,T> nested(State<S,Higher<W1,T>> nested, S value, InstanceDefinitions<W1> def2){
    return Nested.of(nested, StateInstances.definitions(value),def2);
  }
  public static <W1,S, T> Product<Higher<state,S>,W1,T> product(State<S,T> s, S value, Active<W1,T> active){
    return Product.of(allTypeclasses(s,value), active);
  }
  public static <W1,S, T> Coproduct<W1,Higher<state,S>,T> coproduct(State<S,T> s, S value, InstanceDefinitions<W1> def2){
    return Coproduct.right(s,def2, StateInstances.definitions(value));
  }

  public <S,T> Active<Higher<state,S>,T> allTypeclasses(State<S,T> s,S value){
    return Active.of(s, StateInstances.definitions(value));
  }

  public <W2,R,S,T> Nested<Higher<state,S>,W2,R> mapM(State<S,T> s,S value, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(s.map(fn), StateInstances.definitions(value), defs);
  }
  public static <S> InstanceDefinitions<Higher<state, S>> definitions(S val){
    return new InstanceDefinitions<Higher<state, S>>() {

      @Override
      public <T, R> Functor<Higher<state, S>> functor() {
        return StateInstances.functor();
      }

      @Override
      public <T> Pure<Higher<state, S>> unit() {
        return StateInstances.unit();
      }

      @Override
      public <T, R> Applicative<Higher<state, S>> applicative() {
        return StateInstances.applicative();
      }

      @Override
      public <T, R> Monad<Higher<state, S>> monad() {
        return StateInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<Higher<state, S>>> monadZero() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<MonadPlus<Higher<state, S>>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<Higher<state, S>> monadRec() {
        return StateInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<Higher<state, S>>> monadPlus(MonoidK<Higher<state, S>> m) {
        return Maybe.nothing();
      }

      @Override
      public <C2, T> Traverse<Higher<state, S>> traverse() {
        return StateInstances.traverse(val);
      }

      @Override
      public <T> Foldable<Higher<state, S>> foldable() {
        return StateInstances.foldable(val);
      }

      @Override
      public <T> Option<Comonad<Higher<state, S>>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<Higher<state, S>>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }
  public static <S> Functor<Higher<state, S>> functor() {
    return new Functor<Higher<state, S>>() {
      @Override
      public <T, R> Higher<Higher<state, S>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<state, S>, T> ds) {
        return narrowK(ds).map(fn);
      }
    };
  }
  public static <S> Pure<Higher<state, S>> unit() {
    return new Pure<Higher<state, S>>() {

      @Override
      public <T> Higher<Higher<state, S>, T> unit(T value) {
        return State.constant(value);
      }
    };
  }
  public static <S> Applicative<Higher<state, S>> applicative() {
    return new Applicative<Higher<state, S>>() {

      @Override
      public <T, R> Higher<Higher<state, S>, R> ap(Higher<Higher<state, S>, ? extends Function<T, R>> fn, Higher<Higher<state, S>, T> apply) {
        State<S, ? extends Function<T, R>> f = narrowK(fn);
        State<S, T> ap = narrowK(apply);
        return f.flatMap(fn1->ap.map(a->fn1.apply(a)));
      }

      @Override
      public <T, R> Higher<Higher<state, S>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<state, S>, T> ds) {
        return StateInstances.<S>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<state, S>, T> unit(T value) {
        return StateInstances.<S>unit().unit(value);
      }
    };
  }
  public static <S> Monad<Higher<state, S>> monad() {
    return new Monad<Higher<state, S>>() {


      @Override
      public <T, R> Higher<Higher<state, S>, R> ap(Higher<Higher<state, S>, ? extends Function<T, R>> fn, Higher<Higher<state, S>, T> apply) {
        return StateInstances.<S>applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<Higher<state, S>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<state, S>, T> ds) {
        return StateInstances.<S>functor().map(fn,ds);
      }

      @Override
      public <T> Higher<Higher<state, S>, T> unit(T value) {
        return StateInstances.<S>unit().unit(value);
      }

      @Override
      public <T, R> Higher<Higher<state, S>, R> flatMap(Function<? super T, ? extends Higher<Higher<state, S>, R>> fn, Higher<Higher<state, S>, T> ds) {
        return narrowK(ds).flatMap(fn.andThen(h->narrowK(h)));
      }
    };
  }
  public static <S> Traverse<Higher<state, S>> traverse(S defaultValue) {
    return new Traverse<Higher<state, S>>() {
      @Override
      public <C2, T, R> Higher<C2, Higher<Higher<state, S>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<state, S>, T> ds) {
        State<S, T> s = narrowK(ds);
        Higher<C2, R> x = fn.apply(s.eval(defaultValue));
        return applicative.map(r->State.constant(r),x);
      }

      @Override
      public <C2, T> Higher<C2, Higher<Higher<state, S>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<state, S>, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }

      @Override
      public <T, R> Higher<Higher<state, S>, R> ap(Higher<Higher<state, S>, ? extends Function<T, R>> fn, Higher<Higher<state, S>, T> apply) {
        return StateInstances.<S>applicative().ap(fn,apply);
      }

      @Override
      public <T> Higher<Higher<state, S>, T> unit(T value) {
        return StateInstances.<S>unit().unit(value);
      }

      @Override
      public <T, R> Higher<Higher<state, S>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<state, S>, T> ds) {
        return StateInstances.<S>functor().map(fn,ds);
      }
    };
  }

  public static <S> Foldable<Higher<state,S>> foldable(S val) {
    return new Foldable<Higher<state, S>>() {


      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<state, S>, T> ds) {
        return monoid.fold(narrowK(ds).eval(val));

      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<state, S>, T> ds) {
        return monoid.fold(narrowK(ds).eval(val));
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<state, S>, T> nestedA) {
        return foldLeft(mb,narrowK(nestedA).<R>map(fn));
      }
    };
  }
  public static <S> MonadRec<Higher<state,S>> monadRec() {
    return new MonadRec<Higher<state,S>>() {
      @Override
      public <T, R> Higher<Higher<state, S>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<state, S>, ? extends Either<T, R>>> fn) {
        return narrowK(fn.apply(initial)).flatMap( eval ->
          eval.fold(s->narrowK(tailRec(s,fn)), p->State.constant(p)));
      }
    };
  }


}
