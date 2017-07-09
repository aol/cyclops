package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.collections.mutable.ListX;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Xor;
import cyclops.control.lazy.Either;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.typeclasses.functions.FunctionK;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.SemigroupK;
import cyclops.typeclasses.monad.Applicative;
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
                                    Transformable<T>, To<Active<W,T>> {


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

    /**
     * Perform a custom operation
     *
     * <pre>
     *     {@code
     *       Active<list,Integer> active = Active.of(ListX.of(1,2,3), ListX.Instances.definitions());
     *      Active<list, ListX<Integer>> grouped = active.to(ListX::narrowK, l -> l.grouped(10));
     *     }
     * </pre>
     *
     * @param narrow Function that narrows Higher Kinded encoding to it's concrete type
     * @param fn Transformation function
     * @param <S> Concrete type
     * @param <R> Return type
     * @return Transformed Active after custom operation
     */
    public <S,R> Active<W,R> to(Function<? super Higher<W, T>,? extends S> narrow,Function<? super S,? extends Higher<W,R>> fn){
        return Active.of(fn.apply(narrow.apply(single)),def1);

    }

    public <C,R> R visit(Function<? super Higher<W, T>,? extends C> narrow,Function<? super C,? extends R> visitor){
        return visitor.apply(narrow.apply(single));
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
    public <R> Active<W,Tuple2<T, R>> zip(Higher<W, R> fb) {
        return of(def1.applicative().zip(single,fb),def1);
    }
    /**
    public <R> Active<W,Tuple2<T, R>> zip(Active<W, R> fb) {
        return of(def1.applicative().zip(single,fb.single),def1);
    }
     **/
    public <W2> Active<W2, T> mapK(FunctionK<W,W2,T> fn) {
        return of( fn.apply(single), fn.definitions());
    }
    public <R> Active<W, R> map(Function<? super T, ? extends R> fn) {
        return of(def1.functor().map(fn, single), def1);
    }
    public static void main(String[] args){

        Active<list,Integer> list = ListX.of(1,2,3).allTypeclasses();
        list.<ListX<Xor<Integer,Integer>>,Integer>tailRec(ListX.Instances::widen,1,i-> 1<100_000 ? ListX.of(Xor.secondary(i+1)) : ListX.of(Xor.primary(i)));
        list.<ListX<Integer>>concrete(ListX.Instances::widen);


        Active<list,Integer> a = list.<ListX<Integer>>concrete(ListX.Instances::widen)
                                    .flatMap(i->ListX.of(2));

        Active<list,Integer>.Narrowed<ListX<Integer>> x = list.concrete(ListX.Instances::widen);

        list.concrete((ListX<Integer> in)->{
            ListX<Integer> x = in;
            return ListX.Instances.widen(x);
        });

    }
    public <R1,R> Active<W, R> tailRec(Function<? super R1,? extends Higher<W, Xor<T, R>>> narrow,T initial,Function<? super T,? extends R1> fn){

        return Active.of(def1.monadRec().<T,R>tailRec(initial,fn.andThen(r->narrow.apply(r))),def1);
    }
    public <C> Narrowed<C> concreteOps(Function<C,Higher<W,T>> fn){
        return new Narrowed<C>(fn);
    }
    public <C,R> NarrowedXor<R,C> concreteRec(Function<C,Higher<W, Xor<T, R>>> fn){
        return new NarrowedXor<R,C>(fn);
    }

    @AllArgsConstructor
    class Narrowed<R>{
        // flatMap,plus, sum
        Function<? super R,? extends Higher<W,T>> narrow;


        public  Active<W, T> flatMap(Function<? super T, ? extends R> fn) {
            return of(def1.monad().flatMap(fn.andThen(narrow), single), def1);
        }

        public Active<W,T> plus(SemigroupK<W,T> semigroupK, R add){
            return of(semigroupK.apply(single,narrow.apply(add)),def1);
        }
        public Maybe<Active<W,T>> sum(ListX<R> list){
            return Active.this.plus().flatMap(s->Maybe.just(s.sum(list.map(narrow))));
        }

        public Maybe<Active<W,T>> plus(R r){
            return Active.this.plus().flatMap(s->Maybe.just(s.plus(narrow.apply(r))));
        }

    }
    @AllArgsConstructor
    class NarrowedXor<R,R1>{
        Function<R1,Higher<W, Xor<T, R>>> narrow;
        public  Active<W, R> tailRec(T initial,Function<? super T,? extends R1> fn){

            return Active.of(def1.monadRec().<T,R>tailRec(initial,fn.andThen(r->narrow.apply(r))),def1);
        }
    }
    public <T2, R> Active<W, R> zip(Higher<W, T2> fb, BiFunction<? super T,? super T2,? extends R> f) {
        return of(def1.applicative().zip(single,fb,f),def1);
    }

    public <T2,R> Eval<Active<W,R>> lazyZip(Eval<Higher<W,T2>> lazy, BiFunction<? super T,? super T2,? extends R> fn) {
        return lazy.map(e-> zip(e,fn));
    }
    public <T2,R> Eval<Active<W,R>> lazyZipA(Eval<Active<W,T2>> lazy, BiFunction<? super T,? super T2,? extends R> fn) {
        return lazy.map(e->zip(e.getSingle(),fn));
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
    public <C,R> Active<W, R> ap(C c,Function<? super C, ? extends Higher<W, ? extends Function<T, R>>> fn) {
        return return ap(fn.apply(c));
    }
    public Active<W,T> plus(SemigroupK<W,T> semigroupK, Higher<W,T> add){
        return of(semigroupK.apply(single,add),def1);
    }
    public Active<W,T> plus(SemigroupK<W,T> semigroupK, Active<W,T> add){
        return of(semigroupK.apply(single,add.getSingle()),def1);
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
        return new Folds();
    }
    public Maybe<Folds> folds(){
        return def1.foldable().visit(e->Maybe.just(new Folds()),Maybe::none);
    }
    public Maybe<Traverse> traverse(){
        return def1.traverse().visit(e->Maybe.just(new Traverse()),Maybe::none);
    }
    public Plus plusUnsafe(){
        return new Plus();
    }
    public Maybe<Plus> plus(){
        return def1.foldable().visit(e->Maybe.just(new Plus()),Maybe::none);
    }

    public class Plus{

        public MonoidK<W,T> monoidK(){
            return def1.monadPlus().get().asMonoid();
        }
        public Monoid<Higher<W,T>> monoid(){
            return def1.monadPlus().get().narrowMonoid();
        }
        public Active<W,T> zero(){
            Higher<W, T> h = def1.monadZero().get().narrowZero();
            return of(h, def1);

        }
        public Active<W,T> sum(ListX<Higher<W, T>> list){
            return of(def1.monadPlus().visit(p->p.sum(list.plus(single)),()->single),def1);
        }
        public Active<W,T> sumA(ListX<Active<W, T>> list){
            return sum(list.map(Active::getActive));
        }
        public Active<W,T> plus(Higher<W, T> a){
            return of(def1.monadPlus().visit(p->p.plus(single,a),()->single),def1);
        }
        public Active<W,T> plusA(Active<W, T> ac){
            Higher<W, T> a =ac.single;
            return plus(a);
        }
    }


    public <R> Active<W, R> tailRec(T initial,Function<? super T,? extends Higher<W, ? extends Xor<T, R>>> fn){
        return Active.of(def1.monadRec().<T,R>tailRec(initial,fn),def1);
    }
    public <R> Active<W, R> tailRecA(T initial,Function<? super T,? extends Active<W, ? extends Xor<T, R>>> fn){
        return Active.of(def1.monadRec().<T,R>tailRec(initial,fn.andThen(Active::getActive)),def1);
    }

    public class Unfolds{
        public <R, T> Active<W, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn){
            return Active.of(def1.unfoldable().get().unfold(b,fn),def1);
        }

        public <T> Active<W, T> replicate(long n, T value) {
            return unfold(n,i -> i>0? Optional.of(tuple(value, i<Long.MAX_VALUE? i-1 : i)) : Optional.empty());
        }
        public <R> Nested<W, W,R> replicate(Function<? super T,Long> fn, Function<? super T,R> mapper) {
            return Nested.of(def1.functor().map(value->replicate(fn.apply(value), mapper.apply(value)).getSingle(),single),def1,def1);
        }
        public Nested<W, W,T> replicate(long n) {
            return Nested.of(def1.functor().map(value->replicate(n,value).getSingle(),single),def1,def1);
        }
        public <T> Active<W, T> cycle(T value) {
            return replicate(Long.MAX_VALUE, value);
        }

        public <R> Active<W,R> none() {
            return unfold((T) null, t -> Optional.<Tuple2<R, T>>empty());
        }
        public <T> Active<W,T> one(T a) {
            return replicate(1, a);
        }

    }

    public class Folds {

        public <R> R foldMap(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
            return def1.foldable().visit(p->p.foldMap(mb,fn,single),()->mb.zero());
        }

        public T foldRight(Monoid<T> monoid) {
            return  def1.foldable().visit(p -> p.foldRight(monoid, single), () -> monoid.zero());
        }


        public T foldRight(T identity, BinaryOperator<T> semigroup) {
            return foldRight(Monoid.fromBiFunction(identity, semigroup));
        }

        public T foldLeft(Monoid<T> monoid) {
            return def1.foldable().visit(p -> p.foldLeft(monoid, single), () -> monoid.zero());
        }


        public T foldLeft(T identity, BinaryOperator<T> semigroup) {
            return foldLeft(Monoid.fromBiFunction(identity, semigroup));
        }

    }

    public class Traverse{
        public  <W2, R> Higher<W2, Higher<W, R>> flatTraverse(Applicative<W2> applicative,
                                                               Function<? super T,? extends Higher<W2, Higher<W, R>>>f) {
            return def1.traverse()
                       .get()
                       .flatTraverse(applicative,def1.monad(),single,f);
        }
    }

    public <W2> Product<W,W2,T> concat(Active<W2,T> active){
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
