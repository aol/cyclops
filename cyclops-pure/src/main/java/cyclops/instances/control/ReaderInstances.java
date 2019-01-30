package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.reader;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Reader;
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
import cyclops.typeclasses.functor.ProFunctor;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.MonadPlus;
import cyclops.typeclasses.monad.MonadRec;
import cyclops.typeclasses.monad.MonadZero;
import cyclops.typeclasses.monad.Traverse;
import cyclops.typeclasses.monad.TraverseByTraverse;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

import static cyclops.control.Reader.narrowK;

@UtilityClass
public  class ReaderInstances {
  public static <IN,T,R> Reader<IN,R> tailRec(T initial,Function<? super T,? extends Reader<IN, ? extends Either<T, R>>> fn ){
    return narrowK(ReaderInstances.<IN, T, R>monadRec().tailRec(initial, fn));
  }
  public static  <R,T> Kleisli<Higher<reader,T>,Reader<T,R>,R> kindKleisli(){
    return Kleisli.of(ReaderInstances.monad(), Reader::widen);
  }

  public static  <T,R> Cokleisli<Higher<reader,T>,R,Reader<T,R>> kindCokleisli(){
    return Cokleisli.of(Reader::narrowK);
  }
  public static <W1,T,R> Nested<Higher<reader,T>,W1,R> nested(Reader<T,Higher<W1,R>> nested, T defaultValue, InstanceDefinitions<W1> def2){
    return Nested.of(nested, ReaderInstances.definitions(defaultValue),def2);
  }
  public static <W1,T,R> Product<Higher<reader,T>,W1,R> product(Reader<T,R> r, T defaultValue, Active<W1,R> active){
    return Product.of(allTypeclasses(r,defaultValue),active);
  }
  public static <W1,T,R> Coproduct<W1,Higher<reader,T>,R> coproduct(Reader<T,R> r, T defaultValue, InstanceDefinitions<W1> def2){
    return Coproduct.right(r,def2, ReaderInstances.definitions(defaultValue));
  }
  public static <T,R> Active<Higher<reader,T>,R> allTypeclasses(Reader<T,R> r,T defaultValue){
    return Active.of(r, ReaderInstances.definitions(defaultValue));
  }
  public static <W2,R2,T,R> Nested<Higher<reader,T>,W2,R2> mapM(Reader<T,R> r,T defaultValue,Function<? super R,? extends Higher<W2,R2>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(r.mapFn(fn), ReaderInstances.definitions(defaultValue), defs);
  }

  public static <IN> InstanceDefinitions<Higher<reader, IN>> definitions(IN in) {
    return new InstanceDefinitions<Higher<reader, IN>>() {

      @Override
      public <T, R> Functor<Higher<reader, IN>> functor() {
        return ReaderInstances.functor();
      }

      @Override
      public <T> Pure<Higher<reader, IN>> unit() {
        return ReaderInstances.unit();
      }

      @Override
      public <T, R> Applicative<Higher<reader, IN>> applicative() {
        return ReaderInstances.applicative();
      }

      @Override
      public <T, R> Monad<Higher<reader, IN>> monad() {
        return ReaderInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<Higher<reader, IN>>> monadZero() {
        return Option.none();
      }

      @Override
      public <T> Option<MonadPlus<Higher<reader, IN>>> monadPlus() {
        return Option.none();
      }

      @Override
      public <T> MonadRec<Higher<reader, IN>> monadRec() {
        return ReaderInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<Higher<reader, IN>>> monadPlus(MonoidK<Higher<reader, IN>> m) {
        return Option.none();
      }


      @Override
      public <C2, T> Traverse<Higher<reader, IN>> traverse() {
        return ReaderInstances.traversable(in);
      }

      @Override
      public <T> Foldable<Higher<reader, IN>> foldable() {
        return ReaderInstances.foldable(in);
      }

      @Override
      public <T> Option<Comonad<Higher<reader, IN>>> comonad() {
        return Option.none();
      }

      @Override
      public <T> Option<Unfoldable<Higher<reader, IN>>> unfoldable() {
        return Option.none();
      }
    };
  }

  private final static ReaderTypeclass INSTANCE = new ReaderTypeclass();

  public static class ReaderTypeclass<IN> implements  Monad<Higher<reader, IN>>,
                                                        MonadRec<Higher<reader, IN>>,
                                                        ProFunctor<reader>{


      @Override
      public <A, B, C, D> Higher<Higher<reader, C>, D> dimap(Function<? super C, ? extends A> f, Function<? super B, ? extends D> g, Higher<Higher<reader, A>, B> ds) {
          Reader<A, B> r = narrowK(ds);
          Function<? super C, ? extends D> f1 = g.compose(r).compose(f);
          Reader<C,D> r1 = in->f1.apply(in);
          return r1;
      }

      @Override
      public <T, R> Higher<Higher<reader, IN>, R> flatMap(Function<? super T, ? extends Higher<Higher<reader, IN>, R>> fn, Higher<Higher<reader, IN>, T> ds) {
          Reader<IN, T> mapper = narrowK(ds);
          Reader<IN, R> res = mapper.flatMap(fn.andThen(Reader::narrowK));
          return res;
      }

      @Override
      public <T, R> Higher<Higher<reader, IN>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<reader, IN>, ? extends Either<T, R>>> fn) {
          return null;
      }



      @Override
      public <T, R> Higher<Higher<reader, IN>, R> ap(Higher<Higher<reader, IN>, ? extends Function<T, R>> fn, Higher<Higher<reader, IN>, T> apply) {
          Reader<IN, ? extends Function<T, R>> f = narrowK(fn);
          Reader<IN, T> ap = narrowK(apply);
          Reader<IN, R> res = in -> f.apply(in).apply(ap.apply(in));
          return res;
      }

      @Override
      public <T> Higher<Higher<reader, IN>, T> unit(T value) {
          Reader<IN, T> fn = __ -> value;
          return fn;
      }

      @Override
      public <T, R> Higher<Higher<reader, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<reader, IN>, T> ds) {
          Reader<IN, T> fn1 = narrowK(ds);
          Reader<IN, R> res = fn1.mapFn(fn);
          return res;
      }
  }
  public static <IN> Functor<Higher<reader, IN>> functor() {
    return INSTANCE;
  }

