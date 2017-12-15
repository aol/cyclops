
package cyclops.free;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import cyclops.companion.Functions;
import cyclops.function.Lambda;
import com.oath.cyclops.hkt.DataWitness.free;
import com.oath.cyclops.hkt.DataWitness.freeAp;
import cyclops.instances.free.FreeApInstances;
import cyclops.instances.free.FreeInstances;
import cyclops.typeclasses.NaturalTransformation;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Function;

//FreeAp refs : = https://github.com/typelevel/cats/blob/master/free/src/main/scala/cats/free/FreeApplicative.scala
public interface FreeAp<F, T> extends Higher2<freeAp,F, T> {
    static <F, T> FreeAp<F, T> pure(T value) {
        return new Pure(value);
    }
    static <F, T,P> FreeAp<F, T> ap(Higher<F,P> fp, FreeAp<F,Function<P, T>> fn){
        return new Ap(fp,fn);
    }
    default <P, R> FreeAp<F, R> ap(FreeAp<F, ? extends Function<T, R>> b){
        return b.<P,FreeAp<F, R>>visit(f->this.map(f),
                 (pivot,fn)->ap(pivot,ap(fn.map(fx->a->p->fx.apply(p).apply(a)))));
    }
    default <P, R> FreeAp<F, R> map(Function<? super T,? extends R> f){
            return this.<P,FreeAp<F, R>>visit(a->pure(f.apply(a)),
                    (pivot,fn)-> ap(pivot, fn.map(it -> {
                        Function<P,? extends R> x = f.compose(it);
                        return Functions.narrow(x);
                    })));
    }
    default <P,G> Higher<G, T> foldMap(NaturalTransformation<F, G> f, Applicative<G> applicative){
        return this.<P,Higher<G, T>>visit(a->applicative.unit(a),
                (pivot,fn)->applicative.zip(f.apply(pivot),fn.foldMap(f,applicative),(a, g)->g.apply(a)));
    }

    default <P> Higher<F, T> fold(Applicative<F> applicative){
        return this.<P, F>foldMap(NaturalTransformation.identity(), applicative);
    }
    default Free<F, T> monad(Applicative<F> applicative){
        return Free.narrowK(foldMap(new NaturalTransformation<F, Higher<free, F>>() {
            @Override
            public <T> Higher<Higher<free, F>, T> apply(Higher<F, T> a) {
                Free<F, T> res = Free.liftF(a, applicative);
                return res;
            }
        }, FreeInstances.applicative(applicative, applicative)));

    }
    default <P,G> FreeAp<G, T> compile(NaturalTransformation<F, G> f, Applicative<G> applicative){
        return FreeAp.narrowK(foldMap(new NaturalTransformation<F, Higher<freeAp, G>>() {

            @Override
            public <T> Higher<Higher<freeAp, G>, T> apply(Higher<F, T> a) {
                return FreeAp.lift(f.apply(a),applicative);
            }
        }, FreeApInstances.applicative(applicative, applicative)));
    }
    static <F,A> FreeAp<F,A> lift(Higher<F,A> fa, Applicative<F> applicative) {
        return ap(fa,pure(Lambda.l1(a -> a)));
    }


    <P,R> R visit(Function<? super T,? extends R> pure, BiFunction<? super Higher<F,P>,FreeAp<F,Function<P, T>>,? extends R> ap);

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Pure<F, A> implements FreeAp<F,A> {
        private final  A a;

        @Override
        public <P,R> R visit(Function<? super A, ? extends R> pure, BiFunction<? super Higher<F, P>, FreeAp<F, Function<P, A>>, ? extends R> ap) {
            return pure.apply(a);
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Ap<F, P, A> implements FreeAp<F,A> {
        private final Higher<F,P> pivot;
        private final FreeAp<F,Function<P,A>> fn;

        @Override
        public <P,R> R visit(Function<? super A, ? extends R> pure, BiFunction<? super Higher<F, P>, FreeAp<F, Function<P, A>>, ? extends R> ap) {
           Higher<F,P> p = (Higher)pivot;
            return (R)ap.apply((Higher<F, P>)pivot, (FreeAp) fn);
         }
    }
    static <F,T> FreeAp<F,T> narrowK(Higher<Higher<freeAp, F>, T> ds){
        return (FreeAp<F,T>)ds;
    }


}
