package cyclops.control;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.anyM.AnyMValue;
import cyclops.async.Future;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.identity;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Identity<T> implements Higher<identity,T>, Iterable<T> {
     private final T value;

     public static <T> Identity<T> of(T value){
         return new Identity<>(value);
     }

    public static <W1,T> Nested<identity,W1,T> nested(Identity<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    public <W1> Product<identity,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    public <W1> Coproduct<W1,identity,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
     public T get(){
         return value;
     }
     public T extract(){
         return value;
     }
     public <R> R visit(Function<? super T, ? extends R> fn){
         return fn.apply(value);
     }
     public Identity<Identity<T>> nest(){
         return of(this);
     }
     public <R> Identity<R> coflatMap(Function<? super Identity<? super T>, ? extends R> fn){
         return of(fn.apply(this));
     }
     public <R> Identity<R> map(Function<? super T,? extends R> fn){
         return new Identity<>(fn.apply(value));
     }
    public <R> Identity<R> flatMap(Function<? super T,? extends Identity<? extends R>> fn){
        return narrow(fn.apply(value));
    }

    public AnyMValue<identity,T> anyM(){
        return AnyM.fromIdentity(this);
    }

    public static <T> Identity<T> narrow(Identity<? extends T> id){
        return (Identity<T>)id;
    }
    public static <T> Identity<T> narrowK(Higher<identity,T> ds){
        return (Identity<T>)ds;
    }

    public Active<identity,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }

    public <W2,R> Nested<identity,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }

    @Override
    public Iterator<T> iterator() {
        return Arrays.asList(value).iterator();
    }

    public static class Instances{

        public static InstanceDefinitions<identity> definitions(){
            return new InstanceDefinitions<identity>() {
                @Override
                public <T, R> Functor<identity> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<identity> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<identity> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<identity> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<identity>> monadZero() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<MonadPlus<identity>> monadPlus() {
                    return Maybe.none();
                }

                @Override
                public <T> MonadRec<identity> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<identity>> monadPlus(Monoid<Higher<identity, T>> m) {
                    return Maybe.none();
                }

                @Override
                public <C2, T> Maybe<Traverse<identity>> traverse() {
                    return Maybe.just(Instances.traverse());
                }

                @Override
                public <T> Maybe<Foldable<identity>> foldable() {
                    return Maybe.just(Instances.foldable());
                }

                @Override
                public <T> Maybe<Comonad<identity>> comonad() {
                    return Maybe.just(Instances.comonad());
                }

                @Override
                public <T> Maybe<Unfoldable<identity>> unfoldable() {
                    return Maybe.none();
                }
            };
        }

        public static Functor<identity> functor(){
            return new Functor<identity>(){

                @Override
                public <T, R> Higher<identity, R> map(Function<? super T, ? extends R> fn, Higher<identity, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }

        public static Pure<identity> unit(){
            return new Pure<identity>(){


                @Override
                public <T> Higher<identity, T> unit(T value) {
                    return of(value);
                }
            };
        }
        public static Applicative<identity> applicative(){
            return new Applicative<identity>(){


                @Override
                public <T, R> Higher<identity, R> ap(Higher<identity, ? extends Function<T, R>> fn, Higher<identity, T> apply) {
                    Identity<? extends Function<T, R>> f = narrowK(fn);
                    Identity<T> ap = narrowK(apply);
                    return f.flatMap(x -> ap.map(x));
                }

                @Override
                public <T, R> Higher<identity, R> map(Function<? super T, ? extends R> fn, Higher<identity, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<identity, T> unit(T value) {
                    return Instances.unit().unit(value);
                }
            };
        }
        public static Monad<identity> monad(){
            return new Monad<identity>(){


                @Override
                public <T, R> Higher<identity, R> ap(Higher<identity, ? extends Function<T, R>> fn, Higher<identity, T> apply) {
                    return applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<identity, R> map(Function<? super T, ? extends R> fn, Higher<identity, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<identity, T> unit(T value) {
                    return Instances.unit().unit(value);
                }

                @Override
                public <T, R> Higher<identity, R> flatMap(Function<? super T, ? extends Higher<identity, R>> fn, Higher<identity, T> ds) {
                    return narrowK(ds).flatMap(fn.andThen(i->narrowK(i)));
                }
            };
        }
        public static  MonadRec<identity> monadRec() {

            return new MonadRec<identity>(){
                @Override
                public <T, R> Higher<identity, R> tailRec(T initial, Function<? super T, ? extends Higher<identity, ? extends Xor<T, R>>> fn) {
                    Identity<? extends Xor<T, R>> next[] = new Identity[1];
                    next[0] = Identity.of(Xor.secondary(initial));
                    boolean cont = true;
                    do {

                        cont = next[0].visit(p -> p.visit(s -> {
                            next[0] = narrowK(fn.apply(s));
                            return true;
                        }, __ -> false));
                    } while (cont);
                    return next[0].map(Xor::get);
                }




            };


        }
        public static Traverse<identity> traverse(){
            return new Traverse<identity>(){

                @Override
                public <T, R> Higher<identity, R> ap(Higher<identity, ? extends Function<T, R>> fn, Higher<identity, T> apply) {
                    return applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<identity, R> map(Function<? super T, ? extends R> fn, Higher<identity, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<identity, T> unit(T value) {
                    return Instances.unit().unit(value);
                }

                @Override
                public <C2, T, R> Higher<C2, Higher<identity, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<identity, T> ds) {
                    Identity<T> id = narrowK(ds);
                    Function<R, Identity<R>> rightFn = r -> of(r);
                    return applicative.map(rightFn, fn.apply(id.value));
                }

                @Override
                public <C2, T> Higher<C2, Higher<identity, T>> sequenceA(Applicative<C2> applicative, Higher<identity, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }
            };
        }
        public static Foldable<identity> foldable(){
            return new Foldable<identity>(){


                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<identity, T> ds) {
                    return monoid.apply(narrowK(ds).get(),monoid.zero());
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<identity, T> ds) {
                    return monoid.apply(monoid.zero(),narrowK(ds).get());
                }
            };
        }
        public static Comonad<identity> comonad(){
            return new ComonadByPure<identity>(){


                @Override
                public <T> T extract(Higher<identity, T> ds) {
                    return narrowK(ds).extract();
                }

                @Override
                public <T, R> Higher<identity, R> map(Function<? super T, ? extends R> fn, Higher<identity, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<identity, T> unit(T value) {
                    return Instances.unit().unit(value);
                }
            };
        }

    }

}
