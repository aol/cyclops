package cyclops.control;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.function.*;
import cyclops.monads.Witness;
import cyclops.monads.Witness.reader;
import cyclops.typeclasses.Active;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;

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

    default Active<Higher<reader,T>,R> allTypeclasses(){
        return Active.of(this, Instances.definitions());
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
    public static class Instances{
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
            };
        }
        public static <IN> Functor<Higher<reader,IN>> functor(){
            return new Functor<Higher<reader,IN>>(){
                @Override
                public <T, R> Higher<Higher<reader, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<reader,IN>, T> ds) {
                    Reader<IN, T> fn1 = narrowK(ds);
                    Reader<IN, R> res = fn1.map(fn);
                    return res;
                }
            };
        }

        public static <IN> Pure<Higher<reader,IN>> unit(){
            return new Pure<Higher<reader,IN>>() {
                @Override
                public <R> Higher<Higher<reader, IN>, R> unit(R value) {
                    Reader<IN,R> fn = __ -> value;
                    return fn;
                }
            };
        }

        public static <IN> Applicative<Higher<reader,IN>> applicative(){
            return new Applicative<Higher<reader,IN>>(){

                @Override
                public <T, R> Higher<Higher<reader, IN>, R> ap(Higher<Higher<reader, IN>, ? extends Function<T, R>> fn, Higher<Higher<reader, IN>, T> apply) {
                    Reader<IN,? extends Function<T, R>> f = Reader.narrowK(fn);
                    Reader<IN, T> ap = Reader.narrowK(apply);
                    Reader<IN,R> res = in->f.apply(in).apply(ap.apply(in));
                    return res;
                }

                @Override
                public <T, R> Higher<Higher<reader, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<reader, IN>, T> ds) {
                    return Instances.<IN>functor().map(fn,ds);
                }

                @Override
                public <R> Higher<Higher<reader, IN>, R> unit(R value) {
                    return Instances.<IN>unit().unit(value);
                }
            };
        }
        public static <IN> Monad<Higher<reader,IN>> monad() {
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


    }
}
