package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher3;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.async.Future;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.CompletableFutures;
import cyclops.companion.CompletableFutures.CompletableFutureKind;
import cyclops.companion.Optionals;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.companion.Streams;
import cyclops.companion.Streams.StreamKind;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Try;
import cyclops.control.Xor;
import cyclops.function.Group;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.SemigroupK;
import cyclops.typeclasses.functor.Compose;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 * Class for working with Nested Data Structures.
 *
 * E.g. to work with a List of Optionals
 * <pre>
 *     {@code
 *      import cyclops.monads.Witness.list;
        import cyclops.monads.Witness.optional;

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
 * @param <W1> First Witness type {@see cyclops.monads.Witness}
 * @param <W2> Second Witness type {@see cyclops.monads.Witness}
 * @param <T> Nested Data Type
 */
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@EqualsAndHashCode(of={"nest"})
public class Nested<W1,W2,T> implements Transformable<T>,
                                        Higher3<nested,W1,W2,T>,To<Nested<W1,W2,T>> {

    private final Higher<W1,Higher<W2,T>> nested;
    private final Compose<W1,W2> composedFunctor;
    private final InstanceDefinitions<W1> def1;
    private final InstanceDefinitions<W2> def2;





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


    public <R> Nested<W1,W2,R> map(Function<? super T,? extends R> fn){
        Higher<W1, Higher<W2, R>> res = composedFunctor.map(fn, nested);
        return new Nested<>(res,composedFunctor,def1,def2);
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

    public <C,R> NarrowedTailRec<C,R> concreteTailRec(Kleisli<W2,C,Xor<T,R>> widen){
        return new NarrowedTailRec<>(widen);
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
        private final Kleisli<W2,C,Xor<T,R>> widen;

        public  Nested<W1,W2,R> tailRec(T initial,Function<? super T,? extends C> fn){
            return Nested.this.tailRec(initial,fn.andThen(widen));
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
                    Maybe.just(sum(narrow.apply(s.monoid2().zero()), (C a, C b) -> narrow.apply(s.monoid2().apply(widen.apply(a), widen.apply(b))), list))
            );
        }

    }

    public <R> Nested<W1,W2,R> flatMapA(Function<? super T, ? extends Active<W2,R>> fn){
        Higher<W1, Higher<W2, R>> res = composedFunctor.map1(a->def2.monad().flatMap(fn.andThen(t->t.getSingle()), a),nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }

    public <R> Nested<W1,W2, R> tailRec(T initial,Function<? super T,? extends Higher<W2, ? extends Xor<T, R>>> fn){
        return flatMapA(in->Active.of(def2.unit().unit(in),def2).tailRec(initial,fn));

    }


    public Traverse traverseUnsafe(){
        return def1.traverse().visit(s-> new Traverse(),()->null);
    }
    public Folds foldsUnsafe(){
        return def2.foldable().visit(s-> new Folds(),()->null);
    }
    public Unfolds unfoldsUnsafe(){
        return def2.unfoldable().visit(s-> new Unfolds(),()->null);
    }
    public Maybe<Traverse> traverse(){
        return def1.traverse().visit(s-> Maybe.just(new Traverse()),Maybe::none);
    }
    public Maybe<Folds> folds(){
        return def2.foldable().visit(s-> Maybe.just(new Folds()),Maybe::none);
    }
    public Maybe<Unfolds> unfolds(){
        return def2.unfoldable().visit(s-> Maybe.just(new Unfolds()),Maybe::none);
    }
    public Plus plusUnsafe(){
        return new Plus();
    }

    public Nested<W1,W2,T> plusNested(SemigroupK<W2,T> semigroupK, Higher<W2,T> add){
        return of(def1.functor().map(i -> semigroupK.apply(i, add),nested), def1, def2);
    }

    public Maybe<Plus> plus(){
        if(def1.monadPlus().isPresent() && def2.monadPlus().isPresent()){
            return Maybe.just(plusUnsafe());
        }
        return Maybe.none();
    }

    public class Plus{
        public Monoid<Higher<W2,T>> monoid2(){
            return def2.monadPlus().get().narrowMonoid();
        }
        public Nested<W1,W2,T> sum(ListX<Nested<W1,W2, T>> list){
            return of(def1.monadPlus().visit(p -> p.sum(list.plus(Nested.this).map(x -> x.nested)), () -> nested),def1,def2);
        }

        public Nested<W1,W2,T> plus(Higher<W2, T> b){
            Functor<W1> f = def1.functor();
            MonadPlus<W2> mp = def2.monadPlus().get();
            Higher<W1, Higher<W2, T>> x = f.map(a -> mp.plus(a, b), nested);
            return of(x,def1,def2);
        }
        public Nested<W1,W2,T> plus(Nested<W1,W2, T> b){
            Monad<W1> f = def1.monad();
            MonadPlus<W2> mp = def2.monadPlus().get();
            Higher<W1, Higher<W2, T>> x2 = f.flatMap(a -> {
                Nested<W1, W2, T> r = plus(a);
                return r.nested;
            }, b.nested);
            return of(x2,def1,def2);
        }

    }


    public class Folds{


        public <R> R foldMapBoth(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
            return foldMapA(mb,fn).foldsUnsafe().foldRight(mb);
        }
        public T foldBothl(T identity, BinaryOperator<T> semigroup){
            return  foldl(Monoid.fromBiFunction(identity, semigroup)).foldsUnsafe().foldLeft(identity,semigroup);
        }
        public T foldBothr(T identity, BinaryOperator<T> semigroup){
            return  foldr(Monoid.fromBiFunction(identity, semigroup)).foldsUnsafe().foldRight(identity,semigroup);
        }
        public  T foldBothRight(Monoid<T> monoid){
            return Active.of(foldRight(monoid),def1).foldsUnsafe().foldRight(monoid);
        }
        public  T foldBothLeft(Monoid<T> monoid){
            return Active.of(foldRight(monoid),def1).foldsUnsafe().foldLeft(monoid);
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
            return def1.functor().map(a -> def2.foldable().get().foldMap(mb, fn,a), nested);
        }

        public  Higher<W1,T> foldRight(Monoid<T> monoid){
            return def1.functor().map(a -> def2.foldable().get().foldRight(monoid, a), nested);
        }
        public  Higher<W1,T> foldLeft(Monoid<T> monoid){
            return def1.functor().map(a -> def2.foldable().get().foldLeft(monoid, a), nested);
        }

        public Higher<W1,T> foldLeft(T identity, BinaryOperator<T> semigroup){
            return foldLeft(Monoid.fromBiFunction(identity, semigroup));
        }
        public Higher<W1,T> foldRight(T identity, BinaryOperator<T> semigroup){
            return foldRight(Monoid.fromBiFunction(identity, semigroup));
        }
    }
    public class Unfolds{
        public <R> Nested<W1,W2, R> unfold(Function<? super T, Optional<Tuple2<R, T>>> fn){
            Unfoldable<W2> unf = def2.unfoldable().get();
            Higher<W1, Higher<W2, R>> x = def1.functor().map(a -> def2.monad().flatMap(c -> unf.unfold(c, fn), a), nested);
            return Nested.of(x,def1,def2);
        }
        private <T2> Nested<W1,W2, T> unfoldPrivate(T2 b,Function<T2, Optional<Tuple2<T, T2>>> fn){
            Unfoldable<W2> unf = def2.unfoldable().get();
            Higher<W1, Higher<W2, T>> x = def1.functor().map(a -> def2.monad().flatMap(c -> unf.unfold(b, fn.andThen(o->o.map(t->t.map1(v->c)))), a), nested);
            return Nested.of(x,def1,def2);
        }

        private <T,R> Nested<W1,W2, R> unfoldIgnore(T b,Function<T, Optional<Tuple2<R, T>>> fn){
            Unfoldable<W2> unf = def2.unfoldable().get();
            Higher<W1, Higher<W2, R>> x = def1.functor().map(a -> def2.monad().flatMap(c -> unf.unfold(b, fn), a), nested);
            return Nested.of(x,def1,def2);
        }

        public <R> Nested<W1,W2, R> replaceWith(int n, R value) {
            return this.<Integer,R>unfoldIgnore(n, i-> Optional.of(Tuple.tuple(value, i - 1)));
        }

        public  Nested<W1,W2, T> replicate(int n) {
            return this.<Integer>unfoldPrivate(n, i-> Optional.of(Tuple.tuple(null, i - 1)));
        }


        public <R> Nested<W1,W2, R> none() {
            return unfold(t -> Optional.<Tuple2<R, T>>empty());
        }
        public <R> Nested<W1,W2, R> replaceWith(R a) {
            return replaceWith(1, a);
        }
    }
    public class Traverse {

        public Nested<W2, W1, T> sequence(){
            Higher<W2, Higher<W1, T>> res = def1.traverse().get().sequenceA(def2.applicative(), nested);
            return of(res,def2,def1);
        }
        public  <R> Nested<W2, W1, R> traverse(Function<? super T,? extends R> fn){
            return sequence().map(fn);
        }

        public <R> Active<W1,R> foldMap(Monoid<R> mb, final Function<? super T,? extends R> fn) {
            return Active.of(def1.functor().map(f -> def2.traverse().get().foldMap(mb, fn, f), nested),def1);
        }
    }

    public String toString(){
        return "Nested["+nested.toString()+"]";
    }

    @Override
    public <U> Nested<W1,W2,U> cast(Class<? extends U> type) {
        return (Nested<W1,W2,U>)Transformable.super.cast(type);
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
        return of(hkt, CompletableFutures.Instances.definitions(), Streams.Instances.definitions());
    }
    public static <T> Nested<optional,stream,T> optionalStream(Optional<? extends Stream<T>> optionalList){
        OptionalKind<StreamKind<T>> opt = OptionalKind.widen(optionalList).map(StreamKind::widen);
        Higher<optional,Higher<stream,T>> hkt = (Higher)opt;
        return of(hkt, Optionals.Instances.definitions(), Streams.Instances.definitions());
    }

    public static <T> Nested<optional,list,T> optionalList(Optional<? extends List<T>> optionalList){
        OptionalKind<ListX<T>> opt = OptionalKind.widen(optionalList).map(ListX::fromIterable);
        Higher<optional,Higher<list,T>> hkt = (Higher)opt;
        return of(hkt, Optionals.Instances.definitions(), ListX.Instances.definitions());
    }
    public static <T, X extends Throwable> Nested<future,Higher<tryType,X>,T> futureTry(Future<? extends Try<T,X>> futureTry){
        Higher<future,Higher<Higher<tryType,X>,T>> hkt = (Higher)futureTry;
        return of(hkt, Future.Instances.definitions(), Try.Instances.definitions());
    }
    public static <T, X extends Throwable> Nested<list,Higher<tryType,X>,T> listTry(List<? extends Try<T,X>> futureTry){
        Higher<list,Higher<Higher<tryType,X>,T>> hkt = (Higher)futureTry;
        return of(hkt, ListX.Instances.definitions(), Try.Instances.definitions());
    }
    public static <L,R> Nested<list,Higher<xor,L>,R> listXor(List<? extends Xor<L,R>> listXor){
        Higher<list,Higher<Higher<xor,L>,R>> hkt = (Higher)listXor;
        return of(hkt, ListX.Instances.definitions(), Xor.Instances.definitions());
    }
    public static <L,R> Nested<future,Higher<xor,L>,R> futureXor(Future<? extends Xor<L,R>> futureXor){
        Higher<future,Higher<Higher<xor,L>,R>> hkt = (Higher)futureXor;
        return of(hkt, Future.Instances.definitions(), Xor.Instances.definitions());
    }
    public static <T> Nested<future,list,T> futureList(Future<? extends List<T>> futureList){
        return of(futureList.map(ListX::fromIterable),Future.Instances.definitions(), ListX.Instances.definitions());
    }
    public static <T> Nested<future,vectorX,T> futureVector(Future<VectorX<T>> futureList){
        Higher<future,Higher<vectorX,T>> hkt = (Higher)futureList;
        return of(hkt,Future.Instances.definitions(), VectorX.Instances.definitions());
    }
    public static <W1,W2,T> Nested<W1,W2,T> narrowK(Higher<Higher<Higher<nested, W1>, W2>, T> ds){
        return (Nested<W1,W2,T>)ds;
    }



    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Instances<W1,W2>  {



        public static <T, R,W1,W2> Functor<Higher<Higher<nested, W1>, W2>> functor() {
            return new Functor<Higher<Higher<nested, W1>, W2>>(){

                @Override
                public <T, R> Higher<Higher<Higher<nested, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }


        public static <T,W1,W2> Pure<Higher<Higher<nested, W1>, W2>> unit(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2) {
            return new Pure<Higher<Higher<nested, W1>, W2>>(){

                @Override
                public <T> Higher<Higher<Higher<nested, W1>, W2>, T> unit(T value) {
                    return Nested.of(def1.unit().unit(def2.unit().unit(value)),def1,def2);

                }
            };
        }



        public static <T,W1,W2>  Foldable<Higher<Higher<nested, W1>, W2>> foldable() {
            return new Foldable<Higher<Higher<nested, W1>, W2>>(){

                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return narrowK(ds).foldsUnsafe().foldBothRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<nested, W1>, W2>, T> ds) {
                    return narrowK(ds).foldsUnsafe().foldBothLeft(monoid);
                }
            };
        }


        public static <T,W1,W2>  Unfoldable<Higher<Higher<nested, W1>, W2>> unfoldable(InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2) {
            return new Unfoldable<Higher<Higher<nested, W1>, W2>>(){

                @Override
                public <R, T> Higher<Higher<Higher<Witness.nested, W1>, W2>, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return narrowK(unit(def1,def2).unit(b)).unfoldsUnsafe().unfold(fn);
                }
            };
        }
    }
}
