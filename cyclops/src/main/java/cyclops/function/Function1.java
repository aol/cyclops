package cyclops.function;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.oath.cyclops.hkt.Higher;
import cyclops.collections.immutable.VectorX;
import cyclops.control.*;

import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.DequeX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.control.Future;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import cyclops.typeclasses.functor.Functor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

@FunctionalInterface
public interface Function1<T,  R> extends Function<T,R>{

    public static <T1,  T3,R> Function1<T1, R> of(final Function<T1, R> triFunc){
    return a->triFunc.apply(a);
  }
    public static <T1,  T3,R> Function1<T1, R> λ(final Function1<T1, R> triFunc){
        return triFunc;
    }
    public static <T1,  T3,R> Function1<? super T1,? extends R> λv(final Function1<? super T1,? extends R> triFunc){
        return triFunc;
    }

    default <R2> R2 toType(Function<? super Function1<? super T, ? extends R>, ? extends R2> reduce){
      return reduce.apply(this);
    }

    default Function0<R> applyLazy(T t){
        return ()->apply(t);
    }

    default Eval<R> later(T t){
        return Eval.later(()->apply(t));
    }
    default Eval<R> always(T t){
        return Eval.always(()->apply(t));
    }
    default Eval<R> now(T t){
        return Eval.now(apply(t));
    }

    public R apply(T a);

    /**
     * Apply before advice to this function, capture the input with the provided Consumer
     *
     * @param action LESS advice
     * @return Function with LESS advice attached
     */
    default Function1<T, R> before(final Consumer<? super T> action){
        return FluentFunctions.of(this).before(action);
    }
    /**
     * Apply MORE advice to this function capturing both the input and the emitted with the provided BiConsumer
     *
     * @param action MORE advice
     * @return  Function with MORE advice attached
     */
    default Function1<T, R> after(final BiConsumer<? super T,? super R> action) {
        return FluentFunctions.of(this).after(action);
    }


    default <W1,W2> Function1<Higher<W1,T>,Higher<W2,R>> liftNT(Function<Higher<W1,T>,Higher<W2,T>> hktTransform, Functor<W2> functor){
        return (T1)-> functor.map(this,hktTransform.apply(T1));
    }


    default Function1<T,Maybe<R>> lift(){
       return (T1)-> Maybe.fromLazy(Eval.later(()-> Maybe.ofNullable(apply(T1))));
    }
    default Function1<T, Future<R>> lift(Executor ex){
       return (T1)-> Future.of(()->apply(T1),ex);
    }
    default Function1<T, Try<R,Throwable>> liftTry(){
       return (T1)->  Try.withCatch(()->apply(T1),Throwable.class);
    }
    default Function1<T,   Option<R>> liftOpt(){
       return (T1)-> Option.ofNullable(apply(T1));
    }



    default Function1<T,R> memoize(){
        return Memoize.memoizeFunction(this);
    }
    default Function1<T,R> memoize(Cacheable<R> c){
        return Memoize.memoizeFunction(this,c);
    }
    default Function1<T, R> memoizeAsync(ScheduledExecutorService ex, String cron){
        return Memoize.memoizeFunctionAsync(this,ex,cron);
    }
    default Function1<T, R> memoizeAsync(ScheduledExecutorService ex, long timeToLiveMillis){
        return Memoize.memoizeFunctionAsync(this,ex,timeToLiveMillis);
    }

    default <T2,R2> Function1<Either<T, T2>, Either<R, R2>> merge(Function<? super T2, ? extends R2> fn) {
        Function1<T, Either<R, R2>> first = andThen(Either::left);
        Function<? super T2, ? extends Either<R,R2>> second = fn.andThen(Either::right);
        return first.fanIn(second);

    }

    default <T2> Function1<Either<T, T2>, R> fanIn(Function<? super T2, ? extends R> fanIn) {
        return e ->   e.visit(this, fanIn);
    }
    default <__> Function1<Either<T, __>, Either<R, __>> leftFn() {

        return either->  either.bimap(this,Function.identity());
    }
    default <__> Function1<Either<__, T>, Either<__,R>> rightFn() {

        return either->  either.bimap(Function.identity(),this);
    }


    default <R1> Function1<T,Tuple2<R,R1>> product(Function1<? super T, ? extends R1> fn){
        return in -> Tuple.tuple(apply(in),fn.apply(in));
    }

