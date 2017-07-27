package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.immutable.VectorX;
import cyclops.monads.Witness;
import cyclops.monads.Witness.supplier;
import cyclops.monads.function.AnyMFn0;
import cyclops.typeclasses.free.Free;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.functor.Functor;
import org.jooq.lambda.function.Function0;

import cyclops.control.Eval;
import cyclops.async.Future;
import cyclops.control.Maybe;
import cyclops.control.Try;

@FunctionalInterface
public interface Fn0<R> extends Supplier<R>{


    public static <  T3,R> Fn0< R> λ(final Fn0<R> triFunc){
        return triFunc;
    }
    public static <  T3,R> Fn0<? extends R> λv(final Fn0<? extends R> triFunc){
        return triFunc;
    }

    default Eval<R> toEval(){
        return Eval.later(this);
    }
    default R apply(){
        return get();
    }
    default Fn0<Maybe<R>> lift(){
        return ()-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply())));
    }

    default Fn0<Future<R>> lift(Executor ex){
       return ()-> Future.of(()->apply(),ex);
    }
    default Fn0<   Try<R,Throwable>> liftTry(){
       return ()->  Try.withCatch(()->apply(),Throwable.class);
    }
    default Fn0<   Optional<R>> liftOpt(){
       return ()-> Optional.ofNullable(apply());
    }

    default <W extends WitnessType<W>> AnyMFn0<W,R> liftF(W witness){
        return ()-> witness.adapter().unit(this.get());
    }
    
    default Fn0<R> memoize(){
        return Memoize.memoizeSupplier(this);
    }
    default Fn0<R> memoize(Cacheable<R> c){
        return Memoize.memoizeSupplier(this,c);
    }
    default Fn0<R> memoizeAsync(ScheduledExecutorService ex, String cron){
        return Memoize.memoizeSupplierAsync(this,ex,cron);
    }
    default Fn0<R> memoizeAsync(ScheduledExecutorService ex, long timeToLiveMillis){
        return Memoize.memoizeSupplierAsync(this,ex,timeToLiveMillis);
    }


    default <V> Fn0<V> andThen(Function<? super R, ? extends V> after) {
        return () -> after.apply(get());
    }
    default <R1> R1 fnTo(Function<? super Fn0<R>,? extends R1> reduce){
        return reduce.apply(this);
    }
    default Fn0.FunctionalOperations<R> functionOps(){
        return ()->get();
    }
    interface FunctionalOperations<R> extends Fn0<R> {
        default <V> Fn0<V> apply(final Supplier<? extends Function<? super R,? extends V>> applicative) {
            return () -> applicative.get().apply(this.apply());
        }
        default <R1> Fn0<R1> map(final Function<? super R,? extends R1 > f){
            return () -> f.apply(this.apply());
        }
        default <R1> Fn0<R1> flatMap(final Function<? super R, ? extends Supplier<? extends R1>> f) {
            return () -> f.apply(apply()).get();
        }
        default <R1> Fn0<R1> coflatMap(final Function<? super Supplier<? super R>, ? extends  R1> f) {
            return () -> f.apply(this);
        }
        default Free<supplier, R> free(){
            return suspend(() -> Free.done(get()));
        }
        default Fn0<ReactiveSeq<R>> liftStream() {
            return () -> ReactiveSeq.of(apply());
        }

        default Fn0<Future<R>> liftFuture() {
            return () -> Future.ofResult(apply());
        }
        default <W extends WitnessType<W>> Fn0<FutureT<W,R>> liftFutureT(W witness) {
            return liftFuture().andThen(f->f.liftM(witness));
        }

        default Fn0<ListX<R>> liftList() {
            return () -> ListX.of(apply());
        }
        default <W extends WitnessType<W>> Fn0<ListT<W,R>> liftListT(W witness) {
            return liftList().andThen(l->l.liftM(witness));
        }

        default Fn0<LinkedListX<R>> liftPStack() {
            return () -> LinkedListX.of(apply());
        }

        default Fn0<VectorX<R>> liftPVector() {
            return () -> VectorX.of(apply());
        }
    }
    public static <A> Free<supplier, A> suspend(final SupplierKind<Free<supplier, A>> f){
        return Free.suspend(f);
    }
    public static <A> A run(final Free<supplier, A> f){
        return f.go(a -> ((Fn0<Free<supplier, A>>)a).apply(), Fn0.Instances.functor);
    }
    static interface SupplierKind<R> extends Fn0<R>, Higher<supplier,R> {


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
