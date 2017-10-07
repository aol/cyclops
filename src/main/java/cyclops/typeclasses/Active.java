package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.collections.mutable.ListX;
import cyclops.control.*;
import cyclops.function.*;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.FunctionK;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.SemigroupK;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.MonadPlus;
import cyclops.typeclasses.monad.Traverse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import static cyclops.collections.tuple.Tuple.tuple;

/**
 * Provide easy access to all typeclasses for a type
 * e.g.
 *
 * <pre>
 *     {@code
 *       Active<list,Integer> active = Active.of(ListX.of(1,2,3),ListX.Instances.definitions());
 *       Active<list,Integer> doubled = active.transform(i->i*2);
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
    public static <C,W, T> Active<W, T> of(Function<? super C,? extends Higher<W, T>> single, C c,InstanceDefinitions<W> def1) {
        return of(single.apply(c),def1);
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
     *      Active<list, ListX<Integer>> grouped = active.custom(ListX::narrowK, l -> l.grouped(10));
     *     }
     * </pre>
     *
     * @param narrow Function that narrows Higher Kinded encoding to it's concrete type
     * @param fn Transformation function
     * @param <S> Concrete type
     * @param <R> Return type
     * @return Transformed Active after custom operation
     */
    public <S,R> Active<W,R> custom(Function<? super Higher<W, T>,? extends S> narrow,Function<? super S,? extends Higher<W,R>> fn){
        return Active.of(fn.apply(narrow.apply(single)),def1);

    }

    /**
     * Convert this Active to a new type via the underlying concrete type
     * e.g. Given an Active List
     * <pre>
     *     {@code
     *     Active<list,Integer> active = Active.of(ListX.of(1,2,3), ListX.Instances.definitions());
     *     }
     * </pre>
     *
     * We can convert it to a set via concreteConversion
     *
     * <pre>
     *     {@code
     *      SetX<Integer> set = active.concreteConversion(ListX.<Integer>kindCokleisli())
                                      .to(ListX::toSetX());
     *     }
     * </pre>
     * Most cyclops-react types provide kindCokleisli implementations that convert a Higher Kinded encoding of the type
     * back to the concrete type
     *
     * @param narrow Narrowing function (Cokleisli) to a concrete type
     * @param <S> Concrete type
     * @param <R> Return type
     * @return Converter that works on the concrete type
     */
    public <S,R> Converter<S> concreteConversion(Function<? super Higher<W, T>,? extends S> narrow){
        return  new Converter<S>(){


            @Override
            public <R> R to(Function<S, R> fn) {
                return fn.apply(narrow.apply(single));
            }
        };
    }
    public static interface Converter<S>{
        public <R> R to(Function<S,R> fn);
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

    public <W2> Active<W2, T> mapK(FunctionK<W,W2,T> fn) {
        return of( fn.apply(single), fn.definitions());
    }
    public <R> Active<W, R> map(Function<? super T, ? extends R> fn) {
        return of(def1.functor().map(fn, single), def1);
    }
    public <R> Active<W,R> mapWithIndex(BiFunction<? super T,Long,? extends R> f) {
        return of(def1.traverse().mapWithIndex(f,single),def1);
    }
    public T intercalate(Monoid<T> monoid, T value) {
        return def1.foldable().intercalate(monoid,value,single);
    }

    public boolean anyMatch(Predicate<? super T> pred){
        return def1.foldable().anyMatch(pred,single);
    }
    public boolean allMatch(Predicate<? super T> pred){
        return def1.foldable().allMatch(pred,single);
    }

    public T getAt(int index){
        return toListX().get(index);
    }

    public Active<W,T> reverse(){
        return Active.of(def1.traverse().reverse(single),def1);
    }
    public  ListX<T> toListX(){
        return def1.foldable().listX(single);
    }
    public  ReactiveSeq<T> stream(){
        return toListX().stream();
    }
    public  long size() {
        return def1.foldable().size(single);
    }
    public <R> Active<W,Tuple2<T,Long>> zipWithIndex() {
        return mapWithIndex(Tuple::tuple);
    }

    public <W2,T2,R> Active<W,R> zipWith(Active<W2,T2> a,  BiFunction<? super T,? super Maybe<T2>,? extends R> f) {
        return Active.of(def1.traverse().zipWith(a.def1.foldable(),f,single,a.single),def1);
    }
    public <W2,T2,R> Active<W,R> zipWith(Foldable<W2> foldable,Higher<W2,T2> a,  BiFunction<? super T,? super Maybe<T2>,? extends R> f) {
        return Active.of(def1.traverse().zipWith(foldable,f,single,a),def1);
    }


    public <C> Narrowed<C> concreteMonoid(Kleisli<W,C,T> widen,Cokleisli<W,T,C> narrow){
        return new Narrowed<C>(widen,narrow);
    }

    /**
     * Use concreteFlatMap to access a flatMap operator that works with the concrete type (rather than the higher kind encoding)
     *
     * e.g. using a vavr Vector
     *
     * <pre>
     *     {@code
     *     Active<vector,Integer> vector = Vectors.allTypeclasses(Vector.of(1,2,3));
     *
     *     vector.concreteFlatMap(Vectors.kindKleisli())
                .flatMap(i->Vector.of(1,2,3)); //flatMap accepts Vector rather than Higher<vector,T>
     *
     *     }
     * </pre>
     *
     * Note this is not typically needed for cyclops-react types
     *
     *
     */
    public <C,R> NarrowedFlatMap<C,R> concreteFlatMap(Kleisli<W,C,R> widen){
        return new NarrowedFlatMap<>(widen);
    }
    public <C,R> NarrowedApplicative<C,R> concreteAp(Kleisli<W,C,Function<T,R>> widen){
        return new NarrowedApplicative<>(widen);
    }
    public <C,R> NarrowedTailRec<C,R> concreteTailRec(Kleisli<W,C,Xor<T,R>> widen){
        return new NarrowedTailRec<>(widen);
    }
    @AllArgsConstructor
    class NarrowedFlatMap<C,R>{
        private final Kleisli<W,C,R> narrow;

        public Active<W, R> flatMap(Function<? super T, ? extends C> fn) {
            return Active.this.flatMap(fn.andThen(narrow));
        }
        public <R2> Active<W, R2> zip(C fb, BiFunction<? super T,? super R,? extends R2> f) {
            return Active.this.zip(narrow.apply(fb),f);
        }
        public  Active<W, Tuple2<T,R>> zip(C fb) {
            return Active.this.zip(narrow.apply(fb));
        }
    }
    @AllArgsConstructor
    class NarrowedTailRec<C,R>{
        private final Kleisli<W,C,Xor<T,R>>  narrow;

        public  Active<W, R> tailRec(T initial,Function<? super T,? extends C> fn){
            return Active.of(def1.monadRec().<T,R>tailRec(initial,fn.andThen(r->narrow.apply(r))),def1);
        }
    }

    @AllArgsConstructor
    class NarrowedApplicative<C,R>{
        private final Kleisli<W,C,Function<T,R>>  narrow;

        public  Active<W, R> ap(C fn) {
            return of(def1.applicative().ap(narrow.apply(fn), single), def1);
        }
    }


    @AllArgsConstructor
    class Narrowed<C>{
        //plus, sum

        private final Kleisli<W,C,T> widen;
        private final Cokleisli<W,T,C> narrow;

        public C extract(){
            return narrow.apply(single);
        }
        public Active<W,T> plus(Monoid<C> m,C add){
            return sum(m,ListX.of(add));
        }
        public Active<W,T> sum(C seed, BinaryOperator<C> op,ListX<C> list){
            C res =list.plus(narrow.apply(single)).foldLeft(seed,(a,b)->op.apply(a,b));
            return Active.of(widen.apply(res),def1);
        }
        public Active<W,T> sum(Monoid<C> s,ListX<C> list){
            C res =list.plus(narrow.apply(single)).foldLeft(s.zero(),(a,b)->s.apply(a,b));
            return Active.of(widen.apply(res),def1);
        }
        public Active<W,T> sumInverted(Group<C> s, ListX<C> list){
            C res = s.invert(list.plus(narrow.apply(single)).foldLeft(s.zero(),(a,b)->s.apply(a,b)));
            return Active.of(widen.apply(res),def1);
        }
        public Maybe<Active<W,T>> sum(ListX<C> list){
            return Active.this.plus().flatMap(s ->
                    Maybe.just(sum(narrow.apply(s.monoid().zero()), (C a, C b) -> narrow.apply(s.monoid().apply(widen.apply(a), widen.apply(b))), list))
            );
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
    public <R> Active<W,R> zip(Active<W,T> p2,Active<W,T> p3,Function3<? super T,? super T, ? super T,? extends R> zipper){
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
        return ap(fn.apply(c));
    }
    public Active<W,T> plus(SemigroupK<W,T> semigroupK, Higher<W,T> add){
        return of(semigroupK.apply(single,add),def1);
    }
    public Active<W,T> plus(SemigroupK<W,T> semigroupK, Active<W,T> add){
        return of(semigroupK.apply(single,add.getSingle()),def1);
    }


    public Unfolds unfolds(Unfoldable<W> unfoldable){
        return new Unfolds(unfoldable);
    }

    public Plus plus(MonadPlus<W> plus){
        return new Plus(plus);
    }

    public Unfolds unfoldsDefault(){
        return new Unfolds(def1.unfoldable().visit(p->p,()->new Unfoldable.UnsafeValueUnfoldable<>()));
    }

    @Deprecated
    public Plus plusUnsafe(){
        return new Plus(def1.monadPlus().get());
    }


    public Maybe<Unfolds> unfolds(){
        return def1.unfoldable().visit(e->Maybe.just(new Unfolds(e)),Maybe::none);
    }
    public Maybe<Plus> plus(){
        return def1.monadPlus().visit(e->Maybe.just(new Plus(e)),Maybe::none);
    }

    @AllArgsConstructor
    public class Plus{
        private final MonadPlus<W> monadPlus;
        public MonoidK<W,T> monoidK(){
            return monadPlus.asMonoid();
        }
        public Monoid<Higher<W,T>> monoid(){
            return monadPlus.narrowMonoid();
        }
        public Active<W,T> zero(){
            Higher<W, T> h = monadPlus.narrowZero();
            return of(h, def1);

        }
        public Active<W,T> sum(ListX<Higher<W, T>> list){
            return of(monadPlus.sum(list.plus(single)),def1);
        }
        public Active<W,T> sumA(ListX<Active<W, T>> list){
            return sum(list.map(Active::getActive));
        }
        public Active<W,T> plus(Higher<W, T> a){
            return of(monadPlus.plus(single,a),def1);
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
    @AllArgsConstructor
    public class Unfolds{
        private final Unfoldable<W> unfoldable;

        public <R, T> Active<W, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn){
            return Active.of(unfoldable.unfold(b,fn),def1);
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



    public <R> R foldMap(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
        return def1.foldable().foldMap(mb,fn,single);
    }

    public T foldRight(Monoid<T> monoid) {
        return  def1.foldable().foldRight(monoid, single);
    }


    public T foldRight(T identity, BinaryOperator<T> semigroup) {
        return foldRight(Monoid.fromBiFunction(identity, semigroup));
    }

    public T foldLeft(Monoid<T> monoid) {
        return def1.foldable().foldLeft(monoid, single);
    }


    public T foldLeft(T identity, BinaryOperator<T> semigroup) {
        return foldLeft(Monoid.fromBiFunction(identity, semigroup));
    }

    public  <W2, R> Higher<W2, Higher<W, R>> flatTraverse(Applicative<W2> applicative,
                                                          Function<? super T,? extends Higher<W2, Higher<W, R>>>f) {
        return def1.traverse()
                .flatTraverse(applicative,def1.monad(),single,f);
    }
    public <C2, R> Higher<C2, Active<W,R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn){
        return traverseA(applicative,fn,this);

    }
    public static <W,T,C2, R> Higher<C2, Active<W,R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,Active<W,T> n){

        Traverse<W> traverse = n.def1.traverse();
        Higher<C2, Higher<W, R>> r = traverse.traverseA(applicative,fn,n.single);
        Higher<C2, Active<W,R>> x = applicative.map(nr -> Active.of(nr, n.def1), r);
        return x;

    }
    public  <C2,T> Higher<C2, Active<W,T>> sequenceA(Applicative<C2> applicative,
                                                         Active<W,Higher<C2,T>> ds){
       return traverseA(applicative, i -> i, ds);

    }

    public   <C2, R> Higher<C2, Active<W,R>> flatTraverseA(Applicative<C2> applicative,
                                                              Function<? super T,? extends Higher<C2, Active<W, R>>> f) {
        return applicative.map_(traverseA(applicative, f), it->  it.flatMapA(a->a));
    }

    public  <C2,T> Higher<C2, Active<W,T>> flatSequenceA(Applicative<C2> applicative, Active<W,Higher<C2,Active<W,T>>> fgfa) {
        return applicative.map(i -> i.flatMapA(Function.identity()),sequenceA(applicative, fgfa) );
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

  
    public <T2, R1, R2, R3, R> Active<W,R> forEach4(final Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Higher<W,R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends Higher<W,R3>> value3,
                                                    final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return of(Comprehensions.of(def1.monad()).forEach4(this.single,value1,value2,value3,yieldingFunction),def1);
    }

   
    public <T2, R1, R2, R3, R> Maybe<Active<W,R>> forEach4(final Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Higher<W,R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends Higher<W,R3>> value3, final Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        if(!def1.monadZero().isPresent())
            return Maybe.none();
        return Maybe.just(of(Comprehensions.of(def1.monadZero().get()).forEach4(this.single,value1,value2,value3,filterFunction,yieldingFunction),def1));
    }


    public <T2, R1, R2, R> Active<W,R> forEach3(final Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Higher<W,R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return of(Comprehensions.of(def1.monad()).forEach3(this.single,value1,value2,yieldingFunction),def1);
    }

    public <T2, R1, R2, R> Maybe<Active<W,R>> forEach3(final Function<? super T, ? extends Higher<W,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Higher<W,R2>> value2, final Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
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

    public String show(){
        return def1.show().show(single);
    }
    public String toString(){
        return "Active["+single.toString()+"]";
    }
}
