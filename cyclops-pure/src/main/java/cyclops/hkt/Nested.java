package cyclops.hkt;


import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.*;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher3;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Monoids;
import cyclops.control.*;
import cyclops.data.ImmutableList;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Group;
import cyclops.function.Monoid;
import cyclops.instances.control.EitherInstances;
import cyclops.instances.control.FutureInstances;
import cyclops.instances.control.TryInstances;
import cyclops.instances.jdk.CompletableFutureInstances;
import cyclops.instances.jdk.OptionalInstances;
import cyclops.instances.jdk.StreamInstances;
import cyclops.instances.reactive.collections.immutable.VectorXInstances;
import cyclops.instances.reactive.collections.mutable.ListXInstances;
import cyclops.kinds.CompletableFutureKind;
import cyclops.kinds.OptionalKind;
import cyclops.kinds.StreamKind;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.Comprehensions;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.SemigroupK;
import cyclops.typeclasses.functor.Compose;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import cyclops.transformers.Transformer;
import cyclops.transformers.TransformerFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Class for working with Nested Data Structures.
 *
 * E.g. to work with a List of Optionals
 * <pre>
 *     {@code
 *      import Witness.list;
        import Witness.optional;

 *      Nested<list,optional,Integer> listOfOptionalInt = Nested.of(ListX.of(Optionals.OptionalKind.of(2)),ListX.Instances.definitions(),Optionals.Instances.definitions());
 *      //Nested[List[Optional[2]]]
 *     }
 *
 * </pre>
 *
 * Transform nest data
 * <pre>
 *     {@code
 *     Nested<list,optional,Integer> listOfOptionalInt;  //Nested[List[Optional[2]]]
 *     Nested<list,optional,Integer> doubled = listOfOptionalInt.map(i->i*2);
 *      //Nested[List[Optional[4]]]
 *     }
 *
 *
 * </pre>
 *
 * Sequencing data
 * <pre>
 *     {@code
 *     Nested<list,optional,Integer> listOfOptionalInt;  //Nested[List[Optional[2]]]
 *     Nested<optional,list,Integer> sequenced = listOfOptionalInt.sequence();
 *     //Nested[Optional[List[2]]]
 *
 *     }
 *
 *
 * </pre>
 *
 *
 * @param <W1> First Witness type {@see Witness}
 * @param <W2> Second Witness type {@see Witness}
 * @param <T> Nested Data Type
 */
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@EqualsAndHashCode(of={"nested"})
public class Nested<W1,W2,T> implements Transformable<T>,
                                        Higher3<nested,W1,W2,T>,To<Nested<W1,W2,T>> {



    public final Higher<W1,Higher<W2,T>> nested;
    private final Compose<W1,W2> composedFunctor;

    public final InstanceDefinitions<W1> def1;

    public final InstanceDefinitions<W2> def2;





    public Transformer<W1,W2,T> transformer(TransformerFactory<W1,W2> factory){
        return factory.build(this);
    }


    public static <W1,W2,T> Nested<W1,W2,T> of(Higher<W1,? extends  Higher<W2,? extends T>> nested,InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2){
        Compose<W1,W2> composed = Compose.compose(def1.functor(),def2.functor());
        return new Nested<>(narrow(nested),composed,def1,def2);
    }
    public static <W1,W2,T> Nested<W1,W2,T> of(Active<W1,? extends  Higher<W2,? extends T>> nested,InstanceDefinitions<W2> def2){
        Compose<W1,W2> composed = Compose.compose(nested.getDef1().functor(),def2.functor());
        return new Nested<>(narrow(nested.getActive()),composed,nested.getDef1(),def2);
    }
    public static <W1,W2,T> Higher<W1,Higher<W2,T>> narrow(Higher<W1,? extends  Higher<W2,? extends T>> nested){
        return (Higher<W1,Higher<W2,T>>) nested;
    }

    public static <W,T> Active<W,T> flatten(Nested<W,W,T> nested){
        return Active.of(nested.def1.monad().flatMap(i->i,nested.nested),nested.def1);
    }

    public Higher<W1, Higher<W2, T>> getNested() {
        return nested;
    }

    public <R> R fold(Function<? super Higher<W1, Higher<W2, T>>, ? extends R> fn){
        return fn.apply(nested);
    }

    public <R> Active<W1,R> pure1(R value){
        return Active.of(def1.unit().unit(value),def1);
    }
    public <R> Nested<W1,W2,R> pure2(R value){
        return Nested.of(def1.unit().unit(def2.unit().unit(value)),def1,def2);
    }

    public <R> Nested<W1,W2,R> map(Function<? super T,? extends R> fn){
        Higher<W1, Higher<W2, R>> res = composedFunctor.map(fn, nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }
    public  Active<W1,ListX<T>> toListX(){
        return Active.of(def1.functor().map(i->def2.foldable().listX(i),nested),def1);
    }
    public  ListX<T> toListXBoth(){
        return toListX().foldLeft(Monoids.listXConcat());
    }
    public Active<W1,ReactiveSeq<T>> stream(){
        return toListX().map(i->i.stream());
    }
    public ReactiveSeq<T> streamBoth(){
        return stream().foldLeft(Monoids.combineReactiveSeq());
    }
    public  Active<W1,Long> size() {
        return Active.of(def1.functor().map(i->def2.foldable().size(i),nested),def1);
    }
    public Nested<W1,W2,T> reverse(){
       return Nested.of(def1.traverse().reverse( def1.functor().map(i->def2.traverse().reverse(i),nested)),def1,def2);
    }
    public  long totalSize() {
        return size().foldLeft(Monoids.longSum);
    }
    public <R> Nested<W1,W2,R> mapWithIndex(BiFunction<? super T,Long,? extends R> f) {
        return of(composedFunctor.mapWithIndex(f,nested),def1,def2);
    }
    public <R> Nested<W1,W2,Tuple2<T,Long>> zipWithIndex() {
        return mapWithIndex(Tuple::tuple);
    }

    public  Nested<W1,W2,T> peek(Consumer<? super T> fn){
        Higher<W1, Higher<W2, T>> res = composedFunctor.peek(fn, nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }

    public <R> Function<Nested<W1,W2,T>, Nested<W1,W2,R>> lift(final Function<? super T, ? extends R> fn) {
        return t -> map(fn);
    }

    public <R> Nested<W1,W2,R> ap(Higher<W2,? extends Function<T, R>> fn){
        Higher<W1, Higher<W2, R>> res = def1.functor().map(a -> def2.applicative().ap(fn, a), nested);
        return of(res,def1,def2);
    }

    public <R> Nested<W1,W2,R> flatMap(Function<? super T, ? extends Higher<W2,R>> fn){
        Higher<W1, Higher<W2, R>> res = composedFunctor.map1(a->def2.monad().flatMap(fn, a),nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }

    public <R,X> Nested<W1,W2,R> flatMap(Function<? super X,? extends Higher<W2,R>> widenFn,Function<? super T,? extends X> fn){
        Higher<W1, Higher<W2, R>> res = composedFunctor.map1(a->def2.monad().flatMap(fn.andThen(widenFn), a),nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }
    public <T2, R> Nested<W1,W2, R> zip(Higher<W2, T2> fb, BiFunction<? super T,? super T2,? extends R> f) {
        return of(def1.functor().map_(nested, i -> def2.applicative().zip(i, fb, f)),def1,def2);

    }
    public <T2, R> Nested<W1,W2, Tuple2<T,T2>> zip(Higher<W2, T2> fb) {
       return zip(fb,Tuple::tuple);
    }
    public <C> Narrowed<C> concreteMonoid(Kleisli<W2,C,T> widen, Cokleisli<W2,T,C> narrow){
        return new Narrowed<C>(widen,narrow);
    }
    public <C,R> NarrowedFlatMap<C,R> concreteFlatMap(Kleisli<W2,C,R> widen){
        return new NarrowedFlatMap<>(widen);
    }

    public <C,R> NarrowedApplicative<C,R> concreteAp(Kleisli<W2,C,Function<T,R>> widen){
        return new NarrowedApplicative<>(widen);
    }

    public <C,R> NarrowedTailRec<C,R> concreteTailRec(Kleisli<W2,C,Either<T,R>> widen){
        return new NarrowedTailRec<>(widen);
    }
    public <S,R> Converter<W1,S> concreteConversion(Function<? super Higher<W2, T>,? extends S> narrow2){
        return  new Converter<W1,S>(){
            @Override
            public <R> Active<W1,R> to(Function<S, R> fn) {
                return Active.of(def1.functor().map(f -> fn.apply(narrow2.apply(f)), nested),def1);
            }
        };
    }
    public static interface Converter<W,S>{
        public <R> Active<W,R> to(Function<S,R> fn);
    }
    @AllArgsConstructor
    class NarrowedFlatMap<C,R>{
        private final Kleisli<W2,C,R> widen;

        public Nested<W1,W2,R> flatMap(Function<? super T, ? extends C> fn) {
            return Nested.this.flatMap(fn.andThen(widen));
        }
        public <R2> Nested<W1,W2,R2> zip(C fb, BiFunction<? super T,? super R,? extends R2> f) {
            return Nested.this.zip(widen.apply(fb),f);
        }
        public  Nested<W1,W2,Tuple2<T,R>> zip(C fb) {
            return Nested.this.zip(widen.apply(fb));
        }
    }
    @AllArgsConstructor
    class NarrowedTailRec<C,R>{
        private final Kleisli<W2,C,Either<T,R>> widen;

        public  Nested<W1,W2,R> tailRecN(T initial,Function<? super T,? extends C> fn){
            return Nested.this.tailRecN(initial,fn.andThen(widen));
        }
    }
    @AllArgsConstructor
    class NarrowedApplicative<C,R>{
        private final Kleisli<W2,C,Function<T,R>> widen;

        public  Nested<W1,W2, R> ap(C fn) {
            return Nested.this.ap(widen.apply(fn));
        }
    }
    @AllArgsConstructor
    class Narrowed<C>{
        //plus, sum

        private final Kleisli<W2,C,T> widen;
        private final Cokleisli<W2,T,C> narrow;

        public Active<W1,C> extract(){
            return Active.of(def1.functor().map_(nested,f->narrow.apply(f)),def1);
        }
        public Nested<W1,W2,T> plus(Monoid<C> m,C add){
            return sum(m,ListX.of(add));
        }
        public Nested<W1,W2,T> sum(C seed, BinaryOperator<C> op,ListX<C> list){
            return of(def1.functor().map_(nested,f-> {
                C res = list.plus(narrow.apply(f)).foldLeft(seed, (a, b) -> op.apply(a, b));
                return widen.apply(res);
            }),def1,def2);
        }
        public Nested<W1,W2,T> sum(Monoid<C> s,ListX<C> list){
            return of(def1.functor().map_(nested,f-> {
                C res = list.plus(narrow.apply(f)).foldLeft(s.zero(), (a, b) -> s.apply(a, b));
                return widen.apply(res);
            }),def1,def2);
        }
        public Nested<W1,W2,T> sumInverted(Group<C> s, ListX<C> list){
            return of(def1.functor().map_(nested,f-> {
            C res = s.invert(list.plus(narrow.apply(f)).foldLeft(s.zero(),(a,b)->s.apply(a,b)));
            return widen.apply(res);
            }),def1,def2);
        }
        public Maybe<Nested<W1,W2,T>> sum(ListX<C> list){
            return Nested.this.plus().flatMap(s ->
                    Maybe.just(sum(narrow.apply(s.monoid().zero()), (C a, C b) -> narrow.apply(s.monoid().apply(widen.apply(a), widen.apply(b))), list))
            );
        }

    }

    public <R> Nested<W1,W2,R> flatMapA(Function<? super T, ? extends Active<W2,R>> fn){
        Higher<W1, Higher<W2, R>> res = composedFunctor.map1(a->def2.monad().flatMap(fn.andThen(t->t.getSingle()), a),nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }

    public <R> Nested<W1,W2, R> tailRecN(T initial,Function<? super T,? extends Higher<W2, ? extends Either<T, R>>> fn){
        return flatMapA(in->Active.of(def2.unit().unit(in),def2).tailRec(initial,fn));
    }
    public <R> Nested<W1,W2, R> tailRec(T initial,Function<? super T,? extends Nested<W1,W2, ? extends Either<T, R>>> fn){
        return narrowK(Instances.monadRec(def1, def2).tailRec(initial, fn));
    }



    public Unfolds unfolds(Unfoldable<W2> unf){
        return  new Unfolds(unf);
    }
    public Plus plus(MonadPlus<W1> plus1, MonadPlus<W2> plus2){
        return new Plus(plus1,plus2);
    }



    public Unfolds unfoldsUnsafe(){
        return def2.unfoldable().visit(s-> new Unfolds(s),()->new Unfolds(new Unfoldable.UnsafeValueUnfoldable<>()));
    }
    private Plus plusUnsafe(){
        return new Plus(def1.monadPlus().orElse(null),def2.monadPlus().orElse(null));
    }




    public Maybe<Unfolds> unfolds(){
        return def2.unfoldable().visit(s-> Maybe.just(new Unfolds(s)),Maybe::nothing);
    }


    public Nested<W1,W2,T> plusNested(SemigroupK<W2> semigroupK, Higher<W2,T> add){
        return of(def1.functor().map(i -> semigroupK.apply(i, add),nested), def1, def2);
    }

    public Maybe<Plus> plus(){
        if(def1.monadPlus().isPresent() && def2.monadPlus().isPresent()){
            return Maybe.just(plusUnsafe());
        }
        return Maybe.nothing();
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Plus{
        private final  MonadPlus<W1> plus1;
        private final  MonadPlus<W2> plus2;
        public Monoid<Higher<W2,T>> monoid(){
            return def2.monadPlus().orElse(null).monoid().asMonoid();
        }
        public Nested<W1,W2,T> sum(ImmutableList<Nested<W1,W2, T>> list){
            return of(plus1.sum(list.plus(Nested.this).map(x -> x.nested)),def1,def2);
        }

        public Nested<W1,W2,T> plus(Higher<W2, T> b){
            Functor<W1> f = def1.functor();
            MonadPlus<W2> mp = plus2;
            Higher<W1, Higher<W2, T>> x = f.map(a -> mp.plus(a, b), nested);
            return of(x,def1,def2);
        }
        public Nested<W1,W2,T> plus(Nested<W1,W2, T> b){
            Monad<W1> f = def1.monad();
            MonadPlus<W2> mp = plus2;
            Higher<W1, Higher<W2, T>> x2 = f.flatMap(a -> {
                Nested<W1, W2, T> r = plus(a);
                return r.nested;
            }, b.nested);
            return of(x2,def1,def2);
        }

    }

    public <R> R foldMapBoth(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
        return def1.foldable().foldRight(mb,foldMap(mb,fn));
    }
    public T foldBothl(T identity, BinaryOperator<T> semigroup){
        return  def1.foldable().foldLeft(identity,semigroup,foldLeft(Monoid.fromBiFunction(identity, semigroup)));
    }
    public T foldBothr(T identity, BinaryOperator<T> semigroup){

        return  def1.foldable().foldRight(identity,semigroup,foldRight(Monoid.fromBiFunction(identity, semigroup)));
    }
    public  T foldBothRight(Monoid<T> monoid){
        return def1.foldable().foldRight(monoid,foldRight(monoid));

    }
    public  T foldBothLeft(Monoid<T> monoid){
        return def1.foldable().foldLeft(monoid,foldLeft(monoid));

    }

    public <R> R foldRight(Monoid<T> monoid, Function<? super Higher<W1,T>,? extends R> narrowK){
        return narrowK.apply(foldRight(monoid));
    }
    public  <R> R foldLeft(Monoid<T> monoid, Function<? super Higher<W1,T>,? extends R> narrowK){
        return narrowK.apply(foldLeft(monoid));
    }


    public Active<W1,T> foldl(T identity, BinaryOperator<T> semigroup){
        return  foldl(Monoid.fromBiFunction(identity, semigroup));
    }
    public Active<W1,T> foldr(T identity, BinaryOperator<T> semigroup){
        return foldr(Monoid.fromBiFunction(identity, semigroup));
    }
    public Active<W1,T> foldl(Monoid<T> monoid){
        return Active.of(foldLeft(monoid),def1);
    }
    public Active<W1,T> foldr(Monoid<T> monoid){
        return Active.of(foldRight(monoid),def1);
    }
    public <R> Active<W1,R> foldMapA(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
        return Active.of(foldMap(mb, fn), def1);
    }


    public <R> Higher<W1,R> foldMap(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
        return def1.functor().map(a -> def2.foldable().foldMap(mb, fn,a), nested);
    }

    public  Higher<W1,T> foldRight(Monoid<T> monoid){
        return def1.functor().map(a -> def2.foldable().foldRight(monoid, a), nested);
    }
    public  Higher<W1,T> foldLeft(Monoid<T> monoid){
        return def1.functor().map(a -> def2.foldable().foldLeft(monoid, a), nested);
    }

    public Higher<W1,T> foldLeft(T identity, BinaryOperator<T> semigroup){
        return foldLeft(Monoid.fromBiFunction(identity, semigroup));
    }
    public Higher<W1,T> foldRight(T identity, BinaryOperator<T> semigroup){
        return foldRight(Monoid.fromBiFunction(identity, semigroup));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Unfolds{

        private final Unfoldable<W2> unfold2;

        public <R> Nested<W1,W2, R> unfold(Function<? super T, Option<Tuple2<R, T>>> fn){
            Unfoldable<W2> unf = unfold2;
            Higher<W1, Higher<W2, R>> x = def1.functor().map(a -> def2.monad().flatMap(c -> unf.unfold(c, fn), a), nested);
            return Nested.of(x,def1,def2);
        }
        private <T2> Nested<W1,W2, T> unfoldPrivate(T2 b,Function<T2, Option<Tuple2<T, T2>>> fn){
            Unfoldable<W2> unf = unfold2;
            Higher<W1, Higher<W2, T>> x = def1.functor().map(a -> def2.monad().flatMap(c -> unf.unfold(b, fn.andThen(o->o.map(t->t.map1(v->c)))), a), nested);
            return Nested.of(x,def1,def2);
        }

        private <T,R> Nested<W1,W2, R> unfoldIgnore(T b,Function<T, Option<Tuple2<R, T>>> fn){
            Unfoldable<W2> unf = unfold2;
            Higher<W1, Higher<W2, R>> x = def1.functor().map(a -> def2.monad().flatMap(c -> unf.unfold(b, fn), a), nested);
            return Nested.of(x,def1,def2);
        }

        public <R> Nested<W1,W2, R> replaceWith(int n, R value) {
            return this.<Integer,R>unfoldIgnore(n, i-> Option.of(Tuple.tuple(value, i - 1)));
        }

        public  Nested<W1,W2, T> replicate(int n) {
            return this.<Integer>unfoldPrivate(n, i-> Option.some(Tuple.tuple(null, i - 1)));
        }


        public <R> Nested<W1,W2, R> none() {
            return unfold(t -> Option.<Tuple2<R, T>>none());
        }
        public <R> Nested<W1,W2, R> replaceWith(R a) {
            return replaceWith(1, a);
        }
    }

    public <C2, R> Higher<C2, Nested<W1,W2,R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn){
        Nested<W1, W2, T> n = this;
        ComposedTraverse<W1,W2> ct = ComposedTraverse.of(n.def1.traverse(),n.def2.traverse(),n.def2.applicative());
        Higher<C2, Higher<W1, Higher<W2, R>>> r = ct.traverse(applicative,fn,n.nested);
        Higher<C2, Nested<W1,W2,R>> x = applicative.map(nr -> Nested.of(nr, n.def1, n.def2), r);
        return x;

    }
    public  <C2,T> Higher<C2, Nested<W1,W2,T>> sequenceA(Applicative<C2> applicative,
                                                     Nested<W1,W2,Higher<C2,T>> ds){
        Higher<C2, Higher<Higher<Higher<nested, W1>, W2>, T>> x = Instances.traverseA(applicative, a -> a, ds);
        return (Higher)x;

    }

    public   <C2, R> Higher<C2, Nested<W1,W2,R>> flatTraverse(Applicative<C2> applicative,
                                                                Function<? super T,? extends Higher<C2, Nested<W1,W2, R>>> f, TransformerFactory<W1,W2> factory) {
        return applicative.map_(traverseA(applicative, f), it->  it.transformer(factory).flatMap(a->a));
    }

    public  <C2,T> Higher<C2, Nested<W1,W2,T>> flatSequence(Applicative<C2> applicative, Nested<W1,W2,Higher<C2,Nested<W1,W2,T>>> fgfa,TransformerFactory<W1,W2> factory) {
        return applicative.map(i -> i.transformer(factory).flatMap(Function.identity()),sequenceA(applicative, fgfa) );
    }
    public Nested<W2, W1, T> sequence(){
        Higher<W2, Higher<W1, T>> res = def1.traverse().sequenceA(def2.applicative(), nested);
        return of(res,def2,def1);
    }
    public  <R> Nested<W2, W1, R> traverse(Function<? super T,? extends R> fn){
        return sequence().map(fn);
    }



    public String toString(){
        return "Nested["+nested.toString()+"]";
    }

    @Override
    public <R> Nested<W1,W2,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Nested<W1,W2,R>)Transformable.super.trampoline(mapper);
    }

    @Override
    public <R> Nested<W1,W2,R> retry(Function<? super T, ? extends R> fn) {
        return (Nested<W1,W2,R>)Transformable.super.retry(fn);
    }

    @Override
    public <R> Nested<W1,W2,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (Nested<W1,W2,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }


    public static <T> Nested<completableFuture,stream,T> completableFutureStream(CompletableFuture<? extends Stream<T>> optionalList){
        CompletableFutureKind<StreamKind<T>> opt = CompletableFutureKind.widen(optionalList.thenApply(StreamKind::widen));
        Higher<completableFuture,Higher<stream,T>> hkt = (Higher)opt;
        return of(hkt, CompletableFutureInstances.definitions(), StreamInstances.definitions());
    }
    public static <T> Nested<optional,stream,T> optionalStream(Optional<? extends Stream<T>> optionalList){
        OptionalKind<StreamKind<T>> opt = OptionalKind.widen(optionalList).map(StreamKind::widen);
        Higher<optional,Higher<stream,T>> hkt = (Higher)opt;
        return of(hkt, OptionalInstances.definitions(), StreamInstances.definitions());
    }

    public static <T> Nested<optional,list,T> optionalList(Optional<? extends List<T>> optionalList){
        OptionalKind<ListX<T>> opt = OptionalKind.widen(optionalList).map(ListX::fromIterable);
        Higher<optional,Higher<list,T>> hkt = (Higher)opt;
        return of(hkt, OptionalInstances.definitions(), ListXInstances.definitions());
    }
    public static <T, X extends Throwable> Nested<future,Higher<tryType,X>,T> futureTry(Future<? extends Try<T,X>> futureTry){
        Higher<future,Higher<Higher<tryType,X>,T>> hkt = (Higher)futureTry;
        return of(hkt, FutureInstances.definitions(), TryInstances.definitions());
    }
    public static <T, X extends Throwable> Nested<list,Higher<tryType,X>,T> listTry(List<? extends Try<T,X>> futureTry){
        Higher<list,Higher<Higher<tryType,X>,T>> hkt = (Higher)futureTry;
        return of(hkt, ListXInstances.definitions(), TryInstances.definitions());
    }
    public static <L,R> Nested<list,Higher<either,L>,R> listXor(List<? extends Either<L,R>> listXor){
        Higher<list,Higher<Higher<either,L>,R>> hkt = (Higher)listXor;
        return of(hkt, ListXInstances.definitions(), EitherInstances.definitions());
    }
    public static <L,R> Nested<future,Higher<either,L>,R> futureXor(Future<? extends Either<L,R>> futureXor){
        Higher<future,Higher<Higher<either,L>,R>> hkt = (Higher)futureXor;
        return of(hkt, FutureInstances.definitions(), EitherInstances.definitions());
    }
    public static <T> Nested<future,list,T> futureList(Future<? extends List<T>> futureList){
        return of(futureList.map(ListX::fromIterable), FutureInstances.definitions(), ListXInstances.definitions());
    }
    public static <T> Nested<future,vectorX,T> futureVector(Future<VectorX<T>> futureList){
        Higher<future,Higher<vectorX,T>> hkt = (Higher)futureList;
        return of(hkt, FutureInstances.definitions(), VectorXInstances.definitions());
    }
    public static <W1,W2,T> Nested<W1,W2,T> narrowK(Higher<Higher<Higher<nested, W1>, W2>, T> ds){
        return (Nested<W1,W2,T>)ds;
    }
    public Option<NestedComprehensions.Guarded<W1, W2, T>> comprehensionsGuarded(TransformerFactory<W1,W2> factory){
        InstanceDefinitions<Higher<Higher<nested, W1>, W2>> defs = definitions(def1,def2,factory);
        return defs.monadZero().map(z->
                NestedComprehensions.of(this,z)
        );
    }

    public NestedComprehensions<W1,W2,T> comprehensions(TransformerFactory<W1,W2> factory){
        InstanceDefinitions<Higher<Higher<nested, W1>, W2>> defs = definitions(def1,def2,factory);
        return NestedComprehensions.of(this,defs.monad());

    }
    public Option<Comprehensions.Guarded<Higher<Higher<DataWitness.nested, W1>, W2>>> comprehensionsGuardedHk(TransformerFactory<W1,W2> factory){
        InstanceDefinitions<Higher<Higher<nested, W1>, W2>> defs = definitions(def1,def2,factory);
        return defs.monadZero().map(z->
            Comprehensions.of(z)
        );
    }

    public Comprehensions<Higher<Higher<nested, W1>, W2>> comprehensionsHk(TransformerFactory<W1,W2> factory){
        InstanceDefinitions<Higher<Higher<nested, W1>, W2>> defs = definitions(def1,def2,factory);
        return Comprehensions.of(defs.monad());

    }

    public Active<Higher<Higher<nested, W1>, W2>,T> allTypeclasses(TransformerFactory<W1,W2> factory){
        return Active.of(this,definitions(def1,def2,factory));
    }
    public InstanceDefinitions<Higher<Higher<nested, W1>, W2>> definitions(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2,TransformerFactory<W1,W2> factory){
        return definitions(def1,def2,factory,Maybe.nothing());
    }
    public InstanceDefinitions<Higher<Higher<nested, W1>, W2>> definitions(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2,TransformerFactory<W1,W2> factory,Maybe<Higher<W2,?>> zero){
        return new InstanceDefinitions<Higher<Higher<nested, W1>, W2>>() {
            @Override
            public <T, R> Functor<Higher<Higher<nested, W1>, W2>> functor() {
                return Instances.functor();
            }

            @Override
            public <T> Option<Unfoldable<Higher<Higher<DataWitness.nested, W1>, W2>>> unfoldable() {
                return Maybe.just(Instances.unfoldable(def1,def2));
            }

            @Override
            public <T> Pure<Higher<Higher<nested, W1>, W2>> unit() {
                return Instances.unit(def1,def2);
            }

            @Override
            public <T, R> Applicative<Higher<Higher<nested, W1>, W2>> applicative() {
                return Instances.applicative(def1,def2,factory);
            }

            @Override
            public <T, R> Monad<Higher<Higher<nested, W1>, W2>> monad() {
                return Instances.monad(def1,def2,factory);
            }

            @Override
            public <T, R> Option<MonadZero<Higher<Higher<DataWitness.nested, W1>, W2>>> monadZero() {
                return zero.map(z->Instances.monadZero(def1,def2,factory,z));
            }

            @Override
            public <T> Option<MonadPlus<Higher<Higher<DataWitness.nested, W1>, W2>>> monadPlus() {
                return Maybe.nothing();
            }

            @Override
            public <T> MonadRec<Higher<Higher<nested, W1>, W2>> monadRec() {
                return Instances.monadRec(def1,def2);
            }

            @Override
            public <T> Option<MonadPlus<Higher<Higher<DataWitness.nested, W1>, W2>>> monadPlus(MonoidK<Higher<Higher<DataWitness.nested, W1>, W2>> m) {
                return Maybe.nothing();
            }

            @Override
            public <C2, T> Traverse<Higher<Higher<nested, W1>, W2>> traverse() {
                return Instances.traverse(def1,def2,factory);
            }

            @Override
            public <T> Foldable<Higher<Higher<nested, W1>, W2>> foldable() {
                return Instances.foldable();
            }

            @Override
            public <T> Option<Comonad<Higher<Higher<DataWitness.nested, W1>, W2>>> comonad() {
                return Maybe.nothing();
            }
        };
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Instances  {



        public static <W1,W2> Functor<Higher<Higher<nested, W1>, W2>> functor() {
            return new Functor<Higher<Higher<nested, W1>, W2>>(){

                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }


        public static <W1,W2> Pure<Higher<Higher<nested, W1>, W2>> unit(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2) {
            return new Pure<Higher<Higher<nested, W1>, W2>>(){

                @Override
                public <T> Higher<Higher<Higher<nested, W1>, W2>, T> unit(T value) {
                    return Nested.of(def1.unit().unit(def2.unit().unit(value)),def1,def2);

                }
            };
        }
        public static <W1,W2> Applicative<Higher<Higher<nested, W1>, W2>> applicative(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2,TransformerFactory<W1,W2> factory) {
            return new Applicative<Higher<Higher<nested, W1>, W2>>() {
                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> ap(Higher<Higher<Higher<nested, W1>, W2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<nested, W1>, W2>, T> apply) {
                    Nested<W1, W2, ? extends Function<T, R>> fnA = narrowK(fn);
                    Nested<W1, W2, T> ap = narrowK(apply);
                    return factory.build(ap).flatMap(t->
                        fnA.map(f->f.apply(t))
                    );
                }

                @Override
                public <T> Higher<Higher<Higher<nested, W1>, W2>, T> unit(T value) {
                    return Instances.<W1,W2>unit(def1,def2)
                            .unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return Instances.<W1,W2>functor()
                            .map(fn,ds);
                }
            };
        }
        public static <W1,W2> MonadRec<Higher<Higher<nested, W1>, W2>> monadRec(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2) {
            return new MonadRec<Higher<Higher<nested, W1>, W2>>() {
                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<Higher<nested, W1>, W2>, ? extends Either<T, R>>> fn) {

                    Higher<Higher<Higher<nested, W1>, W2>, Either<T, R>>[] next = new Higher[1];
                    next[0] = Instances.unit(def1, def2).unit(Either.left(initial));
                    Foldable<Higher<Higher<nested, W1>, W2>> foldable = Instances.foldable();
                    boolean cont[] = {true};
                    do {
                        BinaryOperator<Either<T,R>> bifn = (a, b)->{
                            if (cont[0] && b.visit(s -> {
                                Higher<Higher<Higher<nested, W1>, W2>, ? extends Either<T, R>> x = fn.apply(s);
                                next[0] = (Higher)x;
                                return true;
                            }, pr -> false)) cont[0] = true;
                            else cont[0] = false;
                            return Either.left(initial);
                        };
                        foldable.foldLeft(Either.<T,R>left(initial),bifn,next[0]);

                    } while (cont[0]);
                    Nested<W1, W2, Either<T, R>> res = narrowK(next[0]);
                    return res.map(x->x.orElse(null));
                }
            };

        }
        public static <W1,W2,C2, T, R> Higher<C2, Higher<Higher<Higher<nested, W1>, W2>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<Higher<nested, W1>, W2>, T> ds){
            Nested<W1, W2, T> n = narrowK(ds);
            ComposedTraverse<W1,W2> ct = ComposedTraverse.of(n.def1.traverse(),n.def2.traverse(),n.def2.applicative());
            Higher<C2, Higher<W1, Higher<W2, R>>> r = ct.traverse(applicative,fn,n.nested);
            Higher<C2, Higher<Higher<Higher<nested, W1>, W2>, R>> x = applicative.map(nr -> Nested.of(nr, n.def1, n.def2), r);
            return x;

        }
        public static <W1,W2,T> Traverse<Higher<Higher<nested, W1>, W2>> traverse(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2, TransformerFactory<W1,W2> factory){
            return General.traverseByTraverse(applicative(def1,def2,factory), (a, b, c)-> traverseA(a,b,c));
        }

        public static <W1,W2> Monad<Higher<Higher<nested, W1>, W2>> monad(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2,TransformerFactory<W1,W2> factory) {
            return new Monad<Higher<Higher<nested, W1>, W2>>() {
                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<nested, W1>, W2>, R>> fn, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return narrowK(ds).transformer(factory)
                                      .flatMap(fn.andThen(a -> narrowK(a)));

                }

                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> ap(Higher<Higher<Higher<nested, W1>, W2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<nested, W1>, W2>, T> apply) {
                    return Instances.<W1,W2>applicative(def1,def2,factory)
                                    .ap(fn,apply);
                }

                @Override
                public <T> Higher<Higher<Higher<nested, W1>, W2>, T> unit(T value) {
                    return Instances.<W1,W2>unit(def1,def2)
                                    .unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return Instances.<W1,W2>functor()
                                   .map(fn,ds);
                }
            };
        }
        public static <W1,W2> MonadZero<Higher<Higher<nested, W1>, W2>> monadZero(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2,TransformerFactory<W1,W2> factory,Higher<W2,?> zero) {
            return new MonadZero<Higher<Higher<nested, W1>, W2>>() {

                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> ap(Higher<Higher<Higher<nested, W1>, W2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<nested, W1>, W2>, T> apply) {
                    return Instances.applicative(def1,def2,factory).ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return Instances.<W1,W2>functor()
                                    .map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<nested, W1>, W2>, T> zero() {
                    Higher<W2,T> identity = ( Higher<W2,T>)zero;
                    Higher<W1, Higher<W2, T>> res = def1.unit().unit(identity);
                    return Nested.of(res,def1,def2);
                }

                @Override
                public <T> Higher<Higher<Higher<nested, W1>, W2>, T> unit(T value) {
                    return Instances.unit(def1,def2).unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<nested, W1>, W2>, R>> fn, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return Instances.monad(def1,def2,factory).flatMap(fn,ds);
                }
            };
        }

        public static <W1,W2>  Foldable<Higher<Higher<nested, W1>, W2>> foldable() {
            return new Foldable<Higher<Higher<nested, W1>, W2>>(){

                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return narrowK(ds).foldBothRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return narrowK(ds).foldBothLeft(monoid);
                }
                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<Higher<nested, W1>, W2>, T> nestedA) {
                    return narrowK(nestedA).<R>map(fn).foldBothLeft(mb);
                }
            };
        }


        public static <W1,W2>  Unfoldable<Higher<Higher<nested, W1>, W2>> unfoldable(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2) {
            return new Unfoldable<Higher<Higher<nested, W1>, W2>>(){

                @Override
                public <R, T> Higher<Higher<Higher<nested, W1>, W2>, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
                    return narrowK(unit(def1,def2).unit(b)).unfoldsUnsafe().unfold(fn);
                }
            };
        }
    }

    @AllArgsConstructor(access= AccessLevel.PRIVATE)
    public static class NestedComprehensions<W1,W2,T> {


        public static  <W1,W2,T> NestedComprehensions<W1,W2,T> of(Nested<W1,W2,T> value1,Monad<Higher<Higher<nested, W1>, W2>>  monad){
            return new NestedComprehensions<>(monad,value1);
        }
        public static <W1,W2,T> NestedComprehensions.Guarded<W1,W2,T> of(Nested<W1,W2,T> value1,MonadZero<Higher<Higher<nested, W1>, W2>>  monad){
            return new NestedComprehensions.Guarded<>(monad,value1);
        }

        private final Monad<Higher<Higher<nested, W1>, W2>> monad;
        private final Nested<W1,W2,T> value1;

        public  < T2, T3, R1, R2, R3, R> Nested<W1,W2,R> forEach4(
                                                                  Function<? super T, ? extends Nested<W1,W2,R1>> value2,
                                                                  BiFunction<? super T, ? super R1, ? extends Nested<W1,W2,R2>> value3,
                                                                  Function3<? super T, ? super R1, ? super R2, ? extends Nested<W1,W2,R3>> value4,
                                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

            return narrowK(monad.flatMap_(value1,in -> {

                Nested<W1,W2,R1> a = value2.apply(in);
                return monad.flatMap_(a,ina -> {
                    Nested<W1,W2,R2> b = value3.apply(in,ina);
                    return monad.flatMap_(b,inb -> {
                        Nested<W1,W2,R3> c = value4.apply(in,ina,inb);
                        return monad.map_(c, in2 -> yieldingFunction.apply(in, ina, inb, in2));
                    });

                });

            }));

        }





        public  <T2, R1, R2, R> Nested<W1,W2,R> forEach3(
                                                          Function<? super T, ? extends Nested<W1,W2,R1>> value2,
                                                          BiFunction<? super T, ? super R1, ? extends Nested<W1,W2,R2>> value3,
                                                          Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

            return narrowK(monad.flatMap_(value1,in -> {

                Nested<W1,W2,R1> a = value2.apply(in);
                return monad.flatMap_(a,ina -> {
                    Nested<W1,W2,R2> b = value3.apply(in,ina);
                    return monad.map_(b, in2 -> yieldingFunction.apply(in, ina, in2));
                });


            }));

        }



        public  <R1, R> Nested<W1,W2,R> forEach2(Function<? super T, ? extends Nested<W1,W2,R1>> value2,
                                                 BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

            Higher<Higher<Higher<nested, W1>, W2>, R> x = monad.flatMap_(value1, in -> {

                Nested<W1, W2, R1> a = value2.apply(in);
                return monad.map_(a, in2 -> yieldingFunction.apply(in, in2));
            });
            return narrowK(x);


        }
        @AllArgsConstructor(access= AccessLevel.PRIVATE)
        public static class Guarded<W1,W2,T> {

            private final MonadZero<Higher<Higher<nested, W1>, W2>> monadZero;
            private final Nested<W1,W2,T> value1;
            public  <T2, T3, R1, R2, R3, R> Nested<W1,W2,R> forEach4(
                                                                      Function<? super T, ? extends Nested<W1,W2,R1>> value2,
                                                                      BiFunction<? super T, ? super R1, ? extends Nested<W1,W2,R2>> value3,
                                                                      Function3<? super T, ? super R1, ? super R2, ? extends Nested<W1,W2,R3>> value4,
                                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

                return narrowK(monadZero.flatMap_(value1,in -> {

                    Nested<W1,W2,R1> a = value2.apply(in);
                    return monadZero.flatMap_(a,ina -> {
                        Nested<W1,W2,R2> b = value3.apply(in,ina);
                        return monadZero.flatMap_(b,inb -> {
                            Nested<W1,W2,R3> c = value4.apply(in,ina,inb);

                            Nested<W1,W2, R3> x = narrowK(monadZero.filter_(c, in2 -> filterFunction.apply(in, ina, inb, in2)));
                            return monadZero.map_(x, in2 -> yieldingFunction.apply(in, ina, inb, in2));
                        });

                    });

                }));

            }
            public  <T2, R1, R2, R> Nested<W1,W2,R> forEach3(
                                                              Function<? super T, ? extends Nested<W1,W2,R1>> value2,
                                                              BiFunction<? super T, ? super R1, ? extends Nested<W1,W2,R2>> value3,
                                                              Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

                return narrowK(monadZero.flatMap_(value1,in -> {

                    Nested<W1,W2,R1> a = value2.apply(in);
                    return monadZero.flatMap_(a,ina -> {
                        Nested<W1,W2,R2> b = value3.apply(in,ina);
                        Nested<W1,W2, R2> x = narrowK(monadZero.filter_(b, in2 -> filterFunction.apply(in, ina, in2)));
                        return   monadZero.map_(x,in2 -> yieldingFunction.apply(in, ina, in2));
                    });



                }));

            }
            public  <R1, R> Nested<W1,W2,R> forEach2(Function<? super T, ? extends Nested<W1,W2,R1>> value2,
                                                     BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

                return narrowK(monadZero.flatMap_(value1,in -> {

                    Nested<W1,W2,R1> a = value2.apply(in);
                    Nested<W1,W2, R1> x = narrowK(monadZero.filter_(a, in2 -> filterFunction.apply(in, in2)));
                    return    monadZero.map_(x,in2 -> yieldingFunction.apply(in,  in2));
                }));




            }


        }


    }

}