  public static <IN> Pure<Higher<reader, IN>> unit() {
    return INSTANCE;
  }

  public static <IN> Applicative<Higher<reader, IN>> applicative() {
    return INSTANCE;
  }

  public static <IN> Foldable<Higher<reader, IN>> foldable(IN t) {
    return new Foldable<Higher<reader, IN>>() {
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<reader, IN>, T> ds) {
        return foldLeft(monoid,ds);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<reader, IN>, T> ds) {
        Reader<IN, T> r = narrowK(ds);
        return r.foldLeft(t,monoid);

      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<reader, IN>, T> nestedA) {
        return foldLeft(mb,narrowK(nestedA).<R>mapFn(fn));
      }
    };
  }

  public static <IN,C2, T, R> Higher<C2, Higher<Higher<reader, IN>, R>> traverseA(IN t, Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<reader, IN>, T> ds) {
    Reader<IN, T> r = narrowK(ds);

    return applicative.map(i -> {
      Reader<IN,R> res = a->i;
      return res;
    }, fn.apply(r.apply(t)));
  }
  public static <IN> Traverse<Higher<reader, IN>> traversable(IN t) {
      return new Traverse<Higher<reader, IN>>() {
          @Override
          public <C2, T, R> Higher<C2, Higher<Higher<reader, IN>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<reader, IN>, T> ds) {
              Reader<IN, T> r = narrowK(ds);

              return applicative.map(i -> {
                  Reader<IN,R> res = a->i;
                  return res;
              }, fn.apply(r.apply(t)));
          }

          @Override
          public <C2, T> Higher<C2, Higher<Higher<reader, IN>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<reader, IN>, Higher<C2, T>> ds) {
              return traverseA(applicative,Function.identity(),ds);
          }

          @Override
          public <T, R> Higher<Higher<reader, IN>, R> ap(Higher<Higher<reader, IN>, ? extends Function<T, R>> fn, Higher<Higher<reader, IN>, T> apply) {
              return ReaderInstances.<IN>applicative().ap(fn,apply);
          }

          @Override
          public <T> Higher<Higher<reader, IN>, T> unit(T value) {
              return ReaderInstances.<IN>applicative().unit(value);
          }

          @Override
          public <T, R> Higher<Higher<reader, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<reader, IN>, T> ds) {
              return ReaderInstances.<IN>applicative().map(fn,ds);
          }
      };



  }
  public static <IN> Monad<Higher<reader, IN>> monad() {
    return INSTANCE;

  }

  public static <IN,R> ProFunctor<reader> profunctor() {
    return INSTANCE;
  }

  public static <IN, T, R> MonadRec<Higher<reader, IN>> monadRec() {
    return INSTANCE;
  }
}
