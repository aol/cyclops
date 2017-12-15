package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.constant;
import com.oath.cyclops.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.control.Constant;
import cyclops.data.Seq;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.SemigroupK;
import cyclops.typeclasses.monad.Applicative;

import java.util.function.Function;

import static cyclops.control.Constant.narrowK;

public  class ConstantInstances {

  public static <T,P> SemigroupK<Higher<constant,T>> semigroupK(Semigroup<T> monoid){

    return new SemigroupK<Higher<constant, T>>() {
      @Override
      public <T2> Higher<Higher<constant, T>, T2> apply(Higher<Higher<constant, T>, T2> t1, Higher<Higher<constant, T>, T2> t2) {
        return Constant.of(monoid.apply(narrowK(t1).value, narrowK(t2).value));
      }
    };

  }
  public static <T,P> MonoidK<Higher<constant,T>> monoidK(Monoid<T> monoid){
    return new MonoidK<Higher<constant, T>>() {
      @Override
      public <T2> Higher<Higher<constant, T>, T2> zero() {
        return Constant.of(monoid.zero());
      }

      @Override
      public <T2> Higher<Higher<constant, T>, T2> apply(Higher<Higher<constant, T>, T2> t1, Higher<Higher<constant, T>, T2> t2) {
        return Constant.of(monoid.apply(narrowK(t1).value, narrowK(t2).value));
      }
    };
  }


  public static <T1,P> Applicative<Higher<constant,T1>> applicative(Monoid<T1> m){
    return new Applicative<Higher<constant,T1>>(){


      @Override
      public <T, R> Higher<Higher<constant, T1>, R> ap(Higher<Higher<constant, T1>, ? extends Function<T, R>> fn, Higher<Higher<constant, T1>, T> apply) {
        return Constant.of(m.apply(narrowK(fn).value,narrowK(apply).value));
      }

      @Override
      public <T, R> Higher<Higher<constant, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<constant, T1>, T> ds) {
        return narrowK(ds).map(fn);
      }

      @Override
      public <T> Higher<Higher<constant, T1>, T> unit(T value) {
        return Constant.of(m.zero());

      }
    };
  }
}
