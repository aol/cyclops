package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Xor;
import cyclops.function.Monoid;
import cyclops.typeclasses.monad.Applicative;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;
import java.util.function.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Coproduct<W1,W2,T> implements  Filters<T>,
                                            Transformable<T>{

    private final Xor<Higher<W1,T>,Higher<W2,T>> xor;
    private final InstanceDefinitions<W1> def1;
    private final InstanceDefinitions<W2> def2;



    public static  <W1,W2,T> Coproduct<W1,W2,T> of(Xor<Higher<W1,T>,
            Higher<W2,T>> xor,InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2){
        return new Coproduct<>((Xor)xor,def1,def2);
    }
    
    public static  <W1,W2,T> Coproduct<W1,W2,T> right(Higher<W2,T> right,InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2){
        return new Coproduct<>(Xor.primary(right),def1,def2);
    }
    public static  <W1,W2,T> Coproduct<W1,W2,T> left(Higher<W1,T> left,InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2){
        return new Coproduct<>(Xor.secondary(left),def1,def2);
    }
    
    public Coproduct<W1,W2,T> filter(Predicate<? super T> test) {
        return of(xor.map(m -> def2.<T, T>monadZero().visit(s->s.filter(test, m),()->m))
               .secondaryMap(m -> def1.<T, T>monadZero().visit(s->s.filter(test, m),()->m)),def1,def2);
    }

    public <R>  Coproduct<W1,W2,R> coflatMap(final Function<? super  Coproduct<W1,W2,T>, R> mapper){
        return visit(leftM ->  left(def1.unit()
                        .unit(mapper.apply(this)),def1,def2),
                    rightM -> right(def2.unit()
                            .unit(mapper.apply(this)),def1,def2));
    }

    @Override
    public <U> Coproduct<W1,W2,U> ofType(Class<? extends U> type) {
        return (Coproduct<W1,W2,U>)Filters.super.ofType(type);
    }

    @Override
    public Coproduct<W1,W2,T> filterNot(Predicate<? super T> predicate) {
        return filter(predicate.negate());
    }

    @Override
    public Coproduct<W1,W2,T> notNull() {
        return (Coproduct<W1,W2,T>)Filters.super.notNull();
    }

    @Override
    public <U>  Coproduct<W1,W2,U> cast(Class<? extends U> type) {
        return (Coproduct<W1,W2,U>)Transformable.super.cast(type);
    }

    @Override
    public <R>  Coproduct<W1,W2,R> map(Function<? super T, ? extends R> fn) {

        return of(xor.map(m->{
            Higher<W2, ? extends R> x = def2.<T, R>functor().map(fn, m);
            return (Higher<W2, R>)x;
        }).secondaryMap(m->{
            Higher<W1, ? extends R> x = def1.<T, R>functor().map(fn, m);
            return (Higher<W1, R>)x;
        }),def1,def2);
    }


    @Override
    public  Coproduct<W1,W2,T> peek(Consumer<? super T> c) {
        return map(a->{
            c.accept(a);
            return a;
        });
    }

    @Override
    public String toString() {
        return "Coproduct["+xor.toString()+"]";
    }

    @Override
    public <R>  Coproduct<W1,W2,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Coproduct<W1,W2,R>)Transformable.super.trampoline(mapper);
    }

    @Override
    public <R>  Coproduct<W1,W2,R> retry(Function<? super T, ? extends R> fn) {
        return (Coproduct<W1,W2,R>)Transformable.super.retry(fn);
    }

    @Override
    public <R>  Coproduct<W1,W2,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (Coproduct<W1,W2,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }



    public <R> R visit(Function<? super Higher<W1,? super T>, ? extends R> left,Function<? super Higher<W2,? super T>, ? extends R> right ){
        return xor.visit(left,right);
    }

    public Coproduct<W2,W1,T> swap(){
        return of(xor.swap(),def2,def1);
    }

    public Folds foldsUnsafe(){
        return def1.foldable().visit(s-> new Folds(),()->null);
    }
    public Maybe<Folds> folds(){
        return def1.foldable().visit(e->Maybe.just(new Folds()),Maybe::none);
    }
    public Maybe<Traverse> traverse(){
        return def1.traverse().visit(e->Maybe.just(new Traverse()),Maybe::none);
    }
    public class Folds {


        public T foldRight(Monoid<T> monoid) {
            return xor.visit(left->def1.foldable().get().foldRight(monoid, left),
                    right->def2.foldable().get().foldRight(monoid, right));

        }


        public T foldRight(T identity, BinaryOperator<T> semigroup) {
            return xor.visit(left->def1.foldable().get().foldRight(Monoid.fromBiFunction(identity, semigroup), left),
                    right->def2.foldable().get().foldRight(Monoid.fromBiFunction(identity, semigroup), right));

        }

        public T foldLeft(Monoid<T> monoid) {
            return xor.visit(left->def1.foldable().get().foldLeft(monoid, left),
                    right->def2.foldable().get().foldLeft(monoid, right));
        }


        public T foldLeft(T identity, BinaryOperator<T> semigroup) {
            return xor.visit(left->def1.foldable().get().foldLeft(Monoid.fromBiFunction(identity, semigroup), left),
                    right->def2.foldable().get().foldLeft(Monoid.fromBiFunction(identity, semigroup), right));
        }

    }

    public class Traverse{

        public <W3, R> Higher<W3,Coproduct<W1,W2, R>> traverse(Applicative<W3> applicative, Function<? super T, Higher<W3, R>> f){
            return xor.visit(it->{
                return applicative.map(x->of(Xor.secondary(x),def1,def2),def1.traverse().get().traverseA(applicative, f, it));
            },it->{
                return applicative.map(x->of(Xor.primary(x),def1,def2),def2.traverse().get().traverseA(applicative, f, it));
            });
        }


    }

}
