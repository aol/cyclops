package cyclops.function;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import cyclops.control.*;
import cyclops.data.LazySeq;
import cyclops.data.Seq;


import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;

@FunctionalInterface
public interface Function0<R> extends Supplier<R> {

    public static <R> Function0< R> λ(final Supplier<R> triFunc){
    return ()->triFunc.get();
  }

    public static <R> Function0< R> λ(final Function0<R> triFunc){
        return triFunc;
    }
    public static <R> Function0<? extends R> λv(final Function0<? extends R> triFunc){
        return triFunc;
    }

    default <R2> R2 toType(Function<? super Function0<R>, ? extends R2> reduce){
    return reduce.apply(this);
  }

    default Future<R> future(Executor ex){
        return Future.of(CompletableFuture.supplyAsync(this,ex));
    }
    default Eval<R> toEval(){
        return Eval.later(this);
    }
    default R apply(){
        return get();
    }
    default Function0<Maybe<R>> lift(){
        return ()-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply())));
    }

    default Function0<Future<R>> lift(Executor ex){
       return ()-> Future.of(()->apply(),ex);
    }
    default Function0<   Try<R,Throwable>> liftTry(){
       return ()->  Try.withCatch(()->apply(),Throwable.class);
    }
    default Function0<Option<R>> liftOpt(){
       return ()-> Option.ofNullable(apply());
    }



    default Function0<R> memoize(){
        return Memoize.memoizeSupplier(this);
    }
    default Function0<R> memoize(Cacheable<R> c){
        return Memoize.memoizeSupplier(this,c);
    }
    default Function0<R> memoizeAsync(ScheduledExecutorService ex, String cron){
        return Memoize.memoizeSupplierAsync(this,ex,cron);
    }
    default Function0<R> memoizeAsync(ScheduledExecutorService ex, long timeToLiveMillis){
        return Memoize.memoizeSupplierAsync(this,ex,timeToLiveMillis);
    }
    default ReactiveSeq<R> stream() {
        return ReactiveSeq.of(get());
    }



    /**
     * Use the value stored in this Value to seed a Stream generated from the provided function
     *
     * @param fn Function to generate a Stream
     * @return Stream generated from a seed value (the Value stored in this Value) and the provided function
     */
    default ReactiveSeq<R> iterate(final UnaryOperator<R> fn) {
        return ReactiveSeq.iterate(get(), fn);
    }

    /**
     * @return A Stream that repeats the value stored in this Value over and over
     */
    default ReactiveSeq<R> generate() {
        return ReactiveSeq.generate(this);
    }


    default <V> Function0<V> andThen(Function<? super R, ? extends V> after) {
        return () -> after.apply(get());
    }
    default <R1> R1 fnTo(Function<? super Function0<R>,? extends R1> reduce){
        return reduce.apply(this);
    }
    default <V> Function0<V> apply(final Supplier<? extends Function<? super R,? extends V>> applicative) {
      return () -> applicative.get().apply(this.apply());
    }

    default Function0.FunctionalOperations<R> functionOps(){
        return ()->get();
    }
    default <R1> Function0<R1> mapFn(final Function<? super R,? extends R1 > f){
      return () -> f.apply(this.apply());
    }
    default <R1> Function0<R1> flatMapFn(final Function<? super R, ? extends Supplier<? extends R1>> f) {
      return () -> f.apply(apply()).get();
    }
    default <R1> Function0<R1> coflatMapFn(final Function<? super Supplier<? super R>, ? extends  R1> f) {
      return () -> f.apply(this);
    }
    interface FunctionalOperations<R> extends Function0<R> {


        default Function0<ReactiveSeq<R>> liftStream() {
            return () -> ReactiveSeq.of(apply());
        }

        default Function0<Future<R>> liftFuture() {
            return () -> Future.ofResult(apply());
        }


        default Function0<Seq<R>> liftSeq() {
            return () -> Seq.of(apply());
        }



        default Function0<LazySeq<R>> liftLazySeq() {
            return () -> LazySeq.of(apply());
        }

        default Function0<Vector<R>> liftVector() {
            return () -> Vector.of(apply());
        }
    }



}
