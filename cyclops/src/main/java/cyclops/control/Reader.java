package cyclops.control;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.function.*;
import com.oath.cyclops.hkt.DataWitness.reader;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.functor.ProFunctor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

/**
 * An interface that represents the Reader monad
 *
 * A technique for functional dependency injection. Functions rather than values
 * are manipulated and dependencies injected into Functions to execute them.
 *
 * {@see <a href="https://medium.com/@johnmcclean/dependency-injection-using-the-reader-monad-in-java8-9056d9501c75">Dependency injection using the Reader Monad in Java 8</a>}
 *
 *
 *
 * @author johnmcclean
 *
 * @param <T> Current input type of Function
 * @param <R> Current return type of Function
 */
@FunctionalInterface
public interface Reader<T, R> extends Function1<T, R>, Transformable<R>,Higher<Higher<reader,T>,R> {

    public static <T,R> Reader<T,R> of(Reader<T,R> i){
        return i;
    }
    public static <IN,T,R> Reader<IN,R> tailRec(T initial,Function<? super T,? extends Reader<IN, ? extends Either<T, R>>> fn ){
        return narrowK(Instances.<IN, T, R>monadRec().tailRec(initial, fn));
    }
    public static  <R,T> Kleisli<Higher<reader,T>,Reader<T,R>,R> kindKleisli(){
        return Kleisli.of(Instances.monad(), Reader::widen);
    }
    public static <T,R> Higher<Higher<reader,T>, R> widen(Reader<T,R> narrow) {
        return narrow;
    }
    public static  <T,R> Cokleisli<Higher<reader,T>,R,Reader<T,R>> kindCokleisli(){
        return Cokleisli.of(Reader::narrowK);
    }
    public static <W1,T,R> Nested<Higher<reader,T>,W1,R> nested(Reader<T,Higher<W1,R>> nested, T defaultValue,InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(defaultValue),def2);
    }
    default <W1> Product<Higher<reader,T>,W1,R> product(T defaultValue,Active<W1,R> active){
        return Product.of(allTypeclasses(defaultValue),active);
    }
    default <W1> Coproduct<W1,Higher<reader,T>,R> coproduct(T defaultValue, InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions(defaultValue));
    }
    default Active<Higher<reader,T>,R> allTypeclasses(T defaultValue){
        return Active.of(this, Instances.definitions(defaultValue));
    }
    default <W2,R2> Nested<Higher<reader,T>,W2,R2> mapM(T defaultValue,Function<? super R,? extends Higher<W2,R2>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(defaultValue), defs);
    }

    default  <R2> Reader<T, Tuple2<R,R2>> zip(Reader<T, R2> o){
        return zip(o, Tuple::tuple);
    }
    default  <R2,B> Reader<T, B> zip(Reader<T, R2> o,BiFunction<? super R,? super R2,? extends B> fn){
        return flatMap(a -> o.map(b -> fn.apply(a,b)));
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.functor.Transformable#transform(java.util.function.Function)
     */
    @Override
    default <R1> Reader<T, R1> map(final Function<? super R, ? extends R1> f2) {
        return FluentFunctions.of(this.andThen(f2));
    }

    /**
     * FlatMap this Reader by applying the prodived function and unnesting to a single Reader
     *
     * @param f Transformation function to be flattened
     * @return Transformed Reader
     */
    default <R1> Reader<T, R1> flatMap(final Function<? super R, ? extends Reader<T, R1>> f) {
        return FluentFunctions.of(a -> f.apply(apply(a))
                                        .apply(a));
    }


    default <R1, R2, R3, R4> Reader<T,R4> forEach4(Function<? super R, Function<? super T,? extends R1>> value2,
                                                   BiFunction<? super R, ? super R1, Function<? super T,? extends R2>> value3,
                                                   Function3<? super R, ? super R1, ? super R2,Function<? super T, ? extends R3>> value4,
                                                   Function4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {


        Reader<? super T, ? extends R4> res =  this.flatMap(in -> {

            Reader<T,R1> a = narrow(FluentFunctions.of(value2.apply(in)));
            return a.flatMap(ina -> {
                Reader<T,R2> b = narrow(FluentFunctions.of(value3.apply(in,ina)));
                return b.flatMap(inb -> {

                    Reader<T,R3> c = narrow(FluentFunctions.of(value4.apply(in,ina,inb)));

                    return c.map(in2 -> {

                         return yieldingFunction.apply(in, ina, inb, in2);


                    });

                });


            });


        });
        return (Reader<T,R4>)res;

    }

    static <T,R>  Reader<T,R> narrow(Reader<? super T,? extends R> broad){
        return  (Reader<T,R>)broad;
    }


    default <R1, R2, R4> Reader<T,R4> forEach3(Function<? super R, Function<? super T,? extends R1>> value2,
                                                         BiFunction<? super R, ? super R1, Function<? super T,? extends R2>> value3,
                                                         Function3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = narrow(FluentFunctions.of(value2.apply(in)));
            return a.flatMap(ina -> {
                Reader<T,R2> b = narrow(FluentFunctions.of(value3.apply(in,ina)));
                return b.map(in2 -> {
                        return yieldingFunction.apply(in, ina, in2);

                    });



            });

        });

    }



    default <R1, R4> Reader<T,R4> forEach2(Function<? super R, Function<? super T,? extends R1>> value2,
                                                BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = narrow(FluentFunctions.of(value2.apply(in)));
            return a.map(in2 -> {
                    return yieldingFunction.apply(in, in2);

                });




        });


    }
    public static <T,R> Reader<T,  R> narrowK(Higher<Higher<reader,T>,R> hkt){
        return (Reader<T,R>)hkt;
    }

    public static class Instances {
        public static <IN> InstanceDefinitions<Higher<reader, IN>> definitions(IN in) {
            return new InstanceDefinitions<Higher<reader, IN>>() {

                @Override
                public <T, R> Functor<Higher<reader, IN>> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<Higher<reader, IN>> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<Higher<reader, IN>> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<Higher<reader, IN>> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Option<MonadZero<Higher<reader, IN>>> monadZero() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Option<MonadPlus<Higher<reader, IN>>> monadPlus() {
                    return Maybe.nothing();
                }

                @Override
                public <T> MonadRec<Higher<reader, IN>> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Option<MonadPlus<Higher<reader, IN>>> monadPlus(MonoidK<Higher<reader, IN>> m) {
                    return Maybe.nothing();
                }


                @Override
                public <C2, T> Traverse<Higher<reader, IN>> traverse() {
                    return Instances.traversable(in);
                }

                @Override
                public <T> Foldable<Higher<reader, IN>> foldable() {
                    return Instances.foldable(in);
                }

                @Override
                public <T> Option<Comonad<Higher<reader, IN>>> comonad() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Option<Unfoldable<Higher<reader, IN>>> unfoldable() {
                    return Maybe.nothing();
                }
            };
        }

        public static <IN> Functor<Higher<reader, IN>> functor() {
            return new Functor<Higher<reader, IN>>() {
                @Override
                public <T, R> Higher<Higher<reader, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<reader, IN>, T> ds) {
                    Reader<IN, T> fn1 = narrowK(ds);
                    Reader<IN, R> res = fn1.map(fn);
                    return res;
                }
            };
        }

        public static <IN> Pure<Higher<reader, IN>> unit() {
            return new Pure<Higher<reader, IN>>() {
                @Override
                public <R> Higher<Higher<reader, IN>, R> unit(R value) {
                    Reader<IN, R> fn = __ -> value;
                    return fn;
                }
            };
        }

        public static <IN> Applicative<Higher<reader, IN>> applicative() {
            return new Applicative<Higher<reader, IN>>() {

                @Override
                public <T, R> Higher<Higher<reader, IN>, R> ap(Higher<Higher<reader, IN>, ? extends Function<T, R>> fn, Higher<Higher<reader, IN>, T> apply) {
                    Reader<IN, ? extends Function<T, R>> f = Reader.narrowK(fn);
                    Reader<IN, T> ap = Reader.narrowK(apply);
                    Reader<IN, R> res = in -> f.apply(in).apply(ap.apply(in));
                    return res;
                }

                @Override
                public <T, R> Higher<Higher<reader, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<reader, IN>, T> ds) {
                    return Instances.<IN>functor().map(fn, ds);
                }

                @Override
                public <R> Higher<Higher<reader, IN>, R> unit(R value) {
                    return Instances.<IN>unit().unit(value);
                }
            };
        }

        public static <IN> Foldable<Higher<reader, IN>> foldable(IN t) {
            return new Foldable<Higher<reader, IN>>() {
                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<reader, IN>, T> ds) {
                    return foldLeft(monoid,ds);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<reader, IN>, T> ds) {
                    Reader<IN, T> r = narrowK(ds);
                    return r.foldLeft(t,monoid);

                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<reader, IN>, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }
            };
        }

        public static <IN,C2, T, R> Higher<C2, Higher<Higher<reader, IN>, R>> traverseA(IN t,Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<reader, IN>, T> ds) {
            Reader<IN, T> r = narrowK(ds);

            return applicative.map(i -> {
                Reader<IN,R> res = a->i;
                return res;
            }, fn.apply(r.apply(t)));
        }
        public static <IN> Traverse<Higher<reader, IN>> traversable(IN t) {

            return General.traverseByTraverse(applicative(), (a,b,c)-> traverseA(t,a,b,c));

        }
        public static <IN> Monad<Higher<reader, IN>> monad() {
            return new Monad<Higher<reader, IN>>() {

                @Override
                public <T, R> Higher<Higher<reader, IN>, R> ap(Higher<Higher<reader, IN>, ? extends Function<T, R>> fn, Higher<Higher<reader, IN>, T> apply) {
                    return Instances.<IN>applicative().ap(fn, apply);
                }

                @Override
                public <T, R> Higher<Higher<reader, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<reader, IN>, T> ds) {
                    return Instances.<IN>functor().map(fn, ds);
                }

                @Override
                public <T> Higher<Higher<reader, IN>, T> unit(T value) {
                    return Instances.<IN>unit().unit(value);
                }

                @Override
                public <T, R> Higher<Higher<reader, IN>, R> flatMap(Function<? super T, ? extends Higher<Higher<reader, IN>, R>> fn, Higher<Higher<reader, IN>, T> ds) {
                    Reader<IN, T> mapper = Reader.narrowK(ds);
                    Function1<IN, R> res = mapper.flatMap(fn.andThen(Reader::narrowK));
                    return res.reader();
                }
            };

        }

        public static <IN,R> ProFunctor<reader> profunctor() {
                return new ProFunctor<reader>() {

                    @Override
                    public <A, B, C, D> Higher<Higher<reader, C>, D> dimap(Function<? super C, ? extends A> f, Function<? super B, ? extends D> g, Higher<Higher<reader, A>, B> p) {
                        Reader<A, B> r = narrowK(p);
                        Function<? super C, ? extends D> f1 = g.compose(r).compose(f);
                        Reader<C,D> r1 = in->f1.apply(in);
                        return r1;
                    }
                };
            }

        public static <IN, T, R> MonadRec<Higher<reader, IN>> monadRec() {
             return new MonadRec<Higher<reader, IN>>() {
                @Override
                public <T, R> Higher<Higher<reader, IN>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<reader, IN>, ? extends Either<T, R>>> fn) {

                    Reader<IN, Reader<IN, R>> reader = (IN in) -> {
                        Reader<IN, ? extends Either<T, R>> next[] = new Reader[1];
                        next[0] = __ -> Either.left(initial);
                        boolean cont = true;
                        do {

                            cont = next[0].apply(in).visit(s -> {
                                Reader<IN, ? extends Either<T, R>> x = narrowK(fn.apply(s));

                                next[0] = narrowK(fn.apply(s));
                                return true;
                            }, pr -> false);
                        } while (cont);
                        return next[0].map(x->x.orElse(null));
                    };
                    return reader.flatMap(Function.identity());

                }


            };


        }
    }

    default R foldLeft(T t, Monoid<R> monoid){
        Function1<T, R> x = this.andThen(v -> monoid.apply(monoid.zero(), v));
        return x.apply(t);
    }


}
