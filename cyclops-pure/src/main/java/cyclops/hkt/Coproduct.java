package cyclops.hkt;


import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher3;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Future;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.instances.data.LazySeqInstances;
import cyclops.instances.data.SeqInstances;
import cyclops.instances.data.VectorInstances;
import cyclops.control.*;
import cyclops.control.LazyEither;
import cyclops.control.Maybe;
import cyclops.data.ImmutableList;
import cyclops.function.Monoid;
import com.oath.cyclops.hkt.DataWitness.*;
import cyclops.instances.control.EvalInstances;
import cyclops.instances.control.FutureInstances;
import cyclops.instances.control.MaybeInstances;
import cyclops.instances.control.TryInstances;
import cyclops.instances.jdk.CompletableFutureInstances;
import cyclops.instances.jdk.OptionalInstances;
import cyclops.instances.jdk.StreamInstances;
import cyclops.instances.reactive.PublisherInstances;
import cyclops.kinds.CompletableFutureKind;
import cyclops.kinds.OptionalKind;
import cyclops.kinds.StreamKind;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.SemigroupK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;


import java.util.concurrent.Executor;
import java.util.function.*;
import java.util.stream.Stream;

import static cyclops.data.tuple.Tuple.tuple;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of="either")
@Getter
public class Coproduct<W1,W2,T> implements  Filters<T>,Higher3<coproduct,W1,W2,T>,
                                            Transformable<T>, To<Coproduct<W1,W2,T>> {

    private final Either<Higher<W1,T>,Higher<W2,T>> xor;
    private final InstanceDefinitions<W1> def1;
    private final InstanceDefinitions<W2> def2;



    public static  <W1,W2,T> Coproduct<W1,W2,T> of(Either<Higher<W1,T>,
                    Higher<W2,T>> xor, InstanceDefinitions<W1> def1, InstanceDefinitions<W2> def2){
        return new Coproduct<>((Either)xor,def1,def2);
    }

    public static  <W1,W2,T> Coproduct<W1,W2,T> right(Higher<W2,T> right,InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2){
        return new Coproduct<>(Either.right(right),def1,def2);
    }
    public static  <W1,W2,T> Coproduct<W1,W2,T> left(Higher<W1,T> left,InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2){
        return new Coproduct<>(Either.left(left),def1,def2);
    }

    public Coproduct<W1,W2,T> filter(Predicate<? super T> test) {
        return of(xor.map(m -> def2.<T, T>monadZero().fold(s->s.filter(test, m),()->m))
               .mapLeft(m -> def1.<T, T>monadZero().fold(s->s.filter(test, m),()->m)),def1,def2);
    }

    public <R>  Coproduct<W1,W2,R> coflatMap(final Function<? super  Coproduct<W1,W2,T>, R> mapper){
        return fold(leftM ->  left(def1.unit()
                        .unit(mapper.apply(this)),def1,def2),
                    rightM -> right(def2.unit()
                            .unit(mapper.apply(this)),def1,def2));
    }

    @Override
    public <U> Coproduct<W1,W2,U> ofType(Class<? extends U> type) {
        return (Coproduct<W1,W2,U>)Filters.super.ofType(type);
    }
    public Active<W1,T> activeLeft(MonoidK<W1> m, Higher<W1,T> concat){
        Higher<W1, T> h = xor.fold(s -> m.apply(s, concat), p -> m.zero());
        return Active.of(h,def1);
    }
    public Active<W2,T> activeSecond(MonoidK<W2> m, Higher<W2,T> concat){
        Higher<W2, T> h = xor.fold(s -> m.zero(), p -> m.apply(p, concat));
        return Active.of(h,def2);
    }

    public Coproduct<W1,W2,T> plusLeft(SemigroupK<W1> semigroupK, Higher<W1,T> add){
        return of(xor.flatMapLeft(s -> Either.left(semigroupK.apply(s, add))),def1,def2);
    }
    public Coproduct<W1,W2,T> plusRight(SemigroupK<W2> semigroupK, Higher<W2,T> add){
        return of(xor.flatMap(p -> Either.right(semigroupK.apply(p, add))),def1,def2);
    }

    public Product<W1,W2,T> product(MonoidK<W1> m1, MonoidK<W2> m2){
        return Product.of(xor.fold(s -> Tuple.tuple(s, m2.zero()), p -> Tuple.tuple(m1.zero(), p)),def1,def2);
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
    public <R>  Coproduct<W1,W2,R> map(Function<? super T, ? extends R> fn) {

        return of(xor.map(m->{
            Higher<W2, ? extends R> x = def2.<T, R>functor().map(fn, m);
            return (Higher<W2, R>)x;
        }).mapLeft(m->{
            Higher<W1, ? extends R> x = def1.<T, R>functor().map(fn, m);
            return (Higher<W1, R>)x;
        }),def1,def2);
    }
    public <R> Coproduct<W1,W2,R> mapWithIndex(BiFunction<? super T,Long,? extends R> f) {
        return of(xor.map(m->{
        Higher<W2, ? extends R> x = def2.<T, R>traverse().mapWithIndex(f, m);
        return (Higher<W2, R>)x;
    }).mapLeft(m->{
        Higher<W1, ? extends R> x = def1.<T, R>traverse().mapWithIndex(f, m);
        return (Higher<W1, R>)x;
    }),def1,def2);

    }
    public <R> Coproduct<W1,W2,Tuple2<T,Long>> zipWithIndex() {
        return mapWithIndex(Tuple::tuple);
    }

    public Either<Higher<W1,T>,Higher<W2,T>> asXor(){
        return xor;
    }
    public Either<Active<W1,T>,Active<W2,T>> asActiveXor(){
        return xor.bimap(s->Active.of(s,def1),p->Active.of(p,def2));
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



    public <R> R fold(Function<? super Higher<W1,? super T>, ? extends R> left, Function<? super Higher<W2,? super T>, ? extends R> right ){
        return xor.fold(left,right);
    }

    public Coproduct<W2,W1,T> swap(){
        return of(xor.swap(),def2,def1);
    }


    public Maybe<Plus> plus(){
        MonadPlus<W1> plus1 = def1.monadPlus().fold(p->p,()->null);
        MonadPlus<W2> plus2 = def2.monadPlus().fold(p->p,()->null);
        return xor.fold(s->def1.monadPlus().isPresent() ? Maybe.just(new Plus(plus1,plus2)) : Maybe.nothing(),
                                p->def2.monadPlus().isPresent() ? Maybe.just(new Plus(plus1,plus2)) : Maybe.nothing());
    }

    public Unfolds unfoldsDefault(){
        Unfoldable<W1> unf1 = def1.unfoldable().fold(a->  a ,()-> new Unfoldable.UnsafeValueUnfoldable<>());
        Unfoldable<W2> unf2 = def2.unfoldable().fold(a->  a ,()-> new Unfoldable.UnsafeValueUnfoldable<>());
        return new Unfolds(unf1,unf2);
    }


    public Maybe<Unfolds> unfolds(){
        Unfoldable<W1> unf1 = def1.unfoldable().fold(a->  a ,()-> new Unfoldable.UnsafeValueUnfoldable<>());
        Unfoldable<W2> unf2 = def2.unfoldable().fold(a->  a ,()-> new Unfoldable.UnsafeValueUnfoldable<>());

        return xor.fold(s-> def1.unfoldable().isPresent() ? Maybe.just(new Unfolds(unf1,unf2)) : Maybe.nothing(), p-> def2.unfoldable().isPresent() ? Maybe.just(new Unfolds(unf1,unf2)) : Maybe.nothing());
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Plus{
        private final  MonadPlus<W1> plus1;
        private final  MonadPlus<W2> plus2;
        public Coproduct<W1,W2,T> plus(Coproduct<W1,W2,T> a){

            if(xor.isLeft() && a.xor.isLeft()){
                    Higher<W1, T> plused = plus1.plus(xor.leftOrElse(null), a.xor.leftOrElse(null));
                    return Coproduct.left(plused,def1,def2);
            }
            if(xor.isRight() && a.xor.isRight()){
                Higher<W2, T> plused = plus2.plus(xor.orElse(null), a.getXor().orElse(null));
                return Coproduct.right(plused,def1,def2);
            }
            return Coproduct.this;

        }
        public Coproduct<W1,W2,T> sum(ImmutableList<Coproduct<W1,W2,T>> l){
            ImmutableList<Coproduct<W1,W2,T>> list = l.plus(Coproduct.this);
            if(xor.isLeft()){
                Higher<W1, T> summed = plus1.sum(list.map(c -> c.xor.leftOrElse(null)));
                return Coproduct.left(summed,def1,def2);
            }
            if(xor.isRight()){
                Higher<W2, T> summed = plus2.sum(list.map(c -> c.xor.orElse(null)));
                return Coproduct.right(summed,def1,def2);
            }
            return Coproduct.this;
        }

    }


    public <R> R foldMap(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
        return xor.fold(left->def1.foldable().foldMap(mb, fn, left),
                right->def2.foldable().foldMap(mb, fn, right));
    }
    public T foldRight(Monoid<T> monoid) {
        return xor.fold(left->def1.foldable().foldRight(monoid, left),
                right->def2.foldable().foldRight(monoid, right));

    }

    public T foldRight(T identity, BinaryOperator<T> semigroup) {
        return foldRight(Monoid.fromBiFunction(identity,semigroup));

    }
    public Seq<T> toSeq(){
        return xor.fold(left->def1.foldable().seq(left),
                right->def2.foldable().seq(right));
    }
    public LazySeq<T> toLazySeq(){
        return xor.fold(left->def1.foldable().lazySeq(left),
            right->def2.foldable().lazySeq(right));
    }
    public  ReactiveSeq<T> stream(){
        return toLazySeq().stream();
    }
    public Coproduct<W1,W2,T> reverse() {
        return xor.fold(l -> {
            return Coproduct.of(Either.left(def1.traverse().reverse(l)), def1, def2);
        }, r -> {
            return Coproduct.of(Either.right(def2.traverse().reverse(r)), def1, def2);
        });

    }
    public  long size() {
        return xor.fold(left->def1.foldable().size(left),
                right->def2.foldable().size(right));
    }

    public T foldLeft(Monoid<T> monoid) {
        return xor.fold(left->def1.foldable().foldLeft(monoid, left),
                right->def2.foldable().foldLeft(monoid, right));
    }


    public T foldLeft(T identity, BinaryOperator<T> semigroup) {
        return foldLeft(Monoid.fromBiFunction(identity,semigroup));
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Unfolds{
        private final Unfoldable<W1> unf1;
        private final Unfoldable<W2> unf2;
        public <R, T> Coproduct<W1,W2, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn){
            Either<Higher<W1,R>,Higher<W2,R>> res = xor.fold(left -> Either.left(unf1.unfold(b, fn)), r -> Either.right(unf2.unfold(b, fn)));
            Coproduct<W1, W2, R> cop = Coproduct.of(res, def1, def2);
            return cop;
        }

        public <T> Coproduct<W1,W2,T> replicate(int n, T value) {
            return unfold(n,i -> Option.some(tuple(value, i-1)));
        }

        public <R> Coproduct<W1,W2,R> none() {
            return unfold((T) null, t -> Option.<Tuple2<R, T>>none());
        }
        public <T> Coproduct<W1,W2,T> one(T a) {
            return replicate(1, a);
        }

    }

    public <W3, R> Higher<W3,Coproduct<W1,W2, R>> traverseA(Applicative<W3> applicative, Function<? super T, Higher<W3, R>> f){
        return traverseA(applicative,f,this);
    }
    public static <W1,W2,T,C2, R> Higher<C2, Coproduct<W1,W2,R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,Coproduct<W1,W2,T> n){

        return n.xor.fold(it->{
            return applicative.map(x->of(Either.left(x),n.def1,n.def2),n.def1.traverse().traverseA(applicative, fn, it));
        },it->{
            return applicative.map(x->of(Either.right(x),n.def1,n.def2),n.def2.traverse().traverseA(applicative, fn, it));
        });

    }
    public  <C2,T> Higher<C2, Coproduct<W1,W2,T>> sequenceA(Applicative<C2> applicative,
                                                          Coproduct<W1,W2,Higher<C2,T>> ds){
        return traverseA(applicative, i -> i, ds);

    }



    public static  <W1,T> Coproduct<W1,vector,T> vector(Vector<T> list, InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(list),def1, VectorInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,vector,T> vector(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Either.right(Vector.of(values)),def1, VectorInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,lazySeq,T> lazySeq(LazySeq<T> list,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(list),def1, LazySeqInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,lazySeq,T> lazySeq(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Either.right(LazySeq.of(values)),def1, LazySeqInstances.definitions());
    }


    public static  <W1,T> Coproduct<W1,seq,T> seq(Seq<T> list,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(list),def1, SeqInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,seq,T> seq(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Either.right(Seq.of(values)),def1, SeqInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,stream,T> stream(Stream<T> stream,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(StreamKind.widen(stream)),def1, StreamInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,stream,T> stream(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Either.right(StreamKind.of(values)),def1, StreamInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,reactiveSeq,T> reactiveSeq(ReactiveSeq<T> stream,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(stream),def1, PublisherInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,reactiveSeq,T> reactiveSeq(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Either.right(ReactiveSeq.of(values)),def1, PublisherInstances.definitions());
    }
    public static  <W1,X extends Throwable,T> Coproduct<W1,Higher<tryType,X>,T> success(T value,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(Try.success(value)),def1, TryInstances.definitions());
    }
    public static  <W1,X extends Throwable,T> Coproduct<W1,Higher<tryType,X>,T> failure(X value,InstanceDefinitions<W1> def1){
        return new Coproduct<W1,Higher<tryType,X>,T>(Either.right(Try.failure(value)),def1, TryInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,future,T> futureOf(Supplier<T> value, Executor ex,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(Future.of(value, ex)),def1, FutureInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,completableFuture,T> completableFutureOf(Supplier<T> value, Executor ex,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(CompletableFutureKind.supplyAsync(value, ex)),def1, CompletableFutureInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,eval,T> later(Supplier<T> value,InstanceDefinitions<W1> def1){
        return new Coproduct<>(LazyEither.right(Eval.later(value)),def1, EvalInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,optional,T> ofNullable(T value,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(OptionalKind.ofNullable(value)),def1, OptionalInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,option,T> just(T value, InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(Maybe.just(value)),def1, MaybeInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,option,T> none(InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(Maybe.nothing()),def1, MaybeInstances.definitions());
    }
    public static  <W1,T> Coproduct<W1,option,T> maybeNullabe(T value, InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(Maybe.ofNullable(value)),def1, MaybeInstances.definitions());
    }
    public static <W1,W2,T> Coproduct<W1,W2,T> narrowK(Higher<Higher<Higher<coproduct, W1>, W2>, T> ds){
        return (Coproduct<W1,W2,T>)ds;
    }

    public static class Instances<W1,W2>  {



        public static <W1, W2> Functor<Higher<Higher<coproduct, W1>, W2>> functor() {
            return new Functor<Higher<Higher<coproduct, W1>, W2>>(){

                @Override
                public <T, R> Higher<Higher<Higher<coproduct, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<coproduct, W1>, W2>, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }


        public <T> Pure<Higher<Higher<coproduct, W1>, W2>> unit(InstanceDefinitions<W1> def1, InstanceDefinitions def2) {
            return new Pure<Higher<Higher<coproduct, W1>, W2>>(){

                @Override
                public <T> Higher<Higher<Higher<coproduct, W1>, W2>, T> unit(T value) {
                    return Coproduct.right(def2.unit().unit(value),def1,def2);
                }
            };
        }


        public static <W1,W2,T> Foldable<Higher<Higher<coproduct, W1>, W2>> foldable() {
            return new Foldable<Higher<Higher<coproduct, W1>, W2>>(){

                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<coproduct, W1>, W2>, T> ds) {
                    return narrowK(ds).foldRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<coproduct, W1>, W2>, T> ds) {
                    return narrowK(ds).foldLeft(monoid);
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<Higher<coproduct, W1>, W2>, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }
            };
        }





        public static <W1,W2,T> Unfoldable<Higher<Higher<coproduct, W1>, W2>> unfoldable(Coproduct<W1,W2,T> cop) {
            return new Unfoldable<Higher<Higher<coproduct, W1>, W2>>(){

                @Override
                public <R, T> Higher<Higher<Higher<coproduct, W1>, W2>, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
                    return cop.unfolds().orElse(null).unfold(b,fn);
                }
            };
        }
    }

}
