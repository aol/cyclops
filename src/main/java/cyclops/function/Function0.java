package cyclops.function;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.collectionx.mutable.ListX;
import cyclops.collectionx.immutable.VectorX;
import cyclops.monads.Witness.supplier;
import cyclops.monads.function.AnyMFunction0;
import cyclops.typeclasses.free.Free;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.functor.Functor;


import cyclops.control.lazy.Eval;
import cyclops.async.Future;
import cyclops.control.lazy.Maybe;
import cyclops.control.Try;

@FunctionalInterface
public interface Function0<R> extends Supplier<R>{


    public static <  T3,R> Function0< R> λ(final Function0<R> triFunc){
        return triFunc;
    }
    public static <  T3,R> Function0<? extends R> λv(final Function0<? extends R> triFunc){
        return triFunc;
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
    default Function0<   Optional<R>> liftOpt(){
       return ()-> Optional.ofNullable(apply());
    }

    default <W extends WitnessType<W>> AnyMFunction0<W,R> liftF(W witness){
        return ()-> witness.adapter().unit(this.get());
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
    default Function0.FunctionalOperations<R> functionOps(){
        return ()->get();
    }
    interface FunctionalOperations<R> extends Function0<R> {
        default <V> Function0<V> apply(final Supplier<? extends Function<? super R,? extends V>> applicative) {
            return () -> applicative.get().apply(this.apply());
        }
        default <R1> Function0<R1> map(final Function<? super R,? extends R1 > f){
            return () -> f.apply(this.apply());
        }
        default <R1> Function0<R1> flatMap(final Function<? super R, ? extends Supplier<? extends R1>> f) {
            return () -> f.apply(apply()).get();
        }
        default <R1> Function0<R1> coflatMap(final Function<? super Supplier<? super R>, ? extends  R1> f) {
            return () -> f.apply(this);
        }
        default Free<supplier, R> free(){
            return suspend(() -> Free.done(get()));
        }
        default Function0<ReactiveSeq<R>> liftStream() {
            return () -> ReactiveSeq.of(apply());
        }

        default Function0<Future<R>> liftFuture() {
            return () -> Future.ofResult(apply());
        }
        default <W extends WitnessType<W>> Function0<FutureT<W,R>> liftFutureT(W witness) {
            return liftFuture().andThen(f->f.liftM(witness));
        }

        default Function0<ListX<R>> liftList() {
            return () -> ListX.of(apply());
        }
        default <W extends WitnessType<W>> Function0<ListT<W,R>> liftListT(W witness) {
            return liftList().andThen(l->l.liftM(witness));
        }

        default Function0<LinkedListX<R>> liftPStack() {
            return () -> LinkedListX.of(apply());
        }

        default Function0<VectorX<R>> liftPVector() {
            return () -> VectorX.of(apply());
        }
    }
    public static <A> Free<supplier, A> suspend(final SupplierKind<Free<supplier, A>> f){
        return Free.suspend(f);
    }
    public static <A> A run(final Free<supplier, A> f){
        return f.go(a -> ((Function0<Free<supplier, A>>)a).apply(), Function0.Instances.functor);
    }
    static interface SupplierKind<R> extends Function0<R>, Higher<supplier,R> {


        default <R1> R1 kindTo(Function<? super SupplierKind<R>,? extends R1> reduce){
            return reduce.apply(this);
        }
            default <V> SupplierKind<V> apply(final Supplier<? extends Function<? super R,? extends V>> applicative) {
                return () -> applicative.get().apply(this.apply());
            }
            default <R1> SupplierKind<R1> map(final Function<? super R,? extends R1 > f){
                return () -> f.apply(this.apply());
            }
            default <R1> SupplierKind<R1> flatMap(final Function<? super R, ? extends Supplier<? extends R1>> f) {
                return () -> f.apply(apply()).get();
            }
            default <R1> SupplierKind<R1> coflatMap(final Function<? super Supplier<? super R>, ? extends  R1> f) {
                return () -> f.apply(this);
            }
            default Free<supplier, R> free(){
                return suspend(() -> Free.done(get()));
            }
            default SupplierKind<ReactiveSeq<R>> liftStream() {
                return () -> ReactiveSeq.of(apply());
            }

            default SupplierKind<Future<R>> liftFuture() {
                return () -> Future.ofResult(apply());
            }

            default SupplierKind<ListX<R>> liftList() {
                return () -> ListX.of(apply());
            }


            default SupplierKind<LinkedListX<R>> liftPStack() {
                return () -> LinkedListX.of(apply());
            }

            default SupplierKind<VectorX<R>> liftPVector() {
                return () -> VectorX.of(apply());
            }

    }
    public static class Instances {

        public static final Functor<supplier> functor =
                new Functor<supplier>() {
                    @Override
                    public <T, R> SupplierKind<R> map(Function<? super T, ? extends R> f, Higher<supplier, T> fa) {
                        return ((SupplierKind<T>) fa).map(f);
                    }
                };
    }
}
