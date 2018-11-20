package cyclops.function;


import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oath.cyclops.types.foldable.To;
import cyclops.control.*;

import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;

@FunctionalInterface
public interface Function2<T1, T2, R> extends BiFunction<T1,T2,R>, To<Function2<T1,T2,R>> {

    public static <T1, T2, R> Function2<T1,T2, R> of(final BiFunction<T1,T2, R> triFunc){
    return (a,b)->triFunc.apply(a,b);
  }
    public static <T1, T2, R> Function2<T1,T2, R> λ(final Function2<T1,T2, R> triFunc){
        return triFunc;
    }
    public static <T1, T2, R> Function2<? super T1,? super T2,? extends R> λv(final Function2<? super T1,? super T2,? extends R> triFunc){
        return triFunc;
    }

    static <T1,T2,R> Function2<T1,T2,R> left(Function<T1,R> fn ) {
        return (a,b)->fn.apply(a);
    }
    static <T1,T2,R> Function2<T1,T2,R> right(Function<T2,R> fn ) {
        return (a,b)->fn.apply(b);
    }
    static <T1,T2,R> Function2<T1,T2,R> constant(R d) {
        return (a,b)->d;
    }
    static <T1,T2,R> Function2<T1,T2,R> lazyConstant(Supplier<R> d) {
        return (a,b)->d.get();
    }

    public R apply(T1 a, T2 b);

    default Function2<T1, T2,  Maybe<R>> lazyLift(){
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
    default Function2<T1, T2, Option<R>> lift(){
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



        default <V> Function2<T1,T2, V> apply(final BiFunction<? super T1,? super T2,? extends Function<? super R,? extends V>> applicative) {
            return (a,b) -> applicative.apply(a,b).apply(this.apply(a,b));
        }

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


        default Function2<Seq<T1>,Seq<T2>, Seq<R>> seqZip() {
            return (a,b) -> a.zip(b,this);
        }

        default Function2<Seq<T1>,Seq<T2>, Seq<R>> seqM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default Function2<LazySeq<T1>,LazySeq<T2>, LazySeq<R>> lazySeqZip() {
            return (a,b) -> a.zip(b,this);
        }

        default Function2<LazySeq<T1>,LazySeq<T2>, LazySeq<R>> linkedSeqM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default Function2<Vector<T1>,Vector<T2>, Vector<R>> vectorZip() {
            return (a,b) -> a.zip(b,this);
        }


    }

}
