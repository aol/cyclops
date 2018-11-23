package cyclops.instances.reactive;

import com.oath.cyclops.hkt.DataWitness.io;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.reactive.IO;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.MonadPlus;
import cyclops.typeclasses.monad.MonadRec;
import cyclops.typeclasses.monad.MonadZero;
import cyclops.typeclasses.monad.Traverse;
import cyclops.typeclasses.monad.TraverseByTraverse;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.reactive.IO.narrowK;


public class IOInstances {
    public static  <T> Kleisli<io,IO<T>,T> kindKleisli(){
        return Kleisli.of(INSTANCE, IO::widen);
    }

    public static  <T> Cokleisli<io,T,IO<T>> kindCokleisli(){
        return Cokleisli.of(IO::narrowK);
    }
    public static <W1,T> Nested<io,W1,T> nested(IO<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, IOInstances.definitions(),def2);
    }
    public static <W1,T> Product<io,W1,T> product(IO<T> l, Active<W1,T> active){
        return Product.of(allTypeclasses(l),active);
    }
    public static <W1,T> Coproduct<W1, io,T> coproduct(IO<T> l, InstanceDefinitions<W1> def2){
        return Coproduct.right(l,def2, IOInstances.definitions());
    }
    public static <T> Active<io,T> allTypeclasses(IO<T> l){
        return Active.of(l, IOInstances.definitions());
    }
    public static <W2,R,T> Nested<io,W2,R> mapM(IO<T> l, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(l.map(fn), IOInstances.definitions(), defs);
    }

    public static InstanceDefinitions<io> definitions(){
        return new InstanceDefinitions<io>() {
            @Override
            public <T, R> Functor<io> functor() {
                return INSTANCE;
            }

            @Override
            public <T> Pure<io> unit() {
                return INSTANCE;
            }

            @Override
            public <T, R> Applicative<io> applicative() {
                return INSTANCE;
            }

            @Override
            public <T, R> Monad<io> monad() {
                return INSTANCE;
            }

            @Override
            public <T, R> Option<MonadZero<io>> monadZero() {
                return Option.some(INSTANCE);
            }

            @Override
            public <T> Option<MonadPlus<io>> monadPlus() {
                return Option.some(INSTANCE);
            }

            @Override
            public <T> MonadRec<io> monadRec() {
                return INSTANCE;
            }

            @Override
            public <T> Option<MonadPlus<io>> monadPlus(MonoidK<io> m) {
                return Option.some(INSTANCE.withMonoidK(m));
            }

            @Override
            public <C2, T> Traverse<io> traverse() {
                return INSTANCE;
            }

            @Override
            public <T> Foldable<io> foldable() {
                return INSTANCE;
            }

            @Override
            public <T> Option<Comonad<io>> comonad() {
                return Maybe.nothing();
            }

            @Override
            public <T> Option<Unfoldable<io>> unfoldable() {
                return Option.some(INSTANCE);
            }
        };
    }




    private final static IOInstances.IOTypeClasses INSTANCE = new IOInstances.IOTypeClasses();
    @AllArgsConstructor
    @Wither
    public static class IOTypeClasses implements MonadPlus<io>,
                                                MonadRec<io>,
                                                TraverseByTraverse<io>,
                                                Foldable<io>,
                                                Unfoldable<io>{

        private final MonoidK<io> monoidK;
        public IOTypeClasses(){
            monoidK = MonoidKs.combineIO();
        }
        @Override
        public <T> Higher<io, T> filter(Predicate<? super T> predicate, Higher<io, T> ds) {
            return IO.fromPublisher(narrowK(ds).stream().filter(predicate));
        }

        @Override
        public <T, R> Higher<io, Tuple2<T, R>> zip(Higher<io, T> fa, Higher<io, R> fb) {
            return narrowK(fa).zip(narrowK(fb), Tuple::tuple);
        }

        @Override
        public <T1, T2, R> Higher<io, R> zip(Higher<io, T1> fa, Higher<io, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb),f);
        }

        @Override
        public <T> MonoidK<io> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<io, R> flatMap(Function<? super T, ? extends Higher<io, R>> fn, Higher<io, T> ds) {
            return narrowK(ds).flatMap(i->narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<io, R> ap(Higher<io, ? extends Function<T, R>> fn, Higher<io, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn),(a,b)->b.apply(a));
        }

        @Override
        public <T> Higher<io, T> unit(T value) {
            return IO.of(value);
        }

        @Override
        public <T, R> Higher<io, R> map(Function<? super T, ? extends R> fn, Higher<io, T> ds) {
            return narrowK(ds).map(fn);
        }//}, Traverse<io>{

        @Override
        public <T, R> Higher<io, R> tailRec(T initial, Function<? super T, ? extends Higher<io, ? extends Either<T, R>>> fn) {
            return IO.fromPublisher(ReactiveSeq.tailRec(initial, i->narrowK(fn.apply(i)).stream() ));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<io, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<io, T> ds) {
            IO<T> v = narrowK(ds);

            return v.<Higher<C2, Higher<io,R>>>foldLeft(ap.unit(IO.<R>fromPublisher(Spouts.empty())),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> IO.fromPublisher(narrowK(vec).stream().plus(sn))));

        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<io, T> ds) {
            IO<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));

        }

        @Override
        public <T, R> Higher<io, Tuple2<T, Long>> zipWithIndex(Higher<io, T> ds) {
            return IO.fromPublisher(Spouts.from(narrowK(ds)).zipWithIndex());
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<io, T> ds) {
            return Spouts.from(narrowK(ds)).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<io, T> ds) {
            return narrowK(ds).foldLeft(monoid.zero(),monoid);
        }


        @Override
        public <R, T> Higher<io, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return IO.fromPublisher(Spouts.unfold(b,fn));
        }
    }

}