    default <__> Function1<Tuple2<T, __>, Tuple2<R, __>> firstFn() {

        return t-> Tuple.tuple(apply(t._1()),t._2());
    }
    default <__> Function1<Tuple2<__, T>, Tuple2<__, R>> secondFn() {

        return t-> Tuple.tuple(t._1(),apply(t._2()));
    }




    default <R2,R3> Function1<T, Tuple3<R, R2, R3>> product(Function<? super T, ? extends R2> fn2, Function<? super T, ? extends R3> fn3) {
        return a -> Tuple.tuple(apply(a), fn2.apply(a),fn3.apply(a));
    }
    default <R2,R3,R4> Function1<T, Tuple4<R, R2,R3,R4>> product(Function<? super T, ? extends R2> fn2,
                                                                 Function<? super T, ? extends R3> fn3,
                                                                 Function<? super T, ? extends R4> fn4) {
        return a -> Tuple.tuple(apply(a), fn2.apply(a),fn3.apply(a),fn4.apply(a));
    }


    default Function0<R> bind(final T s) {
        return Curry.curry(this)
                    .apply(s);
    }

    @Override
    default <V> Function1<V, R> compose(Function<? super V, ? extends T> before) {
        return v -> apply(before.apply(v));
    }

    @Override
    default <V> Function1<T, V> andThen(Function<? super R, ? extends V> after) {
        return t -> after.apply(apply(t));
    }

    default <V> Function1<Function<? super R, ? extends V>,Function1<T, V>> andThen() {
        return this::andThen;
    }



    static <T,R> Function1<T,R> narrow(Function<? super T, ? extends R> fn){
        if(fn instanceof Function1){
            return (Function1<T,R>)fn;
        }
        return t->fn.apply(t);
    }
    default FunctionalOperations<T,R> functionOps(){
        return in->apply(in);
    }
    default <V> Function1<T, V> apply(final Function<? super T,? extends Function<? super R,? extends V>> applicative) {
      return a -> applicative.apply(a).apply(this.apply(a));
    }

    default <R1> Function1<T, R1> mapFn(final Function<? super R, ? extends R1> f2) {
      return andThen(f2);
    }

    default <R1> Function1<T, R1> flatMapFn(final Function<? super R, ? extends Function<? super T, ? extends R1>> f) {
      return a -> f.apply(apply(a)).apply(a);
    }
    default <R1> Function1<T,R1> coflatMapFn(final Function<? super Function1<? super T,? extends R>, ? extends  R1> f) {
      return in-> f.apply(this);
    }
    interface FunctionalOperations<T1,R> extends Function1<T1,R> {

        default ListX<R> mapF(ListX<T1> list) {
            return list.map(this);
        }
        default DequeX<R> mapF(DequeX<T1> list) {
            return list.map(this);
        }
        default SetX<R> mapF(SetX<T1> set) {
            return set.map(this);
        }

        default LinkedListX<R> mapF(LinkedListX<T1> list) {
            return list.map(this);
        }

        default VectorX<R> mapF(VectorX<T1> list) {
            return list.map(this);
        }
        default Streamable<R> mapF(Streamable<T1> stream) {
            return stream.map(this);
        }

        default ReactiveSeq<R> mapF(ReactiveSeq<T1> stream) {
            return stream.map(this);
        }
        default Eval<R> mapF(Eval<T1> eval) {
            return eval.map(this);
        }
        default Maybe<R> mapF(Maybe<T1> maybe) {
            return maybe.map(this);
        }
        default <X extends Throwable> Try<R,X> mapF(Try<T1,X> xor) {
            return xor.map(this);
        }
        default <ST> Either<ST,R> mapF(Either<ST,T1> xor) {
            return xor.map(this);
        }
        default <ST> Ior<ST,R> mapF(Ior<ST,T1> ior) {
            return ior.map(this);
        }

        default Future<R> mapF(Future<T1> future) {
            return future.map(this);
        }

        default Function1<T1, ReactiveSeq<R>> liftStream() {
            return in -> ReactiveSeq.of(apply(in));
        }

        default Function1<T1, Future<R>> liftFuture() {
            return in -> Future.ofResult(apply(in));
        }


        default Function1<T1, ListX<R>> liftList() {
            return in -> ListX.of(apply(in));
        }


        default Function1<T1, LinkedListX<R>> liftLinkedListX() {
            return in -> LinkedListX.of(apply(in));
        }

        default Function1<T1, VectorX<R>> liftVectorX() {
            return in -> VectorX.of(apply(in));
        }
    }



}
