package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher3;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.async.Future;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.CompletableFutures;
import cyclops.companion.CompletableFutures.CompletableFutureKind;
import cyclops.companion.Optionals;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.companion.Streams;
import cyclops.companion.Streams.StreamKind;
import cyclops.control.*;
import cyclops.control.lazy.Either;
import cyclops.control.lazy.Eval;
import cyclops.control.lazy.Maybe;
import cyclops.control.lazy.Trampoline;
import cyclops.function.Monoid;
import cyclops.monads.Witness.*;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.SemigroupK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

import static cyclops.collections.tuple.Tuple.tuple;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of="xor")
@Getter
public class Coproduct<W1,W2,T> implements  Filters<T>,Higher3<coproduct,W1,W2,T>,
                                            Transformable<T>, To<Coproduct<W1,W2,T>> {

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
    public Active<W1,T> activeLeft(MonoidK<W1,T> m, Higher<W1,T> concat){
        Higher<W1, T> h = xor.visit(s -> m.apply(s, concat), p -> m.zero());
        return Active.of(h,def1);
    }
    public Active<W2,T> activeSecond(MonoidK<W2,T> m, Higher<W2,T> concat){
        Higher<W2, T> h = xor.visit(s -> m.zero(), p -> m.apply(p, concat));
        return Active.of(h,def2);
    }

    public Coproduct<W1,W2,T> plusLeft(SemigroupK<W1,T> semigroupK, Higher<W1,T> add){
        return of(xor.secondaryFlatMap(s -> Xor.secondary(semigroupK.apply(s, add))),def1,def2);
    }
    public Coproduct<W1,W2,T> plusRight(SemigroupK<W2,T> semigroupK, Higher<W2,T> add){
        return of(xor.flatMap(p -> Xor.primary(semigroupK.apply(p, add))),def1,def2);
    }

    public Product<W1,W2,T> product(MonoidK<W1,T> m1, MonoidK<W2,T> m2){
        return Product.of(xor.visit(s -> Tuple.tuple(s, m2.zero()), p -> Tuple.tuple(m1.zero(), p)),def1,def2);
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
    public <R> Coproduct<W1,W2,R> mapWithIndex(BiFunction<? super T,Long,? extends R> f) {
        return of(xor.map(m->{
        Higher<W2, ? extends R> x = def2.<T, R>traverse().mapWithIndex(f, m);
        return (Higher<W2, R>)x;
    }).secondaryMap(m->{
        Higher<W1, ? extends R> x = def1.<T, R>traverse().mapWithIndex(f, m);
        return (Higher<W1, R>)x;
    }),def1,def2);

    }
    public <R> Coproduct<W1,W2,Tuple2<T,Long>> zipWithIndex() {
        return mapWithIndex(Tuple::tuple);
    }

    public Xor<Higher<W1,T>,Higher<W2,T>> asXor(){
        return xor;
    }
    public Xor<Active<W1,T>,Active<W2,T>> asActiveXor(){
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


    public Maybe<Plus> plus(){
        MonadPlus<W1> plus1 = def1.monadPlus().visit(p->p,()->null);
        MonadPlus<W2> plus2 = def2.monadPlus().visit(p->p,()->null);
        return xor.visit(s->def1.monadPlus().isPresent() ? Maybe.just(new Plus(plus1,plus2)) : Maybe.nothing(),
                                p->def2.monadPlus().isPresent() ? Maybe.just(new Plus(plus1,plus2)) : Maybe.nothing());
    }

    public Unfolds unfoldsDefault(){
        Unfoldable<W1> unf1 = def1.unfoldable().visit(a->  a ,()-> new Unfoldable.UnsafeValueUnfoldable<>());
        Unfoldable<W2> unf2 = def2.unfoldable().visit(a->  a ,()-> new Unfoldable.UnsafeValueUnfoldable<>());
        return new Unfolds(unf1,unf2);
    }


    public Maybe<Unfolds> unfolds(){
        Unfoldable<W1> unf1 = def1.unfoldable().visit(a->  a ,()-> new Unfoldable.UnsafeValueUnfoldable<>());
        Unfoldable<W2> unf2 = def2.unfoldable().visit(a->  a ,()-> new Unfoldable.UnsafeValueUnfoldable<>());

        return xor.visit(s-> def1.unfoldable().isPresent() ? Maybe.just(new Unfolds(unf1,unf2)) : Maybe.nothing(), p-> def2.unfoldable().isPresent() ? Maybe.just(new Unfolds(unf1,unf2)) : Maybe.nothing());
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Plus{
        private final  MonadPlus<W1> plus1;
        private final  MonadPlus<W2> plus2;
        public Coproduct<W1,W2,T> plus(Coproduct<W1,W2,T> a){

            if(xor.isSecondary() && a.xor.isSecondary()){
                    Higher<W1, T> plused = plus1.plus(xor.secondaryOrElse(null), a.xor.secondaryOrElse(null));
                    return Coproduct.left(plused,def1,def2);
            }
            if(xor.isPrimary() && a.xor.isPrimary()){
                Higher<W2, T> plused = plus2.plus(xor.orElse(null), a.getXor().orElse(null));
                return Coproduct.right(plused,def1,def2);
            }
            return Coproduct.this;

        }
        public Coproduct<W1,W2,T> sum(ListX<Coproduct<W1,W2,T>> l){
            ListX<Coproduct<W1,W2,T>> list = l.plus(Coproduct.this);
            if(xor.isSecondary()){
                Higher<W1, T> summed = plus1.sum(list.map(c -> c.xor.secondaryOrElse(null)));
                return Coproduct.left(summed,def1,def2);
            }
            if(xor.isPrimary()){
                Higher<W2, T> summed = plus2.sum(list.map(c -> c.xor.orElse(null)));
                return Coproduct.right(summed,def1,def2);
            }
            return Coproduct.this;
        }

    }


    public <R> R foldMap(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
        return xor.visit(left->def1.foldable().foldMap(mb, fn, left),
                right->def2.foldable().foldMap(mb, fn, right));
    }
    public T foldRight(Monoid<T> monoid) {
        return xor.visit(left->def1.foldable().foldRight(monoid, left),
                right->def2.foldable().foldRight(monoid, right));

    }

    public T foldRight(T identity, BinaryOperator<T> semigroup) {
        return foldRight(Monoid.fromBiFunction(identity,semigroup));

    }
    public  ListX<T> toListX(){
        return xor.visit(left->def1.foldable().listX(left),
                right->def2.foldable().listX(right));
    }
    public  ReactiveSeq<T> stream(){
        return toListX().stream();
    }
    public Coproduct<W1,W2,T> reverse() {
        return xor.visit(l -> {
            return Coproduct.of(Xor.secondary(def1.traverse().reverse(l)), def1, def2);
        }, r -> {
            return Coproduct.of(Xor.primary(def2.traverse().reverse(r)), def1, def2);
        });

    }
    public  long size() {
        return xor.visit(left->def1.foldable().size(left),
                right->def2.foldable().size(right));
    }

    public T foldLeft(Monoid<T> monoid) {
        return xor.visit(left->def1.foldable().foldLeft(monoid, left),
                right->def2.foldable().foldLeft(monoid, right));
    }


    public T foldLeft(T identity, BinaryOperator<T> semigroup) {
        return foldLeft(Monoid.fromBiFunction(identity,semigroup));
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Unfolds{
        private final Unfoldable<W1> unf1;
        private final Unfoldable<W2> unf2;
        public <R, T> Coproduct<W1,W2, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn){
            Xor<Higher<W1,R>,Higher<W2,R>> res = xor.visit(left -> Xor.secondary(unf1.unfold(b, fn)), r -> Xor.primary(unf2.unfold(b, fn)));
            Coproduct<W1, W2, R> cop = Coproduct.of(res, def1, def2);
            return cop;
        }

        public <T> Coproduct<W1,W2,T> replicate(int n, T value) {
            return unfold(n,i -> Optional.of(tuple(value, i-1)));
        }

        public <R> Coproduct<W1,W2,R> none() {
            return unfold((T) null, t -> Optional.<Tuple2<R, T>>empty());
        }
        public <T> Coproduct<W1,W2,T> one(T a) {
            return replicate(1, a);
        }

    }

    public <W3, R> Higher<W3,Coproduct<W1,W2, R>> traverseA(Applicative<W3> applicative, Function<? super T, Higher<W3, R>> f){
        return traverseA(applicative,f,this);
    }
    public static <W1,W2,T,C2, R> Higher<C2, Coproduct<W1,W2,R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,Coproduct<W1,W2,T> n){

        return n.xor.visit(it->{
            return applicative.map(x->of(Xor.secondary(x),n.def1,n.def2),n.def1.traverse().traverseA(applicative, fn, it));
        },it->{
            return applicative.map(x->of(Xor.primary(x),n.def1,n.def2),n.def2.traverse().traverseA(applicative, fn, it));
        });

    }
    public  <C2,T> Higher<C2, Coproduct<W1,W2,T>> sequenceA(Applicative<C2> applicative,
                                                          Coproduct<W1,W2,Higher<C2,T>> ds){
        return traverseA(applicative, i -> i, ds);

    }



    public static  <W1,T> Coproduct<W1,vectorX,T> vectorX(VectorX<T> list,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(list),def1, VectorX.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,vectorX,T> vectorX(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Xor.primary(VectorX.of(values)),def1, VectorX.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,linkedListX,T> linkedListX(LinkedListX<T> list,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(list),def1, LinkedListX.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,linkedListX,T> linkedListX(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Xor.primary(LinkedListX.of(values)),def1, LinkedListX.Instances.definitions());
    }


    public static  <W1,T> Coproduct<W1,list,T> listX(List<T> list,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(ListX.fromIterable(list)),def1, ListX.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,list,T> listX(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Xor.primary(ListX.of(values)),def1, ListX.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,stream,T> stream(Stream<T> stream,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(StreamKind.widen(stream)),def1, Streams.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,stream,T> stream(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Xor.primary(StreamKind.of(values)),def1, Streams.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,reactiveSeq,T> reactiveSeq(ReactiveSeq<T> stream,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(stream),def1,ReactiveSeq.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,reactiveSeq,T> reactiveSeq(InstanceDefinitions<W1> def1,T... values){
        return new Coproduct<>(Xor.primary(ReactiveSeq.of(values)),def1,ReactiveSeq.Instances.definitions());
    }
    public static  <W1,X extends Throwable,T> Coproduct<W1,Higher<tryType,X>,T> success(T value,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(Try.success(value)),def1, Try.Instances.definitions());
    }
    public static  <W1,X extends Throwable,T> Coproduct<W1,Higher<tryType,X>,T> failure(X value,InstanceDefinitions<W1> def1){
        return new Coproduct<W1,Higher<tryType,X>,T>(Xor.primary(Try.failure(value)),def1,Try.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,future,T> futureOf(Supplier<T> value, Executor ex,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(Future.of(value, ex)),def1,Future.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,completableFuture,T> completableFutureOf(Supplier<T> value, Executor ex,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(CompletableFutureKind.supplyAsync(value, ex)),def1, CompletableFutures.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,eval,T> later(Supplier<T> value,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Either.right(Eval.later(value)),def1,Eval.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,optional,T> ofNullable(T value,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(OptionalKind.ofNullable(value)),def1,Optionals.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,maybe,T> just(T value,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(Maybe.just(value)),def1,Maybe.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,maybe,T> none(InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(Maybe.nothing()),def1,Maybe.Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,maybe,T> maybeNullabe(T value,InstanceDefinitions<W1> def1){
        return new Coproduct<>(Xor.primary(Maybe.ofNullable(value)),def1,Maybe.Instances.definitions());
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


        public <T> Pure<Higher<Higher<coproduct, W1>, W2>> unit(InstanceDefinitions<W1> def1,InstanceDefinitions def2) {
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
                public <R, T> Higher<Higher<Higher<coproduct, W1>, W2>, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return cop.unfolds().orElse(null).unfold(b,fn);
                }
            };
        }
    }

}
