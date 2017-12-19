package cyclops.function;


import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.oath.cyclops.types.foldable.To;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.VectorX;
import cyclops.control.Option;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Try;

import cyclops.collections.mutable.ListX;
import cyclops.control.Future;
import cyclops.reactive.ReactiveSeq;

@FunctionalInterface
public interface Function2<T1, T2, R> extends BiFunction<T1,T2,R>, To<Function2<T1,T2,R>> {

    public static <T1, T2, T3,R> Function2<T1,T2, R> of(final BiFunction<T1,T2, R> triFunc){
    return (a,b)->triFunc.apply(a,b);
  }
    public static <T1, T2, T3,R> Function2<T1,T2, R> λ(final Function2<T1,T2, R> triFunc){
        return triFunc;
    }
    public static <T1, T2, T3,R> Function2<? super T1,? super T2,? extends R> λv(final Function2<? super T1,? super T2,? extends R> triFunc){
        return triFunc;
    }

    public R apply(T1 a, T2 b);





    default Function2<T1, T2,  Maybe<R>> lift(){
        Function2<T1, T2,  R> host = this;
       return (T1,T2)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(T1,T2))));
    }
    default Function2<T1, T2, Future<R>> lift(Executor ex){
        Function2<T1, T2,  R> host = this;
       return (T1,T2)-> Future.of(()->host.apply(T1,T2),ex);
    }
    default Function2<T1, T2,  Try<R,Throwable>> liftTry(){
        Function2<T1, T2,  R> host = this;
       return (T1,T2)->  Try.withCatch(()->host.apply(T1,T2),Throwable.class);
    }
    default Function2<T1, T2, Option<R>> liftOpt(){
        Function2<T1, T2,  R> host = this;
       return (T1,T2)-> Option.ofNullable(host.apply(T1,T2));
    }


    default Function2<T1,T2,R> memoize(){
        return Memoize.memoizeBiFunction(this);
    }
    default Function2<T1,T2,R> memoize(Cacheable<R> c){
        return Memoize.memoizeBiFunction(this,c);
    }
    default Function2<T1,T2, R> memoizeAsync(ScheduledExecutorService ex, String cron){
        return Memoize.memoizeBiFunctionAsync(this,ex,cron);
    }
    default Function2<T1,T2, R> memoizeAsync(ScheduledExecutorService ex, long timeToLiveMillis){
        return Memoize.memoizeBiFunctionAsync(this,ex,timeToLiveMillis);
    }

    default Function1<? super T1,Function1<? super T2,? extends  R>> curry(){
        return CurryVariance.curry2(this);
    }


    default Function1<T2, R> apply(final T1 s) {
        return Curry.curry2(this)
                    .apply(s);
    }

    default Function2<T2,T1,R> reverse(){
        return (t2,t1)->apply(t1,t2);
    }
    @Override
    default <V> Function2<T1, T2, V> andThen(Function<? super R, ? extends V> after) {
        return (t1,t2)-> after.apply(apply(t1,t2));
    }
    default <V> Function2<T1,T2, V> apply(final BiFunction<? super T1,? super T2,? extends Function<? super R,? extends V>> applicative) {
      return (a,b) -> applicative.apply(a,b).apply(this.apply(a,b));
    }

    default <R1> Function2<T1,T2, R1> mapFn(final Function<? super R, ? extends R1> f2) {
      return andThen(f2);
    }

    default <R1> Function2<T1, T2, R1> flatMapFn(final Function<? super R, ? extends Function<? super T1, ? extends R1>> f) {
      return (a,b)-> f.apply(apply(a,b)).apply(a);
    }
    default FunctionalOperations<T1,T2,R> functionOps(){
        return (a,b)->apply(a,b);
    }

    interface FunctionalOperations<T1,T2,R> extends Function2<T1,T2,R> {





        default Function2<ReactiveSeq<T1>,ReactiveSeq<T2>, ReactiveSeq<R>> streamZip() {
            return (a,b) -> a.zip(b,this);
        }

        default Function2<ReactiveSeq<T1>,ReactiveSeq<T2>, ReactiveSeq<R>> streamM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default Function2<Future<T1>, Future<T2>, Future<R>> futureZip() {
            return (a,b) -> a.zip(b,this);
        }
        default Function2<Future<T1>, Future<T2>, Future<R>> futureM() {
            return (a,b) -> a.forEach2(x->b,this);
        }


        default Function2<ListX<T1>,ListX<T2>, ListX<R>> listXZip() {
            return (a,b) -> a.zip(b,this);
        }

        default Function2<ListX<T1>,ListX<T2>, ListX<R>> listXM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default Function2<LinkedListX<T1>,LinkedListX<T2>, LinkedListX<R>> linkedListXZip() {
            return (a,b) -> a.zip(b,this);
        }

        default Function2<LinkedListX<T1>,LinkedListX<T2>, LinkedListX<R>> linkedListXM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default Function2<VectorX<T1>,VectorX<T2>, VectorX<R>> vectorXZip() {
            return (a,b) -> a.zip(b,this);
        }


    }

}
