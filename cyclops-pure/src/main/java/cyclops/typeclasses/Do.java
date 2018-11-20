package cyclops.typeclasses;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Function2;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Function5;
import cyclops.function.Predicate3;
import cyclops.function.Predicate4;
import cyclops.instances.control.OptionInstances;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.MonadZero;
import lombok.AllArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static cyclops.control.Option.some;


public class Do<W> {

    private final Monad<W> monad;

    private Do(Monad<W> monad) {

        this.monad = monad;
    }

    public <T1> Do1<T1> __(Higher<W, T1> a) {
        return new Do1<>(a);
    }

    public <T1> Do1<T1> __(T1 a) {
        return new Do1<>(monad.unit(a));
    }

    public <T1> Do1<T1> __(Supplier<Higher<W, T1>> a) {
        return new Do1<>(a.get());
    }



    @FunctionalInterface
    public static interface Yield<T,R>{
        R yield(T t);
    }


    @AllArgsConstructor
    public class Do1<T1> {
        private final Higher<W, T1> a;

        public <T2> Do2<T2> __(Higher<W, T2> b) {
            return new Do2<>(Function1.constant(b));
        }

        public <T2> Do2<T2> __(Function<T1,Higher<W, T2>> b) {
            return new Do2<>(b);
        }

        public <T2> Do2<T2> __(T2 b) {

            return new Do2<T2>(Function1.constant(monad.unit(b)));
        }
        public <R> Do1<R> map(Function<? super T1, ? extends R> mapper){
            return new Do1<R>(monad.map_(a,mapper));
        }
        public <R> Yield<Function<? super T1,? extends R>, ? extends Higher<W,R>> guard(MonadZero<W> monadZero,Predicate<? super T1> fn) {
            return in->  new Do1<>(monadZero.filter(fn, a)).yield(in);
        }
        public <R> Higher<W, R> yield(Function<? super T1,  ? extends R> fn) {
            Higher<W, R> hk = monad.map_(a, fn);
            return hk;
        }
        public String show(Show<W> show){
            return show.show(a);
        }
        @AllArgsConstructor
        public class Do2<T2> {
            private final Function<T1,Higher<W, T2>> b;

            public <T3> Do3<T3> __(Higher<W, T3> c) {
                return new Do3<>(Function2.constant(c));
            }
            public <T3> Do3<T3> __(Supplier<Higher<W, T3>> c) {
                return new Do3<>(Function2.lazyConstant(c));
            }
            public <T3> Do3<T3> __(BiFunction<T1,T2,Higher<W, T3>> c) {
                return new Do3<>(c);
            }
            public <T3> Do3<T3> _1(Function<T1,Higher<W, T3>> c) {
                return new Do3<>(Function2.left(c));
            }
            public <T3> Do3<T3> _2(Function<T2,Higher<W, T3>> c) {
                return new Do3<>(Function2.right(c));
            }

            public <T3> Do3<T3> __(T3 c) {
                return new Do3<>(Function2.constant(monad.unit(c)));
            }

            public <R> Yield<BiFunction<? super T1, ? super T2,? extends R>, ? extends Higher<W,R>> guard(MonadZero<W> monadZero, BiPredicate<? super T1,? super T2> fn) {
                return in->  new Do2<>(t1->monadZero.filter(p->fn.test(t1,p), b.apply(t1))).yield(in);
            }
            public <R> Higher<W, R> yield(BiFunction<? super T1, ? super T2, ? extends R> fn) {
                Higher<W, R> hk = monad.flatMap_(a, in -> {


                    return monad.map_(b.apply(in), in2 -> fn.apply(in, in2));
                });
                return hk;
            }

            @AllArgsConstructor
            public class Do3<T3> {
                private final BiFunction<T1,T2,Higher<W, T3>> c;

