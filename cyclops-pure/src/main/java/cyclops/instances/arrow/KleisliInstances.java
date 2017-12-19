package cyclops.instances.arrow;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.kleisli;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Reader;
import cyclops.function.Monoid;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.functor.ProFunctor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;

import java.util.function.Function;

import static cyclops.arrow.Kleisli.narrowK;

public class KleisliInstances {
  public static <IN> InstanceDefinitions<Higher<kleisli, IN>> definitions(IN in) {
    return new InstanceDefinitions<Higher<kleisli, IN>>() {

      @Override
      public <T, R> Functor<Higher<kleisli, IN>> functor() {
        return KleisliInstances.functor();
      }

      @Override
      public <T> Pure<Higher<kleisli, IN>> unit() {
        return KleisliInstances.unit();
      }

      @Override
      public <T, R> Applicative<Higher<kleisli, IN>> applicative() {
        return KleisliInstances.applicative();
      }

      @Override
      public <T, R> Monad<Higher<kleisli, IN>> monad() {
        return KleisliInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<Higher<kleisli, IN>>> monadZero() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<MonadPlus<Higher<kleisli, IN>>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<Higher<kleisli, IN>> monadRec() {
        return KleisliInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<Higher<kleisli, IN>>> monadPlus(MonoidK<Higher<kleisli, IN>> m) {
        return Maybe.nothing();
      }


      @Override
      public <C2, T> Traverse<Higher<kleisli, IN>> traverse() {
        return KleisliInstances.traversable(in);
      }

      @Override
      public <T> Foldable<Higher<kleisli, IN>> foldable() {
        return KleisliInstances.foldable(in);
      }

      @Override
      public <T> Option<Comonad<Higher<kleisli, IN>>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<Higher<kleisli, IN>>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

  public static <W,IN> Functor<Higher<Higher<kleisli,W>,IN>> functor() {
    return new Functor<Higher<Higher<kleisli, W>, IN>>() {
      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> ds) {
        return  narrowK(ds).map(fn);
      }
    };

  }

  public static <W,IN> Pure<Higher<Higher<kleisli,W>,IN>> unit(Monad<W> monad) {
    return new Pure<Higher<Higher<kleisli, W>, IN>>() {
      @Override
      public <T> Higher<Higher<Higher<kleisli, W>, IN>, T> unit(T value) {
        return Kleisli.of(monad,i->monad.unit(value));
      }
    };

  }

  public static <W,IN> Applicative<Higher<Higher<kleisli,W>,IN>> applicative(Monad<W> monad) {

    return new Applicative<Higher<Higher<kleisli, W>, IN>>() {
      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> ap(Higher<Higher<Higher<kleisli, W>, IN>, ? extends Function<T, R>> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> apply) {
        Kleisli<W, IN, ? extends Function<T, R>> k = narrowK(fn);
        Kleisli<W, IN, T> ap = narrowK(apply);
        return k.flatMapK(fn2-> ap.map(t -> fn2.apply(t)));
      }

      @Override
      public <T> Higher<Higher<Higher<kleisli, W>, IN>, T> unit(T value) {
        return KleisliInstances.<W,IN>unit(monad).unit(value);
      }

      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> ds) {
        return KleisliInstances.<W,IN>functor().map(fn,ds);
      }
    };

  }

  public static <IN> Foldable<Higher<kleisli, IN>> foldable(IN t) {
    return new Foldable<Higher<kleisli, IN>>() {
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<Higher<kleisli, IN>, T> ds) {
        return foldLeft(monoid,ds);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<kleisli, IN>, T> ds) {
        Reader<IN, T> r = narrowK(ds);
        return r.foldLeft(t,monoid);

      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<kleisli, IN>, T> nestedA) {
        return foldLeft(mb,narrowK(nestedA).<R>mapFn(fn));
      }
    };
  }

  public static <IN,C2, T, R> Higher<C2, Higher<Higher<kleisli, IN>, R>> traverseA(IN t, Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<kleisli, IN>, T> ds) {
    Reader<IN, T> r = narrowK(ds);

    return applicative.map(i -> {
      Reader<IN,R> res = a->i;
      return res;
    }, fn.apply(r.apply(t)));
  }
  public static <IN> Traverse<Higher<kleisli, IN>> traversable(IN t) {

    return General.traverseByTraverse(applicative(), (a, b, c)-> traverseA(t,a,b,c));

  }
  public static <IN> Monad<Higher<kleisli, IN>> monad() {
    return new Monad<Higher<kleisli, IN>>() {

      @Override
      public <T, R> Higher<Higher<kleisli, IN>, R> ap(Higher<Higher<kleisli, IN>, ? extends Function<T, R>> fn, Higher<Higher<kleisli, IN>, T> apply) {
        return KleisliInstances.<IN>applicative().ap(fn, apply);
      }

      @Override
      public <T, R> Higher<Higher<kleisli, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<kleisli, IN>, T> ds) {
        return KleisliInstances.<IN>functor().map(fn, ds);
      }

      @Override
      public <T> Higher<Higher<kleisli, IN>, T> unit(T value) {
        return KleisliInstances.<IN>unit().unit(value);
      }

      @Override
      public <T, R> Higher<Higher<kleisli, IN>, R> flatMap(Function<? super T, ? extends Higher<Higher<kleisli, IN>, R>> fn, Higher<Higher<kleisli, IN>, T> ds) {
        Reader<IN, T> mapper = narrowK(ds);
        Reader<IN, R> res = mapper.flatMap(fn.andThen(Reader::narrowK));
        return res;
      }
    };

  }

  public static <IN,R> ProFunctor<kleisli> profunctor() {
    return new ProFunctor<kleisli>() {

      @Override
      public <A, B, C, D> Higher<Higher<kleisli, C>, D> dimap(Function<? super C, ? extends A> f, Function<? super B, ? extends D> g, Higher<Higher<kleisli, A>, B> p) {
        Reader<A, B> r = narrowK(p);
        Function<? super C, ? extends D> f1 = g.compose(r).compose(f);
        Reader<C,D> r1 = in->f1.apply(in);
        return r1;
      }
    };
  }

  public static <IN, T, R> MonadRec<Higher<kleisli, IN>> monadRec() {
    return new MonadRec<Higher<kleisli, IN>>() {
      @Override
      public <T, R> Higher<Higher<kleisli, IN>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<kleisli, IN>, ? extends Either<T, R>>> fn) {

        Reader<IN, Reader<IN, R>> kleisli = (IN in) -> {
          Reader<IN, ? extends Either<T, R>> next[] = new Reader[1];
          next[0] = __ -> Either.left(initial);
          boolean cont = true;
          do {

            cont = next[0].apply(in).visit(s -> {
              Reader<IN, ? extends Either<T, R>> x = narrowK(fn.apply(s));

              next[0] = narrowK(fn.apply(s));
              return true;
            }, pr -> false);
          } while (cont);
          return next[0].mapFn(x->x.orElse(null));
        };
        return kleisli.flatMap(Function.identity());

      }


    };


  }
}
