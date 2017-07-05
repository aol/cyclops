package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher3;
import com.aol.cyclops2.hkt.Higher4;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.Zippable;
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
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of="run")
@Getter
public class Product2<W1,W2,T> implements  Filters<T>,Higher3<product,W1,W2,T>,
                                            Transformable<T>, To<Product2<W1,W2,T>> {

    private final Tuple2<Higher<W1,T>,Higher<W2,T>> run;
    private final InstanceDefinitions<W1> def1;
    private final InstanceDefinitions<W2> def2;


    public Tuple2<Higher<W1,T>,Higher<W2,T>> asTuple(){
        return run;
    }

    public static  <W1,W2,T> Product2<W1,W2,T> of(Tuple2<Higher<W1,T>,
            Higher<W2,T>> run, InstanceDefinitions<W1> def1, InstanceDefinitions<W2> def2){
        return new Product2<>(run,def1,def2);
    }
    public static  <W1,W2,T> Product2<W1,W2,T> of(Active<W1,T> a1, Active<W2,T> a2){
        return of(Tuple.tuple(a1.getSingle(),a2.getSingle()),a1.getDef1(),a2.getDef1());
    }
    

    public Product2<W1,W2,T> filter(Predicate<? super T> test) {
        return of(run.map((m1,m2)->{
            Higher<W2, T> x2 = def2.monadZero().get().filter(test,m2);
            Higher<W1,T> x1 = def1.monadZero().get().filter(test,m1);
            return Tuple.tuple(x1, x2);
        }),def1,def2);
    }



    @Override
    public <U> Product2<W1,W2,U> ofType(Class<? extends U> type) {
        return (Product2<W1,W2,U>)Filters.super.ofType(type);
    }

    @Override
    public Product2<W1,W2,T> filterNot(Predicate<? super T> predicate) {
        return filter(predicate.negate());
    }

    @Override
    public Product2<W1,W2,T> notNull() {
        return (Product2<W1,W2,T>)Filters.super.notNull();
    }

    @Override
    public <U> Product2<W1,W2,U> cast(Class<? extends U> type) {
        return (Product2<W1,W2,U>)Transformable.super.cast(type);
    }
    public Product2<W1,W2,Tuple2<T,T>> zip(Product2<W1,W2,T> p2){
        return zip(p2,Tuple::tuple);
    }
    public  Product2<W1,W2,Tuple3<T,T,T>> zip(Product2<W1,W2,T> p2, Product2<W1,W2,T> p3){
        return zip(p2,p3);
    }
    public <R> Product2<W1,W2,R> zip(Product2<W1,W2,T> p2,BiFunction<? super T,? super T, ? extends R> zipper){
        Active<W1,T> a1 = Active.of(run.v1,def1);
        Active<W1,T> a2 = Active.of(p2.run.v1,def1);

        Active<W1,R> a3 = a1.zip(a2,zipper);

        Active<W2,T> b1 = Active.of(run.v2,def2);
        Active<W2,T> b2 = Active.of(p2.run.v2,def2);

        Active<W2,R> b3 = b1.zip(b2,zipper);

        return of(a3,b3);

    }
    public <R> Product2<W1,W2,R> zip(Product2<W1,W2,T> p2,Product2<W1,W2,T> p3,Fn3<? super T,? super T, ? super T,? extends R> zipper){
        Active<W1,T> a1 = Active.of(run.v1,def1);
        Active<W1,T> a2 = Active.of(p2.run.v1,def1);
        Active<W1,T> a3 = Active.of(p3.run.v1,def1);

        Active<W1,R> a4 = a1.zip(a2,a3,zipper);

        Active<W2,T> b1 = Active.of(run.v2,def2);
        Active<W2,T> b2 = Active.of(p2.run.v2,def2);
        Active<W2,T> b3 = Active.of(p3.run.v2,def2);

        Active<W2,R> b4 = b1.zip(b2,b3,zipper);

        return of(a4,b4);
    }
    @Override
    public <R> Product2<W1,W2,R> map(Function<? super T, ? extends R> fn) {

        return of(run.map((m1,m2)->{
            Higher<W2, R> x2 = def2.<T, R>functor().map(fn, m2);
            Higher<W1,R> x1 = def1.<T, R>functor().map(fn, m1);
            return Tuple.tuple(x1, x2);
        }),def1,def2);
    }
    public <R> Product2<W1,W2,R> flatMap(Function<? super T, ? extends Product2<W1,W2,R>> fn) {
        return of(map(fn).run.map((m1,m2)->Tuple.tuple(def1.monad().flatMap(p->p.asTuple().v1,m1),def2.monad().flatMap(p->p.asTuple().v2,m2))),def1,def2);
    }


    @Override
    public Product2<W1,W2,T> peek(Consumer<? super T> c) {
        return map(a->{
            c.accept(a);
            return a;
        });
    }

    @Override
    public String toString() {
        return "Coproduct["+ run.toString()+"]";
    }

    @Override
    public <R> Product2<W1,W2,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Product2<W1,W2,R>)Transformable.super.trampoline(mapper);
    }

    @Override
    public <R> Product2<W1,W2,R> retry(Function<? super T, ? extends R> fn) {
        return (Product2<W1,W2,R>)Transformable.super.retry(fn);
    }

    @Override
    public <R> Product2<W1,W2,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (Product2<W1,W2,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }



    public <R> R visit(BiFunction<? super Higher<W1,? super T>,? super Higher<W2,? super T>, ? extends R> visitor){
        return run.map(visitor);
    }

    public Product2<W2,W1,T> swap(){
        return of(run.swap(),def2,def1);
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
           return run.map((a,b)->{
               T r1 = def1.foldable().get().foldRight(monoid,a);
               T r2 = def2.foldable().get().foldRight(monoid, b);
               return monoid.foldRightI(Arrays.asList(r1,r2));
            });


        }


        public T foldRight(T identity, BinaryOperator<T> semigroup) {
            return foldRight(Monoid.fromBiFunction(identity, semigroup));

        }

        public T foldLeft(Monoid<T> monoid) {
            return run.map((a,b)->{
                T r1 = def1.foldable().get().foldLeft(monoid,a);
                T r2 = def2.foldable().get().foldLeft(monoid, b);
                return monoid.foldLeftI(Arrays.asList(r1,r2));
            });
        }


        public T foldLeft(T identity, BinaryOperator<T> semigroup) {
            return foldLeft(Monoid.fromBiFunction(identity, semigroup));
        }

    }

    public class Traverse{
/**
        public <W3, R> Higher<W3,Product2<W1,W2, R>> traverse(InstanceDefinitions<W3> applicative, Function<? super T, Higher<W3, R>> f){
            run.map( (m1,m2)->{

                Higher<W3, Higher<W1, R>> x1 = def1.traverse().get().traverseA(applicative, f, m1);
                Higher<W3, Higher<W2, R>> x2 = def2.traverse().get().traverseA(applicative, f, m2);
                applicative.map(w1->applicative.map(Tuple.tuple(w1,x2))
            });
            return run.visit(it->{
                return applicative.map(x->of(Xor.secondary(x),def1,def2),def1.traverse().get().traverseA(applicative, f, it));
            },it->{
                return applicative.map(x->of(Xor.primary(x),def1,def2),def2.traverse().get().traverseA(applicative, f, it));
            });
        }
 **/


    }


    public <R1, R> Product2<W1,W2,R> forEach2(Function<? super T, ? extends Product2<W1,W2,R1>> value1, final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return flatMap(a->{
            return value1.apply(a).map(b->yieldingFunction.apply(a,b));
        });

    }
    public <T2, R1, R2, R> Product2<W1,W2,R> forEach3(final Function<? super T, ? extends Product2<W1,W2,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Product2<W1,W2,R2>> value2,
                                                      final Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return flatMap(a->{
            return value1.apply(a).flatMap(b->value2.apply(a,b).map(c->yieldingFunction.apply(a,b,c)));
        });
    }
    public <T2, R1, R2, R3, R> Product2<W1,W2,R> forEach4(final Function<? super T, ? extends Product2<W1,W2,R1>> value1, final BiFunction<? super T, ? super R1, ? extends Product2<W1,W2,R2>> value2, final Fn3<? super T, ? super R1, ? super R2, ? extends Product2<W1,W2,R3>> value3,
                                                    final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return flatMap(a->{
            return value1.apply(a).flatMap(b->value2.apply(a,b).flatMap(c->value3.apply(a,b,c).map(d->yieldingFunction.apply(a,b,c,d))));
        });
    }

    public static <W1,W2,T>  Product2<W1,W2,T> narrowK(Higher<Higher<Higher<product, W1>, W2>, T> ds){
        return (Product2<W1,W2,T>)ds;
    }
    public static class Instances<W1,W2> implements InstanceDefinitions<Higher<Higher<product, W1>, W2>>{
        InstanceDefinitions<W1> def1;
        InstanceDefinitions<W2> def2;

        public static <W1,W2> InstanceDefinitions<Higher<Higher<product, W1>, W2>> definitions(){
            return new Instances<>();
        }

        public  Functor<Higher<Higher<product, W1>, W2>> functor(){
            return new Functor<Higher<Higher<product, W1>, W2>>(){

                @Override
                public <T, R> Higher<Higher<Higher<product, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<product, W1>, W2>, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }
        public  Pure<Higher<Higher<product, W1>, W2>> unit(){
            return new Pure<Higher<Higher<product, W1>, W2>>(){

                @Override
                public <T> Higher<Higher<Higher<product, W1>, W2>, T> unit(T value) {
                    return Product2.of(Active.of(def1,value), Active.of(def2, value));

                }
            };
        }
        public  Applicative<Higher<Higher<product, W1>, W2>> applicative(){
            return new Applicative<Higher<Higher<product, W1>, W2>>(){

                @Override
                public <T, R> Higher<Higher<Higher<product, W1>, W2>, R> ap(Higher<Higher<Higher<product, W1>, W2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<product, W1>, W2>, T> apply) {
                    return narrowK(fn).flatMap(x -> narrowK(apply).map(x));

                }

                @Override
                public <T, R> Higher<Higher<Higher<product, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<product, W1>, W2>, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<product, W1>, W2>, T> unit(T value) {
                    return Instances.this.<T>unit().unit(value);
                }
            };
        }
        public Monad<Higher<Higher<product, W1>, W2>> monad(){
            return new Monad<Higher<Higher<product, W1>, W2>>(){

                @Override
                public <T, R> Higher<Higher<Higher<product, W1>, W2>, R> ap(Higher<Higher<Higher<product, W1>, W2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<product, W1>, W2>, T> apply) {
                    return applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<Higher<product, W1>, W2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<product, W1>, W2>, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<product, W1>, W2>, T> unit(T value) {
                    return Instances.this.<T>unit().unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<product, W1>, W2>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<product, W1>, W2>, R>> fn, Higher<Higher<Higher<product, W1>, W2>, T> ds) {
                    return narrowK(ds).flatMap(fn.andThen(Product2::narrowK));
                }
            };
        }

        @Override
        public <T, R> Maybe<MonadZero<Higher<Higher<product, W1>, W2>>> monadZero() {
            return Maybe.none();
        }

        @Override
        public <T> Maybe<MonadPlus<Higher<Higher<product, W1>, W2>>> monadPlus() {
            return Maybe.none();
        }

        @Override
        public <T> Maybe<MonadPlus<Higher<Higher<product, W1>, W2>>> monadPlus(Monoid<Higher<Higher<Higher<product, W1>, W2>, T>> m) {
            return Maybe.none();
        }

        @Override
        public <C2, T> Maybe<cyclops.typeclasses.monad.Traverse<Higher<Higher<product, W1>, W2>>> traverse() {
            return Maybe.none();
        }

        @Override
        public <T> Maybe<Foldable<Higher<Higher<product, W1>, W2>>> foldable() {
            return Maybe.none();
        }

        @Override
        public <T> Maybe<Comonad<Higher<Higher<product, W1>, W2>>> comonad() {
            return Maybe.none();
        }

        @Override
        public <T> Maybe<Unfoldable<Higher<Higher<product, W1>, W2>>> unfoldable() {
            return Maybe.none();
        }
    }





}
