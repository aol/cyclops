package cyclops.control;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher4;
import cyclops.monads.Witness;
import cyclops.monads.Witness.rws;
import cyclops.monads.Witness.supplier;
import cyclops.typeclasses.Active;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.free.Free;
import cyclops.function.*;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;

import java.util.function.BiFunction;
import java.util.function.Function;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReaderWriterState<R,W,S,T> implements Higher4<rws,R,W,S,T> {



    private final Monoid<W> monoid;
    private final BiFunction<R,S, Free<supplier,Tuple3<W, S, T>>> runState;

    public Tuple3<W,S,T> run(R r,S s) {
        return Fn0.run(runState.apply(r,s));
    }

    public  ReaderWriterState<R,W,S,T> tell(W value) {
        BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> fn =
                (r,s)->runState.apply(r,s).map(t3->Tuple.tuple(monoid.apply(t3.v1,value),t3.v2,t3.v3));

        return suspended(fn,monoid);
    }

    public ReaderWriterState<R,W,S,T> ask() {
         return suspended((r,s) -> runState.apply(r,s).map(t3 -> Tuple.<W,S,T>tuple(monoid.zero(),s,t3.v3)),monoid);
    }


    public ReaderWriterState<R,W,S,T> local(Function<? super  R,? extends R> fn) {
        BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> runFn = (R r, S s) -> runState.apply(fn.apply(r), s);
        return suspended(runFn,monoid);
    }

    public <R2> ReaderWriterState<R,W,S,R2> map(Function<? super T,? extends R2> mapper) {

        return mapState(t -> Tuple.tuple(t.v1, t.v2, mapper.apply(t.v3)));
    }
    public <R2> ReaderWriterState<R,W,S,R2> mapState(Function<Tuple3<W,S,T>, Tuple3<W,S, R2>> fn) {
        return suspended((r,s) -> runState.apply(r,s).map(t3 -> fn.apply(t3)),monoid);
    }
    private static <R,W,S,T> ReaderWriterState<R,W,S,T> suspended(BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> runF,
                                                                  Monoid<W> monoid) {

        return new ReaderWriterState<R, W, S, T>(monoid,(R r ,S s) -> Fn0.suspend(Lambda.λK(()->runF.apply(r,s))));

    }

    public <R2> ReaderWriterState<R,W,S,R2> flatMap(Function<? super T,? extends  ReaderWriterState<R,W,S,R2>> f) {

        return suspended((r,s) -> runState.apply(r, s)
                .flatMap(result -> Free.done(f.apply(result.v3)
                                              .run(r, result.v2)
                                              .map((w2,s2,r2)->Tuple.tuple(monoid.apply(w2,result.v1),s2,r2)

                ))),monoid);
    }


    public <R1, R2, R3, R4> ReaderWriterState<R,W,S,R4> forEach4(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                                      BiFunction<? super T, ? super R1, ReaderWriterState<R,W,S,R2>> value3,
                                                      Fn3<? super T, ? super R1, ? super R2, ReaderWriterState<R,W,S,R3>> value4,
                                                      Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {




        return this.flatMap(in -> {
            
            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                ReaderWriterState<R,W,S,R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {

                    ReaderWriterState<R,W,S,R3> c = value4.apply(in,ina,inb);
                    return c.map(inc->yieldingFunction.apply(in, ina, inb, inc));

                });


            });


        });

    }




    public <R1, R2, R4> ReaderWriterState<R,W,S,R4> forEach3(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                                  BiFunction<? super T, ? super R1, ReaderWriterState<R,W,S,R2>> value3,
                                                  Fn3<? super T, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                ReaderWriterState<R,W,S,R2> b = value3.apply(in,ina);
                return b.map(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }

    public <R1, R4> ReaderWriterState<R,W,S,R4> forEach2(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                              BiFunction<? super T, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.map(in2 -> {
                return yieldingFunction.apply(in, in2);

            });




        });


    }
    public Active<Higher<Higher<Higher<rws,R>,W>,S>,T> allTypeclasses(Monoid<W> monoid){
        return Active.of(this, Instances.definitions(monoid));
    }
    public static <R,W,S,T> ReaderWriterState<R,W,S,T> rws(BiFunction<? super R, ? super S,? extends Tuple3<W,S, T>> runF, Monoid<W> monoid) {

        return new ReaderWriterState<R, W, S, T>(monoid,(r,s) -> Free.done(runF.apply(r,s)));
    }

    public static <R,W,S,T> ReaderWriterState<R,W,S,T> narrowK(Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> hkt){
        return (ReaderWriterState<R,W,S,T>)hkt;
    }
    public static class Instances{

        public static <R,W,S> InstanceDefinitions<Higher<Higher<Higher<rws,R>,W>,S>> definitions(Monoid<W> monoid){
            return new InstanceDefinitions<Higher<Higher<Higher<rws,R>,W>,S>>() {

                @Override
                public <T, R2> Functor<Higher<Higher<Higher<rws, R>, W>, S>> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<Higher<Higher<Higher<rws, R>, W>, S>> unit() {
                    return Instances.unit(monoid);
                }

                @Override
                public <T, R2> Applicative<Higher<Higher<Higher<rws, R>, W>, S>> applicative() {
                    return Instances.applicative(monoid);
                }

                @Override
                public <T, R2> Monad<Higher<Higher<Higher<rws, R>, W>, S>> monad() {
                    return Instances.monad(monoid);
                }

                @Override
                public <T, R2> Maybe<MonadZero<Higher<Higher<Higher<rws, R>, W>, S>>> monadZero() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<Higher<Higher<rws, R>, W>, S>>> monadPlus() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<Higher<Higher<rws, R>, W>, S>>> monadPlus(Monoid<Higher<Higher<Higher<Higher<rws, R>, W>, S>, T>> m) {
                    return Maybe.none();
                }

                @Override
                public <C2, T> Maybe<Traverse<Higher<Higher<Higher<rws, R>, W>, S>>> traverse() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Foldable<Higher<Higher<Higher<rws, R>, W>, S>>> foldable() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Comonad<Higher<Higher<Higher<rws, R>, W>, S>>> comonad() {
                    return Maybe.none();
                }
            }  ;
        }
        public static <R,W,S> Functor<Higher<Higher<Higher<rws,R>,W>,S>> functor(){
            return new  Functor<Higher<Higher<Higher<rws,R>,W>,S>>(){

                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> ds) {
                    ReaderWriterState<R, W, S, T> r = narrowK(ds);
                    return r.map(fn);
                }
            };
        }

        public static <R,W,S>  Pure<Higher<Higher<Higher<rws,R>,W>,S>>unit(Monoid<W> monoid){
            return new Pure<Higher<Higher<Higher<rws,R>,W>,S>>() {

                @Override
                public <T> Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> unit(T value) {
                    return rws((a,s)->Tuple.tuple(monoid.zero(),s,value),monoid);
                }
            };
        }

        public static <R,W,S> Applicative<Higher<Higher<Higher<rws,R>,W>,S>> applicative(Monoid<W> monoid){
            return new Applicative<Higher<Higher<Higher<rws,R>,W>,S>>(){
                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> ap(Higher<Higher<Higher<Higher<rws, R>, W>, S>, ? extends Function<T, R2>> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> apply) {
                    ReaderWriterState<R, W, S, ? extends Function<T, R2>> f = narrowK(fn);
                    ReaderWriterState<R, W, S, T> ap = narrowK(apply);
                    ReaderWriterState<R, W, S, R2> mapped = f.flatMap(x -> ap.map(x));
                    return mapped;
                }

                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> ds) {
                    return Instances.<R,W,S>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> unit(T value) {
                    return Instances.<R,W,S>unit(monoid).unit(value);
                }

            };
        }
        public static <R1,W,S> Monad<Higher<Higher<Higher<rws, R1>,W>,S>> monad(Monoid<W> monoid) {
            return new Monad<Higher<Higher<Higher<rws, R1>,W>,S>>() {


                @Override
                public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
                    ReaderWriterState<R1, W, S, T> r = narrowK(ds);
                    return r.flatMap(fn.andThen(h->narrowK(h)));

                }

                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R2> ap(Higher<Higher<Higher<Higher<rws, R1>, W>, S>, ? extends Function<T, R2>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> apply) {
                    return Instances.<R1,W,S>applicative(monoid).ap(fn,apply);
                }

                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
                    return Instances.<R1, W, S>functor().map(fn, ds);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> unit(T value) {
                    return Instances.<R1,W,S>unit(monoid).unit(value);
                }

            };

        }


    }
}
