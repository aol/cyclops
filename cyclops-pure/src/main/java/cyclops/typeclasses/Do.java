package cyclops.typeclasses;

import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.State;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function1;
import cyclops.function.Function2;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Function5;
import cyclops.function.Monoid;
import cyclops.function.Predicate3;
import cyclops.function.Predicate4;
import cyclops.function.Predicate5;
import cyclops.instances.control.StateInstances;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Compose;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.MonadPlus;
import cyclops.typeclasses.monad.MonadZero;
import cyclops.typeclasses.monad.Traverse;
import lombok.AllArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class Do<W> {

    private final Monad<W> monad;

    private Do(Monad<W> monad) {

        this.monad = monad;
    }

    public <T1> Do1<T1> __(Higher<W, T1> a) {
        return new Do1<>(()->a);
    }

    public <T1> Do1<T1> _of(T1 a) {
        return new Do1<>(()->monad.unit(a));
    }

    public <T1> Do1<T1> __(Supplier<Higher<W, T1>> a) {
        return new Do1<>(a);
    }

    public <T1> Do1<T1> _flatten(Higher<W, Higher<W, T1>> nested){
        return new Do1<>(()->monad.flatten(nested));
    }
    public <T1,R> Kleisli<W,T1,R> kliesli( Function<? super T1, ? extends R> fn){
        return Kleisli.arrow(monad,fn);
    }
    public <T1,R> Kleisli<W,T1,R> kliesliK( Function<? super T1, ? extends Higher<W,R>> fn){
        return Kleisli.of(monad,fn);
    }

    public DoUnfolds expand(Unfoldable<W> unfolds){
        return new DoUnfolds(unfolds);
    }
    public DoUnfolds expand(Supplier<Unfoldable<W>> unfolds){
        return new DoUnfolds(unfolds.get());
    }
    @AllArgsConstructor
    public  class DoUnfolds{
        private final Unfoldable<W> unfolds;

        public <R, T> Do1<R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn){
            return __(unfolds.unfold(b, fn));
        }

        public <T> Do1<T> replicate(long n, T value) {
            return __(unfolds.replicate(n,value));
        }

        public <R> Do1<R> none() {
            return __(unfolds.none());
        }
        public <T> Do1<T> one(T a) {
            return __(unfolds.one(a));
        }
    }
    public <W2,T1> DoNested<W2,T1> __(Functor<W2> f, Higher<W, Higher<W2, T1>> nested){
        return new DoNested<>(nested,f);
    }
    public <W2,T1> DoNested<W2,T1> __(Supplier<Functor<W2>> f, Higher<W, Higher<W2, T1>> nested){
        return new DoNested<>(nested,f.get());
    }
    @AllArgsConstructor
    public class DoNested<W2,T1>{
        private final Higher<W, Higher<W2, T1>> nested;
        private final Compose<W,W2> f;

        public DoNested(Higher<W, Higher<W2, T1>> nested, Functor<W2> f2){
            this.nested =nested;
            this.f= Compose.compose(monad,f2);
        }


        public  Do<W2>.Do1<T1> foldK(Foldable<W> folds,Monad<W2> m2,MonoidK<W2> monoid) {
             return Do.forEach(m2).__(()->folds.foldK(monoid, nested));
        }
        public  Do<W2>.Do1<T1> foldK(Supplier<Foldable<W>> folds,Supplier<Monad<W2>> m2,MonoidK<W2> monoid) {
            return foldK(folds.get(),m2.get(),monoid);
        }
        public  Do<W>.Do1<T1> foldLeft(Foldable<W2> folds,Monoid<T1> monoid) {
            return __(()->monad.map_(nested,i->folds.foldLeft(monoid, i)));
        }
        public  Do<W>.Do1<T1> foldLeft(Supplier<Foldable<W2>> folds,Monoid<T1> monoid) {
            return foldLeft(folds.get(),monoid);
        }

        public <R> Do<W>.Do1<R> map(Function<? super Higher<W2,T1>, ? extends R> fn) {
            return __(()->monad.map_(nested,fn));
        }

        public Do<W2>.DoNested<W,T1> sequence(Traverse<W> traverse,Monad<W2> monad){
            return Do.forEach(monad).__(f.outer(),traverse.sequenceA(monad,nested));
        }

        public <R1,R2> R2 fold(Function<? super Higher<W, R1>, ? extends R2 > fn1, Function<? super Higher<W2,T1>, ? extends R1> fn2) {
            return fn1.apply(monad.map_(nested, fn2));
        }
    }





    @AllArgsConstructor
    public class Do1<T1> {
        private final Supplier<Higher<W, T1>> a;


        public <T2> Do2<T2> __(Higher<W, T2> b) {
            return new Do2<>(Function1.constant(b));
        }

        public <T2> Do2<T2> __(Function<T1,Higher<W, T2>> b) {
            return new Do2<>(b);
        }


        public <T2> Do2<T2> _of(T2 b) {

            return new Do2<T2>(Function1.constant(monad.unit(b)));
        }
        public <T2> Do2<T2> _flatten(Higher<W, Higher<W, T2>> nested){
            return new Do2<>(in->monad.flatten(nested));
        }


        public <R> Do1<R> map(Function<? super T1, ? extends R> mapper){
            return new Do1<R>(()->monad.map_(a.get(),mapper));
        }
        public <R> Do1<R> ap(Higher<W,Function<T1,R>> applicative){
            return new Do1<R>(()->monad.ap(applicative,a.get()));
        }
        public Do1<T1> peek(Consumer<? super T1> mapper){
            return new Do1<>(()->monad.peek(mapper,a.get()));
        }
        public <T2,R> Do1<R> zip(Higher<W, T2> fb, BiFunction<? super T1,? super T2,? extends R> f){
            return new Do1<>(()->monad.zip(a.get(),fb,f));
        }

        public Do1<T1> plus(MonadPlus<W> mp,Higher<W,T1> b){
            return new Do1<>(()->mp.plus(a.get(),b));
        }
        public Do1<T1> plus(Supplier<MonadPlus<W>> mp,Higher<W,T1> b){
            return plus(mp.get(),b);
        }
        public Do1<Tuple2<T1,Long>> zipWithIndex(Traverse<W> traverse){
            return Do.forEach(monad).__(()->traverse.zipWithIndex(a.get()));
        }
        public Do1<Tuple2<T1,Long>> zipWithIndex(Supplier<Traverse<W>>traverse){
            return zipWithIndex(traverse.get());
        }


        public Do1<T1> guard(MonadZero<W> monadZero,Predicate<? super T1> fn) {
            return new Do1<>(()->monadZero.filter(fn, a.get()));
        }

        public <R> Do1<R> yield(Function<? super T1,  ? extends R> fn) {
            return Do.forEach(monad).__(()->monad.map_(a.get(), fn));

        }


        public Higher<W,T1> unwrap(){
            return a.get();
        }

        public <R> R fold(Function<? super Higher<W,T1>,? extends R> fn){
            return fn.apply(a.get());
        }
        public <R> Eval<R> eval(Function<? super Higher<W,T1>,? extends R> fn){
            return Eval.later(()->fn.apply(a.get()));
        }



        public <R> Do2<R> __fold(Foldable<W> folds,Function<? super Fold1.DoFoldable,  ? extends R> fn){
            return __(in->monad.unit(fn.apply(Do.folds(folds).__(a))));
        }
        public <R> Do2<R> __fold(Supplier<Foldable<W>> folds,Function<? super Fold1.DoFoldable,  ? extends R> fn){
            return __fold(folds.get(),fn);
        }


        public Do1<T1> reverse(Traverse<W> traverse){
            return Do.forEach(monad).__(()->traverse.reverse(a.get()));
        }
        public Do1<T1> reverse(Supplier<Traverse<W>> traverse){
            return reverse(traverse.get());
        }



        public Do1<String> show(Show<W> show){
            return new Do1<>(()->monad.unit(show.show(a.get())));
        }
        public Do2<String> _show(Show<W> show){
            return __(i->monad.unit(show.show(a.get())));
        }


        @AllArgsConstructor
        public class Do2<T2> {
            private final Function<T1,Higher<W, T2>> b;
            public <R> Do2<R> map(Function<? super T2, ? extends R> mapper){
                return new Do2<>(in->monad.map_(b.apply(in),mapper));
            }

            public  <R> Do2<R> ap(Higher<W,Function<T2,R>> applicative){
                return new Do2<R>(in->monad.ap(applicative,b.apply(in)));
            }
            public Do2<T2> peek(Consumer<? super T2> mapper){
                return map(t->{
                    mapper.accept(t);
                    return t;
                });
            }
            public <T3,R> Do2<R> zip(Higher<W, T3> fb, BiFunction<? super T2,? super T3,? extends R> f){
                return new Do2<R>(in->monad.zip(b.apply(in),fb,f));
            }

            public Do2<String> show(Show<W> show){

                return new Do2<String>(in->monad.unit(show.show(monad.flatMap_(a.get(), t -> b.apply(t)))));
            }
            public Do3<String> _show(Show<W> show){

                return new Do3<>((x1,x2)->monad.unit(show.show(monad.flatMap_(a.get(), in -> b.apply(in)))));
            }

            public <T3> Do3<T3> __(Higher<W, T3> c) {
                return new Do3<>(Function2.constant(c));
            }
            public <T3> Do3<T3> __(Supplier<Higher<W, T3>> c) {
                return new Do3<>(Function2._0(c));
            }
            public <T3> Do3<T3> __(BiFunction<T1,T2,Higher<W, T3>> c) {
                return new Do3<>(c);
            }

            public <T3> Do3<T3> _flatten(Higher<W, Higher<W, T3>> nested){
                return new Do3<T3>(Function2.constant(monad.flatten(nested)));
            }

            public <T3> Do3<T3> _of(T3 c) {
                return new Do3<>(Function2.constant(monad.unit(c)));
            }

            public   Do2<T2> guard(MonadZero<W> monadZero, BiPredicate<? super T1,? super T2> fn) {
                return  new Do2<>(t1->monadZero.filter(p->fn.test(t1,p), b.apply(t1)));
            }


            public <R> Do1<R> yield(BiFunction<? super T1, ? super T2, ? extends R> fn) {
                return Do.forEach(monad).__(()->monad.flatMap_(a.get(), in -> {


                    return monad.map_(b.apply(in), in2 -> fn.apply(in, in2));
                }));
            }




            @AllArgsConstructor
            public class Do3<T3> {
                private final BiFunction<T1,T2,Higher<W, T3>> c;
                public <R> Do3<R> map(Function<? super T3, ? extends R> mapper){
                    return new Do3<>((a,b)->monad.map_(c.apply(a,b),mapper));
                }

                public  <R> Do3<R> ap(Higher<W,Function<T3,R>> applicative){
                    return new Do3<R>((a,b)->monad.ap(applicative,c.apply(a,b)));
                }
                public Do3<T3> peek(Consumer<? super T3> mapper){
                    return map(t->{
                        mapper.accept(t);
                        return t;
                    });
                }
                public <T4,R> Do3<R> zip(Higher<W, T4> fb, BiFunction<? super T3,? super T4,? extends R> f){
                    return new Do3<R>((a,b)->monad.zip(c.apply(a,b),fb,f));
                }

                public Do3<String> show(Show<W> show){

                    return new Do3<String>((a1,b1)->monad.unit(show.show(monad.flatMap_(a.get(), in -> b.apply(in)))));
                }
                public Do4<String> _show(Show<W> show){
                    return new Do4<>((x1,x2,x3)->monad.unit(show.show(monad.flatMap_(a.get(), in -> b.apply(in)))));

                }
                public <T4> Do4<T4> __(Higher<W, T4> d) {
                    return new Do4<>(Function3.constant(d));
                }
                public <T4> Do4<T4> __(Supplier<Higher<W, T4>> d) {
                    return new Do4<>(Function3.lazyConstant(d));
                }
                public <T4> Do4<T4> __(Function3<T1,T2,T3,Higher<W, T4>> c) {
                    return new Do4<>(c);
                }
                public <T4> Do4<T4> _flatten(Higher<W, Higher<W, T4>> nested){
                    return new Do4<T4>(Function3.constant(monad.flatten(nested)));
                }


                public <T4> Do4<T4> _of(T4 d) {
                    return new Do4<>(Function3.constant(monad.unit(d)));
                }

                public Do3<T3> guard(MonadZero<W> monadZero, Predicate3<? super T1,? super T2, ? super T3> fn) {
                    return new Do3<>((t1, t2) -> monadZero.filter(p -> fn.test(t1, t2, p), c.apply(t1, t2)));
                }
                public <R> Do1<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> fn) {
                   return Do.forEach(monad).__(()->monad.flatMap_(a.get(), in -> {


                        Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                            Higher<W, R> hk3 = monad.map_(c.apply(in,in2), in3 -> fn.apply(in, in2, in3));
                            return hk3;
                        });
                        return hk2;
                    }));

                }

                @AllArgsConstructor
                public class Do4<T4> {
                    private final Function3<T1,T2,T3,Higher<W, T4>> d;

                    public <R> Do4<R> map(Function<? super T4, ? extends R> mapper){
                        return new Do4<>((a,b,c)->monad.map_(d.apply(a,b,c),mapper));
                    }

                    public  <R> Do4<R> ap(Higher<W,Function<T4,R>> applicative){
                        return new Do4<R>((a,b,c)->monad.ap(applicative,d.apply(a,b,c)));
                    }
                    public Do4<T4> peek(Consumer<? super T4> mapper){
                        return map(t->{
                            mapper.accept(t);
                            return t;
                        });
                    }
                    public <T5,R> Do4<R> zip(Higher<W, T5> fb, BiFunction<? super T4,? super T5,? extends R> f){
                        return new Do4<R>((a,b,c)->monad.zip(d.apply(a,b,c),fb,f));
                    }

                    public Do4<String> show(Show<W> show){
                        return new Do4<String>((a1,b1,c1)->monad.unit(show.show(monad.flatMap_(a.get(), in -> b.apply(in)))));
                    }
                    public Do5<String> _show(Show<W> show){
                        return new Do5<>((x1,x2,x3,x4)->monad.unit(show.show(monad.flatMap_(a.get(), in -> b.apply(in)))));
                    }
                    public <T5> Do5<T5> __(Higher<W, T5> e) {
                        return new Do5<>(Function4.constant(e));
                    }

                    public<T5> Do5<T5> __(Supplier<Higher<W, T5>> e) {
                        return new Do5<>(Function4.lazyConstant(e));
                    }
                    public <T5> Do5<T5> __(Function4<T1,T2,T3,T4,Higher<W, T5>> e) {
                        return new Do5<>(e);
                    }

                    public <T5> Do5<T5> _of(T5 e) {
                        return new Do5<>(Function4.constant(monad.unit(e)));
                    }
                    public <T5> Do5<T5> _flatten(Higher<W, Higher<W, T5>> nested){
                        return new Do5<T5>(Function4.constant(monad.flatten(nested)));
                    }


                    public Do4<T4> guard(MonadZero<W> monadZero, Predicate4<? super T1,? super T2, ? super T3, ? super T4> fn) {
                        return new Do4<>((t1,t2,t3)->monadZero.filter(p->fn.test(t1,t2,t3,p), d.apply(t1,t2,t3)));
                    }

                    public <R> Do1<R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
                        return Do.forEach(monad).__(()->monad.flatMap_(a.get(), in -> {


                            Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                                Higher<W, R> hk3 = monad.flatMap_(c.apply(in,in2), in3 -> {
                                    Higher<W, R> hk4 = monad.map_(d.apply(in,in2,in3), in4 -> fn.apply(in, in2, in3, in4));
                                    return hk4;
                                });
                                return hk3;
                            });
                            return hk2;
                        }));
                    }
                    @AllArgsConstructor
                    public class Do5<T5> {
                        private final Function4<T1,T2,T3,T4,Higher<W, T5>> e;

                        public <R> Do5<R> map(Function<? super T5, ? extends R> mapper){
                            return new Do5<>((a,b,c,d)->monad.map_(e.apply(a,b,c,d),mapper));
                        }

                        public  <R> Do5<R> ap(Higher<W,Function<T5,R>> applicative){
                            return new Do5<R>((a,b,c,d)->monad.ap(applicative,e.apply(a,b,c,d)));
                        }
                        public Do5<T5> peek(Consumer<? super T5> mapper){
                            return map(t->{
                                mapper.accept(t);
                                return t;
                            });
                        }
                        public <T6,R> Do5<R> zip(Higher<W, T6> fb, BiFunction<? super T5,? super T6,? extends R> f){
                            return new Do5<R>((a,b,c,d)->monad.zip(e.apply(a,b,c,d),fb,f));
                        }

                        public Do5<String> show(Show<W> show){
                            return new Do5<String>((a1,b1,c1,d1)->monad.unit(show.show(monad.flatMap_(a.get(), in -> b.apply(in)))));
                        }

                        public Do5<T5> guard(MonadZero<W> monadZero,
                                                                                                                                                      Predicate5<? super T1,? super T2, ? super T3, ? super T4, ? super T5> fn) {
                            return new Do5<>((t1,t2,t3,t4)->monadZero.filter(p->fn.test(t1,t2,t3,t4,p), e.apply(t1,t2,t3,t4)));
                        }
                        public <R> Do1<R> yield(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5,? extends R> fn) {
                            return Do.forEach(monad).__(()-> monad.flatMap_(a.get(), in -> {


                                Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                                    Higher<W, R> hk3 = monad.flatMap_(c.apply(in,in2), in3 -> {
                                        Higher<W, R> hk4 = monad.flatMap_(d.apply(in,in2,in3), in4 -> {
                                            Higher<W,R> hk5 = monad.map_(e.apply(in,in2,in3,in4),in5->fn.apply(in, in2, in3, in4,in5));
                                            return hk5;
                                        });
                                        return hk4;
                                    });
                                    return hk3;
                                });
                                return hk2;
                            }));

                        }
                    }
                }
            }
        }


    }

    public static <W> Do<W> forEach(Monad<W> a){
        return new Do(a);
    }

    public static <W> Do<W> forEach(Supplier<Monad<W>> a){
        return forEach(a.get());
    }

    public static <W> Fold1<W> folds(Foldable<W> foldable){
        return new Fold1<>(foldable);
    }

    public static <W> Fold1<W> folds(Supplier<Foldable<W>> foldable){
        return new Fold1<>(foldable.get());
    }
    public static <W> Traverse1<W> traverse(Traverse<W> traverse){
        return new Traverse1<>(traverse);
    }

    public static <W> Traverse1<W> traverse(Supplier<Traverse<W>> traverse){
        return new Traverse1<>(traverse.get());
    }
    public static <W> Sequence1<W> sequence(Traverse<W> traverse){
        return new Sequence1<>(traverse);
    }

    public static <W> Sequence1<W> sequence(Supplier<Traverse<W>> traverse){
        return new Sequence1<>(traverse.get());
    }
    @AllArgsConstructor
    public static class Sequence1<W>{
        private final Traverse<W> traverse;

        public <W2,T1> DoSequence<W2,T1> __(Higher<W, Higher<W2,T1>> a) {
            return new DoSequence<>(a);
        }
        public <W2,T1> DoSequence<W2,T1> __(Supplier<Higher<W, Higher<W2,T1>>> a) {
            return new DoSequence<>(a.get());
        }
        @AllArgsConstructor
        public class DoSequence<W2,T1>{
            private final Higher<W, Higher<W2,T1>> a;

            private  Higher<W2, Higher<W, T1>> sequenceA(Applicative<W2> applicative){
                return traverse.sequenceA(applicative,a);
            }
            public Do<W2>.DoNested<W,T1> traverse(Monad<W2> m1, Applicative<W2> applicative){

                return  Do.forEach(m1).__(traverse, sequenceA(applicative));
            }
        }
    }

    @AllArgsConstructor
    public static class Traverse1<W>{
        private final Traverse<W> traverse;
        public <T1> DoTraverse<T1> __(Higher<W, T1> a) {
            return new DoTraverse<>(a);
        }
        public <T1> DoTraverse<T1> __(Supplier<Higher<W, T1>> a) {
            return new DoTraverse<>(a.get());
        }


        @AllArgsConstructor
        public class DoTraverse<T1>{
            private final Higher<W, T1> a;

            private <W2,R> Higher<W2, Higher<W, R>> traverse(Applicative<W2> applicative, Function<? super T1, ? extends Higher<W2, R>> fn){
                return traverse.traverseA(applicative,fn,a);
            }
            public <W2,R> Do<W2>.DoNested<W,R> traverse(Monad<W2> m1, Function<? super T1, ? extends Higher<W2, R>> fn){
                return  Do.forEach(m1).__(traverse, traverse.traverseA(m1, fn, a));
            }
            public <S,R,R2> State<S,R2> traverseS(Function<? super T1, ? extends State<S,R>> fn,Function<Higher<W,R>,R2> foldFn){
                return  State.narrowK(traverse(StateInstances.applicative(), fn)).map(foldFn);

            }
            public <S,R> Tuple2<S, Do<W>.Do1<R>> runTraverseS(Monad<W> monad,Function<? super T1, ? extends State<S,R>> fn, S val) {
                return traverse.runTraverseS(fn,a,val).map2(i -> Do.forEach(monad).__(i));
            }
            public Do<W>.Do1<T1> reverse(Monad<W> monad){
                return Do.forEach(monad).__(traverse.reverse(a));
            }
            public  <S,R>  Tuple2<S, Do<W>.Do1<R>> mapAccumL (Monad<W> monad,BiFunction<? super S, ? super T1, ? extends Tuple2<S,R>> f,S z) {
                return traverse.mapAccumL(f, a, z)
                    .map2(i -> Do.forEach(monad).__(i));
            }
            public <R> R foldMap(Monoid<R> mb, final Function<? super T1,? extends R> fn) {
                return  traverse.foldMap(mb,fn,a);
            }
            public <R> Do<W>.Do1<R> mapWithIndex(Monad<W> monad,BiFunction<? super T1,Long,? extends R> f) {
                return Do.forEach(monad)
                    .__(traverse.mapWithIndex(f,a));
            }

            public <W2,T2,R> Do<W>.Do1<R> zipWith(Monad<W> monad,Foldable<W2> foldable, BiFunction<? super T1,? super Maybe<T2>,? extends R> f, Higher<W2, T2> ds2) {
                return Do.forEach(monad)
                    .__(traverse.zipWith(foldable,f,a,ds2));

            }
            public <R> Do<W>.Do1<Tuple2<T1,Long>> zipWithIndex(Monad<W> monad) {
                return Do.forEach(monad)
                    .__(traverse.zipWithIndex(a));
            }
        }
    }

    @AllArgsConstructor
    public static class Fold1<W> {
        private final Foldable<W> folds;

        public <T1> DoFoldable<T1> __(Higher<W, T1> a) {
        return new DoFoldable<>(a);
    }

        public <T1> DoFoldable<T1> __(Supplier<Higher<W, T1>> a) {
            return new DoFoldable<>(a.get());
        }

        @AllArgsConstructor
        public class DoFoldable<T1>{
            private final Higher<W, T1> a;

            public <R> R foldMap(final Monoid<R> mb, final Function<? super T1,? extends R> fn){
                return folds.foldMap(mb,fn,a);
            }

            public <R> R foldr(final Function< T1, Function< R, R>> fn, R r){
                return folds.foldr(fn,r,a);
            }


            public T1 foldRight(Monoid<T1> monoid){
                return folds.foldRight(monoid,a);
            }


            public T1 foldRight(T1 identity, BinaryOperator<T1> semigroup){
                return folds.foldRight(identity,semigroup,a);
            }

            public T1 foldLeft(Monoid<T1> monoid){
                return folds.foldLeft(monoid,a);
            }

            public T1 foldLeft(T1 identity, BinaryOperator<T1> semigroup){
                return folds.foldLeft(identity,semigroup,a);
            }
            public  long size() {
                return folds.size(a);
            }
            public  Seq<T1> seq(){
                return folds.seq(a);
            }
            public  LazySeq<T1> lazySeq(){
                return folds.lazySeq(a);
            }
            public  ReactiveSeq<T1> stream(){
                return folds.stream(a);
            }

            public   T1 intercalate(Monoid<T1> monoid, T1 value ){
                return seq().intersperse(value).foldLeft(monoid);
            }

            public   Option<T1> getAt(int index){
                return seq().get(index);
            }

            public   boolean anyMatch(Predicate<? super T1> pred){
                return folds.anyMatch(pred,a);
            }
            public   boolean allMatch(Predicate<? super T1> pred){
                return folds.allMatch(pred,a);
            }
        }

    }

}
