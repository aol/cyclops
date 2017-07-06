package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.function.*;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.Traverse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 * Provide easy access to all typeclasses for a type
 * e.g.
 *
 * <pre>
 *     {@code
 *       Active<list,Integer> active = Active.of(ListX.of(1,2,3),ListX.Instances.definitions());
 *       Active<list,Integer> doubled = active.map(i->i*2);
 *       Active<list,Integer> doubledPlusOne = doubled.flatMap(i->ListX.of(i+1));
 *     }
 *
 * </pre>
 *
 * @param <W> Witness type
 * @param <T> Data type
 */
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@EqualsAndHashCode(of={"single"})
public class Active<W,T> implements Filters<T>,
                                    Transformable<T> {


    @Getter
    private final Higher<W, T> single;
    @Getter
    private final InstanceDefinitions<W> def1;

    public static <W, T> Active<W, T> of(Higher<W, T> single, InstanceDefinitions<W> def1) {
        return new Active<>(single, def1);
    }
    public static <W, T> Active<W, T> of(InstanceDefinitions<W> def1,T value) {
        return new Active<>(def1.unit().unit(value), def1);
    }

    public <R> R visit(Function<? super Higher<W, T>,? extends R> visitor){
        return visitor.apply(single);
    }
    public <R> R visitA(Function<? super Active<W, T>,? extends R> visitor){
        return visitor.apply(this);
    }
    public Higher<W, T> getActive() {
        return single;
    }

    public <R> Active<W, R> unit(R value) {
        return of(def1.unit().unit(value), def1);
    }

    public Active<W, T> filter(Predicate<? super T> predicate) {
        return of(def1.monadZero().visit(s -> s.filter(predicate, single), () -> single), def1);
    }

    public <R> Active<W, R> map(Function<? super T, ? extends R> fn) {
        return of(def1.functor().map(fn, single), def1);
    }

    public Active<W, T> peek(Consumer<? super T> fn) {
        return of(def1.functor().peek(fn, single), def1);
    }

    public <R> Function<Active<W, T>, Active<W, R>> lift(final Function<? super T, ? extends R> fn) {
        return t -> of(def1.functor().map(fn, t.single), def1);
    }
    public Active<W,Tuple2<T,T>> zip(Active<W,T> p2){

        return zip(p2, Tuple::tuple);
    }
    public <R> Active<W,R> zip(Active<W,T> p2,BiFunction<? super T,? super T, ? extends R> zipper){
        Applicative<W> ap = def1.applicative();

        Function<T, Function<T, R>> fn = a->b->zipper.apply(a,b);
        Higher<W, Function<T, Function<T, R>>> hfn = ap.unit(fn);
        return of(ap.ap(ap.ap(hfn,single),p2.getSingle()),def1);
    }
    public Active<W,Tuple3<T,T,T>> zip(Active<W,T> p2, Active<W,T> p3){

        return zip(p2, p3,Tuple::tuple);
    }
    public <R> Active<W,R> zip(Active<W,T> p2,Active<W,T> p3,Fn3<? super T,? super T, ? super T,? extends R> zipper){
        Applicative<W> ap = def1.applicative();

        Function<T, Function<T,Function<T, R>>> fn = a->b->c->zipper.apply(a,b,c);
        Higher<W, Function<T, Function<T,Function<T, R>>>> hfn = ap.unit(fn);
        return of(ap.ap(ap.ap(ap.ap(hfn,single),p2.getSingle()),p3.getSingle()),def1);
    }

    public <R> Active<W, R> flatMap(Function<? super T, ? extends Higher<W, R>> fn) {
        return of(def1.monad().flatMap(fn, single), def1);
    }
    public <R> Active<W, R> flatMapA(Function<? super T, ? extends Active<W, R>> fn) {
        return of(def1.monad().flatMap(fn.andThen(Active::getActive), single), def1);
    }

    public <R> Active<W, R> ap(Higher<W, ? extends Function<T, R>> fn) {
        return of(def1.applicative().ap(fn, single), def1);
    }
    public Traverse traverseUnsafe(){
        return def1.traverse().visit(s-> new Traverse(),()->null);
    }
    public Unfolds unfoldsUnsafe(){
        return def1.unfoldable().visit(s-> new Unfolds(),()->null);
    }
    public Maybe<Unfolds> unfolds(){
        return def1.unfoldable().visit(e->Maybe.just(new Unfolds()),Maybe::none);
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

    public class Unfolds{
        public <R, T> Active<W, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn){
            return Active.of(def1.unfoldable().get().unfold(b,fn),def1);
        }

        public <T> Active<W, T> replicate(int n, T value) {
            return unfold(n,i -> Optional.of(tuple(value, i-1)));
        }

        public <R> Active<W,R> none() {
            return unfold((T) null, t -> Optional.<Tuple2<R, T>>empty());
        }
        public <T> Active<W,T> one(T a) {
            return replicate(1, a);
        }

    }

    public class Folds {


        public T foldRight(Monoid<T> monoid) {
            return def1.foldable().get().foldRight(monoid, single);
        }


        public T foldRight(T identity, BinaryOperator<T> semigroup) {
            return def1.foldable().get().foldRight(Monoid.fromBiFunction(identity, semigroup), single);
        }

        public T foldLeft(Monoid<T> monoid) {
            return def1.foldable().get().foldLeft(monoid, single);
        }


        public T foldLeft(T identity, BinaryOperator<T> semigroup) {
            return def1.foldable().get().foldLeft(identity, semigroup, single);
        }

    }

    public class Traverse{
        public  <W2, R> Higher<W2, Higher<W, R>> flatTraverse(Applicative<W2> applicative,
                                                               Function<? super T,? extends Higher<W2, Higher<W, R>>>f) {
            return def1.traverse().get().flatTraverse(applicative,def1.monad(),single,f);

        }

    }

    public <W2> Product<W,W2,T> plus(Active<W2,T> active){
        return Product.of(this,active);
    }

    @Override
    public <U> Active<W,U> cast(Class<? extends U> type) {
        return (Active<W,U>)Transformable.super.cast(type);
    }

    @Override
    public <U> Active<W,U> ofType(Class<? extends U> type) {
        return (Active<W,U>)Filters.super.ofType(type);
    }

    @Override
    public Active<W,T> filterNot(Predicate<? super T> predicate) {
        return (Active<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public Active<W,T> notNull() {
        return (Active<W,T>)Filters.super.notNull();
    }

    @Override
    public <R> Active<W,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Active<W,R>)Transformable.super.trampoline(mapper);
    }

    @Override
    public <R> Active<W,R> retry(Function<? super T, ? extends R> fn) {
        return (Active<W,R>)Transformable.super.retry(fn);
    }

    @Override
    public <R> Active<W,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (Active<W,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }

  
    public <T2, R1, R2, R3, R> Active<W,R> forEach4(final Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Higher<W,R2>> value2, final Fn3<? super T, ? super R1, ? super R2, ? extends Higher<W,R3>> value3, 
                                                    final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return of(Comprehensions.of(def1.monad()).forEach4(this.single,value1,value2,value3,yieldingFunction),def1);
    }

   
    public <T2, R1, R2, R3, R> Maybe<Active<W,R>> forEach4(final Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Higher<W,R2>> value2, final Fn3<? super T, ? super R1, ? super R2, ? extends Higher<W,R3>> value3, final Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        if(!def1.monadZero().isPresent())
            return Maybe.none();
        return Maybe.just(of(Comprehensions.of(def1.monadZero().get()).forEach4(this.single,value1,value2,value3,filterFunction,yieldingFunction),def1));
    }


    public <T2, R1, R2, R> Active<W,R> forEach3(final Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Higher<W,R2>> value2, final Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return of(Comprehensions.of(def1.monad()).forEach3(this.single,value1,value2,yieldingFunction),def1);
    }

    public <T2, R1, R2, R> Maybe<Active<W,R>> forEach3(final Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Higher<W,R2>> value2, final Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction, final Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        if(!def1.monadZero().isPresent())
            return Maybe.none();
        return Maybe.just(of(Comprehensions.of(def1.monadZero().get()).forEach3(this.single,value1,value2,filterFunction,yieldingFunction),def1));
    }


    public <R1, R> Active<W,R> forEach2(Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return of(Comprehensions.of(def1.monad()).forEach2(this.single,value1,yieldingFunction),def1);
    }


    public <R1, R> Maybe<Active<W,R>> forEach2(Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, Boolean> filterFunction, final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        if(!def1.monadZero().isPresent())
            return Maybe.none();
        return Maybe.just(of(Comprehensions.of(def1.monadZero().get()).forEach2(this.single,value1,filterFunction,yieldingFunction),def1));
    }

    public String toString(){
        return "Active["+single.toString()+"]";
    }
}
