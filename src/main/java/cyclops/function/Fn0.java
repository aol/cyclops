package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.hkt.Higher;
import com.aol.cyclops.types.To;
import cyclops.collections.ListX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.free.Free;
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
public interface Fn0< R> extends Function0<R>, Higher<Fn0.µ,R>{

    static class µ {}
    public static <  T3,R> Fn0< R> λ(final Fn0<R> triFunc){
        return triFunc;
    }
    public static <  T3,R> Fn0<? extends R> λv(final Fn0<? extends R> triFunc){
        return triFunc;
    }

    default Fn0<Maybe<R>> lift(){
        return ()-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply())));
    }

    default Fn0<Future<R>> lift(Executor ex){
       return ()-> Future.ofSupplier(()->apply(),ex);
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
        default Free<Fn0.µ, R> free(){
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

        default Fn0<PStackX<R>> liftPStack() {
            return () -> PStackX.of(apply());
        }

        default Fn0<PVectorX<R>> liftPVector() {
            return () -> PVectorX.of(apply());
        }
    }
    public static <A> Free<Fn0.µ, A> suspend(final Fn0<Free<Fn0.µ, A>> f){
        return Free.suspend(f);
    }
    public static <A> A run(final Free<Fn0.µ, A> f){
        return f.go(a -> ((Fn0<Free<Fn0.µ, A>>)a).apply(), Fn0.Instances.functor);
    }
    static class Instances {
        public static final Functor<Fn0.µ> functor =
                new Functor<µ>() {
                    @Override
                    public <T, R> Fn0<R> map(Function<? super T, ? extends R> f, Higher<µ, T> fa) {
                        return ((Fn0<T>) fa).functionOps().map(f);
                    }
                };
    }
}