                public <T4> Do4<T4> __(Higher<W, T4> d) {
                    return new Do4<>(Function3.constant(d));
                }
                public <T4> Do4<T4> __(Supplier<Higher<W, T4>> d) {
                    return new Do4<>(Function3.lazyConstant(d));
                }
                public <T4> Do4<T4> __(Function3<T1,T2,T3,Higher<W, T4>> c) {
                    return new Do4<>(c);
                }
                public <T4> Do4<T4> _1(Function<T1,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(a));
                }
                public <T4> Do4<T4> _2(Function<T2,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(b));
                }
                public <T4> Do4<T4> _3(Function<T3,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(c));
                }
                public <T4> Do4<T4> _12(BiFunction<T1,T2,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(a,b));
                }
                public <T4> Do4<T4> _23(BiFunction<T2,T3,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(b,c));
                }

                public <T4> Do4<T4> __(T4 d) {
                    return new Do4<>(Function3.constant(monad.unit(d)));
                }

                public <R> Yield<Function3<? super T1, ? super T2,? super T3,? extends R>, ? extends Higher<W,R>> guard(MonadZero<W> monadZero, Predicate3<? super T1,? super T2, ? super T3> fn) {
                    return in->  new Do3<>((t1,t2)->monadZero.filter(p->fn.test(t1,t2,p), c.apply(t1,t2))).yield(in);
                }
                public <R> Higher<W, R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> fn) {
                    Higher<W, R> hk = monad.flatMap_(a, in -> {


                        Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                            Higher<W, R> hk3 = monad.map_(c.apply(in,in2), in3 -> fn.apply(in, in2, in3));
                            return hk3;
                        });
                        return hk2;
                    });
                    return hk;
                }

                @AllArgsConstructor
                public class Do4<T4> {
                    private final Function3<T1,T2,T3,Higher<W, T4>> d;

                    public <T5> Do5<T5> __(Higher<W, T5> e) {
                        return new Do5<>(Function4.constant(e));
                    }

                    public<T5> Do5<T5> __(Supplier<Higher<W, T5>> e) {
                        return new Do5<>(Function4.lazyConstant(e));
                    }
                    public <T5> Do5<T5> __(Function4<T1,T2,T3,T4,Higher<W, T5>> e) {
                        return new Do5<>(e);
                    }
                    public <T5> Do5<T5> __(T5 e) {
                        return new Do5<>(Function4.constant(monad.unit(e)));
                    }

                    public <R> Yield<Function4<? super T1, ? super T2,? super T3,? super T4,? extends R>, ? extends Higher<W,R>> guard(MonadZero<W> monadZero, Predicate4<? super T1,? super T2, ? super T3, ? super T4> fn) {
                        return in->  new Do4<>((t1,t2,t3)->monadZero.filter(p->fn.test(t1,t2,t3,p), d.apply(t1,t2,t3))).yield(in);
                    }
                    public <R> Higher<W, R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
                        Higher<W, R> hk = monad.flatMap_(a, in -> {


                            Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                                Higher<W, R> hk3 = monad.flatMap_(c.apply(in,in2), in3 -> {
                                    Higher<W, R> hk4 = monad.map_(d.apply(in,in2,in3), in4 -> fn.apply(in, in2, in3, in4));
                                    return hk4;
                                });
                                return hk3;
                            });
                            return hk2;
                        });
                        return hk;
                    }
                    @AllArgsConstructor
                    public class Do5<T5> {
                        private final Function4<T1,T2,T3,T4,Higher<W, T5>> e;

                        public <R> Higher<W, R> yield(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5,? extends R> fn) {
                            Higher<W, R> hk = monad.flatMap_(a, in -> {


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
                            });
                            return hk;
                        }
                    }
                }
            }
        }


    }

    public static <W,T1> Do<W> forEach(Monad<W> a){
        return new Do(a);
    }

    public static <W,T1> Do<W> forEach(Supplier<Monad<W>> a){
        return new Do(a.get());
    }

    public static Maybe<String> opt(Number i){
        return null;
    }
    public static Maybe<String> op1(){
        return null;
    }

    public static void test(){
          Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(Do::opt);

        Do.forEach(OptionInstances::monad)
            .__(Do::op1);

        Do.forEach(OptionInstances::monad)
            .__("hello");

    }
}
