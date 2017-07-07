
package cyclops.typeclasses.free;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import cyclops.companion.Functions;
import cyclops.function.Lambda;
import cyclops.monads.Witness.free;
import cyclops.monads.Witness.freeAp;
import cyclops.typeclasses.NaturalTransformation;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Function;

//FreeApplicative refs : = https://github.com/typelevel/cats/blob/master/free/src/main/scala/cats/free/FreeApplicative.scala
public interface FreeApplicative<F, T> extends Higher2<freeAp,F, T> {
    static <F, T> FreeApplicative<F, T> pure(T value) {
        return new Pure(value);
    }
    static <F, T,P> FreeApplicative<F, T> ap(Higher<F,P> fp, FreeApplicative<F,Function<P, T>> fn){
        return new Ap(fp,fn);
    }
    default <P, R> FreeApplicative<F, R> ap(FreeApplicative<F, ? extends Function<T, R>> b){
        return b.<P,FreeApplicative<F, R>>visit(f->this.map(f),
                 (pivot,fn)->ap(pivot,ap(fn.map(fx->a->p->fx.apply(p).apply(a)))));
    }
    default <P, R> FreeApplicative<F, R> map(Function<? super T,? extends R> f){
            return this.<P,FreeApplicative<F, R>>visit(a->pure(f.apply(a)),
                    (pivot,fn)-> ap(pivot, fn.map(it -> {
                        Function<P,? extends R> x = f.compose(it);
                        return Functions.narrow(x);
                    })));
    }
    default <P,G> Higher<G, T> foldMap(NaturalTransformation<F, G> f, Applicative<G> applicative){
        return this.<P,Higher<G, T>>visit(a->applicative.unit(a),
                (pivot,fn)->applicative.map2(f.apply(pivot),fn.foldMap(f,applicative),(a,g)->g.apply(a)));
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
        }, Free.Instances.applicative(applicative, applicative)));

    }
    default <P,G> FreeApplicative<G, T> compile(NaturalTransformation<F, G> f, Applicative<G> applicative){
        return FreeApplicative.narrowK(foldMap(new NaturalTransformation<F, Higher<freeAp, G>>() {

            @Override
            public <T> Higher<Higher<freeAp, G>, T> apply(Higher<F, T> a) {
                return FreeApplicative.lift(f.apply(a),applicative);
            }
        }, FreeApplicative.Instances.applicative(applicative, applicative)));
    }
    static <F,A> FreeApplicative<F,A> lift(Higher<F,A> fa, Applicative<F> applicative) {
        return ap(fa,pure(Lambda.l1(a -> a)));
    }


    <P,R> R visit(Function<? super T,? extends R> pure, BiFunction<? super Higher<F,P>,FreeApplicative<F,Function<P, T>>,? extends R> ap);

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Pure<F, A> implements FreeApplicative<F,A>{
        private final  A a;

        @Override
        public <P,R> R visit(Function<? super A, ? extends R> pure, BiFunction<? super Higher<F, P>, FreeApplicative<F, Function<P, A>>, ? extends R> ap) {
            return pure.apply(a);
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Ap<F, P, A> implements FreeApplicative<F,A>{
        private final Higher<F,P> pivot;
        private final FreeApplicative<F,Function<P,A>> fn;

        @Override
        public <P,R> R visit(Function<? super A, ? extends R> pure, BiFunction<? super Higher<F, P>, FreeApplicative<F, Function<P, A>>, ? extends R> ap) {
           return (R)ap.apply((Higher)pivot, (FreeApplicative) fn);
        }
    }
    static <F,T> FreeApplicative<F,T> narrowK(Higher<Higher<freeAp, F>, T> ds){
        return (FreeApplicative<F,T>)ds;
    }

    static  class Instances {
        public static <F> Applicative<Higher<freeAp, F>> applicative(cyclops.typeclasses.Pure<F> pure,Functor<F> functor) {
            return new Applicative<Higher<freeAp, F>>() {

                @Override
                public <T, R> Higher<Higher<freeAp, F>, R> ap(Higher<Higher<freeAp, F>, ? extends Function<T, R>> fn, Higher<Higher<freeAp, F>, T> apply) {
                    FreeApplicative<F, ? extends Function<T, R>> f = narrowK(fn);
                    FreeApplicative<F, T> a = narrowK(apply);
                    return a.ap(f);

                }

                @Override
                public <T, R> Higher<Higher<freeAp, F>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<freeAp, F>, T> ds) {
                    return narrowK(ds).map(fn);
                }

                @Override
                public <T> Higher<Higher<freeAp, F>, T> unit(T value) {
                    return FreeApplicative.pure(value);
                }
            };


        }
    }
}