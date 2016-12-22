package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
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
import cyclops.Matchables;
import org.jooq.lambda.function.Function2;

@FunctionalInterface
public interface F2<T1, T2, R> extends Function2<T1,T2,R> {

    public static <T1, T2, T3,R> F2<T1,T2, R> λ(final F2<T1,T2, R> triFunc){
        return triFunc;
    }
    public static <T1, T2, T3,R> F2<? super T1,? super T2,? extends R> λv(final F2<? super T1,? super T2,? extends R> triFunc){
        return triFunc;
    }
    
    public R apply(T1 a, T2 b);


    default <W extends WitnessType<W>> MFunction2<W,T1,T2,R> liftF(){
        return AnyM.liftF2(this);
    }
    
    
    default F2<T1, T2,  Maybe<R>> lift(){
        F2<T1, T2,  R> host = this;
       return (T1,T2)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(T1,T2))));
    }
    default F2<T1, T2,  FutureW<R>> lift(Executor ex){
        F2<T1, T2,  R> host = this;
       return (T1,T2)-> FutureW.ofSupplier(()->host.apply(T1,T2),ex);
    }
    default F2<T1, T2,  Try<R,Throwable>> liftTry(){
        F2<T1, T2,  R> host = this;
       return (T1,T2)->  Try.withCatch(()->host.apply(T1,T2),Throwable.class);
    }
    default F2<T1, T2,  Optional<R>> liftOpt(){
        F2<T1, T2,  R> host = this;
       return (T1,T2)-> Optional.ofNullable(host.apply(T1,T2));
    }

    
    default F2<T1,T2,R> memoize(){
        return Memoize.memoizeBiFunction(this);
    }
    default F2<T1,T2,R> memoize(Cacheable<R> c){
        return Memoize.memoizeBiFunction(this,c);
    }
    
    default F1<? super T1,F1<? super T2,? extends  R>> curry(){
        return CurryVariance.curry2(this);
    }
    
    
    default F1<T2, R> apply(final T1 s) {
        return Curry.curry2(this)
                    .apply(s);
    }

    default F2<T2,T1,R> reverse(){
        return (t2,t1)->apply(t1,t2);
    }
    @Override
    default <V> F2<T1, T2, V> andThen(Function<? super R, ? extends V> after) {
        return (t1,t2)-> after.apply(apply(t1,t2));
    }

    default FunctionalOperations<T1,T2,R> functionOps(){
        return (a,b)->apply(a,b);
    }

    interface FunctionalOperations<T1,T2,R> extends F2<T1,T2,R>{
        default <W extends WitnessType<W>> F2<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,R>> anyMZip() {
            return (a,b) -> (AnyM<W,R>)a.zip(b,this);
        }

        /**
        default <W extends WitnessType<W>> F2<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,R>> anyMM() {
            return (a,b) -> Matchables.anyM(a)
                                      .visit(v->v.forEach2(x->b,this),s->s.forEach2(x->b,this));
        }
         **/
        default F2<ReactiveSeq<T1>,ReactiveSeq<T2>, ReactiveSeq<R>> streamZip() {
            return (a,b) -> a.zip(b,this);
        }

        default F2<ReactiveSeq<T1>,ReactiveSeq<T2>, ReactiveSeq<R>> streamM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default F2<FutureW<T1>, FutureW<T2>, FutureW<R>> futureZip() {
            return (a,b) -> a.zip(b,this);
        }
        default F2<FutureW<T1>, FutureW<T2>, FutureW<R>> futureM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default <W extends WitnessType<W>> F2<FutureT<W,T1>,FutureT<W,T2>, FutureT<W,R>> futureTM(W witness) {
            return (a,b) -> a.forEach2M(x->b,this);
        }
        default <W extends WitnessType<W>> F2<FutureT<W,T1>,FutureT<W,T2>, FutureT<W,R>> futureTZip(W witness) {
            return (a,b) -> a.zip(b,this);
        }

        default F2<ListX<T1>,ListX<T2>, ListX<R>> listXZip() {
            return (a,b) -> a.zip(b,this);
        }

        default F2<ListX<T1>,ListX<T2>, ListX<R>> listXM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default F2<PStackX<T1>,PStackX<T2>, PStackX<R>> pstackXZip() {
            return (a,b) -> a.zip(b,this);
        }

        default F2<PStackX<T1>,PStackX<T2>, PStackX<R>> pstackXM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default F2<PVectorX<T1>,PVectorX<T2>, PVectorX<R>> pvectorXZip() {
            return (a,b) -> a.zip(b,this);
        }

        default <W extends WitnessType<W>> F2<ListT<W,T1>,ListT<W,T2>, ListT<W,R>> listTM(W witness) {
            return (a,b) -> a.forEach2M(x->b,this);
        }
        default <W extends WitnessType<W>> F2<ListT<W,T1>,ListT<W,T2>, ListT<W,R>> listTZip(W witness) {
            return (a,b) -> a.zip(b,this);
        }
    }

}
