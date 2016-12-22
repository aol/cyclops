package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.aol.cyclops.control.*;
import com.aol.cyclops.control.monads.transformers.FutureT;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.anyM.WitnessType;
import org.jooq.lambda.function.Function0;
import org.jooq.lambda.function.Function1;
import org.jooq.lambda.tuple.Tuple1;

@FunctionalInterface
public interface F1<T1,  R> extends Function1<T1,R> {

    public static <T1,  T3,R> F1<T1, R> λ(final F1<T1, R> triFunc){
        return triFunc;
    }
    public static <T1,  T3,R> F1<? super T1,? extends R> λv(final F1<? super T1,? extends R> triFunc){
        return triFunc;
    }

    default Reader<T1,R> reader(){
        return in->apply(in);
    }
    public R apply(T1 a);


    default F1<T1,Maybe<R>> lift(){
       return (T1)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(T1))));
    }
    default F1<T1,   FutureW<R>> lift(Executor ex){
       return (T1)-> FutureW.ofSupplier(()->apply(T1),ex);
    }
    default F1<T1,   Try<R,Throwable>> liftTry(){
       return (T1)->  Try.withCatch(()->apply(T1),Throwable.class);
    }
    default F1<T1,   Optional<R>> liftOpt(){
       return (T1)-> Optional.ofNullable(apply(T1));
    }

    default <W extends WitnessType<W>> MFunction1<W,T1,R> liftF(){
        return AnyM.liftF(this);
    }

    
    default F1<T1,R> memoize(){
        return Memoize.memoizeFunction(this);
    }
    default F1<T1,R> memoize(Cacheable<R> c){
        return Memoize.memoizeFunction(this,c);
    }



    default F0<R> bind(final T1 s) {
        return Curry.curry(this)
                    .apply(s);
    }

    @Override
    default <V> F1<V, R> compose(Function<? super V, ? extends T1> before) {
        return v -> apply(before.apply(v));
    }

    @Override
    default <V> F1<T1, V> andThen(Function<? super R, ? extends V> after) {
        return t -> after.apply(apply(t));
    }

    default <V> F1<Function<? super R, ? extends V>,F1<T1, V>> andThen() {
        return this::andThen;
    }



    default FunctionalOperations<T1,R> functionOps(){
        return in->apply(in);
    }

    interface FunctionalOperations<T1,R> extends F1<T1,R>{

        default <V> F1<T1, V> apply(final Function<? super T1,? extends Function<? super R,? extends V>> applicative) {
            return a -> applicative.apply(a).apply(this.apply(a));
        }

        default <R1> F1<T1, R1> mapFn1(final Function<? super R, ? extends R1> f2) {
            return andThen(f2);
        }

        default <R1> F1<T1, R1> flatMapFn1(final Function<? super R, ? extends Function<? super T1, ? extends R1>> f) {
            return a -> f.apply(apply(a)).apply(a);
        }

        default <W extends WitnessType<W>> AnyM<W, R> mapF(AnyM<W, T1> functor) {
            return functor.map(this);
        }
        default <W extends WitnessType<W>> FutureT<W,R> mapF(FutureT<W,T1> future) {
            return future.map(this);
        }
        default <W extends WitnessType<W>> ListT<W,R> mapF(ListT<W,T1> list) {
            return list.map(this);
        }
        default ListX<R> mapF(ListX<T1> list) {
            return list.map(this);
        }
        default DequeX<R> mapF(DequeX<T1> list) {
            return list.map(this);
        }
        default SetX<R> mapF(SetX<T1> set) {
            return set.map(this);
        }

        default PStackX<R> mapF(PStackX<T1> list) {
            return list.map(this);
        }

        default PVectorX<R> mapF(PVectorX<T1> list) {
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
        default <ST> Xor<ST,R> mapF(Xor<ST,T1> xor) {
            return xor.map(this);
        }
        default <ST> Ior<ST,R> mapF(Ior<ST,T1> ior) {
            return ior.map(this);
        }

        default FutureW<R> mapF(FutureW<T1> futureW) {
            return futureW.map(this);
        }

        default F1<T1, ReactiveSeq<R>> liftStream() {
            return in -> ReactiveSeq.of(apply(in));
        }

        default F1<T1, FutureW<R>> liftFuture() {
            return in -> FutureW.ofResult(apply(in));
        }
        default <W extends WitnessType<W>> F1<T1, FutureT<W,R>> liftFutureT(W witness) {
            return liftFuture().andThen(f->f.liftM(witness));
        }

        default F1<T1, ListX<R>> liftList() {
            return in -> ListX.of(apply(in));
        }
        default <W extends WitnessType<W>> F1<T1, ListT<W,R>> liftListT(W witness) {
            return liftList().andThen(l->l.liftM(witness));
        }

        default F1<T1, PStackX<R>> liftPStack() {
            return in -> PStackX.of(apply(in));
        }

        default F1<T1, PVectorX<R>> liftPVector() {
            return in -> PVectorX.of(apply(in));
        }
    }
}
