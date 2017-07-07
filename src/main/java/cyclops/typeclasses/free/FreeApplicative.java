
package cyclops.typeclasses.free;

import com.aol.cyclops2.hkt.Higher;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.monads.Witness;
import cyclops.monads.Witness.free;
import cyclops.typeclasses.NaturalTransformation;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Function;


public interface FreeApplicative<F,A> {
    static <F,A> FreeApplicative<F,A> pure(A a) {
        return new Pure(a);
    }
    static <F,A,P> FreeApplicative<F,A> ap(Higher<F,P> fp,FreeApplicative<F,Function<P,A>> fn){
        return new Ap(fp,fn);
    }
    default <P,B> FreeApplicative<F,B> ap(FreeApplicative<F, Function<A, B>> b){
        return b.<P,FreeApplicative<F,B>>visit(f->this.map(f),
                 (pivot,fn)->ap(pivot,ap(fn.map(fx->a->p->fx.apply(p).apply(a)))));
    }
    default <P,B> FreeApplicative<F,B> map(Function<A,B> f){
            return this.<P,FreeApplicative<F,B>>visit(a->pure(f.apply(a)),
                    (pivot,fn)-> ap(pivot, fn.map(it -> f.compose(it))));
    }
    default <P,G> Higher<G,A> foldMap(NaturalTransformation<F, G> f,Applicative<G> applicative){
        return this.<P,Higher<G,A>>visit(a->applicative.unit(a),
                (pivot,fn)->applicative.map2(f.apply(pivot),fn.foldMap(f,applicative),(a,g)->g.apply(a)));
    }

    default <P> Higher<F,A> fold(Applicative<F> applicative){
        return this.<P, F>foldMap(NaturalTransformation.identity(), applicative);
    }
    default Free<F,A> monad(Applicative<F> applicative){
        return Free.narrowK(foldMap(new NaturalTransformation<F, Higher<free, F>>() {
            @Override
            public <T> Higher<Higher<free, F>, T> apply(Higher<F, T> a) {
                Free<F, T> res = Free.liftF(a, applicative);
                return res;
            }
        }, Free.Instances.applicative(applicative, applicative)));

    }

    static <F,A> FreeApplicative<F,A> lift(Higher<F,A> fa, Applicative<F> applicative) {
        return ap(fa,pure(Lambda.l1(a -> a)));
    }
    
    <P,R> R visit(Function<? super A,? extends R> pure,BiFunction<? super Higher<F,P>,FreeApplicative<F,Function<P,A>>,? extends R> ap);

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
}