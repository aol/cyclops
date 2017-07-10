package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher3;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.async.Future;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.companion.CompletableFutures;
import cyclops.companion.CompletableFutures.CompletableFutureKind;
import cyclops.companion.Optionals;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.companion.Streams;
import cyclops.companion.Streams.StreamKind;
import cyclops.control.*;
import cyclops.control.lazy.Either;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.comonad.Comonad;
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
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

import static org.jooq.lambda.tuple.Tuple.tuple;

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

    /**
     * @return An Instance of Folds that will use Monoid.zero if the foldable type class is unavailable
     */
    public Folds foldsUnsafe(){
        return new Folds();
    }

    public Unfolds unfoldsUnsafe(){
        return xor.visit(s-> def1.unfoldable().isPresent() ? new Unfolds() : null,p-> def2.foldable().isPresent() ? new Unfolds() : null);
    }
    public Traverse traverseUnsafe(){
        return xor.visit(s-> def1.traverse().isPresent() ? new Traverse() : null,p-> def2.traverse().isPresent() ? new Traverse() : null);
    }

    public Maybe<Folds> folds(){
        return xor.visit(s-> def1.foldable().isPresent() ? Maybe.just(new Folds()) : Maybe.none(),p-> def2.foldable().isPresent() ? Maybe.just(new Folds()) : Maybe.none());
    }
    public Maybe<Unfolds> unfolds(){
        return xor.visit(s-> def1.unfoldable().isPresent() ? Maybe.just(new Unfolds()) : Maybe.none(),p-> def2.unfoldable().isPresent() ? Maybe.just(new Unfolds()) : Maybe.none());
    }
    public Maybe<Traverse> traverse(){
        return xor.visit(s-> def1.traverse().isPresent() ? Maybe.just(new Traverse()) : Maybe.none(),p-> def2.traverse().isPresent() ? Maybe.just(new Traverse()) : Maybe.none());
    }
    public class Plus{

        public Coproduct<W1,W2,T> plus(Coproduct<W1,W2,T> a){

            if(xor.isSecondary() && a.xor.isSecondary()){
                    Higher<W1, T> plused = def1.monadPlus().get().plus(xor.secondaryGet(), a.xor.secondaryGet());
                    return Coproduct.left(plused,def1,def2);
            }
            if(xor.isPrimary() && a.xor.isPrimary()){
                Higher<W2, T> plused = def2.monadPlus().get().plus(xor.get(), a.getXor().get());
                return Coproduct.right(plused,def1,def2);
            }
            return Coproduct.this;

        }
        public Coproduct<W1,W2,T> sum(ListX<Coproduct<W1,W2,T>> l){
            ListX<Coproduct<W1,W2,T>> list = l.plus(Coproduct.this);
            if(xor.isSecondary()){
                Higher<W1, T> summed = def1.monadPlus().get().sum(list.map(c -> c.xor.secondaryGet()));
                return Coproduct.left(summed,def1,def2);
            }
            if(xor.isPrimary()){
                Higher<W2, T> summed = def2.monadPlus().get().sum(list.map(c -> c.xor.get()));
                return Coproduct.right(summed,def1,def2);
            }
            return Coproduct.this;
        }

    }


    public class Folds {

        public <R> R foldMap(final Monoid<R> mb, final Function<? super T,? extends R> fn) {
            return xor.visit(left->def1.foldable().visit(p -> p.foldMap(mb, fn, left), () -> mb.zero()),
                            right->def2.foldable().visit(p -> p.foldMap(mb, fn, right), () -> mb.zero()));
        }
        public T foldRight(Monoid<T> monoid) {
            return xor.visit(left->def1.foldable().visit(p->p.foldRight(monoid, left),()->monoid.zero()),
                    right->def2.foldable().visit(p->p.foldRight(monoid, right),()->monoid.zero()));

        }

        public T foldRight(T identity, BinaryOperator<T> semigroup) {
            return foldRight(Monoid.fromBiFunction(identity,semigroup));

        }

        public T foldLeft(Monoid<T> monoid) {
            return xor.visit(left->def1.foldable().visit(p->p.foldLeft(monoid, left),()->monoid.zero()),
                    right->def2.foldable().visit(p->p.foldLeft(monoid, right),()->monoid.zero()));
        }


        public T foldLeft(T identity, BinaryOperator<T> semigroup) {
            return foldLeft(Monoid.fromBiFunction(identity,semigroup));
        }

    }

    public class Unfolds{
        public <R, T> Coproduct<W1,W2, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn){
            Xor<Higher<W1,R>,Higher<W2,R>> res = xor.visit(left -> Xor.secondary(def1.unfoldable().get().unfold(b, fn)), r -> Xor.primary(def2.unfoldable().get().unfold(b, fn)));
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
    public class Traverse{

        public <W3, R> Higher<W3,Coproduct<W1,W2, R>> traverse(Applicative<W3> applicative, Function<? super T, Higher<W3, R>> f){
            return xor.visit(it->{
                return applicative.map(x->of(Xor.secondary(x),def1,def2),def1.traverse().get().traverseA(applicative, f, it));
            },it->{
                return applicative.map(x->of(Xor.primary(x),def1,def2),def2.traverse().get().traverseA(applicative, f, it));
            });
        }


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
        return new Coproduct<>(Xor.primary(Maybe.none()),def1,Maybe.Instances.definitions());
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
                    return narrowK(ds).foldsUnsafe().foldRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<coproduct, W1>, W2>, T> ds) {
                    return narrowK(ds).foldsUnsafe().foldLeft(monoid);
                }
            };
        }





        public static <W1,W2,T> Unfoldable<Higher<Higher<coproduct, W1>, W2>> unfoldable(Coproduct<W1,W2,T> cop) {
            return new Unfoldable<Higher<Higher<coproduct, W1>, W2>>(){

                @Override
                public <R, T> Higher<Higher<Higher<coproduct, W1>, W2>, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return cop.unfolds().get().unfold(b,fn);
                }
            };
        }
    }

}
