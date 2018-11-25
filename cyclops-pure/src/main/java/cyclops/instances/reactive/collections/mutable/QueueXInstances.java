package cyclops.instances.reactive.collections.mutable;


import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.reactive.collections.mutable.QueueX;
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
import lombok.experimental.UtilityClass;
import lombok.experimental.Wither;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.oath.cyclops.data.ReactiveWitness.queue;
import static cyclops.reactive.collections.mutable.QueueX.narrowK;

/**
 * Companion class for creating Type Class instances for working with Queues
 * @author johnmcclean
 *
 */
@UtilityClass
public class QueueXInstances {
    public static <T> Kleisli<queue, QueueX<T>, T> kindKleisli() {
        return Kleisli.of(QueueXInstances.monad(), QueueX::widen);
    }

    public static <T> Cokleisli<queue, T, QueueX<T>> kindCokleisli() {
        return Cokleisli.of(QueueX::narrowK);
    }

    public static <W1, T> Nested<queue, W1, T> nested(QueueX<Higher<W1, T>> nested, InstanceDefinitions<W1> def2) {
        return Nested.of(nested, QueueXInstances.definitions(), def2);
    }

    public static <W1, T> Product<queue, W1, T> product(QueueX<T> q, Active<W1, T> active) {
        return Product.of(allTypeclasses(q), active);
    }

    public static <W1, T> Coproduct<W1, queue, T> coproduct(QueueX<T> q, InstanceDefinitions<W1> def2) {
        return Coproduct.right(q, def2, QueueXInstances.definitions());
    }

    public static <T> Active<queue, T> allTypeclasses(QueueX<T> q) {
        return Active.of(q, QueueXInstances.definitions());
    }

    public static <W2, R, T> Nested<queue, W2, R> mapM(QueueX<T> q, Function<? super T, ? extends Higher<W2, R>> fn, InstanceDefinitions<W2> defs) {
        return Nested.of(q.map(fn), QueueXInstances.definitions(), defs);
    }

    public static InstanceDefinitions<queue> definitions() {
        return new InstanceDefinitions<queue>() {
            @Override
            public <T, R> Functor<queue> functor() {
                return QueueXInstances.functor();
            }

            @Override
            public <T> Pure<queue> unit() {
                return QueueXInstances.unit();
            }

            @Override
            public <T, R> Applicative<queue> applicative() {
                return QueueXInstances.zippingApplicative();
            }

            @Override
            public <T, R> Monad<queue> monad() {
                return QueueXInstances.monad();
            }

            @Override
            public <T, R> Option<MonadZero<queue>> monadZero() {
                return Option.some(QueueXInstances.monadZero());
            }

            @Override
            public <T> Option<MonadPlus<queue>> monadPlus() {
                return Option.some(QueueXInstances.monadPlus());
            }

            @Override
            public <T> MonadRec<queue> monadRec() {
                return QueueXInstances.monadRec();
            }

            @Override
            public <T> Option<MonadPlus<queue>> monadPlus(MonoidK<queue> m) {
                return Option.some(QueueXInstances.monadPlus(m));
            }

            @Override
            public <C2, T> Traverse<queue> traverse() {
                return QueueXInstances.traverse();
            }

            @Override
            public <T> Foldable<queue> foldable() {
                return QueueXInstances.foldable();
            }

            @Override
            public <T> Option<Comonad<queue>> comonad() {
                return Maybe.nothing();
            }

            @Override
            public <T> Option<Unfoldable<queue>> unfoldable() {
                return Option.some(QueueXInstances.unfoldable());
            }
        };
    }

    public static Pure<queue> unit() {
        return INSTANCE;
    }

    private final static QueueXTypeClasses INSTANCE = new QueueXTypeClasses();

    @AllArgsConstructor
    @Wither
    public static class QueueXTypeClasses implements MonadPlus<queue>,
        MonadRec<queue>,
        TraverseByTraverse<queue>,
        Foldable<queue>,
        Unfoldable<queue> {

        private final MonoidK<queue> monoidK;

        public QueueXTypeClasses() {
            monoidK = MonoidKs.queueXConcat();
        }

        @Override
        public <T> Higher<queue, T> filter(Predicate<? super T> predicate, Higher<queue, T> ds) {
            return narrowK(ds).filter(predicate);
        }

        @Override
        public <T, R> Higher<queue, Tuple2<T, R>> zip(Higher<queue, T> fa, Higher<queue, R> fb) {
            return narrowK(fa).zip(narrowK(fb));
        }

        @Override
        public <T1, T2, R> Higher<queue, R> zip(Higher<queue, T1> fa, Higher<queue, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb), f);
        }

        @Override
        public <T> MonoidK<queue> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<queue, R> flatMap(Function<? super T, ? extends Higher<queue, R>> fn, Higher<queue, T> ds) {
            return narrowK(ds).concatMap(i -> narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<queue, R> ap(Higher<queue, ? extends Function<T, R>> fn, Higher<queue, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn), (a, b) -> b.apply(a));
        }

        @Override
        public <T> Higher<queue, T> unit(T value) {
            return QueueX.of(value);
        }

        @Override
        public <T, R> Higher<queue, R> map(Function<? super T, ? extends R> fn, Higher<queue, T> ds) {
            return narrowK(ds).map(fn);
        }


        @Override
        public <T, R> Higher<queue, R> tailRec(T initial, Function<? super T, ? extends Higher<queue, ? extends Either<T, R>>> fn) {
            return QueueX.tailRec(initial, i -> narrowK(fn.apply(i)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<queue, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<queue, T> ds) {
            QueueX<T> v = narrowK(ds);
            return v.<Higher<C2, Higher<queue, R>>>foldLeft(ap.unit(QueueX.<R>empty()),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<queue, T> ds) {
            QueueX<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(), (a, b) -> mb.apply(a, fn.apply(b)));
        }

        @Override
        public <T, R> Higher<queue, Tuple2<T, Long>> zipWithIndex(Higher<queue, T> ds) {
            return narrowK(ds).zipWithIndex();
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<queue, T> ds) {
            return narrowK(ds).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<queue, T> ds) {
            return narrowK(ds).foldLeft(monoid);
        }


        @Override
        public <R, T> Higher<queue, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return QueueX.unfold(b, fn);
        }


    }

    public static Unfoldable<queue> unfoldable() {

        return INSTANCE;
    }

    public static MonadPlus<queue> monadPlus(MonoidK<queue> m) {

        return INSTANCE.withMonoidK(m);
    }

    public static <T, R> Applicative<queue> zippingApplicative() {
        return INSTANCE;
    }

    public static <T, R> Functor<queue> functor() {
        return INSTANCE;
    }

    public static <T, R> Monad<queue> monad() {
        return INSTANCE;
    }

    public static <T, R> MonadZero<queue> monadZero() {

        return INSTANCE;
    }

    public static <T> MonadPlus<queue> monadPlus() {

        return INSTANCE;
    }

    public static <T, R> MonadRec<queue> monadRec() {

        return INSTANCE;
    }


    public static <C2, T> Traverse<queue> traverse() {
        return INSTANCE;
    }

    public static <T, R> Foldable<queue> foldable() {
        return INSTANCE;
    }

}
