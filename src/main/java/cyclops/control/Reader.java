package cyclops.control;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.react.threads.SequentialElasticPools;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.async.Future;
import cyclops.async.SimpleReact;
import cyclops.function.*;
import cyclops.monads.Witness;
import cyclops.monads.Witness.reader;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

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
public interface Reader<T, R> extends Fn1<T, R>, Transformable<R>,Higher<Higher<reader,T>,R> {

    public static <W1,T,R> Nested<Higher<reader,T>,W1,R> nested(Reader<T,Higher<W1,R>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<Higher<reader,T>,W1,R> product(Active<W1,R> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,Higher<reader,T>,R> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<Higher<reader,T>,R> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R2> Nested<Higher<reader,T>,W2,R2> mapM(Function<? super R,? extends Higher<W2,R2>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }

    default  <R2> Reader<T, Tuple2<R,R2>> zip(Reader<T, R2> o){
        return zip(o, Tuple::tuple);
    }
    default  <R2,B> Reader<T, B> zip(Reader<T, R2> o,BiFunction<? super R,? super R2,? extends B> fn){
        return flatMap(a -> o.map(b -> fn.apply(a,b)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.Transformable#map(java.util.function.Function)
     */
    @Override
    default <R1> Reader<T, R1> map(final Function<? super R, ? extends R1> f2) {
        return FluentFunctions.of(this.andThen(f2));
    }

    /**
     * FlatMap this Reader by applying the prodived function and unnesting to a singleUnsafe Reader
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
                                                   Fn3<? super R, ? super R1, ? super R2,Function<? super T, ? extends R3>> value4,
                                                   Fn4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {


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
                                                         Fn3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

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
        public static <IN> InstanceDefinitions<Higher<reader, IN>> definitions() {
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
                public <T, R> Maybe<MonadZero<Higher<reader, IN>>> monadZero() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<reader, IN>>> monadPlus() {
                    return Maybe.none();
                }

                @Override
                public <T> MonadRec<Higher<reader, IN>> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<reader, IN>>> monadPlus(Monoid<Higher<Higher<reader, IN>, T>> m) {
                    return Maybe.none();
                }


                @Override
                public <C2, T> Maybe<Traverse<Higher<reader, IN>>> traverse() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Foldable<Higher<reader, IN>>> foldable() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Comonad<Higher<reader, IN>>> comonad() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Unfoldable<Higher<reader, IN>>> unfoldable() {
                    return Maybe.none();
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
                    Fn1<IN, R> res = mapper.flatMap(fn.andThen(Reader::narrowK));
                    return res.reader();
                }
            };

        }

        public static <IN, T, R> MonadRec<Higher<reader, IN>> monadRec() {
             return new MonadRec<Higher<reader, IN>>() {
                @Override
                public <T, R> Higher<Higher<reader, IN>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<reader, IN>, ? extends Xor<T, R>>> fn) {

                    Reader<IN, Reader<IN, R>> reader = (IN in) -> {
                        Reader<IN, ? extends Xor<T, R>> next[] = new Reader[1];
                        next[0] = __ -> Xor.secondary(initial);
                        boolean cont = true;
                        do {

                            cont = next[0].apply(in).visit(s -> {
                                Reader<IN, ? extends Xor<T, R>> x = narrowK(fn.apply(s));

                                next[0] = narrowK(fn.apply(s));
                                return true;
                            }, pr -> false);
                        } while (cont);
                        return next[0].map(Xor::get);
                    };
                    return reader.flatMap(Function.identity());

                }


            };


        }
    }
}
